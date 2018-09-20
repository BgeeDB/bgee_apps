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
        ID, NAME, DESCRIPTION, DATA_SOURCE_ID;
    }

	/**
	 * Retrieve from a data source a {@code MicroarrayExperimentTO}, corresponding to 
	 * an Affymetrix experiment with the ID {@code expId}, or {@code null} 
	 * if no corresponding experiment could be found.  
	 * 
	 * @param expId            A {@code String} representing the ID of the Affymetrix experiment to retrieve
	 *                         from the data source.
     * @param attributes       A {@code Collection} of {@code Attribute}s to specify the information to retrieve
     *                         from the data source.
	 * @return	               A {@code AffymetrixExpTO}, encapsulating all the data related to
	 *                         the Affymetrix experiment, {@code null} if none could be found.
     * @throws DAOException    If an error occurred when accessing the data source. 
	 */
	public MicroarrayExperimentTO getExperimentById(String expId, Collection<Attribute> attributes) throws DAOException;

	/**
	 * Allows to retrieve {@code MicroarrayExperimentTO}s according to the provided filters,
	 * ordered by microarray experiment ID.
     * <p>
     * The {@code MicroarrayExperimentTO}s are retrieved and returned as a {@code MicroarrayExperimentTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results 
     * are retrieved.
	 *
	 * @param filters          A {@code Collection} of {@code DAORawDataFilter}s allowing to specify
	 *                         which experiments to retrieve.
	 * @param attributes       A {@code Collection} of {@code Attribute}s to specify the information to retrieve
	 *                         from the data source.
	 * @return                 A {@code MicroarrayExperimentTOResultSet} allowing to retrieve the targeted
	 *                         {@code MicroarrayExperimentTO}s.
	 * @throws DAOException    If an error occurred while accessing the data source.
	 */
    public MicroarrayExperimentTOResultSet getExperiments(Collection<DAORawDataFilter> filters,
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
	    public MicroarrayExperimentTO(String id, String name, String description, Integer dataSourceId) {
	        super(id, name, description, dataSourceId);
	    }
	}
}
