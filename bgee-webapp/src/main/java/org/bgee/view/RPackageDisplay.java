package org.bgee.view;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.NamedEntity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyElement;
import org.bgee.model.species.Species;

public interface RPackageDisplay{
	
	void displayCalls(List<String> attrs, Stream<ExpressionCall> callsStream);

    /**
     * Display the rows read from a generated expression call file.
     *
     * @param columns   A {@code List} of {@code String}s that are the names of the columns,
     *                  in the order the values of each row are provided in.
     * @param rows      A {@code Stream} of {@code String[]}s that are the values of each row,
     *                  one value per column of {@code columns}.
     */
    void displayCallsFromFile(List<String> columns, Stream<String[]> rows);
	void displayAnatEntities (List<String> attrs, Stream<AnatEntity> anatEntitiesStream);
	void displayAERelations(List<String> attrs, Ontology<AnatEntity, String> anatEntityOnt);
	void displaySpecies(List<String> attrs, List<Species> SpeciesList);
	<T extends NamedEntity<U> & OntologyElement<T, U>,U extends Comparable<U>>
	    void displayPropagation(List<String> attrs, Set<T> descendants);
//	void displayAERelations(List<String> attrs, Ontology<AnatEntity, String> anatEntityRelations);

}
