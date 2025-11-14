package io.evitadb.tutorial;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.query.require.PriceContentMode;
import io.evitadb.api.requestResponse.data.EntityReferenceContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.evitadb.api.query.Query.query;
import static io.evitadb.api.query.QueryConstraints.*;

/**
 * This example class shows how to create evitaDB client and connect to the server.
 * It generates randomized data for testing purposes.
 *
 * @author Jan NovotnÃ½ (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
public class Main {

    // Random data arrays - expanded for better variety
    private static final String[] BRAND_NAMES = {
            // Established tech brands
            "Apple", "Samsung", "Google", "Sony", "Microsoft", "LG", "Huawei",
            "OnePlus", "Xiaomi", "Motorola", "Nokia", "Oppo", "Vivo", "Realme", "Nothing",
            // Additional tech brands
            "Asus", "Acer", "Dell", "HP", "Lenovo", "MSI", "Razer", "Alienware",
            "Intel", "AMD", "Nvidia", "Corsair", "Logitech", "Anker", "Belkin",
            "JBL", "Bose", "Sennheiser", "Audio-Technica", "HyperX", "SteelSeries",
            "Xiaomi", "Honor", "Redmi", "Poco", "iQOO", "ZTE", "TCL", "Alcatel",
            // Gaming & peripherals
            "Gigabyte", "EVGA", "Sapphire", "Zotac", "PNY", "Palit",
            "Creative", "Roccat", "Cougar", "Cooler Master", "Thermaltake",
            // Emerging brands
            "Framework", "Fairphone", "Purism", "Pine64", "System76"
    };

    private static final String[] CATEGORY_NAMES = {
            // Mobile & tablets
            "Cell phones", "Smartphones", "Tablets", "E-readers", "Phablets",
            // Computers
            "Laptops", "Ultrabooks", "Gaming laptops", "Chromebooks", "Desktop PCs",
            "Workstations", "Mini PCs", "All-in-one PCs", "Gaming desktops",
            // Wearables
            "Smart watches", "Fitness trackers", "Smart bands", "Smart rings", "Health monitors",
            // Audio
            "Headphones", "Earbuds", "Wireless earphones", "Gaming headsets", "Studio headphones",
            "Bluetooth speakers", "Smart speakers", "Soundbars", "Portable speakers",
            // Gaming
            "Gaming consoles", "Handheld consoles", "Gaming controllers", "Gaming keyboards",
            "Gaming mice", "Gaming chairs", "VR headsets", "AR glasses",
            // Display
            "Televisions", "Monitors", "Gaming monitors", "Curved monitors", "Ultrawide monitors",
            "Portable monitors", "Projectors",
            // Photography
            "Cameras", "Digital cameras", "DSLR cameras", "Mirrorless cameras", "Action cameras",
            "Webcams", "Security cameras", "Drones",
            // Peripherals
            "Keyboards", "Mechanical keyboards", "Wireless keyboards", "Ergonomic keyboards",
            "Mice", "Trackballs", "Graphics tablets", "Drawing tablets",
            // Accessories
            "Chargers", "Power banks", "Charging cables", "Phone cases", "Screen protectors",
            "Laptop bags", "Cases & covers", "Stands & mounts", "Docking stations",
            // Storage
            "External HDDs", "External SSDs", "USB flash drives", "Memory cards", "NAS devices",
            // Networking
            "Routers", "Modems", "WiFi extenders", "Network switches", "Mesh systems",
            // Smart home
            "Smart lights", "Smart plugs", "Smart thermostats", "Smart locks", "Smart doorbells"
    };

    private static final String[] PRODUCT_PREFIXES = {
            // Size variants
            "Pro", "Max", "Ultra", "Plus", "Mini", "Lite", "SE", "Air", "Studio", "Edge",
            // Performance tiers
            "Premium", "Elite", "Advanced", "Standard", "Essential", "Basic",
            "Extreme", "Supreme", "Ultimate", "Prime",
            // Feature descriptors
            "Gaming", "Professional", "Creator", "Business", "Home", "Student",
            "Portable", "Compact", "Slim", "Flex", "Yoga", "Spin",
            // Version indicators
            "X", "XR", "S", "T", "G", "H", "K", "F", "U", "M",
            // Special editions
            "Limited", "Special", "Anniversary", "Signature", "Founders",
            "Black", "White", "Red", "Blue", "RGB"
    };

    private static final String[] GRAPHICS_PROCESSORS = {
            // Apple Silicon
            "M1", "M1 Pro", "M1 Max", "M1 Ultra", "M2", "M2 Pro", "M2 Max", "M3", "M3 Pro", "M3 Max",
            "A14 Bionic", "A15 Bionic", "A16 Bionic", "A17 Pro", "A18 Bionic", "A18 Pro",
            // Qualcomm Snapdragon
            "Snapdragon 8 Gen 3", "Snapdragon 8 Gen 2", "Snapdragon 8 Gen 1", "Snapdragon 888",
            "Snapdragon 870", "Snapdragon 865", "Snapdragon 7 Gen 2", "Snapdragon 7 Gen 1",
            // Samsung Exynos
            "Exynos 2400", "Exynos 2300", "Exynos 2200", "Exynos 2100", "Exynos 1480",
            // MediaTek
            "Dimensity 9300", "Dimensity 9200", "Dimensity 9000", "Dimensity 8200", "Dimensity 8100",
            // Huawei Kirin
            "Kirin 9000S", "Kirin 9000", "Kirin 990", "Kirin 980",
            // NVIDIA GeForce
            "RTX 4090", "RTX 4080", "RTX 4070 Ti", "RTX 4070", "RTX 4060 Ti", "RTX 4060",
            "RTX 3090", "RTX 3080", "RTX 3070", "RTX 3060", "GTX 1660 Ti", "GTX 1660", "GTX 1650",
            // AMD Radeon
            "RX 7900 XTX", "RX 7900 XT", "RX 7800 XT", "RX 7700 XT", "RX 7600",
            "RX 6950 XT", "RX 6900 XT", "RX 6800 XT", "RX 6700 XT", "RX 6600 XT",
            // Intel Arc
            "Arc A770", "Arc A750", "Arc A580", "Arc A380",
            // Intel Integrated
            "Intel Iris Xe", "Intel UHD Graphics", "Intel HD Graphics"
    };

    private static final Currency[] CURRENCIES = {
            Currency.getInstance("USD"), // US Dollar
            Currency.getInstance("EUR"), // Euro
            Currency.getInstance("GBP"), // British Pound
            Currency.getInstance("JPY"), // Japanese Yen
            Currency.getInstance("CNY"), // Chinese Yuan
            Currency.getInstance("KRW"), // South Korean Won
            Currency.getInstance("INR"), // Indian Rupee
            Currency.getInstance("AUD"), // Australian Dollar
            Currency.getInstance("CAD"), // Canadian Dollar
            Currency.getInstance("CHF")  // Swiss Franc
    };

    public static void main(String[] args) throws Exception {
        final EvitaContract evita = new EvitaClient(
                EvitaClientConfiguration.builder()
                        .host("localhost")
                        .build()
        );

        System.out.println("evitaDB connected ... defining schema");
        // clear existing catalog to start with fresh new one
        evita.deleteCatalogIfExists("evita-tutorial");
        // define new catalog
        evita.defineCatalog("evita-tutorial")
                .withDescription("This is a tutorial catalog.")
                .updateViaNewSession(evita);

        // define entity schemas by Java interfaces
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    session.goLiveAndClose();
                }
        );

        System.out.println("- catalog `evita-tutorial` created, now defining entity schemas");

        // define entity schemas by Java interfaces
        evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    session.defineEntitySchemaFromModelClass(Brand.class);
                    session.defineEntitySchemaFromModelClass(Category.class);
                    session.defineEntitySchemaFromModelClass(Product.class);
                }
        );

        System.out.println("- entity schemas defined, now creating randomized data");

        // create lots of randomized data with session management
        final EntityIdContainer container = setUpRandomizedData(evita, 100); // Create 100 products

        System.out.println("- created " + container.productIds.size() + " products, "
                + container.brandIds.size() + " brands, "
                + container.categoryIds.size() + " categories");

        // Run continuous simulation (blocks until Ctrl+C)
        runContinuousSimulation(evita, container);

        // close the connection (after shutdown hook)
        evita.close();
        System.out.println("evitaDB connection closed");
    }

    private static EntityIdContainer setUpRandomizedData(EvitaContract evita, int numberOfProducts) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();
                    EntityIdContainer container = new EntityIdContainer();

                    System.out.println("- creating " + BRAND_NAMES.length + " brands ...");
                    // Create all brands
                    Map<String, EntityReferenceContract> brandRefs = new HashMap<>();
                    for (String brandName : BRAND_NAMES) {
                        EntityReferenceContract brandRef = session.createNewEntity(BrandEditor.class)
                                .setName(brandName, Locale.ENGLISH)
                                .upsertVia(session);
                        brandRefs.put(brandName, brandRef);
                        container.brandIds.offer(brandRef.getPrimaryKey());
                    }
                    System.out.println(" ok.");

                    System.out.println("- creating " + CATEGORY_NAMES.length + " categories ...");
                    // Create all categories
                    Map<String, EntityReferenceContract> categoryRefs = new HashMap<>();
                    for (String categoryName : CATEGORY_NAMES) {
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
                        String productPrefix = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
                        String productName = brandName + " " + productPrefix + " " + (random.nextInt(20) + 1);

                        // Random specifications
                        int cores = (random.nextInt(4) + 1) * 2; // 2, 4, 6, 8 cores
                        String graphics = GRAPHICS_PROCESSORS[random.nextInt(GRAPHICS_PROCESSORS.length)];

                        // Random pricing
                        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextInt(1900)); // $100-$2000
                        BigDecimal taxRate = BigDecimal.valueOf(random.nextInt(25) + 5); // 5-30% tax
                        Currency currency = CURRENCIES[random.nextInt(CURRENCIES.length)];

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

    private static void updateSingleProduct(EvitaContract evita, int productId) {
        // Each update operation uses its own session
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
                                    editor.setName(generateRandomProductName(random), Locale.ENGLISH);
                                }

                                // Update cores (40% chance)
                                if ((updateMask & 2) != 0 || random.nextDouble() < 0.4) {
                                    editor.setCores((random.nextInt(4) + 1) * 2); // 2, 4, 6, 8 cores
                                }

                                // Update graphics (30% chance)
                                if ((updateMask & 4) != 0 || random.nextDouble() < 0.3) {
                                    editor.setGraphics(GRAPHICS_PROCESSORS[random.nextInt(GRAPHICS_PROCESSORS.length)]);
                                }

                                // Update price (50% chance) - THIS WAS MISSING!
                                if ((updateMask & 8) != 0 || random.nextDouble() < 0.5) {
                                    BigDecimal newPrice = BigDecimal.valueOf(100 + random.nextInt(1900)); // $100-$2000
                                    BigDecimal taxAmount = newPrice.multiply(BigDecimal.valueOf(random.nextInt(25) + 5)).divide(BigDecimal.valueOf(100)); // 5-30% tax
                                    Currency currency = CURRENCIES[random.nextInt(CURRENCIES.length)];

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
                                        // Remove all current categories and add 1-3 new ones
                                        // Note: This assumes we can clear existing categories - adjust based on your API
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
                    // Session automatically closed when lambda completes
                }
        );
    }

    private static int createSingleProduct(EvitaContract evita) {
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
                            System.err.println("\nWarning: Cannot create product - brands: " + brands.size() + ", categories: " + categories.size());
                            return -1; // Can't create product without brand/category
                        }

                    EntityReferenceContract randomBrand = brands.get(random.nextInt(brands.size()));

                    // Select 1-4 random categories for the new product
                    Set<EntityReferenceContract> selectedCategories = new HashSet<>();
                    int numCategories = random.nextInt(4) + 1; // 1-4 categories

                    for (int i = 0; i < numCategories && selectedCategories.size() < numCategories; i++) {
                        selectedCategories.add(categories.get(random.nextInt(categories.size())));
                    }

                    // Generate random pricing with more variety
                    BigDecimal basePrice = BigDecimal.valueOf(50 + random.nextInt(2950)); // $50-$3000
                    BigDecimal taxRate = BigDecimal.valueOf(random.nextInt(30) + 5); // 5-35% tax
                    BigDecimal taxAmount = basePrice.multiply(taxRate).divide(BigDecimal.valueOf(100));
                    BigDecimal priceWithTax = basePrice.add(taxAmount);
                    Currency currency = CURRENCIES[random.nextInt(CURRENCIES.length)];

                    // Create product with full randomization
                    ProductEditor productEditor = session.createNewEntity(ProductEditor.class)
                            .setName(generateRandomProductName(random), Locale.ENGLISH)
                            .setCores((random.nextInt(6) + 1) * 2) // 2, 4, 6, 8, 10, 12 cores for more variety
                            .setGraphics(GRAPHICS_PROCESSORS[random.nextInt(GRAPHICS_PROCESSORS.length)])
                            .setBrandId(randomBrand.getPrimaryKey())
                            .setBasicPrice(basePrice, priceWithTax, taxAmount, currency, 1);

                    // Add all selected categories
                    for (EntityReferenceContract categoryRef : selectedCategories) {
                        productEditor.addCategoryId(categoryRef.getPrimaryKey());
                    }

                        EntityReferenceContract productRef = productEditor.upsertVia(session);
                        return productRef.getPrimaryKey();
                        // Session automatically closed when lambda completes
                    }
            );
        } catch (Exception e) {
            System.err.println("\nException in createSingleProduct: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private static String generateRandomProductName(Random random) {
        String brand = BRAND_NAMES[random.nextInt(BRAND_NAMES.length)];
        String prefix = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
        int model = random.nextInt(100) + 1; // Expanded range 1-100

        // Add some variety to naming
        String[] suffixes = {"", " 5G", " WiFi", " Bluetooth", " Pro", " Gaming", " Business"};
        String suffix = suffixes[random.nextInt(suffixes.length)];

        return brand + " " + prefix + " " + model + suffix;
    }

    private static String getBrandNameFromRef(Map<String, EntityReferenceContract> brandRefs, EntityReferenceContract targetRef) {
        return brandRefs.entrySet().stream()
                .filter(entry -> entry.getValue().getPrimaryKey().equals(targetRef.getPrimaryKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Unknown Brand");
    }

    /**
     * Container for entity IDs used in continuous simulation.
     */
    static class EntityIdContainer {
        final Queue<Integer> productIds = new ConcurrentLinkedQueue<>();
        final Queue<Integer> brandIds = new ConcurrentLinkedQueue<>();
        final Queue<Integer> categoryIds = new ConcurrentLinkedQueue<>();
    }

    /**
     * Statistics tracker for continuous simulation operations.
     */
    static class OperationStatistics {
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

        public synchronized void recordProductUpdate() { productUpdates++; totalOperations++; }
        public synchronized void recordProductCreate() { productCreates++; totalOperations++; }
        public synchronized void recordProductDelete() { productDeletes++; totalOperations++; }
        public synchronized void recordBrandUpdate() { brandUpdates++; totalOperations++; }
        public synchronized void recordBrandCreate() { brandCreates++; totalOperations++; }
        public synchronized void recordBrandDelete() { brandDeletes++; totalOperations++; }
        public synchronized void recordCategoryUpdate() { categoryUpdates++; totalOperations++; }
        public synchronized void recordCategoryCreate() { categoryCreates++; totalOperations++; }
        public synchronized void recordCategoryDelete() { categoryDeletes++; totalOperations++; }
        public synchronized void recordError() { errors++; }

        public synchronized long getTotalOperations() { return totalOperations; }

        public synchronized void printProgress(int productCount, int brandCount, int categoryCount) {
            System.out.println(String.format(
                "\nOperation #%d | Products: %d | Brands: %d | Categories: %d",
                totalOperations, productCount, brandCount, categoryCount
            ));
            System.out.println(String.format(
                "  Product ops: %d updated, %d created, %d deleted",
                productUpdates, productCreates, productDeletes
            ));
            System.out.println(String.format(
                "  Brand ops: %d updated, %d created, %d deleted",
                brandUpdates, brandCreates, brandDeletes
            ));
            System.out.println(String.format(
                "  Category ops: %d updated, %d created, %d deleted",
                categoryUpdates, categoryCreates, categoryDeletes
            ));
            if (errors > 0) {
                System.out.println(String.format("  Errors: %d", errors));
            }
        }

        public synchronized void printFinalStatistics() {
            System.out.println("\n=== Final Statistics ===");
            System.out.println(String.format("Total operations: %d", totalOperations));
            System.out.println(String.format("Products: %d created, %d deleted, %d updated",
                productCreates, productDeletes, productUpdates));
            System.out.println(String.format("Brands: %d created, %d deleted, %d updated",
                brandCreates, brandDeletes, brandUpdates));
            System.out.println(String.format("Categories: %d created, %d deleted, %d updated",
                categoryCreates, categoryDeletes, categoryUpdates));
            System.out.println(String.format("Errors: %d", errors));
        }
    }

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
                        updateSingleProduct(evita, productId);
                        stats.recordProductUpdate();
                    }
                }
            } else if (operationType < 80) {
                // 35% - Create product (much higher to grow the catalog)
                try {
                    int newProductId = createSingleProduct(evita);
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
                    deleteRandomProduct(evita, container.productIds);
                    stats.recordProductDelete();
                }
            // 10% operations for master data (brands & categories)
            } else if (operationType < 93) {
                // 3% - Update brand
                updateRandomBrand(evita, container.brandIds);
                stats.recordBrandUpdate();
            } else if (operationType < 96) {
                // 3% - Update category
                updateRandomCategory(evita, container.categoryIds);
                stats.recordCategoryUpdate();
            } else if (operationType < 97) {
                // 1% - Create brand (rarely - master data)
                int newBrandId = createRandomBrand(evita);
                if (newBrandId > 0) {
                    container.brandIds.offer(newBrandId);
                    stats.recordBrandCreate();
                }
            } else if (operationType < 99) {
                // 2% - Create category (occasionally - master data)
                int newCategoryId = createRandomCategory(evita);
                if (newCategoryId > 0) {
                    container.categoryIds.offer(newCategoryId);
                    stats.recordCategoryCreate();
                }
            } else {
                // 1% - Delete brand or category (very rarely)
                if (random.nextBoolean()) {
                    deleteRandomBrand(evita, container.brandIds);
                    stats.recordBrandDelete();
                } else {
                    deleteRandomCategory(evita, container.categoryIds);
                    stats.recordCategoryDelete();
                }
            }
        } catch (Exception e) {
            stats.recordError();
            throw e;
        }
    }

    private static void deleteRandomProduct(EvitaContract evita, Queue<Integer> productIds) {
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

    private static void updateRandomBrand(EvitaContract evita, Queue<Integer> brandIds) {
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
                                    String newName = BRAND_NAMES[random.nextInt(BRAND_NAMES.length)]
                                            + " " + random.nextInt(100);
                                    editor.setName(newName, Locale.ENGLISH);
                                    editor.upsertVia(session);
                                });
                    }
                }
        );
    }

    private static int createRandomBrand(EvitaContract evita) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();
                    String brandName = BRAND_NAMES[random.nextInt(BRAND_NAMES.length)]
                            + " " + random.nextInt(1000);

                    EntityReferenceContract brandRef = session.createNewEntity(BrandEditor.class)
                            .setName(brandName, Locale.ENGLISH)
                            .upsertVia(session);

                    return brandRef.getPrimaryKey();
                }
        );
    }

    private static void deleteRandomBrand(EvitaContract evita, Queue<Integer> brandIds) {
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

    private static void updateRandomCategory(EvitaContract evita, Queue<Integer> categoryIds) {
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
                                        String newName = CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)]
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

    private static int createRandomCategory(EvitaContract evita) {
        return evita.updateCatalog(
                "evita-tutorial",
                session -> {
                    Random random = ThreadLocalRandom.current();
                    String categoryName = CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)]
                            + " " + random.nextInt(1000);

                    EntityReferenceContract categoryRef = session.createNewEntity(CategoryEditor.class)
                            .setName(categoryName, Locale.ENGLISH)
                            .upsertVia(session);

                    return categoryRef.getPrimaryKey();
                }
        );
    }

    private static void deleteRandomCategory(EvitaContract evita, Queue<Integer> categoryIds) {
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

    private static void readProductAndPrintToConsole(EvitaContract evita, int productId) {
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
                    System.out.println("\tSelling price with VAT: " + product.getPriceForSale().priceWithTax() + " " + product.getPriceForSale().currency());
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
}