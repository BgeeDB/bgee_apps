package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import org.bgee.model.dao.api.exception.DataAccessException;

/**
 * DAO defining queries using or retrieving {@link AffymetrixChipTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see AffymetrixChipTO
 * @since Bgee 01
 */
public interface AffymetrixChipDAO 
{
	/**
	 * Retrieve from a data source a {@code AffymetrixChipTO}, corresponding to 
	 * the Affymetrix chip, with the Bgee chip ID {@code bgeeAffymetrixChipId}, 
	 * {@code null} if no corresponding chip was found.  
	 * 
	 * @param bgeeAffymetrixChipId	 	A {@code String} representing the ID 
	 * 									in the Bgee database of the Affymetrix chip 
	 * 									that needs to be retrieved from the data source. 
	 * @return	An {@code AffymetrixChipTO}, encapsulating all the data 
	 * 			related to the Affymetrix chip, {@code null} if none could be found. 
     * @throws DataAccessException 	If an error occurred when accessing the data source.
	 */
	public AffymetrixChipTO getAffymetrixChipById(String bgeeAffymetrixChipId) 
			throws DataAccessException;
}
