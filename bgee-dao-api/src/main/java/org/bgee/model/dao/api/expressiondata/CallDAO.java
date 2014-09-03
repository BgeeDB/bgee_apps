package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
     * <li>{@code DEVSTAGEID: corresponds to {@link CallTO#getDevStageId()}.
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
        // TODO modify the class to be immutable.
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
            NODATA, LOWQUALITY, HIGHQUALITY;
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
        private String devStageId;
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
            super();
            this.id = null;
            this.geneId = null;
            this.anatEntityId = null;
            this.devStageId = null;
            this.affymetrixData = DataState.NODATA;
            this.estData = DataState.NODATA;
            this.inSituData = DataState.NODATA;
            this.relaxedInSituData = DataState.NODATA;
            this.rnaSeqData = DataState.NODATA;
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
         */
        public CallTO(String id, String geneId, String anatEntityId, String devStageId, 
                DataState affymetrixData, DataState estData, DataState inSituData, 
                DataState relaxedInSituData, DataState rnaSeqData) {
            super();
            this.id = id;
            this.geneId = geneId;
            this.anatEntityId = anatEntityId;
            this.devStageId = devStageId;
            this.affymetrixData = affymetrixData;
            this.estData = estData;
            this.inSituData = inSituData;
            this.relaxedInSituData = relaxedInSituData;
            this.rnaSeqData = rnaSeqData;
        }
        
        //**************************************
        // GETTERS/SETTERS
        //**************************************
        /** 
         * Convert a {@code DataState} into a {@code String} corresponding to 
         * the data source quality of the contribution of a data type.
         * 
         * @param dataState An {@code DataState} defining the contribution of a data type.
         * @return          A {@code String} representing the given {@code DataState}. 
         */
        public static String convertDataStateToDataSourceQuality(DataState dataState) {
            log.entry(dataState);
            
            String databaseEnum = null;
            
            if (dataState == DataState.NODATA) {
                databaseEnum = "no data";
            } else if (dataState == DataState.LOWQUALITY) {
                databaseEnum = "poor quality";
            } else if (dataState == DataState.HIGHQUALITY) {
                databaseEnum = "high quality";
            }
            
            return log.exit(databaseEnum);
        }

        /**
         * Convert a data source quality of the contribution of a data type
         * into a {@code DataState}.
         * 
         * @param databaseEnum  A {@code String} that defining the contribution of a data type.
         * @return              An {@code DataState} representing the given {@code String}. 
         */
        public static DataState convertDataSourceQualityToDataState(String databaseEnum) {
            log.entry(databaseEnum);
            
            DataState dataState = null;
            if (databaseEnum.equals("no data")) {
                dataState = DataState.NODATA;
            } else if (databaseEnum.equals("poor quality")) {
                dataState = DataState.LOWQUALITY;
            } else if (databaseEnum.equals("high quality")) {
                dataState = DataState.HIGHQUALITY;
            } 
            
            return log.exit(dataState);
        }

        /**
         * Return the index of the given {@code DataState}.
         * <p>
         * Note that index starting by 1.
         * 
         * @param dataState The {@code DataState} defining the requested minimum contribution 
         *                  to the generation of the calls to be used.
         * @return          An {@code int} that is the index of the given {@code DataState}.
         */
        public static int getMinLevelData(DataState dataState) {
            log.entry(dataState);
            return log.exit(dataState.ordinal() + 1);
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
        public String getDevStageId() {
            return devStageId;
        }
        /**
         * @param devStageId    the {@code String} representing the ID of the 
         *                      developmental stage associated to this call.
         */
        void setDevStageId(String devStageId) {
            this.devStageId = devStageId;
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
        
        /**
         * Create a {@code String} composed with all {@code String}s of a {@code Set} separated 
         * by the given separator.
         * <p>
         * That methods is useful for passing a {@code Set} of {@code String} (for instance, IDs) 
         * to a store procedure that does not accept {@code Collection} or array.
         * 
         * @param set       A {@code Set} of {@code String}s that must be put into a single 
         *                  {@code String}.
         * @param separator A {@code char} that is the separator to use.
         * @return          A {@code String} composed with all {@code String}s of a {@code Set} 
         *                  separated by the given separator. If {@code Set} is null or empty, 
         *                  returns an empty {@code String}.
         */
        public static String createStringFromSet(Set<String> set, char separator) {
            log.entry(set);
            if (set == null || set.size() ==0) {
                return log.exit("");
            }
            StringBuilder myString = new StringBuilder();
            Iterator<String> i = set.iterator();
            boolean isFirst = true;
            while(i.hasNext() ) {
                if (!isFirst && set.size() > 1) {
                    myString.append(separator);
                }
                myString.append(i.next());
                isFirst = false;
            }
            return log.exit(myString.toString());
        }

        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Gene ID: " + this.getGeneId() + 
                " - Developmental stage ID: " + this.getDevStageId() +
                " - Anatomical entity ID: " + this.getAnatEntityId() +
                " - Contribution of Affymetrix data: " + this.getAffymetrixData() +
                " - Contribution of EST data: " + this.getESTData() +
                " - Contribution of <em>in situ</em> data: " + this.getInSituData() +
                " - Contribution of relaxed <em>in situ</em> data: " + this.getRelaxedInSituData() +
                " - Contribution of RNA-Seq data: " + this.getRNASeqData();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            if (id != null) {
                return (prime * result + id.hashCode());
            } else if (geneId != null && devStageId != null && anatEntityId != null) {
                result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
                result = prime * result + ((devStageId == null) ? 0 : devStageId.hashCode());
                result = prime * result + ((anatEntityId == null) ? 0 : anatEntityId.hashCode());
                return result;
            }
            return super.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CallTO other = (CallTO) obj;
            
            if (id != null) {
                if (!id.equals(other.id)) {
                    return false;
                }
                return true;
            } else if (geneId != null && devStageId != null && anatEntityId != null) {
                if (!geneId.equals(other.geneId)) {
                    return false;
                }
                if (!devStageId.equals(other.devStageId)) {
                    return false;
                }
                if (!anatEntityId.equals(other.anatEntityId)) {
                    return false;
                }
                return true;
            }
            return super.equals(obj);
        }
    }
}
