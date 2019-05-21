package org.bgee.model.anatdev.multispemapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.species.Taxon;

/**
 * Represents the supporting information for a relation of similarity between anatomical entities.
 * Used in {@link AnatEntitySimilarity} objects, to provide the summary support information
 * for a similarity relation in a specific taxon (all raw annotations for a 'relation' in a 'taxon'
 * are summarized to provide an overall representation of them).
 *
 * @author Frederic Bastian
 * @version Bgee 14 Mar 2019
 * @see AnatEntitySimilarity
 * @since Bgee 14 Mar 2019
 */
//TODO: add management of ECO term and CIO term
//TODO: add management of raw annotations (with annotator info, ref ID, supporting text, etc)
public class AnatEntitySimilarityTaxonSummary {
    private final static Logger log = LogManager.getLogger(AnatEntitySimilarityTaxonSummary.class.getName());

    private final Taxon taxon;
    //TODO: this could be replace by an CIO term attribute, that will allow to retrieve
    //the trust status
    private final boolean trusted;
    private final boolean positive;

    public AnatEntitySimilarityTaxonSummary(Taxon taxon, boolean trusted, boolean positive) {
        if (taxon == null) {
            throw log.throwing(new IllegalArgumentException("Taxon cannot be null."));
        }
        this.taxon = taxon;
        this.trusted = trusted;
        this.positive = positive;
    }

    /**
     * @return  The {@code Taxon} that was used for annotating this relation of similarity
     *          for anatomical entity.
     */
    public Taxon getTaxon() {
        return taxon;
    }
    /**
     * @return  A {@code boolean} that is {@code true} if the annotation have enough support
     *          to be considered reliable, {@code false} otherwise.
     */
    public boolean isTrusted() {
        return trusted;
    }
    /**
     * @return  A {@code boolean} that is {@code true} if the annotation is positive (to report
     *          the existence of a relation of similarity for anatomical entity), {@code false}
     *          if the annotation is negated (to report the absence of a relation of similarity
     *          that would otherwise seem plausible).
     */
    public boolean isPositive() {
        return positive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (positive ? 1231 : 1237);
        result = prime * result + ((taxon == null) ? 0 : taxon.hashCode());
        result = prime * result + (trusted ? 1231 : 1237);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AnatEntitySimilarityTaxonSummary other = (AnatEntitySimilarityTaxonSummary) obj;
        if (positive != other.positive) {
            return false;
        }
        if (taxon == null) {
            if (other.taxon != null) {
                return false;
            }
        } else if (!taxon.equals(other.taxon)) {
            return false;
        }
        if (trusted != other.trusted) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AnatEntitySimilarityTaxonSummary [taxon=").append(taxon)
               .append(", trusted=").append(trusted)
               .append(", positive=").append(positive)
               .append("]");
        return builder.toString();
    }
}
