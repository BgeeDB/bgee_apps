package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GOTermTO;
import org.bgee.model.dao.api.gene.GOTermTO.Domain;
import org.bgee.model.dao.api.gene.GeneOntologyDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

public class MySQLGeneOntologyDAO extends MySQLDAO<GeneOntologyDAO.Attribute> 
    implements GeneOntologyDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLGeneOntologyDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLGeneOntologyDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }


    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************
    /**
     * Inserts the provided Gene Ontology terms into the Bgee database, represented as 
     * a {@code Collection} of {@code GOTermTO}s.
     * 
     * @param terms     a {@code Collection} of {@code GOTermTO}s to be inserted 
     *                  into the database.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code terms}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertTerms(Collection<GOTermTO> terms) throws DAOException {
        log.entry(terms);

        String sql = "Insert into geneOntologyTerm (GOId, GOTerm, GODomain) values ";
        for (int i = 0; i < terms.size(); i++) {
            if (i > 0) {
                sql += ", ";
            }
            sql += "(?, ?, ?) ";
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            int paramIndex = 1;
            for (GOTermTO termTO: terms) {
                stmt.setString(paramIndex, termTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, termTO.getName());
                paramIndex++;
                stmt.setString(paramIndex, this.domainToString(termTO.getDomain()));
                paramIndex++;
            }
            return log.exit(stmt.executeUpdate());

        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Convert a {@code GOTermTO.Domain} into a {@code String} suitable for insertion 
     * into the database. This inserted {@code String} is likely to be specific 
     * to the data source used as storage, so this information is present in this 
     * {@code DAO}, not in the {@code TransferObject}.
     * 
     * @param domain    The {@code GOTermTO.Domain} to be transformed into a {@code String} 
     *                  for insertion into the database.
     * @return          A {@code String} corresponding to {@code domain}, to be inserted 
     *                  into the database.
     */
    String domainToString(Domain domain) {
        switch (domain) {
        case BP: 
            return "biological process";
        case CC: 
            return "cellular component";
        case MF: 
            return "molecular function";
        default: 
            throw new AssertionError("The domain " + domain + " is not recognised, " +
            		"or not used in the database"); 
        }
            
            
    }
}
