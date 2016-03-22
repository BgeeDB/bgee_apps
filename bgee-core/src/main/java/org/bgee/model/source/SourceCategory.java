package org.bgee.model.source;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

/**
 * An {@code enum} defining the category of data source. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13, Mar. 2016
 */
public enum SourceCategory implements BgeeEnumField {
    //WARNING: these Enums must be declared in order, from the lowest quality 
    //to the highest quality. This is because the compareTo implementation 
    //of the Enum class will be used.
    NONE, GENOMICS, PROTEOMICS, IN_SITU, AFFYMETRIX, EST, RNA_SEQ, ONTOLOGY;

    @Override
    public String getStringRepresentation() {
        return this.name();
    }
    
    /**
     * Convert the {@code String} representation of a source category (for instance, 
     * retrieved from request) into a {@code SourceCategory}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code SourceCategory} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a source category.
     * @return                          A {@code SourceCategory} corresponding to {@code representation}.
     * @throw IllegalArgumentException  If {@code representation} does not correspond 
     *                                  to any {@code SourceCategory}.
     * @see #convert(Class, String)
     */
    public static final SourceCategory convertToSourceCategory(String representation) {
        return BgeeEnum.convert(SourceCategory.class, representation);
    }
}
