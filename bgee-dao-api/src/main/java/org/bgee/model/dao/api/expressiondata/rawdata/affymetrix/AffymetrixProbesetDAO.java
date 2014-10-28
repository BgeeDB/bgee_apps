package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link AffymetrixProbesetTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see AffymetrixProbesetTO
 * @since Bgee 01
 */
//TODO: extends DAO<AffymetrixProbesetDAO.Attribute> 
public interface AffymetrixProbesetDAO {

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
	
	/**
	 * Remove link between some Affymetrix probesets and their associated no-expression 
	 * call because of no-expression conflicts. The probesets will not be deleted, 
	 * but their association to the specified no-expression calls will be. A reason 
	 * for exclusion should be provided in the data source, such as 'noExpression conflict'.
	 * 
	 * @param noExprIds    A {@code Set} of {@code String}s that are the IDs of 
	 *                     the no-expression calls in conflict, whose association to 
	 *                     probesets should be removed. 
	 * @return             An {@code int} that is the number of probesets that were actually 
	 *                     updated as a result of the call to this method. 
	 * @throws IllegalArgumentException    If a no-expression call ID was not associated 
	 *                                     to any probeset. 
	 * @throws DAOException                If an error occurred while updating the data. 
	 */
	public int updateNoExpressionConflicts(Set<String> noExprIds) 
	        throws DAOException, IllegalArgumentException;
}
