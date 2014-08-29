package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

/**
 * DAO defining queries using or retrieving {@link DiffExpressionCallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public interface DiffExpressionCallDAO extends DAO<DiffExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the 
     * {@code DiffExpressionCallTO}s obtained from this {@code DiffExpressionCallDAO}.
     * <ul>
     * <li>{@code ID: corresponds to {@link CallTO#getId()}.
     * <li>{@code GENEID: corresponds to {@link CallTO#getGeneId()}.
     * <li>{@code DEVSTAGEID: corresponds to {@link CallTO#getDevStageId()}.
     * <li>{@code ANATENTITYID: corresponds to {@link CallTO#getAnatEntityId()}.
     * <li>{@code AFFYMETRIXDATA: corresponds to {@link CallTO#getAffymetrixData()}.
     * <li>{@code ESTDATA: corresponds to {@link CallTO#getESTData()}.
     * <li>{@code INSITUDATA: corresponds to {@link CallTO#getInSituData()}.
     * <li>{@code RELAXEDINSITUDATA: corresponds to {@link CallTO#getRelaxedInSituData()}.
     * <li>{@code RNASEQDATA;: corresponds to {@link CallTO#getRNASeqData()}.

     * <li>{@code DIFFCALLTYPE}: corresponds to {@link DiffExpressionCallTO#getDiffCallType()}.
     * <li>{@code MINCONDITIONCOUNT}: corresponds to 
     *                                {@link DiffExpressionCallTO#getMinConditionCount()}.
     * <li>{@code FACTOR}: corresponds to {@link DiffExpressionCallTO#getFactor()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENEID, DEVSTAGEID, ANATENTITYID, 
        AFFYMETRIXDATA, ESTDATA, INSITUDATA, RELAXEDINSITUDATA, RNASEQDATA,
        DIFFCALLTYPE, MINCONDITIONCOUNT, FACTOR;
    }

    /**
     * {@code DAOResultSet} specifics to {@code DiffExpressionCallTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface DiffExpressionCallTOResultSet extends DAOResultSet<DiffExpressionCallTO> {
    }

    /**
     * A {@code CallTO} specific to differential expression calls (comparison of 
     * the expression of a gene in different conditions, as part of a differential 
     * expression analysis). Their specificities are that: they can be associated to 
     * different differential expression call types, see {@link DiffCallType}; 
     * they are associated to the minimum number of conditions that were compared 
     * among all the differential expression analyzes that allowed to produce that call; 
     * and that they are associated to a {@link Factor}, defining what was the comparison 
     * factor used during the analyzes generating this call.
     * <p>
     * Of note, there is no data propagation from anatomical entities nor developmental stages 
     * for differential expression calls.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class DiffExpressionCallTO extends CallTO {
        private static final long serialVersionUID = 1130761423323249175L;
        /**
         * Represents different types of differential expression calls obtained 
         * from differential expression analyzes: 
         * <ul>
         * <li>{@code OVEREXPRESSED}: over-expression calls.
         * <li>{@code UNDEREXPRESSED}: under-expression calls.
         * <li>{@code NOTDIFFEXPRESSED}: means that a gene was studied in 
         * a differential expression analysis, but was <strong>not</strong> found to be 
         * differentially expressed (neither {@code OVEREXPRESSED} nor 
         * {@code UNDEREXPRESSED} calls). 
         * </ul>
         */
        public enum DiffCallType {
            OVEREXPRESSED, UNDEREXPRESSED, NOTDIFFEXPRESSED;
        }
        /**
         * Define the different types of differential expression analyses, 
         * based on the experimental factor studied: 
         * <ul>
         * <li>ANATOMY: analyzes comparing different anatomical structures at a same 
         * (broad) developmental stage. The experimental factor is the anatomy, 
         * these analyzes try to identify in which anatomical structures genes are 
         * differentially expressed. 
         * <li>DEVELOPMENT: analyzes comparing for a same anatomical structure 
         * different developmental stages. The experimental factor is the developmental time, 
         * these analyzes try to identify for a given anatomical structures at which 
         * developmental stages genes are differentially expressed. 
         * </ul>
         */
        public enum Factor {
            ANATOMY, DEVELOPMENT;
        }
        
        /**
         * The {@code DiffCallType} that is the type of differential expression of this call.
         */
        private DiffCallType diffCallType;
        /**
         * An {@code int} defining the minimum number of conditions that were compared, 
         * among all the differential expression analyzes that allowed to produce this call.
         */
        private int minConditionCount;
        /**
         * A {@code Factor} defining what was the comparison factor used during 
         * the differential expression analyzes generating this call. 
         */
        private Factor factor;
        
        /**
         * Default constructor
         */
        DiffExpressionCallTO() {
            super();
            this.diffCallType = null;
            this.factor = null;
            this.minConditionCount = 0;
        }
        
        /**
         * Constructor providing the type of differential expression of this call, 
         * the comparison factor used, and the minimum number of conditions compared.
         * 
         * @param diffCallType  The {@code DiffCallType} that is the type of 
         *                      differential expression of this call.
         * @param factor        The {@code Factor} defining what was the comparison 
         *                      factor used during the differential expression analyzes 
         *                      generating this call.
         * @param minConditionCount The {@code int} defining the minimum number of 
         *                          conditions that were compared, among all the differential 
         *                          expression analyzes that allowed to produce this call.
         */
        DiffExpressionCallTO(DiffCallType diffCallType, Factor factor, int minConditionCount) {
            super();
            this.diffCallType = diffCallType;
            this.factor = factor;
            this.minConditionCount = minConditionCount;
        }
        
        /**
         * @return  the {@code DiffCallType} that is the type of differential expression 
         *          of this call.
         */
        public DiffCallType getDiffCallType() {
            return this.diffCallType;
        }
        /**
         * @param callType  the {@code DiffCallType} that is the type of differential 
         *                  expression of this call.
         */
        void setDiffCallType(DiffCallType callType) {
            this.diffCallType = callType;
        }

        /**
         * @return  the {@code Factor} defining what was the comparison factor used 
         *          during the differential expression analyzes generating this call. 
         */
        public Factor getFactor() {
            return factor;
        }
        /**
         * @param factor    the {@code Factor} defining what was the comparison 
         *                  factor used during the differential expression analyzes 
         *                  generating this call. 
         */
        void setFactor(Factor factor) {
            this.factor = factor;
        }
        
        /**
         * @return  the {@code int} defining the minimum number of conditions that 
         *          were compared, among all the differential expression analyzes 
         *          that allowed to produce this call.
         */
        public int getMinConditionCount() {
            return minConditionCount;
        }
        /**
         * @param conditionCount    the {@code int} defining the minimum number of 
         *                          conditions that were compared, among all the 
         *                          differential expression analyzes that allowed 
         *                          to produce this call.
         */
        void setMinConditionCount(int conditionCount) {
            this.minConditionCount = conditionCount;
        }
    }
}
