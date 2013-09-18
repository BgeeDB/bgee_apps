package org.bgee.model.expressiondata.querytools.filters;

/**
 * A {@link Filter} based on expression data calls. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface CallFilter extends Filter {
    public CallFilter mergeSameGeneCallFilter(CallFilter callFilter);
    public CallFilter mergeDifferentGeneCallFilter(CallFilter callFilter);
}
