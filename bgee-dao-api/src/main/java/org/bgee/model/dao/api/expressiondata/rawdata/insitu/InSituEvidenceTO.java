package model.data.common.expressionData.inSituHybridizationData;

import java.io.Serializable;

import org.bgee.model.dao.api.TransferObject;

public class InSituEvidenceTO extends TransferObject implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 546546L;
	
	public String inSituExperimentId;
	
	public InSituEvidenceTO()
	{
		super();
	}

}
