package org.bgee.model.expressiondata.baseelements;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;

/**
 * An {@code enum} defining the expression data types used in Bgee:
 * <ul>
 * <li>{@code AFFYMETRIX}: microarray Affymetrix.
 * <li>{@code EST}: Expressed Sequence Tag.
 * <li>{@code IN_SITU}: <em>in situ</em> hybridization data.
 * <li>{@code RELAXED_IN_SITU}: use of <em>in situ</em> hybridization data 
 * to infer more information about absence of expression: the inference 
 * considers expression patterns described by <em>in situ</em> data as complete. 
 * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
 * only localizations of expression, implicitly stating absence of expression 
 * in all other tissues. When <em>in situ</em> data are available for a gene, 
 * this data type considered that absence of expression is assumed in any organ existing 
 * at the developmental stage studied in the <em>in situ</em>, with no report of 
 * expression by any data type, in the organ itself, or any substructure. 
 * <li>{@code RNA_SEQ}: RNA-Seq data.
 * <li>{@code FULL_LENGTH}: Full length single cell RNA-Seq data.
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
//TODO: why don't we have a "ALL" data type?? This would be much cleaner than having to provide "null" 
//everywhere...
public enum DataType implements BgeeEnumField {
    AFFYMETRIX("Affymetrix"), EST("EST"), IN_SITU("in situ hybridization"), RNA_SEQ("RNA-Seq"),
    FULL_LENGTH("full length single cell RNA-Seq");
    
    private final String representation;
    
    private DataType(String representation) {
        this.representation = representation;
    }

    @Override
    public String getStringRepresentation() {
        return this.representation;
    }
    
    /**
     * Convert the {@code Collection} of {@code String}s that are string representations of data types
     * into a {@code Set} of {@code DataType}s.
     * Operation performed by calling {@link BgeeEnum#convertStringSetToEnumSet(Class, Collection)}
     * with {@code DataType} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representations           A {@code Collection} of {@code String}s that are string
     *                                  representations of data types.
     * @return                          A {@code Set} of {@code DataType}s corresponding 
     *                                  to {@code representations}.
     * @throws IllegalArgumentException If a representation does not correspond 
     *                                  to any {@code DataType}.
     */
    public static final Set<DataType> convertToDataTypeSet(Collection<String> representations) {
        return BgeeEnum.convertStringSetToEnumSet(DataType.class, representations);
    }
}
