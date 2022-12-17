package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
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
     * Allows to retrieve {@code ESTTO}s according to the provided filters.
     * <p>
     * The {@code ESTTO}s are retrieved and returned as a {@code ESTTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter ESTs to retrieve. The query uses AND between elements
     *                          of a same filter and uses OR between filters.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code ESTTOResultSet} allowing to retrieve the
     *                          targeted {@code ESTTOResultSet}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public ESTTOResultSet getESTs(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

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