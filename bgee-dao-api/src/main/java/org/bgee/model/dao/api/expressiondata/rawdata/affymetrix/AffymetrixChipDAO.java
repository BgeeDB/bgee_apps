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
	 * Retrieve from a data source a <code>AffymetrixChipTO</code>, corresponding to 
	 * the Affymetrix chip, with the Bgee chip ID <code>bgeeAffymetrixChipId</code>, 
	 * <code>null</code> if no corresponding chip was found.  
	 * 
	 * @param bgeeAffymetrixChipId	 	A <code>String</code> representing the ID 
	 * 									in the Bgee database of the Affymetrix chip 
	 * 									that needs to be retrieved from the data source. 
	 * @return	An <code>AffymetrixChipTO</code>, encapsulating all the data 
	 * 			related to the Affymetrix chip, <code>null</code> if none could be found. 
     * @throws DataAccessException 	If an error occurred when accessing the data source.
	 */
	public AffymetrixChipTO getAffymetrixChipById(String bgeeAffymetrixChipId) 
			throws DataAccessException;
}
