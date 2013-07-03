package org.bgee.model.expressiondata;

public class DataParameters {
	/**
	 * Define the different types of expression data calls.
	 * <ul>
	 * <li><code>EXPRESSION</code>: standard expression calls.
	 * <li><code>NOEXPRESSION</code>: no-expression calls (absence of expression 
	 * explicitly reported).
	 * <li><code>OVEREXPRESSION</code>: over-expression calls.
	 * <li><code>UNDEREXPRESSION</code>: under-expression calls.
	 * <li><code>NODIFFEXPRESSION</code>: means that a gene was studied in 
	 * a differential expression analysis, but was <strong>not</strong> found to be 
	 * differentially expressed (neither <code>OVEREXPRESSION</code> nor 
	 * <code>UNDEREXPRESSION</code> calls). This is different from <code>NOEXPRESSION</code>, 
	 * as the gene could actually be expressed, but, not differentially. 
	 * </ul>
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
	 */
    public enum CallType {
    	EXPRESSION, NOEXPRESSION, OVEREXPRESSION, UNDEREXPRESSION, NODIFFEXPRESSION;
    }
    /**
     * Define the different expression data types used in Bgee.
     * <ul>
     * <li><code>AFFYMETRIX</code>: microarray Affymetrix.
     * <li><code>EST</code>: Expressed Sequence Tag.
     * <li><code>INSITU</code>: <em>in situ</em> hybridization data.
     * <li><code>RELAXEDINSITU</code>: use of <em>in situ</em> hybridization data 
     * to infer absence of expression: the inference 
	 * considers expression patterns described by <em>in situ</em> data as complete. 
	 * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
	 * only localizations of expression, implicitly stating absence of expression 
	 * in all other tissues. When <em>in situ</em> data are available for a gene, 
	 * we considered that absence of expression is assumed in any organ existing 
	 * at the developmental stage studied in the <em>in situ</em>, with no report of 
	 * expression by any data type, in the organ itself, or any substructure. 
     * <li><code>RNASEQ</code>: RNA-Seq data.
     * </ul>
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DataType {
    	AFFYMETRIX, EST, INSITU, RELAXEDINSITU, RNASEQ;
    }
    /**
     * Define the different confidence level in expression data. 
     * These information is computed differently based on the type of call 
     * and the data type.
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DataQuality {
    	LOW, HIGH;
    }
}
