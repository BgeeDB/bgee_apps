package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import org.bgee.model.dao.api.exception.DataAccessException;

/**
 * <code>DAO</code> related to RNA-Seq experiments, using {@link RNASeqExpTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see RNASeqExpTO
 * @since Bgee 12
 */
public interface RNASeqExpDAO 
{
	/**
	 * Retrieve from the data source a <code>RNASeqExpTO</code>,  
	 * corresponding to the RNA-Seq experiment with the ID <code>expId</code>, 
	 * <code>null</code> if none could be found.  
	 * 
	 * @param expId 		A <code>String</code> representing the ID 
	 * 						of the RNA-Seq experiment to retrieved 
	 * 						from the data source. 
	 * @return	A <code>RNASeqExpTO</code>, encapsulating all the data 
	 * 			related to the RNA-Seq experiment retrieved from the data source, 
	 * 			or <code>null</code> if none could be found. 
     * @throws DataAccessException 	If an error occurred when accessing the data source.
	 */
	public RNASeqExpTO getExperimentById(String expId) throws DataAccessException;
}
