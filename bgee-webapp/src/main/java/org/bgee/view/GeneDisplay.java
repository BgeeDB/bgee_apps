package org.bgee.view;

import java.util.List;
import java.util.Map;

import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.gene.Gene;

/**
 * 
 * @author Philippe Moret
 * @version Bgee 13, Nov.
 * @since   Bgee 13, Nov.
 */
public interface GeneDisplay {


	/**
	 * Displays the default gene page (when no arguments are given)
	 */
	void displayGenePage();
	
	/**
	 * Displays the gene page of a given {@code Gene}, using the provided information.
	 * @param gene             The {@code Gene} to be displayed
	 * @param calls            The {@code List} of {@code ExpressionCall} ordered by relevance
	 * @param anatEntitiesMap  The {@code Map} of {@code AnatEntity} expected to contain all {@code AnatEntity}
	 * 						   referenced in the calls
	 * @param devStageMap      The {@code Map} of {@code DevStage} expected to contain all {@code DevStage}
	 * 						   referenced in the calls 
	 */
	void displayGene(Gene gene, List<ExpressionCall> calls, Map<String, AnatEntity> anatEntitiesMap, 
			Map<String, DevStage> devStageMap);
	
}
