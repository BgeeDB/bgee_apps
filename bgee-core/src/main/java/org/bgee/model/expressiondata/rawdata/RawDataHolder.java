package org.bgee.model.expressiondata.rawdata;

/**
 * Interface that must be implemented by any class that can hold raw data, 
 * used in Bgee to generate data calls (see 
 * {@link org.bgee.model.expressiondata.DataParameters.CallType} for a list 
 * of data calls available)
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public interface RawDataHolder {
	/**
	 * Indicate whether this <code>RawDataHolder</code> currently holds some data 
	 * (for instance, an <code>AffymetrixChip</code>, or a <code>RNASeqExp</code>)
	 * 
	 * @return 	<code>true</code> if this <code>RawDataHolder</code> currently holds 
	 * 			some data.
	 */
    public boolean hasData();
    /**
	 * Indicate whether this <code>RawDataHolder</code> currently holds some data count
	 * (for instance, a count of Affymetrix chips used, or a count of RNA-Seq 
	 * experiment used).
	 * 
	 * @return 	<code>true</code> if this <code>RawDataHolder</code> currently holds 
	 * 			any data count.
	 */
    public boolean hasDataCount();
}
