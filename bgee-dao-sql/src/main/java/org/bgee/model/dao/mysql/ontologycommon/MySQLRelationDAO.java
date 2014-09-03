package org.bgee.model.dao.mysql.ontologycommon;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;

/**
 * A {@code RelationDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.RelationDAO.RelationTO
 * @since Bgee 13
 */
public class MySQLRelationDAO extends MySQLDAO<RelationDAO.Attribute> 
                                    implements RelationDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLRelationDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLRelationDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RelationTOResultSet getAllAnatEntityRelations(Set<String> speciesIds) {
        log.entry(speciesIds);
        // TODO Auto-generated method stub
        return log.exit(null);
    }

    @Override
    public int insertAnatEntityRelations(Collection<RelationTO> relationTOs) {
        log.entry(relationTOs);
        // TODO Auto-generated method stub
        return log.exit(0);
    }

    /**
     * Inserts the provided relations between Gene Ontology terms into the Bgee database, 
     * represented as a {@code Collection} of {@code RelationTO}s. 
     * 
     * @param relations         A {@code Collection} of {@code RelationTO}s to be inserted into the 
     *                          database.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code relations}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertGeneOntologyRelations(Collection<RelationTO> relations) throws DAOException {
        log.entry(relations);
        
        //to not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        //and because of laziness, we insert terms one at a time
        int relInsertedCount = 0;
        //TODO: this is where the new system appears to suck... continue here.
        String sql = "Insert into geneOntologyRelation (goAllTargetId, goAllSourceId) " +
                "values (?, ?) ";
        
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            
            for (RelationTO rel: relations) {
                stmt.setString(1, rel.getTargetId());
                stmt.setString(2, rel.getSourceId());
                relInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
            
            return log.exit(relInsertedCount);
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

}
