package org.bgee.model.dao.api.source;

import java.util.Collection;
import java.util.List;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SourceTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see SourceTO
 * @since Bgee 11
 */
public interface SourceDAO extends DAO<SourceDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code SourceTO}s 
     * obtained from this {@code SourceDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link SourceTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link SourceTO#getName()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME;
    }
	/**
     * Return all sources used in Bgee as a {@code Collection} 
     * of {@code SourceTO}s, retrieved from the data source.
     * 
     * @return 	a {@code Collection} of {@code SourceTO}, 
     * 			representing the sources used in Bgee.
     * @throws DAOException 	If an error occurred when accessing the data source. 
     */
	public Collection<SourceTO> getAllDataSources() throws DAOException;

	/**
     * Return sources used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from, as a {@code Collection} 
     * of {@code SourceTO}s ordered by their {@code categoryId}.
     * 
     * @return 	a {@code Collection} of {@code SourceTO},  
     * 			representing the sources where some Bgee data actually come from, 
     * 			order by their {@code categoryId}.
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
	public List<TransferObject> getDisplayableDataSources() throws DAOException;

	/**
     * Retrieve a data source (e.g., ArrayExpress) by its ID, 
     * and return it as a {@code SourceTO} object.
     * 
     * @param dataSourceId 	a {@code String} representing the ID of the data source 
     * 						to retrieve
     * @return				a {@code SourceTO}, corresponding to {@code dataSourceId}
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
	public SourceTO getDataSourceById(String dataSourceId) throws DAOException;
}
