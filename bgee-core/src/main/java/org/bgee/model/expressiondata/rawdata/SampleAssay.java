package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.Entity;

/**
 * Represent experimental objects that allow to generate expression data, for instance: 
 * an Affymetrix chip, a RNA-Seq library, or an <em>in situ</em> image. 
 * Such objects: 
 * <ul>
 * <li>are always part of an {@link Experiment}. The precise type of the 
 * {@code Experiment} is provided by the {@code T} generic parameter.
 * <li>are always sources of {@link CallSource}s. 
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>	the precise type of the {@link Experiment} that contains 
 * 				this {@code SampleAssay}.
 */
public abstract class SampleAssay<T extends Experiment> extends Entity {
	
	/**
	 * A {@code String} corresponding to the ID 
	 * of the {@link Experiment} containing this {@code SampleAssay}. 
	 * It can be used to load the {@link #experiment} attribute after obtaining 
	 * this {@code SampleAssay}. 
	 * @see #experiment
	 */
    private String experimentId;
    /**
     * The {@link Experiment} containing this {@code SampleAssay}.
     * @see #experimentId
     */
    private T experiment;
	
	/**
     * Constructor providing the {@code id} of this {@code SampleAssay}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespace only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespace only. 
     */
	public SampleAssay(String id) throws IllegalArgumentException {
		super(id);
		this.setExperiment(null);
		this.setExperimentId(null);
	}
	
	/**
	 * Load the {@link Experiment} containing this {@code SampleAssay}, 
	 * by obtaining it from a {@code DAO}, only if not already loaded 
	 * ({@link #getExperiment()} returning {@code null}).
	 * 
	 * @see #getExperiment()
	 * @see #getExperimentId()
	 */
	public abstract void loadExperiment();
	
	/**
	 * Get the ID of the {@link Experiment} containing this {@code SampleAssay}.
	 * 
	 * @return 	a {@code String} corresponding to the {@code Experiment} ID.
	 * @see #getExperiment()
	 */
	public String getExperimentId() {
		if (this.getExperiment() != null) {
			return this.getExperiment().getId();
		} 
		return this.experimentId;
	}

	/**
	 * Set the ID of the {@link Experiment} containing this {@code SampleAssay}. 
	 * This is useful when retrieving this {@code SampleAssay} 
	 * from a {@code DAO}, before loading the actual {@code Experiment}.
	 * 
	 * @param 	A {@code String} that is the ID of the {@code Experiment} 
	 * 			containing this {@code SampleAssay}
	 */
	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	/**
	 * Get the {@link Experiment} containing this {@code SampleAssay}. 
	 * Should return {@code null} if it has not yet been loaded 
	 * (using {@link #loadExperiment()}), or set (using 
	 * {@link setExperiment(Experiment)})
	 * 
	 * @return	the {@code Experiment} containing this {@code SampleAssay}.
	 */
	public T getExperiment() {
		return this.experiment;
	}

	/**
	 * Set the {@link Experiment} containing this {@code SampleAssay}. 
	 * @param 	experiment The {@code Experiment} to set.
	 */
	public void setExperiment(T experiment) {
		this.experiment = experiment;
	}

}
