package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.InstanceEditor;
import io.evitadb.api.requestResponse.data.annotation.ParentEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * This interface represents the writable counterpart of the Category interface and allows to alter its data.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2024
 */
public interface CategoryEditor extends Category, InstanceEditor<Category> {

    @Nonnull
    CategoryEditor setName(@Nonnull String name, @Nonnull Locale locale);

    @ParentEntity
    @Nonnull
    CategoryEditor setParentCategoryId(@Nullable Integer parentCategoryId);

    @ParentEntity
    @Nonnull
    CategoryEditor setParentCategory(@Nullable Category parentCategory);

}
