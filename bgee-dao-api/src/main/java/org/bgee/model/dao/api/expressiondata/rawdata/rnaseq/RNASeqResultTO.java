package org.bgee.model.dao.common.expressiondata.rawdata.rnaseq;

import java.io.Serializable;

import model.data.common.expressionData.ExpressionDataLinkedToGeneTO;

/**
 * A <code>TransferObject</code> used to communicate 
 * information related to RNA-Seq results 
 * between the <code>model</code> layer (the business logic layer), 
 * and the <code>model.data</code> layer (the data source layer).
 * It encapsulates the information that could be retrieved from the data source. 
 * <p>
 * RNA-Seq results hold expression call for a gene, 
 * so this <code>RNASeqResultTO</code> is a child class of <code>CallSourceRawDataTO</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqResultDAO
 * @since Bgee 12
 *
 */
public class RNASeqResultTO extends CallSourceRawDataTO implements Serializable
{
    private static final long serialVersionUID = 9192921864601490175L;
    
    /**
	 * A <code>String</code> corresponding to the ID 
	 * of the RNA-Seq library this result belongs to. 
	 */
	public String rnaSeqLibraryId;
	/**
	 * A <code>float</code> representing the log2 RPK 
	 * (Reads Per Kilobase) for this gene in this library.
	 */
	public float log2RPK;
	/**
	 * An int representing the number of reads aligned to this gene 
	 * in this library. 
	 */
	public int readsCount;
	/**
	 * A <code>String</code> representing the expression call for this gene 
	 * in this library ('undefined', 'present', 'absent').
	 * @TODO change this for an Enum.
	 */
	public String detectionFlag;

    /**
     * Default constructor.
     */
    public RNASeqResultTO()
    {
    	super();
    	this.rnaSeqLibraryId = null;
    	this.log2RPK = -999999;
    	this.readsCount = 0;
    	this.detectionFlag = "undefined";
    }
}
