package org.bgee.model.dao.api.ontologycommon;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link EvidenceOntologyTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see EvidenceOntologyTO
 * @since Bgee 13
 */
public interface EvidenceOntologyDAO extends DAO<EvidenceOntologyDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code ECOTermTO}s 
     * obtained from this {@code EvidenceOntologyDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link EvidenceOntologyDAO#getId()}.
     * <li>{@code NAME}: corresponds to {@link EvidenceOntologyDAO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link EvidenceOntologyDAO#getDescription()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION;
    }
    
    /**
     * Retrieves all Evidence Ontology terms from data source.
     * <p>
     * The Evidence Ontology terms are retrieved and returned as an {@code ECOTermTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are 
     * retrieved.
     * 
     * @return  An {@code ECOTermTOResultSet} containing all Evidence Ontology terms
     *          from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public ECOTermTOResultSet getAllECOTerms() throws DAOException;

    /**
     * Inserts the provided Evidence Ontology terms into the data source, 
     * represented as a {@code Collection} of {@code ECOTermTO}s. 
     * 
     * @param ecoTermsTOs   A {@code Collection} of {@code ECOTermTO}s to be inserted into 
     *                      the data source.
     * @return              An {@code int} that is the number of inserted Evidence Ontology terms.
     * @throws IllegalArgumentException If {@code ecoTermsTOs} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying to insert 
     *                                  Evidence Ontology terms. The {@code SQLException} will be 
     *                                  wrapped into a {@code DAOException} ({@code DAO}s do not 
     *                                  expose these kind of implementation details).
     */
    public int insertECOTerms(Collection<ECOTermTO> ecoTermsTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code ECOTermTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface ECOTermTOResultSet extends DAOResultSet<ECOTermTO> {
    }

    /**
     * An {@code EntityTO} representing an Evidence Ontology term, as stored in the Bgee database. 
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class ECOTermTO extends EntityTO {

        private static final long serialVersionUID = 5801391933268876747L;

        /**
         * Constructor providing the ID, the name, and the description of the Evidence Ontology term.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id            A {@code String} that is the ID of this CIO term. 
         * @param name          A {@code String} that is the name of this CIO term.
         * @param description   A {@code String} that is the description of this CIO term.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public ECOTermTO(String id, String name, String description) 
                throws IllegalArgumentException {
            super(id, name, description);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
