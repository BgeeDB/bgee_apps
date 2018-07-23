package org.bgee.model.dao.api.expressiondata.rawdata.est;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;

/**
 * DAO defining queries using or retrieving {@link ESTTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @since Bgee 01
 */
public interface ESTDAO extends DAO<ESTDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code ESTTO}s obtained from
     * this {@code ESTDAO}.
     * <ul>
     * <li>{@code EST_ID}: corresponds to {@link ESTTO#getId()}.
     * <li>{@code EST_ID2}: corresponds to {@link ESTTO#getEstId2()}.
     * <li>{@code EST_LIBRARY_ID}: corresponds to {@link ESTTO#getAssayId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link ESTTO#getBgeeGeneId()}.
     * <li>{@code UNIGENE_CLUSTER_ID}: corresponds to {@link ESTTO#getUniGeneClusterId()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link ESTTO#getExpressionId()}.
     * <li>{@code EST_DATA}: corresponds to {@link ESTTO#getExpressionConfidence()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        EST_ID, EST_ID2, EST_LIBRARY_ID, BGEE_GENE_ID, UNIGENE_CLUSTER_ID, EXPRESSION_ID, EST_DATA;
    }

    /**
     * A {@code TransferObject} representing an EST, as stored in the Bgee database.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class ESTTO extends CallSourceTO<String> {
        private static final long serialVersionUID = -6130411930176920545L;

        /**
         * A {@code String} that is the primary ID of this EST.
         */
        private final String id;
        /**
         * A {@code String} representing the secondary ID of the EST (ESTs have two IDs in Unigene).
         */
        public final String estId2;
        /**
         * A {@code String} representing the ID of UniGene Cluster associated to this EST.
         */
        public final String uniGeneClusterId;

        public ESTTO(String estId, String estId2, String estLibraryId, String uniGeneClusterId, Integer bgeeGeneId,
                DataState expressionConfidence, Integer expressionId) {
            super(estLibraryId, bgeeGeneId, DetectionFlag.PRESENT, expressionConfidence, ExclusionReason.NOT_EXCLUDED, expressionId);
            this.id = estId;
            this.estId2 = estId2;
            this.uniGeneClusterId = uniGeneClusterId;
        }

        /**
         * @return the {@code String} representing the main ID of the EST (ESTs have two IDs in Unigene).
         */
        public String getId() {
            return this.id;
        }
        /**
         * @return the {@code String} representing the secondary ID of the EST (ESTs have two IDs in Unigene).
         */
        public String getEstId2() {
            return this.estId2;
        }
        /**
         * @return the {@code String} representing the ID of UniGene Cluster associated to this EST.
         */
        public String getUniGeneClusterId() {
            return this.uniGeneClusterId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ESTTO [id=").append(id).append(", estId2=").append(estId2)
                    .append(", estLibraryId=").append(getAssayId())
                    .append(", bgeeGeneId=").append(getBgeeGeneId())
                    .append(", uniGeneClusterId=").append(uniGeneClusterId)
                    .append(", expressionConfidence=").append(getExpressionConfidence())
                    .append(", expressionId=").append(getExpressionId()).append("]");
            return builder.toString();
        }
    }
}