package org.bgee.model.expressiondata.baseelements;

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
public enum DataType {
    AFFYMETRIX, EST, IN_SITU, RELAXED_IN_SITU, RNA_SEQ;
}
