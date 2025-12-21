package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.cdc.ChangeCaptureContent;
import io.evitadb.api.requestResponse.cdc.ChangeSystemCaptureRequest;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

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
public class ChangeCaptureSetup {

    /**
     * ChangeCaptureSetup entry point demonstrating CDC functionality in evitaDB.
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

        CompletableFuture.anyOf(
            engineSubscription.onClose(),
            CompletableFuture.runAsync(() -> {
                // wait for user input before closing
                System.out.println("\nPress enter to close the connection...");
                try {
                    System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
        ).join();

        // close the connection
        evita.close();
        System.out.println("evitaDB connection closed");
    }

}
