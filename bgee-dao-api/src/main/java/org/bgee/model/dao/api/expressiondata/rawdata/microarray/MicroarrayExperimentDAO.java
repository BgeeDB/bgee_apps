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
	 * Retrieve from a data source a {@code MicroarrayExperimentTO}, corresponding to 
	 * an Affymetrix experiment with the ID {@code expId}, or {@code null} 
	 * if no corresponding experiment could be found.  
	 * 
	 * @param expIds           A {@code Collection} of {code String} representing the IDs of
	 *                         the Affymetrix experiments to retrieve from the data source.
     * @param attributes       A {@code Collection} of {@code Attribute}s to specify the information
     *                         to retrieve from the data source.
	 * @return	               A {@code AffymetrixExpTO}, encapsulating all the data related to
	 *                         the Affymetrix experiment, {@code null} if none could be found.
     * @throws DAOException    If an error occurred when accessing the data source. 
	 */
	public MicroarrayExperimentTOResultSet getExperimentFromIds(Collection<String> expId,
	        Collection<Attribute> attributes) throws DAOException;

	   /**
     * Retrieve from a data source a {@code MicroarrayExperimentTO}, corresponding to
     * an Affymetrix experiment with the ID {@code expId}, or {@code null}
     * if no corresponding experiment could be found.
     * 
     * @param expIds            A {@code Collection} of {code String} representing the IDs of
     *                          the Affymetrix experiments to retrieve from the data source.
     * @param chipIds           A {@code Collection} of {code String} representing the IDs of
     *                          the Affymetrix chips for which Affymetrix Experiments have to 
     *                          be retrieved.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code AffymetrixExpTO}, encapsulating all the data related to
     *                          the Affymetrix experiment, {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public MicroarrayExperimentTOResultSet getExperiments(Collection<String> expId,
            Collection<String> chipIds, DAORawDataFilter rawDataFilter, 
            Collection<Attribute> attributes) throws DAOException;

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
