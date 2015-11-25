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

	void displayGenePage();
	
	void displayGene(Gene gene, List<ExpressionCall> calls, Map<String, AnatEntity> anatEntitiesMap, 
			Map<String, DevStage> devStageMap);
	
}
