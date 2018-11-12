package org.bgee.model.dao.api.expressiondata.rawdata.est;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.DetectionFlag;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
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
     * An {@code EntityTO} representing an EST, as stored in the Bgee database.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class ESTTO extends EntityTO<String> implements CallSourceTO<String> {
        private static final long serialVersionUID = -6130411930176920545L;

        /**
         * A {@code String} that is the ID of the assay this EST is part of.
         */
        private final String estLibraryId;
        /**
         * A {@code String} representing the secondary ID of the EST (ESTs have two IDs in Unigene).
         */
        private final String estId2;
        /**
         * A {@code String} representing the ID of UniGene Cluster associated to this EST.
         */
        private final String uniGeneClusterId;
        /**
         * The {@code CallSourceDataTO} carrying the information about
         * the produced call of presence/absence of expression.
         */
        private final CallSourceDataTO callSourceDataTO;

        public ESTTO(String estId, String estId2, String estLibraryId, String uniGeneClusterId, Integer bgeeGeneId,
                DataState expressionConfidence, Integer expressionId) {
            super(estId);
            this.estId2 = estId2;
            this.uniGeneClusterId = uniGeneClusterId;
            this.estLibraryId = estLibraryId;
            this.callSourceDataTO = new CallSourceDataTO(bgeeGeneId, DetectionFlag.PRESENT,
                    expressionConfidence, ExclusionReason.NOT_EXCLUDED, expressionId);
        }

        @Override
        public String getAssayId() {
            return this.estLibraryId;
        }
        @Override
        public CallSourceDataTO getCallSourceDataTO() {
            return this.callSourceDataTO;
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
            builder.append("ESTTO [id=").append(this.getId()).append(", estId2=").append(estId2)
                    .append(", estLibraryId=").append(estLibraryId)
                    .append(", uniGeneClusterId=").append(uniGeneClusterId)
                    .append(", callSourceDataTO=").append(this.callSourceDataTO).append("]");
            return builder.toString();
        }
    }
}