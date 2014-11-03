package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.io.Serializable;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO;

/**
 * DAO defining queries using or retrieving {@link ESTTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 01
 */
public interface ESTDAO extends DAO<ESTDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code ESTTO}s obtained from
     * this {@code ESTDAO}.
     * <ul>
     * <li>{@code ESTID}: corresponds to {@link ESTTO#getId()}.
     * <li>{@code ESTID2}: corresponds to {@link ESTTO#getESTId2()}.
     * <li>{@code ESTLIBRARYID}: corresponds to {@link ESTTO#getESTLibraryId()}.
     * <li>{@code GENEID}: corresponds to {@link ESTTO#getGeneId()}.
     * <li>{@code UNIGENECLUSTERID}: corresponds to {@link ESTTO#getUniGeneClusterId()}.
     * <li>{@code EXPRESSIONID}: corresponds to {@link ESTTO#getExpressionId()}.
     * <li>{@code ESTDATA}: corresponds to {@link ESTTO#getExpressionConfidence()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ESTID, ESTID2, ESTLIBRARYID, GENEID, UNIGENECLUSTERID, EXPRESSIONID, ESTDATA;
    }

    /**
     * A {@code TransferObject} representing an EST, as stored in the Bgee database.
     * <p>
     * For information on this {@code TransferObject} and its fields, see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO
     * @since Bgee 11
     */
    /*
     * (non-javadoc)
     * This TO is not in it's final version. We need to known if CallSourceRawDataTO is necessary 
     * and consistent. Need to be thinking.
     */
    public final class ESTTO extends CallSourceRawDataTO implements Serializable {

        private static final long serialVersionUID = 12343L;

        /**
         * A {@code String} representing the second ID of the EST (ESTs have two IDs in Unigene).
         */
        public final String estId2;

        /**
         * A {@code String} representing the ID of the EST Library associated to this EST.
         */
        public final String estLibraryId;

        /**
         * A {@code String} representing the ID of UniGene Cluster associated to this EST.
         */
        public final String UniGeneClusterId;

        /**
         * Constructor providing the IDs (ESTs have two IDs in Unigene), the ID of the EST Library,
         * the gene ID, the ID of UniGene Cluster, the ID of the expression, and the expression 
         * confidence of this EST.
         * 
         * @param estId             A {@code String} that is the ID of this EST.
         * @param estId2            A {@code String} that is the second ID of this EST.
         * @param estLibraryId      A {@code String} that is the ID of the EST Library associated 
         *                          to this EST.
         * @param geneId            A {@code String} that is the ID of the gene associated to 
         *                          this EST.
         * @param UniGeneClusterId  A {@code String} representing the ID of UniGene Cluster
         *                          associated to this EST.
         * @param expressionId      A {@code String} that is the ID of the expression associated
         *                          to this EST.
         * @param estData           A {@code DataState} that is the expression confidence 
         *                          of this EST.
         */
        public ESTTO(String estId, String estId2, String estLibraryId, String geneId, 
                String UniGeneClusterId, String expressionId, DataState estData) {
            super(estId, geneId, DetectionFlag.UNDEFINED, expressionId, null, estData, 
                    ExclusionReason.NOTEXCLUDED);
            this.estId2 = estId2;
            this.estLibraryId = estLibraryId;
            this.UniGeneClusterId = UniGeneClusterId;
        }

        /**
         * @return the {@code String} representing the second ID of the EST (ESTs have two IDs in 
         * Unigene).
         */
        public String getEstId2() {
            return this.estId2;
        }

        /**
         * @return the {@code String} representing the ID of the EST Library associated to this EST.
         */
        public String getEstLibraryId() {
            return this.estLibraryId;
        }

        /**
         * @return the {@code String} representing the ID of UniGene Cluster associated to this EST.
         */
        public String getUniGeneClusterId() {
            return this.UniGeneClusterId;
        }
    }
}
