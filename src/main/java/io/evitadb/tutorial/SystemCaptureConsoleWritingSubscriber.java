package io.evitadb.tutorial;


import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.cdc.ChangeCaptureContent;
import io.evitadb.api.requestResponse.cdc.ChangeCatalogCaptureRequest;
import io.evitadb.api.requestResponse.cdc.ChangeSystemCapture;
import io.evitadb.api.requestResponse.schema.mutation.engine.CreateCatalogSchemaMutation;
import io.evitadb.api.requestResponse.schema.mutation.engine.MakeCatalogAliveMutation;
import io.evitadb.utils.ConsoleWriter;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.concurrent.Flow;

/**
 * A subscriber implementation that writes Change Data Capture (CDC) events to the console.
 * This class subscribes to a stream of {@link ChangeSystemCapture} events and processes them
 * one at a time, printing each received event to the console. It implements a basic
 * back-pressure mechanism by requesting one item at a time.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2025
 */
class SystemCaptureConsoleWritingSubscriber implements Flow.Subscriber<ChangeSystemCapture>, Closeable {
    private final EvitaContract evita;
    private Flow.Subscription subscription;

    public SystemCaptureConsoleWritingSubscriber(@Nonnull EvitaContract evita) {
        this.evita = evita;
    }

    /**
     * Called when the subscriber is first subscribed to the publisher.
     * Stores the subscription for later use and requests the first item.
     *
     * @param subscription the subscription to store
     */
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        // request the first item
        subscription.request(1);
    }

    /**
     * Processes each received system-level CDC event by printing it to the console.
     * When a {@link CreateCatalogSchemaMutation} is detected, it automatically subscribes
     * to the newly created catalog's change stream using a {@link CatalogCaptureConsoleWritingSubscriber}.
     * After processing, requests the next item maintaining the flow.
     *
     * @param item the CDC event to process
     */
    @Override
    public void onNext(ChangeSystemCapture item) {
        if (item.body() instanceof MakeCatalogAliveMutation mcam) {
            ConsoleWriter.writeLine("-> New catalog is alive: " + mcam.getCatalogName() + ", registering CDC stream for it", ConsoleWriter.ConsoleColor.BRIGHT_GREEN);
            evita.updateCatalog(
                mcam.getCatalogName(),
                session -> {
                    session.registerChangeCatalogCapture(
                        ChangeCatalogCaptureRequest.builder()
                            .content(ChangeCaptureContent.BODY)
                            .build()
                    ).subscribe(new CatalogCaptureConsoleWritingSubscriber(mcam.getCatalogName()));
                }
            );
        } else {
            // print the received item
            System.out.println("Received change capture: " + item);
        }
        // request the next item
        subscription.request(1);
    }

    /**
     * Handles any errors that occur during the subscription by printing
     * the error message to the console's error stream.
     *
     * @param throwable the error that occurred
     */
    @Override
    public void onError(Throwable throwable) {
        // print the error
        ConsoleWriter.writeLine("Error occurred: " + throwable.getMessage(), ConsoleWriter.ConsoleColor.BRIGHT_RED);
    }

    /**
     * Called when the publisher signals there will be no more items.
     * Prints a completion message to the console.
     */
    @Override
    public void onComplete() {
        // print completion message
        ConsoleWriter.writeLine("Change capture stream completed.", ConsoleWriter.ConsoleColor.BRIGHT_YELLOW);
    }

    @Override
    public void close() {
        // close is called when, subscription is canceled
        ConsoleWriter.writeLine("System change capture subscription closed.", ConsoleWriter.ConsoleColor.BRIGHT_YELLOW);
    }
}
