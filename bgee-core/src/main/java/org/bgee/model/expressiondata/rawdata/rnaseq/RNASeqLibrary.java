package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.rawdata.DataAnnotated;
import org.bgee.model.expressiondata.rawdata.SampleAssay;

/**
 * Class related to RNA-Seq library. 
 * Is contained by a {@code RNASeqExp} 
 * and is the container of {@code RNASeqResult}s. 
 * Is mapped to anatomical and developmental ontologies 
 * (child class of {@code RawDataAnnotated}).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqExp
 * @see RNASeqResult
 * @since Bgee 12
 */
public class RNASeqLibrary extends SampleAssay implements DataAnnotated 
{

	/**
	 * An {@code enum} listing the different types of RNA-Seq libraries 
	 * used in Bgee: 
	 * <ul>
	 * <li>{@code SINGLE}: single read libraries.
	 * <li>{@code PAIRED}: paired-end read libraries.
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public enum RNASeqLibraryType {
		SINGLE, PAIRED;
	}
	
	/**
	 * The {@code RNASeqExp} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
	private RNASeqExp rNASeqExp;
	/**
	 * A {@code String} corresponding to the ID 
	 * of the Rna-Seq experiment this library belongs to. 
	 * This attribute is useful when the {@code rNASeqExp} is not set. 
	 * When both are set, this {@code rnaSeqExperimentId} matches 
	 * the {@code id} attribute of the {@code RNASeqExp}. 
	 * @see rNASeqExp
	 */
	private String rnaSeqExperimentId;

	/**
	 * A {@code String} representing the secondary ID 
	 * of this {@code RNASeqLibrary}, 
	 * corresponding to the ID used in the SRA database. 
	 * <p>
	 * The ID used in Bgee is different and comes from the GEO database. 
	 * It is stored in the regular {@code id} attribute of this {@code RNASeqLibrary}.
	 * 
	 * @see model.Entity#id
	 */
	private String secondaryLibraryId;
	/**
	 * A {@code String} representing the ID of the platform used 
	 * to generate this RNA-Seq library.
	 */
	private String platformId;
	/**
	 * A {@code float} representing the threshold in log2 RPK 
	 * (Reads Per Kilobase), above which genes are considered as "present".
	 */
	private float log2RPKThreshold;
	/**
	 * A {@code float} representing the percentage of genes 
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	private float allGenesPercentPresent;
	/**
	 * A {@code float} representing the percentage of protein-coding genes 
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	private float proteinCodingGenesPercentPresent;
	/**
	 * A {@code float} representing the percentage of intronic regions  
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	private float intronicRegionsPercentPresent;
	/**
	 * A {@code float} representing the percentage of intergenic regions  
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	private float intergenicRegionsPercentPresent;
	/**
	 * An {@code int} representing the count of reads present in this library.
	 */
	private int allReadsCount;
	/**
	 * An {@code int} representing the count of remaining reads in this library 
	 * after filtering by tophat.
	 */
	private int usedReadsCount;
	/**
	 * An {@code int} representing the count of reads from this library 
	 * that could be aligned to the transcriptome, intergenic regions, and intronic regions.
	 */
	private int alignedReadsCount;
	/**
	 * An {@code int} representing the minimum length in bases of reads aligned in this library.
	 */
	private int minReadLength;
	/**
	 * An {@code int} representing the maximum length in bases of reads aligned in this library.
	 */
	private int maxReadLength;
	/**
	 * An {@code enum} representing the two types of library used: 
	 * {@code SINGLE}, corresponding to libraries built using single reads (no paired-end reads used), 
	 * and {@code PAIRED} for libraries built using paired-end reads. 
	 */
	public enum LibraryType {SINGLE, PAIRED;}
	/**
	 * The type of this library: either {@code SINGLE} for libraries built 
	 * using single reads (not paired-end), 
	 * or {@code PAIRED} for libraries built using paired-end reads.
	 * 
	 * @see LibraryType
	 */
	private LibraryType libraryType;
	
	
	/**
	 * Default constructor. 
	 */
    public RNASeqLibrary()
    {//TODO
    	super(null);
    	this.setRnaSeqExperiment(null);
    	this.setRnaSeqExperimentId(null);
    	this.setSecondaryLibraryId(null);
    	this.setPlatformId(null);
    	this.setLog2RPKThreshold(-999999);
    	this.setAllGenesPercentPresent(0);
    	this.setProteinCodingGenesPercentPresent(0);
    	this.setIntronicRegionsPercentPresent(0);
    	this.setIntergenicRegionsPercentPresent(0);
    	this.setAllReadsCount(0);
    	this.setUsedReadsCount(0);
    	this.setAlignedReadsCount(0);
    	this.setMinReadLength(0);
    	this.setMaxReadLength(0);
    	this.setLibraryType(LibraryType.SINGLE);
    }
    
    
//    
//    /**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code RNASeqExp} container of this {@code RNASeqLibrary}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code RNASeqExp} container of this {@code RNASeqLibrary}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code RNASeqExp} container.
//	 * @see #rNASeqExp
//	 * @see #getDataSource()
//	 */
//	@Override
//	public DataSource getDataSourceFromContainer()
//	{
//		if (this.getRnaSeqExperiment() != null) {
//	        return this.getRnaSeqExperiment().getDataSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code RNASeqExp} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code RNASeqExp} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #rNASeqExp
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getDataSourceIdFromContainer()
//	{
//		if (this.getRnaSeqExperiment() != null) { 
//			return this.getRnaSeqExperiment().getDataSourceId();
//		}
//		return "";
//	}
//	
//	
//	/**
//     * Retrieve the {@code RNASeqExp} this {@code RNASeqLibrary} belongs to, 
//     * by using the ID provided by {@code #getRnaSeqExperimentId()}, 
//     * and store it by using <code>#setRnaSeqExperiment(RNASeqExp)<code>.
//     */
//	public void loadRnaSeqExperiment() 
//	{
//		RNASeqExpFactory loader = new RNASeqExpFactory();
//		this.setRnaSeqExperiment(loader.getExperimentById(this.getRnaSeqExperimentId()));
//	}
    
    
    
	/**
	 * @return 	the rNASeqExp
	 * @see 	#rNASeqExp
	 */
    public RNASeqExp getRnaSeqExperiment() {
		return this.rNASeqExp;
	}
	/**
	 * @param 	rNASeqExp the rNASeqExp to set
	 * @see 	#rNASeqExp
	 */
	public void setRnaSeqExperiment(RNASeqExp rNASeqExp) {
		this.rNASeqExp = rNASeqExp;
	}
//	/**
//	 * Returns either the value of {@code rnaSeqExperimentId}, 
//	 * or the of the {@code id} of the {@code RNASeqExp} 
//	 * stored in {@code rNASeqExp}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the RNA-Seq experiment this library belongs to. 
//	 * @see 	#rnaSeqExperimentId
//	 * @see 	#rNASeqExp
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getRnaSeqExperimentId() {
//		return this.getIdByEntityOrId(this.getRnaSeqExperiment(), this.rnaSeqExperimentId);
//	}
	/**
	 * @param 	rnaSeqExperimentId the rnaSeqExperimentId to set
	 * @see 	#rnaSeqExperimentId
	 */
	public void setRnaSeqExperimentId(String rnaSeqExperimentId) {
		this.rnaSeqExperimentId = rnaSeqExperimentId;
	}

	/**
	 * @return 	the secondaryLibraryId
	 * @see 	#secondaryLibraryId
	 */
	public String getSecondaryLibraryId() {
		return this.secondaryLibraryId;
	}
	/**
	 * @param 	secondaryLibraryId the secondaryLibraryId to set
	 * @see 	#secondaryLibraryId
	 */
	public void setSecondaryLibraryId(String secondaryLibraryId) {
		this.secondaryLibraryId = secondaryLibraryId;
	}

	/**
	 * @return 	the platformId
	 * @see 	#platformId
	 */
	public String getPlatformId() {
		return this.platformId;
	}
	/**
	 * @param 	platformId the platformId to set
	 * @see 	#platformId
	 */
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	/**
	 * @return 	the log2RPKThreshold
	 * @see 	#log2RPKThreshold
	 */
	public float getLog2RPKThreshold() {
		return this.log2RPKThreshold;
	}
	/**
	 * @param 	log2rpkThreshold the log2RPKThreshold to set
	 * @see 	#log2RPKThreshold
	 */
	public void setLog2RPKThreshold(float log2rpkThreshold) {
		this.log2RPKThreshold = log2rpkThreshold;
	}

	/**
	 * @return 	the allGenesPercentPresent
	 * @see 	#allGenesPercentPresent
	 */
	public float getAllGenesPercentPresent() {
		return this.allGenesPercentPresent;
	}
	/**
	 * @param 	allGenesPercentPresent the allGenesPercentPresent to set
	 * @see 	#allGenesPercentPresent
	 */
	public void setAllGenesPercentPresent(float allGenesPercentPresent) {
		this.allGenesPercentPresent = allGenesPercentPresent;
	}

	/**
	 * @return 	the proteinCodingGenesPercentPresent
	 * @see 	#proteinCodingGenesPercentPresent
	 */
	public float getProteinCodingGenesPercentPresent() {
		return this.proteinCodingGenesPercentPresent;
	}
	/**
	 * @param 	proteinCodingGenesPercentPresent the proteinCodingGenesPercentPresent to set
	 * @see 	#proteinCodingGenesPercentPresent
	 */
	public void setProteinCodingGenesPercentPresent(
			float proteinCodingGenesPercentPresent) {
		this.proteinCodingGenesPercentPresent = proteinCodingGenesPercentPresent;
	}

	/**
	 * @return 	the intronicRegionsPercentPresent
	 * @see 	#intronicRegionsPercentPresent
	 */
	public float getIntronicRegionsPercentPresent() {
		return this.intronicRegionsPercentPresent;
	}
	/**
	 * @param 	intronicRegionsPercentPresent the intronicRegionsPercentPresent to set
	 * @see 	#intronicRegionsPercentPresent
	 */
	public void setIntronicRegionsPercentPresent(
			float intronicRegionsPercentPresent) {
		this.intronicRegionsPercentPresent = intronicRegionsPercentPresent;
	}

	/**
	 * @return 	the intergenicRegionsPercentPresent
	 * @see 	#intergenicRegionsPercentPresent
	 */
	public float getIntergenicRegionsPercentPresent() {
		return this.intergenicRegionsPercentPresent;
	}
	/**
	 * @param 	intergenicRegionsPercentPresent the intergenicRegionsPercentPresent to set
	 * @see 	#intergenicRegionsPercentPresent
	 */
	public void setIntergenicRegionsPercentPresent(
			float intergenicRegionsPercentPresent) {
		this.intergenicRegionsPercentPresent = intergenicRegionsPercentPresent;
	}

	/**
	 * @return 	the allReadsCount
	 * @see 	#allReadsCount
	 */
	public int getAllReadsCount() {
		return this.allReadsCount;
	}
	/**
	 * @param 	allReadsCount the allReadsCount to set
	 * @see 	#allReadsCount
	 */
	public void setAllReadsCount(int allReadsCount) {
		this.allReadsCount = allReadsCount;
	}

	/**
	 * @return 	the usedReadsCount
	 * @see 	#usedReadsCount
	 */
	public int getUsedReadsCount() {
		return this.usedReadsCount;
	}
	/**
	 * @param 	usedReadsCount the usedReadsCount to set
	 * @see 	#usedReadsCount
	 */
	public void setUsedReadsCount(int usedReadsCount) {
		this.usedReadsCount = usedReadsCount;
	}

	/**
	 * @return 	the alignedReadsCount
	 * @see 	#alignedReadsCount
	 */
	public int getAlignedReadsCount() {
		return this.alignedReadsCount;
	}
	/**
	 * @param 	alignedReadsCount the alignedReadsCount to set
	 * @see 	#alignedReadsCount
	 */
	public void setAlignedReadsCount(int alignedReadsCount) {
		this.alignedReadsCount = alignedReadsCount;
	}

	/**
	 * @return 	the minReadLength
	 * @see 	#minReadLength
	 */
	public int getMinReadLength() {
		return this.minReadLength;
	}
	/**
	 * @param 	minReadLength the minReadLength to set
	 * @see 	#minReadLength
	 */
	public void setMinReadLength(int minReadLength) {
		this.minReadLength = minReadLength;
	}

	/**
	 * @return 	the maxReadLength
	 * @see 	#maxReadLength
	 */
	public int getMaxReadLength() {
		return this.maxReadLength;
	}
	/**
	 * @param 	maxReadLength the maxReadLength to set
	 * @see 	#maxReadLength
	 */
	public void setMaxReadLength(int maxReadLength) {
		this.maxReadLength = maxReadLength;
	}

	/**
	 * @return 	the libraryType
	 * @see 	#libraryType
	 */
	public LibraryType getLibraryType() {
		return this.libraryType;
	}
	/**
	 * @param 	libraryType the libraryType to set
	 * @see 	#libraryType
	 */
	public void setLibraryType(LibraryType libraryType) {
		this.libraryType = libraryType;
	}



    @Override
    public void loadExperiment() {
        // TODO Auto-generated method stub
        
    }
}
