package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

/**
 * An <code>interface</code> that must be implemented by all <code>DAO</code>s 
 * related to microarray experiments; 
 * for instance, to retrieve microarray experiments from a data source, 
 * or to update microarray experiments into a data source.
 * <p>
 * The communication between the DAO and the <code>model</code> layer 
 * is achieved through the use of <code>TransferObject</code>s  
 * (in that case, <code>AffymetrixExpTO</code>s).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixExpTO
 * @see model.data.sql.mysql.expressionData.affymetrixData.MysqlMicroarrayExperimentDAO
 * @since Bgee 01
 *
 */
public interface AffymetrixExpDAO 
{
	/**
	 * Retrieve from a data source a <code>AffymetrixExpTO</code>,  
	 * encapsulating the data related to a microarray experiment, 
	 * using an experiment ID (<code>microarrayExperimentId</code>).  
	 * 
	 * @param microarrayExperimentId 	A <code>String</code> representing the ID 
	 * 									of the microarray experiment that needs to be retrieved 
	 * 									from the data source. 
	 * @return	A <code>AffymetrixExpTO</code>, encapsulating all the data 
	 * 			related to the microarray experiment retrieved from the data source. 
	 */
	public AffymetrixExpTO getExperimentById(String microarrayExperimentId);
}
