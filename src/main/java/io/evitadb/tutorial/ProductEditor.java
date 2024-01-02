package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.InstanceEditor;
import io.evitadb.api.requestResponse.data.PriceContract;
import io.evitadb.api.requestResponse.data.annotation.CreateWhenMissing;
import io.evitadb.api.requestResponse.data.annotation.ReferenceRef;
import io.evitadb.api.requestResponse.data.annotation.RemoveWhenExists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * This interface represents the writable counterpart of the Product interface and allows to alter its data.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2024
 */
public interface ProductEditor extends Product, InstanceEditor<Product> {

    @Nonnull
    ProductEditor setName(@Nonnull String name, @Nonnull Locale locale);

    @Nonnull
    ProductEditor setCores(@Nonnull Integer cores);

    @Nonnull
    ProductEditor setGraphics(@Nonnull String graphics);

    @Nonnull
    ProductEditor setPrice(@Nonnull PriceContract price);

    /**
     * Sets the brand of the product.
     * @param brandId id of the brand to be set
     * @return this editor
     */
    @ReferenceRef(REFERENCE_BRAND)
    @Nonnull
    ProductEditor setBrandId(int brandId);

    /**
     * Sets the brand of the product.
     * @param brand brand to be set
     * @return this editor
     */
    @ReferenceRef(REFERENCE_BRAND)
    @Nonnull
    ProductEditor setBrand(@Nonnull Brand brand);

    /**
     * Adds a category to the product.
     * @param categoryId id of the category to be added
     * @return this editor
     */
    @ReferenceRef(REFERENCE_CATEGORIES)
    @CreateWhenMissing
    @Nonnull
    ProductEditor addCategoryId(int categoryId);

    /**
     * Adds a category to the product.
     * @param category category to be added
     * @return this editor
     */
    @ReferenceRef(REFERENCE_CATEGORIES)
    @CreateWhenMissing
    @Nonnull
    ProductEditor addCategory(@Nonnull Category category);

    /**
     * Removes the category with particular id from the product.
     * @param categoryId id of the category to be removed
     * @return this editor
     */
    @ReferenceRef(REFERENCE_CATEGORIES)
    @RemoveWhenExists
    @Nonnull
    ProductEditor removeCategoryById(int categoryId);

    /**
     * Overwrites all categories of the product.
     * @param categoryId new category ids of the product
     * @return this editor
     */
    @ReferenceRef(REFERENCE_CATEGORIES)
    @Nonnull
    ProductEditor setCategoryIds(int... categoryId);

    /**
     * Overwrites all categories of the product.
     * @param category new categories of the product
     * @return this editor
     */
    @ReferenceRef(REFERENCE_CATEGORIES)
    @Nonnull
    ProductEditor setCategories(@Nullable Category... category);

}
