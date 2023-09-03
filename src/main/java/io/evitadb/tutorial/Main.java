package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.util.Set;

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

        System.out.println("evitaDB connected ... creating new catalog `evita-tutorial`:");
        // let's create a new catalog with a description
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                .updateViaNewSession(evita);

        System.out.println("Catalog `evita-tutorial` created, listing all catalogs:");
        // list all catalogs
        final Set<String> catalogNames = evita.getCatalogNames();
        if (catalogNames.isEmpty()) {
            // because we've just defined a new catalog this should never happen
            System.out.println("\t- No catalogs found");
        } else {
            // list all catalogs including the newly created one
            for (String catalogName : catalogNames) {
                System.out.println("\t- Catalog: " + catalogName);
            }
        }
        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
