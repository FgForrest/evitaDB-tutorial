package io.evitadb.tutorial;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe container for tracking entity IDs during simulation.
 * This class holds queues of entity primary keys for products, brands, and categories,
 * which are used by the continuous simulation to perform random CRUD operations.
 *
 * <p>All queues use {@link ConcurrentLinkedQueue} for thread-safe access during
 * concurrent simulation operations.</p>
 *
 * @author Jan Novotny (novotny@fg.cz), FG Forrest a.s. (c) 2025
 */
public class EntityIdContainer {

    /**
     * Queue of product primary keys available for update/delete operations.
     */
    public final Queue<Integer> productIds = new ConcurrentLinkedQueue<>();

    /**
     * Queue of brand primary keys available for update/delete operations.
     */
    public final Queue<Integer> brandIds = new ConcurrentLinkedQueue<>();

    /**
     * Queue of category primary keys available for update/delete operations.
     */
    public final Queue<Integer> categoryIds = new ConcurrentLinkedQueue<>();

}
