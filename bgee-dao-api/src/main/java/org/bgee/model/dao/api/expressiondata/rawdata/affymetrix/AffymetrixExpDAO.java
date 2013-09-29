package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link AffymetrixExpTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see AffymetrixExpTO
 * @since Bgee 01
 */
public interface AffymetrixExpDAO 
{
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
	public AffymetrixExpTO getExperimentById(String expId) throws DAOException;
}
