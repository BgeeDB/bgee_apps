package org.bgee.view;

import java.util.List;

import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.gene.Gene;

/**
 * 
 * @author Philippe Moret
 * @version Bgee 13, Nov.
 * @since   Bgee 13, Nov.
 */
public interface GeneDisplay {

	void displayGenePage();
	
	void displayGene(Gene gene, List<ExpressionCall> calls);
	
}
