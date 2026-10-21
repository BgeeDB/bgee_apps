package org.bgee.view;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.NamedEntity;
import org.bgee.model.ontology.OntologyElement;
import org.bgee.model.species.Species;

public interface RPackageDisplay{

    /**
     * Display rows for topAnat: expression calls, conditions or relations between conditions,
     * read from the files generated for it, or the genes of a background.
     *
     * @param columns   A {@code List} of {@code String}s that are the names of the columns,
     *                  in the order the values of each row are provided in.
     * @param rows      A {@code Stream} of {@code String[]}s that are the values of each row,
     *                  one value per column of {@code columns}.
     */
    void displayRows(List<String> columns, Stream<String[]> rows);
	void displaySpecies(List<String> attrs, List<Species> SpeciesList);
	<T extends NamedEntity<U> & OntologyElement<T, U>,U extends Comparable<U>>
	    void displayPropagation(List<String> attrs, Set<T> descendants);

}
