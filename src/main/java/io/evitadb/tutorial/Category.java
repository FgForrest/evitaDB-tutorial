package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.annotation.Attribute;
import io.evitadb.api.requestResponse.data.annotation.Entity;
import io.evitadb.api.requestResponse.data.annotation.ParentEntity;

import javax.annotation.Nonnull;
import java.util.Optional;

import static io.evitadb.tutorial.Category.ENTITY_NAME;

/**
 * This interface describes the contract of the Category entity.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
@Entity(
    name = ENTITY_NAME,
    description = "A category of products."
)
public interface Category {

    String ENTITY_NAME = "Category";

    /**
     * Name of the product category.
     * @return name of the product category
     */
    @Attribute(
        name = "name",
        description = "Name of the product category.",
        localized = true,
        filterable = true,
        sortable = true
    )
    @Nonnull
    String getName();

    /**
     * Parent category of this category (if defined).
     * @return parent category of this category or empty value if this category is a root category
     */
    @ParentEntity
    Optional<Category> getParentCategory();

}
