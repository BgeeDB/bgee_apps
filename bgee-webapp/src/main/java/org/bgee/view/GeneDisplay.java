package org.bgee.view;

import java.util.List;

import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.gene.Gene;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author Philippe Moret
 * @version Bgee 13, Nov.
 * @since   Bgee 13, Nov.
 */
public interface GeneDisplay {


	/**
	 * Displays the default gene page (when no arguments are given)
	 */
    //XXX: what is this method used for?
	void displayGenePage();
	
	/**
	 * Displays information about a specific {@code Gene}.
	 * 
	 * @param gene             The {@code Gene} to be displayed.
	 * @param calls            A {@code List} of {@code ExpressionCall}s related to {@code gene}.
	 * @param conditionUtils   A {@code ConditionUtils} loaded from all {@code Condition}s 
	 *                         retrieved from the {@code ExpressionCall}s in {@code calls}.
	 */
	void displayGene(Gene gene, List<ExpressionCall> calls, ConditionUtils conditionUtils);
}
