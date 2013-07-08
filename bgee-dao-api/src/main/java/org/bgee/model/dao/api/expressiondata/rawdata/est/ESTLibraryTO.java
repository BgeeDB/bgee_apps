package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.est.ESTLibrary}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.est.ESTLibrary
 * @since Bgee 11
 */
public class ESTLibraryTO extends RawDataAnnotatedTO implements Serializable
{

	private static final long serialVersionUID = 42352L;
	
	public ESTLibraryTO()
	{
		super();
		this.description = "";
		this.dataSourceId = "";
	}
	
	public String description;
	public String dataSourceId;

}
