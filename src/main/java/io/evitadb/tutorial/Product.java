package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.PriceContract;
import io.evitadb.api.requestResponse.data.SealedInstance;
import io.evitadb.api.requestResponse.data.annotation.*;
import io.evitadb.api.requestResponse.schema.dto.ReferenceIndexType;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
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
     *
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
     *
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
     *
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
     * A tip for the product.
     *
     * @return tip for the product
     */
    @AssociatedData(
        name = "tip",
        description = "A tip for the product.",
        nullable = true
    )
    @Nonnull
    String getTip();

    /**
     * An idea behind the product.
     *
     * @return idea behind the product
     */
    @AssociatedData(
        name = "idea",
        description = "An idea behind the product.",
        nullable = true
    )
    @Nonnull
    String getIdea();

    /**
     * Metadata of the product.
     *
     * @return metadata of the product
     */
    @AssociatedData(
        name = "metadata",
        description = "Metadata of the product.",
        nullable = true
    )
    @Nonnull
    String getMetadata();

    /**
     * Price the product can be sold for.
     */
    @Price(priceList = "basic")
    @Nonnull
    ProductEditor setBasicPrice(
        @Nonnull BigDecimal priceWithoutTax,
        @Nonnull BigDecimal priceWithTax,
        @Nonnull BigDecimal taxRate,
        @Nonnull Currency currency,
        int priceId
    );

    /**
     * Price the product can be sold for.
     *
     * @return price for sale
     */
    @PriceForSale
    @Nonnull
    PriceContract getPriceForSale();

    /**
     * Brand of the product.
     *
     * @return brand of the product
     */
    @Reference(
        name = REFERENCE_BRAND,
        description = "Brand of the product.",
        entity = Brand.ENTITY_NAME,
        allowEmpty = false,
        indexed = ReferenceIndexType.FOR_FILTERING
    )
    @Nonnull
    Brand getBrand();

    /**
     * Categories the product belongs to.
     *
     * @return categories the product belongs to
     */
    @Reference(
        name = REFERENCE_CATEGORIES,
        description = "Categories the product belongs to.",
        entity = Category.ENTITY_NAME,
        indexed = ReferenceIndexType.FOR_FILTERING
    )
    @Nonnull
    List<Category> getCategories();

}
