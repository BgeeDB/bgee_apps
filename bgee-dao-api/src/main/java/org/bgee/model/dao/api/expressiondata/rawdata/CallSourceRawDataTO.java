package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.TransferObject;

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
