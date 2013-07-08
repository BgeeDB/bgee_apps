package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;

/**
 * <code>TransferObject</code> for the class 
 * {@link org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqLibrary}.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqLibrary
 * @since Bgee 12
 */
public class RNASeqLibraryTO extends RawDataAnnotatedTO implements Serializable
{
	private static final long serialVersionUID = 1434335L;
	
	/**
	 * A <code>String</code> corresponding to the ID 
	 * of the Rna-Seq experiment this library belongs to. 
	 */
	public String rnaSeqExperimentId;

	/**
	 * A <code>String</code> representing the secondary ID 
	 * of this library, 
	 * corresponding to the ID used in the SRA database. 
	 * <p>
	 * The ID used in Bgee is different and comes from the GEO database. 
	 * It is stored in the regular <code>id</code> attribute of this <code>RNASeqLibrary</code>.
	 * 
	 * @see model.Entity#id
	 */
	public String secondaryLibraryId;
	/**
	 * A <code>String</code> representing the ID of the platform used 
	 * to generate this RNA-Seq library.
	 */
	public String platformId;
	/**
	 * A <code>float</code> representing the threshold in log2 RPK 
	 * (Reads Per Kilobase), above which genes are considered as "present".
	 */
	public float log2RPKThreshold;
	/**
	 * A <code>float</code> representing the percentage of genes 
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	public float allGenesPercentPresent;
	/**
	 * A <code>float</code> representing the percentage of protein-coding genes 
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	public float proteinCodingGenesPercentPresent;
	/**
	 * A <code>float</code> representing the percentage of intronic regions  
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	public float intronicRegionsPercentPresent;
	/**
	 * A <code>float</code> representing the percentage of intergenic regions  
	 * flagged as "present" in this library (values from 0 to 100). 
	 */
	public float intergenicRegionsPercentPresent;
	/**
	 * An <code>int</code> representing the count of reads present in this library.
	 */
	public int allReadsCount;
	/**
	 * An <code>int</code> representing the count of remaining reads in this library 
	 * after filtering by tophat.
	 */
	public int usedReadsCount;
	/**
	 * An <code>int</code> representing the count of reads from this library 
	 * that could be aligned to the transcriptome, intergenic regions, and intronic regions.
	 */
	public int alignedReadsCount;
	/**
	 * An <code>int</code> representing the minimum length in bases of reads aligned in this library.
	 */
	public int minReadLength;
	/**
	 * An <code>int</code> representing the maximum length in bases of reads aligned in this library.
	 */
	public int maxReadLength;
	/**
	 * A <code>String</code> representing the type of this library 
	 * (either "single" for single reads libraries, 
	 * or "paired" for libraries using paired-end reads).
	 */
	public String libraryType;
	
	/**
	 * Default constructor. 
	 */
    public RNASeqLibraryTO()
    {
    	super();
    	this.rnaSeqExperimentId = null;
    	this.secondaryLibraryId = null;
    	this.platformId = null;
    	this.log2RPKThreshold = -999999;
    	this.allGenesPercentPresent = 0;
    	this.proteinCodingGenesPercentPresent = 0;
    	this.intronicRegionsPercentPresent = 0;
    	this.intergenicRegionsPercentPresent = 0;
    	this.allReadsCount = 0;
    	this.usedReadsCount = 0;
    	this.alignedReadsCount = 0;
    	this.minReadLength = 0;
    	this.maxReadLength = 0;
    	this.libraryType = null;
    }
}
