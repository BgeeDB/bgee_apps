package org.bgee.model.dao.api.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link GOTermTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see GOTermTO
 * @since Bgee 13
 */
public interface GeneOntologyDAO extends DAO<GeneOntologyDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GOTermTO}s 
     * obtained from this {@code GeneOntologyDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link GOTermTO#getId()}.
     * <li>{@code LABEL}: corresponds to {@link GOTermTO#getName()}.
     * <li>{@code DOMAIN}: corresponds to {@link GOTermTO#getDomain()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, LABEL, DOMAIN;
    }
    
    /**
     * Inserts the provided Gene Ontology terms into the Bgee database, represented as 
     * a {@code Collection} of {@code GOTermTO}s. Note that this method will also 
     * insert the alternative IDs of each term, if any (see {@code GOTermTO#getAltIds()}).
     * 
     * @param terms     a {@code Collection} of {@code GOTermTO}s to be inserted 
     *                  into the database.
     * @throws IllegalArgumentException If {@code terms} is empty or null. 
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code terms}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertTerms(Collection<GOTermTO> terms) throws DAOException, IllegalArgumentException;
    
    /**
     * {@code DAOResultSet} specifics to {@code GOTermTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
	public interface GOTermTOResultSet extends DAOResultSet<GOTermTO> {
		
	}

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
         * A {@code Set} of {@code String}s that are the alternative IDs of this GO term. 
         * For instance, {@code GO:0035083} is an alternative ID to {@code GO:0035082}.
         * Note that this {@code Set} is made unmodifiable at instantiation, after having been 
         * populated. 
         */
        private final Set<String> altIds;
        
        /**
         * Constructor providing the ID (for instance, {@code GO:2001316}), the name 
         * (also known as label, for instance, {@code secretory granule lumen}), and 
         * the {@link Domain} of this Gene Ontology term.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id        a {@code String} that is the ID of this GO term.
         * @param name      a {@code String} that is the name (or label) of this GO term.
         * @param domain    a {@code Domain} which this GO term belongs to.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public GOTermTO(String id, String name, Domain domain) throws IllegalArgumentException {
            this(id, name, domain, null);
        }
        /**
         * Constructor providing the ID (for instance, {@code GO:2001316}), the name 
         * (also known as label, for instance, {@code secretory granule lumen}),  
         * the {@link Domain} of this Gene Ontology term, and some alternative IDs 
         * for this GO term (for instance, {@code GO:0035083} is an alternative ID 
         * to {@code GO:0035082}).
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id        a {@code String} that is the ID of this GO term.
         * @param name      a {@code String} that is the name (or label) of this GO term.
         * @param domain    a {@code Domain} which this GO term belongs to.
         * @param altIds    a {@code Collection} of {@code String}s that are the alternative IDs 
         *                  of this GO term.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public GOTermTO(String id, String name, Domain domain, Collection<String> altIds) 
                throws IllegalArgumentException{
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

}
