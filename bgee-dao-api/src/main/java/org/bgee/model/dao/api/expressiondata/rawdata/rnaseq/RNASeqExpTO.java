package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.io.Serializable;

import model.data.common.TransferObject;

/**
 * A <code>TransferObject</code> used to communicate 
 * information related to RNA-Seq experiments 
 * between the <code>model</code> layer (the business logic layer), 
 * and the <code>model.data</code> layer (the data source layer).
 * It encapsulates the information that could be retrieved from the data source. 
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqExpDAO
 * @since Bgee 12
 *
 */
public class RNASeqExpTO extends TransferObject implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 17567458L;
	
	/**
	 * A <code>String</code> containing the description of the RNA-Seq experiment. 
	 */
	public String description;
	/**
	 * A <code>String</code> representing the ID of the data source 
	 * where this RNA-Seq experiment comes from. 
	 */
	public String dataSourceId;
	
	/**
	 * Default constructor. 
	 */
	public RNASeqExpTO()
	{
		super();
		this.description = "";
		this.dataSourceId = "";
	}

}