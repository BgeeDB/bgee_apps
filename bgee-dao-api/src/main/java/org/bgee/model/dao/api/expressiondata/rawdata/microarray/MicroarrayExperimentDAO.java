package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;

/**
 * DAO defining queries using or retrieving {@link MicroarrayExperimentTO}s.
 *
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @since Bgee 01
 */
public interface MicroarrayExperimentDAO extends DAO<MicroarrayExperimentDAO.Attribute> {
	/**
	 * Retrieve from a data source a {@code AffymetrixExpTO}, corresponding to 
	 * an Affymetrix experiment with the ID {@code expId}, or {@code null} 
	 * if no corresponding experiment could be found.  
	 * 
	 * @param expId 	A {@code String} representing the ID 
	 * 					of the Affymetrix experiment to retrieve from the data source. 
	 * @return	A {@code AffymetrixExpTO}, encapsulating all the data 
	 * 			related to the Affymetrix experiment, {@code null} if none 
	 * 			could be found.
     * @throws DAOException 	If an error occurred when accessing the data source. 
	 */
	public MicroarrayExperimentTO getExperimentById(String expId) throws DAOException;
	
	/**
	 * {@code TransferObject} for Affymetrix {@coe RawDataExperimentTO}.
	 * 
	 * @author Frederic Bastian
	 * @author Valentine Rech de Laval
	 * @version Bgee 14
	 * @since Bgee 11
	 */
	public final class MicroarrayExperimentTO extends ExperimentTO<String> {
	    private static final long serialVersionUID = 17567457L;
        private final static Logger log = LogManager.getLogger(MicroarrayExperimentTO.class.getName());

	    /**
	     * Default constructor. 
	     */
	    public MicroarrayExperimentTO(String id, String name, String description, Integer dataSourceId) {
	        super(id, name, description, dataSourceId);
	    }
	}
}
