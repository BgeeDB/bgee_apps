package org.bgee.pipeline.expression;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class used to delete or update conflicting no-expression calls in Bgee and associated 
 * raw data. See notably {@link #filterNoExpressionCalls(List)}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class FilterNoExprCalls extends MySQLDAOUser {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(FilterNoExprCalls.class.getName());
    
    /**
     * A {@code Set} of {@code String}s that are the IDs of basic no-expression calls 
     * that were invalidated and that were based, amongst others, on Affymetrix data. 
     */
    private Set<String> affyNoExprIds;
    /**
     * A {@code Set} of {@code String}s that are the IDs of basic no-expression calls 
     * that were invalidated and that were based, amongst others, on <em>in situ</em> data. 
     */
    private Set<String> inSituNoExprIds;
    /**
     * A {@code Set} of {@code String}s that are the IDs of basic no-expression calls 
     * that were invalidated and that were based, amongst others, on RNA-Seq data. 
     */
    private Set<String> rnaSeqNoExprIds;
    /**
     * A {@code Set} of {@code String}s that are the IDs of no-expression calls 
     * that were completely invalidated and that should be deleted from the data source. 
     */
    private Set<String> toDeleteNoExprIds;
    /**
     * A {@code Set} of {@code NoExpressionCallTO}s representing no-expression calls 
     * that must be updated, because at least one of the data type they were based on 
     * showed a conflict expression/no-expression, but not all of them (otherwise, 
     * their ID would be stored in {@link #toDeleteNoExprIds}).
     */
    private Set<NoExpressionCallTO> noExprCallsToUpdate;
    
    /**
     * Default constructor using default {@code MySQLDAOManager}.
     */
    public FilterNoExprCalls() {
        this(null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public FilterNoExprCalls(MySQLDAOManager manager) {
        super(manager);
        this.initAttributes();
    }
    
    /**
     * Initialize attributes of this class, that need to be reinitialized when iterating 
     * several species, for cleaning no-expression calls one species at a time.  
     */
    private void initAttributes() {
        log.entry();
        this.affyNoExprIds = new HashSet<String>();
        this.inSituNoExprIds = new HashSet<String>();
        this.rnaSeqNoExprIds = new HashSet<String>();
        this.toDeleteNoExprIds = new HashSet<String>();
        this.noExprCallsToUpdate = new HashSet<NoExpressionCallTO>();
        log.exit();
    }
    
    /**
     * Filter conflicting basic no-expression calls in the data source, either for 
     * the requested species, or for all species in the database if {@code speciesIds} 
     * is {@code null} or empty. 
     * <p> 
     * A no-expression call is considered to be conflicting when there exists 
     * an expression call for the same gene, in the same anatomical structure/developmental 
     * stage, or in a child anatomical structure/developmental stage. 
     * <p>
     * i) If all the data types are conflicting, the no-expression call is deleted: for instance, 
     * a no-expression call produced by Affymetrix data, while there exists a corresponding 
     * expression call produced by techniques including Affymetrix data; ii) If only 
     * some data types are conflicting, the no-expression call is updated to set the corresponding 
     * data types to 'no data': for instance, if a no-expression call was produced 
     * by Affymetrix data and RNA-Seq data, while there exists a corresponding 
     * expression call produced by Affymetrix data, the no-expression call will be updated 
     * to keep unchanged the RNA-Seq data type, but to change the Affymetrix data type to 'no data'.
     * <p>
     * Corresponding raw data are also updated to set to null their no-expression ID 
     * and to set their reason for exclusion to 'no-expression conflict'.
     * <p>
     * There is no implementation to filter propagated global no-expression calls, because 
     * with the existing code, filtering would be incomplete: for instance, 
     * if a no-expression call in organ A, was propagated to child organs B and C, and 
     * there is expression of the same gene in organ B: the no-expression calls in A and B 
     * would be removed, but not in C, thus this is inconsistent. So, basically, 
     * basic no-expression calls should be filtered before propagating them, and they should not 
     * be filtered on the basis of the global calls. 
     * 
     * @param speciesIds                A {@code List} of {@code String}s that are the IDs 
     *                                  of the species for which the filtering is requested. 
     *                                  If empty or {@code null}, the filtering will be 
     *                                  performed for all species in the database.
     * @throws IllegalArgumentException If an ID provided through {@code SpeciesId}s is not 
     *                                  found in the database.
     */
    public void filterNoExpressionCalls(List<String> speciesIds) 
            throws IllegalArgumentException{
        log.entry(speciesIds);
        
        try {
            List<String> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                    this.getSpeciesDAO());

            for (String speciesId: speciesIdsToUse) {
                this.startTransaction();
                
                this.filterNoExpressionCalls(speciesId);
                
                this.commit();
            }
        } finally {
            this.closeDAO();
        }
        
        log.exit();
    }
    
    /**
     * Perform same operations as {@link #filterNoExpressionCalls(List)} but 
     * for a single species and without dealing with operations related to connection 
     * to the data source. Notably, this method is not responsible for starting 
     * nor committing a transaction, nor to close the {@code DAOManager} afterwards. 
     * These are the responsibilities of the caller. This is useful when another class, 
     * already engaged in a transaction, requires a no-expression filtering for a species. 
     * 
     * @param speciesId A {@code String} that is the ID of a species for which 
     *                  the filtering is requested. 
     * @throws IllegalArgumentException If {@code speciesId} is not found in the database.
     */
    public void filterNoExpressionCalls(String speciesId) throws IllegalArgumentException {
        log.entry(speciesId);
        
        //check validity of speciesId
        BgeeDBUtils.checkAndGetSpeciesIds(Arrays.asList(speciesId), this.getSpeciesDAO());
        
        this.initAttributes();
        Set<String> speciesFilter = new HashSet<String>();
        speciesFilter.add(speciesId);
        

        //------------------ Relations ---------------------
        //get the reflexive/direct/indirect is_a/part_of relations between stages 
        //and between anat entities for this species
        Map<String, Set<String>> anatEntityRels = 
                BgeeDBUtils.getAnatEntityChildrenFromParents(speciesFilter, 
                        this.getRelationDAO());
        Map<String, Set<String>> stageRels = 
                BgeeDBUtils.getStageChildrenFromParents(speciesFilter, 
                        this.getRelationDAO());

        
        //------------------ Retrieve expression calls ---------------------
        //get the global expression calls for this species (it should work the same 
        //with basic expression calls, but we're never too careful! This will increase 
        //the memory usage, but not the process time, as retrieval of conflicting 
        //expression calls are hashCode-based). 
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(speciesFilter);
        params.setIncludeSubstructures(true);
        //we do not want the IDs of the expression calls, otherwise the equals/hashCode methods 
        //will be based on these IDs, while we want the comparisons to be based on 
        //the anatEntityId, stageId and geneId. 
        Collection<ExpressionCallDAO.Attribute> attributes = 
                new HashSet<ExpressionCallDAO.Attribute>(
                        Arrays.asList(ExpressionCallDAO.Attribute.values()));
        attributes.remove(ExpressionCallDAO.Attribute.ID);
        ExpressionCallDAO dao = this.getExpressionCallDAO();
        dao.setAttributes(attributes);
        //we want to be able to retrieve an expression call using a 'get' method 
        //based on hashCode, not to iterate all expression calls each time 
        //we need to retrieve a specific one. We use a Map to do that, 
        //see http://stackoverflow.com/a/18380755/1768736
        //Also, we store the calls associated to their geneId, this will make analyzes 
        //of a no-expression call faster. So we use a Map where keys are gene IDs, 
        //and value is a Map where keys and values are the same associated expression calls. 
        Map<String, Map<ExpressionCallTO, ExpressionCallTO>> exprCallTOsByGene = 
                new HashMap<String, Map<ExpressionCallTO, ExpressionCallTO>>();
        log.debug("Retrieving expression calls for species {}...", speciesId);
        ExpressionCallTOResultSet exprRs = dao.getExpressionCalls(params);
        while (exprRs.next()) {
            ExpressionCallTO exprCallTO = exprRs.getTO();
            //sanity checks
            if (exprCallTO.getId() != null || 
                    exprCallTO.getGeneId() == null || 
                    exprCallTO.getAnatEntityId() == null || 
                    exprCallTO.getStageId() == null) {
                throw log.throwing(new AssertionError("The IDs fo the expression calls " +
                        "should not be retrieved, their associated gene ID, " +
                        "anat entity ID, and stage ID, should be retrieved. " +
                        "Offending expressionCallTO: " + exprCallTO));
            }
            if (exprCallTO.getAffymetrixData() == null || 
                    exprCallTO.getESTData() == null || 
                    exprCallTO.getInSituData() == null || 
                    exprCallTO.getRNASeqData() == null) {
                throw log.throwing(new AssertionError("All data types should be retrieved. " +
                        "Offending expressionCallTO: " + exprCallTO));
            }
            Map<ExpressionCallTO, ExpressionCallTO> geneExprCallTOs = 
                    exprCallTOsByGene.get(exprCallTO.getGeneId());
            if (geneExprCallTOs == null) {
                geneExprCallTOs = new HashMap<ExpressionCallTO, ExpressionCallTO>();
                exprCallTOsByGene.put(exprCallTO.getGeneId(), geneExprCallTOs);
            }
            geneExprCallTOs.put(exprCallTO, exprCallTO);
        }
        //no need for a finally close, this part of the code is already 
        //in a try-finally block that will close everything in all cases.
        exprRs.close();
        log.debug("Done retrieving expression calls for species {}, calls retrieved for {} genes.", 
                speciesId, exprCallTOsByGene.size());


        //------------------ Analyze no-expression calls ---------------------
        //now, iterate the no-expression calls, and identify conflicts
        NoExpressionCallParams noExprParams = new NoExpressionCallParams();
        noExprParams.addAllSpeciesIds(speciesFilter);
        noExprParams.setIncludeParentStructures(false);
        log.debug("Analyzing no-expression calls...");
        NoExpressionCallTOResultSet noExprRs = 
                this.getNoExpressionCallDAO().getNoExpressionCalls(noExprParams);
        int i = 0;
        while (noExprRs.next()) {
            i++;
            if (log.isDebugEnabled() && i % 100000 == 0) {
                log.debug("{} no-expression calls examined.", i);
            }
            NoExpressionCallTO noExprCallTO = noExprRs.getTO();
            //sanity check
            if (noExprCallTO.getId() == null || 
                    noExprCallTO.getGeneId() == null || 
                    noExprCallTO.getAnatEntityId() == null || 
                    noExprCallTO.getStageId() == null || 
                    noExprCallTO.getAffymetrixData() == null || 
                    noExprCallTO.getInSituData() == null || 
                    noExprCallTO.getRelaxedInSituData() == null || 
                    noExprCallTO.getRNASeqData() == null) {
                throw log.throwing(new AssertionError("All data of no-expression calls " +
                        "must be retrieved. Offending noExpressionCallTO: " + 
                        noExprCallTO));
            }
            if (noExprCallTO.isIncludeParentStructures() || 
                noExprCallTO.getOriginOfLine() != NoExpressionCallTO.OriginOfLine.SELF) {
                throw log.throwing(new AssertionError("No-expression cleaning " +
                        "can be performed only on basic no-expression calls. " +
                        "Offending noExpressionCallTO: " + noExprCallTO));
            }
            this.analyzeNoExprCallTO(noExprCallTO, 
                    exprCallTOsByGene.get(noExprCallTO.getGeneId()), 
                    anatEntityRels, stageRels);
        }
        //no need for a finally close, this part of the code is already 
        //in a try-finally block that will close everything in all cases. 
        noExprRs.close();
        log.debug("Done analyzing no-expression calls.");


        //------------------ Update no-expression calls ---------------------
        this.updateDataSource();

        //free memory
        this.initAttributes();
        log.exit();
    }
    
    /**
     * Analyze a no-expression call to determine if there exist some conflicting expression data. 
     * Expression data are conflicting if there exist some expression call 
     * in the same anatomical entity and developmental stage as the no-expression call 
     * {@code noExprCallTO}, or in any of the child anatomical entities and/or child stages. 
     * This method will fill the attributes {@link #affyNoExprIds}, {@link #inSituNoExprIds}, 
     * {@link #rnaSeqNoExprIds}, {@link toDeleteNoExprIds} and {@link #noExprCallsToUpdate}.
     * 
     * @param noExprCallTO      The {@code NoExpressionCallTO} to be checked for conflicting 
     *                          expression data.
     * @param exprCallTOs       A {@code Map} of {@code ExpressionCallTO}s that could lead
     *                          to a conflict with {@code noExprCallTO}. In this {@code Map}, 
     *                          keys and values are equal; a {@code Map} is used solely 
     *                          to efficiently retrieve {@code ExpressionCallTO}s based on 
     *                          their hashCode, see <a 
     *                          href='http://stackoverflow.com/a/18380755/1768736' target='_blank'>
     *                          http://stackoverflow.com/a/18380755/1768736</a>. 
     * @param anatEntityRels    A {@code Map} where keys are IDs of anatomical entities 
     *                          that are targets of a relation, the associated value 
     *                          being a {@code Set} of {@code String}s that are 
     *                          the IDs of their associated sources. This {@code Map} allows 
     *                          to efficiently retrieve all children of an anatomical entity 
     *                          by any is_a/part_of relation, even indirect or reflexive. 
     * @param stageRels         A {@code Map} where keys are IDs of developmental stages  
     *                          that are targets of a relation, the associated value 
     *                          being a {@code Set} of {@code String}s that are 
     *                          the IDs of their associated sources. This {@code Map} allows 
     *                          to efficiently retrieve all children of a stage 
     *                          by any is_a/part_of relation, even indirect or reflexive. 
     */
    private void analyzeNoExprCallTO(NoExpressionCallTO noExprCallTO, 
            Map<ExpressionCallTO, ExpressionCallTO> exprCallTOs, 
            Map<String, Set<String>> anatEntityRels, Map<String, Set<String>> stageRels) {
        log.entry(noExprCallTO, exprCallTOs, anatEntityRels, stageRels);
        log.trace("no-expresion call examined: {}", noExprCallTO);
        
        //------------------ sanity checks --------------------------
        //check validity of the no-expression calls
        if (noExprCallTO.isIncludeParentStructures() || 
                noExprCallTO.getOriginOfLine() != NoExpressionCallTO.OriginOfLine.SELF) {
            throw log.throwing(new IllegalArgumentException("No-expression cleaning " +
            		"can be performed only on basic no-expression calls. " +
            		"Offending no-expression call: " + noExprCallTO));
        }
        
        //try to find expression calls in the same organ/stage as the no-expression call, 
        //or in a child organ and/or in a child stage.
        Set<String> childAnatEntityIds = anatEntityRels.get(noExprCallTO.getAnatEntityId());
        if (childAnatEntityIds == null) {
            log.trace(anatEntityRels);
            throw log.throwing(new IllegalStateException("The anatomical entity " +
                    noExprCallTO.getAnatEntityId() + " is not defined as existing " +
                            "in the species of gene " + noExprCallTO.getGeneId() + 
                            ", while it has no-expression data in it."));
        }
        Set<String> childStageIds = stageRels.get(noExprCallTO.getStageId());
        if (childStageIds == null) {
            throw log.throwing(new IllegalStateException("The stage " +
                    noExprCallTO.getStageId() + " is not defined as existing " +
                            "in the species of gene " + noExprCallTO.getGeneId() + 
                            ", while it has no-expression data in it."));
        }
        

        //------------------ Find conflicts --------------------------
        boolean affyDataConflict          = false;
        boolean inSituDataConflict        = false;
        boolean relaxedInSituDataConflict = false;
        boolean rnaSeqDataConflict        = false;

        //anatEntityRels and stageRels contain reflexive relations, so, same organ/stage 
        //will be also tested when iterating the relations
        checkConflict: for (String childAnatEntityId: childAnatEntityIds) {
            for (String childStageId: childStageIds) {
                log.trace("Trying to retrieve expression calls in anat entity {} and stage {}", 
                        childAnatEntityId, childStageId);
                //creating a fake expression call, to retrieve the real call 
                //(quicker than iterating the whole list of ExpressionCallTO, 
                //because based on hashCode)
                ExpressionCallTO fakeExprCallTO = new ExpressionCallTO(null, 
                        noExprCallTO.getGeneId(), childAnatEntityId, childStageId, 
                        null, null, null, null, false, false, null);
                //hashCode and equals methods are based on the geneId-anatEntityId-stageId, 
                //because we requested the expression call TOs to not contain their ID.
                ExpressionCallTO realExprCallTO = exprCallTOs.get(fakeExprCallTO);
                //conflict found
                if (realExprCallTO != null) {
                    log.trace("Conflicting expression call retrieved: {}", realExprCallTO);
                    //check for each data type whether there is a conflict
                    if (noExprCallTO.getAffymetrixData() != DataState.NODATA && 
                            realExprCallTO.getAffymetrixData() != DataState.NODATA) {
                        affyDataConflict = true;
                        log.trace("Affymetrix data conflicting.");
                    }
                    if (noExprCallTO.getInSituData() != DataState.NODATA && 
                            realExprCallTO.getInSituData() != DataState.NODATA) {
                        inSituDataConflict = true;
                        log.trace("In-situ data conflicting.");
                    }
                    if (noExprCallTO.getRelaxedInSituData() != DataState.NODATA && 
                            realExprCallTO.getInSituData() != DataState.NODATA) {
                        relaxedInSituDataConflict = true;
                        log.trace("Relaxed in-situ data conflicting.");
                    }
                    if (noExprCallTO.getRNASeqData() != DataState.NODATA && 
                            realExprCallTO.getRNASeqData() != DataState.NODATA) {
                        rnaSeqDataConflict = true;
                        log.trace("RNA-Seq data conflicting.");
                    }
                }
                log.trace("Overall conflicts: affymetrix {} - in situ {} - relaxed in situ {} - RNA-Seq {}", 
                        affyDataConflict, inSituDataConflict, relaxedInSituDataConflict, 
                        rnaSeqDataConflict);
                
                //if there is no remaining accepted data for the no-expression call, 
                //no need to continue the iterations
                if ((noExprCallTO.getAffymetrixData() == DataState.NODATA || affyDataConflict) && 
                        (noExprCallTO.getInSituData() == DataState.NODATA || inSituDataConflict) && 
                        (noExprCallTO.getRelaxedInSituData() == DataState.NODATA || 
                            relaxedInSituDataConflict) && 
                        (noExprCallTO.getRNASeqData() == DataState.NODATA || rnaSeqDataConflict)) {
                    log.trace("No remaining accepted data, stop iterations");
                    break checkConflict;
                }
            }
        }
        

        //------------------ store information to update/delete --------------------------
        //store information necessary for updating the data source
        String noExprId = noExprCallTO.getId();
        NoExpressionCallTO noExprCallTOForUpdate = new NoExpressionCallTO(noExprId, 
                noExprCallTO.getGeneId(), noExprCallTO.getAnatEntityId(), noExprCallTO.getStageId(), 
                (affyDataConflict? DataState.NODATA: noExprCallTO.getAffymetrixData()), 
                (inSituDataConflict? DataState.NODATA: noExprCallTO.getInSituData()), 
                (relaxedInSituDataConflict? DataState.NODATA: noExprCallTO.getRelaxedInSituData()), 
                (rnaSeqDataConflict? DataState.NODATA: noExprCallTO.getRNASeqData()), 
                //here, isIncludeParentStructures should always be false, and 
                //originOfLine always equal to SELF
                noExprCallTO.isIncludeParentStructures(), noExprCallTO.getOriginOfLine());
        
        //if there is no remaining data at all for the no-expression call, delete it
        if (noExprCallTOForUpdate.getAffymetrixData() == DataState.NODATA && 
                noExprCallTOForUpdate.getInSituData() == DataState.NODATA && 
                noExprCallTOForUpdate.getRelaxedInSituData() == DataState.NODATA && 
                noExprCallTOForUpdate.getRNASeqData() == DataState.NODATA) {
            this.toDeleteNoExprIds.add(noExprId);
            log.trace("No-expression call will be deleted.");
        } else if (affyDataConflict || inSituDataConflict || 
                relaxedInSituDataConflict || rnaSeqDataConflict) {
            //update the call only if there was at least one conflict
            this.noExprCallsToUpdate.add(noExprCallTOForUpdate);
            log.trace("No-expression call will be updated: {}", noExprCallTOForUpdate);
        } else {
            log.trace("No conflicts, thus no modifications needed for this call.");
        }
        
        //to update raw data
        if (affyDataConflict) {
            this.affyNoExprIds.add(noExprId);
            log.trace("Associated raw Affymetrix data will be removed.");
        }
        //for relaxedInSituData, there is no raw data associated
        if (inSituDataConflict) {
            this.inSituNoExprIds.add(noExprId);
            log.trace("Associated raw in situ data will be removed.");
        }
        if (rnaSeqDataConflict) {
            this.rnaSeqNoExprIds.add(noExprId);
            log.trace("Associated raw RNA-Seq data will be removed.");
        }
        
        log.exit();
    }
    
    /**
     * Update the data source. 
     * <ul>
     * <li>Raw data with conflicting no-expression data are updated. 
     * This includes: Affymetrix probesets associated to a no-expression ID stored in 
     * {@link #affyNoExprIds}; <em>in-situ</em> spots associated to a no-expression ID 
     * stored in {@link #inSituNoExprIds}; RNA-Seq results associated to a no-expression ID 
     * stored in {@link #rnaSeqNoExprIds}. 
     * <li>No-expression calls with no more data after removal of conflicts are deleted 
     * from the data source. Their IDs are stored in {@link toDeleteNoExprIds}.
     * <li>No-expression calls with some conflicting data, but with some remaining valid data, 
     * are updated. They are stored in {@link #noExprCallsToUpdate}.
     * </ul>
     */
    private void updateDataSource() {
        log.entry();
        log.debug("Updating data source...");
        
        if (!this.affyNoExprIds.isEmpty()) {
            int updateCount = this.getAffymetrixProbesetDAO().updateNoExpressionConflicts(
                    this.affyNoExprIds);
            if (updateCount != this.affyNoExprIds.size()) {
                throw log.throwing(new AssertionError("Incorrect number of probesets updated, " +
                		"expected: " + this.affyNoExprIds.size() + " but was: " + updateCount));
            }
        }
        if (!this.inSituNoExprIds.isEmpty()) {
            int updateCount = this.getInSituSpotDAO().updateNoExpressionConflicts(
                    this.inSituNoExprIds);
            if (updateCount != this.inSituNoExprIds.size()) {
                throw log.throwing(new AssertionError("Incorrect number of in-situ spots updated, " +
                        "expected: " + this.inSituNoExprIds.size() + " but was: " + updateCount));
            }
        }
        if (!this.rnaSeqNoExprIds.isEmpty()) {
            int updateCount = this.getRNASeqResultDAO().updateNoExpressionConflicts(
                    this.rnaSeqNoExprIds);
            if (updateCount != this.rnaSeqNoExprIds.size()) {
                throw log.throwing(new AssertionError("Incorrect number of RNA-Seq results updated, " +
                        "expected: " + this.rnaSeqNoExprIds.size() + " but was: " + updateCount));
            }
        }
        if (!this.toDeleteNoExprIds.isEmpty()) {
            int deleteCount = this.getNoExpressionCallDAO().deleteNoExprCalls(
                    this.toDeleteNoExprIds, false);
            if (deleteCount != this.toDeleteNoExprIds.size()) {
                throw log.throwing(new AssertionError("Incorrect number of no-expression calls " +
                        "deleted, expected: " + this.toDeleteNoExprIds.size() + 
                        " but was: " + deleteCount));
            }
        }
        if (!this.noExprCallsToUpdate.isEmpty()) {
            int updateCount = this.getNoExpressionCallDAO().updateNoExprCalls(
                    this.noExprCallsToUpdate);
            if (updateCount != this.noExprCallsToUpdate.size()) {
                throw log.throwing(new AssertionError("Incorrect number of no-expression calls " +
                        "updated, expected: " + this.noExprCallsToUpdate.size() + 
                        " but was: " + updateCount));
            }
        }
        
        log.debug("Done updating data source.");
        log.exit();
    }
}
