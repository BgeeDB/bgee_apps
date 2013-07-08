package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.TransferObject;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.RawDataAnnotated}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.RawDataAnnotated
 * @since Bgee 11
 */
public class RawDataAnnotatedTO extends TransferObject
{
    public String organId;
    public String stageId;
	
	public RawDataAnnotatedTO()
    {
    	super();
    	
    	this.organId = "";
    	this.stageId = "";
    }
}
