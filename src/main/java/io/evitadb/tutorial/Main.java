package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.data.structure.EntityReference;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.util.Locale;

import static io.evitadb.api.query.Query.query;
import static io.evitadb.api.query.QueryConstraints.*;

/**
 * This example class shows how to create evitaDB client and connect to the server.
 * It lists all catalogs available on the server and closes the connection.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
public class Main {

    public static void main(String[] args) throws Exception {
        final EvitaContract evita = new EvitaClient(
                EvitaClientConfiguration.builder()
                        .host("localhost")
                        .port(5556)
                        .build()
        );

        System.out.println("evitaDB connected ... defining schema");
        // clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // define new catalog
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                .updateViaNewSession(evita);

        // define entity schemas by Java interfaces
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    session.defineEntitySchemaFromModelClass(Brand.class);
                    session.defineEntitySchemaFromModelClass(Category.class);
                    session.defineEntitySchemaFromModelClass(Product.class);

                    // TODO: temporary workaround until evitaLab supports warm-up state
                    session.goLiveAndClose();
                }
        );

        // create some data via custom contracts
        final int productId = setUpNewProduct(evita);

        // now read the product and print its data to console
        readProductAndPrintToConsole(evita, productId);

        // if we want to update the product, we can do it like this
        updateExistingProduct(evita, productId);

        // now read the updated product again and print its data to console
        readProductAndPrintToConsole(evita, productId);

        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

    private static int setUpNewProduct(EvitaContract evita) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    System.out.print("evitaDB ... creating Apple brand ...");
                    // create a new brand
                    final EntityReference appleBrandRef = session.createNewEntity(BrandEditor.class)
                            .setName("Apple", Locale.ENGLISH)
                            .upsertVia(session);
                    System.out.println(" ok.");

                    System.out.print("evitaDB ... creating Cell phones category ...");
                    // create a new category
                    final EntityReference cellPhonesRef = session.createNewEntity(CategoryEditor.class)
                            .setName("Cell phones", Locale.ENGLISH)
                            .upsertVia(session);
                    System.out.println(" ok.");

                    System.out.print("evitaDB ... creating iPhone 12 product ...");
                    // create a new product linked to the brand and category
                    final EntityReference productRef = session.createNewEntity(ProductEditor.class)
                            .setName("iPhone 12", Locale.ENGLISH)
                            .setCores(6)
                            .setGraphics("A14 Bionic")
                            .setBrandId(appleBrandRef.getPrimaryKey())
                            .addCategoryId(cellPhonesRef.getPrimaryKey())
                            .upsertVia(session);
                    System.out.println(" ok.");

                    return productRef.getPrimaryKey();
                }
        );
    }

    private static void readProductAndPrintToConsole(EvitaContract evita, int productId) {
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

                System.out.println("Product name: " + product.getName());
                System.out.println("Product cores: " + product.getCores());
                System.out.println("Product graphics: " + product.getGraphics());
                System.out.println("Product brand: " + product.getBrand().getName());
                System.out.println(
                        "Product categories: " +
                                product.getCategories()
                                        .stream()
                                        .map(Category::getName)
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("<none>")
                );
            }
    );
    }

    private static void updateExistingProduct(EvitaContract evita, int productId) {
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    System.out.print("evitaDB ... updating iPhone 12 product to Pro ...");
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
