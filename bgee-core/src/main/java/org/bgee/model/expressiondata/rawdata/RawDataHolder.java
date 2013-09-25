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
	 * Indicate whether this {@code RawDataHolder} currently holds some data 
	 * (for instance, an {@code Experiment})
	 * 
	 * @return 	{@code true} if this {@code RawDataHolder} currently holds 
	 * 			some data.
	 */
    public boolean hasData();
    /**
	 * Indicate whether this {@code RawDataHolder} currently holds some data count
	 * (for instance, a count of the number of experiments used).
	 * 
	 * @return 	{@code true} if this {@code RawDataHolder} currently holds 
	 * 			any data count.
	 */
    public boolean hasDataCount();
}
