package model.data.common.expressionData.affymetrixData;

import java.io.Serializable;

import model.data.common.expressionData.ExpressionDataLinkedToGeneTO;

/**
 * A <code>TransferObject</code> used to communicate 
 * information related to Affymetrix probesets  
 * between the <code>model</code> layer (the business logic layer), 
 * and the <code>model.data</code> layer (the data source layer).
 * It encapsulates the information that could be retrieved from the data source. 
 * <p>
 * Affymetrix probesets hold expression call for a gene, 
 * so this <code>AffymetrixProbesetTO</code> is a child class of <code>CallSourceRawDataTO</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixProbesetDAO
 * @since Bgee 01
 *
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
