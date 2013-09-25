package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.io.Serializable;

import org.bgee.model.dao.api.TransferObject;

/**
 * {@code TransferObject} for the class 
 * {@link org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence}.
 * <p>
 * For information on this {@code TransferObject} and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence
 * @since Bgee 11
 */
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
