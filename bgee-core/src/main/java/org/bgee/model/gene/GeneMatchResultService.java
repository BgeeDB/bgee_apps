package org.bgee.model.gene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.species.Species;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bgee.model.gene.GeneMatch.MatchSource.DESCRIPTION;
import static org.bgee.model.gene.GeneMatch.MatchSource.SYNONYM;
import static org.bgee.model.gene.GeneMatch.MatchSource.XREF;

/**
 * Class allowing to manage and retrieve {@code GeneMatchResult}s.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @see     GeneMatchResult
 * @since   Bgee 14, Apr. 2019
 */
public class GeneMatchResultService {
    
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(GeneMatchResultService.class.getName());


    private static final String SPHINX_SEPARATOR = "\\|\\|";
    /**
     * @see #getSphinxClient()
     */
    private final SphinxClient sphinxClient;

    /**
     * Construct a new {@code GeneMatchResultService} using the provided {@code BgeeProperties}. 
     */
    public GeneMatchResultService(BgeeProperties props) {
        this(new SphinxClient(props.getSearchServerURL(), Integer.valueOf(props.getSearchServerPort())));
    }

    /**
     * Construct a new {@code GeneMatchResultService} using the provided {@code SphinxClient}. 
     */
    public GeneMatchResultService(SphinxClient sphinxClient) {
        this.sphinxClient = sphinxClient;
    }

    /**
     * @return  The {@code SphinxClient} used by this {@code GeneMatchResultService}.
     */
    public SphinxClient getSphinxClient() {
        return sphinxClient;
    }

    /**
     * Search the genes.
     *
     * @param searchTerm    A {@code String} containing the query 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are species Ids
     *                      (may be empty to search on all species).
     * @param limitStart    An {@code int} representing the index of the first element to return.
     * @param resultPerPage An {@code int} representing the number of elements to return
     * @return              A {@code GeneMatchResult} of results (ordered).
     */
    public GeneMatchResult searchByTerm(final String searchTerm, Collection<Integer> speciesIds,
                                        int limitStart, int resultPerPage) {
        log.entry(searchTerm, speciesIds, limitStart, resultPerPage);

        if (speciesIds != null && !speciesIds.isEmpty()) {
            throw new UnsupportedOperationException("Search with species parameter is not implemented");
        }

        SphinxResult result = this.getSphinxResult(searchTerm, limitStart, resultPerPage, "bgee_genes", null);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.exit(new GeneMatchResult(0, null));
        }

        // get mapping between attributes names and their index
        Map<String, Integer> attrNameToIdx = new HashMap<>();
        for (int idx = 0; idx < result.attrNames.length; idx++) {
            attrNameToIdx.put(result.attrNames[idx], idx);
        }

        // build list of GeneMatch
        List<GeneMatch> geneMatches = Arrays.stream(result.matches)
                .map(m -> getGeneMatch(m, searchTerm, attrNameToIdx))
                .sorted()
                .collect(Collectors.toList());

        return log.exit(new GeneMatchResult(result.totalFound, geneMatches));
    }

    /**
     * Retrieve autocomplete suggestions for the gene search from the provided {@code searchTerm}.
     *
     * @param searchTerm    A {@code String} containing the query 
     * @param resultPerPage An {@code int} representing the number of elements to return
     * @return              A {@code List} of {@code String}s that are suggestions
     *                      for the gene search autocomplete (ordered).
     */
    public List<String> autocomplete(final String searchTerm, int resultPerPage) {
        log.entry(searchTerm, resultPerPage);

        // The index of the first element is not necessary, as it's for the autocomplete we start at 0.
        // We use the ranker SPH_RANK_SPH04 to get field equals the exact query first.
        SphinxResult result = this.getSphinxResult(searchTerm, 0, resultPerPage, "bgee_autocomplete",
                SphinxClient.SPH_RANK_SPH04);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.exit(new ArrayList<>());
        }

        // build list of propositions
        List<String> propositions = Arrays.stream(result.matches)
                .map(m -> String.valueOf(m.attrValues.get(0)))
                .collect(Collectors.toList());

        return log.exit(propositions);
    }

    /**
     * Retrieve sphinx result from provided parameters.
     *
     * @param searchTerm    A {@code String} that is the query.
     * @param limitStart    An {@code int} that is the index of the first element to return.
     * @param resultPerPage An {@code int} that is the number of elements to return.
     * @param index         A {@code String} that is the index to query.
     * @param ranker        An {@code Integer} that is the ranking mode.
     * @return              The {@code SphinxResult} that is the result of the query.
     */
    private SphinxResult getSphinxResult(String searchTerm, int limitStart, int resultPerPage,
                                         String index, Integer ranker) {
        log.entry(searchTerm, limitStart, resultPerPage, index, ranker);
        try {
            sphinxClient.SetLimits(limitStart, resultPerPage);
            if (ranker != null) {
                sphinxClient.SetRankingMode(ranker, null);
            }
            return log.exit(sphinxClient.Query(searchTerm, index));
        } catch (SphinxException e) {
            throw log.throwing(new IllegalStateException(
                    "Sphinx search has generated an exception", e));
        }
    }

    /**
     * Convert a {@code SphinxMatch} into a {@code GeneMatch}.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code GeneMatch} that is the converted {@code SphinxMatch}.
     */
    private GeneMatch getGeneMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap) {
        log.entry(match, term, attrIndexMap);

        String attrs = (String) match.attrValues.get(attrIndexMap.get("genenamesynonym"));
        String[] split = attrs.toLowerCase().split(SPHINX_SEPARATOR);
        List<String> synonyms = Arrays.stream(split).collect(Collectors.toList());

        Species species = new Species(((Long) match.attrValues.get(attrIndexMap.get("speciesid"))).intValue(),
                String.valueOf(match.attrValues.get(attrIndexMap.get("speciescommonname"))),
                null, String.valueOf(match.attrValues.get(attrIndexMap.get("speciesgenus"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("speciesname"))),
                null, null, ((Long) match.attrValues.get(attrIndexMap.get("speciesdisplayorder"))).intValue());
        
        Gene gene = new Gene(String.valueOf(match.attrValues.get(attrIndexMap.get("geneid"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("genename"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("genedescription"))),
                synonyms,
                null, // x-refs are null because, at that point, we don't know data source of them
                species,
                ((Long) match.attrValues.get(attrIndexMap.get("genemappedtogeneidcount"))).intValue());

        // If the gene name, id or description match there is no term
        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
        //and returns terms, that are then not matched here
        final String termLowerCase = term.toLowerCase();
        final String termLowerCaseEscaped = termLowerCase.replaceAll("\\\\", "");

        final String geneIdLowerCase = gene.getEnsemblGeneId().toLowerCase();
        if (geneIdLowerCase.contains(termLowerCase) || geneIdLowerCase.contains(termLowerCaseEscaped)) {
            return log.exit(new GeneMatch(gene, null, GeneMatch.MatchSource.ID));
        }

        final String geneNameLowerCase = gene.getName().toLowerCase();
        if (geneNameLowerCase.contains(termLowerCase) || geneNameLowerCase.contains(termLowerCaseEscaped)) {
            return log.exit(new GeneMatch(gene, null, GeneMatch.MatchSource.NAME));
        }
        final String descriptionLowerCase = gene.getDescription().toLowerCase();
        if (descriptionLowerCase.contains(termLowerCase) || descriptionLowerCase.contains(termLowerCaseEscaped)) {
            return log.exit(new GeneMatch(gene, null, DESCRIPTION));
        }

        // otherwise we fetch term and find the first match
        // even if synonyms are in genes, we need to store which synonym matches the query   
        final String geneNameSynonym = this.getMatch(match, "genenamesynonym", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneNameSynonym != null) {
            return log.exit(new GeneMatch(gene, geneNameSynonym, SYNONYM));
        }

        final String geneXRef = this.getMatch(match, "genexref", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneXRef != null) {
            return log.exit(new GeneMatch(gene, geneXRef, XREF));
        }

        throw log.throwing(new IllegalStateException("No match found. Term: " + term
                + " Match;" + match.attrValues));
    }

    private String getMatch(SphinxMatch match, String attribute, Map<String, Integer> attrIndexMap,
                            String termLowerCase, String termLowerCaseEscaped) {
        log.entry(match, attribute, attrIndexMap, termLowerCase, termLowerCaseEscaped);

        String attrs = (String) match.attrValues.get(attrIndexMap.get(attribute));
        String[] split = attrs.toLowerCase().split(SPHINX_SEPARATOR);

        List<String> terms = Arrays.stream(split)
                .filter(s ->  s.contains(termLowerCase) ||
                        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
                        //and returns terms, that are then not matched here
                        s.contains(termLowerCaseEscaped))
                .collect(Collectors.toList());
        if (terms.size() > 0) {
            return log.exit(terms.get(0));
        }
        return log.exit(null);
    }
}
