package org.bgee.model.dao.api.source;

import java.util.Collection;
import java.util.List;

import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DataAccessException;

/**
 * DAO defining queries using or retrieving {@link SourceTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see SourceTO
 * @since Bgee 11
 */
public interface SourceDAO 
{
	/**
     * Return all sources used in Bgee as a {@code Collection} 
     * of {@code SourceTO}s, retrieved from the data source.
     * 
     * @return 	a {@code Collection} of {@code SourceTO}, 
     * 			representing the sources used in Bgee.
     * @throws DataAccessException 	If an error occurred when accessing the data source. 
     */
	public Collection<SourceTO> getAllDataSources() throws DataAccessException;

	/**
     * Return sources used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from, as a {@code Collection} 
     * of {@code SourceTO}s ordered by their {@code categoryId}.
     * 
     * @return 	a {@code Collection} of {@code SourceTO},  
     * 			representing the sources where some Bgee data actually come from, 
     * 			order by their {@code categoryId}.
     * @throws DataAccessException 	If an error occurred when accessing the data source.
     */
	public List<TransferObject> getDisplayableDataSources() throws DataAccessException;

	/**
     * Retrieve a data source (e.g., ArrayExpress) by its ID, 
     * and return it as a {@code SourceTO} object.
     * 
     * @param dataSourceId 	a {@code String} representing the ID of the data source 
     * 						to retrieve
     * @return				a {@code SourceTO}, corresponding to {@code dataSourceId}
     * @throws DataAccessException 	If an error occurred when accessing the data source.
     */
	public SourceTO getDataSourceById(String dataSourceId) throws DataAccessException;
}
