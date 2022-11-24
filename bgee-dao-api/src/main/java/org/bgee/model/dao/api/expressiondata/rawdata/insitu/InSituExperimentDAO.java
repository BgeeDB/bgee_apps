package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;

/**
 * DAO defining queries using or retrieving {@link InSituExperimentTO}s. 
 *
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see InSituExperimentTO
 * @since Bgee 01
 */
public interface InSituExperimentDAO extends DAO<InSituExperimentDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code InSituExperimentTO}s
     * obtained from this {@code InSituExperimentDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link InSituExperimentTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link InSituExperimentTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link InSituExperimentTO#getDescription()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link InSituExperimentTO#getDataSourceId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("inSituExperimentId"), NAME("inSituExperimentName"),
        DESCRIPTION("inSituExperimentDescription"), DATA_SOURCE_ID("dataSourceId");

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
     * Allows to retrieve {@code InSituExperimentTO}s according to the provided filters,
     * ordered by insitu experiment IDs.
     * <p>
     * The {@code InSituExperimentTO}s are retrieved and returned as a
     * {@code InSituExperimentTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDatafilters    A {@code Collection} of {@code DAORawDataFilter} allowing to filter which
     *                          experiment to retrieve. The query uses AND between elements of a same filter and
     *                          uses OR between filters.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code InSituExperimentTOResultSet} allowing to retrieve the targeted
     *                          {@code InSituExperimentTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public InSituExperimentTOResultSet getInSituExperiments(Collection<DAORawDataFilter> rawDatafilters,
            Integer offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

    public interface InSituExperimentTOResultSet extends DAOResultSet<InSituExperimentTO> {}

    /**
     * {@code TransferObject} in situ hybridization experiments.
     *
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class InSituExperimentTO extends ExperimentTO<String> {

        private static final long serialVersionUID = 8572165147198917938L;

        public InSituExperimentTO(String id, String name, String description,
                Integer dataSourceId) {
            super(id, name, description, dataSourceId);
        }
    }
}