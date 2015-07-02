package org.bgee.model.expressiondata.rawdata.affymetrix;

import org.bgee.model.expressiondata.rawdata.CallSourceRawData;

/**
 * Class related to Affymetrix probesets. 
 * Is contained by an {@code AffymetrixChip}. 
 * Hold expression data for a gene 
 * (child class of {@code CallSourceRawData}). 
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see AffymetrixChip
 * @since Bgee 01
 */
public class AffymetrixProbeset extends CallSourceRawData
{
	/**
	 * The {@code AffymetrixChip} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
    private AffymetrixChip affymetrixChip;
    /**
     * ID in the Bgee database of the Affymetrix chip which this probeset belongs to. 
     * (ID in the Bgee database is different from the ID in the source database: 
     * bgeeAffymetrixChipId is unique in Bgee, while affymetrixChipId (ID from source database) 
     * is not (the couple microarrayExperimentId - affymetrixChipId is). 
     * In {@code AffymetrixChip} objects, bgeeAffymetrixChipId is stored 
     * in the {@code id} attribute, and affymetrixChipId in the {@code affymetrixChipId}attribute).
     * <p>
     * This attribute is useful when the {@code affymetrixChip} is not set. 
	 * When both are set, this {@code bgeeAffymetrixChipId} matches 
	 * the {@code id} attribute of the {@code AffymetrixChip}
     * 
     * @see AffymetrixChip#id
     */
    private String bgeeAffymetrixChipId;
    
    private float normalizedSignalIntensity;
	private String detectionFlag;
	
	/**
	 * Default constructor. 
	 */
	public AffymetrixProbeset()
    {
    	super();
    	this.setAffymetrixChip(null);
    	this.setBgeeAffymetrixChipId(null);
    	this.setNormalizedSignalIntensity(0);
    	this.setDetectionFlag("undefined");
    }
//	
//	/**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code AffymetrixChip} container of this {@code AffymetrixProbeset}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code AffymetrixChip} container of this {@code AffymetrixProbeset}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code AffymetrixChip} container.
//	 * @see #affymetrixChip
//	 * @see #getDataSource()
//	 */
//	@Override
//	public DataSource getDataSourceFromContainer()
//	{
//		if (this.getAffymetrixChip() != null) { 
//			return this.getAffymetrixChip().getDataSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code AffymetrixChip} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code AffymetrixChip} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #affymetrixChip
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getDataSourceIdFromContainer()
//	{
//		if (this.getAffymetrixChip() != null) { 
//			return this.getAffymetrixChip().getDataSourceId();
//		}
//		return "";
//	}
//	
//
//	public void loadChip() 
//	{
//		AffymetrixChipFactory chipLoader = new AffymetrixChipFactory();
//		this.setAffymetrixChip(chipLoader.getAffymetrixChipById(this.getBgeeAffymetrixChipId()));
//	}
	
	
	
	public void setAffymetrixChip(AffymetrixChip affyChip)
	{
		this.affymetrixChip = affyChip;
	}
	public AffymetrixChip getAffymetrixChip()
	{
		return this.affymetrixChip;
	}
	/**
	 * @param 	bgeeAffyChipId the bgeeAffymetrixChipId to set
	 * @see 	#bgeeAffymetrixChipId
	 */
	public void setBgeeAffymetrixChipId(String bgeeAffyChipId)
	{
		this.bgeeAffymetrixChipId = bgeeAffyChipId;
	}
//	/**
//	 * Returns either the value of {@code bgeeAffymetrixChipId}, 
//	 * or the of the {@code id} of the {@code AffymetrixChip} 
//	 * stored in {@code affymetrixChip}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the affymetrix chip this probeset belongs to. 
//	 * @see 	#bgeeAffymetrixChipId
//	 * @see 	#affymetrixChip
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getBgeeAffymetrixChipId()
//	{
//		return this.getIdByEntityOrId(this.getAffymetrixChip(), this.bgeeAffymetrixChipId);
//	}
	
	public void setNormalizedSignalIntensity(float nsi)
	{
		this.normalizedSignalIntensity = nsi;
	}
	public float getNormalizedSignalIntensity()
	{
		return this.normalizedSignalIntensity;
	}


	public void setDetectionFlag(String detectFlag) 
	{
		this.detectionFlag = detectFlag;
	}
	public String getDetectionFlag()
	{
		return this.detectionFlag;
	}
	public String getExpressedNotExpressed()
	{
		if (this.detectionFlag.equals("marginal") || 
			this.detectionFlag.equals("present")) {
			return "expressed";
		}
		return "not expressed";
	}
}
