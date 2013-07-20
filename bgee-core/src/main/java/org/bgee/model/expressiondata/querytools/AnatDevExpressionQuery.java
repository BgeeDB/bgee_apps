package org.bgee.model.expressiondata.querytools;

import java.util.Collection;

import org.bgee.model.expressiondata.querytools.AnatDevRequirement.GeneCallRequirement;

/**
 * This class allows to retrieve <code>AnatomicalEntity</code>s and 
 * <code>Stage</code>s based on their gene expression data. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnatDevExpressionQuery 
{
    
    
    restriction on organs? (e.g., jacknife on HOGs for my analyses): only in organs/never in organs kind of?
    		useful for all anaylses or only this class?
    				
    				also, parameters "with mean expression level by experiment", probably useful for all query tools
    				this could be compute for each gene for an organ query, or for each organ on a gene query
    				this could be a last view, after data count, raw data: mean expression compared from raw data
    			    and maybe we can compute a rank for all organs for each experiment independently, something like that
}
