package org.bgee.model.dao.api.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries using or retrieving {@link CallTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see CallTO
 * @since Bgee 13
 */
public interface CallDAO extends DAO<CallDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code CallTO}s 
     * obtained from this {@code CallDAO}.
     * <ul>
     * <li>{@code ID: corresponds to {@link CallTO#getId()}.
     * <li>{@code GENEID: corresponds to {@link CallTO#getGeneId()}.
     * <li>{@code STAGEID: corresponds to {@link CallTO#getDevStageId()}.
     * <li>{@code ANATENTITYID: corresponds to {@link CallTO#getAnatEntityId()}.
     * <li>{@code AFFYMETRIXDATA: corresponds to {@link CallTO#getAffymetrixData()}.
     * <li>{@code ESTDATA: corresponds to {@link CallTO#getESTData()}.
     * <li>{@code INSITUDATA: corresponds to {@link CallTO#getInSituData()}.
     * <li>{@code RELAXEDINSITUDATA: corresponds to {@link CallTO#getRelaxedInSituData()}.
     * <li>{@code RNASEQDATA;: corresponds to {@link CallTO#getRNASeqData()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENEID, DEVSTAGEID, ANATENTITYID, 
        AFFYMETRIXDATA, ESTDATA, INSITUDATA, RELAXEDINSITUDATA, RNASEQDATA;
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
     */
    public abstract class CallTO implements TransferObject {
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
        public enum DataState {
            NODATA("no data"), 
            LOWQUALITY("poor quality"), 
            HIGHQUALITY("high quality");
            
            /**
             * Convert the {@code String} representation of a data state (for instance, 
             * retrieved from a database) into a {@code DataState}. This method 
             * compares {@code representation} to the value returned by 
             * {@link #getStringRepresentation()}, as well as to the value 
             * returned by {@link Enum#name()}, for each {@code DataState}, 
             * .
             * 
             * @param representation    A {@code String} representing a data state.
             * @return  A {@code DataState} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code DataState}.
             */
            public static final DataState convertToDataState(String representation) {
                log.entry(representation);
                
                for (DataState dataState: DataState.values()) {
                    if (dataState.getStringRepresentation().equals(representation) || 
                            dataState.name().equals(representation)) {
                        return log.exit(dataState);
                    }
                }
                throw log.throwing(new IllegalArgumentException("\"" + representation + 
                        "\" does not correspond to any DataState"));
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
            
            /**
             * @return  A {@code String} that is the representation 
             *          for this {@code DataState}, for instance to be used in a database.
             */
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
        //-----------core attributes: id/gene/anat entity/dev stage---------------
        /**
         * A {@code String} representing the ID of this call.
         */
        private String id;
        /**
         * A {@code String} representing the ID of the gene associated to this call.
         */
        private String geneId;
        /**
         * A {@code String} representing the ID of the developmental stage associated to 
         * this call.
         */
        private String stageId;
        /**
         * A {@code String} representing the ID of the anatomical entity associated to 
         * this call.
         */
        private String anatEntityId;
        
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
            this(null, null, null, null, DataState.NODATA, DataState.NODATA, 
                    DataState.NODATA, DataState.NODATA, DataState.NODATA);
        }
        
        /**
         * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
         * the contribution of Affymetrix, EST, <em>in situ</em>, "relaxed" <em>in situ</em> and, 
         * RNA-Seq data to the generation of this call.
         * 
         * @param id                   A {@code String} that is the ID of this call.
         * @param geneId               A {@code String} that is the ID of the gene associated to 
         *                             this call.
         * @param anatEntityId         A {@code String} that is the ID of the anatomical entity
         *                             associated to this call. 
         * @param stageId           A {@code String} that is the ID of the developmental stage 
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
        protected CallTO(String id, String geneId, String anatEntityId, String devStageId, 
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData) {
            super();
            this.id = id;
            this.geneId = geneId;
            this.anatEntityId = anatEntityId;
            this.stageId = devStageId;
            this.affymetrixData = affymetrixData;
            this.estData = estData;
            this.inSituData = inSituData;
            this.relaxedInSituData = relaxedInSituData;
            this.rnaSeqData = rnaSeqData;
        }

        //**************************************
        // GETTERS/SETTERS
        //**************************************
        //-----------core attributes: id/gene/anat entity/dev stage---------------
        /**
         * @return the {@code String} representing the ID of this call.
         */
        public String getId() {
            return id;
        }
        /**
         * @param id    the {@code String} representing the ID of this call.
         */
        void setId(String id) {
            this.id = id;
        }

        /**
         * @return the {@code String} representing the ID of the gene associated to this call.
         */
        public String getGeneId() {
            return geneId;
        }
        /**
         * @param geneId    the {@code String} representing the ID of the gene associated to 
         *                  this call.
         */
        void setGeneId(String geneId) {
            this.geneId = geneId;
        }
        /**
         * @return  the {@code String} representing the ID of the developmental stage 
         *          associated to this call.
         */
        public String getStageId() {
            return stageId;
        }
        /**
         * @param stageId    the {@code String} representing the ID of the 
         *                      developmental stage associated to this call.
         */
        void setStageId(String devStageId) {
            this.stageId = devStageId;
        }
        /**
         * @return  the {@code String} representing the ID of the anatomical entity 
         *          associated to this call.
         */
        public String getAnatEntityId() {
            return anatEntityId;
        }
        /**
         * @param anatEntityId  the {@code String} representing the ID of the 
         *                      anatomical entity associated to this call.
         */
        void setAnatEntityId(String anatEntityId) {
            this.anatEntityId = anatEntityId;
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
        void setRNASeqData(DataState rnaSeqData) {
            this.rnaSeqData = rnaSeqData;
        }
        
        
        //**************************************
        // Object methods overridden
        //**************************************
        
        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Gene ID: " + this.getGeneId() + 
                " - Stage ID: " + this.getStageId() +
                " - Anatomical entity ID: " + this.getAnatEntityId() +
                " - Affymetrix data: " + this.getAffymetrixData() +
                " - EST data: " + this.getESTData() +
                " - in situ data: " + this.getInSituData() +
                " - relaxed in situ data: " + this.getRelaxedInSituData() +
                " - RNA-Seq data: " + this.getRNASeqData();
        }

        /**
         * Implementation of hashCode specific to {@code CallTO}s: 
         * <ul>
         * <li>if {@link #getId()} returned a non-null value, the hashCode 
         * will be based solely on it. 
         * <li>Otherwise, if the attributes used as a unique key are all non-null 
         * ({@link #getGeneId()}, {@link #getAnatEntityId()}, {@link #getStageId()}), 
         * the hashCode will be base solely on them. This is because these fields 
         * allow to uniquely identified a call. This is useful when aggregating 
         * data from different types, to determine all {@link DataState}s for a given 
         * call (a gene, with data in an organ, during a developmental stage)
         * <li>Otherwise, hashCode will be based on all attributes of this class.
         * </ul>
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            
            //if id is not null, we will base the hashCode solely on it.
            //Otherwise, if geneId, stageId, anatEntityId are all not null, 
            //we will base the hashCode solely on them.
            //Otherwise, all fields will be considered to generate the hashCode.
            
            if (id != null || this.useOtherAttributesForHashCodeEquals()) {
                result = prime * result + ((id == null) ? 0 : id.hashCode());
            } 
            if (id == null) {
                result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
                result = prime * result + ((stageId == null) ? 0 : stageId.hashCode());
                result = prime * result + ((anatEntityId == null) ? 0 : anatEntityId.hashCode());
                
                if (this.useOtherAttributesForHashCodeEquals()) {
                    result = prime * result + 
                            ((affymetrixData == null) ? 0 : affymetrixData.hashCode());
                    result = prime * result
                            + ((estData == null) ? 0 : estData.hashCode());
                    result = prime * result
                            + ((inSituData == null) ? 0 : inSituData.hashCode());
                    result = prime * result
                            + ((relaxedInSituData == null) ? 0 : relaxedInSituData.hashCode());
                    result = prime * result
                            + ((rnaSeqData == null) ? 0 : rnaSeqData.hashCode());
                }
            }
            return result;
        }

        /**
         * Implementation of equals specific to {@code CallTO}s and consistent with 
         * the {@link #hashCode()} implementation. See {@link #hashCode()} for more details.
         * @see #hashCode()
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CallTO)) {
                return false;
            }
            CallTO other = (CallTO) obj;
            
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else {
                //if id is not null, we will base the equals solely on it
                return id.equals(other.id);
            }
            
            if (geneId == null) {
                if (other.geneId != null) {
                    return false;
                }
            } else if (!geneId.equals(other.geneId)) {
                return false;
            }
            if (anatEntityId == null) {
                if (other.anatEntityId != null) {
                    return false;
                }
            } else if (!anatEntityId.equals(other.anatEntityId)) {
                return false;
            }
            if (stageId == null) {
                if (other.stageId != null) {
                    return false;
                }
            } else if (!stageId.equals(other.stageId)) {
                return false;
            }
            //if geneId, stageId, anatEntityId are all not null, 
            //we will base the equals solely on them.
            if (!this.useOtherAttributesForHashCodeEquals()) {
                return true;
            }
            
            //otherwise, we will base the equals on all attributes.
            if (affymetrixData != other.affymetrixData) {
                return false;
            }
            if (estData != other.estData) {
                return false;
            }
            if (inSituData != other.inSituData) {
                return false;
            }
            if (relaxedInSituData != other.relaxedInSituData) {
                return false;
            }
            if (rnaSeqData != other.rnaSeqData) {
                return false;
            }
            
            return true;
        }
        
        /**
         * Determines whether all attributes should be used in {@code hashCode} and 
         * {@code equals}  methods, see {@link #hashCode()} for details. 
         * @return  {@code true} if all attributes should be used in {@code hashCode} and 
         *          {@code equals} method, false otherwise.
         */
        protected boolean useOtherAttributesForHashCodeEquals() {
            if (id != null) {
                return false;
            }
            if (geneId != null && anatEntityId != null && stageId != null) {
                return false;
            }
            return true;
        }
    }
}
