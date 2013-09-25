package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * {@code TransferObject} for the class 
 * {@link org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqResult}.
 * <p>
 * For information on this {@code TransferObject} and its fields, 
 * see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqResult
 * @since Bgee 12
 */
public class RNASeqResultTO extends CallSourceRawDataTO implements Serializable
{
    private static final long serialVersionUID = 9192921864601490175L;
    
    /**
	 * A {@code String} corresponding to the ID 
	 * of the RNA-Seq library this result belongs to. 
	 */
	public String rnaSeqLibraryId;
	/**
	 * A {@code float} representing the log2 RPK 
	 * (Reads Per Kilobase) for this gene in this library.
	 */
	public float log2RPK;
	/**
	 * An int representing the number of reads aligned to this gene 
	 * in this library. 
	 */
	public int readsCount;
	/**
	 * A {@code String} representing the expression call for this gene 
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
