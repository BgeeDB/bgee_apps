package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.expressiondata.CallTO;

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
     * <li>{@code INCLUDESUBSTRUCTURES}: corresponds to 
     * {@link ExpressionCallTO#isIncludeSubstructures()()}.
     * <li>{@code INCLUDESUBSTAGES}: corresponds to {@link ExpressionCallTO#isIncludeSubStages()()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        INCLUDESUBSTRUCTURES, INCLUDESUBSTAGES;
    }
    
    /**
     * Retrieve all expression calls from data source according {@code ExpressionCallParams}.
     * <p>
     * The genes are retrieved and returned as a {@code ExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @return A {@code ExpressionCallTOResultSet} containing all genes from data source.
     */
    public ExpressionCallTOResultSet getAllExpressionCalls(ExpressionCallParams params);
    
    /**
     * {@code DAOResultSet} specifics to {@code GeneTO}s
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
         * Default constructor.
         */
        ExpressionCallTO() {
            super();
            this.setIncludeSubstructures(false);
            this.setIncludeSubStages(false);
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
        public ExpressionCallTO(String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData,
                boolean includeSubstructures, boolean includeSubStages) {
            super(geneId, anatEntityId, devStageId, affymetrixData, estData, inSituData, 
                    relaxedInSituData, rnaSeqData);
            this.setIncludeSubstructures(includeSubstructures);
            this.setIncludeSubStages(includeSubStages);
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
    }
}
