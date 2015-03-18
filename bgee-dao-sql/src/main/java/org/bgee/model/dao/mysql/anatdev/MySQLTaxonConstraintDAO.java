package org.bgee.model.dao.mysql.anatdev;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;


/**
 * A {@code TaxonConstraintDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO
 * @since Bgee 13
 */
public class MySQLTaxonConstraintDAO extends MySQLDAO<TaxonConstraintDAO.Attribute> 
                                     implements TaxonConstraintDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLTaxonConstraintDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLTaxonConstraintDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    /*
     * (non-javadoc)
     * All the insert methods of that class are not factorize in a single method because of 
     * table names, entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public int insertAnatEntityRelationTaxonConstraints(Collection<TaxonConstraintTO> contraints)
                    throws DAOException, IllegalArgumentException {
        log.entry(contraints);

        if (contraints == null || contraints.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No anatomical entity relation taxon constraint is given, " +
                    "then no constraint is inserted"));
        }

        String sqlExpression = "INSERT INTO anatEntityRelationTaxonConstraint " +
                                            "(anatEntityRelationId, speciesId) VALUES (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int contraintInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (TaxonConstraintTO contraint: contraints) {
                stmt.setInt(1, Integer.parseInt(contraint.getEntityId()));
                if (contraint.getSpeciesId() == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, Integer.parseInt(contraint.getSpeciesId()));
                }
                contraintInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(contraintInsertedCount);
    }

    @Override
    /*
     * (non-javadoc)
     * All the insert methods of that class are not factorize in a single method because of 
     * table names, entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public int insertAnatEntityTaxonConstraints(Collection<TaxonConstraintTO> contraints)
            throws DAOException, IllegalArgumentException {
        log.entry(contraints);

        if (contraints == null || contraints.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No anatomical entity taxon constraint is given, " +
                    "then no constraint is inserted"));
        }

        String sqlExpression = "INSERT INTO anatEntityTaxonConstraint (anatEntityId, speciesId) " +
                               "VALUES (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int contraintInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (TaxonConstraintTO contraint: contraints) {
                stmt.setString(1, contraint.getEntityId());
                if (contraint.getSpeciesId() == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, Integer.parseInt(contraint.getSpeciesId()));
                }
                contraintInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(contraintInsertedCount);
    }

    @Override
    /*
     * (non-javadoc)
     * All the insert methods of that class are not factorize in a single method because of 
     * table names, entity ID column names, and, most important of all, types of this column 
     * (int or string) are different.
     */
    public int insertStageTaxonConstraints(Collection<TaxonConstraintTO> contraints)
            throws DAOException, IllegalArgumentException {
        log.entry(contraints);

        if (contraints == null || contraints.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No stage taxon constraint is given, then no constraint is inserted"));
        }

        String sqlExpression = "INSERT INTO stageTaxonConstraint (stageId, speciesId) " +
                               "VALUES (?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        int contraintInsertedCount = 0;
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlExpression)) {
            for (TaxonConstraintTO contraint: contraints) {
                stmt.setString(1, contraint.getEntityId());
                if (contraint.getSpeciesId() == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, Integer.parseInt(contraint.getSpeciesId()));
                }

                contraintInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(contraintInsertedCount);
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code MySQLTaxonConstraintTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLTaxonConstraintTOResultSet extends MySQLDAOResultSet<TaxonConstraintTO> 
                                                 implements TaxonConstraintTOResultSet {
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLTaxonConstraintTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected TaxonConstraintTO getNewTO() throws DAOException {
            log.entry();
            
            String entityId = null, speciesId = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("stageId") || 
                                column.getValue().equals("anatEntityId") || 
                                column.getValue().equals("anatEntityRelationId")) {
                        entityId = currentResultSet.getString(column.getKey());
                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = currentResultSet.getString(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
           
            return log.exit(new TaxonConstraintTO(entityId, speciesId));
        }
    }
}
