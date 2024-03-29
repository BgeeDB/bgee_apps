package org.bgee.view;

import java.util.Collection;
import java.util.Set;

import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.controller.CommandGene.SpeciesGeneListResponse;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.search.SearchMatchResult;

/**
 * Interface defining methods to be implemented by views related to {@code Gene}s.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Nov. 2015
 */
public interface GeneDisplay {
    
    /**
     * Displays the result of a gene search. 
     * @param searchTerm    A {@code String} that is the query of the gene search. 
     * @param result        A {@code GeneMatchResult} that are the results of the query. 
     */
    void displayGeneSearchResult(String searchTerm, SearchMatchResult<Gene> result);

    /**
     * Display a list of all genes belonging to a species.
     *
     * @param speciesGeneListResponse   The {@code SpeciesGeneListResponse} containing the response
     *                                  to querying all {@code Gene}s for a {@code Species}.
     */
    void displaySpeciesGeneList(SpeciesGeneListResponse speciesGeneListResponse);

    /**
     * Displays the general information for a list of genes.
     *
     * @param genes  The {@code Collection} of {@code Gene}s for which we want to display general information for.
     */
    void displayGeneGeneralInformation(Collection<Gene> genes);

    /**
     * Displays homology information for a gene.
     *
     * @param geneHomologs  The {@code GeneHomologs} containing the homologs of the requested genes.
     */
    void displayGeneHomologs(GeneHomologs geneHomologs);

    /**
     * Displays XRef information for a gene.
     *
     * @param gene  The {@code Gene} for which to display XRef information.
     */
    void displayGeneXRefs(Gene gene);

    /**
     * Displays expression results for the requested gene.
     *
     * @param geneExpressionResponse    The {@code GeneExpressionResponse} containing the expression results
     *                                  for the requested gene.
     */
    void displayGeneExpression(GeneExpressionResponse geneExpressionResponse);

    /**
     * Displays a {@code Set} of {@code Gene}s.
     * 
     * @param genes     A {@code Set} of {@code Gene}s to be displayed.
     */
    void displayGeneChoice(Set<Gene> genes);
}
