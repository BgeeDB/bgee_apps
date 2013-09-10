package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.Entity;

/**
 * Represents a source of gene expression data calls, for instance, 
 * an Affymetrix probeset, an EST, a value of RPKM for a gene in a RNA-Seq library.
 * <p>
 * <code>CallSource</code>s are always produced by {@link ExperimentalObject}s. 
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
public abstract class CallSource<T extends ExperimentalObject> extends Entity {
	
	/**
	 * A <code>String</code> corresponding to the ID 
	 * of the {@link ExperimentalObject} that produced this <code>CallSource</code>. 
	 * It can be used to load the {@link #experimentalObject} attribute after obtaining 
	 * this <code>CallSource</code>. 
	 * @see #experimentalObject
	 */
    private String experimentalObjectId;
    /**
     * The {@link ExperimentalObject} that produced this <code>CallSource</code>.
     * @see #experimentalObjectId
     */
    private T experimentalObject;
    
    //TODO
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
	 * Load the {@link ExperimentalObject} that produced this <code>CallSource</code>, 
	 * by obtaining it from a <code>DAO</code>, only if not already loaded 
	 * ({@link #getExperimentalObject()} returning <code>null</code>).
	 * 
	 * @see #getExperimentalObject()
	 * @see #getExperimentalObjectId()
	 */
	public abstract void loadExperimentalObject();
	
	/**
	 * Get the ID of the {@link ExperimentalObject} that produced this <code>CallSource</code>.
	 * 
	 * @return 	a <code>String</code> corresponding to the <code>ExperimentalObject</code> ID.
	 * @see #getExperimentalObject()
	 */
	public String getExperimentalObjectId() {
		if (this.getExperimentalObject() != null) {
			return this.getExperimentalObject().getId();
		} 
		return this.experimentalObjectId;
	}

	/**
	 * Set the ID of the {@link ExperimentalObject} that produced this <code>CallSource</code>. 
	 * This is useful when retrieving this <code>CallSource</code> 
	 * from a <code>DAO</code>, before loading the actual <code>ExperimentalObject</code>.
	 * 
	 * @param 	A <code>String</code> that is the ID of the <code>ExperimentalObject</code> 
	 * 			that produced this <code>CallSource</code>
	 */
	public void setExperimentalObjectId(String experimentalObjectId) {
		this.experimentalObjectId = experimentalObjectId;
	}

	/**
	 * Get the {@link ExperimentalObject} that produced this <code>CallSource</code>. 
	 * Should return <code>null</code> if it has not yet been loaded 
	 * (using {@link #loadExperimentalObject()}), or set (using 
	 * {@link setExperimentalObject(ExperimentalObject)})
	 * 
	 * @return	the <code>ExperimentalObject</code> that produced this <code>CallSource</code>.
	 */
	public T getExperimentalObject() {
		return this.experimentalObject;
	}

	/**
	 * Set the {@link ExperimentalObject} that produced this <code>CallSource</code>. 
	 * @param 	experimentalObject The <code>ExperimentalObject</code> to set.
	 */
	public void setExperimentalObject(T experimentalObject) {
		this.experimentalObject = experimentalObject;
	}
}
