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

        System.out.println("evitaDB connected ... discovering catalogs:");
        final Set<String> catalogNames = evita.getCatalogNames();
        if (catalogNames.isEmpty()) {
            System.out.println("\t- No catalogs found");
        } else {
            for (String catalogName : catalogNames) {
                System.out.println("\t- Catalog: " + catalogName);
            }
        }
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
