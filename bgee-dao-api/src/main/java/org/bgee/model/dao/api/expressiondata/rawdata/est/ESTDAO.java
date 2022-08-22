package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
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
     * <li>{@code PVALUE}: corresponds to {@link ESTTO#getPValue()}.
     * <li>{@code EST_DATA}: corresponds to {@link ESTTO#getExpressionConfidence()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        EST_ID("estId"), EST_ID2("estId2"), EST_LIBRARY_ID("estLibraryId"),
        BGEE_GENE_ID("bgeeGeneId"), UNIGENE_CLUSTER_ID("UniGeneClusterId"),
        EXPRESSION_ID("expressionId"), PVALUE("pValue"), EST_DATA("estData");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;

        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }

    /**
     * retrieve ESTs filtered on EST library IDs.
     * 
     * @param estLibraryIds     A {@code Collection} of {@code String} that corresponds
     *                          to EST libraries for which ESTs have to be retrieved.
     * @param attrs             A {@code Collection} of {@code ESTDAO.Attribute}s defining the
     *                          attributes to populate in the returned {@code ESTTO}s.
     *                          If {@code null} or empty, all attributes are populated.
     * 
     * @return                  A {@code ESTTOResultSet} containing ESTs
     */
    public ESTTOResultSet getESTByLibraryIds(Collection<String> estLibraryIds,
            Collection<ESTDAO.Attribute> attrs);

    /**
     * retrieve ESTs filtered on bgee gene IDs, speciesIds and conditions
     * 
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * @param attrs             A {@code Collection} of {@code ESTDAO.Attribute}s defining the
     *                          attributes to populate in the returned {@code ESTTO}s.
     *                          If {@code null} or empty, all attributes are populated.
     * 
     * @return                  A {@code ESTTOResultSet} containing ESTs
     */
    public ESTTOResultSet getESTByRawDataFilter(DAORawDataFilter rawDataFilter,
            Collection<ESTDAO.Attribute> attrs);

    /**
     * retrieve ESTs filtered on species IDs, bgee gene IDs, conditions, EST library IDs and 
     * est IDs
     * 
     * @param estLibraryIds     A {@code Collection} of {@code String} that corresponds
     *                          to EST libraries for which ESTs have to be retrieved.
     * @param estIds            A {@code Collection} of {@code String} that corresponds
     *                          to EST IDs for which ESTs have to be retrieved. ESTs have
     *                          two IDs in the database. This filtering can be on any of
     *                          them.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * @param attrs             A {@code Collection} of {@code ESTDAO.Attribute}s defining the
     *                          attributes to populate in the returned {@code ESTTO}s.
     *                          If {@code null} or empty, all attributes are populated. 
     * 
     * @return                  A {@code ESTTOResultSet} containing ESTs
     */
    public ESTTOResultSet getESTs(Collection<String> estLibraryIds, Collection<String> estIds,
            DAORawDataFilter rawDataFilter, Collection<ESTDAO.Attribute> attrs);

    public interface ESTTOResultSet extends DAOResultSet<ESTTO> {}

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
                DataState expressionConfidence, BigDecimal pValue, Long expressionId) {
            super(estId);
            this.estId2 = estId2;
            this.uniGeneClusterId = uniGeneClusterId;
            this.estLibraryId = estLibraryId;
            this.callSourceDataTO = new CallSourceDataTO(bgeeGeneId, pValue,
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