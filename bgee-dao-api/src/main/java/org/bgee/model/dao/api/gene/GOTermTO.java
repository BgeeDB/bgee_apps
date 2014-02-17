package org.bgee.model.dao.api.gene;

import org.bgee.model.dao.api.EntityTO;

/**
 * {@code EntityTO} representing a Gene Ontology term in the Bgee database.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GOTermTO extends EntityTO {
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
     * Constructor providing the ID (for instance, {@code GO:2001316}), the name 
     * (also known as label, for instance, {@code secretory granule lumen}), and 
     * the {@link Domain} of this Gene Ontology term.
     * 
     * @param id
     * @param name
     * @param domain
     */
    public GOTermTO(String id, String name, Domain domain) {
        super(id, name);
        this.domain = domain;
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

    @Override
    public String toString() {
        return "ID: " + this.getId() + " - Label: " + this.getName() + 
                " - Domain: " + this.getDomain();
    }
}
