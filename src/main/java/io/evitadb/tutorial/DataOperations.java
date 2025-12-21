package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.query.require.PriceContentMode;
import io.evitadb.api.requestResponse.data.EntityReferenceContract;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.evitadb.api.query.Query.query;
import static io.evitadb.api.query.QueryConstraints.*;

/**
 * Utility class providing all CRUD operations for evitaDB entities (products, brands, categories).
 * This class encapsulates the data manipulation logic, keeping the Main class focused on
 * application flow and simulation orchestration.
 *
 * <p>All methods use {@link RandomDataProvider} for random data generation and accept
 * {@link EvitaContract} as the first parameter for database access.</p>
 *
 * <p>This is a utility class with a private constructor - all methods are static.</p>
 *
 * @author Jan Novotny (novotny@fg.cz), FG Forrest a.s. (c) 2025
 */
public final class DataOperations {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DataOperations() {
        // Utility class - no instantiation
    }

    // ==================== INITIAL DATA SETUP ====================

    /**
     * Creates the initial set of randomized data including all brands, all categories,
     * and the specified number of products with random associations.
     *
     * <p>This method creates:</p>
     * <ul>
     *   <li>All brands from {@link RandomDataProvider#BRAND_NAMES}</li>
     *   <li>All categories from {@link RandomDataProvider#CATEGORY_NAMES}</li>
     *   <li>The specified number of products with random brand, 1-3 categories, pricing, and attributes</li>
     * </ul>
     *
     * @param evita            the evitaDB client connection
     * @param numberOfProducts the number of products to create
     * @return container with all created entity IDs for simulation use
     */
    public static EntityIdContainer setUpRandomizedData(EvitaContract evita, int numberOfProducts) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();
                    EntityIdContainer container = new EntityIdContainer();

                    System.out.println("- creating " + RandomDataProvider.BRAND_NAMES.length + " brands ...");
                    // Create all brands
                    Map<String, EntityReferenceContract> brandRefs = new HashMap<>();
                    for (String brandName : RandomDataProvider.BRAND_NAMES) {
                        EntityReferenceContract brandRef = session.createNewEntity(BrandEditor.class)
                                .setName(brandName, Locale.ENGLISH)
                                .upsertVia(session);
                        brandRefs.put(brandName, brandRef);
                        container.brandIds.offer(brandRef.getPrimaryKey());
                    }
                    System.out.println(" ok.");

                    System.out.println("- creating " + RandomDataProvider.CATEGORY_NAMES.length + " categories ...");
                    // Create all categories
                    Map<String, EntityReferenceContract> categoryRefs = new HashMap<>();
                    for (String categoryName : RandomDataProvider.CATEGORY_NAMES) {
                        EntityReferenceContract categoryRef = session.createNewEntity(CategoryEditor.class)
                                .setName(categoryName, Locale.ENGLISH)
                                .upsertVia(session);
                        categoryRefs.put(categoryName, categoryRef);
                        container.categoryIds.offer(categoryRef.getPrimaryKey());
                    }
                    System.out.println(" ok.");

                    System.out.println("- creating " + numberOfProducts + " products ...");
                    // Create random products
                    List<EntityReferenceContract> brandRefsList = new ArrayList<>(brandRefs.values());
                    List<EntityReferenceContract> categoryRefsList = new ArrayList<>(categoryRefs.values());

                    for (int i = 0; i < numberOfProducts; i++) {
                        // Random brand
                        EntityReferenceContract randomBrand = brandRefsList.get(random.nextInt(brandRefsList.size()));

                        // Random categories (1-3 categories per product)
                        Set<EntityReferenceContract> randomCategories = new HashSet<>();
                        int numCategories = random.nextInt(3) + 1;
                        for (int j = 0; j < numCategories; j++) {
                            randomCategories.add(categoryRefsList.get(random.nextInt(categoryRefsList.size())));
                        }

                        // Generate random product name
                        String brandName = getBrandNameFromRef(brandRefs, randomBrand);
                        String productPrefix = RandomDataProvider.getRandomProductPrefix(random);
                        String productName = brandName + " " + productPrefix + " " + (random.nextInt(20) + 1);

                        // Random specifications
                        int cores = (random.nextInt(4) + 1) * 2; // 2, 4, 6, 8 cores
                        String graphics = RandomDataProvider.getRandomGraphicsProcessor(random);

                        // Random pricing
                        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextInt(1900)); // $100-$2000
                        BigDecimal taxRate = BigDecimal.valueOf(random.nextInt(25) + 5); // 5-30% tax
                        Currency currency = RandomDataProvider.getRandomCurrency(random);

                        // Create product
                        ProductEditor productEditor = session.createNewEntity(ProductEditor.class)
                                .setName(productName, Locale.ENGLISH)
                                .setCores(cores)
                                .setGraphics(graphics)
                                .setBrandId(randomBrand.getPrimaryKey())
                                .setBasicPrice(basePrice, basePrice, BigDecimal.ZERO, currency, 1);

                        // Add categories
                        for (EntityReferenceContract categoryRef : randomCategories) {
                            productEditor.addCategoryId(categoryRef.getPrimaryKey());
                        }

                        EntityReferenceContract productRef = productEditor.upsertVia(session);
                        container.productIds.offer(productRef.getPrimaryKey());

                        if ((i + 1) % 10 == 0) {
                            System.out.print(".");
                        }
                    }
                    System.out.println(" ok.");

                    return container;
                }
        );
    }

    // ==================== PRODUCT OPERATIONS ====================

    /**
     * Creates a single new product with random attributes, pricing, and associations.
     * The product is assigned a random brand and 1-4 random categories, along with
     * random associated data content from resource files.
     *
     * @param evita the evitaDB client connection
     * @return the primary key of the created product, or -1 if creation failed
     */
    public static int createSingleProduct(EvitaContract evita) {
        try {
            return evita.updateCatalog(
                    "evita-tutorial",
                    session -> {
                        Random random = ThreadLocalRandom.current();

                        // Get random existing brand and category (just references, no full fetch needed)
                        List<EntityReferenceContract> brands = session.query(
                                query(collection("Brand")),
                                EntityReferenceContract.class
                        ).getRecordData();

                        List<EntityReferenceContract> categories = session.query(
                                query(collection("Category")),
                                EntityReferenceContract.class
                        ).getRecordData();

                        if (brands.isEmpty() || categories.isEmpty()) {
                            System.err.println("\nWarning: Cannot create product - brands: "
                                    + brands.size() + ", categories: " + categories.size());
                            return -1;
                        }

                        EntityReferenceContract randomBrand = brands.get(random.nextInt(brands.size()));

                        // Select 1-4 random categories for the new product
                        Set<EntityReferenceContract> selectedCategories = new HashSet<>();
                        int numCategories = random.nextInt(4) + 1;

                        for (int i = 0; i < numCategories && selectedCategories.size() < numCategories; i++) {
                            selectedCategories.add(categories.get(random.nextInt(categories.size())));
                        }

                        // Generate random pricing with more variety
                        BigDecimal basePrice = BigDecimal.valueOf(50 + random.nextInt(2950)); // $50-$3000
                        BigDecimal taxRate = BigDecimal.valueOf(random.nextInt(30) + 5); // 5-35% tax
                        BigDecimal taxAmount = basePrice.multiply(taxRate).divide(BigDecimal.valueOf(100));
                        BigDecimal priceWithTax = basePrice.add(taxAmount);
                        Currency currency = RandomDataProvider.getRandomCurrency(random);

                        // Create product with full randomization
                        ProductEditor productEditor = session.createNewEntity(ProductEditor.class)
                                .setName(RandomDataProvider.generateRandomProductName(random), Locale.ENGLISH)
                                .setCores((random.nextInt(6) + 1) * 2) // 2, 4, 6, 8, 10, 12 cores
                                .setGraphics(RandomDataProvider.getRandomGraphicsProcessor(random))
                                .setBrandId(randomBrand.getPrimaryKey())
                                .setBasicPrice(basePrice, priceWithTax, taxAmount, currency, 1);

                        // Attach randomized example contents from resources
                        String tip = RandomDataProvider.getRandomMarkdownContent(random);
                        String idea = RandomDataProvider.getRandomHtmlContent(random);
                        String metadata = RandomDataProvider.getRandomJsonContent(random);
                        if (tip != null) productEditor.setTip(tip);
                        if (idea != null) productEditor.setIdea(idea);
                        if (metadata != null) productEditor.setMetadata(metadata);

                        // Add all selected categories
                        for (EntityReferenceContract categoryRef : selectedCategories) {
                            productEditor.addCategoryId(categoryRef.getPrimaryKey());
                        }

                        EntityReferenceContract productRef = productEditor.upsertVia(session);
                        return productRef.getPrimaryKey();
                    }
            );
        } catch (Exception e) {
            System.err.println("\nException in createSingleProduct: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Updates a single product with randomly modified attributes.
     * The update applies changes based on probability:
     * <ul>
     *   <li>60% chance: Update name</li>
     *   <li>40% chance: Update CPU cores</li>
     *   <li>30% chance: Update graphics processor</li>
     *   <li>50% chance: Update price</li>
     *   <li>10% chance: Update brand</li>
     *   <li>15% chance: Update categories</li>
     * </ul>
     *
     * @param evita     the evitaDB client connection
     * @param productId the primary key of the product to update
     */
    public static void updateSingleProduct(EvitaContract evita, int productId) {
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();

                    session.getEntity(Product.class, productId, entityFetchAllContent())
                            .ifPresent(product -> {
                                ProductEditor editor = product.openForWrite();

                                // Randomly update different attributes (not all at once)
                                int updateMask = random.nextInt(16); // 0-15 for different combinations

                                // Update name (60% chance)
                                if ((updateMask & 1) != 0 || random.nextDouble() < 0.6) {
                                    editor.setName(RandomDataProvider.generateRandomProductName(random), Locale.ENGLISH);
                                }

                                // Update cores (40% chance)
                                if ((updateMask & 2) != 0 || random.nextDouble() < 0.4) {
                                    editor.setCores((random.nextInt(4) + 1) * 2); // 2, 4, 6, 8 cores
                                }

                                // Update graphics (30% chance)
                                if ((updateMask & 4) != 0 || random.nextDouble() < 0.3) {
                                    editor.setGraphics(RandomDataProvider.getRandomGraphicsProcessor(random));
                                }

                                // Update price (50% chance)
                                if ((updateMask & 8) != 0 || random.nextDouble() < 0.5) {
                                    BigDecimal newPrice = BigDecimal.valueOf(100 + random.nextInt(1900));
                                    BigDecimal taxAmount = newPrice.multiply(
                                            BigDecimal.valueOf(random.nextInt(25) + 5)
                                    ).divide(BigDecimal.valueOf(100));
                                    Currency currency = RandomDataProvider.getRandomCurrency(random);

                                    editor.setBasicPrice(newPrice, newPrice.add(taxAmount), taxAmount, currency, 1);
                                }

                                // Randomly update brand (10% chance)
                                if (random.nextDouble() < 0.1) {
                                    List<EntityReferenceContract> brands = session.query(
                                            query(collection("Brand")),
                                            EntityReferenceContract.class
                                    ).getRecordData();

                                    if (!brands.isEmpty()) {
                                        EntityReferenceContract newBrand = brands.get(random.nextInt(brands.size()));
                                        editor.setBrandId(newBrand.getPrimaryKey());
                                    }
                                }

                                // Randomly update categories (15% chance)
                                if (random.nextDouble() < 0.15) {
                                    List<EntityReferenceContract> categories = session.query(
                                            query(collection("Category")),
                                            EntityReferenceContract.class
                                    ).getRecordData();

                                    if (!categories.isEmpty()) {
                                        int numCategories = random.nextInt(3) + 1; // 1-3 categories
                                        Set<EntityReferenceContract> selectedCategories = new HashSet<>();

                                        for (int i = 0; i < numCategories && selectedCategories.size() < numCategories; i++) {
                                            selectedCategories.add(categories.get(random.nextInt(categories.size())));
                                        }

                                        for (EntityReferenceContract categoryRef : selectedCategories) {
                                            editor.addCategoryId(categoryRef.getPrimaryKey());
                                        }
                                    }
                                }

                                editor.upsertVia(session);
                            });
                }
        );
    }

    /**
     * Deletes a random product from the catalog.
     * The product ID is polled from the queue; if successful, the product is removed.
     *
     * @param evita      the evitaDB client connection
     * @param productIds queue of available product IDs
     */
    public static void deleteRandomProduct(EvitaContract evita, Queue<Integer> productIds) {
        if (productIds.isEmpty()) {
            return;
        }

        Integer productId = productIds.poll();
        if (productId == null) {
            return;
        }

        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    session.deleteEntity(Product.class, productId);
                }
        );
    }

    // ==================== BRAND OPERATIONS ====================

    /**
     * Creates a new brand with a random name (base name + random number suffix).
     *
     * @param evita the evitaDB client connection
     * @return the primary key of the created brand
     */
    public static int createRandomBrand(EvitaContract evita) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();
                    String brandName = RandomDataProvider.getRandomBrandName(random)
                            + " " + random.nextInt(1000);

                    EntityReferenceContract brandRef = session.createNewEntity(BrandEditor.class)
                            .setName(brandName, Locale.ENGLISH)
                            .upsertVia(session);

                    return brandRef.getPrimaryKey();
                }
        );
    }

    /**
     * Updates a random brand with a new name (base name + random number suffix).
     *
     * @param evita    the evitaDB client connection
     * @param brandIds queue of available brand IDs (used for tracking, not selection)
     */
    public static void updateRandomBrand(EvitaContract evita, Queue<Integer> brandIds) {
        if (brandIds.isEmpty()) {
            return;
        }

        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    List<EntityReferenceContract> brands = session.query(
                            query(collection("Brand")),
                            EntityReferenceContract.class
                    ).getRecordData();

                    if (!brands.isEmpty()) {
                        Random random = ThreadLocalRandom.current();
                        EntityReferenceContract brandRef = brands.get(random.nextInt(brands.size()));

                        session.getEntity(Brand.class, brandRef.getPrimaryKey(), entityFetchAllContent())
                                .ifPresent(brand -> {
                                    BrandEditor editor = brand.openForWrite();
                                    String newName = RandomDataProvider.getRandomBrandName(random)
                                            + " " + random.nextInt(100);
                                    editor.setName(newName, Locale.ENGLISH);
                                    editor.upsertVia(session);
                                });
                    }
                }
        );
    }

    /**
     * Deletes a random brand from the catalog.
     * The brand is only deleted if no products reference it. A minimum of 5 brands
     * is always maintained to ensure products can always have brand associations.
     *
     * @param evita    the evitaDB client connection
     * @param brandIds queue of available brand IDs
     */
    public static void deleteRandomBrand(EvitaContract evita, Queue<Integer> brandIds) {
        if (brandIds.size() <= 5) {
            // Keep minimum 5 brands
            return;
        }

        Integer brandId = brandIds.poll();
        if (brandId == null) {
            return;
        }

        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    // Check if any products reference this brand
                    List<EntityReferenceContract> products = session.query(
                            query(
                                    collection("Product"),
                                    filterBy(
                                            referenceHaving("brand", entityPrimaryKeyInSet(brandId))
                                    ),
                                    require(entityFetch())
                            ),
                            EntityReferenceContract.class
                    ).getRecordData();

                    if (products.isEmpty()) {
                        // Safe to delete
                        session.deleteEntity(Brand.class, brandId);
                    } else {
                        // Re-add brand ID back to queue if it has references
                        brandIds.offer(brandId);
                    }
                }
        );
    }

    // ==================== CATEGORY OPERATIONS ====================

    /**
     * Creates a new category with a random name (base name + random number suffix).
     *
     * @param evita the evitaDB client connection
     * @return the primary key of the created category
     */
    public static int createRandomCategory(EvitaContract evita) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();
                    String categoryName = RandomDataProvider.getRandomCategoryName(random)
                            + " " + random.nextInt(1000);

                    EntityReferenceContract categoryRef = session.createNewEntity(CategoryEditor.class)
                            .setName(categoryName, Locale.ENGLISH)
                            .upsertVia(session);

                    return categoryRef.getPrimaryKey();
                }
        );
    }

    /**
     * Updates a random category with a new name and/or parent category.
     * <ul>
     *   <li>70% chance: Update name</li>
     *   <li>30% chance: Change parent (or remove parent)</li>
     * </ul>
     *
     * @param evita       the evitaDB client connection
     * @param categoryIds queue of available category IDs (used for tracking, not selection)
     */
    public static void updateRandomCategory(EvitaContract evita, Queue<Integer> categoryIds) {
        if (categoryIds.isEmpty()) {
            return;
        }

        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    List<EntityReferenceContract> categories = session.query(
                            query(collection("Category")),
                            EntityReferenceContract.class
                    ).getRecordData();

                    if (!categories.isEmpty()) {
                        Random random = ThreadLocalRandom.current();
                        EntityReferenceContract categoryRef = categories.get(random.nextInt(categories.size()));

                        session.getEntity(Category.class, categoryRef.getPrimaryKey(), entityFetchAllContent())
                                .ifPresent(category -> {
                                    CategoryEditor editor = category.openForWrite();

                                    // 70% chance to update name
                                    if (random.nextDouble() < 0.7) {
                                        String newName = RandomDataProvider.getRandomCategoryName(random)
                                                + " " + random.nextInt(100);
                                        editor.setName(newName, Locale.ENGLISH);
                                    }

                                    // 30% chance to change parent (or set to null)
                                    if (random.nextDouble() < 0.3) {
                                        if (random.nextBoolean() && categories.size() > 1) {
                                            // Set random parent (avoid self-reference)
                                            EntityReferenceContract parent;
                                            do {
                                                parent = categories.get(random.nextInt(categories.size()));
                                            } while (parent.getPrimaryKey().equals(categoryRef.getPrimaryKey()));

                                            editor.setParentCategoryId(parent.getPrimaryKey());
                                        } else {
                                            // Remove parent
                                            editor.setParentCategoryId(null);
                                        }
                                    }

                                    editor.upsertVia(session);
                                });
                    }
                }
        );
    }

    /**
     * Deletes a random category from the catalog.
     * The category is only deleted if no products reference it. A minimum of 5 categories
     * is always maintained to ensure products can always have category associations.
     *
     * @param evita       the evitaDB client connection
     * @param categoryIds queue of available category IDs
     */
    public static void deleteRandomCategory(EvitaContract evita, Queue<Integer> categoryIds) {
        if (categoryIds.size() <= 5) {
            // Keep minimum 5 categories
            return;
        }

        Integer categoryId = categoryIds.poll();
        if (categoryId == null) {
            return;
        }

        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    // Check if any products reference this category
                    List<EntityReferenceContract> products = session.query(
                            query(
                                    collection("Product"),
                                    filterBy(
                                            referenceHaving("categories", entityPrimaryKeyInSet(categoryId))
                                    ),
                                    require(entityFetch())
                            ),
                            EntityReferenceContract.class
                    ).getRecordData();

                    if (products.isEmpty()) {
                        // Safe to delete
                        session.deleteEntity(Category.class, categoryId);
                    } else {
                        // Re-add category ID back to queue if it has references
                        categoryIds.offer(categoryId);
                    }
                }
        );
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Reads a product by ID and prints its details to the console.
     * This method is useful for debugging and verifying product data.
     *
     * @param evita     the evitaDB client connection
     * @param productId the primary key of the product to read
     */
    public static void readProductAndPrintToConsole(EvitaContract evita, int productId) {
        System.out.println("- reading product with id " + productId + " ...");

        evita.queryCatalog(
                "evita-tutorial",
                session -> {
                    final Product product = session.queryOne(
                                    query(
                                            filterBy(
                                                    entityPrimaryKeyInSet(productId),
                                                    entityLocaleEquals(Locale.ENGLISH),
                                                    priceInPriceLists("basic"),
                                                    priceInCurrency("EUR")
                                            ),
                                            require(
                                                    entityFetch(
                                                            attributeContentAll(),
                                                            priceContent(PriceContentMode.RESPECTING_FILTER),
                                                            referenceContent(
                                                                    Product.REFERENCE_BRAND,
                                                                    entityFetch(attributeContentAll())
                                                            ),
                                                            referenceContent(
                                                                    Product.REFERENCE_CATEGORIES,
                                                                    entityFetch(attributeContentAll())
                                                            )
                                                    )
                                            )
                                    ),
                                    Product.class
                            )
                            .orElseThrow(
                                    () -> new IllegalStateException("Product with id " + productId + " not found.")
                            );

                    System.out.println("\tProduct name: " + product.getName());
                    System.out.println("\tSelling price with VAT: " + product.getPriceForSale().priceWithTax()
                            + " " + product.getPriceForSale().currency());
                    System.out.println("\tProduct cores: " + product.getCores());
                    System.out.println("\tProduct graphics: " + product.getGraphics());
                    System.out.println("\tProduct brand: " + product.getBrand().getName());
                    System.out.println(
                            "\tProduct categories: " +
                                    product.getCategories()
                                            .stream()
                                            .map(Category::getName)
                                            .reduce((a, b) -> a + ", " + b)
                                            .orElse("<none>")
                    );
                }
        );
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Looks up the brand name from a map of brand references.
     *
     * @param brandRefs map of brand names to their entity references
     * @param targetRef the target brand reference to look up
     * @return the brand name, or "Unknown Brand" if not found
     */
    private static String getBrandNameFromRef(
            Map<String, EntityReferenceContract> brandRefs,
            EntityReferenceContract targetRef
    ) {
        return brandRefs.entrySet().stream()
                .filter(entry -> entry.getValue().getPrimaryKey().equals(targetRef.getPrimaryKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Unknown Brand");
    }

}
