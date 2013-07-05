package model.data.common.expressionData.inSituHybridizationData;

import java.io.Serializable;

import model.data.common.expressionData.ExpressionDataLinkedToGeneTO;

public class InSituSpotTO extends CallSourceRawDataTO implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 12433455L;
	
	public String inSituEvidenceId;
	public String organId;
	public String stageId;

	public String detectionFlag;
	
	public InSituSpotTO()
	{
		super();
		this.detectionFlag = "undefined";
	}

}
