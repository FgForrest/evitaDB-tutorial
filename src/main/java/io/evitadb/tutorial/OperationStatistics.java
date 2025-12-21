package io.evitadb.tutorial;

/**
 * Thread-safe statistics tracker for continuous simulation operations.
 * This class maintains counters for all CRUD operations performed on products,
 * brands, and categories, as well as error counts.
 *
 * <p>All mutator and accessor methods are synchronized to ensure thread safety
 * during concurrent simulation operations.</p>
 *
 * @author Jan Novotny (novotny@fg.cz), FG Forrest a.s. (c) 2025
 */
public class OperationStatistics {

    private long totalOperations = 0;
    private long productUpdates = 0;
    private long productCreates = 0;
    private long productDeletes = 0;
    private long brandUpdates = 0;
    private long brandCreates = 0;
    private long brandDeletes = 0;
    private long categoryUpdates = 0;
    private long categoryCreates = 0;
    private long categoryDeletes = 0;
    private long errors = 0;

    /**
     * Records a product update operation.
     */
    public synchronized void recordProductUpdate() {
        productUpdates++;
        totalOperations++;
    }

    /**
     * Records a product create operation.
     */
    public synchronized void recordProductCreate() {
        productCreates++;
        totalOperations++;
    }

    /**
     * Records a product delete operation.
     */
    public synchronized void recordProductDelete() {
        productDeletes++;
        totalOperations++;
    }

    /**
     * Records a brand update operation.
     */
    public synchronized void recordBrandUpdate() {
        brandUpdates++;
        totalOperations++;
    }

    /**
     * Records a brand create operation.
     */
    public synchronized void recordBrandCreate() {
        brandCreates++;
        totalOperations++;
    }

    /**
     * Records a brand delete operation.
     */
    public synchronized void recordBrandDelete() {
        brandDeletes++;
        totalOperations++;
    }

    /**
     * Records a category update operation.
     */
    public synchronized void recordCategoryUpdate() {
        categoryUpdates++;
        totalOperations++;
    }

    /**
     * Records a category create operation.
     */
    public synchronized void recordCategoryCreate() {
        categoryCreates++;
        totalOperations++;
    }

    /**
     * Records a category delete operation.
     */
    public synchronized void recordCategoryDelete() {
        categoryDeletes++;
        totalOperations++;
    }

    /**
     * Records an error during operation execution.
     */
    public synchronized void recordError() {
        errors++;
    }

    /**
     * Returns the total number of operations performed.
     *
     * @return total operations count
     */
    public synchronized long getTotalOperations() {
        return totalOperations;
    }

    /**
     * Prints current progress to the console including operation counts and entity totals.
     *
     * @param productCount  current number of products
     * @param brandCount    current number of brands
     * @param categoryCount current number of categories
     */
    public synchronized void printProgress(int productCount, int brandCount, int categoryCount) {
        System.out.printf(
                "\nOperation #%d | Products: %d | Brands: %d | Categories: %d%n",
                totalOperations, productCount, brandCount, categoryCount
        );
        System.out.printf(
                "  Product ops: %d updated, %d created, %d deleted%n",
                productUpdates, productCreates, productDeletes
        );
        System.out.printf(
                "  Brand ops: %d updated, %d created, %d deleted%n",
                brandUpdates, brandCreates, brandDeletes
        );
        System.out.printf(
                "  Category ops: %d updated, %d created, %d deleted%n",
                categoryUpdates, categoryCreates, categoryDeletes
        );
        if (errors > 0) {
            System.out.printf("  Errors: %d%n", errors);
        }
    }

    /**
     * Prints final statistics summary to the console.
     * Called when the simulation is shutting down.
     */
    public synchronized void printFinalStatistics() {
        System.out.println("\n=== Final Statistics ===");
        System.out.printf("Total operations: %d%n", totalOperations);
        System.out.printf("Products: %d created, %d deleted, %d updated%n",
                productCreates, productDeletes, productUpdates);
        System.out.printf("Brands: %d created, %d deleted, %d updated%n",
                brandCreates, brandDeletes, brandUpdates);
        System.out.printf("Categories: %d created, %d deleted, %d updated%n",
                categoryCreates, categoryDeletes, categoryUpdates);
        System.out.printf("Errors: %d%n", errors);
    }

}
