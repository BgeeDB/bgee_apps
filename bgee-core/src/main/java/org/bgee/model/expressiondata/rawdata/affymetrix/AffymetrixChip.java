package org.bgee.model.expressiondata.rawdata.affymetrix;

import org.bgee.model.expressiondata.rawdata.DataAnnotated;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.SampleAssay;
import org.bgee.model.source.Source;

/**
 * Class related to Affymetrix chip. 
 * Is contained by a {@code AffymetrixExp} 
 * and is the container of {@code AffymetrixProbeset}s. 
 * Is mapped to anatomical and developmental ontologies 
 * (child class of {@code RawDataAnnotated}).
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see AffymetrixProbeset
 * @see AffymetrixExp
 * @since Bgee 01
 */
public class AffymetrixChip extends SampleAssay implements DataAnnotated
{
	/**
	 * An {@code enum} listing the different methods to generate expression calls 
	 * on Affymetrix chips: 
	 * <ul>
	 * <li>{@code MAS5}: expression calls from the MAS5 software. Such calls 
	 * are usually taken from a processed MAS5 file, and imply that the data 
	 * were also normalizd using MAS5.
	 * <li>{@code SCHUSTER}: Wilcoxon test on the signal of probesets 
	 * against a subset of weakly expressed probesets, to generate expression calls 
	 * (see <a href="http://www.ncbi.nlm.nih.gov/pubmed/17594492">
	 * Schuster et al., Genome Biology (2007)</a>). Such calls usually implies 
	 * that raw data were available, and were normalized using GCRMA. 
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 01
	 */
	public enum AffymetrixDetectionType {
		MAS5, SCHUSTER;
	}
	/**
	 * An {@code enum} listing the different methods used ib Bgee 
	 * to normalize Affymetrix data: 
	 * <ul>
	 * <li>{@code MAS5}: normalization using the MAS5 software. Using 
	 * this naormalization usually means that only the processed MAS5 files 
	 * were available, otherwise another method would be used. 
	 * <li>{@code RMA}: normalization by RMA method.
	 * <li>{@code GCRMA}: normalization by GCRMA method. This is the default 
	 * method in Bgee when raw data are available. 
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 01
	 */
	public enum AffymetrixNormalizationType {
		MAS5, RMA, GCRMA;
	}
	/**
	 * The {@code AffymetrixExp} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
    private AffymetrixExp affymetrixExp;
    /**
	 * A {@code String} corresponding to the ID 
	 * of the microarray experiment this chip belongs to. 
	 * This attribute is useful when the {@code affymetrixExp} is not set. 
	 * When both are set, this {@code microarrayExperimentId} matches 
	 * the {@code id} attribute of the {@code AffymetrixExp}. 
	 * @see affymetrixExp
	 */
    private String microarrayExperimentId;
    /**
     * A {@code String} representing the affymetrixChipId, 
     * meaning the ID of the chip in the source database 
     * (the ID in the bgee database is different, and is stored in the 
     * {@code id} attribute of this object).
     * The couple microarrayExperimentId - affymetrixChipId is unique 
     * in the database, affymetrixChipId alone is not.
     */
    private String affymetrixChipId;
    
    private String chipType;
    private String normalizationType;
    private String detectionType;
	
    /**
     * Default constructor. 
     */
	public AffymetrixChip(String  id)
    {
    	super(id);
    	this.setMicroarrayExperimentId(null);
    	this.setAffymetrixChipId(null);
    	this.setChipType(null);
    	this.setDetectionType(null);
    }
//	
//	/**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code AffymetrixExp} container of this {@code AffymetrixChip}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code AffymetrixExp} container of this {@code AffymetrixChip}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code AffymetrixExp} container.
//	 * @see #affymetrixExp
//	 * @see #getDataSource()
//	 */
//	@Override
//	public Source getSourceFromContainer()
//	{
//		if (this.getMicroarrayExperiment() != null) {
//	        return this.getMicroarrayExperiment().getSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code AffymetrixExp} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code AffymetrixExp} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #affymetrixExp
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getSourceIdFromContainer()
//	{
//		if (this.getMicroarrayExperiment() != null) { 
//			return this.getMicroarrayExperiment().getSourceId();
//		}
//		return "";
//	}
//
//	public void loadMicroarrayExperiment() 
//	{
//		AffymetrixExpFactory loader = new AffymetrixExpFactory();
//		this.setMicroarrayExperiment(loader.getExperimentById(this.getMicroarrayExperimentId()));
//	}
	
	
	public void setMicroarrayExperiment(AffymetrixExp exp)
	{
		this.affymetrixExp = exp;
	}
	public AffymetrixExp getMicroarrayExperiment()
	{
		return this.affymetrixExp;
	}
	public void setMicroarrayExperimentId(String expId)
	{
		this.microarrayExperimentId = expId;
	}
	/**
	 * Returns either the value of {@code microarrayExperimentId}, 
	 * or the of the {@code id} of the {@code AffymetrixExp} 
	 * stored in {@code affymetrixExp}, depending on which one is set. 
	 * 
	 * @return 	the ID of the microarray experiment this chip belongs to. 
	 * @see 	#microarrayExperimentId
	 * @see 	#affymetrixExp
	 * @see 	#getIdByEntityOrId(Entity, String)
	 */
	public String getMicroarrayExperimentId()
	{
		//return this.getIdByEntityOrId(this.getMicroarrayExperiment(), this.microarrayExperimentId);
		return null; //TODO
	}
	
	/**
	 * @return 	the affymetrixChipId
	 * @see 	#affymetrixChipId
	 */
	public String getAffymetrixChipId() {
		return this.affymetrixChipId;
	}

	/**
	 * @param 	affymetrixChipId the affymetrixChipId to set
	 * @see 	#affymetrixChipId
	 */
	public void setAffymetrixChipId(String affymetrixChipId) {
		this.affymetrixChipId = affymetrixChipId;
	}

	public void setChipType(String type)
	{
		this.chipType = type;
	}
	public String getChipType()
	{
		return this.chipType;
	}
	public void setNormalizationType(String type)
	{
		this.normalizationType = type;
	}
	public String getNormalizationType()
	{
		return this.normalizationType;
	}
	public void setDetectionType(String type)
	{
		this.detectionType = type;
	}
	public String getDetectionType()
	{
		return this.detectionType;
	}

    @Override
    public void loadExperiment() {
        // TODO Auto-generated method stub
        
    }

}
