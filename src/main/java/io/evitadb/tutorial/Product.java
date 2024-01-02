package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.PriceContract;
import io.evitadb.api.requestResponse.data.SealedInstance;
import io.evitadb.api.requestResponse.data.annotation.Attribute;
import io.evitadb.api.requestResponse.data.annotation.Entity;
import io.evitadb.api.requestResponse.data.annotation.Reference;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * This interface describes the contract of the Product entity.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
@Entity(
    name = "Product",
    description = "A product in inventory."
)
public interface Product extends Serializable, SealedInstance<Product, ProductEditor> {

    String REFERENCE_BRAND = "brand";
    String REFERENCE_CATEGORIES = "categories";

    /**
     * Name of the product.
     * @return name of the product
     */
    @Attribute(
            name = "name",
            description = "Name of the product.",
            localized = true,
            filterable = true,
            sortable = true,
            representative = true
    )
    @Nonnull
    String getName();

    /**
     * Number of CPU cores in the laptop.
     * @return number of CPU cores
     */
    @Attribute(
            name = "cores",
            description = "Number of CPU cores.",
            filterable = true
    )
    @Nonnull
    Integer getCores();

    /**
     * Description of the graphics card in the laptop.
     * @return graphics card
     */
    @Attribute(
            name = "graphics",
            description = "Graphics card.",
            filterable = true
    )
    @Nonnull
    String getGraphics();

    /**
     * Price the product can be sold for.
     * @return price for sale
     */
    @Nonnull
    PriceContract getPriceForSale();

    /**
     * Brand of the product.
     * @return brand of the product
     */
    @Reference(
            name = REFERENCE_BRAND,
            description = "Brand of the product.",
            entity = Brand.ENTITY_NAME,
            allowEmpty = false,
            indexed = true
    )
    @Nonnull
    Brand getBrand();

    /**
     * Categories the product belongs to.
     * @return categories the product belongs to
     */
    @Reference(
            name = REFERENCE_CATEGORIES,
            description = "Categories the product belongs to.",
            entity = Category.ENTITY_NAME,
            indexed = true
    )
    @Nonnull
    List<Category> getCategories();

}
