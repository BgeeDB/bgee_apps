package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.dao.api.EntityTO;

/**
 * {@code EntityTO} representing a Gene Ontology term in the Bgee database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GOTermTO extends EntityTO {
	private static final long serialVersionUID = 5833418034550328328L;

	/**
     * An {@code enum} listing the different domains a GO term can be attributed to 
     * in the Bgee database.
     * <ul>
     * <li>BP: Biological Process
     * <li>CC: Cellular Component
     * <li>MF: Molecular Function
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum Domain {
        BP, CC, MF;
    }
    
    /**
     * The {@link Domain} that this Gene Ontology term belongs to.
     */
    private final Domain domain;
    /**
     * A {@code Set} of {@String}s that are the alternative IDs of this GO term. 
     * For instance, {@code GO:0035083} is an alternative ID to {@code GO:0035082}.
     * Note that this {@code Set} is made unmodifiable at instantiation, after having been 
     * populated. 
     */
    private final Set<String> altIds;
    
    /**
     * Constructor providing the ID (for instance, {@code GO:2001316}), the name 
     * (also known as label, for instance, {@code secretory granule lumen}), and 
     * the {@link Domain} of this Gene Ontology term.
     * 
     * @param id        a {@code String} that is the ID of this GO term.
     * @param name      a {@code String} that is the name (or label) of this GO term.
     * @param domain    a {@code Domain} which this GO term belongs to.
     */
    public GOTermTO(String id, String name, Domain domain) {
        this(id, name, domain, null);
    }
    /**
     * Constructor providing the ID (for instance, {@code GO:2001316}), the name 
     * (also known as label, for instance, {@code secretory granule lumen}),  
     * the {@link Domain} of this Gene Ontology term, and some alternative IDs 
     * for this GO term (for instance, {@code GO:0035083} is an alternative ID 
     * to {@code GO:0035082}).
     * 
     * @param id        a {@code String} that is the ID of this GO term.
     * @param name      a {@code String} that is the name (or label) of this GO term.
     * @param domain    a {@code Domain} which this GO term belongs to.
     * @param altIds    a {@code Collection} of {@code String}s that are the alternative IDs 
     *                  of this GO term.
     */
    public GOTermTO(String id, String name, Domain domain, Collection<String> altIds) {
        super(id, name);
        this.domain = domain;
        Set<String> tempAltIds;
        if (altIds != null) {
            tempAltIds = new HashSet<String>(altIds);
        } else {
            tempAltIds = new HashSet<String>();
        }
        this.altIds = Collections.unmodifiableSet(tempAltIds);
    }
    
    /**
     * @return  The {@code String} that is the label of this term 
     *          (for instance, "secretory granule lumen").
     *          Corresponds to the DAO {@code Attribute} {@link GeneOntologyDAO.Attribute 
     *          LABEL}. Returns {@code null} if value not set.
     */
    @Override
    public String getName() {
        //method overridden only to provide a more accurate javadoc
        return super.getName();
    }
    /**
     * @return  The {@link Domain} that this Gene Ontology term belongs to.
     */
    public Domain getDomain() {
        return this.domain;
    }
    
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the alternative IDs 
     *          of this GO term. For instance, {@code GO:0035083} is an alternative ID 
     *          to {@code GO:0035082}.
     */
    public Set<String> getAltIds() {
        return this.altIds;
    }

    @Override
    public String toString() {
        return "ID: " + this.getId() + " - Label: " + this.getName() + 
                " - Domain: " + this.getDomain() + 
                ((this.getAltIds().isEmpty()) ? "" : " - AltIds: " + this.getAltIds());
    }
}
