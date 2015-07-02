package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.rawdata.CallSourceRawData;

/**
 * Class related to RNA-Seq result for a gene. 
 * Is contained by a {@code RNASeqLibrary}. 
 * Hold expression call for a gene 
 * (child class of {@code CallSourceRawData}). 
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqLibrary
 * @since Bgee 12
 */
public class RNASeqResult extends CallSourceRawData
{
	/**
	 * The {@code RNASeqLibrary} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
	private RNASeqLibrary rNASeqLibrary;
	/**
	 * A {@code String} corresponding to the ID 
	 * of the RNA-Seq library this result belongs to. 
	 * This attribute is useful when the {@code rNASeqLibrary} is not set. 
	 * When both are set, this {@code rnaSeqLibraryId} matches 
	 * the {@code id} attribute of the {@code RNASeqLibrary}. 
	 * @see microarrayExperiment
	 */
	private String rnaSeqLibraryId;
	/**
	 * A {@code float} representing the log2 RPK 
	 * (Reads Per Kilobase) for this gene in this library.
	 */
	private float log2RPK;
	/**
	 * An int representing the number of reads aligned to this gene 
	 * in this library. 
	 */
	private int readsCount;
	/**
	 * A {@code String} representing the expression call for this gene 
	 * in this library ('undefined', 'present', 'absent').
	 * @TODO change this for an Enum.
	 */
	private String detectionFlag;
	
	/**
	 * Default constructor. 
	 */
    public RNASeqResult()
    {
    	super();
    	this.setRnaSeqLibrary(null);
    	this.setRnaSeqLibraryId(null);
    	this.setLog2RPK(-999999);
    	this.setReadsCount(0);
    	this.setDetectionFlag("undefined");
    }
    
    
//    
//    /**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code RNASeqLibrary} container of this {@code RNASeqResult}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code RNASeqLibrary} container of this {@code RNASeqResult}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code RNASeqLibrary} container.
//	 * @see #rNASeqLibrary
//	 * @see #getDataSource()
//	 */
//	@Override
//	public DataSource getDataSourceFromContainer()
//	{
//		if (this.getRnaSeqLibrary() != null) {
//	        return this.getRnaSeqLibrary().getDataSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code RNASeqLibrary} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code RNASeqLibrary} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #rNASeqLibrary
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getDataSourceIdFromContainer()
//	{
//		if (this.getRnaSeqLibrary() != null) { 
//			return this.getRnaSeqLibrary().getDataSourceId();
//		}
//		return "";
//	}
//    
//	
//    /**
//     * Retrieve the {@code RNASeqLibrary} this {@code RNASeqResult} belongs to, 
//     * by using the ID provided by {@code #getRnaSeqLibraryId()}, 
//     * and store it by using <code>#setRnaSeqLibrary(RNASeqLibrary)<code>.
//     */
//	public void loadRnaSeqLibrary() 
//	{
//		RNASeqLibraryFactory loader = new RNASeqLibraryFactory();
//		this.setRnaSeqLibrary(loader.getRnaSeqLibraryById(this.getRnaSeqLibraryId()));
//	}
    

	/**
	 * @return 	the rNASeqLibrary
	 * @see 	#rNASeqLibrary
	 */
    public RNASeqLibrary getRnaSeqLibrary() {
		return this.rNASeqLibrary;
	}
	/**
	 * @param 	rNASeqLibrary the rNASeqLibrary to set
	 * @see 	#rNASeqLibrary
	 */
	public void setRnaSeqLibrary(RNASeqLibrary rNASeqLibrary) {
		this.rNASeqLibrary = rNASeqLibrary;
	}

//	/**
//	 * Returns either the value of {@code rnaSeqLibraryId}, 
//	 * or the of the {@code id} of the {@code RNASeqLibrary} 
//	 * stored in {@code rNASeqLibrary}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the RNA-Seq library this result belongs to. 
//	 * @see 	#rnaSeqLibraryId
//	 * @see 	#rNASeqLibrary
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getRnaSeqLibraryId() {
//		return this.getIdByEntityOrId(this.getRnaSeqLibrary(), this.rnaSeqLibraryId);
//	}
	/**
	 * @param 	rnaSeqLibraryId the rnaSeqLibraryId to set
	 * @see 	#rnaSeqLibraryId
	 */
	public void setRnaSeqLibraryId(String rnaSeqLibraryId) {
		this.rnaSeqLibraryId = rnaSeqLibraryId;
	}

	/**
	 * @return 	the log2RPK
	 * @see 	#log2RPK
	 */
	public float getLog2RPK() {
		return this.log2RPK;
	}
	/**
	 * @param 	log2rpk the log2RPK to set
	 * @see 	#log2RPK
	 */
	public void setLog2RPK(float log2rpk) {
		this.log2RPK = log2rpk;
	}

	/**
	 * @return 	the readsCount
	 * @see 	#readsCount
	 */
	public int getReadsCount() {
		return this.readsCount;
	}
	/**
	 * @param 	readsCount the readsCount to set
	 * @see 	#readsCount
	 */
	public void setReadsCount(int readsCount) {
		this.readsCount = readsCount;
	}

	/**
	 * @return 	the detectionFlag
	 * @see 	#detectionFlag
	 */
	public String getDetectionFlag() {
		return this.detectionFlag;
	}
	/**
	 * @param 	detectionFlag the detectionFlag to set
	 * @see 	#detectionFlag
	 */
	public void setDetectionFlag(String detectionFlag) {
		this.detectionFlag = detectionFlag;
	}
}
