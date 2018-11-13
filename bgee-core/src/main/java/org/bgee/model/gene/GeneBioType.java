package org.bgee.model.gene;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class describing gene biotypes. As of Bgee 14, there is not much information about them,
 * but this class exists so tht we can add additional information in the future if needed
 * (e.g., Vega classification).
 *
 * @author Frederic Bastian
 * @version Bgee 14 Sep. 2018
 * @see Gene
 * @see <a target='_top' href='http://vega.archive.ensembl.org/info/about/gene_and_transcript_types.html'>http://vega.archive.ensembl.org/info/about/gene_and_transcript_types.html</a>
 * @since Bgee 14 Sep. 2018
 */
//Note: this class does not extend NamedEntity, because we don't want to expose
//the internal Bgee biotype IDs.
public class GeneBioType {
    private final static Logger log = LogManager.getLogger(GeneBioType.class.getName());

    private final String name;

    public GeneBioType(String name) {
        if (StringUtils.isBlank(name)) {
            throw log.throwing(new IllegalArgumentException("A biotype name mut be provided"));
        }
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(obj instanceof GeneBioType)) {
            return false;
        }
        GeneBioType other = (GeneBioType) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneBioType [name=").append(name).append("]");
        return builder.toString();
    }
}
