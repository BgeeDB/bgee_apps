package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;

import org.bgee.model.dao.api.TransferObject;

/**
 * A <code>TransferObject</code> used to communicate 
 * information related to microarray experiments 
 * between the <code>model</code> layer (the business logic layer), 
 * and the <code>model.data</code> layer (the data source layer).
 * It encapsulates the information that could be retrieved from the data source. 
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixExpDAO
 * @since Bgee 01
 *
 */
public class AffymetrixExpTO extends TransferObject implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 17567457L;
	
	/**
	 * A <code>String</code> containing the description of the microarray experiment. 
	 */
	public String description;
	/**
	 * A <code>String</code> representing the ID of the data source 
	 * where this microarray experiment comes from. 
	 */
	public String dataSourceId;
	
	/**
	 * Default constructor. 
	 */
	public AffymetrixExpTO()
	{
		super();
		this.description = "";
		this.dataSourceId = "";
	}

}
