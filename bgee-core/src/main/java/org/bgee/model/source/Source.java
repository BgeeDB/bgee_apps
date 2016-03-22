package org.bgee.model.source;

import java.util.Date;

import org.bgee.model.NamedEntity;

/**
 * Class allowing to describe data sources. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13, Mar. 2016
 */
public class Source extends NamedEntity {

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
     * Constructor providing the {@code id} of this {@code Source}.
     * <p>
     * This {@code id} cannot be {@code null}, or blank,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @throws IllegalArgumentException If {@code id} is {@code null}, or blank. 
     */
    public Source(String id) throws IllegalArgumentException {
        super(id);
    }
    
    /**
     * Constructor providing the {@code id}, {@code name}, and {@code description} of this {@code Source}.
     * <p> 
     * This {@code id} cannot be {@code null}, or blank, 
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id            A {@code String} representing the ID of this object.
     * @param name          A {@code String} representing the name of the data source.
     * @param description   A {@code String} representing the description of the data source.
     * @throws IllegalArgumentException     if {@code id} is {@code null}, or blank.
     */
    public Source(String id, String name, String description) throws IllegalArgumentException {
        super(id, name, description);
    }

    /**
     * Constructor providing the {@code id}, {@code name}, and {@code description} of this {@code Source}.
     * <p> 
     * This {@code id} cannot be {@code null}, or blank, 
     * otherwise an {@code IllegalArgumentException} will be thrown.
     * 
     * @param id                A {@code String} that is the ID of the data source.
     * @param name              A {@code String} that is the name of the data source.
     * @param description       A {@code String} that is the description of the data source.
     * @param xRefUrl           A {@code String} that is the URL for cross-references to data source.
     * @param experimentUrl     A {@code String} that is the URL to experiment (for expression data source).
     * @param evidenceUrl       A {@code String} that is the URL to evidence (for expression data source).
     * @param baseUrl           A {@code String} that is the URL to the home page of the data source.
     * @param releaseDate       A {@code String} that is the date of data source used.
     * @param releaseVersion    A {@code String} that is the version of data source used.
     * @param toDisplay         A {@code Boolean} defining whether the data source should be displayed.
     * @param category          A {@code Category} that is the data source category.
     * @param displayOrder      An {@code Integer} that is the data source display ordering.
     * @throws IllegalArgumentException     If {@code id} is {@code null}, or blank.
     */
    public Source(String id, String name, String description, String xRefUrl,
            String experimentUrl, String evidenceUrl, String baseUrl, Date releaseDate,
            String releaseVersion, Boolean toDisplay, SourceCategory category, Integer displayOrder)
                    throws IllegalArgumentException {
        super(id, name, description);
        this.xRefUrl = xRefUrl;
        this.experimentUrl = experimentUrl;
        this.evidenceUrl = evidenceUrl;
        this.baseUrl = baseUrl;
        this.releaseDate = releaseDate;
        this.releaseVersion = releaseVersion;
        this.toDisplay = toDisplay;
        this.category = category;
        this.displayOrder = displayOrder;
    }

    public String getxRefUrl() {
        return xRefUrl;
    }

    public String getExperimentUrl() {
        return experimentUrl;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public Boolean getToDisplay() {
        return toDisplay;
    }

    public SourceCategory getCategory() {
        return category;
    }

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
        Source other = (Source) obj;
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
        return super.toString() + " - X-ref URL: " + getxRefUrl() + 
                " - Experiment URL: " + getExperimentUrl() + " - Evidence URL: " + getEvidenceUrl() + 
                " - Base URL: " + getBaseUrl() + " - Release date: " + getReleaseDate() + 
                " - Release version: " + getReleaseVersion() + " - To display: " + getToDisplay() + 
                " - Category: " + getCategory() + " - Display order: " + getDisplayOrder();
    }
}
