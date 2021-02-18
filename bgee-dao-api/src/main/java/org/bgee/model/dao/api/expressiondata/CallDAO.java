package org.bgee.model.dao.api.expressiondata;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries using or retrieving {@link CallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see CallTO
 * @since Bgee 13
 */
public interface CallDAO<T extends Enum<T> & CallDAO.Attribute> extends DAO<T> {
    
    /**
     * Interface implemented by {@code Enum} classes allowing to select 
     * what are the attributes to populate in the {@code CallTO}s obtained 
     * from a {@code CallDAO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public static interface Attribute extends DAO.Attribute {
        /**
         * @return  A {@code boolean} allowing to determine whether this {@code Attribute} 
         *          is related to a data type, meaning that the method related to 
         *          this {@code Attribute} in an {@code CallTO} returns a {@code DataState}.
         */
        public boolean isDataTypeAttribute();
    }
    /**
     * The attributes available to order retrieved {@code CallTO}s
     * <ul>
     * <li>{@code GENE_ID}: corresponds to {@link CallTO#getGeneId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link CallTO#getStageId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link CallTO#getAnatEntityId()}.
     * <li>{@code OMA_GROUP_ID}: order results by the OMA group genes belong to. 
     * If this {@code OrderingAttribute} is used in a query not specifying any targeted taxon 
     * for gene orthology, then the {@code OMAParentNodeId} of the gene is used (see 
     * {@link org.bgee.model.dao.api.gene.GeneDAO.GeneTO#getOMAParentNodeId()}); otherwise, 
     * the OMA group the gene belongs to at the level of the targeted taxon is used. 
     * <li>{@code MEAN_RANK}: order results by mean rank of the gene in the corresponding condition. 
     * Only the mean ranks computed from the data types requested in the query are considered. 
     * </ul>
     */
    enum OrderingAttribute implements DAO.OrderingAttribute {
        GENE_ID, STAGE_ID, ANAT_ENTITY_ID, OMA_GROUP_ID;
    }

    /**
     * A {@code TransferObject} carrying information about calls present in the Bgee database, 
     * common to all types of calls (expression calls, no-expression calls, differential 
     * expression calls). A call is defined by a triplet 
     * gene/anatomical entity/developmental stage, with associated information about 
     * the data types that allowed to produce it, and with which confidence.
     * <p>
     * For simplicity, a {@code CallTO} can carry the {@link DataState}s associated to 
     * any data type, despite the fact that specific subclasses might not be associated to 
     * all of them (some data types do not allow to produce some types of call). But 
     * in that case, the {@code DataState} of such non-available data types will simply 
     * be {@code NODATA}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     * 
     * @param T The type of {@code Attribute} associated to this {@code CallTO}.
     */
    public static abstract class CallTO<T extends Enum<T> & CallDAO.Attribute> extends EntityTO<Integer> {
        // TODO modify the class to be immutable. Use a Builder pattern?
        private static final long serialVersionUID = 2157139618099008406L;
        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(CallTO.class.getName());

        /**
         * An {@code enum} used to define, for each data type that allowed to generate 
         * a call (Affymetrix, RNA-Seq, ...), its contribution to the generation 
         * of the call.
         * <ul>
         * <li>{@code NODATA}: no data from the associated data type allowed to produce 
         * the call.
         * <li>{@code LOWQUALITY}: some data from the associated data type allowed 
         * to produce the call, but with a low quality.
         * <li>{@code HIGHQUALITY}: some data from the associated data type allowed 
         * to produce the call, and with a high quality.
         * </ul>
         * 
         * @author Frederic Bastian
         * @version Bgee 13
         * @since Bgee 13
         */
        public enum DataState implements EnumDAOField {
            NODATA("no data"), 
            LOWQUALITY("poor quality"), 
            HIGHQUALITY("high quality");
            
            /**
             * Convert the {@code String} representation of a data state (for instance, 
             * retrieved from a database) into a {@code DataState}. Operation performed 
             * by calling {@link TransferObject#convert(Class, String)} with {@code DataState} 
             * as the {@code Class} argument, and {@code representation} 
             * as the {@code String} argument.
             * .
             * 
             * @param representation    A {@code String} representing a data state.
             * @return  A {@code DataState} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code DataState}.
             */
            public static final DataState convertToDataState(String representation) {
                log.entry(representation);
                return log.traceExit(TransferObject.convert(DataState.class, representation));
            }
            
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code DataState}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code DataState}.
             */
            private DataState(String stringRepresentation) {
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
        
        //**************************************
        // ATTRIBUTES
        //**************************************
        /**
         * A {@code String} representing the ID of the gene associated to this call.
         */
        private Integer bgeeGeneId;
        /**
         * An {@code Integer} representing the ID of the condition associated to this call.
         */
        private Integer conditionId;
        
        //-----------DataState for each data type---------------
        /**
         * The {@code DataState} defining the contribution of Affymetrix data 
         * to the generation of this call.
         */
        private DataState affymetrixData;
        /**
         * The {@code DataState} defining the contribution of EST data 
         * to the generation of this call.
         */
        private DataState estData;
        /**
         * The {@code DataState} defining the contribution of <em>in situ</em> data 
         * to the generation of this call.
         */
        private DataState inSituData;
        /**
         * The {@code DataState} defining the contribution of "relaxed" <em>in situ</em> 
         * data to the generation of this call. "Relaxed" <em>in situ</em> data are used 
         * to infer absence of expression, by considering <em>in situ</em> data as complete: 
         * absence of expression of a gene is assumed in any organ existing at 
         * the developmental stage studied by some <em>in situ</em> data, with no report 
         * of expression.
         */
        private DataState relaxedInSituData;
        /**
         * The {@code DataState} defining the contribution of RNA-Seq data 
         * to the generation of this call.
         */
        private DataState rnaSeqData;

        /**
         * Default constructor.
         */
        protected CallTO() {
            this(null, null, null, DataState.NODATA, DataState.NODATA, 
                    DataState.NODATA, DataState.NODATA, DataState.NODATA);
        }
        
        /**
         * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
         * the contribution of Affymetrix, EST, <em>in situ</em>, "relaxed" <em>in situ</em> and, 
         * RNA-Seq data to the generation of this call.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                   An {@code Integer} that is the ID of this call.
         * @param bgeeGeneId           An {@code Integer} that is the ID of the gene associated to 
         *                             this call.
         * @param conditionId          An {@code Integer} that is the ID of the condition
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
         */
        protected CallTO(Integer id, Integer bgeeGeneId, Integer conditionId, 
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData) {
            super(id);
            this.bgeeGeneId = bgeeGeneId;
            this.conditionId = conditionId;
            this.affymetrixData = affymetrixData;
            this.estData = estData;
            this.inSituData = inSituData;
            this.relaxedInSituData = relaxedInSituData;
            this.rnaSeqData = rnaSeqData;
        }
        
        /**
         * Map the {@code Attribute}s of type {@code T} related to data types 
         * to their corresponding {@code DataState} defined in this {@code CallTO}.
         * For instance, if the method {@code getAffymetrixData} returns {@code DataState.HIGHQUALITY}, 
         * then the returned {@code Map} will contain an entry 
         * {@code AFFYMETRIX_DATA} -> {@code DataState.HIGHQUALITY}. Values can be {@code null}.
         * 
         * @return          A {@code Map} where keys are {@code T}s related to data types (see 
         *                  {@link CallDAO.Attribute#isDataTypeAttribute()}), 
         *                  the associated value being the corresponding {@code DataState} 
         *                  defined in this {@code ExpressionCallTO}.
         */
        public abstract Map<T, DataState> extractDataTypesToDataStates();
        

        
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
        protected EnumMap<T, DataState> extractFilteringDataTypes(Class<T> attributeType) {
            log.traceEntry();
            
            final Map<T, DataState> typesToStates = this.extractDataTypesToDataStates();
            
            Set<DataState> states = new HashSet<>(typesToStates.values());
            //if we only have null and/or DataState.NODATA values, 
            //it is equivalent to having no filtering for data types.
            if ((states.size() == 1 && 
                    (states.contains(null) || states.contains(DataState.NODATA))) || 
                (states.size() == 2 && states.contains(null) && 
                states.contains(DataState.NODATA))) {
                
                return log.traceExit(new EnumMap<>(attributeType));
            }
            
            //otherwise, get the data types with a filtering requested
            return log.traceExit(typesToStates.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue() != DataState.NODATA)
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), 
                            (k, v) -> {throw log.throwing(
                                    new IllegalArgumentException("Key used more than once: " + k));}, 
                            //Cannot write EnumMap<>, Eclipse manages to infer the correct type, but not javac. 
                            () -> new EnumMap<T, DataState>(attributeType))));
        }

        //**************************************
        // GETTERS/SETTERS
        //**************************************

        /**
         * @return the {@code Integer} representing the ID of the gene associated to this call.
         */
        public Integer getBgeeGeneId() {
            return bgeeGeneId;
        }
        /**
         * @return  the {@code Integer} representing the ID of the condition 
         *          associated to this call.
         */
        public Integer getConditionId() {
            return conditionId;
        }
        
        //-----------DataState for each data type---------------
        /**
         * @return  the {@code DataState} defining the contribution of Affymetrix data 
         *          to the generation of this call.
         */
        public DataState getAffymetrixData() {
            return affymetrixData;
        }
        /**
         * @param affymetrixData    the {@code DataState} defining the contribution 
         *                          of Affymetrix data to the generation of this call.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setAffymetrixData(DataState affymetrixData) {
            this.affymetrixData = affymetrixData;
        }
        /**
         * @return  the {@code DataState} defining the contribution of EST data 
         *          to the generation of this call.
         */
        public DataState getESTData() {
            return estData;
        }
        /**
         * @param estData   the {@code DataState} defining the contribution 
         *                  of EST data to the generation of this call.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setESTData(DataState estData) {
            this.estData = estData;
        }
        /**
         * @return  the {@code DataState} defining the contribution of <em>in situ</em> data 
         *          to the generation of this call.
         */
        public DataState getInSituData() {
            return inSituData;
        }
        /**
         * @param inSituData    the {@code DataState} defining the contribution 
         *                      of <em>in situ</em> data to the generation of this call.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setInSituData(DataState inSituData) {
            this.inSituData = inSituData;
        }
        /**
         * "Relaxed" <em>in situ</em> data are used to infer absence of expression, 
         * by considering <em>in situ</em> data as complete: absence of expression 
         * of a gene is assumed in any organ existing at the developmental stage 
         * studied by some <em>in situ</em> data, with no report of expression.
         * 
         * @return  the {@code DataState} defining the contribution of relaxed 
         *          <em>in situ</em> data to the generation of this call.
         */
        public DataState getRelaxedInSituData() {
            return relaxedInSituData;
        }
        /**
         * "Relaxed" <em>in situ</em> data are used to infer absence of expression, 
         * by considering <em>in situ</em> data as complete: absence of expression 
         * of a gene is assumed in any organ existing at the developmental stage 
         * studied by some <em>in situ</em> data, with no report of expression.
         * 
         * @param inSituData    the {@code DataState} defining the contribution 
         *                      of relaxed <em>in situ</em> data to the generation 
         *                      of this call.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setRelaxedInSituData(DataState inSituData) {
            this.relaxedInSituData = inSituData;
        }
        /**
         * @return  the {@code DataState} defining the contribution of RNA-Seq data 
         *          to the generation of this call.
         */
        public DataState getRNASeqData() {
            return rnaSeqData;
        }
        /**
         * @param rnaSeqData    the {@code DataState} defining the contribution 
         *                      of RNA-Seq data to the generation of this call.
         */
        //deprecated because all TOs should now be immutable. 
        @Deprecated
        void setRNASeqData(DataState rnaSeqData) {
            this.rnaSeqData = rnaSeqData;
        }


        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Bgee Gene ID: " + this.getBgeeGeneId() + 
                " - Condition ID: " + this.getConditionId() +
                " - Affymetrix data: " + this.getAffymetrixData() +
                " - EST data: " + this.getESTData() +
                " - in situ data: " + this.getInSituData() +
                " - relaxed in situ data: " + this.getRelaxedInSituData() +
                " - RNA-Seq data: " + this.getRNASeqData();
        }
    }
}
