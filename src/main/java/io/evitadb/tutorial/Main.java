package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.schema.Cardinality;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

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

        System.out.println("evitaDB connected ... defining ad-doc schema:");
        // clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // define new catalog
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                // define brand schema
                .withEntitySchema(
                        "Brand",
                        whichIs -> whichIs.withDescription("A manufacturer of products.")
                                .withAttribute(
                                        "name", String.class,
                                        thatIs -> thatIs.localized().filterable().sortable()
                                )
                )
                // define category schema
                .withEntitySchema(
                        "Category",
                        whichIs -> whichIs.withDescription("A category of products.")
                                .withAttribute(
                                        "name", String.class,
                                        thatIs -> thatIs.localized().filterable().sortable()
                                )
                                .withHierarchy()
                )
                // define product schema
                .withEntitySchema(
                        "Product",
                        whichIs -> whichIs.withDescription("A product in inventory.")
                                .withAttribute(
                                        "name", String.class,
                                        thatIs -> thatIs.localized().filterable().sortable()
                                )
                                .withAttribute(
                                        "cores", Integer.class,
                                        thatIs -> thatIs.withDescription("Number of CPU cores.")
                                                .filterable()
                                )
                                .withAttribute(
                                        "graphics", String.class,
                                        thatIs -> thatIs.withDescription("Graphics card.")
                                                .filterable()
                                )
                                .withPrice()
                                .withReferenceToEntity(
                                        "brand", "Brand", Cardinality.EXACTLY_ONE,
                                        thatIs -> thatIs.indexed()
                                )
                                .withReferenceToEntity(
                                        "categories", "Category", Cardinality.ZERO_OR_MORE,
                                        thatIs -> thatIs.indexed()
                                )
                )
                // and now push all the definitions (mutations) to the server
                .updateViaNewSession(evita);
        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
