package io.evitadb.tutorial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Centralized provider of random data for the evitaDB tutorial.
 * This utility class contains all predefined data arrays (brands, categories, product prefixes,
 * graphics processors, currencies) and provides methods for random selection from these arrays.
 *
 * <p>The class also handles loading of example content from resource files (markdown, HTML, JSON)
 * which are used to populate associated data fields on products.</p>
 *
 * <p>This is a utility class with a private constructor - all methods are static.</p>
 *
 * @author Jan Novotny (novotny@fg.cz), FG Forrest a.s. (c) 2025
 */
public final class RandomDataProvider {

    // ==================== BRAND NAMES ====================

    /**
     * Array of technology brand names used for generating random brands and products.
     * Includes established tech brands, gaming/peripherals manufacturers, and emerging brands.
     */
    public static final String[] BRAND_NAMES = {
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

    // ==================== CATEGORY NAMES ====================

    /**
     * Array of product category names covering mobile devices, computers, wearables,
     * audio equipment, gaming, displays, photography, peripherals, accessories,
     * storage, networking, and smart home products.
     */
    public static final String[] CATEGORY_NAMES = {
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

    // ==================== PRODUCT PREFIXES ====================

    /**
     * Array of product naming prefixes used to generate product names.
     * Includes size variants, performance tiers, feature descriptors, version indicators,
     * and special editions.
     */
    public static final String[] PRODUCT_PREFIXES = {
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

    // ==================== GRAPHICS PROCESSORS ====================

    /**
     * Array of graphics processor names covering Apple Silicon, Qualcomm Snapdragon,
     * Samsung Exynos, MediaTek Dimensity, Huawei Kirin, NVIDIA GeForce,
     * AMD Radeon, and Intel Arc/integrated graphics.
     */
    public static final String[] GRAPHICS_PROCESSORS = {
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

    // ==================== CURRENCIES ====================

    /**
     * Array of supported currencies for product pricing.
     */
    public static final Currency[] CURRENCIES = {
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

    // ==================== RESOURCE LOADING ====================

    /**
     * Directory path for example data resources (markdown, HTML, JSON files).
     */
    private static final String RESOURCE_DATA_DIR = "META-INF/dataExamples";

    /**
     * Lazy-loaded list of markdown file paths from resources.
     */
    private static List<String> MARKDOWN_FILES;

    /**
     * Lazy-loaded list of HTML file paths from resources.
     */
    private static List<String> HTML_FILES;

    /**
     * Lazy-loaded list of JSON file paths from resources.
     */
    private static List<String> JSON_FILES;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RandomDataProvider() {
        // Utility class - no instantiation
    }

    // ==================== RANDOM SELECTION METHODS ====================

    /**
     * Returns a randomly selected brand name from {@link #BRAND_NAMES}.
     *
     * @param random the random number generator to use
     * @return a random brand name
     */
    public static String getRandomBrandName(Random random) {
        return BRAND_NAMES[random.nextInt(BRAND_NAMES.length)];
    }

    /**
     * Returns a randomly selected category name from {@link #CATEGORY_NAMES}.
     *
     * @param random the random number generator to use
     * @return a random category name
     */
    public static String getRandomCategoryName(Random random) {
        return CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)];
    }

    /**
     * Returns a randomly selected product prefix from {@link #PRODUCT_PREFIXES}.
     *
     * @param random the random number generator to use
     * @return a random product prefix
     */
    public static String getRandomProductPrefix(Random random) {
        return PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
    }

    /**
     * Returns a randomly selected graphics processor name from {@link #GRAPHICS_PROCESSORS}.
     *
     * @param random the random number generator to use
     * @return a random graphics processor name
     */
    public static String getRandomGraphicsProcessor(Random random) {
        return GRAPHICS_PROCESSORS[random.nextInt(GRAPHICS_PROCESSORS.length)];
    }

    /**
     * Returns a randomly selected currency from {@link #CURRENCIES}.
     *
     * @param random the random number generator to use
     * @return a random currency
     */
    public static Currency getRandomCurrency(Random random) {
        return CURRENCIES[random.nextInt(CURRENCIES.length)];
    }

    /**
     * Generates a random product name combining brand, prefix, model number, and optional suffix.
     * Example outputs: "Apple Pro 42", "Samsung Gaming 7 5G", "Dell Business 15 WiFi"
     *
     * @param random the random number generator to use
     * @return a randomly generated product name
     */
    public static String generateRandomProductName(Random random) {
        String brand = BRAND_NAMES[random.nextInt(BRAND_NAMES.length)];
        String prefix = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
        int model = random.nextInt(100) + 1; // Model number 1-100

        // Add variety with optional suffixes
        String[] suffixes = {"", " 5G", " WiFi", " Bluetooth", " Pro", " Gaming", " Business"};
        String suffix = suffixes[random.nextInt(suffixes.length)];

        return brand + " " + prefix + " " + model + suffix;
    }

    // ==================== RESOURCE CONTENT METHODS ====================

    /**
     * Returns random markdown content from the resource files.
     * Used for populating the "tip" associated data on products.
     *
     * @param random the random number generator to use
     * @return random markdown content, or null if no files are available
     */
    public static String getRandomMarkdownContent(Random random) {
        ensureResourceFileListsLoaded();
        return pickRandomContent(MARKDOWN_FILES, random);
    }

    /**
     * Returns random HTML content from the resource files.
     * Used for populating the "idea" associated data on products.
     *
     * @param random the random number generator to use
     * @return random HTML content, or null if no files are available
     */
    public static String getRandomHtmlContent(Random random) {
        ensureResourceFileListsLoaded();
        return pickRandomContent(HTML_FILES, random);
    }

    /**
     * Returns random JSON content from the resource files.
     * Used for populating the "metadata" associated data on products.
     *
     * @param random the random number generator to use
     * @return random JSON content, or null if no files are available
     */
    public static String getRandomJsonContent(Random random) {
        ensureResourceFileListsLoaded();
        return pickRandomContent(JSON_FILES, random);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Ensures that resource file lists are loaded. This method is thread-safe
     * and performs lazy loading of file lists only once.
     */
    private static synchronized void ensureResourceFileListsLoaded() {
        if (MARKDOWN_FILES != null && HTML_FILES != null && JSON_FILES != null) {
            return;
        }
        MARKDOWN_FILES = listResourceFilesByExtension(".md");
        HTML_FILES = listResourceFilesByExtension(".html");
        JSON_FILES = listResourceFilesByExtension(".json");
    }

    /**
     * Lists all resource files in the data directory with the specified extension.
     * Supports both file system and JAR-based resource loading.
     *
     * @param extension the file extension to filter by (e.g., ".md", ".html")
     * @return list of resource paths matching the extension
     */
    private static List<String> listResourceFilesByExtension(String extension) {
        try {
            URL dirUrl = RandomDataProvider.class.getClassLoader().getResource(RESOURCE_DATA_DIR);
            if (dirUrl == null) {
                return Collections.emptyList();
            }
            String protocol = dirUrl.getProtocol();
            if ("file".equals(protocol)) {
                try {
                    URI uri = dirUrl.toURI();
                    java.nio.file.Path dirPath = java.nio.file.Paths.get(uri);
                    try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.list(dirPath)) {
                        return paths
                                .filter(Files::isRegularFile)
                                .map(p -> p.getFileName().toString())
                                .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(extension))
                                .map(name -> RESOURCE_DATA_DIR + "/" + name)
                                .collect(Collectors.toList());
                    }
                } catch (URISyntaxException e) {
                    return Collections.emptyList();
                }
            } else if ("jar".equals(protocol)) {
                try {
                    JarURLConnection jarConnection = (JarURLConnection) dirUrl.openConnection();
                    try (JarFile jarFile = jarConnection.getJarFile()) {
                        List<String> result = new ArrayList<>();
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (!entry.isDirectory() && name.startsWith(RESOURCE_DATA_DIR + "/")
                                    && name.toLowerCase(Locale.ROOT).endsWith(extension)) {
                                result.add(name);
                            }
                        }
                        return result;
                    }
                } catch (IOException e) {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Reads a resource file as a string.
     *
     * @param resourcePath the classpath resource path
     * @return the file contents as a string, or null if reading fails
     */
    private static String readResourceAsString(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        try (InputStream is = RandomDataProvider.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Picks a random file from the list and returns its contents.
     *
     * @param files  list of resource file paths
     * @param random the random number generator to use
     * @return the contents of a randomly selected file, or null if the list is empty
     */
    private static String pickRandomContent(List<String> files, Random random) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        String path = files.get(random.nextInt(files.size()));
        return readResourceAsString(path);
    }

}
