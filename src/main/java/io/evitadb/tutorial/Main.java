package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.EvitaSessionContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main entry point for the evitaDB tutorial application.
 * This class demonstrates how to connect to an evitaDB server, define entity schemas,
 * create initial randomized data, and run a continuous simulation of CRUD operations.
 *
 * <p>The application performs the following steps:</p>
 * <ol>
 *   <li>Connects to a local evitaDB server</li>
 *   <li>Creates or recreates the "evita-tutorial" catalog</li>
 *   <li>Defines entity schemas for {@link Brand}, {@link Category}, and {@link Product}</li>
 *   <li>Populates initial data using {@link DataOperations#setUpRandomizedData}</li>
 *   <li>Runs continuous simulation until interrupted (Ctrl+C)</li>
 * </ol>
 *
 * <p>The simulation randomly performs CRUD operations on entities with the following
 * probability distribution:</p>
 * <ul>
 *   <li>45% - Update existing product</li>
 *   <li>35% - Create new product</li>
 *   <li>10% - Delete product (if more than 50 exist)</li>
 *   <li>3% - Update brand</li>
 *   <li>3% - Update category</li>
 *   <li>1% - Create brand</li>
 *   <li>2% - Create category</li>
 *   <li>1% - Delete brand or category</li>
 * </ul>
 *
 * @author Jan Novotny (novotny@fg.cz), FG Forrest a.s. (c) 2025
 * @see DataOperations for CRUD operation implementations
 * @see RandomDataProvider for random data generation
 */
public class Main {

    /**
     * Application entry point. Connects to evitaDB, sets up the catalog and schemas,
     * creates initial data, and starts the continuous simulation.
     *
     * @param args command line arguments (not used)
     * @throws Exception if connection or catalog setup fails
     */
    public static void main(String[] args) throws Exception {
        final EvitaContract evita = new EvitaClient(
                EvitaClientConfiguration.builder()
                        .host("localhost")
                        .build()
        );

        System.out.println("evitaDB connected ... defining schema");
        // Clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // Define new catalog
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                .updateViaNewSession(evita);

        // Switch catalog to live mode
        evita.updateCatalog(
                "evita-tutorial",
                EvitaSessionContract::goLiveAndClose
        );

        System.out.println("- catalog `evita-tutorial` created, now defining entity schemas");

        // Define entity schemas by Java interfaces
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    session.defineEntitySchemaFromModelClass(Brand.class);
                    session.defineEntitySchemaFromModelClass(Category.class);
                    session.defineEntitySchemaFromModelClass(Product.class);
                }
        );

        System.out.println("- entity schemas defined, now creating randomized data");

        // Create lots of randomized data with session management
        final EntityIdContainer container = DataOperations.setUpRandomizedData(evita, 100);

        System.out.println("- created " + container.productIds.size() + " products, "
                + container.brandIds.size() + " brands, "
                + container.categoryIds.size() + " categories");

        // Run continuous simulation (blocks until Ctrl+C)
        runContinuousSimulation(evita, container);

        // Close the connection (after shutdown hook)
        evita.close();
        System.out.println("evitaDB connection closed");
    }

    /**
     * Runs a continuous simulation of random CRUD operations until interrupted.
     * The simulation executes one operation per second and prints progress every 10 operations.
     * A shutdown hook is registered to print final statistics when the application is terminated.
     *
     * @param evita     the evitaDB client connection
     * @param container container holding entity ID queues for the simulation
     */
    private static void runContinuousSimulation(EvitaContract evita, EntityIdContainer container) {
        final OperationStatistics stats = new OperationStatistics();
        final AtomicBoolean running = new AtomicBoolean(true);

        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutting down gracefully...");
            running.set(false);
            stats.printFinalStatistics();
        }));

        System.out.println("\n=== Starting continuous simulation (Press Ctrl+C to stop) ===\n");

        while (running.get()) {
            try {
                performRandomOperation(evita, container, stats);

                // Print progress every 10 operations
                if (stats.getTotalOperations() % 10 == 0) {
                    stats.printProgress(
                            container.productIds.size(),
                            container.brandIds.size(),
                            container.categoryIds.size()
                    );
                }

                // Sleep for 1 second
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                System.out.println("\nInterrupted, shutting down...");
                running.set(false);
                break;
            } catch (Exception e) {
                System.err.println("\nOperation failed: " + e.getMessage());
                stats.recordError();
            }
        }
    }

    /**
     * Performs a single random operation based on weighted probability distribution.
     * The operation type is selected randomly and delegated to the appropriate method
     * in {@link DataOperations}.
     *
     * @param evita     the evitaDB client connection
     * @param container container holding entity ID queues
     * @param stats     statistics tracker for recording the operation
     */
    private static void performRandomOperation(
            EvitaContract evita,
            EntityIdContainer container,
            OperationStatistics stats
    ) {
        Random random = ThreadLocalRandom.current();
        int operationType = random.nextInt(100);

        try {
            // 90% operations focus on products (main transactional data)
            if (operationType < 45) {
                // 45% - Update product
                if (!container.productIds.isEmpty()) {
                    Integer productId = container.productIds.peek();
                    if (productId != null) {
                        DataOperations.updateSingleProduct(evita, productId);
                        stats.recordProductUpdate();
                    }
                }
            } else if (operationType < 80) {
                // 35% - Create product (much higher to grow the catalog)
                try {
                    int newProductId = DataOperations.createSingleProduct(evita);
                    if (newProductId > 0) {
                        container.productIds.offer(newProductId);
                        stats.recordProductCreate();
                    } else {
                        System.err.println("\nWarning: Failed to create product (no brands/categories available?)");
                    }
                } catch (Exception e) {
                    System.err.println("\nError creating product: " + e.getMessage());
                    stats.recordError();
                }
            } else if (operationType < 90) {
                // 10% - Delete product (only if we have more than 50 products)
                if (container.productIds.size() > 50) {
                    DataOperations.deleteRandomProduct(evita, container.productIds);
                    stats.recordProductDelete();
                }
                // 10% operations for master data (brands & categories)
            } else if (operationType < 93) {
                // 3% - Update brand
                DataOperations.updateRandomBrand(evita, container.brandIds);
                stats.recordBrandUpdate();
            } else if (operationType < 96) {
                // 3% - Update category
                DataOperations.updateRandomCategory(evita, container.categoryIds);
                stats.recordCategoryUpdate();
            } else if (operationType < 97) {
                // 1% - Create brand (rarely - master data)
                int newBrandId = DataOperations.createRandomBrand(evita);
                if (newBrandId > 0) {
                    container.brandIds.offer(newBrandId);
                    stats.recordBrandCreate();
                }
            } else if (operationType < 99) {
                // 2% - Create category (occasionally - master data)
                int newCategoryId = DataOperations.createRandomCategory(evita);
                if (newCategoryId > 0) {
                    container.categoryIds.offer(newCategoryId);
                    stats.recordCategoryCreate();
                }
            } else {
                // 1% - Delete brand or category (very rarely)
                if (random.nextBoolean()) {
                    DataOperations.deleteRandomBrand(evita, container.brandIds);
                    stats.recordBrandDelete();
                } else {
                    DataOperations.deleteRandomCategory(evita, container.categoryIds);
                    stats.recordCategoryDelete();
                }
            }
        } catch (Exception e) {
            stats.recordError();
            throw e;
        }
    }

}
