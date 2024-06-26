package org.bgee.model.dao.api.expressiondata.call;

import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.OrderingDAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO;

/**
 * DAO defining queries using or retrieving {@link DiffExpressionCallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13
 */
public interface DiffExpressionCallDAO 
    extends OrderingDAO<DiffExpressionCallDAO.Attribute, DiffExpressionCallDAO.OrderingAttribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the 
     * {@code DiffExpressionCallTO}s obtained from this {@code DiffExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link DiffExpressionCallTO#getId()}.
     * <li>{@code GENE_ID}: corresponds to {@link DiffExpressionCallTO#getBgeeGeneId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link DiffExpressionCallTO#getConditionId()}.
     * <li>{@code COMPARISON_FACTOR}: corresponds to 
     *                                  {@link DiffExpressionCallTO#getComparisonFactor()}.
     * <li>{@code DIFF_EXPR_CALL_AFFYMETRIX}: corresponds to 
     *                        {@link DiffExpressionCallTO#getDiffExprCallTypeAffymetrix()}.
     * <li>{@code DIFF_EXPR_AFFYMETRIX_DATA}: corresponds to 
     *                        {@link DiffExpressionCallTO#getAffymetrixData()}.
     * <li>{@code BEST_P_VALUE_AFFYMETRIX}: corresponds to 
     *                        {@link DiffExpressionCallTO#getBestPValueAffymetrix()}.
     * <li>{@code CONSISTENT_DEA_COUNT_AFFYMETRIX}: corresponds to 
     *                        {@link DiffExpressionCallTO#getConsistentDEACountAffymetrix()}.
     * <li>{@code INCONSISTENT_DEA_COUNT_AFFYMETRIX_FOUND}: corresponds to 
     *                        {@link DiffExpressionCallTO#getInconsistentDEACountAffymetrix()}.
     * <li>{@code DIFF_EXPR_CALL_RNA_SEQ}: corresponds to 
     *                        {@link DiffExpressionCallTO#getDiffExprCallTypeRNASeq()}.
     * <li>{@code DIFF_EXPR_RNA_SEQ_DATA}: corresponds to 
     *                        {@link DiffExpressionCallTO#getRNASeqData()}.
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
    public enum Attribute implements CallDAO.Attribute {
        ID(false), GENE_ID(false), CONDITION_ID(false), COMPARISON_FACTOR(false),
        DIFF_EXPR_CALL_AFFYMETRIX(false), DIFF_EXPR_AFFYMETRIX_DATA(true), BEST_P_VALUE_AFFYMETRIX(false), 
        CONSISTENT_DEA_COUNT_AFFYMETRIX(false), INCONSISTENT_DEA_COUNT_AFFYMETRIX(false),
        DIFF_EXPR_CALL_RNA_SEQ(false), DIFF_EXPR_RNA_SEQ_DATA(true), BEST_P_VALUE_RNA_SEQ(false), 
        CONSISTENT_DEA_COUNT_RNA_SEQ(false), INCONSISTENT_DEA_COUNT_RNA_SEQ(false);
        
        /**
         * @see #isDataTypeAttribute()
         */
        private final boolean dataTypeAttribute;
        
        private Attribute(boolean dataTypeAttribute) {
            this.dataTypeAttribute = dataTypeAttribute;
        }
        
        @Override
        public boolean isDataTypeAttribute() {
            return dataTypeAttribute;
        }
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
    public enum OrderingAttribute implements DAO.OrderingAttribute {
        OMA_GROUP
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
    public final class DiffExpressionCallTO extends CallTO<Attribute> {
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
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code DiffExprCallType}.
             * @see #convert(Class, String)
             */
            public static final DiffExprCallType convertToDiffExprCallType(String representation) {
                log.entry(representation);
                return log.traceExit(TransferObject.convert(DiffExprCallType.class, representation));
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
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code ComparisonFactor}.
             */
            public static final ComparisonFactor convertToComparisonFactor(String representation) {
                log.entry(representation);
                return log.traceExit(TransferObject.convert(ComparisonFactor.class, representation));
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
            this(null, null, null, null, null, null, null, 
                    null, null, null, null, null, null, null);
        }
        
        /**
         * Constructor providing the type of differential expression of this call, 
         * the comparison factor used, and the minimum number of conditions compared.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                                An {@code Integer} that is the ID of this call.
         * @param bgeeGeneId                        An {@code Integer} that is the ID of the gene associated to 
         *                                          this call.
         * @param conditionId                       An {@code Integer} that is the ID of the condition
         *                                          associated to this call. 
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
        public DiffExpressionCallTO(Integer id, Integer bgeeGeneId, Integer conditionId, 
                ComparisonFactor comparisonFactor, DiffExprCallType diffExprCallTypeAffymetrix, 
                DataState diffExprAffymetrixData, Float bestPValueAffymetrix, 
                Integer consistentDEACountAffymetrix, Integer inconsistentDEACountAffymetrix, 
                DiffExprCallType diffExprCallTypeRNASeq, DataState diffExprRNASeqData,
                Float bestPValueRNASeq, Integer consistentDEACountRNASeq, 
                Integer inconsistentDEACountRNASeq) {
            super(id, bgeeGeneId, conditionId, diffExprAffymetrixData, null, null, 
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

        @Override
        public Map<Attribute, DataState> extractDataTypesToDataStates() {
            log.traceEntry();
            
            Map<Attribute, DataState> typesToStates = new EnumMap<>(Attribute.class);
            
            typesToStates.put(Attribute.DIFF_EXPR_AFFYMETRIX_DATA, this.getAffymetrixData());
            typesToStates.put(Attribute.DIFF_EXPR_RNA_SEQ_DATA, this.getRNASeqData());
            
            return log.traceExit(typesToStates);
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
        protected EnumMap<Attribute, DataState> extractFilteringDataTypes() {
            log.traceEntry();
            return log.traceExit(super.extractFilteringDataTypes(Attribute.class));
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
    }
}
