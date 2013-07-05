package org.bgee.model.dao.api.source;

import java.io.Serializable;
import java.util.Date;

import model.data.common.TransferObject;

/**
 * <code>TransferObject</code> for the class <code>DataSource</code>, 
 * to hold values of the fields retrieved from the data source for this class.
 * <p>
 * For information on this <code>TransferObject</code> and its fields, see the corresponding class.
 * 
 * @author Frederic Bastian
 * @version Bgee 11, June 2012
 * @see model.DataSource
 * @since Bgee 11
 *
 */
public class SourceTO extends TransferObject implements Serializable
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
