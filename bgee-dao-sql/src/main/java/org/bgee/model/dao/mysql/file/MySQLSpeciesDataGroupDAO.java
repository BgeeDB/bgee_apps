package org.bgee.model.dao.mysql.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The MySQL implementation of the {@link SpeciesDataGroupDAO} interface.
 *
 * @author Philippe Moret
 */
public class MySQLSpeciesDataGroupDAO extends MySQLDAO<SpeciesDataGroupDAO.Attribute> implements SpeciesDataGroupDAO {

    private static final Logger log = LogManager.getLogger(MySQLSpeciesDataGroupDAO.class.getName());

    /**
     * The underlying MySQL table name
     */
    private static final String SPECIES_DATAGROUP_TABLE = "speciesDataGroup";

    private static final Map<String, SpeciesDataGroupDAO.Attribute> columnToAttributesMap;

    static {
        columnToAttributesMap = new HashMap<>();
        columnToAttributesMap.put("speciesDataGroupId", SpeciesDataGroupDAO.Attribute.ID);
        columnToAttributesMap.put("name", SpeciesDataGroupDAO.Attribute.NAME);
        columnToAttributesMap.put("description", SpeciesDataGroupDAO.Attribute.DESCRIPTION);
    }

    /**
     * Finds the {@link org.bgee.model.dao.api.file.SpeciesDataGroupDAO.Attribute} from a column name.
     *
     * @param columnName The column name.
     * @return The {@link org.bgee.model.dao.api.file.SpeciesDataGroupDAO.Attribute} corresponding to the column name
     * @throws IllegalArgumentException If the columnName doesn't match any attributes.
     */
    private static SpeciesDataGroupDAO.Attribute getAttributeByColumnName(String columnName) {
        log.entry(columnName);
        SpeciesDataGroupDAO.Attribute attribute = columnToAttributesMap.get(columnName);
        if (attribute == null) {
            throw log.throwing(new IllegalArgumentException("Unknown column name : " + columnName));
        } 
        return attribute;
    }

    /**
     * Default constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO}
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLSpeciesDataGroupDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }


    @Override
    public SpeciesDataGroupTOResultSet getAllSpeciesDataGroup() throws DAOException {
        log.entry();
        Collection<SpeciesDataGroupDAO.Attribute> attrs = getAttributes();
        String sql = generateSelectAllStatement(SPECIES_DATAGROUP_TABLE, attrs, columnToAttributesMap);
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLSpeciesDataGroupTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    public class MySQLSpeciesDataGroupTOResultSet extends MySQLDAOResultSet<SpeciesDataGroupTO> implements
            SpeciesDataGroupTOResultSet {

        private MySQLSpeciesDataGroupTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected SpeciesDataGroupTO getNewTO() throws DAOException {
            log.entry();
            final ResultSet currentResultSet = this.getCurrentResultSet();
            try {
                String id = null, name = null, description = null;
                for (String colName : getColumnLabels().values()) {
                    SpeciesDataGroupDAO.Attribute attr = getAttributeByColumnName(colName);
                    switch (attr) {
                        case ID:
                            id = currentResultSet.getString(colName);
                            break;
                        case DESCRIPTION:
                            description = currentResultSet.getString(colName);
                            break;
                        case NAME:
                            name = currentResultSet.getString(colName);
                            break;
                        default:
                            throw log.throwing(new UnrecognizedColumnException(colName));
                    }
                }
                return log.exit(new SpeciesDataGroupTO(id, name, description));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

    /**
     * The MySQL implementation of the {@code DAOResultSet} interfacte for {@code SpeciesToDataGroupMemberTO}
     */
    class MySQLSpeciesToDataGroupTOResultSet extends MySQLDAOResultSet<SpeciesToDataGroupTO>
            implements SpeciesToDataGroupTOResultSet {

        /**
         * Construct a result set from the {@code BgeePreparedStatement}
         *
         * @param stmt the {@code BgeePreparedStatement}
         */
        private MySQLSpeciesToDataGroupTOResultSet(BgeePreparedStatement stmt) {
            super(stmt);
        }

        @Override
        protected SpeciesToDataGroupTO getNewTO() throws DAOException {
            log.entry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                String speciesId = null, groupId = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    String currentValue = currentResultSet.getString(columnName);
                    switch (columnName) {
                        case "speciesId":
                            speciesId = currentValue;
                            break;
                        case "speciesDataGroupId":
                            groupId = currentValue;
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new SpeciesToDataGroupTO(groupId, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

    @Override
    public SpeciesToDataGroupTOResultSet getAllSpeciesToDataGroup() {
        log.entry();
        String sql = "SELECT speciesId, speciesDataGroupId FROM speciesToDataGroup";
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLSpeciesToDataGroupTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

}
