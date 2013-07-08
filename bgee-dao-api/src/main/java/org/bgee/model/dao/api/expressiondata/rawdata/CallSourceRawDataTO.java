package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.TransferObject;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.CallSourceRawData}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.CallSourceRawData
 * @since Bgee 11
 */
public class CallSourceRawDataTO extends TransferObject
{
    public String geneId;
    public String expressionId;
    public String expressionConfidence;
	
	public CallSourceRawDataTO()
    {
    	super();
    	
    	this.geneId = "";
    	this.expressionId = "";
    	this.expressionConfidence = "";
    }
}
