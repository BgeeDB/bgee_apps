package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

/**
 * DAO defining queries using or retrieving {@link ExpressionCallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13
 */
public interface ExpressionCallDAO extends DAO<ExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ExpressionCallTO}s 
     * obtained from this {@code ExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ExpressionCallTO#getId()}.
     * <li>{@code GENE_ID}: corresponds to {@link ExpressionCallTO#getGeneId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link ExpressionCallTO#getStageId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link ExpressionCallTO#getAnatEntityId()}.
     * <li>{@code AFFYMETRIX_DATA}: corresponds to {@link ExpressionCallTO#getAffymetrixData()}.
     * <li>{@code EST_DATA}: corresponds to {@link ExpressionCallTO#getESTData()}.
     * <li>{@code IN_SITU_DATA}: corresponds to {@link ExpressionCallTO#getInSituData()}.
     * <li>{@code RNA_SEQ_DATA}: corresponds to {@link ExpressionCallTO#getRNASeqData()}.
     * <li>{@code INCLUDE_SUBSTRUCTURES}: corresponds to 
     * {@link ExpressionCallTO#isIncludeSubstructures()}.
     * <li>{@code INCLUDE_SUBSTAGES}: corresponds to {@link ExpressionCallTO#isIncludeSubStages()}.
     * <li>{@code ANAT_ORIGIN_OF_LINE}: corresponds to {@link ExpressionCallTO#getOriginOfLine()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENE_ID, STAGE_ID, ANAT_ENTITY_ID, 
        AFFYMETRIX_DATA, EST_DATA, IN_SITU_DATA, RNA_SEQ_DATA,
        INCLUDE_SUBSTRUCTURES, INCLUDE_SUBSTAGES, 
        ANAT_ORIGIN_OF_LINE, STAGE_ORIGIN_OF_LINE, OBSERVED_DATA;
    }
    
    /**
     * Retrieve expression calls from the data source. The parameters are provided through 
     * {@code DAOCallFilter}s. Note that while it is possible to filter genes to consider 
     * notably by providing ID lists in the {@code DAOCallFilter}s, it is also possible to provide 
     * a global list ({@code globalGeneIds}), overriding any gene IDs provided in the {@code DAOCallFilter}s, 
     * to avoid the need of repeating a same gene ID filtering in several {@code DAOCallFilter}s, 
     * which could be costly, as many gene IDs could be provided.
     * <p>
     * Please also note that all the {@code ExpressionCallTO}s in the provided {@code DAOCallFilter}s 
     * should all have the same propagation states (methods {@code isIncludeSubstructures} and 
     * {@code isIncludeSubStages}), otherwise, an {@code IllegalArgumentException} is thrown.
     * <p>
     * It is possible to request only for genes that are orthologous at the level of a targeted taxon, 
     * by setting {@code taxonId}. In that case, only genes that are orthologous in this taxon 
     * will be considered. If the species member of the provided taxon should be 
     * further filtered, a list of species IDs should be defined in the {@code DAOCallFilter}s. 
     * Otherwise, all species existing in the targeted taxon will be considered. 
     * If {@code taxonId} is blank, then no gene orthology is considered. 
     * <p>
     * The expression calls are retrieved and returned as an {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param callFiters            A {@code Collection} of {@code DAOCallFilter}s using 
     *                              {@code ExpressionCallTO}s, allowing to configure this query. 
     *                              If several {@code DAOCallFilter}s are provided, they are seen 
     *                              as "OR" conditions. Can be {@code null} or empty.
     * @param globalGeneIds         A {@code Set} of {@code String}s that are IDs of genes 
     *                              to globally filter this query, overriding any gene IDs 
     *                              provided in the {@code DAOCallFilter}s. 
     *                              Can be {@code null} or empty.
     * @param taxonId               A {@code String} that is the ID of a targeted taxon, 
     *                              that should be non-null if only orthologous genes are requested. 
     *                              Can be {@code null}.
     * @param attributes            A {@code Collection} of {@code ExpressionCallDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code ExpressionCallTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code CallDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code ExpressionCallTO}s, 
     *                              the associated value being a {@code DAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      An {@code ExpressionCallTOResultSet} allowing to obtain 
     *                              the requested {@code ExpressionCallTO}s.
     * @throws DAOException             If an error occurred while accessing the data source. 
     * @throws IllegalArgumentException If the {@code DAOCallFilter}s provided define multiple 
     *                                  expression propagation states requested.
     */
    public ExpressionCallTOResultSet getExpressionCalls(
            Collection<DAOCallFilter<ExpressionCallTO>> callFiters, Collection<String> globalGeneIds, 
            String taxonId, Collection<Attribute> attributes, 
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
    public final class ExpressionCallTO extends CallTO {
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
         * Default constructor.
         */
        ExpressionCallTO() {
            this(null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        /**
         * Constructor providing the ID, the gene ID, the anatomical entity ID, the developmental 
         * stage ID, the contribution of Affymetrix, EST, <em>in situ</em>, "relaxed" 
         * <em>in situ</em> and, RNA-Seq data to the generation of this call, whether this 
         * expression call was generated using data from the developmental stage and/or anatomical 
         * entity with the ID alone, or by also considering all descendants by is_a or part_of 
         * relations, even indirect, the origin of line.
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
         */
        public ExpressionCallTO(String id, String geneId, String anatEntityId, String stageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState rnaSeqData,
                Boolean includeSubstructures, Boolean includeSubStages, 
                OriginOfLine anatOrigin, OriginOfLine stageOrigin, Boolean observedData) {
            super(id, geneId, anatEntityId, stageId, affymetrixData, estData, inSituData, 
                    null, rnaSeqData);
            this.includeSubstructures = includeSubstructures;
            this.includeSubStages = includeSubStages;
            this.anatOriginOfLine = anatOrigin;
            this.stageOriginOfLine = stageOrigin;
            this.observedData = observedData;
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

        //**************************************
        // Object methods overridden
        //**************************************
        @Override
        public String toString() {
            return super.toString() + " - Include Substructures: " + this.isIncludeSubstructures() + 
                    " - Include SubStages: " + this.isIncludeSubStages() + 
                    " - Anatomy origin Of Line: " + this.getAnatOriginOfLine() + 
                    " - Stage origin Of Line: " + this.getStageOriginOfLine() + 
                    " - Observed data: " + this.isObservedData();
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
