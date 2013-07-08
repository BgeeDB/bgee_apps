package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;

import org.bgee.model.dao.api.TransferObject;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixExp}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixExp
 * @since Bgee 11
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
