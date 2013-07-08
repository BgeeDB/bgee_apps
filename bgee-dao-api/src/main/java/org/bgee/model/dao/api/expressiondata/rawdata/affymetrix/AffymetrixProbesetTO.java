package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixProbeset}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixProbeset
 * @since Bgee 11
 */
public class AffymetrixProbesetTO extends CallSourceRawDataTO implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 112434L;

    public String bgeeAffymetrixChipId;
    public float normalizedSignalIntensity;

	public String detectionFlag;
	
	public AffymetrixProbesetTO()
	{
		super();
		this.bgeeAffymetrixChipId = null;
		this.normalizedSignalIntensity = 0;
		this.detectionFlag = "undefined";
	}

}
