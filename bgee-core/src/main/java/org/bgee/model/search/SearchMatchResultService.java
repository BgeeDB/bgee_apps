package org.bgee.model.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.rawdata.baseelements.ExperimentAssay;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
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
 * @author  Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @see     SearchMatchResult
 * @since   Bgee 14, Apr. 2019
 */
public class SearchMatchResultService extends CommonService {
    private final static Logger log = LogManager.getLogger(SearchMatchResultService.class.getName());

    @FunctionalInterface
    protected static interface TriFunction<T, U, V, R> {
        public R apply(T t, U u, V v);
    }

    // as for Bgee 15.0 the default timeout was to stringent. Increased it to 3 seconds
    //TODO FB: these two attributes should be Bgee properties
    private static final int SPHINX_CONNECT_TIMEOUT = 3000;
    /**
     * An {@code int} that is the maximum permitted number of results to retrieve
     * from a Sphinx query. Current value: 10,000.
     */
    public static final int MAX_RESULTS = 10000;

    private static final String SPHINX_SEPARATOR = "\\|\\|";

    //XXX FB: maybe these two attributes should also be Bgee properties?
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
     * @see #getSphinxStrainIndex()
     */
    private final String sphinxStrainSearchIndex;
    /**
     * @see #getSphinxAutocompleteIndex()
     */
    private final String sphinxAutocompleteIndex;
    /**
     * @see #getSphinxExperimentIndex()
     */
    private final String sphinxExperimentSearchIndex;
    /**
     * @see #getSphinxAssayIndex()
     */
    private final String sphinxAssaySearchIndex;
    /**
     * In order to avoid querying the GeneBioType for each letter used to search for genes,
     * we instantiate this {@code Map} with the service.
     */
    final Map<Integer, GeneBioType> geneBioTypeMap;

    /**
     * Construct a new {@code SearchMatchResultService} using the provided {@code BgeeProperties}.
     */
    public SearchMatchResultService(BgeeProperties props, ServiceFactory serviceFactory) {
        this(props, serviceFactory, null);
    }
    public SearchMatchResultService(BgeeProperties props, ServiceFactory serviceFactory,
            Map<Integer, GeneBioType> geneBioTypeMap) {
        this(new SphinxClient(props.getSearchServerURL(), Integer.valueOf(props.getSearchServerPort())),
                serviceFactory, props.getSearchGenesIndex(), props.getSearchAnatEntitiesIndex(),
                props.getSearchStrainsIndex(), props.getSearchAutocompleteIndex(),
                props.getSearchExperimentsIndex(), props.getSearchAssaysIndex(),
                geneBioTypeMap);
    }
    /**
     * Construct a new {@code SearchMatchResultService} using the provided {@code SphinxClient}.
     */
    protected SearchMatchResultService(SphinxClient sphinxClient, ServiceFactory serviceFactory,
            String sphinxGeneSearchIndex, String sphinxAnatEntitySearchIndex,
            String sphinxStrainSearchIndex, String sphinxAutocompleteIndex,
            String sphinxExperimentSearchIndex, String sphinxAssaySearchIndex,
            Map<Integer, GeneBioType> geneBioTypeMap) {
        super(serviceFactory);
        sphinxClient.SetConnectTimeout(SPHINX_CONNECT_TIMEOUT);
        this.sphinxClient = sphinxClient;
        this.sphinxGeneSearchIndex = sphinxGeneSearchIndex;
        this.sphinxAnatEntitySearchIndex = sphinxAnatEntitySearchIndex;
        this.sphinxAutocompleteIndex = sphinxAutocompleteIndex;
        this.sphinxStrainSearchIndex = sphinxStrainSearchIndex;
        this.sphinxExperimentSearchIndex = sphinxExperimentSearchIndex;
        this.sphinxAssaySearchIndex = sphinxAssaySearchIndex;
        this.geneBioTypeMap = Collections.unmodifiableMap(
                geneBioTypeMap == null || geneBioTypeMap.isEmpty()?
                        loadGeneBioTypeMap(this.getDaoManager().getGeneDAO()):
                new HashMap<>(geneBioTypeMap));
    }

    /**
     * @return  The {@code String} used as name for genes index
     */
    private String getSphinxGeneSearchIndex() {
        return sphinxGeneSearchIndex;
    }
    /**
     * @return  The {@code String} used as name for anat. entities index
     */
    private String getSphinxAnatEntitySearchIndex() {
        return sphinxAnatEntitySearchIndex;
    }
    /**
     * @return  The {@code String} used as name for strain index
     */
    private String getSphinxStrainSearchIndex() {
        return sphinxStrainSearchIndex;
    }
    /**
     * @return  The {@code String} used as name for gene autocomplete index
     */
    private String getSphinxAutocompleteIndex() {
        return sphinxAutocompleteIndex;
    }

    /**
     * @return  The {@code String} used as name for experiment index
     */
    private String getSphinxExperimentSearchIndex() {
        return sphinxExperimentSearchIndex;
    }
    /**
     * @return  The {@code String} used as name for assay index
     */
    private String getSphinxAssaySearchIndex() {
        return sphinxAssaySearchIndex;
    }

    /**
     * Search the genes.
     *
     * @param searchTerm    A {@code String} containing the query
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are species Ids
     *                      (may be empty to search on all species).
     * @param offset        An {@code Integer} defining at which index to start to retrieve results.
     *                      Can be {@code null} to start from the first index (0).
     * @param limit         An {@code Integer} defining the number of results to retrieve.
     *                      Can be {@code null} to retrieve {@link #MAX_RESULTS} results.
     * @return              A {@code SearchMatchResult} of results (ordered).
     * @throws IllegalArgumentException If {@code offset} is negative, or {@code limit} is less than
     *                                  or equal to 0.
     * @throws IllegalStateException    If the search encountered an error.
     */
    public SearchMatchResult<Gene> searchGenesByTerm(final String searchTerm,
            Collection<Integer> speciesIds, Integer offset, Integer limit)
                    throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}, {}, {}", searchTerm, speciesIds, offset, limit);

        return log.traceExit(this.search(
                (match, term, attrIndexMap) -> getGeneMatch(match, term, attrIndexMap, this.geneBioTypeMap),
                this.getSphinxGeneSearchIndex(), null, Gene.class, searchTerm, speciesIds, false,
                offset, limit));
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
     * @param offset            An {@code Integer} defining at which index to start to retrieve results.
     *                          Can be {@code null} to start from the first index (0).
     * @param limit             An {@code Integer} defining the number of results to retrieve.
     *                          Can be {@code null} to retrieve {@link #MAX_RESULTS} results.
     * @return                  A {@code SearchMatchResult} of results (ordered).
     * @throws IllegalArgumentException If {@code offset} is negative, or {@code limit} is less than
     *                                  or equal to 0.
     * @throws IllegalStateException    If the search encountered an error.
     */
    public SearchMatchResult<AnatEntity> searchAnatEntitiesByTerm(final String searchTerm,
            Collection<Integer> speciesIds, boolean withAnatEntities, boolean withCellTypes,
            Integer offset, Integer limit) throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}, {}, {}", searchTerm, speciesIds, withAnatEntities, withCellTypes,
                offset, limit);

        if (!withAnatEntities && !withCellTypes) {
            throw log.throwing(new IllegalStateException(
                    "At least one of withAnatEntities or withCellTypes has to be true"));
        }
        //Specific configuration of sphinxClient for this query
        if (!withAnatEntities || !withCellTypes) {
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
        return log.traceExit(this.search(
                (match, term, attrIndexMap) -> getAnatEntityMatch(match, term, attrIndexMap),
                this.getSphinxAnatEntitySearchIndex(), null, AnatEntity.class, searchTerm,
                speciesIds, true, offset, limit));
    }

    /**
     * Search strains.
     *
     * @param searchTerm        A {@code String} containing the query
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are species Ids
     *                          (may be empty to search on all species).
     * @param offset            An {@code Integer} defining at which index to start to retrieve results.
     *                          Can be {@code null} to start from the first index (0).
     * @param limit             An {@code Integer} defining the number of results to retrieve.
     *                          Can be {@code null} to retrieve {@link #MAX_RESULTS} results.
     * @return                  A {@code SearchMatchResult} of results (ordered).
     * @throws IllegalArgumentException If {@code offset} is negative, or {@code limit} is less than
     *                                  or equal to 0.
     * @throws IllegalStateException    If the search encountered an error.
     */
    public SearchMatchResult<String> searchStrainsByTerm(final String searchTerm,
            Collection<Integer> speciesIds, Integer offset, Integer limit)
                throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}, {}, {}", searchTerm, speciesIds, offset, limit);

        return log.traceExit(this.search(
                (match, term, attrIndexMap) -> getStrainMatch(match, term, attrIndexMap),
                this.getSphinxStrainSearchIndex(), null, String.class, searchTerm,
                speciesIds, false, offset, limit));
    }

    /**
     * Search experiments and assays.
     *
     * @param searchTerm        A {@code String} containing the query
     * @param offset            An {@code Integer} defining at which index to start to retrieve results.
     *                          Can be {@code null} to start from the first index (0).
     * @param limit             An {@code Integer} defining the number of results to retrieve.
     *                          Can be {@code null} to retrieve {@link #MAX_RESULTS} results.
     * @return                  A {@code SearchMatchResult} of results (ordered).
     * @throws IllegalArgumentException If {@code offset} is negative, or {@code limit} is less than
     *                                  or equal to 0.
     * @throws IllegalStateException    If the search encountered an error.
     */
    public SearchMatchResult<ExperimentAssay> searchExperimentsAndAssaysByTerm(
            String searchTerm, Integer offset, Integer limit)
                throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}, {}", searchTerm, offset, limit);
        checkOffsetLimit(offset, limit);
        int newOffset = offset == null? 0: offset;
        int newLimit = limit == null? MAX_RESULTS: limit;

        //We query both the experiment and assay indexes
        SearchMatchResult<ExperimentAssay> experiments = this.search(
                (match, term, attrIndexMap) -> getExperimentMatch(match, term, attrIndexMap),
                this.getSphinxExperimentSearchIndex(), null, ExperimentAssay.class, searchTerm,
                null, false,
                //we get all possible results for both queries
                null, null);
        SearchMatchResult<ExperimentAssay> assays = this.search(
                (match, term, attrIndexMap) -> getAssayMatch(match, term, attrIndexMap),
                this.getSphinxAssaySearchIndex(), null, ExperimentAssay.class, searchTerm,
                null, false,
                //we get all possible results for both queries
                null, null);

        //Then we merge the results to sort and offset/limit over them all
        int totalMatchCount = experiments.getTotalMatchCount() + assays.getTotalMatchCount();
        List<SearchMatch<ExperimentAssay>> searchMatches = Stream.concat(
                experiments.getSearchMatches().stream(), assays.getSearchMatches().stream())
                .sorted()
                .skip(newOffset)
                .limit(newLimit)
                .collect(Collectors.toList());
        return log.traceExit(new SearchMatchResult<>(totalMatchCount, searchMatches,
                ExperimentAssay.class));
    }

    /**
     * Retrieve autocomplete suggestions for the gene search from the provided {@code searchTerm}.
     *
     * @param searchTerm    A {@code String} containing the query
     * @param limit         An {@code Integer} defining the number of results to retrieve.
     *                      Can be {@code null} to retrieve {@link #MAX_RESULTS} results.
     * @return              A {@code List} of {@code String}s that are suggestions
     *                      for the gene search autocomplete (ordered).
     * @throws IllegalArgumentException If {@code limit} is less than or equal to 0.
     * @throws IllegalStateException    If the search encountered an error.
     */
    public List<String> autocomplete(final String searchTerm, Integer limit)
            throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}", searchTerm, limit);

        return log.traceExit(
                //We hack a little bit the search method to return simply a List of Strings,
                //and not a SearchMatchResult
                this.search((match, term, attrIndexMap) -> new SearchMatch<String>(
                        String.valueOf(match.attrValues.get(0)), null,
                        SearchMatch.MatchSource.ID, String.class),
                        this.getSphinxAutocompleteIndex(),
                        // We use the ranker SPH_RANK_SPH04 to get field equals the exact query first.
                        SphinxClient.SPH_RANK_SPH04,
                        String.class, searchTerm, null, false,
                        // The index of the first element is not necessary,
                        //as it's for the autocomplete we start at 0.
                        0,
                        limit)
                .getSearchMatches().stream()
                .map(sm -> sm.getMatch())
                .collect(Collectors.toList()));
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
     * Generalization of retrieving a {@code SearchMatchResult}.
     *
     * @param getMatchFunction  A {@link SearchMatchResultService.TriFunction TriFunction}
     *                          to map a {@code SphinxMatch} into a {@code SearchMatch}.
     *                          First argument is the {@code SearchMatch}, second argument is
     *                          the {@code String} formatted searched term, third argument is
     *                          a {@code Map} where keys are {@code String}s corresponding to attributes,
     *                          the associated values being {@code Integer}s corresponding
     *                          to index of the attribute
     * @param index             A {@code String} that is the name of the Sphinx index to use
     *                          for the search.
     * @param ranker            An {@code Integer} defining the SPhinx ranker to use
     *                          (as defined in the {@code SphinxClient} class static attributes).
     * @param classType         A {@code Class} that is the type of {@code T} to return in the
     *                          {@code SearchMatchResult}.
     * @param searchTerm        A {@code String} that is the unformatted search term.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species
     *                          for which to retrieve matches. Can be {@code null} or empty.
     * @param multiSpeciesTerms A {@code boolean} defining whether the matches can exist
     *                          in several species. In that case, a special ID value is inserted
     *                          in the Sphinx index, "0", meaning that the match exists
     *                          in all Bgee species.
     * @param offset            An {@code Integer} defining at which index to start to retrieve results.
     *                          Can be {@code null} to start from the first index (0).
     * @param limit             An {@code Integer} defining the number of results to retrieve.
     *                          Can be {@code null} to retrieve {@code MAX_RESULTS} results.
     * @param <T>               The type of object contained in the returned {@code SearchMatchResult}.
     * @return                  A {@code SearchMatchResult} containing the {@code SearchMatch}es
     *                          for the type {@code T}.
     * @throws IllegalArgumentException If {@code offset} is negative, or {@code limit} is less than
     *                                  or equal to 0.
     * @throws IllegalStateException    If the search encountered an error.
     */
    private <T> SearchMatchResult<T> search(
            TriFunction<SphinxMatch, String,  Map<String, Integer>, SearchMatch<T>> getMatchFunction,
            String index, Integer ranker, Class<T> classType, String searchTerm,
            Collection<Integer> speciesIds, boolean multiSpeciesTerms,
            Integer offset, Integer limit) throws IllegalArgumentException, IllegalStateException {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}", getMatchFunction, index, ranker,
                classType, searchTerm, speciesIds, multiSpeciesTerms, offset, limit);
        checkOffsetLimit(offset, limit);
        int newOffset = offset == null? 0: offset;
        int newLimit = limit == null? MAX_RESULTS: limit;

        // We need to get the formatted term here, even if the term is formatted
        // in the method getSphinxResult(), to set correctly SearchMatches.
        String formattedTerm = this.getFormattedTerm(searchTerm);
        SphinxResult result = null;
        try {
            if (speciesIds != null && !speciesIds.isEmpty()) {
                //If a term can exist in several species, in the index a speciesId = 0 means
                //that the term exists for all species. If the speciesId
                // Collection does not yet contains 0 we have to add it.
                Set<Integer> hackedSpeciesIds = new HashSet<>(speciesIds);
                if (multiSpeciesTerms) {
                    hackedSpeciesIds.add(0);
                }
                sphinxClient.SetFilter("speciesId",
                        hackedSpeciesIds.stream().mapToInt(x -> x).toArray(), false);
            }

            //We always retrieve the max allowed number of results,
            //independently of what the client requested,
            //because we reorder the results in this Service, so we need to have a wide net
            //to catch the fish we're looking for and order it properly.
            sphinxClient.SetLimits(0, MAX_RESULTS, MAX_RESULTS);
            if (ranker != null) {
                sphinxClient.SetRankingMode(ranker, null);
            }

            String queryTerm = "^" + formattedTerm + "$ | \"" + formattedTerm + "\"";
            result = sphinxClient.Query(queryTerm, index);

        } catch (SphinxException e) {
            throw log.throwing(new IllegalStateException(
                    "Sphinx search has generated an exception", e));
        }
        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }
        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.traceExit(new SearchMatchResult<>(0, null, classType));
        }

        // get mapping between attributes names and their index
        Map<String, Integer> attrNameToIdx = new HashMap<>();
        for (int idx = 0; idx < result.attrNames.length; idx++) {
            attrNameToIdx.put(result.attrNames[idx], idx);
        }

        // build list of SearchMatch
        List<SearchMatch<T>> searchMatches = Arrays.stream(result.matches)
                .map(m -> getMatchFunction.apply(m, formattedTerm, attrNameToIdx))
                //We need to remove duplicates now, before setting offset and limit.
                //Duplicates can be present if more than one species is selected
                //or if both anat. entities and cell types are queried.
                //And it's more optimized to sort after removing duplicates.
                .collect(Collectors.toSet()).stream()
                .sorted()
                .skip(newOffset)
                .limit(newLimit)
                .collect(Collectors.toList());

        //When we make search for terms that can exist in multiple species
        //(multiSpeciesTerms is true), we can have redundant matches
        //because of the different species. We try as much as possible to get the correct
        //total number of matches.
        int resultCount = result.totalFound;
        //If we receive all results, then this is the total number of matches.
        if (searchMatches.size() < newLimit) {
            resultCount = searchMatches.size();
        }
        return log.traceExit(new SearchMatchResult<>(resultCount, searchMatches, classType));
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
            return log.traceExit(new SearchMatch<Gene>(gene, null,
                    SearchMatch.MatchSource.ID, Gene.class));
        }

        final String geneNameLowerCase = gene.getName().toLowerCase();
        if (geneNameLowerCase.contains(termLowerCase) || geneNameLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<Gene>(gene, null,
                    SearchMatch.MatchSource.NAME, Gene.class));
        }
        final String descriptionLowerCase = gene.getDescription().toLowerCase();
        if (descriptionLowerCase.contains(termLowerCase) || descriptionLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<Gene>(gene, null,
                    SearchMatch.MatchSource.DESCRIPTION, Gene.class));
        }

        // otherwise we fetch term and find the first match
        // even if synonyms are in genes, we need to store which synonym matches the query
        final String geneNameSynonym = this.getMatch(match, "genenamesynonym", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneNameSynonym != null) {
            return log.traceExit(new SearchMatch<Gene>(gene, geneNameSynonym,
                    SearchMatch.MatchSource.SYNONYM, Gene.class));
        }

        final String geneXRef = this.getMatch(match, "genexref", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneXRef != null) {
            return log.traceExit(new SearchMatch<Gene>(gene, geneXRef,
                    SearchMatch.MatchSource.XREF, Gene.class));
        }
        return log.traceExit(new SearchMatch<Gene>(gene, null,
                SearchMatch.MatchSource.MULTIPLE, Gene.class));
    }

    /**
     * Convert a {@code SphinxMatch} into a {@code SearchMatch<AnatEntity>}.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code SearchMatch} that is the converted {@code SphinxMatch}.
     */
    private SearchMatch<AnatEntity> getAnatEntityMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap) {
        log.traceEntry("{}, {}, {}", match, term, attrIndexMap);
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
            return log.traceExit(new SearchMatch<AnatEntity>(anatEntity, null,
                    SearchMatch.MatchSource.ID, AnatEntity.class));
        }

        final String nameLowerCase = anatEntity.getName().toLowerCase();
        if (nameLowerCase.contains(termLowerCase) || nameLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<AnatEntity>(anatEntity, null,
                    SearchMatch.MatchSource.NAME, AnatEntity.class));
        }
        return log.traceExit(new SearchMatch<AnatEntity>(anatEntity, null,
                SearchMatch.MatchSource.MULTIPLE, AnatEntity.class));
    }

    /**
     * Convert a {@code SphinxMatch} into a {@code SearchMatch<String>}.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code SearchMatch} that is the converted {@code SphinxMatch}.
     */
    private SearchMatch<String> getStrainMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap) {
        log.traceEntry("{}, {}, {}", match, term, attrIndexMap);
        String strain = String.valueOf(match.attrValues.get(attrIndexMap.get("strain")));
        //for now the only match can be the name
        return log.traceExit(new SearchMatch<String>(strain, null,
                    SearchMatch.MatchSource.NAME, String.class));

    }

    /**
     * Convert a {@code SphinxMatch} into a {@code SearchMatch<ExperimentAssay>}
     * for an experiment request.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code SearchMatch} that is the converted {@code SphinxMatch}.
     */
    private SearchMatch<ExperimentAssay> getExperimentMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap) {
        log.traceEntry("{}, {}, {}", match, term, attrIndexMap);

        ExperimentAssay expAssay = new ExperimentAssay(
                    String.valueOf(match.attrValues.get(attrIndexMap.get("experimentid"))),
                    String.valueOf(match.attrValues.get(attrIndexMap.get("experimentname"))),
                    String.valueOf(match.attrValues.get(attrIndexMap.get("experimentdescription"))));

        // If the gene name, id or description match there is no term
        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
        //and returns terms, that are then not matched here
        final String termLowerCase = term.toLowerCase();
        final String termLowerCaseEscaped = termLowerCase.replaceAll("\\\\", "");

        final String idLowerCase = expAssay.getId().toLowerCase();
        if (idLowerCase.contains(termLowerCase) || idLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                    SearchMatch.MatchSource.ID, ExperimentAssay.class));
        }
        final String nameLowerCase = expAssay.getName().toLowerCase();
        if (nameLowerCase.contains(termLowerCase) || nameLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                    SearchMatch.MatchSource.NAME, ExperimentAssay.class));
        }
        final String descriptionLowerCase = expAssay.getDescription().toLowerCase();
        if (descriptionLowerCase.contains(termLowerCase) || descriptionLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                    SearchMatch.MatchSource.DESCRIPTION, ExperimentAssay.class));
        }
        return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                SearchMatch.MatchSource.MULTIPLE, ExperimentAssay.class));
    }
    /**
     * Convert a {@code SphinxMatch} into a {@code SearchMatch<ExperimentAssay>}
     * for an assay request.
     *
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @return              The {@code SearchMatch} that is the converted {@code SphinxMatch}.
     */
    private SearchMatch<ExperimentAssay> getAssayMatch(final SphinxMatch match, final String term,
                                   final Map<String, Integer> attrIndexMap) {
        log.traceEntry("{}, {}, {}", match, term, attrIndexMap);

        String id = String.valueOf(match.attrValues.get(attrIndexMap.get("assayid")));
        String name = String.valueOf(match.attrValues.get(attrIndexMap.get("assayname")));
        if (StringUtils.isBlank(name)) {
            name = id;
        }
        ExperimentAssay expAssay = new ExperimentAssay(id, name, null);

        // If the gene name, id or description match there is no term
        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
        //and returns terms, that are then not matched here
        final String termLowerCase = term.toLowerCase();
        final String termLowerCaseEscaped = termLowerCase.replaceAll("\\\\", "");

        final String idLowerCase = expAssay.getId().toLowerCase();
        if (idLowerCase.contains(termLowerCase) || idLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                    SearchMatch.MatchSource.ID, ExperimentAssay.class));
        }
        final String nameLowerCase = expAssay.getName().toLowerCase();
        if (nameLowerCase.contains(termLowerCase) || nameLowerCase.contains(termLowerCaseEscaped)) {
            return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                    SearchMatch.MatchSource.NAME, ExperimentAssay.class));
        }
        return log.traceExit(new SearchMatch<ExperimentAssay>(expAssay, null,
                SearchMatch.MatchSource.MULTIPLE, ExperimentAssay.class));
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

    private static void checkOffsetLimit(Integer offset, Integer limit) {
        log.traceEntry("{}, {}", offset, limit);
        if (offset != null && offset < 0) {
            throw log.throwing(new IllegalArgumentException("offset cannot be negative"));
        }
        if (limit != null && limit <= 0) {
            throw log.throwing(new IllegalArgumentException(
                    "limit cannot be less than or equal to 0"));
        }
        log.traceExit();
    }
}