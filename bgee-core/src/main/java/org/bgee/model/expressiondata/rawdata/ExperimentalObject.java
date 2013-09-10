package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.Entity;

/**
 * Represent experimental objects that allow to generate expression data, for instance: 
 * an Affymetrix chip, a RNA-Seq library, or an <em>in situ</em> image. 
 * Such objects: 
 * <ul>
 * <li>are always part of an {@link Experiment}. The precise type of the 
 * <code>Experiment</code> is provided by the <code>T</code> generic parameter.
 * <li>are always sources of {@link CallSource}s. 
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>	the precise type of the {@link Experiment} that contains 
 * 				this <code>ExperimentalObject</code>.
 */
public abstract class ExperimentalObject<T extends Experiment> extends Entity {
	
	/**
	 * A <code>String</code> corresponding to the ID 
	 * of the {@link Experiment} containing this <code>ExperimentalObject</code>. 
	 * It can be used to load the {@link #experiment} attribute after obtaining 
	 * this <code>ExperimentalObject</code>. 
	 * @see #experiment
	 */
    private String experimentId;
    /**
     * The {@link Experiment} containing this <code>ExperimentalObject</code>.
     * @see #experimentId
     */
    private T experiment;
	
	/**
     * Constructor providing the <code>id</code> of this <code>ExperimentalObject</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespace only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of this object.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespace only. 
     */
	public ExperimentalObject(String id) throws IllegalArgumentException {
		super(id);
		this.setExperiment(null);
		this.setExperimentId(null);
	}
	
	/**
	 * Load the {@link Experiment} containing this <code>ExperimentalObject</code>, 
	 * by obtaining it from a <code>DAO</code>, only if not already loaded 
	 * ({@link #getExperiment()} returning <code>null</code>).
	 * 
	 * @see #getExperiment()
	 * @see #getExperimentId()
	 */
	public abstract void loadExperiment();
	
	/**
	 * Get the ID of the {@link Experiment} containing this <code>ExperimentalObject</code>.
	 * 
	 * @return 	a <code>String</code> corresponding to the <code>Experiment</code> ID.
	 * @see #getExperiment()
	 */
	public String getExperimentId() {
		if (this.getExperiment() != null) {
			return this.getExperiment().getId();
		} 
		return this.experimentId;
	}

	/**
	 * Set the ID of the {@link Experiment} containing this <code>ExperimentalObject</code>. 
	 * This is useful when retrieving this <code>ExperimentalObject</code> 
	 * from a <code>DAO</code>, before loading the actual <code>Experiment</code>.
	 * 
	 * @param 	A <code>String</code> that is the ID of the <code>Experiment</code> 
	 * 			containing this <code>ExperimentalObject</code>
	 */
	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	/**
	 * Get the {@link Experiment} containing this <code>ExperimentalObject</code>. 
	 * Should return <code>null</code> if it has not yet been loaded 
	 * (using {@link #loadExperiment()}), or set (using 
	 * {@link setExperiment(Experiment)})
	 * 
	 * @return	the <code>Experiment</code> containing this <code>ExperimentalObject</code>.
	 */
	public T getExperiment() {
		return this.experiment;
	}

	/**
	 * Set the {@link Experiment} containing this <code>ExperimentalObject</code>. 
	 * @param 	experiment The <code>Experiment</code> to set.
	 */
	public void setExperiment(T experiment) {
		this.experiment = experiment;
	}

}
