package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneOntologyDAO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO.Domain;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code GeneOntologyDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO
 * @since Bgee 13
 */
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
    @Override
    public int insertTerms(Collection<GOTermTO> terms) throws DAOException, IllegalArgumentException {
        log.entry(terms);

        if (terms == null || terms.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No term is given, then no term is inserted"));
        }
        
        //to not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        //and because of laziness, we insert terms one at a time
        int termInsertedCount = 0;
        String sql = "Insert into geneOntologyTerm (goId, goTerm, goDomain) values (?, ?, ?) ";
        
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            for (GOTermTO termTO: terms) {
                //insert GO term
                stmt.setString(1, termTO.getId());
                stmt.setString(2, termTO.getName());
                stmt.setString(3, this.domainToString(termTO.getDomain()));
                termInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
                
                //insert altIds
                if (!termTO.getAltIds().isEmpty()) {
                    String altIdSql = "insert into geneOntologyTermAltId (goId, goAltId) values ";
                    for (int i = 0; i < termTO.getAltIds().size(); i++) {
                        if (i > 0) {
                            altIdSql += ", ";
                        }
                        altIdSql += "(?, ?)";
                    }
                    try (BgeePreparedStatement altIdStmt = 
                            this.getManager().getConnection().prepareStatement(altIdSql)) {
                        int paramIndex = 1;
                        for (String altId: termTO.getAltIds()) {
                            altIdStmt.setString(paramIndex, termTO.getId());
                            paramIndex++;
                            altIdStmt.setString(paramIndex, altId);
                            paramIndex++;
                        }
                        altIdStmt.executeUpdate();
                        altIdStmt.clearParameters();
                    }
                }
            }
            return log.traceExit(termInsertedCount);
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
