package org.bgee.model.expressiondata.baseelements;

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
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public enum DataType implements BgeeEnumField {
    AFFYMETRIX, EST, IN_SITU, RNA_SEQ;

    @Override
    public String getStringRepresentation() {
        return this.name();
    }
    
    /**
     * Convert the {@code Set} of {@code String}s that are string representations of data types
     * into a {@code Set} of {@code DataType}s.
     * Operation performed by calling {@link BgeeEnum#convertStringSetToEnumSet(Class, Set)} with 
     * {@code DataType} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representations           A {@code Set} of {@code String}s that are string
     *                                  representations of data types.
     * @return                          A {@code Set} of {@code DataType}s corresponding 
     *                                  to {@code representations}.
     * @throw IllegalArgumentException  If a representation does not correspond 
     *                                  to any {@code DataType}.
     */
    public static final Set<DataType> convertToDataTypeSet(Set<String> representations) {
        return BgeeEnum.convertStringSetToEnumSet(DataType.class, representations);
    }
}
