package org.bgee.model.dao.api.ontologycommon;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link CIOStatementTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see CIOStatementTO
 * @since Bgee 13
 */
public interface CIOStatementDAO extends DAO<CIOStatementDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code CIOStatementTO}s 
     * obtained from this {@code CIOStatementDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link CIOStatementDAO#getId()}.
     * <li>{@code NAME}: corresponds to {@link CIOStatementDAO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link CIOStatementDAO#getDescription()}.
     * <li>{@code TRUSTED}: corresponds to {@link CIOStatementDAO#isTrusted()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, TRUSTED;
    }
    
    /**
     * Retrieves all CIO statements from data source.
     * <p>
     * The CIO statements are retrieved and returned as a {@code CIOStatementTOResultSet}. It is 
     * the responsibility of the caller to close this {@code DAOResultSet} once results are 
     * retrieved.
     * 
     * @return              An {@code CIOStatementTOResultSet} containing all CIO statements
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public CIOStatementTOResultSet getAllCIOStatements() throws DAOException;

    /**
     * Inserts the provided CIO statements into the data source, 
     * represented as a {@code Collection} of {@code CIOStatementTO}s. 
     * 
     * @param cioStatementTOs   A {@code Collection} of {@code CIOStatementTO}s to be inserted into 
     *                          the data source.
     * @return                  An {@code int} that is the number of inserted CIO statements.
     * @throws IllegalArgumentException If {@code cioStatementTOs} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying to insert 
     *                                  CIO statements. The {@code SQLException} will be 
     *                                  wrapped into a {@code DAOException} ({@code DAO}s do not 
     *                                  expose these kind of implementation details).
     */
    public int insertCIOStatements(Collection<CIOStatementTO> cioStatementTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code CIOStatementTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface CIOStatementTOResultSet extends DAOResultSet<CIOStatementTO> {
    }

    /**
     * An {@code EntityTO} representing a CIO term, as stored in the Bgee database. 
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class CIOStatementTO extends EntityTO {

        private static final long serialVersionUID = 7509933615802695073L;

        /**
         * A {@code Boolean} defining whether this CIO term is used to capture a trusted evidence 
         * line ({@code true}), or whether it indicates that the evidence should not be trusted 
         * ({@code false}).
         */
        private final Boolean trusted;

        /**
         * Constructor providing the ID, the name, the description, and the {@code Boolean} defining
         * whether this CIO term is used to capture a trusted evidence line, or whether it indicates
         * that the evidence should not be trusted.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id            A {@code String} that is the ID of this CIO term. 
         * @param name          A {@code String} that is the name of this CIO term.
         * @param description   A {@code String} that is the description of this CIO term.
         * @param trusted       A {@code Boolean} defining whether this CIO term is used to capture 
         *                      a trusted evidence line ({@code true}), or whether it indicates that 
         *                      the evidence should not be trusted ({@code false}).
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public CIOStatementTO(String id, String name, String description, Boolean trusted) 
                throws IllegalArgumentException {
            super(id, name, description);
            this.trusted = trusted;
        }

        /**
         * @return  the {@code Boolean} defining whether this CIO term is used to capture a trusted 
         *          evidence line ({@code true}), or whether it indicates that the evidence should 
         *          not be trusted ({@code false}).
         */
        public Boolean isTrusted() {
            return this.trusted;
        }

        @Override
        public String toString() {
            return super.toString() + " - Trusted: " + trusted;
        }
    }
}
