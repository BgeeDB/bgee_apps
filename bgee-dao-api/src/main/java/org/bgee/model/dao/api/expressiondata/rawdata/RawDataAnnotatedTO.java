package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.TransferObject;

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
