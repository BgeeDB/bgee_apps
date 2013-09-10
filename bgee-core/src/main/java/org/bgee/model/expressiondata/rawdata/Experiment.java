package org.bgee.model.expressiondata.rawdata;

import org.apache.commons.lang.StringUtils;
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
	 * Log4j2 <code>Logger</code> of this class.
	 */
	private final static Logger log = LogManager.getLogger(Experiment.class.getName());
	
	/**
	 * A <code>String</code> corresponding to the ID 
	 * of the {@link org.bgee.model.source.Source Source} of this <code>Experiment</code>. 
	 * It can be used to load the {@link #source} attribute after obtaining 
	 * this <code>Experiment</code>. 
	 * @see #source
	 */
    private String sourceId;
    /**
     * The <code>Source</code> of this <code>Experiment</code>.
     * @see #dataSourceId
     */
    private Source source;
    
	/**
     * Constructor providing the <code>id</code> of this <code>Experiment</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespace only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of this object.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespace only. 
     */
	public Experiment(String id) throws IllegalArgumentException {
		super(id);
		this.setSource(null);
		this.setSourceId(null);
	}

	/**
	 * Load the {@link org.bgee.model.source.Source Source} of this <code>Experiment</code>, 
	 * by obtaining it from a <code>DAO</code>, only if not already loaded 
	 * ({@link #getSource()} returning <code>null</code>).
	 * 
	 * @see #getSource()
	 * @see #getSourceId()
	 */
	public void loadSource()
	{
		log.entry();
		if (this.getSource() == null && StringUtils.isNotBlank(this.getSourceId())) {
		    log.debug("Load Source of Experiment {} with ID {}", this.getId(), 
		    		this.getSourceId());
			SourceFactory loader = new SourceFactory();
			this.setSource(
					loader.getSourceById(this.getSourceId()));
		}
		log.exit();
	}
	
	/**
	 * A helper to get the name of the {@link org.bgee.model.source.Source Source} 
	 * of this <code>Experiment</code>, if has been already loaded 
	 * (using {@link #loadSource()}) or set (using 
	 * {@link setSource(org.bgee.model.source.Source)}).
	 * Return an empty <code>String</code> if no the source was not found.
	 * 
	 * @return 	a <code>String</code> corresponding to the value returned by a call to 
	 * 			<code>getName()</code> of the <code>Source</code> of 
	 * 			this <code>Experiment/code>. Return an empty <code>String</code> 
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
	 * of this <code>Experiment</code>.
	 * 
	 * @return 	a <code>String</code> corresponding to the <code>Source</code> ID.
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
	 * of this <code>Experiment</code>. This is useful when retrieving 
	 * this <code>Experiment</code> from a <code>DAO</code>, before loading 
	 * the actual <code>Source</code>.
	 * 
	 * @param 	A <code>String</code> that is the ID of the <code>Source</code> 
	 * 			of this <code>Experiment</code>
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Get the {@link org.bgee.model.source.Source Source} of this <code>Experiment</code>. 
	 * Should return <code>null</code> if it has not yet been loaded 
	 * (using {@link #loadSource()}), or set (using 
	 * {@link setSource(org.bgee.model.source.Source)})
	 * 
	 * @return	the <code>Source</code> of this <code>Experiment</code>.
	 */
	public Source getSource() {
		return this.source;
	}

	/**
	 * Set the {@link org.bgee.model.source.Source Source} of this <code>Experiment</code>. 
	 * @param 	dataSource The <code>Source</code> to set.
	 */
	public void setSource(Source source) {
		this.source = source;
	}
}
