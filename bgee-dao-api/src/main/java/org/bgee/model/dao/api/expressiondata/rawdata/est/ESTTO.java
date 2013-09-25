package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * {@code TransferObject} for the class 
 * {@link org.bgee.model.expressiondata.rawdata.est.EST}.
 * <p>
 * For information on this {@code TransferObject} and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.est.EST
 * @since Bgee 11
 */
public class ESTTO extends CallSourceRawDataTO implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 12343L;

	public ESTTO()
    {
    	super();
    	this.estLibraryId = null;
    }
	
	public String estLibraryId;
}
