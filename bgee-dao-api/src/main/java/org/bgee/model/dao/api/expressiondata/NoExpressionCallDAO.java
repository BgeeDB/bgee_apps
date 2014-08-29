package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

public interface NoExpressionCallDAO extends DAO<NoExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code NoExpressionCallTO}s 
     * obtained from this {@code NoExpressionCallDAO}.
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
     * <li>{@code INCLUDEPARENTSTRUCTURES}: corresponds to 
     * {@link NoExpressionCallTO#isIncludeParentStructures()}.
     * <li>{@code ORIGINOFLINE}: corresponds to {@link NoExpressionCallTO#getOriginOfLine()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENEID, DEVSTAGEID, ANATENTITYID, 
        AFFYMETRIXDATA, ESTDATA, INSITUDATA, RELAXEDINSITUDATA, RNASEQDATA,
        INCLUDEPARENTSTRUCTURES, ORIGINOFLINE;
    }

    /**
     * Retrieve all no expression calls from data source according {@code NoExpressionCallParams}.
     * <p>
     * The no expression calls are retrieved and returned as a {@code NoExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param params  An {@code NoExpressionCallParams} that provide the parameters specific 
     *                to no expression calls
     * @return        An {@code NoExpressionCallTOResultSet} containing all no expression calls 
     *                from data source.
     */
    public NoExpressionCallTOResultSet getAllNoExpressionCalls(NoExpressionCallParams params);
    
    /**
     * Inserts the provided no expression calls into the Bgee database, 
     * represented as a {@code Collection} of {@code NoExpressionCallTO}s. 
     * <p>
     * The expression calls are retrieved and returned as a {@code NoExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param noExpressionCalls A {@code Collection} of {@code NoExpressionCallTO}s 
     *                          to be inserted into the database.
     * @return                  An {@code int} that is the number of inserted 
     *                          no expression calls.
     */
    public int insertNoExpressionCalls(Collection<NoExpressionCallTO> noExpressionCalls);


    /**
     * {@code DAOResultSet} specifics to {@code NoExpressionCallTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface NoExpressionCallTOResultSet extends DAOResultSet<NoExpressionCallTO> {
        
    }

    /**
     * A {@code CallTO} specific to no-expression calls (explicit report of absence 
     * of expression). Their specificity is that they can be produced using data propagation 
     * from parent anatomical entities by <em>is_a</em> or <em>part_of</em> relations. 
     * See {@link #isIncludeParentStructures()} for more details.
     * <p>
     * Of note, there is no data propagation from developmental stages for no-expression 
     * calls.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class NoExpressionCallTO extends CallTO {
        private static final long serialVersionUID = 5793434647776540L;
        /**
         * A {@code boolean} defining whether this no-expression call was generated 
         * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
         * relations, even indirect. If {@code true}, all its parents were considered. 
         * So for instance, if B is_a A, and absence of expression has been reported in A, 
         * then B could benefit from this information. In other words, when a gene 
         * is not expressed in a structure, it is expressed nowhere in that structure.
         */
        private boolean includeParentStructures;

        /**
         * An {@code Enum} used to define the origin of line.
         * <ul>
         * <li>{@code SELF}: this no expression call exists in itself.
         * <li>{@code PARENT}: this no expression call exists in one of its parents.
         * <li>{@code BOTH}: this no expression call exists in itself, AND in one parent at the 
         * same time.
         * </ul>
         */
        public enum OriginOfLineType {
            SELF, PARENT, BOTH;
        }
        
        /**
         * An {@code OriginOfLineType} used to define the origin of line: either {@code SELF},
         * {@code PARENT} or {@code BOTH}.
         * 
         * @see OriginOfLineType
         */
        private OriginOfLineType originOfLine; 

        /**
         * Default constructor.
         */
        NoExpressionCallTO() {
            super();
            this.includeParentStructures = false;
            originOfLine = OriginOfLineType.SELF;
        }

        /**
         * Default constructor.
         */
        NoExpressionCallTO(String id, String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData, boolean includeParentStructures) {
            super(id, geneId, anatEntityId, devStageId, affymetrixData, estData, inSituData, 
                    relaxedInSituData, rnaSeqData);
            this.includeParentStructures = includeParentStructures;
            originOfLine = OriginOfLineType.SELF;
       }

        /**
         * Default constructor.
         */
        NoExpressionCallTO(String id, String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData, boolean includeParentStructures,
                OriginOfLineType originOfLine) {
            super(id, geneId, anatEntityId, devStageId, affymetrixData, estData, inSituData, 
                    relaxedInSituData, rnaSeqData);
            this.includeParentStructures = includeParentStructures;
            this.originOfLine = originOfLine;
      }

        /**
         * Returns the {@code boolean} defining whether this no-expression call was generated 
         * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
         * relations, even indirect. If {@code true}, all its parents were considered. 
         * So for instance, if B is_a A, and absence of expression has been reported in A, 
         * then B could benefit from this information. In other words, when a gene 
         * is not expressed in a structure, it is expressed nowhere in that structure.
         * 
         * @return  If {@code true}, all parents of the anatomical entity were considered.
         */
        public boolean isIncludeParentStructures() {
            return includeParentStructures;
        }

        /**
         * Sets the {@code boolean} defining whether this no-expression call was generated 
         * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
         * relations, even indirect. If {@code true}, all its parents were considered. 
         * So for instance, if B is_a A, and absence of expression has been reported in A, 
         * then B could benefit from this information. In other words, when a gene 
         * is not expressed in a structure, it is expressed nowhere in that structure.
         * 
         * @param includeParentStructures   A {@code boolean} defining whether parents 
         *                                  of the anatomical entity were considered.
         */
        void setIncludeParentStructures(boolean includeParentStructures) {
            this.includeParentStructures = includeParentStructures;
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
            return super.toString() +  
                    " - Include Parent Structures: " + this.isIncludeParentStructures() +
                    " - Origin Of Line: " + this.getOriginOfLine();
        }
}
}
