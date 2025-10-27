package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.query.require.PriceContentMode;
import io.evitadb.api.requestResponse.data.EntityReferenceContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.math.BigDecimal;
import java.util.Currency;
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
                session.goLiveAndClose();
            }
        );

        System.out.println("- catalog `evita-tutorial` created, now defining entity schemas");

        // define entity schemas by Java interfaces
        evita.updateCatalog(
            "evita-tutorial",
            session -> {
                session.defineEntitySchemaFromModelClass(Brand.class);
                session.defineEntitySchemaFromModelClass(Category.class);
                session.defineEntitySchemaFromModelClass(Product.class);
            }
        );

        System.out.println("- entity schemas defined, now creating some data");

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
                    .setBasicPrice(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, Currency.getInstance("EUR"), 1)
                    .upsertVia(session);
                System.out.println(" ok.");

                return productRef.getPrimaryKey();
            }
        );
    }

    private static void readProductAndPrintToConsole(EvitaContract evita, int productId) {
        System.out.println("- reading product with id " + productId + " ...");

        evita.queryCatalog(
            "evita-tutorial",
            session -> {
                final Product product = session.queryOne(
                        query(
                            filterBy(
                                entityPrimaryKeyInSet(productId),
                                entityLocaleEquals(Locale.ENGLISH),
                                priceInPriceLists("basic"),
                                priceInCurrency("EUR")
                            ),
                            require(
                                entityFetch(
                                    attributeContentAll(),
                                    priceContent(PriceContentMode.RESPECTING_FILTER),
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
                System.out.println("\tSelling price with VAT: " + product.getPriceForSale().priceWithTax() + " " + product.getPriceForSale().currency());
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
