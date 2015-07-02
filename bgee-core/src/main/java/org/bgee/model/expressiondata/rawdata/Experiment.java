package org.bgee.model.expressiondata.rawdata;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceFactory;

/**
 * Represent an experiment conducted to generate expression data.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class Experiment extends Entity {

	/**
	 * Log4j2 {@code Logger} of this class.
	 */
	private final static Logger log = LogManager.getLogger(Experiment.class.getName());
	
	/**
	 * A {@code String} corresponding to the ID 
	 * of the {@link org.bgee.model.source.Source Source} of this {@code Experiment}. 
	 * It can be used to load the {@link #source} attribute after obtaining 
	 * this {@code Experiment}. 
	 * @see #source
	 */
    private String sourceId;
    /**
     * The {@code Source} of this {@code Experiment}.
     * @see #dataSourceId
     */
    private Source source;
    
	/**
     * Constructor providing the {@code id} of this {@code Experiment}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespace only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespace only. 
     */
	public Experiment(String id) throws IllegalArgumentException {
		super(id);
		this.setSource(null);
		this.setSourceId(null);
	}

	/**
	 * Load the {@link org.bgee.model.source.Source Source} of this {@code Experiment}, 
	 * by obtaining it from a {@code DAO}, only if not already loaded 
	 * ({@link #getSource()} returning {@code null}).
	 * 
	 * @see #getSource()
	 * @see #getSourceId()
	 */
	public void loadSource()
	{
	    //TODO
//		log.entry();
//		if (this.getSource() == null && StringUtils.isNotBlank(this.getSourceId())) {
//		    log.debug("Load Source of Experiment {} with ID {}", this.getId(), 
//		    		this.getSourceId());
//			SourceFactory loader = new SourceFactory();
//			this.setSource(
//					loader.getSourceById(this.getSourceId()));
//		}
//		log.exit();
	}
	
	/**
	 * A helper to get the name of the {@link org.bgee.model.source.Source Source} 
	 * of this {@code Experiment}, if has been already loaded 
	 * (using {@link #loadSource()}) or set (using 
	 * {@link setSource(org.bgee.model.source.Source)}).
	 * Return an empty {@code String} if no the source was not found.
	 * 
	 * @return 	a {@code String} corresponding to the value returned by a call to 
	 * 			{@code getName()} of the {@code Source} of 
	 * 			this {@code Experiment/code>. Return an empty <code>String} 
	 * 			if the source was not set.
	 * @see #getSource()
	 */
	public String getSourceName()
	{
		if (this.getSource() != null) {
			return this.getSource().getName();
		} 
		return "";
	}
	
	/**
	 * Get the ID of the {@link org.bgee.model.source.Source Source} 
	 * of this {@code Experiment}.
	 * 
	 * @return 	a {@code String} corresponding to the {@code Source} ID.
	 * @see #getSource()
	 */
	public String getSourceId() {
		if (this.getSource() != null) {
			return this.getSource().getId();
		} 
		return this.sourceId;
	}

	/**
	 * Set the ID of the {@link org.bgee.model.source.Source Source} 
	 * of this {@code Experiment}. This is useful when retrieving 
	 * this {@code Experiment} from a {@code DAO}, before loading 
	 * the actual {@code Source}.
	 * 
	 * @param 	A {@code String} that is the ID of the {@code Source} 
	 * 			of this {@code Experiment}
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Get the {@link org.bgee.model.source.Source Source} of this {@code Experiment}. 
	 * Should return {@code null} if it has not yet been loaded 
	 * (using {@link #loadSource()}), or set (using 
	 * {@link setSource(org.bgee.model.source.Source)})
	 * 
	 * @return	the {@code Source} of this {@code Experiment}.
	 */
	public Source getSource() {
		return this.source;
	}

	/**
	 * Set the {@link org.bgee.model.source.Source Source} of this {@code Experiment}. 
	 * @param 	dataSource The {@code Source} to set.
	 */
	public void setSource(Source source) {
		this.source = source;
	}
}
