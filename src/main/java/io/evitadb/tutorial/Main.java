package io.evitadb.tutorial;

import io.evitadb.api.CommitProgress;
import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.cdc.ChangeCaptureContent;
import io.evitadb.api.requestResponse.cdc.ChangeSystemCaptureRequest;
import io.evitadb.api.requestResponse.data.EntityReferenceContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static io.evitadb.api.query.Query.query;
import static io.evitadb.api.query.QueryConstraints.*;

/**
 * This example class demonstrates Change Data Capture (CDC) functionality in evitaDB.
 * It shows how to:
 * <ul>
 *     <li>Connect to evitaDB server and register CDC listeners at system and catalog levels</li>
 *     <li>Create a catalog and entity schemas</li>
 *     <li>Create and update entities asynchronously with commit progress tracking</li>
 *     <li>Monitor CDC events for all database changes</li>
 * </ul>
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
public class Main {

    /**
     * Main entry point demonstrating CDC functionality in evitaDB.
     * Sets up CDC listeners, creates a catalog with entity schemas, creates and updates
     * a product entity, and monitors all changes through CDC events.
     *
     * @param args command line arguments (not used)
     * @throws Exception if any error occurs during execution
     */
    public static void main(String[] args) throws Exception {
        final EvitaContract evita = new EvitaClient(
            EvitaClientConfiguration.builder()
                .host("localhost")
                .build()
        );

        final SystemCaptureConsoleWritingSubscriber engineSubscription = new SystemCaptureConsoleWritingSubscriber(evita);
        evita.registerSystemChangeCapture(
            ChangeSystemCaptureRequest.builder()
                .content(ChangeCaptureContent.BODY)
                .build()
        ).subscribe(engineSubscription);

        System.out.println("evitaDB connected ... defining schema");
        // clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // define new catalog
        evita.defineCatalog("evita-tutorial")
            .withDescription("This is a tutorial catalog.")
            .updateViaNewSession(evita);

        System.out.println("- catalog `evita-tutorial` created, now defining entity schemas");
        // define entity schemas by Java interfaces
        evita.updateCatalog(
            "evita-tutorial",
            session -> {
                session.defineEntitySchemaFromModelClass(Brand.class);
                session.defineEntitySchemaFromModelClass(Category.class);
                session.defineEntitySchemaFromModelClass(Product.class);

                System.out.println("- entity schemas defined, switching to transactional state");

                // we need to transition to "live" = transactional state
                session.goLiveAndClose();
            }
        );
        System.out.println("- now creating some data");

        // create some data via custom contracts
        final int productId = setUpNewProduct(evita);

        // if we want to update the product, we can do it like this
        updateExistingProduct(evita, productId);

        // finally delete the catalog again to clean up
        System.out.println("- deleting catalog `evita-tutorial` to clean up ...");
        evita.deleteCatalogIfExists("evita-tutorial");

        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

    /**
     * Creates a new product with associated brand and category entities asynchronously.
     * This method demonstrates:
     * <ul>
     *     <li>Asynchronous catalog updates using {@code updateCatalogAsync}</li>
     *     <li>Creating related entities (brand, category) and linking them to the product</li>
     *     <li>Tracking commit progress through various stages (conflict resolution, WAL append, visibility)</li>
     *     <li>Waiting for changes to become visible before reading the created entity</li>
     * </ul>
     *
     * @param evita the evitaDB client instance
     * @return the primary key of the newly created product
     */
    private static int setUpNewProduct(EvitaContract evita) {
        final AtomicInteger productId = new AtomicInteger();
        final CommitProgress commitProgress = evita.updateCatalogAsync(
            "evita-tutorial",
            session -> {
                System.out.print("- creating Apple brand ...");
                // create a new brand
                final EntityReferenceContract appleBrandRef = session.createNewEntity(BrandEditor.class)
                    .setName("Apple", Locale.ENGLISH)
                    .upsertVia(session);
                System.out.println(" ok.");

                System.out.print("- creating Cell phones category ...");
                // create a new category
                final EntityReferenceContract cellPhonesRef = session.createNewEntity(CategoryEditor.class)
                    .setName("Cell phones", Locale.ENGLISH)
                    .upsertVia(session);
                System.out.println(" ok.");

                System.out.print("- creating iPhone 12 product ...");
                // create a new product linked to the brand and category
                final EntityReferenceContract productRef = session.createNewEntity(ProductEditor.class)
                    .setName("iPhone 12", Locale.ENGLISH)
                    .setCores(6)
                    .setGraphics("A14 Bionic")
                    .setBrandId(appleBrandRef.getPrimaryKey())
                    .addCategoryId(cellPhonesRef.getPrimaryKey())
                    .upsertVia(session);
                System.out.println(" ok.");

                productId.set(productRef.getPrimaryKey());
            }
        );

        commitProgress.onConflictResolved()
            .thenAccept(
                commitVersions -> System.out.println(
                    "- tx accepted, changes will be visible in version: " + commitVersions.catalogVersion() + "."
                )
            );
        commitProgress.onWalAppended()
            .thenAccept(commitVersions -> System.out.println("- tx written to WAL."));

        // wait for the commit to be visible and return assigned productId
        commitProgress.onChangesVisible()
            .thenAcceptAsync(
                commitVersions -> {
                    System.out.println("- tx changes visible to all now.");
                    // now we can safely read the product
                    evita.queryCatalog(
                        "evita-tutorial",
                        session -> {
                            // now read the updated product again and print its data to console
                            readProductAndPrintToConsole(evita, productId.get());
                        }
                    );
                }
            )
            // wait until previous block is finished
            .toCompletableFuture()
            .join();

        return productId.get();
    }

    /**
     * Queries and displays a product entity with all its associated data.
     * Fetches the product along with its brand and category references, including
     * all attributes in English locale, and prints the information to the console.
     *
     * @param evita the evitaDB client instance
     * @param productId the primary key of the product to retrieve
     */
    private static void readProductAndPrintToConsole(EvitaContract evita, int productId) {
        System.out.println("- reading product with id " + productId + " ...");
        evita.queryCatalog(
            "evita-tutorial",
            session -> {
                final Product product = session.queryOne(
                        query(
                            filterBy(
                                entityPrimaryKeyInSet(productId),
                                entityLocaleEquals(Locale.ENGLISH)
                            ),
                            require(
                                entityFetch(
                                    attributeContentAll(),
                                    referenceContent(
                                        Product.REFERENCE_BRAND,
                                        entityFetch(attributeContentAll())
                                    ),
                                    referenceContent(
                                        Product.REFERENCE_CATEGORIES,
                                        entityFetch(attributeContentAll())
                                    )
                                )
                            )
                        ),
                        Product.class
                    )
                    .orElseThrow(
                        () -> new IllegalStateException("Product with id " + productId + " not found.")
                    );

                System.out.println("\tProduct name: " + product.getName());
                System.out.println("\tProduct cores: " + product.getCores());
                System.out.println("\tProduct graphics: " + product.getGraphics());
                System.out.println("\tProduct brand: " + product.getBrand().getName());
                System.out.println(
                    "\tProduct categories: " +
                        product.getCategories()
                            .stream()
                            .map(Category::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("<none>")
                );
            }
        );
    }

    /**
     * Updates an existing product entity by modifying its name and attributes.
     * Demonstrates the pattern of fetching an entity, opening it for modification,
     * making changes, and persisting them back to the database.
     *
     * @param evita the evitaDB client instance
     * @param productId the primary key of the product to update
     */
    private static void updateExistingProduct(EvitaContract evita, int productId) {
        evita.updateCatalog(
            "evita-tutorial",
            session -> {
                System.out.print("- updating iPhone 12 product to Pro ...");
                session.getEntity(
                        Product.class, productId, entityFetchAllContent()
                    )
                    .orElseThrow()
                    .openForWrite()
                    .setName("iPhone 12 Pro", Locale.ENGLISH)
                    .setCores(8)
                    .upsertVia(session);
                System.out.println(" ok.");
            }
        );
    }

}
