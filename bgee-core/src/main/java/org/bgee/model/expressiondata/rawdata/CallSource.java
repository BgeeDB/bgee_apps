package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.Entity;
import org.bgee.model.expressiondata.Call;

/**
 * Represents a source of gene expression data calls, for instance, 
 * an Affymetrix probeset, an EST, a value of RPKM for a gene in a RNA-Seq library.
 * <p>
 * <code>CallSource</code>s are always produced by {@link SampleAssay}s. 
 * The precise type of the <code>ExperimentObject</code> is provided by the <code>T</code> 
 * generic parameter.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>	The precise type of <code>ExperimentObject</code> that produced this 
 * 				<code>CallSource</code>
 */
public abstract class CallSource<T extends SampleAssay> extends Entity {
	
	/**
	 * A <code>String</code> corresponding to the ID 
	 * of the {@link SampleAssay} that produced this <code>CallSource</code>. 
	 * It can be used to load the {@link #sampleAssay} attribute after obtaining 
	 * this <code>CallSource</code>. 
	 * @see #sampleAssay
	 */
    private String sampleAssayId;
    /**
     * The {@link SampleAssay} that produced this <code>CallSource</code>.
     * @see #sampleAssayId
     */
    private T sampleAssay;
    
    //TODO
    plutot, juste stoker un CallType et une DataQuality. La classe Call represente d'avantage un résumé global
    private Call callGenerated;
	
	/**
     * Constructor providing the <code>id</code> of this <code>CallSource</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespace only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of this object.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespace only. 
     */
	public CallSource(String id) throws IllegalArgumentException {
		super(id);
	}
	
	/**
	 * Load the {@link SampleAssay} that produced this <code>CallSource</code>, 
	 * by obtaining it from a <code>DAO</code>, only if not already loaded 
	 * ({@link #getSampleAssay()} returning <code>null</code>).
	 * 
	 * @see #getSampleAssay()
	 * @see #getSampleAssayId()
	 */
	public abstract void loadSampleAssay();
	
	/**
	 * Get the ID of the {@link SampleAssay} that produced this <code>CallSource</code>.
	 * 
	 * @return 	a <code>String</code> corresponding to the <code>SampleAssay</code> ID.
	 * @see #getSampleAssay()
	 */
	public String getSampleAssayId() {
		if (this.getSampleAssay() != null) {
			return this.getSampleAssay().getId();
		} 
		return this.sampleAssayId;
	}

	/**
	 * Set the ID of the {@link SampleAssay} that produced this <code>CallSource</code>. 
	 * This is useful when retrieving this <code>CallSource</code> 
	 * from a <code>DAO</code>, before loading the actual <code>SampleAssay</code>.
	 * 
	 * @param 	A <code>String</code> that is the ID of the <code>SampleAssay</code> 
	 * 			that produced this <code>CallSource</code>
	 */
	public void setSampleAssayId(String sampleAssayId) {
		this.sampleAssayId = sampleAssayId;
	}

	/**
	 * Get the {@link SampleAssay} that produced this <code>CallSource</code>. 
	 * Should return <code>null</code> if it has not yet been loaded 
	 * (using {@link #loadSampleAssay()}), or set (using 
	 * {@link setSampleAssay(SampleAssay)})
	 * 
	 * @return	the <code>SampleAssay</code> that produced this <code>CallSource</code>.
	 */
	public T getSampleAssay() {
		return this.sampleAssay;
	}

	/**
	 * Set the {@link SampleAssay} that produced this <code>CallSource</code>. 
	 * @param 	sampleAssay The <code>SampleAssay</code> to set.
	 */
	public void setSampleAssay(T sampleAssay) {
		this.sampleAssay = sampleAssay;
	}
}
