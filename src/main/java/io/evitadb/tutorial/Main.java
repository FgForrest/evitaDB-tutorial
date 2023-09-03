package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.schema.Cardinality;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

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

        System.out.println("evitaDB connected ... defining ad-doc schema:");
        // clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // define new catalog
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                .updateViaNewSession(evita);
        // define a new product
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    // let's create a Lenovo brand first
                    session.createNewEntity("Brand", 1)
                            .setAttribute("name", Locale.ENGLISH, "Lenovo")
                            .upsertVia(session);

                    // now create an example category tree
                    session.createNewEntity("Category", 10)
                            .setAttribute("name", Locale.ENGLISH, "Electronics")
                            .upsertVia(session);

                    session.createNewEntity("Category", 11)
                            .setAttribute("name", Locale.ENGLISH, "Laptops")
                            // laptops will be a child category of electronics
                            .setParent(10)
                            .upsertVia(session);

                    // finally, create a product
                    session.createNewEntity("Product")
                            // with a few attributes
                            .setAttribute("name", Locale.ENGLISH, "ThinkPad P15 Gen 1")
                            .setAttribute("cores", 8)
                            .setAttribute("graphics", "NVIDIA Quadro RTX 4000 with Max-Q Design")
                            // and price for sale
                            .setPrice(
                                    1, "basic",
                                    Currency.getInstance("USD"),
                                    new BigDecimal("1420"), new BigDecimal("20"), new BigDecimal("1704"),
                                    true
                            )
                            // link it to the manufacturer
                            .setReference(
                                    "brand", "Brand",
                                    Cardinality.EXACTLY_ONE,
                                    1
                            )
                            // and to the laptop category
                            .setReference(
                                    "categories", "Category",
                                    Cardinality.ZERO_OR_MORE,
                                    11
                            )
                            .upsertVia(session);
                }
        );
        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
