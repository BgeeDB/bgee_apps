package org.bgee.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.species.Species;

public interface RPackageDisplay {
	
	void displayCalls(List<String> attrs, Stream<ExpressionCall> callsStream);
	void displayAnatEntities (List<String> attrs, Stream<AnatEntity> anatEntitiesStream);
	void displayAERelations(List<String> attrs, Map<String, Set<String>> anatEntityRelations);
	void displaySpecies(List<String> attrs, List<Species> SpeciesList);
//	void displayAERelations(List<String> attrs, Ontology<AnatEntity, String> anatEntityRelations);

}
