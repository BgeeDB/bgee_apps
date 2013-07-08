package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip
 * @since Bgee 11
 */
public class AffymetrixChipTO extends RawDataAnnotatedTO implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1434334L;
	
    public String microarrayExperimentId;
    public String affymetrixChipId;
    
    public String chipType;
    public String normalizationType;
    public String detectionType;
    
    /**
	 * Default constructor. 
	 */
	public AffymetrixChipTO()
	{
		super();
		this.microarrayExperimentId = null;
		this.affymetrixChipId = null;
		this.chipType = null;
		this.normalizationType = null;
		this.detectionType = null;
	}

}
