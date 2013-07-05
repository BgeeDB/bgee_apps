package org.bgee.model.data.common.source;

import java.util.Collection;

import model.data.common.TransferObject;

public interface SourceDAO 
{
	/**
     * Return all the data sources used in Bgee as a <code>Collection</code> of <code>TransferObject</code> , 
     * retrieved from a data source.
     * 
     * @return 	a <code>Collection</code> of <code>TransferObject</code> 
     *          castable to <code>SourceTO</code> objects, 
     * 			representing the data sources used in Bgee.
     * @see SourceTO
     */
	public Collection<TransferObject> getAllDataSources();

	/**
     * Return data sources used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from, as a <code>Collection</code> of <code>TransferObject</code>, 
     * retrieved from a data source, castable to <code>SourceTO</code> objects, 
     * and ordered by their category.
     * 
     * @return 	a <code>Collection</code> of <code>TransferObject</code>, 
     * 			castable to <code>SourceTO</code> objects, 
     * 			representing the data sources where some Bgee data actually come from, 
     * 			order by their category.
     * @see SourceTO
     */
	public Collection<TransferObject> getDisplayableDataSources();

	/**
     * Retrieve a data source (e.g., ArrayExpress) by its ID, 
     * and return it as a <code>SourceTO</code> object.
     * 
     * @param dataSourceId 	a <code>String</code> representing the ID of the data source to retrieve
     * @return				a <code>SourceTO</code>, corresponding to <code>dataSourceId</code>
     * @see SourceTO
     */
	public SourceTO getDataSourceById(String dataSourceId);
}
