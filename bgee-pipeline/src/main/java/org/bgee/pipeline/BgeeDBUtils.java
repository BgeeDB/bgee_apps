package org.bgee.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;


/**
 * This class provides convenient common methods that retrieve data from Bgee.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class BgeeDBUtils {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(BgeeDBUtils.class.getName());
    

    /**
     * Retrieves all species IDs present into the Bgee database. 
     * 
     * @param speciesDAO    A {@code SpeciesDAO} to use to retrieve information about species 
     *                      from the Bgee data source.
     * @return A {@code Set} of {@code String}s containing species IDs of the Bgee database.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static List<String> getSpeciesIdsFromDb(SpeciesDAO speciesDAO) throws DAOException {
        log.entry(speciesDAO);

        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID);
        
        try (SpeciesTOResultSet rsSpecies = speciesDAO.getAllSpecies()) {
            List<String> speciesIdsInBgee = new ArrayList<String>();
            while (rsSpecies.next()) {
                speciesIdsInBgee.add(rsSpecies.getTO().getId());
            }
            return log.exit(speciesIdsInBgee); 
        } 
    }
    
    /**
     * Retrieve and validate species IDs from the Bgee data source. If {@code speciesIds} 
     * is {@code null} or empty, this method will return the IDs of all the species present 
     * in Bgee (as returned by {@link #getSpeciesIdsFromDb(SpeciesDAO)}). Otherwise, 
     * this method will check that all provided IDs correspond to actual species in Bgee, and 
     * will return the validated {@code Collection} provided as argument. If an ID 
     * is not found in Bgee, this method will throw an {@code IllegalArgumentException}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are IDs of species, 
     *                      to be validated.
     * @param speciesDAO    A {@code SpeciesDAO} to use to retrieve information about species 
     *                      from the Bgee data source
     * @return              A {@code List} of {@code String}s that are the IDs of all species 
     *                      in Bgee, if {@code speciesIds} was {@code null} or empty, 
     *                      otherwise, returns the argument {@code speciesIds} itself.
     * @throws IllegalArgumentException If {@code speciesIds} is not {@code null} nor empty 
     *                                  and an ID is not found in Bgee.
     */
    public static List<String> checkAndGetSpeciesIds(List<String> speciesIds, SpeciesDAO speciesDAO) 
            throws IllegalArgumentException {
        log.entry(speciesIds, speciesDAO);
        
        List<String> speciesIdsFromDb = BgeeDBUtils.getSpeciesIdsFromDb(speciesDAO); 
        if (speciesIds == null || speciesIds.isEmpty()) {
            return log.exit(speciesIdsFromDb);
        } else if (!speciesIdsFromDb.containsAll(speciesIds)) {
            //copy to avoid modifying user input, maybe the caller 
            //will recover from the exception (but it should not...)
            List<String> debugSpeciesIds = new ArrayList<String>(speciesIds);
            debugSpeciesIds.removeAll(speciesIdsFromDb);
            throw log.throwing(new IllegalArgumentException("Some species IDs " +
                    "could not be found in Bgee: " + debugSpeciesIds));
        } 
        return log.exit(speciesIds);
    }
    
    /**
     * Retrieves is_a/part_of anatomical entity relatives from the Bgee data source 
     * with parent anatomical entities associated to their descendants. In the returned 
     * {@code Map}, keys are IDs of anatomical entities that are the target of a relation, 
     * the associated value containing their associated source IDs.
     * <p>
     * Relations are retrieved for the species provided through {@code speciesIds}. 
     * If {@code speciesIds} is {@code null} or empty, relations for any species are retrieved. 
     * Only relations with a {@code RelationType} {@code ISA_PARTOF} are considered, 
     * but with any {@code RelationStatus} ({@code REFLEXIVE}, {@code DIRECT}, 
     * {@code INDIRECT}). 
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between anatomical entities from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of anatomical entities that are the target of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated sources. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getAnatEntityChildrenFromParents(Set<String> speciesIds, 
            RelationDAO relationDAO) throws DAOException {
        log.entry(speciesIds, relationDAO);
        return log.exit(BgeeDBUtils.getIsAPartOfRelativesFromDb(
                speciesIds, true, true, relationDAO));
        
    }
    /**
     * Retrieves is_a/part_of anatomical entity relatives from the Bgee data source 
     * with descent anatomical entities associated to their parents. In the returned 
     * {@code Map}, keys are IDs of anatomical entities that are the source of a relation, 
     * the associated value containing their associated target IDs.
     * <p>
     * Relations are retrieved for the species provided through {@code speciesIds}. 
     * If {@code speciesIds} is {@code null} or empty, relations for any species are retrieved. 
     * Only relations with a {@code RelationType} {@code ISA_PARTOF} are considered, 
     * but with any {@code RelationStatus} ({@code REFLEXIVE}, {@code DIRECT}, 
     * {@code INDIRECT}). 
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between anatomical entities from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of anatomical entities that are the source of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated targets. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getAnatEntityParentsFromChildren(Set<String> speciesIds, 
            RelationDAO relationDAO) throws DAOException {
        log.entry(speciesIds, relationDAO);
        return log.exit(BgeeDBUtils.getIsAPartOfRelativesFromDb(
                speciesIds, true, false, relationDAO));
        
    }
    /**
     * Retrieves is_a/part_of developmental stage relatives from the Bgee data source 
     * with parent stages associated to their descendants. In the returned 
     * {@code Map}, keys are IDs of stages that are the target of a relation, 
     * the associated value containing their associated source IDs.
     * <p>
     * Relations are retrieved for the species provided through {@code speciesIds}. 
     * If {@code speciesIds} is {@code null} or empty, relations for any species are retrieved. 
     * Only relations with a {@code RelationType} {@code ISA_PARTOF} are considered, 
     * but with any {@code RelationStatus} ({@code REFLEXIVE}, {@code DIRECT}, 
     * {@code INDIRECT}). 
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between stages from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of stages that are the target of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated sources. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getStageChildrenFromParents(Set<String> speciesIds, 
            RelationDAO relationDAO) throws DAOException {
        log.entry(speciesIds, relationDAO);
        return log.exit(BgeeDBUtils.getIsAPartOfRelativesFromDb(
                speciesIds, false, true, relationDAO));
        
    }
    
    /**
     * Retrieves is_a/part_of relatives from the Bgee data source. The returned {@code Map} 
     * contains either IDs of anatomical entities retrieved from relations between 
     * anatomical entities (if {@code anatEntityRelatives} is {@code true}), or IDs 
     * of developmental stages retrieved from relations between developmental stages 
     * (if {@code anatEntityRelatives} is {@code false}). If {@code targetsBySource} 
     * is {@code true}, the keys in the returned {@code Map} are source IDs of the relations, 
     * the associated value being a {@code Set} that are the IDs of their associated targets. 
     * If {@code targetsBySource} is {@code false}, the keys in the returned {@code Map} 
     * are target IDs of the relations, the associated value being a {@code Set} that are 
     * the IDs of their associated sources. 
     * <p>
     * Relations are retrieved for the species provided through {@code speciesIds}. 
     * If {@code speciesIds} is {@code null} or empty, relations for any species are retrieved. 
     * Only relations with a {@code RelationType} {@code ISA_PARTOF} are considered, 
     * but with any {@code RelationStatus} ({@code REFLEXIVE}, {@code DIRECT}, 
     * {@code INDIRECT}). 
     * 
     * @param speciesIds            A {@code Set} of {@code String}s that are the IDs of species 
     *                              to retrieve relations for. Can be {@code null} or empty.
     * @param anatEntityRelatives   A {@code boolean} defining whether to retrieve relations 
     *                              for anatomical entities, or for developmental stages. 
     *                              If {@code true}, relations between anatomical entities 
     *                              are retrieved.
     * @param targetsBySource       A {@code boolean} defining whether the returned {@code Map} 
     *                              will associate a source to its targets, or a target 
     *                              to its sources. If {@code true}, it will associate 
     *                              a source to its targets.
     * @param relationDAO           A {@code RelationDAO} to use to retrieve information about 
     *                              relations between anatomical entities or developmental stages
     *                              from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of either the sources or the targets of relations, the associated 
     *                      value being {@code Set} of {@code String}s that are the IDs 
     *                      of either the associated targets, or sources, respectively. 
     *                      If {@code targetsBySource} is {@code true}, it will associate 
     *                      sources to their targets. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    private static Map<String, Set<String>> getIsAPartOfRelativesFromDb(Set<String> speciesIds, 
            boolean anatEntityRelatives, boolean childrenFromParents, RelationDAO relationDAO) 
                    throws DAOException {
        log.entry(speciesIds, anatEntityRelatives, childrenFromParents, relationDAO);
        
        //store original attributes to restore relationDAO in proper state afterwards.
        Collection<RelationDAO.Attribute> attributes = relationDAO.getAttributes();
        
        relationDAO.setAttributes(RelationDAO.Attribute.SOURCEID, 
                RelationDAO.Attribute.TARGETID);
    
        // get direct, indirect, and reflexive is_a/part_of relations 
        // No need to close the ResultSet, it's done by getAllTOs().
        List<RelationTO> relTOs = null;
        if (anatEntityRelatives) {
            relTOs = relationDAO.getAnatEntityRelations(
                    speciesIds, EnumSet.of(RelationType.ISA_PARTOF), null).getAllTOs();
        } else {
            relTOs = relationDAO.getStageRelations(speciesIds, null).getAllTOs();
        }
        
        //now, populate Map where keys are sourceId and values the associated targetIds, 
        //or the opposite, depending on descendantsByParent
        Map<String, Set<String>> relativesMap = new HashMap<String, Set<String>>();
        for (RelationTO relTO: relTOs) {
            String key = null;
            String value = null;
            if (childrenFromParents) {
                key = relTO.getTargetId();
                value = relTO.getSourceId();
            } else {
                key = relTO.getSourceId();
                value = relTO.getTargetId();
            }
            Set<String> relatives = relativesMap.get(key);
            if (relatives == null) {
                relatives = new HashSet<String>();
                relativesMap.put(key, relatives);
            }
            relatives.add(value);
        }
        
        //restore relationDAO in proper state
        relationDAO.setAttributes(attributes);
        
        return log.exit(relativesMap);
    }

    /**
     * Retrieves all expression calls for given species in a {@code Map} associating gene IDs to 
     * {@code ExpressionCallTO}s, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the expression calls to use.
     * @param exprCallDAO       A {@code ExpressionCallDAO} to use to retrieve information about 
     *                          expression calls from the Bgee data source.
     * @return                  A {@code LinkedHashMap} associating gene IDs to 
     *                          {@code ExpressionCallTO}s of the given species. Returns a 
     *                          {@code LinkedHashMap} to keep the order in which 
     *                          {@code ExpressionCallTO}s are retrieved.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    public static LinkedHashMap<String, List<ExpressionCallTO>> getExpressionCallsByGeneId(
            Set<String> speciesIds, ExpressionCallDAO exprCallDAO) throws DAOException {
        log.entry(speciesIds, exprCallDAO);
        
        log.debug("Start retrieving expression calls for species: {}", speciesIds);
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        List<ExpressionCallTO> exprTOs = exprCallDAO.getExpressionCalls(params).getAllTOs();
        log.debug("Done retrieving expression calls for species {}, {} retrieved.", 
                speciesIds, exprTOs.size());
        
        LinkedHashMap<String, List<ExpressionCallTO>> map = 
                new LinkedHashMap<String, List<ExpressionCallTO>>();
        log.debug("Generating Map from genes to expression calls for species {}...", 
                speciesIds);
        for (ExpressionCallTO exprTO : exprTOs) {
            log.trace("Expression call: {}", exprTO);
            List<ExpressionCallTO> curExprAsSet = map.get(exprTO.getGeneId());
            if (curExprAsSet == null) {
                log.trace("Create new map key: {}", exprTO.getGeneId());
                curExprAsSet = new ArrayList<ExpressionCallTO>();
                map.put(exprTO.getGeneId(), curExprAsSet);
            }
            curExprAsSet.add(exprTO);
        }
        log.debug("Done generating Map from genes to expression calls for species {}, {} entries.", 
                speciesIds, map.size());
        
        return log.exit(map);        
    }

    /**
     * Retrieves all no-expression calls for given species in a {@code Map} associating gene IDs to 
     * {@code NoExpressionCallTO}s, present into the Bgee database.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species 
     *                          allowing to filter the no-expression calls to use.
     * @param noExprCallDAO     A {@code NoExpressionCallDAO} to use to retrieve information about 
     *                          no-expression calls from the Bgee data source.
     * @return                  A {@code LinkedHashMap} associating gene IDs to 
     *                          {@code NoExpressionCallTO}s of the given species. Returns a 
     *                          {@code LinkedHashMap} to keep the order in which 
     *                          {@code NoExpressionCallTO}s are retrieved.
     * @throws DAOException     If an error occurred while getting the data from the Bgee database.
     */
    public static LinkedHashMap<String, List<NoExpressionCallTO>> getNoExpressionCallsByGeneId(
            Set<String> speciesIds, NoExpressionCallDAO noExprCallDAO) throws DAOException {
        log.entry(speciesIds, noExprCallDAO);

        log.debug("Start retrieving no-expression calls for species: {}", speciesIds);
        NoExpressionCallParams params = new NoExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        List<NoExpressionCallTO> noExprTOs = noExprCallDAO.getNoExpressionCalls(params).getAllTOs();
        log.debug("Done retrieving no-expression calls for species {}, {} retrieved.", 
                speciesIds, noExprTOs.size());
        
        LinkedHashMap<String, List<NoExpressionCallTO>> map = 
                new LinkedHashMap<String, List<NoExpressionCallTO>>();
        log.debug("Generating Map from genes to no-expression calls for species {}...", 
                speciesIds);
        for (NoExpressionCallTO noExprTO : noExprTOs) {
            log.trace("No-expression call: {}", noExprTO);
            List<NoExpressionCallTO> curNoExprAsSet = map.get(noExprTO.getGeneId());
            if (curNoExprAsSet == null) {
                log.trace("Create new map key: {}", noExprTO.getGeneId());
                curNoExprAsSet = new ArrayList<NoExpressionCallTO>();
                map.put(noExprTO.getGeneId(), curNoExprAsSet);
            }
            curNoExprAsSet.add(noExprTO);
        }
        log.debug("Done generating Map from genes to no-expression calls for species {}, {} entries.", 
                speciesIds, map.size());
        
        return log.exit(map);        
    }
}
