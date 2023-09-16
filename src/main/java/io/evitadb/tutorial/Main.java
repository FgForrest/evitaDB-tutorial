package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.util.Set;

/**
 * This example class shows how to create evitaDB client and connect to the server.
 * It lists all catalogs available on the server and closes the connection.
 *
 * Prior to this class run following command on the command line to start evitaDB server:
 *
 * <pre>
 * docker run --name evitadb -i --rm --net=host index.docker.io/evitadb/evitadb:latest
 * </pre>
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
        // fetch all catalog names present in the evitaDB server
        final Set<String> catalogNames = evita.getCatalogNames();
        if (catalogNames.isEmpty()) {
            // for initial evitaDB server setup, there should be no catalogs present
            System.out.println("\t- No catalogs found");
        } else {
            // in case of evitaDB with existing data, there should be some catalogs present
            for (String catalogName : catalogNames) {
                System.out.println("\t- Catalog: " + catalogName);
            }
        }
        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
