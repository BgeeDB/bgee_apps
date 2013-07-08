package org.bgee.model.dao.api.source;

import java.util.Collection;
import java.util.List;

import org.bgee.model.dao.api.TransferObject;

/**
 * DAO used to retrieve {@link SourceTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see SourceTO
 * @since Bgee 11
 */
public interface SourceDAO 
{
	/**
     * Return all sources used in Bgee as a <code>Collection</code> 
     * of <code>SourceTO</code>s, retrieved from the data source.
     * 
     * @return 	a <code>Collection</code> of <code>SourceTO</code>, 
     * 			representing the sources used in Bgee.
     */
	public Collection<SourceTO> getAllDataSources();

	/**
     * Return sources used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from, as a <code>Collection</code> 
     * of <code>SourceTO</code>s ordered by their <code>categoryId</code>.
     * 
     * @return 	a <code>Collection</code> of <code>SourceTO</code>,  
     * 			representing the sources where some Bgee data actually come from, 
     * 			order by their <code>categoryId</code>.
     */
	public List<TransferObject> getDisplayableDataSources();

	/**
     * Retrieve a data source (e.g., ArrayExpress) by its ID, 
     * and return it as a <code>SourceTO</code> object.
     * 
     * @param dataSourceId 	a <code>String</code> representing the ID of the data source 
     * 						to retrieve
     * @return				a <code>SourceTO</code>, corresponding to <code>dataSourceId</code>
     */
	public SourceTO getDataSourceById(String dataSourceId);
}
