package io.evitadb.tutorial;


import io.evitadb.api.requestResponse.cdc.ChangeCatalogCapture;
import io.evitadb.utils.ConsoleWriter;

import java.io.Closeable;
import java.util.concurrent.Flow;

/**
 * A subscriber implementation that writes catalog-level Change Data Capture (CDC) events to the console.
 * This class subscribes to a stream of {@link ChangeCatalogCapture} events for a specific catalog
 * and processes them one at a time, printing each received event to the console. It implements
 * a basic back-pressure mechanism by requesting one item at a time.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2025
 */
class CatalogCaptureConsoleWritingSubscriber implements Flow.Subscriber<ChangeCatalogCapture>, Closeable {
    private final String catalogName;
    private Flow.Subscription subscription;

    public CatalogCaptureConsoleWritingSubscriber(String catalogName) {
        this.catalogName = catalogName;
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
     * Processes each received CDC event by printing it to the console.
     * After processing, requests the next item maintaining the flow.
     *
     * @param item the CDC event to process
     */
    @Override
    public void onNext(ChangeCatalogCapture item) {
        // print the received item
        ConsoleWriter.writeLine(this.catalogName + ": received change capture: " + item, ConsoleWriter.ConsoleColor.BRIGHT_GREEN);
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
        ConsoleWriter.writeLine("Change capture stream of catalog `" + this.catalogName + "` completed.", ConsoleWriter.ConsoleColor.BRIGHT_YELLOW);
    }

    @Override
    public void close() {
        // close is called when, subscription is canceled
        ConsoleWriter.writeLine("Catalog change capture of catalog `" + this.catalogName + "` subscription closed.", ConsoleWriter.ConsoleColor.BRIGHT_YELLOW);
    }

}
