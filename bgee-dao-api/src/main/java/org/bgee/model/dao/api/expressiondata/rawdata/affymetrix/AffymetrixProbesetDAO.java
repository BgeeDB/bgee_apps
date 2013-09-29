package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.util.Collection;

import org.bgee.model.dao.api.exception.DAOException;

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
	 * Return a {@code Collection} of {@code String}s  
	 * corresponding to Affymetrix probeset Ids, 
	 * subset of those passed as a parameter ({@code probesetIds}), 
	 * that were not found in the data source.
	 * @param probesetIds	a {@code Collection} of {@code String}s 
	 * 						to be checked for presence in the data source.
	 * @return 				a {@code Collection} of {@code String}s that 
	 * 						could not be found in the list of probeset IDs 
	 * 						in the data source. An empty {@code Collection} 
	 * 						if all IDs were found in the database, 
	 * 						or if {@code probesetIds} was empty.
     * @throws DAOException 	If an error occurred when accessing the data source.
	 */
	public Collection<String> getNonMatchingProbesetIds(Collection<String> probesetIds) 
	    throws DAOException;
}
