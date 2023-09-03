package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.annotation.Attribute;
import io.evitadb.api.requestResponse.data.annotation.Entity;

import javax.annotation.Nonnull;

/**
 * This interface describes the contract of the Brand entity.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
@Entity(
    name = Brand.ENTITY_NAME,
    description = "A manufacturer of products."
)
public interface Brand {

    String ENTITY_NAME = "Brand";

    /**
     * Name of the manufacturer.
     * @return name of the manufacturer
     */
    @Attribute(
        name = "name",
        description = "Name of the manufacturer.",
        localized = true,
        filterable = true,
        sortable = true
    )
    @Nonnull
    String getName();

}
