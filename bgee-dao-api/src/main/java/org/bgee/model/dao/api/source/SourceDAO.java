package org.bgee.model.dao.api.source;

import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SourceTO}s. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @see     SourceTO
 * @since   Bgee 11
 */
public interface SourceDAO extends DAO<SourceDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code SourceTO}s 
     * obtained from this {@code SourceDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link SourceTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link SourceTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link SourceTO#getDescription()}.
     * <li>{@code XREF_URL}: corresponds to {@link SourceTO#getXRefUrl()}.
     * <li>{@code EXPERIMENT_URL}: corresponds to {@link SourceTO#getExperimentUrl()}.
     * <li>{@code EVIDENCE_URL}: corresponds to {@link SourceTO#getEvidenceUrl()}.
     * <li>{@code BASE_URL}: corresponds to {@link SourceTO#getBaseUrl()}.
     * <li>{@code RELEASE_DATE}: corresponds to {@link SourceTO#getReleaseDate()}.
     * <li>{@code RELEASE_VERSION}: corresponds to {@link SourceTO#getReleaseVersion()}.
     * <li>{@code TO_DISPLAY}: corresponds to {@link SourceTO#getToDisplay()}.
     * <li>{@code CATEGORY}: corresponds to {@link SourceTO#getCategory()}.
     * <li>{@code DISPLAY_ORDER}: corresponds to {@link SourceTO#getDisplayOrder()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, XREF_URL, EXPERIMENT_URL, EVIDENCE_URL, BASE_URL, RELEASE_DATE,
        RELEASE_VERSION, TO_DISPLAY, CATEGORY, DISPLAY_ORDER;
    }
    
    /**
     * Return all sources used in Bgee from data source.
     * <p>
     * The sources are retrieved and returned as a {@code SourceTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param attributes    A {@code Collection} of {@code SourceDAO.Attribute}s defining the
     *                      attributes to populate in the returned {@code SourceTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code SourceTOResultSet} containing sources used in Bgee from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public SourceTOResultSet getAllDataSources(Collection<SourceDAO.Attribute> attributes)
            throws DAOException;

    /**
     * Return sources used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from.
     * 
     * @param attributes    A {@code Collection} of {@code SourceDAO.Attribute}s defining the
     *                      attributes to populate in the returned {@code SourceTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code SourceTOResultSet} containing sources used in Bgee,
     *                      that are not used only for xrefs purpose.
     * @throws DAOException If an error occurred when accessing the data source.
     */
    public SourceTOResultSet getDisplayableDataSources(Collection<SourceDAO.Attribute> attributes)
            throws DAOException;

    /**
     * Retrieve a data source (e.g., ArrayExpress) by its ID, and return it as a {@code SourceTO} object.
     * 
     * @param dataSourceId  A {@code String} representing the ID of the data source to retrieve.
     * @param attributes    A {@code Collection} of {@code SourceDAO.Attribute}s defining the
     *                      attributes to populate in the returned {@code SourceTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code SourceTO}, corresponding to {@code dataSourceId}.
     * @throws DAOException             If an error occurred when accessing the data source.
     * @throws IllegalStateException    If retrieved more than one source.
     */
    public SourceTO getDataSourceById(String dataSourceId, Collection<SourceDAO.Attribute> attributes)
            throws DAOException, IllegalStateException;

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
     * @author  Frederic Bastian
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Mar. 2016
     * @since   Bgee 11
     */
    public final class SourceTO extends EntityTO {

        private static final long serialVersionUID = -4966619139786311073L;

        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(SourceTO.class.getName());

        /**
         * An {@code Enum} used to define the data source category to organize the display.
         * 
         * <ul>
         * <li>{@code GENOMICS}: the category is a genomics database.
         * <li>{@code PROTEOMICS}: the category is a proteomics database.
         * <li>{@code IN_SITU}: the category is a <em>in situ</em> data source.
         * <li>{@code AFFYMETRIX}: the category is an Affymetrix data source.
         * <li>{@code EST}: the category is an EST data source.
         * <li>{@code RNA_SEQ}: the category is a RNA-Seq data source.
         * <li>{@code ONTOLOGY}: the category is an ontology.
         * </ul>
         */
        public enum SourceCategory implements EnumDAOField {
            NONE(""), GENOMICS("Genomics database"), PROTEOMICS("Proteomics database"),
            IN_SITU("In situ data source"), AFFYMETRIX("Affymetrix data source"), 
            EST("EST data source"), RNA_SEQ("RNA-Seq data source"), ONTOLOGY("Ontology");

            /**
             * Convert the {@code String} representation of a data source category (for instance, 
             * retrieved from a database) into a {@code Category}. Operation performed by calling
             * {@link TransferObject#convert(Class, String)} with {@code Category} as the 
             * {@code Class} argument, and {@code representation} as the {@code String} argument.
             * 
             * @param representation    A {@code String} representing a data state.
             * @return                  The {@code Category} corresponding to {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code Category}.
             */
            public static final SourceCategory convertToSourceCategory(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(SourceCategory.class, representation));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;

            /**
             * Constructor providing the {@code String} representation of this {@code Category}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to this {@code Category}.
             */
            private SourceCategory(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * A {@code String} that is the URL for cross-references to data source.
         */
        private String xRefUrl;

        /**
         * A {@code String} that is the URL to experiment if it is expression data source.
         * <p>
         * The parameter experiment ID is defined by the syntax [experiment_id].
         */
        private String experimentUrl;

        /**
         * A {@code String} that is the URL to evidence if it is expression data source (<em>in situ</em> 
         * evidence for <em>in situ</em> databases or Affymetrix chips for affymetrix data).
         * <p>
         * The parameter evidence ID is defined by the syntax [evidence_id].
         */
        private String evidenceUrl;

        /**
         * A {@code String} that is the URL to the home page of the data source.
         */
        private String baseUrl;

        /**
         * A {@code Date} that is the date of data source used.
         */
        private Date releaseDate;  

        /**
         * A {@code String} that is the version of data source used (e.g.: Ensembl 67, cvs version xxx).
         */
        private String releaseVersion;

        /**
         * A {@code Boolean} defining whether this data source should be displayed
         * on the page listing data sources.
         */
        private Boolean toDisplay;

        /**
         * A {@code Category} that is the data source category to organize the display.
         */
        private SourceCategory category;

        /**
         * An {@code Integer} that is the data source display ordering to organize the display.
         * <p>
         * Default value is the highest value, so that this field is the last to be displayed
         */
        private Integer displayOrder;

        /**
         * Constructor providing the ID, the name, the description, the URL for cross-references,
         * the URL to experiment for expression, the URL to evidence for expression, the URL to 
         * the home page, the date of data source used, the version, a {@code Boolean} defining
         * whether this data source should be displayed on the page listing data sources, 
         * the data source category, and the data source display ordering.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param sourceId          A {@code String} that is the ID of the data source.
         * @param sourceName        A {@code String} that is the name of the data source.
         * @param sourceDescription A {@code String} that is the description of the data source.
         * @param xRefUrl           A {@code String} that is the URL for cross-references to data source.
         * @param experimentUrl     A {@code String} that is the URL to experiment (for expression data source).
         * @param evidenceUrl       A {@code String} that is the URL to evidence (for expression data source).
         * @param baseUrl           A {@code String} that is the URL to the home page of the data source.
         * @param releaseDate       A {@code String} that is the date of data source used.
         * @param releaseVersion    A {@code String} that is the version of data source used.
         * @param toDisplay         A {@code Boolean} defining whether the data source should be displayed.
         * @param category          A {@code Category} that is the data source category.
         * @param displayOrder      An {@code Integer} that is the data source display ordering.
         */
        public SourceTO(String sourceId, String sourceName, String sourceDescription, String xRefUrl,
                String experimentUrl, String evidenceUrl, String baseUrl, Date releaseDate,
                String releaseVersion, Boolean toDisplay, SourceCategory category, Integer displayOrder) {
            super(sourceId, sourceName, sourceDescription);
            this.xRefUrl        = xRefUrl;
            this.experimentUrl  = experimentUrl;
            this.evidenceUrl    = evidenceUrl;
            this.baseUrl        = baseUrl;
            this.releaseDate    = releaseDate;
            this.releaseVersion = releaseVersion;
            this.toDisplay      = toDisplay;
            this.category       = category;
            this.displayOrder   = displayOrder;
        }
        
        /**
         * @return the {@code String} that is the URL for cross-references to data source.
         */
        public String getXRefUrl() {
            return xRefUrl;
        }

        /**
         * @return the {@code String} that is the URL to experiment (for expression data source).
         */
        public String getExperimentUrl() {
            return experimentUrl;
        }

        /**
         * @return the {@code String} that is the URL to evidence (for expression data source).
         */
        public String getEvidenceUrl() {
            return evidenceUrl;
        }

        /**
         * @return the {@code String} that is the URL to the home page of the data source.
         */
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * @return the {@code String} that is the date of data source used.
         */
        public Date getReleaseDate() {
            return releaseDate;
        }

        /**
         * @return the {@code String} that is the version of data source used.
         */
        public String getReleaseVersion() {
            return releaseVersion;
        }

        /**
         * @return the {@code Boolean} defining whether the data source should be displayed.
         */
        public Boolean isToDisplay() {
            return toDisplay;
        }

        /**
         * @return the {@code Category} that is the data source category.
         */
        public SourceCategory getSourceCategory() {
            return category;
        }

        /**
         * @return the {@code Integer} that is the data source display ordering.
         */
        public Integer getDisplayOrder() {
            return displayOrder;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
            result = prime * result + ((category == null) ? 0 : category.hashCode());
            result = prime * result + ((displayOrder == null) ? 0 : displayOrder.hashCode());
            result = prime * result + ((evidenceUrl == null) ? 0 : evidenceUrl.hashCode());
            result = prime * result + ((experimentUrl == null) ? 0 : experimentUrl.hashCode());
            result = prime * result + ((releaseDate == null) ? 0 : releaseDate.hashCode());
            result = prime * result + ((releaseVersion == null) ? 0 : releaseVersion.hashCode());
            result = prime * result + ((toDisplay == null) ? 0 : toDisplay.hashCode());
            result = prime * result + ((xRefUrl == null) ? 0 : xRefUrl.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            SourceTO other = (SourceTO) obj;
            if (baseUrl == null) {
                if (other.baseUrl != null)
                    return false;
            } else if (!baseUrl.equals(other.baseUrl))
                return false;
            if (category != other.category)
                return false;
            if (displayOrder == null) {
                if (other.displayOrder != null)
                    return false;
            } else if (!displayOrder.equals(other.displayOrder))
                return false;
            if (evidenceUrl == null) {
                if (other.evidenceUrl != null)
                    return false;
            } else if (!evidenceUrl.equals(other.evidenceUrl))
                return false;
            if (experimentUrl == null) {
                if (other.experimentUrl != null)
                    return false;
            } else if (!experimentUrl.equals(other.experimentUrl))
                return false;
            if (releaseDate == null) {
                if (other.releaseDate != null)
                    return false;
            } else if (!releaseDate.equals(other.releaseDate))
                return false;
            if (releaseVersion == null) {
                if (other.releaseVersion != null)
                    return false;
            } else if (!releaseVersion.equals(other.releaseVersion))
                return false;
            if (toDisplay == null) {
                if (other.toDisplay != null)
                    return false;
            } else if (!toDisplay.equals(other.toDisplay))
                return false;
            if (xRefUrl == null) {
                if (other.xRefUrl != null)
                    return false;
            } else if (!xRefUrl.equals(other.xRefUrl))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - XRefUrl: " + this.getXRefUrl() + 
                    " - Experiment URL: " + this.getExperimentUrl() + 
                    " - Evidence URL: " + this.getEvidenceUrl() + " - Base URL: " + this.getBaseUrl() + 
                    " - Release date: " + this.getReleaseDate() + " - Release version: " + this.getReleaseVersion() +
                    " - To display: " + this.isToDisplay() + " - Source category: " + this.getSourceCategory() + 
                    " - Display order: " + getDisplayOrder();
        }
    }
}
