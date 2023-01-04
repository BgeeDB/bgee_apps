package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;

/**
 * DAO defining queries using or retrieving {@link MicroarrayExperimentTO}s.
 *
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14 Sept. 2018
 * @since Bgee 01
 */
public interface MicroarrayExperimentDAO extends DAO<MicroarrayExperimentDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code MicroarrayExperimentTO}s
     * obtained from this {@code MicroarrayExperimentDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link MicroarrayExperimentTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link MicroarrayExperimentTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link MicroarrayExperimentTO#getDescription()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link MicroarrayExperimentTO#getDataSourceId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("microarrayExperimentId"), NAME("microarrayExperimentName"),
        DESCRIPTION("microarrayExperimentDescription"), DATA_SOURCE_ID("dataSourceId");

        /**
         * A {@code String} that is the corresponding field name in {@code MicroarrayExperimentTO}
         * class.
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
     * Allows to retrieve {@code MicroarrayExperimentTO}s according to the provided filters.
     * <p>
     * The {@code MicroarrayExperimentTO}s are retrieved and returned as a
     * {@code MicroarrayExperimentTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter experiments to retrieve. The query uses AND between
     *                          elements of a same filter and uses OR between filters.
     * @param offset            A {@code Long} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row. If
     *                          not null, a limit should be also provided.
     *                          {@code Long} because sometimes the number of potential results
     *                          can be very large.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code AffymetrixProbesetTOResultSet} allowing to retrieve the
     *                          targeted {@code AffymetrixProbesetTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public MicroarrayExperimentTOResultSet getExperiments(Collection<DAORawDataFilter> rawDataFilters,
            Long offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

	/**
     * {@code DAOResultSet} for {@code MicroarrayExperimentTO}s
     * 
     * @author  Frederic Bastian
     * @version Bgee 14, Sept. 2018
     * @since   Bgee 14, Sept. 2018
     */
    public interface MicroarrayExperimentTOResultSet extends DAOResultSet<MicroarrayExperimentTO> {
    }

	/**
	 * {@code TransferObject} for Affymetrix {@coe ExperimentTO}.
	 * 
	 * @author Frederic Bastian
	 * @author Valentine Rech de Laval
	 * @version Bgee 14 Sept. 2018
	 * @since Bgee 11
	 */
	public final class MicroarrayExperimentTO extends ExperimentTO<String> {
        private static final long serialVersionUID = 5255742948654816580L;

        /**
	     * Default constructor. 
	     */
	    public MicroarrayExperimentTO(String id, String name, String description,
	            Integer dataSourceId) {
	        super(id, name, description, dataSourceId);
	    }
	}
}
