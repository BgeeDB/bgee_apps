package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link ExpressionCallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13
 */
public interface ExpressionCallDAO extends CallDAO<ExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ExpressionCallTO}s 
     * obtained from this {@code ExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ExpressionCallTO#getId()}.
     * <li>{@code GENE_ID}: corresponds to {@link ExpressionCallTO#getGeneId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link ExpressionCallTO#getStageId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link ExpressionCallTO#getAnatEntityId()}.
     * <li>{@code AFFYMETRIX_DATA}: corresponds to {@link ExpressionCallTO#getAffymetrixData()}.
     * <li>{@code AFFYMETRIX_RANK}: corresponds to {@link ExpressionCallTO#getAffymetrixRank()}.
     * <li>{@code EST_DATA}: corresponds to {@link ExpressionCallTO#getESTData()}.
     * <li>{@code EST_RANK}: corresponds to {@link ExpressionCallTO#getESTRank()}.
     * <li>{@code IN_SITU_DATA}: corresponds to {@link ExpressionCallTO#getInSituData()}.
     * <li>{@code IN_SITU_RANK}: corresponds to {@link ExpressionCallTO#getInSituRank()}.
     * <li>{@code RNA_SEQ_DATA}: corresponds to {@link ExpressionCallTO#getRNASeqData()}.
     * <li>{@code RNA_SEQ_RANK}: corresponds to {@link ExpressionCallTO#getRNASeqRank()}.
     * <li>{@code GLOBAL_RANK}: corresponds to {@link ExpressionCallTO#getGlobalRank()}.
     * <li>{@code INCLUDE_SUBSTRUCTURES}: corresponds to 
     * {@link ExpressionCallTO#isIncludeSubstructures()}.
     * <li>{@code INCLUDE_SUBSTAGES}: corresponds to {@link ExpressionCallTO#isIncludeSubStages()}.
     * <li>{@code ANAT_ORIGIN_OF_LINE}: corresponds to {@link ExpressionCallTO#getOriginOfLine()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements CallDAO.Attribute {
        ID(false, false, false, "id"), GENE_ID(false, false, false, "geneId"), 
        STAGE_ID(false, false, false, "stageId"), ANAT_ENTITY_ID(false, false, false, "anatEntityId"), 
        GLOBAL_MEAN_RANK(false, true, false, "globalMeanRank"), 
        AFFYMETRIX_DATA(true, false, false, "affymetrixData"), 
        AFFYMETRIX_MEAN_RANK(false, true, false, "affymetrixMeanRank"), 
        EST_DATA(true, false, false, "estData"), EST_MEAN_RANK(false, true, false, "estMeanRank"), 
        IN_SITU_DATA(true, false, false, "inSituData"), IN_SITU_MEAN_RANK(false, true, false, "inSituMeanRank"), 
        RNA_SEQ_DATA(true, false, false, "rNASeqData"), RNA_SEQ_MEAN_RANK(false, true, false, "rNASeqMeanRank"), 
        INCLUDE_SUBSTRUCTURES(false, false, true, "includeSubstructures"), 
        INCLUDE_SUBSTAGES(false, false, true, "includeSubStages"), 
        ANAT_ORIGIN_OF_LINE(false, false, true, "anatOriginOfLine"), 
        STAGE_ORIGIN_OF_LINE(false, false, true, "stageOriginOfLine"), 
        OBSERVED_DATA(false, false, true, "observedData");
        
        /**
         * @see #isDataTypeAttribute()
         */
        private final boolean dataTypeAttribute;
        /**
         * @see #isRankAttribute()
         */
        private final boolean rankAttribute;
        /**
         * @see #isPropagationAttribute()
         */
        private final boolean propagationAttribute;
        /**
         * A {@code String} that is the corresponding field name in {@code ExpressionCallTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        
        private Attribute(boolean dataTypeAttribute, boolean rankAttribute, boolean propagationAttribute, 
                String fieldName) {
            this.dataTypeAttribute = dataTypeAttribute;
            this.rankAttribute = rankAttribute;
            this.propagationAttribute = propagationAttribute;
            this.fieldName = fieldName;
        }
        
        @Override
        public boolean isDataTypeAttribute() {
            return dataTypeAttribute;
        }
        /**
         * @return  A {@code boolean} defining whether this {@code Attribute} is related to 
         *          gene rank information.
         */
        public boolean isRankAttribute() {
            return rankAttribute;
        }
        /**
         * @return  A {@code boolean} defining whether this {@code Attribute} is related to 
         *          data propagation information.
         */
        public boolean isPropagationAttribute() {
            return propagationAttribute;
        }
        
        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }
    /**
     * The attributes available to order retrieved {@code ExpressionCallTO}s
     * <ul>
     * <li>{@code GENE_ID}: corresponds to {@link CallTO#getGeneId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link CallTO#getStageId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link CallTO#getAnatEntityId()}.
     * <li>{@code OMA_GROUP_ID}: order results by the OMA group genes belong to. 
     * If this {@code OrderingAttribute} is used in a query not specifying any targeted taxon 
     * for gene orthology, then the {@code OMAParentNodeId} of the gene is used (see 
     * {@link org.bgee.model.dao.api.gene.GeneDAO.GeneTO.getOMAParentNodeId()}); otherwise, 
     * the OMA group the gene belongs to at the level of the targeted taxon is used. 
     * <li>{@code MEAN_RANK}: Corresponds to {@link ExpressionCallTO#getGlobalMeanRank()}. 
     * Order results by mean rank of the gene in the corresponding condition. 
     * Only the mean ranks computed from the data types requested in the query are considered. 
     * </ul>
     */
    enum OrderingAttribute implements DAO.OrderingAttribute {
        GENE_ID, STAGE_ID, ANAT_ENTITY_ID, OMA_GROUP_ID, MEAN_RANK;
    }
    
    /**
     * Retrieve expression calls from the data source. The parameters are provided through 
     * {@code CallDAOFilter}s and {@code ExpressionCallTO}s. Note that while it is possible 
     * to filter genes to consider notably by providing ID lists in the {@code CallDAOFilter}s, 
     * it is also possible to provide a global list ({@code globalGeneIds}), overriding 
     * any gene IDs provided in the {@code CallDAOFilter}s, to avoid the need of repeating 
     * a same gene ID filtering in several {@code CallDAOFilter}s, which could be costly, 
     * as many gene IDs could be provided.
     * <p>
     * In the provided {@code ExpressionCallTO}s, only the following methods are considered: 
     * <ul>
     * <li>{@code getAffymetrixData}, {@code getESTData}, {@code getInSituData}, {@code getRNASeqData}, 
     * to define the minimum quality level for each data type. If equal to {@code null} 
     * or {@code DataState.NODATA}, then no filtering is performed based on this data type. 
     * All parameters in a same {@code ExpressionCallO} are considered as AND conditions. 
     * <li>{@code getAnatOriginOfLine}, {@code getStageOriginOfLine}, to define whether 
     * the returned calls should be filtered based on the origin of the propagated data.
     * Must be {@code null} to accept all origins. 
     * <li>{@code isObservedData}, to define a criteria of having at least one observation 
     * of the returned calls (no propagated calls only). Must be {@code null} to accept 
     * all observed data states.
     * </ul>
     * <p>
     * It is possible to request only for genes that are orthologous at the level of a targeted taxon, 
     * by setting {@code taxonId}. In that case, only genes that are orthologous in this taxon 
     * will be considered. If the species member of the provided taxon should be 
     * further filtered, a list of species IDs should be defined in the {@code CallDAOFilter}s. 
     * Otherwise, all species existing in the targeted taxon will be considered. 
     * If {@code taxonId} is blank, then no gene orthology is considered. 
     * <p>
     * The expression calls are retrieved and returned as an {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param callFilters           A {@code Collection} of {@code CallDAOFilter}s, 
     *                              allowing to configure this query. If several 
     *                              {@code CallDAOFilter}s are provided, they are seen 
     *                              as "OR" conditions. Can be {@code null} or empty.
     * @param callTOFilters         A {@code Collection} of {@code ExpressionCallTO}s allowing 
     *                              to configure the minimum quality level for each data type, etc. 
     *                              If several {@code ExpressionCallTO}s are provided, 
     *                              they are seen as "OR" conditions. Attributes inside 
     *                              a same {@code ExpressionCallTO} are seend as AND conditions. 
     *                              Can be {@code null} or empty.
     * @param includeSubstructures  A {@code boolean} defining whether the expression calls 
     *                              retrieved should be based on calls generated using data 
     *                              from anatomical entities, and all of their descendants 
     *                              by is_a and part_of relations (expression taking into account 
     *                              substructures).
     * @param includeSubStages      A {@code boolean} defining whether the expression calls 
     *                              retrieved should be based on calls generated using data 
     *                              from developmental stages, and all of their descendants 
     *                              by is_a and part_of relations (expression taking into account 
     *                              sub-stages).
     * @param globalGeneIds         A {@code Set} of {@code String}s that are IDs of genes 
     *                              to globally filter this query, overriding any gene IDs 
     *                              provided in the {@code CallDAOFilter}s. 
     *                              Can be {@code null} or empty.
     * @param taxonId               A {@code String} that is the ID of a targeted taxon, 
     *                              that should be non-null if only orthologous genes are requested. 
     *                              Can be {@code null}.
     * @param attributes            A {@code Collection} of {@code ExpressionCallDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code ExpressionCallTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code ExpressionCallDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code ExpressionCallTO}s, 
     *                              the associated value being a {@code DAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      An {@code ExpressionCallTOResultSet} allowing to obtain 
     *                              the requested {@code ExpressionCallTO}s.
     * @throws DAOException             If an error occurred while accessing the data source. 
     * @throws IllegalArgumentException If the {@code CallDAOFilter}s provided define multiple 
     *                                  expression propagation states requested.
     */
    public ExpressionCallTOResultSet getExpressionCalls(
            Collection<CallDAOFilter> callFilters, Collection<ExpressionCallTO> callTOFilters, 
            boolean includeSubstructures, boolean includeSubStages, 
            Collection<String> globalGeneIds, String taxonId, Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes) 
                    throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve expression calls from data source according {@code ExpressionCallParams}.
     * <p>
     * The expression calls are retrieved and returned as an {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param params        An {@code ExpressionCallParams} that provide the parameters specific 
     *                      to expression calls
     * @return              An {@code ExpressionCallTOResultSet} containing all expression calls 
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    //deprecated because a new CallFilter design is being implemented
    @Deprecated
    public ExpressionCallTOResultSet getExpressionCalls(ExpressionCallParams params) 
            throws DAOException;
    
    /**
     * Retrieve the maximum of expression call IDs from data source according a {@code boolean} 
     * defining whether whether descendants of the anatomical entity were considered.
     * 
     * @param isIncludeSubstructures    A {@code boolean} defining whether descendants 
     *                                  of the anatomical entity were considered.
     * @return                          An {@code int} that is maximum of expression call IDs
     *                                  from data source. If there is no call, return 0.
     * @throws DAOException             If an error occurred when accessing the data source. 
     */
    public int getMaxExpressionCallId(boolean isIncludeSubstructures) 
            throws DAOException;

    /**
     * Inserts the provided expression calls into the Bgee database, 
     * represented as a {@code Collection} of {@code ExpressionCallTO}s. 
     * 
     * @param expressionCalls   A {@code Collection} of {@code ExpressionCallTO}s 
     *                          to be inserted into the database.
     * @return                  An {@code int} that is the number of inserted expression calls.
     * @throws IllegalArgumentException If {@code expressionCalls} is empty or null. 
     * @throws DAOException     If an error occurred while trying to insert expression calls. 
     */
    public int insertExpressionCalls(Collection<ExpressionCallTO> expressionCalls) 
            throws DAOException, IllegalArgumentException;

    /**
     * Inserts the provided correspondence between expression and global expression calls into the 
     * Bgee database, represented as a {@code Collection} of {@code GlobalExpressionToExpressionTO}s. 
     * 
     * @param globalExpressionToExpression  A {@code Collection} of 
     *                                      {@code GlobalExpressionToExpressionTO}s to be inserted 
     *                                      into the database.
     * @return                              An {@code int} that is the number of inserted 
     *                                      TOs.
     * @throws IllegalArgumentException If {@code globalExpressionToExpression} is empty or null. 
     * @throws DAOException                 If an error occurred while trying to insert data.
     */
    public int insertGlobalExpressionToExpression(Collection<GlobalExpressionToExpressionTO> 
            globalExpressionToExpression) throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code ExpressionCallTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface ExpressionCallTOResultSet extends DAOResultSet<ExpressionCallTO> {
        
    }

    /**
     * A {@code CallTO} specific to expression calls. Their specificity is that 
     * they can be produced using data propagation from child anatomical entities 
     * by <em>is_a</em> or <em>part_of</em> relations, and/or from child developmental 
     * stages by <em>is_a</em> or <em>part_of</em> relations. See {@link 
     * #isIncludeSubstructures()} and {@link #isIncludeSubStages()} for more details.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class ExpressionCallTO extends CallTO<Attribute> {
        // TODO modify the class to be immutable.

        private static final long serialVersionUID = 1198652013999835872L;
        
        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(ExpressionCallTO.class.getName());

        /**
         * An {@code Enum} used to define the origin of an expression call, applicable to  
         * both the propagation among anatomical entities, and the propagation 
         * among developmental stages.
         * <ul>
         * <li>{@code SELF}: the expression call was generated from data coming from 
         * its related anatomical entity itself; same definition for developmental stages.
         * <li>{@code DESCENT}: the expression call was generated by data coming from 
         * one of the descendants of its related anatomical entity, through  
         * <em>is_a</em> or <em>part_of</em> relations, even indirect (same logic 
         * for developmental stages).
         * <li>{@code BOTH}: the expression call was generated by data coming from both 
         * its related anatomical entity, and from one of its descendants 
         * by <em>is_a</em> or <em>part_of</em> relations, even indirect (same logic 
         * for developmental stages).
         * </ul>
         */
        public enum OriginOfLine implements EnumDAOField {
            SELF("self"), DESCENT("descent"), BOTH("both");
            
            /**
             * Convert the {@code String} representation of a data state (for instance, 
             * retrieved from a database) into a {@code OriginOfLine}. Operation performed 
             * by calling {@link TransferObject#convert(Class, String)} with {@code OriginOfLine} 
             * as the {@code Class} argument, and {@code representation} 
             * as the {@code String} argument.
             * .
             * 
             * @param representation    A {@code String} representing a data state.
             * @return  A {@code OriginOfLine} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code OriginOfLine}.
             */
            public static final OriginOfLine convertToOriginOfLine(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(OriginOfLine.class, representation));
            }
            
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code OriginOfLine}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code OriginOfLine}.
             */
            private OriginOfLine(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * See {@link #isIncludeSubstructures()}.
         */
        private Boolean includeSubstructures;
        /**
         * An {@code OriginOfLine} used to define how this call was propagated 
         * among anatomical entities. This is different from {@link #includeSubstructures}: 
         * if {@code includeSubstructures} is {@code true}, it means that the expression call 
         * was retrieved by taking into account an anatomical entity and all its descendants. 
         * By using {@code anatOriginOfLine}, it is possible to know exactly how the call 
         * was propagated.
         * 
         * @see OriginOfLine
         */
        private OriginOfLine anatOriginOfLine;
        
        /**
         * See {@link #isIncludeSubStages()}.
         */
        private Boolean includeSubStages; 
        /**
         * An {@code OriginOfLine} used to define how this call was propagated 
         * among developmental stages. This is different from {@link #includeSubStages}: 
         * if {@code includeSubStages} is {@code true}, it means that the expression call 
         * was retrieved by taking into account a stage and all its descendants. 
         * By using {@code stageOriginOfLine}, it is possible to know exactly how the call 
         * was propagated.
         * 
         * @see OriginOfLine
         */
        private OriginOfLine stageOriginOfLine;
        
        /**
         * 
         */
        private Boolean observedData;
        
        /**
         * @see #getAffymetrixMeanRank()
         */
        private final BigDecimal affymetrixMeanRank;
        /**
         * @see #getESTMeanRank()
         */
        private final BigDecimal estMeanRank;
        /**
         * @see #getInSituMeanRank()
         */
        private final BigDecimal inSituMeanRank;
        /**
         * @see #getRNASeqMeanRank()
         */
        private final BigDecimal rnaSeqMeanRank;
        /**
         * @see #getGlobalMeanRank()
         */
        private final BigDecimal globalMeanRank;
        
        /**
         * Default constructor.
         */
        ExpressionCallTO() {
            this(null, null, null, null);
        }

        /**
         * Constructor providing only the data type parameters that are used as part of a 
         * {@link org.bgee.model.dao.api.expressiondata.CallDAOFilter}.
         * 
         * @param affymetrixData        A {@code DataSate} that is the contribution of Affymetrix  
         *                              data to the generation of this call.
         * @param estData               A {@code DataSate} that is the contribution of EST data
         *                              to the generation of this call.
         * @param inSituData            A {@code DataSate} that is the contribution of 
         *                              <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData            A {@code DataSate} that is the contribution of RNA-Seq data
         *                              to the generation of this call.
         */
        public ExpressionCallTO(DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState rnaSeqData) {
            this(affymetrixData, estData, inSituData, rnaSeqData, null, null, null);
        }

        /**
         * Constructor providing all the parameters that are used as part of a 
         * {@link org.bgee.model.dao.api.expressiondata.CallDAOFilter}.
         * 
         * @param affymetrixData        A {@code DataSate} that is the contribution of Affymetrix  
         *                              data to the generation of this call.
         * @param estData               A {@code DataSate} that is the contribution of EST data
         *                              to the generation of this call.
         * @param inSituData            A {@code DataSate} that is the contribution of 
         *                              <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData            A {@code DataSate} that is the contribution of RNA-Seq data
         *                              to the generation of this call.
         * @param anatOrigin            An {@code OriginOfLine} defining how this call 
         *                              was produced among anatomical entities: from 
         *                              the related anatomical itself, 
         *                              or from one of its descendants, or from both.
         * @param stageOrigin           An {@code OriginOfLine} defining how this call 
         *                              was produced among developmental stages: from 
         *                              the related stage itself, 
         *                              or from one of its descendants, or from both.
         * @param observedData          A {@code Boolean} defining whether this expression call 
         *                              was actually observed from experiment, or whether it is 
         *                              generated from some kind of propagation.
         */
        public ExpressionCallTO(DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState rnaSeqData, OriginOfLine anatOrigin, OriginOfLine stageOrigin, Boolean observedData) {
            this(null, null, null, null, null, affymetrixData, null, estData, null, 
                    inSituData, null, rnaSeqData, null, 
                 null, null, anatOrigin, stageOrigin, observedData);
        }

        /**
         * Constructor providing all parameters except information related to ranks.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                    A {@code String} that is the ID of this call.
         * @param geneId                A {@code String} that is the ID of the gene 
         *                              associated to this call.
         * @param anatEntityId          A {@code String} that is the ID of the anatomical entity
         *                              associated to this call. 
         * @param stageId               A {@code String} that is the ID of the developmental stage 
         *                              associated to this call. 
         * @param affymetrixData        A {@code DataSate} that is the contribution of Affymetrix  
         *                              data to the generation of this call.
         * @param estData               A {@code DataSate} that is the contribution of EST data
         *                              to the generation of this call.
         * @param inSituData            A {@code DataSate} that is the contribution of 
         *                              <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData            A {@code DataSate} that is the contribution of RNA-Seq data
         *                              to the generation of this call.
         * @param includeSubstructures  A {@code Boolean} defining whether this expression call was 
         *                              generated using data from the anatomical entity with the ID 
         *                              alone, or by also considering all its descendants by 
         *                              <em>is_a</em> or <em>part_of</em> relations, even indirect.
         * @param includeSubStages      A {@code Boolean} defining whether this expression call was 
         *                              generated using data from the developmental stage with the ID
         *                              alone, or by also considering all its descendants.
         * @param anatOrigin            An {@code OriginOfLine} defining how this call 
         *                              was produced among anatomical entities: from 
         *                              the related anatomical itself, 
         *                              or from one of its descendants, or from both.
         * @param stageOrigin           An {@code OriginOfLine} defining how this call 
         *                              was produced among developmental stages: from 
         *                              the related stage itself, 
         *                              or from one of its descendants, or from both.
         * @param observedData          A {@code Boolean} defining whether this expression call 
         *                              was actually observed from experiment, or whether it is 
         *                              generated from some kind of propagation.
         */
        public ExpressionCallTO(String id, String geneId, String anatEntityId, String stageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState rnaSeqData, Boolean includeSubstructures, Boolean includeSubStages, 
                OriginOfLine anatOrigin, OriginOfLine stageOrigin, Boolean observedData) {
            this(id, geneId, anatEntityId, stageId, null, affymetrixData, null, 
                    estData, null, inSituData, null, rnaSeqData, null, 
                    includeSubstructures, includeSubStages, 
                    anatOrigin, stageOrigin, observedData);
        }

        /**
         * Constructor providing all parameters
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                    A {@code String} that is the ID of this call.
         * @param geneId                A {@code String} that is the ID of the gene 
         *                              associated to this call.
         * @param anatEntityId          A {@code String} that is the ID of the anatomical entity
         *                              associated to this call. 
         * @param stageId               A {@code String} that is the ID of the developmental stage 
         *                              associated to this call. 
         * @param globalMeanRank        A {@code BigDecimal} that is the mean rank of the gene 
         *                              in the condition, based on the mean rank of each data type 
         *                              requested in the query. So for instance, if you configured 
         *                              a {@code ExpressionCallDAOFilter} to only retrieve 
         *                              Affymetrix data, then this rank will be equal to the rank 
         *                              returned by {@link #getAffymetrixMeanRank()}. 
         *                              Note that this information is available only if 
         *                              {@link isIncludeSubstructures()} returns {@code false} 
         *                              or {@code null}.
         * @param affymetrixData        A {@code DataSate} that is the contribution of Affymetrix  
         *                              data to the generation of this call.
         * @param affymetrixMeanRank    A {@code BigDecimal} that is the mean rank of the gene 
         *                              in the condition, based on Affymetrix expression levels. 
         *                              Note that this information is available only if 
         *                              {@link isIncludeSubstructures()} returns {@code false} 
         *                              or {@code null}.
         * @param estData               A {@code DataSate} that is the contribution of EST data
         *                              to the generation of this call.
         * @param estMeanRank           A {@code BigDecimal} that is the mean rank of the gene 
         *                              in the condition, based on EST expression levels. 
         *                              Note that this information is available only if 
         *                              {@link isIncludeSubstructures()} returns {@code false} 
         *                              or {@code null}.
         * @param inSituData            A {@code DataSate} that is the contribution of 
         *                              <em>in situ</em> data to the generation of this call.
         * @param inSituMeanRank        A {@code BigDecimal} that is the mean rank of the gene 
         *                              in the condition, based on in situ expression data. 
         *                              Note that this information is available only if 
         *                              {@link isIncludeSubstructures()} returns {@code false} 
         *                              or {@code null}.
         * @param rnaSeqData            A {@code DataSate} that is the contribution of RNA-Seq data
         *                              to the generation of this call.
         * @param rnaSeqMeanRank        A {@code BigDecimal} that is the mean rank of the gene 
         *                              in the condition, based on RNA-Seq expression levels. 
         *                              Note that this information is available only if 
         *                              {@link isIncludeSubstructures()} returns {@code false} 
         *                              or {@code null}.
         * @param includeSubstructures  A {@code Boolean} defining whether this expression call was 
         *                              generated using data from the anatomical entity with the ID 
         *                              alone, or by also considering all its descendants by 
         *                              <em>is_a</em> or <em>part_of</em> relations, even indirect.
         * @param includeSubStages      A {@code Boolean} defining whether this expression call was 
         *                              generated using data from the developmental stage with the ID
         *                              alone, or by also considering all its descendants.
         * @param anatOrigin            An {@code OriginOfLine} defining how this call 
         *                              was produced among anatomical entities: from 
         *                              the related anatomical itself, 
         *                              or from one of its descendants, or from both.
         * @param stageOrigin           An {@code OriginOfLine} defining how this call 
         *                              was produced among developmental stages: from 
         *                              the related stage itself, 
         *                              or from one of its descendants, or from both.
         * @param observedData          A {@code Boolean} defining whether this expression call 
         *                              was actually observed from experiment, or whether it is 
         *                              generated from some kind of propagation.
         */
        public ExpressionCallTO(String id, String geneId, String anatEntityId, String stageId,
                BigDecimal globalMeanRank, DataState affymetrixData, BigDecimal affymetrixMeanRank, 
                DataState estData, BigDecimal estMeanRank, 
                DataState inSituData, BigDecimal inSituMeanRank, 
                DataState rnaSeqData, BigDecimal rnaSeqMeanRank, 
                Boolean includeSubstructures, Boolean includeSubStages, 
                OriginOfLine anatOrigin, OriginOfLine stageOrigin, Boolean observedData) {
            super(id, geneId, anatEntityId, stageId, affymetrixData, estData, inSituData, 
                    null, rnaSeqData);
            this.includeSubstructures = includeSubstructures;
            this.includeSubStages = includeSubStages;
            this.anatOriginOfLine = anatOrigin;
            this.stageOriginOfLine = stageOrigin;
            this.observedData = observedData;
            
            this.affymetrixMeanRank = affymetrixMeanRank;
            this.estMeanRank = estMeanRank;
            this.inSituMeanRank = inSituMeanRank;
            this.rnaSeqMeanRank = rnaSeqMeanRank;
            this.globalMeanRank = globalMeanRank;
        }
        
        @Override
        public Map<Attribute, DataState> extractDataTypesToDataStates() {
            log.entry();
            
            Map<Attribute, DataState> typesToStates = 
                    new EnumMap<>(ExpressionCallDAO.Attribute.class);
            
            typesToStates.put(Attribute.AFFYMETRIX_DATA, this.getAffymetrixData());
            typesToStates.put(Attribute.EST_DATA, this.getESTData());
            typesToStates.put(Attribute.IN_SITU_DATA, this.getInSituData());
            typesToStates.put(Attribute.RNA_SEQ_DATA, this.getRNASeqData());
            
            return log.exit(typesToStates);
        }
        /**
         * Retrieve from this {@code CallTO} the data types with a filtering requested, 
         * allowing to parameterize queries to the data source. For instance, to only retrieve 
         * calls with an Affymetrix data state equal to {@code HIGHQUALITY}, or with some RNA-Seq data 
         * of any quality (minimal data state {@code LOWQUALITY}).
         * <p>
         * The data types are represented as {@code Attribute}s allowing to request a data type parameter 
         * (see {@link CallDAO.Attribute#isDataTypeAttribute()}). The {@code DataState}s 
         * associated to each data type are retrieved using {@link CallTO#extractDataTypesToDataStates()}. 
         * A check is then performed to ensure that the {@code CallTO} will actually result 
         * in a filtering of the data. For instance, if all data qualities are {@code null},  
         * then it is equivalent to requesting no filtering at all, and the {@code EnumMap} returned 
         * by this method will be empty. 
         * <p>
         * Each quality associated to a data type in a same {@code CallTO} is considered 
         * as an AND condition (for instance, "affymetrixData >= HIGH_QUALITY AND 
         * rnaSeqData >= HIGH_QUALITY"). To configure OR conditions, (for instance, 
         * "affymetrixData >= HIGH_QUALITY OR rnaSeqData >= HIGH_QUALITY"), several {@code CallTO}s 
         * must be provided to this {@code CallDAOFilter}. So for instance, if the quality 
         * of all data types of {@code callTO} are set to {@code LOW_QUALITY}, it will only allow 
         * to retrieve calls with data in all data types. 
         *  
         * @return          An {@code EnumMap} where keys are {@code Attribute}s associated to a data type, 
         *                  the associated value being a {@code DataState} to be used 
         *                  to parameterize queries to the data source (results should have 
         *                  a data state equal to or higher than this value for this data type).
         *                  Returned as an {@code EnumMap} for consistent iteration order 
         *                  when setting parameters in a query. 
         */
        public EnumMap<Attribute, DataState> extractFilteringDataTypes() {
            log.entry();
            return log.exit(super.extractFilteringDataTypes(Attribute.class));
        }
        
        /**
         * Sets the {@code boolean} defining whether this expression call was generated 
         * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered.
         * 
         * @param includeSubstructures  A {@code boolean} defining whether descendants 
         *                              of the anatomical entity were considered.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setIncludeSubstructures(boolean includeSubstructures) {
            this.includeSubstructures = includeSubstructures;
        }
        /**
         * Returns the {@code Boolean} defining whether this expression call was generated 
         * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered. The returned {@code Boolean} can be {@code null} 
         * if the {@link Attribute} {@code INCLUDE_SUBSTRUCTURES} was not requested.
         * <p>
         * Note that when the returned value is {@code true}, {@link #getAnatOriginOfLine()} 
         * can return any values of {@link OriginOfLine}; it can notably still return 
         * the value {@code SELF}, meaning that there were no data to propagate 
         * from substructures –but they were nevertheless examined.
         * <p>
         * At the opposite, when the returned value is {@code false}, then 
         * {@link #getAnatOriginOfLine()} can only return the value {@code SELF}.
         * 
         * @return  If {@code true}, all descendants of the anatomical entity were considered. 
         *          Can be {@code null} if this information was not requested.
         */
        public Boolean isIncludeSubstructures() {
            return includeSubstructures;
        }
        
        /**
         * @return  the {@code OriginOfLine} representing how the expression call 
         *          was propagated among anatomical entities.
         *          Can be {@code null} if this information was not requested.
         * @see #isIncludeSubstructures()
         */
        public OriginOfLine getAnatOriginOfLine() {
            return anatOriginOfLine;
        }
        /**
         * @param originOfLine  the {@code OriginOfLine} representing how the expression call 
         *                      was propagated among anatomical entities.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setAnatOriginOfLine(OriginOfLine originOfLine) {
            this.anatOriginOfLine = originOfLine;
        }

        /**
         * Returns the {@code Boolean} defining whether this expression call was generated 
         * using data from the developmental stage with the ID 
         * {@link ExpressionCallTO#getStageId()} alone, or by also considering all its
         * descendants. If {@code true}, all its descendants were considered. 
         * The returned {@code Boolean} can be {@code null} 
         * if the {@link Attribute} {@code INCLUDE_SUBSTAGES} was not requested.
         * <p>
         * Note that when the returned value is {@code true}, {@link #getStageOriginOfLine()} 
         * can return any values of {@link OriginOfLine}; it can notably still return 
         * the value {@code SELF}, meaning that there were no data to propagate 
         * from sub-stages –but they were nevertheless examined.
         * <p>
         * At the opposite, when the returned value is {@code false}, then 
         * {@link #getStageOriginOfLine()} can only return the value {@code SELF}.
         * 
         * @return  If {@code true}, all descendants of the developmental stage 
         *          were considered. Can be {@code null} if this information was not requested.
         */
        public Boolean isIncludeSubStages() {
            return includeSubStages;
        }
        /**
         * Sets the {@code boolean} defining whether this expression call was generated 
         * using data from the developmental stage with the ID {@link CallTO#getStageId()} 
         * alone, or by also considering all its descendants. If {@code true}, all its descendants 
         * were considered.
         * 
         * @param includeSubstructures  A {@code boolean} defining whether descendants 
         *                              of the developmental stage were considered.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setIncludeSubStages(boolean includeSubStages) {
            this.includeSubStages = includeSubStages;
        }
        
        /**
         * @return  the {@code OriginOfLine} representing how the expression call 
         *          was propagated among developmental stages.
         *          Can be {@code null} if this information was not requested.
         * @see #isIncludeSubStages()
         */
        public OriginOfLine getStageOriginOfLine() {
            return stageOriginOfLine;
        }
        /**
         * @param originOfLine  the {@code OriginOfLine} representing how the expression call 
         *                      was propagated among developmental stages.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setStageOriginOfLine(OriginOfLine originOfLine) {
            this.stageOriginOfLine = originOfLine;
        }

        /**
         * @return the {@code Boolean} defining whether this expression call is observed.
         */
        public Boolean isObservedData() {
            return observedData;
        }
        /**
         * Sets the {@code boolean} defining whether this expression call is observed.
         * 
         * @param observedData A {@code Boolean} defining whether this expression call is observed.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setObservedData(boolean observedData) {
            this.observedData = observedData;
        }

        /**
         * @return  A {@code BigDecimal} that is the mean rank of the gene in the condition, 
         *          based on Affymetrix expression levels. Note that this information is available 
         *          only if {@link isIncludeSubstructures()} returns {@code false} or {@code null}.
         */
        public BigDecimal getAffymetrixMeanRank() {
            return affymetrixMeanRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the mean rank of the gene in the condition, 
         *          based on EST expression levels. Note that this information is available 
         *          only if {@link isIncludeSubstructures()} returns {@code false} or {@code null}.
         */
        public BigDecimal getESTMeanRank() {
            return estMeanRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the mean rank of the gene in the condition, 
         *          based on in situ expression data. Note that this information is available 
         *          only if {@link isIncludeSubstructures()} returns {@code false} or {@code null}.
         */
        public BigDecimal getInSituMeanRank() {
            return inSituMeanRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the mean rank of the gene in the condition, 
         *          based on RNA-Seq expression levels. Note that this information is available 
         *          only if {@link isIncludeSubstructures()} returns {@code false} or {@code null}.
         */
        public BigDecimal getRNASeqMeanRank() {
            return rnaSeqMeanRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the mean rank of the gene in a the condition, 
         *          based on the mean rank of each data type requested in the query. 
         *          So for instance, if you configured an {@code ExpressionCallDAOFilter} 
         *          to only retrieved Affymetrix data, then this rank will be equal to the rank 
         *          returned by {@link #getAffymetrixMeanRank()}. Note that this information is available 
         *          only if {@link isIncludeSubstructures()} returns {@code false} or {@code null}.
         */
        public BigDecimal getGlobalMeanRank() {
            return globalMeanRank;
        }

        //**************************************
        // Object methods overridden
        //**************************************
        @Override
        public String toString() {
            return super.toString() + " - Include Substructures: " + this.isIncludeSubstructures() + 
                    " - Include SubStages: " + this.isIncludeSubStages() + 
                    " - Anatomy origin Of Line: " + this.getAnatOriginOfLine() + 
                    " - Stage origin Of Line: " + this.getStageOriginOfLine() + 
                    " - Observed data: " + this.isObservedData() + 
                    " - Global mean rank: " + this.globalMeanRank + 
                    " - Affymetrix mean rank: " + this.affymetrixMeanRank + 
                    " - EST mean rank: " + this.estMeanRank + 
                    " - In situ mean rank: " + this.inSituMeanRank + 
                    " - RNA-Seq mean rank: " + this.rnaSeqMeanRank;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            if (this.useOtherAttributesForHashCodeEquals()) {
                result = prime * result + 
                        ((includeSubStages == null) ? 0 : includeSubStages.hashCode());
                result = prime * result + 
                        ((includeSubstructures == null) ? 0 : includeSubstructures.hashCode());
                result = prime * result +
                        ((anatOriginOfLine == null) ? 0 : anatOriginOfLine.hashCode());
                result = prime * result +
                        ((stageOriginOfLine == null) ? 0 : stageOriginOfLine.hashCode());
                result = prime * result +
                        ((observedData == null) ? 0 : observedData.hashCode());
                
                result = prime * result + ((affymetrixMeanRank == null) ? 0 : 
                    affymetrixMeanRank.hashCode());
                result = prime * result + ((estMeanRank == null) ? 0 : estMeanRank.hashCode());
                result = prime * result + ((globalMeanRank == null) ? 0 : globalMeanRank.hashCode());
                result = prime * result + ((inSituMeanRank == null) ? 0 : inSituMeanRank.hashCode());
                result = prime * result + ((rnaSeqMeanRank == null) ? 0 : rnaSeqMeanRank.hashCode());
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ExpressionCallTO)) {
                return false;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (this.useOtherAttributesForHashCodeEquals()) {
                ExpressionCallTO other = (ExpressionCallTO) obj;
                if (includeSubStages != other.includeSubStages) {
                    return false;
                }
                if (includeSubstructures != other.includeSubstructures) {
                    return false;
                }
                if (anatOriginOfLine != other.anatOriginOfLine) {
                    return false;
                }
                if (stageOriginOfLine != other.stageOriginOfLine) {
                    return false;
                }
                if (observedData != other.observedData) {
                    return false;
                }
                
                if (affymetrixMeanRank == null) {
                    if (other.affymetrixMeanRank != null) {
                        return false;
                    }
                } else if (!affymetrixMeanRank.equals(other.affymetrixMeanRank)) {
                    return false;
                }
                if (estMeanRank == null) {
                    if (other.estMeanRank != null) {
                        return false;
                    }
                } else if (!estMeanRank.equals(other.estMeanRank)) {
                    return false;
                }
                if (globalMeanRank == null) {
                    if (other.globalMeanRank != null) {
                        return false;
                    }
                } else if (!globalMeanRank.equals(other.globalMeanRank)) {
                    return false;
                }
                if (inSituMeanRank == null) {
                    if (other.inSituMeanRank != null) {
                        return false;
                    }
                } else if (!inSituMeanRank.equals(other.inSituMeanRank)) {
                    return false;
                }
                if (rnaSeqMeanRank == null) {
                    if (other.rnaSeqMeanRank != null) {
                        return false;
                    }
                } else if (!rnaSeqMeanRank.equals(other.rnaSeqMeanRank)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * {@code DAOResultSet} specifics to {@code GlobalExpressionToExpressionTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface GlobalExpressionToExpressionTOResultSet 
                    extends DAOResultSet<GlobalExpressionToExpressionTO> {
    }

    /**
     * A {@code TransferObject} representing relation between an expression call and a global
     * expression call in the data source.
     * <p>
     * This class defines a expression call ID (see {@link #getExpressionId()} 
     * and a global expression call ID (see {@link #getGlobalExpressionId()}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class GlobalExpressionToExpressionTO extends TransferObject {
        // TODO modify the class to be immutable.
        private static final long serialVersionUID = -46963749760698289L;

        /**
         * A {@code String} representing the ID of the expression call.
         */
        private final String expressionId;

        /**
         * A {@code String} representing the ID of the global expression call.
         */
        private final String globalExpressionId;

        /**
         * Constructor providing the expression call ID (see {@link #getExpressionId()}) and 
         * the global expression call ID (see {@link #getGlobalExpressionId()}).
         * 
         * @param expressionId          A {@code String} that is the ID of the expression call.
         * @param globalExpressionId    A {@code String} that is the ID of the global expression 
         *                              call.
         **/
        public GlobalExpressionToExpressionTO(String expressionId, String globalExpressionId) {
            super();
            this.expressionId = expressionId;
            this.globalExpressionId = globalExpressionId;
        }

        /**
         * @return  the {@code String} representing the ID of the expression call.
         */
        public String getExpressionId() {
            return expressionId;
        }

        /**
         * @return  the {@code String} representing the ID of the global expression call.
         */
        public String getGlobalExpressionId() {
            return globalExpressionId;
        }
        
        @Override
        public String toString() {
            return "expressionId: " + expressionId + 
                    " - globalExpressionId: " + globalExpressionId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((expressionId == null) ? 0 : expressionId.hashCode());
            result = prime * result + 
                    ((globalExpressionId == null) ? 0 : globalExpressionId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            GlobalExpressionToExpressionTO other = (GlobalExpressionToExpressionTO) obj;
            if (expressionId == null) {
                if (other.expressionId != null) {
                    return false;
                }
            } else if (!expressionId.equals(other.expressionId)) {
                return false;
            }
            if (globalExpressionId == null) {
                if (other.globalExpressionId != null) {
                    return false;
                }
            } else if (!globalExpressionId.equals(other.globalExpressionId)) {
                return false;
            }
            return true;
        }
    }
}
