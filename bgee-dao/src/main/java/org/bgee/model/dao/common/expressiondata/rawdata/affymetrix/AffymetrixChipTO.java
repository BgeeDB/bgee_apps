package model.data.common.expressionData.affymetrixData;

import java.io.Serializable;

import model.data.common.expressionData.ExpressionDataMappedToOntologiesTO;

/**
 * A <code>TransferObject</code> used to communicate 
 * information related to Affymetrix chips
 * between the <code>model</code> layer (the business logic layer), 
 * and the <code>model.data</code> layer (the data source layer). 
 * It encapsulates the information that could be retrieved from the data source. 
 * <p>
 * Affymetrix chips are mapped to anatomical and developmental ontologies, 
 * so this <code>AffymetrixChipTO</code> is a child class of <code>RawDataAnnotated</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixChipDAO
 * @since Bgee 01
 *
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
