package org.bgee.model.gene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.CommonService;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.species.Species;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

/**
 * Class allowing to manage and retrieve {@code SearchMatchResult}s.
 *
 * @author  Valentine Rech de Laval
 * @author  Julien Wollbrett
 * @version Bgee 15, Oct 2022
 * @see     SearchMatchResult
 * @since   Bgee 14, Apr. 2019
 */
public class SearchMatchResultService extends CommonService {
    
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(SearchMatchResultService.class.getName());

    // as for Bgee 15.0 the default timeout was to stringent. Increased it to 3 seconds
    private static final int SPHINX_CONNECT_TIMEOUT = 3000;
    public static final int SPHINX_MAX_RESULTS = 10000;
    private static final String SPHINX_SEPARATOR = "\\|\\|";
    private static final Integer SPHINX_SEARCH_ANAT_ENTITIES = 1;
    private static final Integer SPHINX_SEARCH_CELL_TYPES = 2;

    /**
     * @see #getSphinxClient()
     */
    private final SphinxClient sphinxClient;
    /**
     * @see #getSphinxGenesIndex()
     */
    private final String sphinxGeneSearchIndex;
    /**
     * @see #getSphinxAnatEntitiesIndex()
     */
    private final String sphinxAnatEntitySearchIndex;
    /**
     * @see #getSphinxAutocompleteIndex()
     */
    private final String sphinxAutocompleteIndex;

    /**
     * Construct a new {@code SearchMatchResultService} using the provided {@code BgeeProperties}.
     */
    public SearchMatchResultService(BgeeProperties props, ServiceFactory serviceFactory) {
        this(new SphinxClient(props.getSearchServerURL(), Integer.valueOf(props.getSearchServerPort())),
                serviceFactory, props.getSearchGenesIndex(), props.getSearchAnatEntitiesIndex(),
                props.getSearchAutocompleteIndex());
    }
    /**
     * Construct a new {@code SearchMatchResultService} using the provided {@code SphinxClient}.
     */
    public SearchMatchResultService(SphinxClient sphinxClient, ServiceFactory serviceFactory, String sphinxGeneSearchIndex,
            String sphinxAnatEntitySearchIndex, String sphinxAutocompleteIndex) {
        super(serviceFactory);
        sphinxClient.SetConnectTimeout(SPHINX_CONNECT_TIMEOUT);
        this.sphinxClient = sphinxClient;
        this.sphinxGeneSearchIndex = sphinxGeneSearchIndex;
        this.sphinxAnatEntitySearchIndex = sphinxAnatEntitySearchIndex;
        this.sphinxAutocompleteIndex = sphinxAutocompleteIndex;
    }

    /**
     * @return  The {@code SphinxClient} used by this {@code SearchMatchResultService}.
     */
    public SphinxClient getSphinxClient() {
        return sphinxClient;
    }
    /**
     * @return  The {@code String} used as name for genes index
     */
    public String getSphinxGeneSearchIndex() {
        return sphinxGeneSearchIndex;
    }
    /**
     * @return  The {@code String} used as name for anat. entities index
     */
    public String getSphinxAnatEntitySearchIndex() {
        return sphinxAnatEntitySearchIndex;
    }
    /**
     * @return  The {@code String} used as name for autocomplete index
     */
    public String getSphinxAutocompleteIndex() {
        return sphinxAutocompleteIndex;
    }

    /**
     * Search the genes.
     *
     * @param searchTerm    A {@code String} containing the query 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are species Ids
     *                      (may be empty to search on all species).
     * @param limitStart    An {@code int} representing the index of the first element to return.
     * @param resultPerPage An {@code int} representing the number of elements to return
     * @return              A {@code SearchMatchResult} of results (ordered).
     */
    public SearchMatchResult<Gene> searchGenesByTerm(final String searchTerm, Collection<Integer> speciesIds,
                                        int limitStart, int resultPerPage) {
        log.traceEntry("{}, {}, {}, {}", searchTerm, speciesIds, limitStart, resultPerPage);

        if (speciesIds != null && !speciesIds.isEmpty()) {
            try {
                sphinxClient.SetFilter("speciesId", speciesIds.stream().mapToInt(x -> x).toArray(), false);
            } catch (SphinxException e) {
                throw log.throwing(new IllegalStateException(
                        "Sphinx search has generated an exception", e));
            }
        }

        // We need to get the formatted term here, even if the term is formatted 
        // in the method getSphinxResult(), to set correctly SearchMatches.
        String formattedTerm = this.getFormattedTerm(searchTerm);

        SphinxResult result = this.getSphinxResult(formattedTerm, limitStart, resultPerPage, this.getSphinxGeneSearchIndex(), null);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.traceExit(new SearchMatchResult<Gene>(0, null));
        }

        // get mapping between attributes names and their index
        Map<String, Integer> attrNameToIdx = new HashMap<>();
        for (int idx = 0; idx < result.attrNames.length; idx++) {
            attrNameToIdx.put(result.attrNames[idx], idx);
        }

        final Map<Integer, GeneBioType> geneBioTypeMap = Collections.unmodifiableMap(
                loadGeneBioTypeMap(this.getDaoManager().getGeneDAO()));

        // build list of SearchMatch
        List<SearchMatch<Gene>> geneMatches = Arrays.stream(result.matches)
                .map(m -> getGeneMatch(m, formattedTerm, attrNameToIdx, geneBioTypeMap))
                .sorted()
                .collect(Collectors.toList());

        return log.traceExit(new SearchMatchResult<Gene>(result.totalFound, geneMatches));
    }

    /**
     * Search anat. entities and/or cell types
     *
     * @param searchTerm        A {@code String} containing the query
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are species Ids
     *                          (may be empty to search on all species).
     * @param withAnatEntities  A {@code boolean} defining   whether anatomical entities have to be
     *                          retrieved or not. if <code>true</code> then anatomical entities are
     *                          retrieved. Not both withAnatEntities and withCellTypes can be false
     *                          at the same time.
     * @param withCellTypes     A {@code boolean} defining   whether cell types have to be
     *                          retrieved or not. if <code>true</code> then cell types are
     *                          retrieved. Not both withAnatEntities and withCellTypes can be false
     *                          at the same time.
     * @param limitStart        An {@code int} representing the index of the first element to return.
     * @param resultPerPage     An {@code int} representing the number of elements to return
     * @return                  A {@code SearchMatchResult} of results (ordered).
     */
    public SearchMatchResult<NamedEntity<String>> searchAnatEntitiesByTerm(final String searchTerm,
            Collection<Integer> speciesIds, boolean withAnatEntities, boolean withCellTypes, int limitStart,
            int resultPerPage) {
        log.traceEntry("{}, {}, {}, {}", searchTerm, speciesIds, withAnatEntities, withCellTypes,
                limitStart, resultPerPage);

        if (speciesIds != null && !speciesIds.isEmpty()) {
            try {
                //in the index a speciesId = 0 means that the anat. entity exists for all species. If the speciesId
                // Collection does not yet contains 0 we have to add it.
                Set<Integer> hackedSpeciesIds = new HashSet<Integer>(speciesIds);
                if (!hackedSpeciesIds.contains(0)) {
                    hackedSpeciesIds.add(0);
                }
                sphinxClient.SetFilter("speciesId", hackedSpeciesIds.stream().mapToInt(x -> x).toArray(), false);
            } catch (SphinxException e) {
                throw log.throwing(new IllegalStateException(
                        "Sphinx search has generated an exception", e));
            }
        }
        if (!withAnatEntities && !withCellTypes) {
            throw log.throwing(new IllegalStateException("At least one of withAnatEntities or withCellTypes"
                    + " has to be true"));
        // filtering done only if not both withAnatEntities and withCellType are true.
        //TODO: update the filtering once the new index is available
        } else if (!(withAnatEntities && withCellTypes)) {
            try {
                if(withAnatEntities) {
                    sphinxClient.SetFilter("type", SPHINX_SEARCH_ANAT_ENTITIES, false);
                }
                if(withCellTypes) {
                    sphinxClient.SetFilter("type", SPHINX_SEARCH_CELL_TYPES, false);
                }
            } catch (SphinxException e) {
                throw log.throwing(new IllegalStateException(
                        "Sphinx search has generated an exception", e));
            }
        }

        // We need to get the formatted term here, even if the term is formatted
        // in the method getSphinxResult(), to set correctly NamedEntityMatch.
        String formattedTerm = this.getFormattedTerm(searchTerm);

        SphinxResult result = this.getSphinxResult(formattedTerm, limitStart, resultPerPage,
                this.getSphinxAnatEntitySearchIndex(), null);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.traceExit(new SearchMatchResult<NamedEntity<String>>(0, null));
        }

        // get mapping between attributes names and their index
        Map<String, Integer> attrNameToIdx = new HashMap<>();
        for (int idx = 0; idx < result.attrNames.length; idx++) {
            attrNameToIdx.put(result.attrNames[idx], idx);
        }

        // build list of SearchMatch
        List<SearchMatch<NamedEntity<String>>> anatEntityMatches = Arrays.stream(result.matches)
                .map(m -> getAnatEntityMatch(m, formattedTerm, attrNameToIdx))
                .sorted()
                // collector removing duplicates. Duplicates can be present if more than one species is
                // selected or if both anat. entities and cell types are queried.
                .collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<SearchMatch<NamedEntity<String>>>(Comparator.comparing(m -> m))),
                        ArrayList::new));

        return log.traceExit(new SearchMatchResult<NamedEntity<String>>(anatEntityMatches.size(),
                anatEntityMatches));
    }

    /**
     * Generate the formatted term.
     * 
     * @param searchTerm    A {@code String} that is the term to be formatted. 
     * @return              The {@code String} that is the formatted.
     */
    private String getFormattedTerm(String searchTerm) {
        return StringUtils.normalizeSpace(searchTerm);
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
        log.traceEntry("{}, {}", searchTerm, resultPerPage);

        // The index of the first element is not necessary, as it's for the autocomplete we start at 0.
        // We use the ranker SPH_RANK_SPH04 to get field equals the exact query first.
        SphinxResult result = this.getSphinxResult(searchTerm, 0, resultPerPage, this.getSphinxAutocompleteIndex(),
                SphinxClient.SPH_RANK_SPH04);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.traceExit(new ArrayList<>());
        }

        // build list of propositions
        List<String> propositions = Arrays.stream(result.matches)
                .map(m -> String.valueOf(m.attrValues.get(0)))
                //sort by the shortest matched terms first
                .sorted(Comparator.comparingInt(m -> m.length()))
                .collect(Collectors.toList());

        return log.traceExit(propositions);
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
        log.traceEntry("{}, {}, {}, {}, {}", searchTerm, limitStart, resultPerPage, index, ranker);

        try {
            sphinxClient.SetLimits(limitStart, resultPerPage, SPHINX_MAX_RESULTS);
            if (ranker != null) {
                sphinxClient.SetRankingMode(ranker, null);
            }
            String queryTerm = "^" + this.getFormattedTerm(searchTerm) + "$ | \"" + this.getFormattedTerm(searchTerm) + "\"";
            return log.traceExit(sphinxClient.Query(queryTerm, index));
        } catch (SphinxException e) {
            throw log.throwing(new IllegalStateException(
                    "Sphinx search has generated an exception", e));
        }
    }

    /**
     * Convert a {@code SphinxMatch} into a {@code SearchMatch<Gene>}.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code SearchMatch} that is the converted {@code SphinxMatch}.
     */
    private SearchMatch<Gene> getGeneMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap,
                                   final Map<Integer, GeneBioType> geneBioTypeMap) {
        log.traceEntry("{}, {}, {}, {}", match, term, attrIndexMap, geneBioTypeMap);

        String attrs = (String) match.attrValues.get(attrIndexMap.get("genenamesynonym"));
        String[] split = attrs.split(SPHINX_SEPARATOR);
        List<String> synonyms = Arrays.stream(split).collect(Collectors.toList());

        Species species = new Species(((Long) match.attrValues.get(attrIndexMap.get("speciesid"))).intValue(),
                String.valueOf(match.attrValues.get(attrIndexMap.get("speciescommonname"))),
                null, String.valueOf(match.attrValues.get(attrIndexMap.get("speciesgenus"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("speciesname"))),
                null, null, null, null, null, null, null,
                ((Long) match.attrValues.get(attrIndexMap.get("speciesdisplayorder"))).intValue());
        
        Gene gene = new Gene(String.valueOf(match.attrValues.get(attrIndexMap.get("geneid"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("genename"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("genedescription"))),
                synonyms,
                null, // x-refs are null because, at that point, we don't know data source of them
                species,
                geneBioTypeMap.get(((Long) match.attrValues.get(attrIndexMap.get("genebiotypeid")))
                        .intValue()),
                ((Long) match.attrValues.get(attrIndexMap.get("genemappedtogeneidcount"))).intValue());

        // If the gene name, id or description match there is no term
        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
        //and returns terms, that are then not matched here
        final String termLowerCase = term.toLowerCase();
        final String termLowerCaseEscaped = termLowerCase.replaceAll("\\\\", "");

        final String geneIdLowerCase = gene.getGeneId().toLowerCase();
        if (geneIdLowerCase.contains(termLowerCase) || geneIdLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<Gene>(gene, null, SearchMatch.MatchSource.ID));
        }

        final String geneNameLowerCase = gene.getName().toLowerCase();
        if (geneNameLowerCase.contains(termLowerCase) || geneNameLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<Gene>(gene, null, SearchMatch.MatchSource.NAME));
        }
        final String descriptionLowerCase = gene.getDescription().toLowerCase();
        if (descriptionLowerCase.contains(termLowerCase) || descriptionLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<Gene>(gene, null, SearchMatch.MatchSource.DESCRIPTION));
        }

        // otherwise we fetch term and find the first match
        // even if synonyms are in genes, we need to store which synonym matches the query   
        final String geneNameSynonym = this.getMatch(match, "genenamesynonym", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneNameSynonym != null) {
            return log.traceExit(new SearchMatch<Gene>(gene, geneNameSynonym, SearchMatch.MatchSource.SYNONYM));
        }

        final String geneXRef = this.getMatch(match, "genexref", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneXRef != null) {
            return log.traceExit(new SearchMatch<Gene>(gene, geneXRef, SearchMatch.MatchSource.XREF));
        }
        return log.traceExit(new SearchMatch<Gene>(gene, geneXRef, SearchMatch.MatchSource.MULTIPLE));
    }

    /**
     * Convert a {@code SphinxMatch} into a {@code SearchMatch<NamedEntity<String>>}.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code SearchMatch} that is the converted {@code SphinxMatch}.
     */
    private SearchMatch<NamedEntity<String>> getAnatEntityMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap) {
        log.traceEntry("{}, {}, {}, {}", match, term, attrIndexMap);
        AnatEntity anatEntity = new AnatEntity(String.valueOf(match.attrValues
                .get(attrIndexMap.get("anatentityid"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("anatentityname"))), null);

        // If the gene name, id or description match there is no term
        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
        //and returns terms, that are then not matched here
        final String termLowerCase = term.toLowerCase();
        final String termLowerCaseEscaped = termLowerCase.replaceAll("\\\\", "");

        final String idLowerCase = anatEntity.getId().toLowerCase();
        if (idLowerCase.contains(termLowerCase) || idLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<NamedEntity<String>>(anatEntity, null,
                    SearchMatch.MatchSource.ID));
        }

        final String nameLowerCase = anatEntity.getName().toLowerCase();
        if (nameLowerCase.contains(termLowerCase) || nameLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<NamedEntity<String>>(anatEntity, null,
                    SearchMatch.MatchSource.NAME));
        }
        return log.traceExit(new SearchMatch<NamedEntity<String>>(anatEntity, null,
                SearchMatch.MatchSource.MULTIPLE));
    }

    private String getMatch(SphinxMatch match, String attribute, Map<String, Integer> attrIndexMap,
                            String termLowerCase, String termLowerCaseEscaped) {
        log.traceEntry("{}, {}, {}, {}, {}" ,match, attribute, attrIndexMap, termLowerCase, 
                termLowerCaseEscaped);

        String attrs = (String) match.attrValues.get(attrIndexMap.get(attribute));
        String[] split = attrs.toLowerCase().split(SPHINX_SEPARATOR);

        List<String> terms = Arrays.stream(split)
                .filter(s ->  s.contains(termLowerCase) ||
                        //Fix issue with term search such as "upk\3a". MySQL does not consider
                        //the backslash and returns terms, that are then not matched here
                        s.contains(termLowerCaseEscaped))
                .collect(Collectors.toList());
        if (terms.size() > 0) {
            return log.traceExit(terms.get(0));
        }
        return log.traceExit((String) null);
    }
}
