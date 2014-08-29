package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;

/**
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public interface ExpressionCallDAO extends DAO<ExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ExpressionCallTO}s 
     * obtained from this {@code ExpressionCallDAO}.
     * <ul>
     * <li>{@code ID: corresponds to {@link CallTO#getId()}.
     * <li>{@code GENEID: corresponds to {@link CallTO#getGeneId()}.
     * <li>{@code DEVSTAGEID: corresponds to {@link CallTO#getDevStageId()}.
     * <li>{@code ANATENTITYID: corresponds to {@link CallTO#getAnatEntityId()}.
     * <li>{@code AFFYMETRIXDATA: corresponds to {@link CallTO#getAffymetrixData()}.
     * <li>{@code ESTDATA: corresponds to {@link CallTO#getESTData()}.
     * <li>{@code INSITUDATA: corresponds to {@link CallTO#getInSituData()}.
     * <li>{@code RELAXEDINSITUDATA: corresponds to {@link CallTO#getRelaxedInSituData()}.
     * <li>{@code RNASEQDATA: corresponds to {@link CallTO#getRNASeqData()}.
     * <li>{@code INCLUDESUBSTRUCTURES}: corresponds to 
     * {@link ExpressionCallTO#isIncludeSubstructures()}.
     * <li>{@code INCLUDESUBSTAGES}: corresponds to {@link ExpressionCallTO#isIncludeSubStages()}.
     * <li>{@code ORIGINOFLINE}: corresponds to {@link NoExpressionCallTO#getOriginOfLine()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENEID, DEVSTAGEID, ANATENTITYID, 
        AFFYMETRIXDATA, ESTDATA, INSITUDATA, RELAXEDINSITUDATA, RNASEQDATA,
        INCLUDESUBSTRUCTURES, INCLUDESUBSTAGES, ORIGINOFLINE;
    }
    
    /**
     * Retrieve all expression calls from data source according {@code ExpressionCallParams}.
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param params  An {@code ExpressionCallParams} that provide the parameters specific 
     *                to expression calls
     * @return        An {@code ExpressionCallTOResultSet} containing all expression calls 
     *                from data source.
     */
    public ExpressionCallTOResultSet getAllExpressionCalls(ExpressionCallParams params);
    
    /**
     * Retrieve all expression calls from data source according {@code Set} of species ID.
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds  A {@code Set} of {@code String} that are the species ID to use as filter.
     * @return            An {@code ExpressionCallTOResultSet} containing all expression calls 
     *                    from data source.
     */
    public ExpressionCallTOResultSet getAllExpressionCalls(Set<String> speciesIds) ;

    /**
     * Inserts the provided expression calls into the Bgee database, 
     * represented as a {@code Collection} of {@code ExpressionCallTO}s. 
     * <p>
     * The expression calls are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param expressionCalls   A {@code Collection} of {@code ExpressionCallTO}s 
     *                          to be inserted into the database.
     * @return                  An {@code int} that is the number of inserted 
     *                          expression calls.
     */
    public int insertExpressionCalls(Collection<ExpressionCallTO> expressionCalls);

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
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class ExpressionCallTO extends CallTO {
        private static final long serialVersionUID = 1198652013999835872L;
        
        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(ExpressionCallTO.class.getName());

        /**
         * A {@code boolean} defining whether this expression call was generated 
         * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered. 
         */
        private boolean includeSubstructures;
        
        /**
         * A {@code boolean} defining whether this expression call was generated 
         * using data from the developmental stage with the ID {@link CallTO#getDevStageId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered.
         */
        private boolean includeSubStages;
        
        /**
         * An {@code Enum} used to define the origin of line.
         * <ul>
         * <li>{@code SELF}: this expression call was generated by its own data.
         * <li>{@code DESCENT}: this expression call was generated by data from one of its 
         * descendants by <em>is_a</em> or <em>part_of</em> relations, even indirect.
         * </ul>
         */
        public enum OriginOfLineType {
            SELF, DESCENT;
        }
        
        /**
         * An {@code OriginOfLineType} used to define the origin of line: either {@code SELF}
         * or {@code DESCENT}.
         * 
         * @see OriginOfLineType
         */
        private OriginOfLineType originOfLine; 
        
        /**
         * Default constructor.
         */
        ExpressionCallTO() {
            super();
            includeSubstructures = false;
            includeSubStages = false;
            originOfLine = OriginOfLineType.SELF;
        }

        /**
         * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
         * the contribution of Affymetrix, EST, <em>in situ</em>, "relaxed" <em>in situ</em> and, 
         * RNA-Seq data to the generation of this call, whether this expression call was generated 
         * using data from the developmental stage and/or anatomical entity with the ID alone, 
         * or by also considering all descendants by is_a or part_of relations, even indirect.
         * 
         * @param geneId               A {@code String} that is the ID of the gene associated to 
         *                             this call.
         * @param anatEntityId         A {@code String} that is the ID of the anatomical entity
         *                             associated to this call. 
         * @param devStageId           A {@code String} that is the ID of the developmental stage 
         *                             associated to this call. 
         * @param affymetrixData       A {@code DataSate} that is the contribution of Affymetrix  
         *                             data to the generation of this call.
         * @param estData              A {@code DataSate} that is the contribution of EST data
         *                             to the generation of this call.
         * @param inSituData           A {@code DataSate} that is the contribution of 
         *                             <em>in situ</em> data to the generation of this call.
         * @param relaxedInSituData    A {@code DataSate} that is the contribution of "relaxed" 
         *                             <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData           A {@code DataSate} that is the contribution of RNA-Seq data
         *                             to the generation of this call.
         * @param includeSubstructures A {@code boolean} defining whether this expression call was 
         *                             generated using data from the anatomical entity with the ID 
         *                             alone, or by also considering all its descendants by 
         *                             <em>is_a</em> or <em>part_of</em> relations, even indirect.
         * @param includeSubStages     A {@code boolean} defining whether this expression call was 
         *                             generated using data from the developmental stage with the ID
         *                             alone, or by also considering all its descendants by 
         *                             <em>is_a</em> or <em>part_of</em> relations, even indirect.
         */
        public ExpressionCallTO(String id, String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData,
                boolean includeSubstructures, boolean includeSubStages) {
            super(id, geneId, anatEntityId, devStageId, affymetrixData, estData, inSituData, 
                    relaxedInSituData, rnaSeqData);
            this.includeSubstructures = includeSubstructures;
            this.includeSubStages = includeSubStages;
            this.originOfLine = OriginOfLineType.SELF;
        }
        
        /**
         * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
         * the contribution of Affymetrix, EST, <em>in situ</em>, "relaxed" <em>in situ</em> and, 
         * RNA-Seq data to the generation of this call, whether this expression call was generated 
         * using data from the developmental stage and/or anatomical entity with the ID alone, 
         * or by also considering all descendants by is_a or part_of relations, even indirect, the
         * origin of line.
         * 
         * @param geneId               A {@code String} that is the ID of the gene associated to 
         *                             this call.
         * @param anatEntityId         A {@code String} that is the ID of the anatomical entity
         *                             associated to this call. 
         * @param devStageId           A {@code String} that is the ID of the developmental stage 
         *                             associated to this call. 
         * @param affymetrixData       A {@code DataSate} that is the contribution of Affymetrix  
         *                             data to the generation of this call.
         * @param estData              A {@code DataSate} that is the contribution of EST data
         *                             to the generation of this call.
         * @param inSituData           A {@code DataSate} that is the contribution of 
         *                             <em>in situ</em> data to the generation of this call.
         * @param relaxedInSituData    A {@code DataSate} that is the contribution of "relaxed" 
         *                             <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData           A {@code DataSate} that is the contribution of RNA-Seq data
         *                             to the generation of this call.
         * @param includeSubstructures A {@code boolean} defining whether this expression call was 
         *                             generated using data from the anatomical entity with the ID 
         *                             alone, or by also considering all its descendants by 
         *                             <em>is_a</em> or <em>part_of</em> relations, even indirect.
         * @param includeSubStages     A {@code boolean} defining whether this expression call was 
         *                             generated using data from the developmental stage with the ID
         *                             alone, or by also considering all its descendants by 
         *                             <em>is_a</em> or <em>part_of</em> relations, even indirect.
         * @param originOfLine         A {@code OriginOfLineType} defining the origin of line.
         */
        public ExpressionCallTO(String id, String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData,
                boolean includeSubstructures, boolean includeSubStages, 
                OriginOfLineType originOfLine) {
            this(id, geneId, anatEntityId, devStageId, affymetrixData, estData, inSituData, 
                    relaxedInSituData, rnaSeqData, includeSubstructures, includeSubStages);
            this.includeSubstructures = includeSubstructures;
            this.originOfLine = originOfLine;
        }

        /**
         * Convert the origin of call from the data source into an {@code OriginOfLineType}.
         * 
         * @param databaseEnum  A {@code String} that is origin of call from the data source.
         * @return              An {@code OriginOfLineType} representing the given {@code String}. 
         */
        public static OriginOfLineType convertDatasourceEnumToOriginOfLineType(String databaseEnum) {
            log.entry(databaseEnum);
            
            OriginOfLineType originType = null;
            if (databaseEnum.equals("self")) {
                originType = OriginOfLineType.SELF;
            } else if (databaseEnum.equals("descent")) {
                originType = OriginOfLineType.DESCENT;
            }
            
            return log.exit(originType);
        }

        /**
         * Convert the origin of call from the data source into an {@code OriginOfLineType}.
         * 
         * @param databaseEnum  A {@code String} that is origin of call from the data source.
         * @return              An {@code OriginOfLineType} representing the given {@code String}. 
         */
        public static String convertOriginOfLineTypeToDatasourceEnum(OriginOfLineType dataType) {
            log.entry(dataType);
            
            String databaseEnum = null;
            if (dataType == OriginOfLineType.SELF) {
                databaseEnum = "self";
            } else if (dataType == OriginOfLineType.DESCENT) {
                databaseEnum = "descent";
            }
            
            return log.exit(databaseEnum);
        }


        /**
         * Returns the {@code boolean} defining whether this expression call was generated 
         * using data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered.
         * 
         * @return  If {@code true}, all descendants of the anatomical entity were considered. 
         */
        public boolean isIncludeSubstructures() {
            return includeSubstructures;
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
        void setIncludeSubstructures(boolean includeSubstructures) {
            this.includeSubstructures = includeSubstructures;
        }

        /**
         * Returns the {@code boolean} defining whether this expression call was generated 
         * using data from the developmental stage with the ID {@link CallTO#getDevStageId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered.
         * 
         * @return  If {@code true}, all descendants of the developmental stage 
         *          were considered. 
         */
        public boolean isIncludeSubStages() {
            return includeSubStages;
        }
        /**
         * Sets the {@code boolean} defining whether this expression call was generated 
         * using data from the developmental stage with the ID {@link CallTO#getDevStageId()} 
         * alone, or by also considering all its descendants by <em>is_a</em> or 
         * <em>part_of</em> relations, even indirect. If {@code true}, all its descendants 
         * were considered.
         * 
         * @param includeSubstructures  A {@code boolean} defining whether descendants 
         *                              of the developmental stage were considered.
         */
        void setIncludeSubStages(boolean includeSubStages) {
            this.includeSubStages = includeSubStages;
        }
        
        /**
         * @return  the {@code OriginOfLineType} representing the origin of the call.
         */
        public OriginOfLineType getOriginOfLine() {
            return originOfLine;
        }
        
        /**
         * @param originOfLine  the {@code OriginOfLineType} representing the origin of the call.
         */
        void setOriginOfLine(OriginOfLineType originOfLine) {
            this.originOfLine = originOfLine;
        }

        @Override
        public String toString() {
            return super.toString() + " - Include SubStages: " + this.isIncludeSubStages() + 
                    " - Include Substructures: " + this.isIncludeSubstructures() +
                    " - Origin Of Line: " + this.getOriginOfLine();
        }
    }
    /**
     * {@code EntityTO} representing relation between expression table and globalExpression 
     * table in the Bgee data source.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class GlobalExpressionToExpressionTO extends CallTO {
        
        private static final long serialVersionUID = -46963749760698289L;
        
        /**
         * A {@code String} representing the ID of the expression call .
         */
        private String expressionId;
        
        /**
         * A {@code String} representing the ID of the global expression call.
         */
        private String globalExpressionId;
        
        /**
         * Default constructor.
         */
        GlobalExpressionToExpressionTO() {
            super();
        }
        
        /**
         * Constructor providing the expression call ID and the global expression call ID.  
         **/
       public GlobalExpressionToExpressionTO(String expressionId, String globalExpressionId) {
            super();
            this.setExpressionId(expressionId);
            this.setGlobalExpressionId(globalExpressionId);
        }
        
        /**
         * @return  the {@code String} representing the ID of the expression call.
         */
        public String getExpressionId() {
            return expressionId;
        }
        
        /**
         * @param expressionId  the {@code String} representing the ID of the expression call.
         */
        void setExpressionId(String expressionId) {
            this.expressionId = expressionId;
        }

        /**
         * @return  the {@code String} representing the ID of the global expression call.
         */
        public String getGlobalExpressionId() {
            return globalExpressionId;
        }
        
        /**
         * @param globalExpressionId    the {@code String} representing the 
         *                              ID of the global expression call.
         */
        void setGlobalExpressionId(String globalExpressionId) {
            this.globalExpressionId = globalExpressionId;
        }
    }
}
