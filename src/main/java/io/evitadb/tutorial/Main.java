package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

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

        System.out.println("evitaDB connected ... defining ad-doc schema:");
        // clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // define new catalog
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                .updateViaNewSession(evita);

        System.out.println("- catalog created: `evita-tutorial`");

        // define entity schemas by Java interfaces
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    session.defineEntitySchemaFromModelClass(Brand.class);
                    session.defineEntitySchemaFromModelClass(Category.class);
                    session.defineEntitySchemaFromModelClass(Product.class);
                }
        );

        System.out.println("- entity schemas defined: `Brand`, `Category`, `Product`");

        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
