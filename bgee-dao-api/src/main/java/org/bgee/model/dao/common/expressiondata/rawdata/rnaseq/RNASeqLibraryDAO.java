package org.bgee.model.dao.common.expressiondata.rawdata.rnaseq;

/**
 * An <code>interface</code> that must be implemented by all <code>DAO</code>s 
 * related to RNA-Seq libraries; 
 * for instance, to retrieve RNA-Seq libraries from a data source, 
 * or to update RNA-Seq libraries into a data source.
 *  
 * The communication between the DAO and the <code>model</code> layer 
 * is achieved through the use of <code>TransferObject</code>s  
 * (in that case, <code>RNASeqLibraryTO</code>s).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqLibraryTO
 * @see model.data.sql.mysql.expressionData.rnaSeqData.MysqlRnaSeqLibraryDAO
 * @since Bgee 12
 *
 */
public interface RNASeqLibraryDAO 
{
	/**
	 * Retrieve from a data source a <code>RNASeqLibraryTO</code>,  
	 * encapsulating the data related to a RNA-Seq library, 
	 * using a library ID (<code>rnaSeqLibraryId</code>).  
	 * 
	 * @param rnaSeqLibraryId	 		A <code>String</code> representing the ID 
	 * 									of the RNA-Seq library that needs to be retrieved 
	 * 									from the data source. 
	 * @return	A <code>RNASeqLibraryTO</code>, encapsulating all the data 
	 * 			related to the RNA-Seq library retrieved from the data source. 
	 */
	public RNASeqLibraryTO getRnaSeqLibraryById(String rnaSeqLibraryId);
}
