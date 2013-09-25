package org.bgee.model.expressiondata.querytools.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@code CompositeCallFilter} allows to use a {@code BasicCallFilter} 
 * and a {@code RawDataFilter} at the same time. It allows to filter 
 * based on expression data calls, as when using a {@code BasicCallFilter} 
 * (this is why it implements the interface {@code CallFilter}), but as if the calls 
 * had been computed from only a subset of the data in Bgee, filtered using 
 * the {@code RawDataFilter}. This leads to re-compute on-the-fly 
 * expression data calls summarizing expression data. As this is computationally intensive, 
 * {@code CompositeCallFilter}s can only be used for queries restrained 
 * to a {@code Gene}, or to a list of {@code Gene}s (as for instance, an 
 * {@link org.bgee.model.expressiondata.querytools.AnatDevExpressionQuery 
 * AnatDevExpressionQuery}). 
 * <p>
 * This class implements the methods from {@code CallFilter}, by delegating 
 * to the {@code BasicCallFilter} instance it holds. So, calling a method 
 * defined by the {@code CallFiler} interface, on an instance of this class, 
 * is equivalent to calling them on the {@code BasicCallFilter} instance it holds.
 * <p>
 * <h3>Explanations about the computations</h3>
 * Bgee summarizes expression data over several experiments or samples. For instance, 
 * if a sample shows expression of a gene with a high confidence in a condition, 
 * and another sample for the same condition shows absence of expression of that gene, 
 * Bgee will consider the gene as expressed in that condition, with a low quality score.
 * <p>
 * Because of this, removing some source raw data, or restraining results to 
 * some source raw data, will change the overall summarized data calls 
 * generated (in the previous example, removing the sample showing expression of the gene 
 * would lead to consider it as not expressed with a high confidence...). 
 * <p>
 * So basically, a {@code CompositeCallFilter} will work as 
 * a {@code BasicCallFilter}, but as if Bgee was containing only the source raw data 
 * filtered from the {@code RawDataFilter}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CompositeCallFilter implements CallFilter {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CompositeCallFilter.class.getName());
	/**
	 * The {@code BasicCallFilter} used to specify the expression data calls 
	 * that should be generated and retrieved.
	 */
    private final BasicCallFilter callFilter;
    /**
     * The {@code RawDataFilter} specifying the raw data that should be used 
     * to generate the expression data calls. 
     */
    private final RawDataFilter rawDataFilter;
    
    //TODO: here, or in an expression query tool?
    private boolean allSamplesAgreement;
    
    /**
     * Default constructor. 
     * 
     * @param callFilter        the {@code BasicCallFilter} used to specify 
     *                          the expression data calls that should be generated 
     *                          and retrieved. 
     * @param rawDataFilter     the {@code RawDataFilter} specifying the raw data 
     *                          that should be used to generate the expression data calls.
     */
    public CompositeCallFilter(BasicCallFilter callFilter, RawDataFilter rawDataFilter) {
        this.callFilter = callFilter;
        this.rawDataFilter = rawDataFilter;
    }
    
    
    @Override
    public CallFilter mergeSameEntityCallFilter(CallFilter filterToMerge) {
        log.entry(filterToMerge);
        return log.exit(this.merge(filterToMerge, true));
    }
    @Override
    public CallFilter mergeDiffEntitiesCallFilter(CallFilter filterToMerge) {
        log.entry(filterToMerge);
        return log.exit(this.merge(filterToMerge, false));
    }
    /**
     * Merges this {@code CompositeCallFilter} with {@code filterToMerge}, 
     * and returns the resulting merged new {@code CompositeCallFilter}.
     * If {@code filterToMerge} cannot be merged with this 
     * {@code CompositeCallFilter}, this method returns {@code null}
     * <p>
     * If {@code sameEntity} is {@code true}, this method should correspond to 
     * {@link CallFilter#mergeSameEntityCallFilter(CallFilter)}, otherwise, to 
     * {@link CallFilter#mergeDiffEntitiesCallFilter(CallFilter)}.
     * 
     * @param filterToMerge       a {@code CallFilter} to be merged with this one.
     * @param sameEntity        a {@code boolean} defining whether 
     *                          {@code filterToMerge} and this 
     *                          {@code CompositeCallFilter} are related to a same 
     *                          {@code Entity}, or different ones. 
     * @return  A newly instantiated {@code CompositeCallFilter} corresponding to 
     *          the merging of this {@code CompositeCallFilter} and of 
     *          {@code filterToMerge}, or {@code null} if they could not be merged. 
     */
    protected CompositeCallFilter merge(CallFilter filterToMerge, boolean sameEntity) {
        log.entry(filterToMerge, sameEntity);
        if (!(filterToMerge instanceof CompositeCallFilter)) {
            return log.exit(null);
        }
        CompositeCallFilter otherFilter = (CompositeCallFilter) filterToMerge;
        //to be merged, the rawDataFilters of these CallFilters should be equals, 
        //whether they are related to a same Entity, or different ones
        if (!this.getRawDataFilter().equals(otherFilter.getRawDataFilter())) {
            return log.exit(null);
        }
        //now we try to merge the BasicCallFilters
        BasicCallFilter mergedFilter = null;
        if (sameEntity) {
            mergedFilter = this.getCallFilter().mergeSameEntityCallFilter(
                    otherFilter.getCallFilter());
        } else {
            mergedFilter = this.getCallFilter().mergeDiffEntitiesCallFilter(
                    otherFilter.getCallFilter());
        }
        if (mergedFilter == null) {
            return log.exit(null);
        }
        return log.exit(new CompositeCallFilter(mergedFilter, this.getRawDataFilter()));
    }
    //************************************
    //  GETTERS/SETTERS
    //************************************
    /**
     * @return  the {@code BasicCallFilter} used to specify the expression data calls 
     *          that should be generated and retrieved.
     */
    public BasicCallFilter getCallFilter() {
        return this.callFilter;
    }
    /**
     * @return  the {@code RawDataFilter} specifying the raw data that should be used 
     *          to generate the expression data calls.
     */
    public RawDataFilter getRawDataFilter() {
        return this.rawDataFilter;
    }
}
