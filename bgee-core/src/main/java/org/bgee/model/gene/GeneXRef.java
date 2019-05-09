package org.bgee.model.gene;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.XRef;
import org.bgee.model.source.Source;

/**
 * Class allowing to describe a cross-reference for {@link Gene}s.
 *
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class GeneXRef extends XRef {
    private final static Logger log = LogManager.getLogger(GeneXRef.class.getName());

    /**
     * A {@code String} that is the public ID of the gene for this XRef,
     * used to generate some XRef URLs. We don't expose this attribute.
     */
    private final String publicGeneId;
    /**
     * A {@code String} that is the scientific name of the species of the gene for this XRef,
     * used to generate some XRef URLs. We don't expose this attribute.
     */
    private final String speciesScientificName;

    public GeneXRef(String xRefId, String xRefName, Source source, String publicGeneId,
            String speciesScientificName) {
        super(xRefId, xRefName, source);
        if (StringUtils.isBlank(publicGeneId)) {
            throw log.throwing(new IllegalArgumentException("A gene ID must be provided"));
        }
        if (StringUtils.isBlank(speciesScientificName)) {
            throw log.throwing(new IllegalArgumentException("A species scientific name must be provided"));
        }
        this.publicGeneId = publicGeneId;
        this.speciesScientificName = speciesScientificName;
    }

    @Override
    public String getXRefUrl(boolean convertAmpersandToHTMLEntity, Function<String, String> urlEncode) {
        log.entry(convertAmpersandToHTMLEntity, urlEncode);

        assert !convertAmpersandToHTMLEntity ||
        !Source.GENE_TAG.contains("&") && !Source.SPECIES_SCIENTIFIC_NAME_TAG.contains("&");

        String xRefUrl = super.getXRefUrl(convertAmpersandToHTMLEntity, urlEncode);
        if (xRefUrl == null) {
            return log.exit(null);
        }
        if (xRefUrl.contains(Source.SPECIES_SCIENTIFIC_NAME_TAG)) {
            xRefUrl = xRefUrl.replace(Source.SPECIES_SCIENTIFIC_NAME_TAG,
                    urlEncode.apply(this.speciesScientificName.replace(" ", "_")));
        }
        if (xRefUrl.contains(Source.GENE_TAG)) {
            xRefUrl = xRefUrl.replace(Source.GENE_TAG, urlEncode.apply(this.publicGeneId));
        }
        return log.exit(xRefUrl);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((publicGeneId == null) ? 0 : publicGeneId.hashCode());
        result = prime * result + ((speciesScientificName == null) ? 0 : speciesScientificName.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GeneXRef other = (GeneXRef) obj;
        if (publicGeneId == null) {
            if (other.publicGeneId != null) {
                return false;
            }
        } else if (!publicGeneId.equals(other.publicGeneId)) {
            return false;
        }
        if (speciesScientificName == null) {
            if (other.speciesScientificName != null) {
                return false;
            }
        } else if (!speciesScientificName.equals(other.speciesScientificName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneXRef [xRefId=").append(this.getXRefId())
               .append(", xRefName=").append(this.getXRefName())
               .append(", source=").append(this.getSource())
               .append(", publicGeneId=").append(publicGeneId)
               .append(", speciesScientificName=").append(speciesScientificName)
               .append("]");
        return builder.toString();
    }
}