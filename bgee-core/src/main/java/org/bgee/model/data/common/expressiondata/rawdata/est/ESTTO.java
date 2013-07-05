package org.bgee.model.data.common.expressiondata.rawdata.est;

import java.io.Serializable;

import model.data.common.expressionData.ExpressionDataLinkedToGeneTO;

public class ESTTO extends CallSourceRawDataTO implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 12343L;

	public EstTO()
    {
    	super();
    	this.estLibraryId = null;
    }
	
	public String estLibraryId;
}
