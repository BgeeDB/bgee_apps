package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.util.Collection;

import org.bgee.model.dao.api.exception.DataAccessException;

/**
 * DAO defining queries using or retrieving {@link AffymetrixProbesetTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see AffymetrixProbesetTO
 * @since Bgee 01
 */
public interface AffymetrixProbesetDAO 
{

	/**
	 * Return a <code>Collection</code> of <code>String</code>s  
	 * corresponding to Affymetrix probeset Ids, 
	 * subset of those passed as a parameter (<code>probesetIds</code>), 
	 * that were not found in the data source.
	 * @param probesetIds	a <code>Collection</code> of <code>String</code>s 
	 * 						to be checked for presence in the data source.
	 * @return 				a <code>Collection</code> of <code>String</code>s that 
	 * 						could not be found in the list of probeset IDs 
	 * 						in the data source. An empty <code>Collection</code> 
	 * 						if all IDs were found in the database, 
	 * 						or if <code>probesetIds</code> was empty.
     * @throws DataAccessException 	If an error occurred when accessing the data source.
	 */
	public Collection<String> getNonMatchingProbesetIds(Collection<String> probesetIds) 
	    throws DataAccessException;
}
