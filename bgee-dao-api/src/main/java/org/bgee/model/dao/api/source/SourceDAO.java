package org.bgee.model.dao.api.source;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
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
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
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
	
    /**
     * {@code DAOResultSet} specifics to {@code SourceTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
	public interface SourceTOResultSet extends DAOResultSet<SourceTO> {
		
	}

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
	public final class SourceTO extends TransferObject
	{
		private static final long serialVersionUID = -4966619139786311073L;
		
		public String xRefUrl;
		public String experimentUrl;
		public String evidenceUrl;
		public String baseUrl;
	    
		public Date releaseDate;  
		public String releaseVersion;
	    
		public Boolean toDisplay;
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

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((baseUrl == null) ? 0 : baseUrl.hashCode());
            result = prime * result
                    + ((category == null) ? 0 : category.hashCode());
            result = prime
                    * result
                    + ((dataSourceDescription == null) ? 0
                            : dataSourceDescription.hashCode());
            result = prime * result
                    + ((evidenceUrl == null) ? 0 : evidenceUrl.hashCode());
            result = prime * result
                    + ((experimentUrl == null) ? 0 : experimentUrl.hashCode());
            result = prime * result
                    + ((releaseDate == null) ? 0 : releaseDate.hashCode());
            result = prime
                    * result
                    + ((releaseVersion == null) ? 0 : releaseVersion.hashCode());
            result = prime * result
                    + ((toDisplay == null) ? 0 : toDisplay.hashCode());
            result = prime * result
                    + ((xRefUrl == null) ? 0 : xRefUrl.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SourceTO)) {
                return false;
            }
            SourceTO other = (SourceTO) obj;
            if (baseUrl == null) {
                if (other.baseUrl != null) {
                    return false;
                }
            } else if (!baseUrl.equals(other.baseUrl)) {
                return false;
            }
            if (category == null) {
                if (other.category != null) {
                    return false;
                }
            } else if (!category.equals(other.category)) {
                return false;
            }
            if (dataSourceDescription == null) {
                if (other.dataSourceDescription != null) {
                    return false;
                }
            } else if (!dataSourceDescription
                    .equals(other.dataSourceDescription)) {
                return false;
            }
            if (evidenceUrl == null) {
                if (other.evidenceUrl != null) {
                    return false;
                }
            } else if (!evidenceUrl.equals(other.evidenceUrl)) {
                return false;
            }
            if (experimentUrl == null) {
                if (other.experimentUrl != null) {
                    return false;
                }
            } else if (!experimentUrl.equals(other.experimentUrl)) {
                return false;
            }
            if (releaseDate == null) {
                if (other.releaseDate != null) {
                    return false;
                }
            } else if (!releaseDate.equals(other.releaseDate)) {
                return false;
            }
            if (releaseVersion == null) {
                if (other.releaseVersion != null) {
                    return false;
                }
            } else if (!releaseVersion.equals(other.releaseVersion)) {
                return false;
            }
            if (toDisplay == null) {
                if (other.toDisplay != null) {
                    return false;
                }
            } else if (!toDisplay.equals(other.toDisplay)) {
                return false;
            }
            if (xRefUrl == null) {
                if (other.xRefUrl != null) {
                    return false;
                }
            } else if (!xRefUrl.equals(other.xRefUrl)) {
                return false;
            }
            return true;
        }
	}

}
