package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

/**
 * An <code>interface</code> that must be implemented by all <code>DAO</code>s 
 * related to RNA-Seq experiments; 
 * for instance, to retrieve RNA-Seq experiments from a data source, 
 * or to update RNA-Seq experiments into a data source.
 *  
 * The communication between the DAO and the <code>model</code> layer 
 * is achieved through the use of <code>TransferObject</code>s  
 * (in that case, <code>RNASeqExpTO</code>s).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqExpTO
 * @see model.data.sql.mysql.expressionData.rnaSeqData.MysqlRnaSeqExperimentDAO
 * @since Bgee 12
 *
 */
public interface RNASeqExpDAO 
{
	/**
	 * Retrieve from a data source a <code>RNASeqExpTO</code>,  
	 * encapsulating the data related to a RNA-Seq experiment, 
	 * using an experiment ID (<code>rnaSeqExperimentId</code>).  
	 * 
	 * @param rnaSeqExperimentId 		A <code>String</code> representing the ID 
	 * 									of the RNA-Seq experiment that needs to be retrieved 
	 * 									from the data source. 
	 * @return	A <code>RNASeqExpTO</code>, encapsulating all the data 
	 * 			related to the RNA-Seq experiment retrieved from the data source. 
	 */
	public RNASeqExpTO getExperimentById(String rnaSeqExperimentId);
}
