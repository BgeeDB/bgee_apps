package org.bgee.model.dao.api.source;

import java.io.Serializable;
import java.util.Date;

import org.bgee.model.dao.api.TransferObject;

/**
 * {@code TransferObject} representing a source of data in the Bgee data source.
 * <p>
 * For information on this {@code TransferObject} and its fields, 
 * see the corresponding class in the {@code bgee-core} module.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 11
 */
public final class SourceTO extends TransferObject implements Serializable
{
	private static final long serialVersionUID = -4966619139786311073L;
	
	public String xRefUrl;
	public String experimentUrl;
	public String evidenceUrl;
	public String baseUrl;
    
	public Date releaseDate;  
	public String releaseVersion;
    
	public boolean toDisplay;
	public String category;

	public String dataSourceDescription;

	public SourceTO() {
		super();
		this.xRefUrl               = null;
		this.experimentUrl         = null;
		this.evidenceUrl           = null;
		this.baseUrl               = null;
		this.releaseDate           = null;
		this.releaseVersion        = null;
		this.toDisplay             = false;
		this.category              = null;
		this.dataSourceDescription = null;
	}
}
