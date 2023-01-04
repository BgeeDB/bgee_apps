package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataWithDataSourceTO;

/**
 * DAO for {@link ESTLibraryTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see ESTLibraryTO
 * @since Bgee 01
 */
public interface ESTLibraryDAO extends DAO<ESTLibraryDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ESTLibraryTO}s
     * obtained from this {@code ESTLibraryDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ESTLibraryTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link ESTLibraryTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link ESTLibraryTO#getDescription()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link ESTLibraryTO#getDataSourceId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link ESTLibraryTO#getConditionId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("estLibraryId"), NAME("estLibraryName"), DESCRIPTION("estLibraryDescription"), 
        DATA_SOURCE_ID("dataSourceId"), CONDITION_ID("conditionId");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTLibraryTO} class.
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
     * Allows to retrieve {@code ESTLibraryTO}s according to the provided filters.
     * <p>
     * The {@code ESTLibraryTO}s are retrieved and returned as a {@code ESTLibraryTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter EST libraries to retrieve. The query uses AND between elements
     *                          of a same filter and uses OR between filters.
     * @param offset            A {@code Long} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row.
     *                          {@code Long} because sometimes the number of potential results
     *                          can be very large.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code ESTLibraryTOResultSet} allowing to retrieve the
     *                          targeted {@code ESTLibraryTOResultSet}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public ESTLibraryTOResultSet getESTLibraries(Collection<DAORawDataFilter> rawDataFilters,
            Long offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code ESTLibraryTO}s
     * 
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public interface ESTLibraryTOResultSet extends DAOResultSet<ESTLibraryTO> {}

    /**
     * {@code TransferObject} for EST libraries.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class ESTLibraryTO extends NamedEntityTO<String>
    implements AssayTO<String>, RawDataAnnotatedTO, RawDataWithDataSourceTO {
        private static final long serialVersionUID = 6500670452213931420L;

        private final Integer dataSourceId;
        private final Integer conditionId;

        public ESTLibraryTO(String id, String name, String description, Integer dataSourceId,
                Integer conditionId) {
            super(id, name, description);
            this.dataSourceId = dataSourceId;
            this.conditionId = conditionId;
        }

        @Override
        public Integer getDataSourceId() {
            return this.dataSourceId;
        }
        @Override
        public Integer getConditionId() {
            return this.conditionId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ESTLibraryTO [id=").append(getId()).append(", name=").append(getName())
                    .append(", description=").append(getDescription()).append(", dataSourceId=").append(dataSourceId)
                    .append(", conditionId=").append(conditionId).append("]");
            return builder.toString();
        }
    }
}