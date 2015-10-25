package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.OrderingDAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

/**
 * DAO defining queries using or retrieving {@link DiffExpressionCallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public interface DiffExpressionCallDAO 
    extends OrderingDAO<DiffExpressionCallDAO.Attribute, DiffExpressionCallDAO.OrderingAttribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the 
     * {@code DiffExpressionCallTO}s obtained from this {@code DiffExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link DiffExpressionCallTO#getId()}.
     * <li>{@code GENE_ID}: corresponds to {@link DiffExpressionCallTO#getGeneId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link DiffExpressionCallTO#getAnatEntityId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link DiffExpressionCallTO#getStageId()}.
     * <li>{@code COMPARISON_FACTOR}: corresponds to 
     *                                  {@link DiffExpressionCallTO#getComparisonFactor()}.
     * <li>{@code DIFF_EXPR_CALL_AFFYMETRIX}: corresponds to 
     *                        {@link DiffExpressionCallTO#getDiffExprCallTypeAffymetrix()}.
     * <li>{@code DIFF_EXPR_AFFYMETRIX_DATA}: corresponds to 
     *                        {@link DiffExpressionCallTO#getDiffExprAffymetrixData()}.
     * <li>{@code BEST_P_VALUE_AFFYMETRIX}: corresponds to 
     *                        {@link DiffExpressionCallTO#getBestPValueAffymetrix()}.
     * <li>{@code CONSISTENT_DEA_COUNT_AFFYMETRIX}: corresponds to 
     *                        {@link DiffExpressionCallTO#getConsistentDEACountAffymetrix()}.
     * <li>{@code INCONSISTENT_DEA_COUNT_AFFYMETRIX_FOUND}: corresponds to 
     *                        {@link DiffExpressionCallTO#getInconsistentDEACountAffymetrix()}.
     * <li>{@code DIFF_EXPR_CALL_RNA_SEQ}: corresponds to 
     *                        {@link DiffExpressionCallTO#getDiffExprCallTypeRNASeq()}.
     * <li>{@code DIFF_EXPR_RNA_SEQ_DATA}: corresponds to 
     *                        {@link DiffExpressionCallTO#getDiffExprRNASeqData()}.
     * <li>{@code BEST_P_VALUE_RNA_SEQ}: corresponds to 
     *                        {@link DiffExpressionCallTO#getBestPValueRNASeq()}.
     * <li>{@code CONSISTENT_DEA_COUNT_RNA_SEQ}: corresponds to 
     *                        {@link DiffExpressionCallTO#getConsistentDEACountRNASeq()}.
     * <li>{@code INCONSISTENT_DEA_COUNT_RNA_SEQ}: corresponds to 
     *                        {@link DiffExpressionCallTO#getInconsistentDEACountRNASeq()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENE_ID, ANAT_ENTITY_ID, STAGE_ID, COMPARISON_FACTOR,
        DIFF_EXPR_CALL_AFFYMETRIX, DIFF_EXPR_AFFYMETRIX_DATA, BEST_P_VALUE_AFFYMETRIX, 
        CONSISTENT_DEA_COUNT_AFFYMETRIX, INCONSISTENT_DEA_COUNT_AFFYMETRIX,
        DIFF_EXPR_CALL_RNA_SEQ, DIFF_EXPR_RNA_SEQ_DATA, BEST_P_VALUE_RNA_SEQ, 
        CONSISTENT_DEA_COUNT_RNA_SEQ, INCONSISTENT_DEA_COUNT_RNA_SEQ,
    }
    
    /**
     * {@code Enum} used to define the attributes to order {@code DiffExpressionCallTO}s
     * obtained from this {@code DiffExpressionCallDAO}.
     * <ul>
     * <li>{@code OMA_GROUP}: corresponds to order by groups of homologous genes.
     * </ul>
     * @see org.bgee.model.dao.api.OrderingDAO#setOrderingAttributes(LinkedHashMap)
     * @see org.bgee.model.dao.api.OrderingDAO#setOrderingAttributes(Enum[])
     * @see org.bgee.model.dao.api.OrderingDAO#clearOrderingAttributes()
     */
    public enum OrderingAttribute implements OrderingDAO.OrderingAttribute {
        OMA_GROUP
    }

    /**
     * Retrieve differential expression calls from data source according to 
     * {@code DiffExpressionCallParams}.
     * <p>
     * The differential expression calls are retrieved and returned as a
     * {@code DiffExpressionCallTOResultSet}. It is the responsibility of the caller to close 
     * this {@code DAOResultSet} once results are retrieved.
     * 
     * @param params        A {@code DiffExpressionCallParams} that provide the parameters specific 
     *                      to differential expression calls
     * @return              A {@code DiffExpressionCallTOResultSet} allowing to retrieve 
     *                      the requested differential expression calls from the data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    //deprecated because a new CallFilter design is being implemented
    @Deprecated
    public DiffExpressionCallTOResultSet getDiffExpressionCalls(DiffExpressionCallParams params) 
            throws DAOException;

    /**
     * Retrieve differential expression calls for genes homologous in the provided taxon 
     * filtered according to {@code DiffExpressionCallParams}.
     * <p>
     * Only genes that are homologous at the level of the provided taxon will be considered. 
     * If it is not needed to consider all species member of the provided taxon, 
     * they can be further filtered using the methods {@code addSpeciesId} or 
     * {@code addAllSpeciesIds} on the provided {@code DiffExpressionCallParams}.
     * <p>
     * The {@code DiffExpressionCallTO}s could be retrieved ordered by groups of genes 
     * homologous at the level of the provided taxon. To determine the homologous group 
     * being iterated, it is necessary to retrieve the mapping from gene IDs to 
     * homologous group IDs, see 
     * {@link org.bgee.model.dao.api.gene.HierarchicalGroupDAO#getGroupToGene(String)}.
     * <p>
     * The differential expression calls are returned through a
     * {@code DiffExpressionCallTOResultSet}. It is the responsibility of the caller to close 
     * this {@code DAOResultSet} once results are retrieved.
     * 
     * @param params        A {@code DiffExpressionCallParams} that provide the parameters specific 
     *                      to differential expression calls
     * @return              A {@code DiffExpressionCallTOResultSet} allowing to retrieve 
     *                      the requested differential expression calls from the data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    @Deprecated
    public DiffExpressionCallTOResultSet getHomologousGenesDiffExpressionCalls(
            String taxonId, DiffExpressionCallParams params) throws DAOException;

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
     * expression analysis). Their specificities are: 
     * they are associated to a {@link ComparisonFactor}, defining what was 
     * the comparison factor used during the analyzes generating this call; for each 
     * data type, various information can be retrieved, such as the {@link DiffExprCallType} 
     * that was produced by this data type, the best p-value supporting it, etc.
     * <p>
     * Of note, there is no data propagation from anatomical entities nor developmental stages 
     * for differential expression calls.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class DiffExpressionCallTO extends CallTO {
        // TODO modify the class to be immutable. Use a Builder pattern?

        private static final long serialVersionUID = 1130761423323249175L;
        
        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = 
                LogManager.getLogger(DiffExpressionCallTO.class.getName());
        /**
         * Represents the differential expression calls obtained 
         * from differential expression analyzes, for a given data type: 
         * <ul>
         * <li>{@code NO_DATA}:             means that the call has never been observed 
         *                                  for the related data type.
         * <li>{@code NOT_EXPRESSED}:       means that the related gene was never seen 
         *                                  as 'expressed' in any of the samples used 
         *                                  in the analysis for the related data type, 
         *                                  it was then not tested for differential expression.
         * <li>{@code OVER_EXPRESSED}:      over-expressed calls.
         * <li>{@code UNDER_EXPRESSED}:     under-expressed calls.
         * <li>{@code NOT_DIFF_EXPRESSED}:  means that the gene was tested for differential 
         *                                  expression, but no significant fold change observed.
         * </ul>
         */
        public enum DiffExprCallType implements EnumDAOField {
            NO_DATA("no data"), NOT_EXPRESSED("not expressed"), OVER_EXPRESSED("over-expression"), 
            UNDER_EXPRESSED("under-expression"), NOT_DIFF_EXPRESSED("no diff expression");
            
            /**
             * Convert the {@code String} representation of a differential expression call type 
             * (for instance, retrieved from a database) into a {@code DiffExprCallType}. 
             * Operation performed by calling {@link TransferObject#convert(Class, String)} with 
             * {@code DiffExprCallType} as the {@code Class} argument, and {@code representation} 
             * as the {@code String} argument.
             * 
             * @param representation            A {@code String} representing a differential 
             *                                  expression call type.
             * @return A {@code DiffExprCallType} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code DiffExprCallType}.
             * @see #convert(Class, String)
             */
            public static final DiffExprCallType convertToDiffExprCallType(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(DiffExprCallType.class, representation));
            }
            
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code DiffExprCallType}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code DiffExprCallType}.
             */
            private DiffExprCallType(String stringRepresentation) {
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
         * Define the comparison factors used for differential expression analysis: 
         * <ul>
         * <li>{@code ANATOMY}: analyzes comparing different anatomical structures at a same 
         * (broad) developmental stage. The experimental factor is the anatomy, 
         * these analyzes try to identify in which anatomical structures genes are 
         * differentially expressed. 
         * <li>{@code DEVELOPMENT}: analyzes comparing for a same anatomical structure 
         * different developmental stages. The experimental factor is the developmental time, 
         * these analyzes try to identify for a given anatomical structures at which 
         * developmental stages genes are differentially expressed. 
         * </ul>
         */
        public enum ComparisonFactor implements EnumDAOField {
            ANATOMY("anatomy"), DEVELOPMENT("development");
            
            /**
             * Convert the {@code String} representation of a comparison factor (for instance, 
             * retrieved from a database) into a {@code ComparisonFactor}. Operation performed by  
             * calling {@link TransferObject#convert(Class, String)} with {@code ComparisonFactor}   
             * as the {@code Class} argument, and {@code representation} as the {@code String} 
             * argument.
             * 
             * @param representation            A {@code String} representing a comparison factor.
             * @return  A {@code ComparisonFactor} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code ComparisonFactor}.
             */
            public static final ComparisonFactor convertToComparisonFactor(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(ComparisonFactor.class, representation));
            }
            
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code ComparisonFactor}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code ComparisonFactor}.
             */
            private ComparisonFactor(String stringRepresentation) {
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
         * A {@code ComparisonFactor} defining what was the comparison factor used during 
         * the differential expression analyzes generating this call. 
         */
        private ComparisonFactor comparisonFactor;
        
        /**
         * A {@code DiffExprCallType} that is the type of differential expression of this call 
         * generated by Affymetrix.
         */
        private DiffExprCallType diffExprCallTypeAffymetrix;
        
        /**
         * A {@code Float} that is best p-value associated to this call among all the analysis 
         * using Affymetrix comparing this condition.
         */
        private Float bestPValueAffymetrix;
        
        /**
         * An {@code Integer} that is the number of analysis using Affymetrix data 
         * where the same call is found.
         */
        private Integer consistentDEACountAffymetrix;
        
        /**
         * An {@code Integer} that is the number of analysis using Affymetrix data where 
         * a different call is found.
         */
        private Integer inconsistentDEACountAffymetrix;
        
        /**
         * A {@code DiffExprCallType} that is the type of differential expression of this call 
         * generated by RNA-seq.
         */
        private DiffExprCallType diffExprCallTypeRNASeq;
        
        /**
         * A {@code Float} that is best p-value associated to this call among all the analysis 
         * using RNA-seq comparing this condition.
         */
        private Float bestPValueRNASeq;
        
        /**
         * An {@code Integer} that is the number of analysis using RNA-seq data where 
         * the same call is found.
         */
        private Integer consistentDEACountRNASeq;
        
        /**
         * An {@code Integer} that is the number of analysis using RNA-seq data where a 
         * different call is found.
         */
        private Integer inconsistentDEACountRNASeq;

        /**
         * Default constructor
         */
        DiffExpressionCallTO() {
            this(null, null, null, null, null, null, null, null, 
                    null, null, null, null, null, null, null);
        }
        
        /**
         * Constructor providing the type of differential expression of this call, 
         * the comparison factor used, and the minimum number of conditions compared.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                                A {@code String} that is the ID of this call.
         * @param geneId                            A {@code String} that is the ID of the gene 
         *                                          associated to this call.
         * @param anatEntityId                      A {@code String} that is the ID of the 
         *                                          anatomical entity associated to this call. 
         * @param stageId                           A {@code String} that is the ID of the 
         *                                          developmental stage associated to this call. 
         * @param comparisonFactor                  The {@code ComparisonFactor} defining what was 
         *                                          the comparison factor used during the 
         *                                          differential expression analyzes generating 
         *                                          this call.
         * @param diffExprCallTypeAffymetrix        A {@code DiffExprCallType} that is the type of 
         *                                          differential expression of this call generated 
         *                                          by Affymetrix.
         * @param bestPValueAffymetrix              A {@code Float} that is best p-value associated 
         *                                          to this call among all the analysis using 
         *                                          Affymetrix comparing this condition.
         * @param consistentDEACountAffymetrix      An {@code Integer} that is the number of 
         *                                          analysis using Affymetrix data where the same 
         *                                          call is found.
         * @param inconsistentDEACountAffymetrix    An {@code Integer} that is the number of  
         *                                          analysis using Affymetrix data where 
         *                                          a different call is found.
         * @param diffExprCallTypeRNASeq            A {@code DiffExprCallType} that is the type of 
         *                                          differential expression of this call generated 
         *                                          by RNA-seq.
         * @param bestPValueRNASeq                  A {@code Float} that is best p-value associated 
         *                                          to this call among all the analysis using 
         *                                          RNA-seq comparing this condition.
         * @param consistentDEACountRNASeq          An {@code Integer} that is the number of 
         *                                          analysis usingRNA-seq data where the same call 
         *                                          is found.
         * @param inconsistentDEACountRNASeq        An {@code Integer} that is the number of 
         *                                          analysis using RNA-seq data where a different 
         *                                          call is found.
         */
        public DiffExpressionCallTO(String id, String geneId, String anatEntityId, String stageId, 
                ComparisonFactor comparisonFactor, DiffExprCallType diffExprCallTypeAffymetrix, 
                DataState diffExprAffymetrixData, Float bestPValueAffymetrix, 
                Integer consistentDEACountAffymetrix, Integer inconsistentDEACountAffymetrix, 
                DiffExprCallType diffExprCallTypeRNASeq, DataState diffExprRNASeqData,
                Float bestPValueRNASeq, Integer consistentDEACountRNASeq, 
                Integer inconsistentDEACountRNASeq) {
            super(id, geneId, anatEntityId, stageId, diffExprAffymetrixData, null, null, 
                    null, diffExprRNASeqData);
            this.comparisonFactor = comparisonFactor;
            this.diffExprCallTypeAffymetrix = diffExprCallTypeAffymetrix;
            this.bestPValueAffymetrix = bestPValueAffymetrix;
            this.consistentDEACountAffymetrix = consistentDEACountAffymetrix;
            this.inconsistentDEACountAffymetrix = inconsistentDEACountAffymetrix;
            this.diffExprCallTypeRNASeq = diffExprCallTypeRNASeq;
            this.bestPValueRNASeq = bestPValueRNASeq;
            this.consistentDEACountRNASeq = consistentDEACountRNASeq;
            this.inconsistentDEACountRNASeq = inconsistentDEACountRNASeq;
        }
        
        /**
         * @return  the {@code ComparisonFactor} defining whether different organs at a same 
         *          (broad) developmental stage were compared ('anatomy'), or a same organ at 
         *          different developmental stages ('development').
         */
        public ComparisonFactor getComparisonFactor() {
            return comparisonFactor;
        }
        
        /**
         * @param comparisonFactor  A {@code ComparisonFactor} defining whether different organs 
         *                          at a same (broad) developmental stage were compared ('anatomy'), 
         *                          or a same organ at different developmental stages ('development').
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setComparisonFactor(ComparisonFactor comparisonFactor) {
            this.comparisonFactor = comparisonFactor;
        }
        
        /**
         * @return  the {@code DiffExprCallType} that is the type of differential expression 
         *          of this call generated by Affymetrix.
         */
        public DiffExprCallType getDiffExprCallTypeAffymetrix() {
            return this.diffExprCallTypeAffymetrix;
        }
        
        /**
         * @param diffExprCallTypeAffymetrix    A {@code DiffExprCallType} that is the type of 
         *                                      differential expression of this call generated by 
         *                                      Affymetrix.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setDiffExprCallTypeAffymetrix(DiffExprCallType diffExprCallTypeAffymetrix) {
            this.diffExprCallTypeAffymetrix = diffExprCallTypeAffymetrix;
        }
        
        /**
         * @return  the {@code Float} that is best p-value associated to this call among all the 
         *          analysis using Affymetrix comparing this condition.
         */
        public Float getBestPValueAffymetrix() {
            return this.bestPValueAffymetrix;
        }
        
        /**
         * @param bestPValueAffymetrix  A {@code Float} that is best p-value associated to this 
         *                              call among all the analysis using Affymetrix comparing 
         *                              this condition.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setBestPValueAffymetrix(Float bestPValueAffymetrix) {
            this.bestPValueAffymetrix = bestPValueAffymetrix;
        }
        
        /**
         * @return  the {@code Integer} that is the number of analysis using Affymetrix data where 
         *          the same call is found.
         */
        public Integer getConsistentDEACountAffymetrix() {
            return this.consistentDEACountAffymetrix;
        }
        
        /**
         * @param consistentDEACountAffymetrix  An {@code Integer} that is the number of analysis 
         *                                      using Affymetrix data where the same call is found.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setConsistentDEACountAffymetrix(Integer consistentDEACountAffymetrix) {
            this.consistentDEACountAffymetrix = consistentDEACountAffymetrix;
        }
        
        /**
         * @return  the {@code Integer} that is the number of analysis using Affymetrix data where 
         *          a different call is found
         */
        public Integer getInconsistentDEACountAffymetrix() {
            return this.inconsistentDEACountAffymetrix;
        }
        
        /**
         * @param inconsistentDEACountAffymetrix    An {@code Integer} that is the number of  
         *                                          analysis using Affymetrix data where 
         *                                          a different call is found.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setInconsistentDEACountAffymetrix(Integer inconsistentDEACountAffymetrix) {
            this.inconsistentDEACountAffymetrix = inconsistentDEACountAffymetrix;
        }
        
        /**
         * @return  the {@code DiffExprCallType} that is the type of differential expression 
         *          of this call generated by RNA-seq.
         */
        public DiffExprCallType getDiffExprCallTypeRNASeq() {
            return this.diffExprCallTypeRNASeq;
        }
        
        /**
         * @param diffExprCallTypeRNASeq    A {@code DiffExprCallType} that is the type of 
         *                                  differential expression of this call generated 
         *                                  by RNA-seq.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setDiffExprCallTypeRNASeq(DiffExprCallType diffExprCallTypeRNASeq) {
            this.diffExprCallTypeRNASeq = diffExprCallTypeRNASeq;
        }
        
        /**
         * @return  the {@code Float} that is best p-value associated to this call among all the 
         *          analysis using RNA-seq comparing this condition.
         */
        public Float getBestPValueRNASeq() {
            return this.bestPValueRNASeq;
        }
        
        /**
         * @param bestPValueRNASeq  A {@code Float} that is best p-value associated to this call 
         *                          among all the analysis using RNA-seq comparing this condition.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setBestPValueRNASeq(Float bestPValueRNASeq) {
            this.bestPValueRNASeq = bestPValueRNASeq;
        }
        
        /**
         * @return  the {@code Integer} that is the number of analysis using RNA-seq data where 
         *          the same call is found.
         */
        public Integer getConsistentDEACountRNASeq() {
            return this.consistentDEACountRNASeq;
        }
        
        /**
         * @param consistentDEACountRNASeq  An {@code Integer} that is the number of analysis using
         *                                  RNA-seq data where the same call is found.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setConsistentDEACountRNASeq(Integer consistentDEACountRNASeq) {
            this.consistentDEACountRNASeq = consistentDEACountRNASeq;
        }
        
        /**
         * @return  the {@code Integer} that is the number of analysis using RNA-seq data where 
         *          a different call is found.
         */
        public Integer getInconsistentDEACountRNASeq() {
            return this.inconsistentDEACountRNASeq;
        }
        
        /**
         * @param inconsistentDEACountRNASeq    An {@code Integer} that is the number of analysis 
         *                                      using RNA-seq data where a different call is found.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setInconsistentDEACountRNASeq(Integer inconsistentDEACountRNASeq) {
            this.inconsistentDEACountRNASeq = inconsistentDEACountRNASeq;
        }
        
        //**************************************
        // Object methods overridden
        //**************************************
        
        @Override
        public String toString() {
            return super.toString() + " - Comparison factor: " + this.getComparisonFactor() + 
                    " - Differential expression call by Affymetrix: " + this.getDiffExprCallTypeAffymetrix() +
                    " - Best p-value with Affymetrix: " + this.getBestPValueAffymetrix() +
                    " - Consistent DEA Count with Affymetrix: " + this.getConsistentDEACountAffymetrix() + 
                    " - Inconsistent DEA Count with Affymetrix: " + this.getInconsistentDEACountAffymetrix() + 
                    " - Differential expression call by RNA-seq: " + this.getDiffExprCallTypeRNASeq() + 
                    " - Best p-value with RNA-seq: " + this.getBestPValueRNASeq() +
                    " - Consistent DEA Count with RNA-seq: " + this.getConsistentDEACountRNASeq() + 
                    " - Inconsistent DEA Count with RNA-seq: " + this.getInconsistentDEACountRNASeq();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            if (this.useOtherAttributesForHashCodeEquals()) {
                result = prime * result
                        + ((comparisonFactor == null) ? 0 : comparisonFactor.hashCode());
                result = prime * result
                        + ((diffExprCallTypeAffymetrix == null) ? 0 : diffExprCallTypeAffymetrix.hashCode());
                result = prime * result
                        + ((bestPValueAffymetrix == null) ? 0 : bestPValueAffymetrix.hashCode());
                result = prime * result
                        + ((consistentDEACountAffymetrix == null) ? 0
                                : consistentDEACountAffymetrix.hashCode());
                result = prime * result
                        + ((inconsistentDEACountAffymetrix == null) ? 0
                                : inconsistentDEACountAffymetrix.hashCode());
                result = prime * result
                        + ((diffExprCallTypeRNASeq == null) ? 0 : diffExprCallTypeRNASeq.hashCode());
                result = prime * result
                        + ((bestPValueRNASeq == null) ? 0 : bestPValueRNASeq.hashCode());
                result = prime * result
                        + ((consistentDEACountRNASeq == null) ? 0 : consistentDEACountRNASeq.hashCode());
                result = prime * result
                        + ((inconsistentDEACountRNASeq == null) ? 0 : inconsistentDEACountRNASeq.hashCode());
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof DiffExpressionCallTO)) {
                return false;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (this.useOtherAttributesForHashCodeEquals()) {
                DiffExpressionCallTO other = (DiffExpressionCallTO) obj;
                if (comparisonFactor != other.comparisonFactor) {
                    return false;
                }
                if (diffExprCallTypeAffymetrix != other.diffExprCallTypeAffymetrix) {
                    return false;
                }
                if (bestPValueAffymetrix == null) {
                    if (other.bestPValueAffymetrix != null) {
                        return false;
                    }
                } else if (!bestPValueAffymetrix.equals(other.bestPValueAffymetrix)) {
                    return false;
                }
                if (consistentDEACountAffymetrix == null) {
                    if (other.consistentDEACountAffymetrix != null) {
                        return false;
                    }
                } else if (!consistentDEACountAffymetrix.equals(other.consistentDEACountAffymetrix)) {
                    return false;
                }
                if (inconsistentDEACountAffymetrix == null) {
                    if (other.inconsistentDEACountAffymetrix != null) {
                        return false;
                    }
                } else if (!inconsistentDEACountAffymetrix.equals(other.inconsistentDEACountAffymetrix)) {
                    return false;
                }

                if (diffExprCallTypeRNASeq != other.diffExprCallTypeRNASeq) {
                    return false;
                }
                if (bestPValueRNASeq == null) {
                    if (other.bestPValueRNASeq != null) {
                        return false;
                    }
                } else if (!bestPValueRNASeq.equals(other.bestPValueRNASeq)) {
                    return false;
                }                
                if (consistentDEACountRNASeq == null) {
                    if (other.consistentDEACountRNASeq != null) {
                        return false;
                    }
                } else if (!consistentDEACountRNASeq.equals(other.consistentDEACountRNASeq)) {
                    return false;
                }
                if (inconsistentDEACountRNASeq == null) {
                    if (other.inconsistentDEACountRNASeq != null)
                        return false;
                } else if (!inconsistentDEACountRNASeq
                        .equals(other.inconsistentDEACountRNASeq))
                    return false;
            }
            return true;
        }
    }
}
