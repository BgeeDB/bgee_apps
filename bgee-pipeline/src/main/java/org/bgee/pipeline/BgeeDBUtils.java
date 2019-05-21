package org.bgee.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;


/**
 * This class provides convenient common methods that retrieve data from Bgee.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 july
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
     * @return A {@code Set} of {@code Integer}s containing species IDs of the Bgee database.
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static List<Integer> getSpeciesIdsFromDb(SpeciesDAO speciesDAO) throws DAOException {
        log.entry(speciesDAO);

        try (SpeciesTOResultSet rsSpecies = speciesDAO.getAllSpecies(EnumSet.of(SpeciesDAO.Attribute.ID))) {
            List<Integer> speciesIdsInBgee = new ArrayList<>();
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
     * @param speciesIds    A {@code List} of {@code Integer}s that are IDs of species, 
     *                      to be validated.
     * @param speciesDAO    A {@code SpeciesDAO} to use to retrieve information about species 
     *                      from the Bgee data source
     * @return              A {@code List} of {@code Integer}s that are the IDs of all species 
     *                      in Bgee, if {@code speciesIds} was {@code null} or empty, 
     *                      otherwise, returns the argument {@code speciesIds} itself.
     * @throws IllegalArgumentException If {@code speciesIds} is not {@code null} nor empty 
     *                                  and an ID is not found in Bgee.
     */
    public static List<Integer> checkAndGetSpeciesIds(List<Integer> speciesIds, SpeciesDAO speciesDAO) 
            throws IllegalArgumentException {
        log.entry(speciesIds, speciesDAO);
        
        List<Integer> speciesIdsFromDb = BgeeDBUtils.getSpeciesIdsFromDb(speciesDAO); 
        if (speciesIds == null || speciesIds.isEmpty()) {
            return log.exit(speciesIdsFromDb);
        } else if (!speciesIdsFromDb.containsAll(speciesIds)) {
            //copy to avoid modifying user input, maybe the caller 
            //will recover from the exception
            List<Integer> debugSpeciesIds = new ArrayList<>(speciesIds);
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
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between anatomical entities from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of anatomical entities that are the target of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated sources. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getAnatEntityChildrenFromParents(Set<Integer> speciesIds, 
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
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between anatomical entities from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of anatomical entities that are the source of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated targets. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getAnatEntityParentsFromChildren(Set<Integer> speciesIds, 
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
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between stages from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of stages that are the target of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated sources. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getStageChildrenFromParents(Set<Integer> speciesIds, 
            RelationDAO relationDAO) throws DAOException {
        log.entry(speciesIds, relationDAO);
        return log.exit(BgeeDBUtils.getIsAPartOfRelativesFromDb(
                speciesIds, false, true, relationDAO));
        
    } 
    /**
     * Retrieves is_a/part_of developmental stage relatives from the Bgee data source 
     * with child stages associated to their parents. In the returned 
     * {@code Map}, keys are IDs of stages that are the source of a relation, 
     * the associated value containing their associated target IDs.
     * <p>
     * Relations are retrieved for the species provided through {@code speciesIds}. 
     * If {@code speciesIds} is {@code null} or empty, relations for any species are retrieved. 
     * Only relations with a {@code RelationType} {@code ISA_PARTOF} are considered, 
     * but with any {@code RelationStatus} ({@code REFLEXIVE}, {@code DIRECT}, 
     * {@code INDIRECT}). 
     * 
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species 
     *                      to retrieve relations for. Can be {@code null} or empty.
     * @param relationDAO   A {@code RelationDAO} to use to retrieve information about 
     *                      relations between stages from the Bgee data source.
     * @return              A {@code Map} where keys are {@code String}s representing the IDs 
     *                      of stages that are the source of a relation, 
     *                      the associated value being a {@code Set} of {@code String}s 
     *                      that are the IDs of their associated targets. 
     * @throws DAOException If an error occurred while getting the data from the Bgee database.
     */
    public static Map<String, Set<String>> getStageParentsFromChildren(Set<Integer> speciesIds, 
            RelationDAO relationDAO) throws DAOException {
        log.entry(speciesIds, relationDAO);
        return log.exit(BgeeDBUtils.getIsAPartOfRelativesFromDb(
                speciesIds, false, false, relationDAO));
        
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
     * @param speciesIds            A {@code Set} of {@code Integer}s that are the IDs of species 
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
    private static Map<String, Set<String>> getIsAPartOfRelativesFromDb(Set<Integer> speciesIds, 
            boolean anatEntityRelatives, boolean childrenFromParents, RelationDAO relationDAO) 
                    throws DAOException {
        log.entry(speciesIds, anatEntityRelatives, childrenFromParents, relationDAO);
        
        //store original attributes to restore relationDAO in proper state afterwards.
        Collection<RelationDAO.Attribute> attributes = relationDAO.getAttributes();
        
        relationDAO.setAttributes(RelationDAO.Attribute.SOURCE_ID, 
                RelationDAO.Attribute.TARGET_ID);
        // get direct, indirect, and reflexive is_a/part_of relations 
        RelationTOResultSet<String> relTORs = null;
        Map<String, Set<String>> relativesMap = new HashMap<String, Set<String>>();
        try {
            if (anatEntityRelatives) {
                relTORs = relationDAO.getAnatEntityRelationsBySpeciesIds(
                        speciesIds, EnumSet.of(RelationType.ISA_PARTOF), null);
            } else {
                relTORs = relationDAO.getStageRelationsBySpeciesIds(speciesIds, null);
            }
            
            //now, populate Map where keys are sourceId and values the associated targetIds, 
            //or the opposite, depending on descendantsByParent
            while (relTORs.next()) {
                RelationTO<String> relTO = relTORs.getTO();
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
        } finally {
            if (relTORs != null) {
                relTORs.close();
            }
        }
        
        //restore relationDAO in proper state
        relationDAO.setAttributes(attributes);
        
        return log.exit(relativesMap);
    }
    
    /**
     * Retrieve a mapping from IDs to {@code TransferObject}s using the {@code DAOResultSet} 
     * {@code rs}.
     * <p>
     * {@code rs} is closed by this method before exiting.
     * 
     * @param rs    A {@code DAOResultSet} returning an {@code T} when calling 
     *              the method {@code DAOResultSet#getTO()}. It should not have been closed, 
     *              and the method {@code next} should not have been already called.
     * @param <T>   The type of {@code EntityTO} in {@code rs}.
     * @param <U>   The type of ID in {@code EntityTO} in {@code rs}.
     * @return      A {@code Map} where keys are {@code U}s corresponding to 
     *              entity IDs, the associated values being {@code T}s 
     *              corresponding to entity TOs. 
     * @throws IllegalArgumentException If {@code rs} does not allow to retrieve EntityTO IDs.
     * @throws IllegalStateException	If several TOs associated to a same ID.
     */
    private static <U extends Comparable<U>, T extends EntityTO<U>> Map<U, T> generateTOsByIdsMap(DAOResultSet<T> rs) {
        log.entry(rs);
        
        Map<U, T> tosByIds = new HashMap<>();
        try {
            while (rs.next()) {
                T entityTO = rs.getTO();
                if (entityTO.getId() == null) {
                    throw log.throwing(new IllegalArgumentException("The provided DAOResultSet " +
                            "does not allow to retrieve EntityTO IDs"));                	
                }

                T previousValue = tosByIds.put(entityTO.getId(), entityTO);
                if (previousValue != null) {
                    throw log.throwing(new IllegalStateException("Several TOs associated to " +
                            "a same ID: " + entityTO.getId() + " - " + previousValue + 
                            " - " + entityTO));
                }
            }
        } finally {
            rs.close();
        }
        return log.exit(tosByIds);
    }

    /**
     * Retrieve a mapping from IDs to names using the {@code DAOResultSet} {@code rs}.
     * <p>
     * {@code rs} is closed by this method before exiting.
     * 
     * @param rs    A {@code DAOResultSet} returning an {@code T} when calling 
     *              the method {@code DAOResultSet#getTO()}. It should not have been closed, 
     *              and the method {@code next} should not have been already called.
     * @param <T>   The type of {@code EntityTO} in {@code rs}.
     * @param <U>   The type of ID in {@code EntityTO} in {@code rs}.
     * @return      A {@code Map} where keys are {@code String}s corresponding to 
     *              entity IDs, the associated values being {@code String}s 
     *              corresponding to entity names. 
     * @throws IllegalArgumentException If {@code rs} does not allow to retrieve EntityTO ID or name.
     * @throws IllegalStateException    If several TOs associated to a same ID.
     */
    //XXX: Currently, we keep specific methods to be able to store and restore specific attributes.
    //     This could be generic using Java 8, but we are just lazy to implement it now.

    private static <U extends Comparable<U>, T extends NamedEntityTO<U>> Map<U, String> generateNamesByIdsMap(DAOResultSet<T> rs) 
    		throws IllegalArgumentException, IllegalStateException {
        log.entry(rs);
        
        Map<U, String> namesByIds = new HashMap<>();
        for (Entry<U, T> entry: generateTOsByIdsMap(rs).entrySet()) {
            if (entry.getValue().getName() == null) {
                throw log.throwing(new IllegalArgumentException("The provided DAOResultSet " +
                        "does not allow to retrieve EntityTO names"));                   
            }
            
            String previousName = namesByIds.put(entry.getKey(), entry.getValue().getName());
            // Keys are unique in a Map, so the previous value should be always null
            assert(previousName == null);
        }
            
        return log.exit(namesByIds);
    }

    /**
     * Retrieve from the data source a mapping from gene IDs to gene names for genes 
     * belonging to the requested species. 
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      for which we want to retrieve gene IDs-names mapping.
     * @param geneDAO       A {@code GeneDAO} to use to retrieve information about genes 
     *                      from the data source.
     * @return              A {@code Map} where keys are {@code String}s corresponding to 
     *                      gene IDs, the associated values being {@code String}s 
     *                      corresponding to gene names. 
     */
    //XXX: Currently, we keep this specific method to be able to store and restore 
    //     specific attributes. This could be generic in generateNamesByIdsMap using Java 8, 
    //     but we are just lazy to implement it now. 
    public static Map<Integer, String> getGeneNamesByIds(Set<Integer> speciesIds, GeneDAO geneDAO) {
        log.entry(speciesIds, geneDAO);
        log.debug("Start retrieving gene names for species: {}", speciesIds);
        //store original attributes to restore geneDAO in proper state afterwards.
        Collection<GeneDAO.Attribute> attributes = geneDAO.getAttributes();
        geneDAO.setAttributes(GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        
        Map<Integer, String> geneNamesByIds = 
                generateNamesByIdsMap(geneDAO.getGenesBySpeciesIds(speciesIds));
        
        //restore geneDAO in proper state
        geneDAO.setAttributes(attributes);
        
        log.debug("Done retrieving gene names for species: {}, {} names retrieved", 
                speciesIds, geneNamesByIds.size());
        return log.exit(geneNamesByIds);
    }
    
    /**
     * Retrieve from the data source a mapping from gene IDs to gene TOs for genes 
     * belonging to the requested species, using attributes stored in {@code geneDAO}.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      for which we want to retrieve gene IDs-names mapping.
     * @param geneDAO       A {@code GeneDAO} to use to retrieve information about genes 
     *                      from the data source.
     * @return              A {@code Map} where keys are {@code String}s corresponding to 
     *                      gene IDs, the associated values being {@code GeneTO}s 
     *                      corresponding to gene TOs. 
     */
    public static Map<Integer, GeneTO> getGeneTOsByIds(Set<Integer> speciesIds, GeneDAO geneDAO) {
        log.entry(speciesIds, geneDAO);
        
        log.debug("Start retrieving gene TOs for species: {}", speciesIds);
        
        Map<Integer, GeneTO> geneTOsByIds = 
                generateTOsByIdsMap(geneDAO.getGenesBySpeciesIds(speciesIds));

        log.debug("Done retrieving gene TOs for species: {}, {} TOs retrieved", 
                speciesIds, geneTOsByIds.size());
        
        return log.exit(geneTOsByIds);
    }
    
    /**
     * Retrieve from the data source a mapping from stage IDs to stage names for stages 
     * belonging to the requested species. 
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      for which we want to retrieve stage IDs-names mapping.
     * @param stageDAO      A {@code StageDAO} to use to retrieve information about stages 
     *                      from the data source.
     * @return              A {@code Map} where keys are {@code String}s corresponding to 
     *                      stage IDs, the associated values being {@code String}s 
     *                      corresponding to stage names. 
     */
    //XXX: Currently, we keep this specific method to be able to store and restore 
    //     specific attributes. This could be generic in generateNamesByIdsMap using Java 8, 
    //     but we are just lazy to implement it now. 
    public static Map<String, String> getStageNamesByIds(Set<Integer> speciesIds, 
            StageDAO stageDAO) {
        log.entry(speciesIds, stageDAO);
        log.debug("Start retrieving stage names for species: {}", speciesIds);
        //store original attributes to restore stageDAO in proper state afterwards.
        Collection<StageDAO.Attribute> attributes = stageDAO.getAttributes();
        stageDAO.setAttributes(StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
        
        Map<String, String> stageNamesByIds = generateNamesByIdsMap(
                stageDAO.getStagesBySpeciesIds(speciesIds));
        
        //restore stageDAO in proper state
        stageDAO.setAttributes(attributes);
        
        log.debug("Done retrieving stage names for species: {}, {} names retrieved", 
                speciesIds, stageNamesByIds.size());
        return log.exit(stageNamesByIds);
    }
    
    /**
     * Retrieve from the data source a mapping from anatomical entity IDs to names 
     * for anatomical entities belonging to the requested species. 
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      for which we want to retrieve anatomical entities' 
     *                      IDs-names mapping.
     * @param anatEntityDAO An {@code AnatEntityDAO} to use to retrieve information about 
     *                      anatomical entities from the data source.
     * @return              A {@code Map} where keys are {@code String}s corresponding to 
     *                      anatomical entity IDs, the associated values being {@code String}s 
     *                      corresponding to anatomical entity names. 
     */
    //XXX: Currently, we keep this specific method to be able to store and restore 
    //     specific attributes. This could be generic in generateNamesByIdsMap using Java 8, 
    //     but we are just lazy to implement it now. 
    public static Map<String, String> getAnatEntityNamesByIds(Set<Integer> speciesIds, 
            AnatEntityDAO anatEntityDAO) {
        log.entry(speciesIds, anatEntityDAO);
        log.debug("Start retrieving anatomical entity names for species: {}", speciesIds);
        //store original attributes to restore anatEntityDAO in proper state afterwards.
        Collection<AnatEntityDAO.Attribute> attributes = anatEntityDAO.getAttributes();
        anatEntityDAO.setAttributes(AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        
        Map<String, String> anatEntityNamesByIds = 
                generateNamesByIdsMap(anatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds));
        
        //restore anatEntityDAO in proper state
        anatEntityDAO.setAttributes(attributes);
        
        log.debug("Done retrieving anatomical entity names for species: {}, {} names retrieved", 
                speciesIds, anatEntityNamesByIds.size());
        return log.exit(anatEntityNamesByIds);
    }

    /**
     * Retrieve from the data source a mapping from CIO IDs to names.
     * 
     * @param cioStatementDAO	A {@code CIOStatementDAO} to use to retrieve information about 
     *                          CIO statements from the data source.
     * @return                  A {@code Map} where keys are {@code String}s corresponding to 
     *                          CIO IDs, the associated values being {@code String}s 
     *                          corresponding to CIO names. 
     * @throws DAOException     If an error occurred while getting the data from 
     * 							the Bgee data source.
     */
    //XXX: Currently, we keep this specific method to be able to store and restore 
    //     specific attributes. This could be generic in generateNamesByIdsMap using Java 8, 
    //     but we are just lazy to implement it now. 
    public static Map<String, String> getCIOStatementNamesByIds(CIOStatementDAO cioStatementDAO) 
    		throws DAOException {
        log.entry(cioStatementDAO);
        
        log.debug("Start retrieving all CIO names");
        
        //store original attributes to restore cioStatementDAO in proper state afterwards.
        Collection<CIOStatementDAO.Attribute> attributes = cioStatementDAO.getAttributes();
        cioStatementDAO.setAttributes(CIOStatementDAO.Attribute.ID, CIOStatementDAO.Attribute.NAME);

        Map<String, String> cioByNames = 
                generateNamesByIdsMap(cioStatementDAO.getAllCIOStatements());
        
        //restore anatEntityDAO in proper state
        cioStatementDAO.setAttributes(attributes);

        log.debug("Done retrieving CIO names, {} names retrieved", cioByNames.size());
        
        return log.exit(cioByNames);
    }

    /**
     * Retrieve from the data source a mapping from CIO IDs to CIO TOs, 
     * using attributes stored in {@code cioStatementDAO}.
     * 
     * @param cioStatementDAO   A {@code CIOStatementDAO} to use to retrieve information about 
     *                          CIO statements from the data source.
     * @return                  A {@code Map} where keys are {@code String}s corresponding to 
     *                          CIO IDs, the associated values being {@code CIOStatementTO}s 
     *                          corresponding to CIO TOs. 
     * @throws DAOException     If an error occurred while getting the data from the Bgee data source.
     */
    public static Map<String, CIOStatementTO> getCIOStatementTOsByIds(CIOStatementDAO cioStatementDAO) 
            throws DAOException {
        log.entry();
        
        log.debug("Start retrieving all CIO TOs");
        
        Map<String, CIOStatementTO> cioByIds = 
                generateTOsByIdsMap(cioStatementDAO.getAllCIOStatements());
        
        log.debug("Done retrieving CIO TOs, {} TOs retrieved", cioByIds.size());
        
        return log.exit(cioByIds);
    }
}
