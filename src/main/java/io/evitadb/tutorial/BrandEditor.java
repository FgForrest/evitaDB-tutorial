package io.evitadb.tutorial;

import io.evitadb.api.requestResponse.data.InstanceEditor;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * This interface represents the writable counterpart of the Brand interface and allows to alter its data.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2024
 */
public interface BrandEditor extends Brand, InstanceEditor<Brand> {

    @Nonnull
    BrandEditor setName(@Nonnull String name, @Nonnull Locale locale);

}
