package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.insitu.InSituSpot}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.insitu.InSituSpot
 * @since Bgee 11
 */
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
