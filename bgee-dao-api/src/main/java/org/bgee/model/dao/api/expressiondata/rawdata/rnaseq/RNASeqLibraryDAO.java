package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import org.bgee.model.dao.api.exception.DataAccessException;

/**
 * <code>DAO</code> related to RNA-Seq experiments, using {@link RNASeqLibraryTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see RNASeqLibraryTO
 * @since Bgee 12
 */
public interface RNASeqLibraryDAO 
{
	/**
	 * Retrieve from a data source a <code>RNASeqLibraryTO</code>,  
	 * corresponding to the RNA-Seq library with the ID <code>libraryId</code>, 
	 * <code>null</code> if none could be found.  
	 * 
	 * @param libraryId	 		A <code>String</code> representing the ID 
	 * 							of the RNA-Seq library to retrieve 
	 * 							from the data source. 
	 * @return	A <code>RNASeqLibraryTO</code>, encapsulating all the data 
	 * 			related to the RNA-Seq library retrieved from the data source, 
	 * 			or <code>null</code> if none could be found. 
     * @throws DataAccessException 	If an error occurred when accessing the data source.
	 */
	public RNASeqLibraryTO getRnaSeqLibraryById(String libraryId) throws DataAccessException;
}
