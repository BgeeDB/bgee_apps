package org.bgee.model.expressiondata.rawdata.diffexpression;

/**
 * A differential expression analysis that generated differential expression calls. 
 * <p>
 * A {@code DiffExpressionAnalysis} compare different conditions, according 
 * to a specified {@link ComparisonFactor} (see {@link #getComparisonFactor()}). 
 * For instance, if the comparison factor is {@code ANATOMY}, it means that different 
 * organs were compared, at an equivalent developmental stage.
 * <p>
 * Each condition (for instance, an organ at a given developmental stage) is represented 
 * several {@link org.bgee.model.expressiondata.rawdata.SampleAssay}s studying 
 * this condition, grouped into {@link SampleAssayGroup}s (see #getSampleAssayGroups()). 
 * Equivalent {@link org.bgee.model.expressiondata.rawdata.CallSource}s in a same 
 * {@code SampleAssayGroup} are grouped as a {@link CallSourceGroup} 
 * (for instance, a same Affymetrix probeset on several chips of a same type, 
 * studying a given condition; or a RNA-Seq result of RPKM value for a same gene, 
 * in different libraries studying a given condition). 
 * <p>
 * A {@code DiffExpressionAnalysis} then consists in the comparison of equivalent 
 * {@code CallSourceGroup}s between its different {@code SampleAssayGroup}s, 
 * in order to generate fold changes, and p-values of likeliness of differential expression 
 * of a gene. For instance, a multiple comparison to the mean of same probesets, 
 * on several chips of the same type, studying different conditions, with replicates 
 * for each condition. Or, a multiple comparison to the mean of RPKM values 
 * for same genes, from several RNA-Seq libraries, studying different conditions, 
 * with library replicates for each condition.
 * <p>
 * As of Bgee 13, a differential expression analysis is performed on one 
 * {@link org.bgee.model.expressiondata.rawdata.Experiment} at a time, but we let 
 * open the possibility to come up with a method that allows comparisons over 
 * multiple experiments. Also, as of Bgee 13, the comparisons are only made along 
 * different anatomical structures or different developmental stages, but we let open 
 * the possibility to add other comparison factors (for instance, comparison over 
 * different genotypes), as they are abstracted into the class {@link ComparisonFactor}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class DiffExpressionAnalysis {
	/**
	 * The factor along which a differential expression analysis was performed: 
	 * <ul>
	 * <li>{@code ANATOMY}: the analysis compared expression in different 
	 * anatomical structures, at an equivalent (broad) developmental stage.
	 * <li>{@code DEVELOPMENT}: the analysis compared expression in a same 
	 * anatomical structure, at different developmental stages. 
	 * </ul>
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
    public enum ComparisonFactor {
    	ANATOMY, DEVELOPMENT;
    }
}
