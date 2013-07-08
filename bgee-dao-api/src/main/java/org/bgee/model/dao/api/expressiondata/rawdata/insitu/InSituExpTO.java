package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.io.Serializable;

import org.bgee.model.dao.api.TransferObject;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.insitu.InSituExp}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.insitu.InSituExp
 * @since Bgee 11
 */
public class InSituExpTO extends TransferObject implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 656756L;
	
	public String description;

	public String dataSourceId;
	
	public InSituExpTO()
	{
		super();
		this.description = "";
		this.dataSourceId = "";
	}

}
