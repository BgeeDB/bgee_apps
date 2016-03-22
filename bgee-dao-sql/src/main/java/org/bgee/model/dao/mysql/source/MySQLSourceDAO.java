package org.bgee.model.dao.mysql.source;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO.SourceCategory;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code SourceDAO} for MySQL.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13
 * @see org.bgee.model.dao.api.source.SourceDAO.SourceTO
 */
public class MySQLSourceDAO extends MySQLDAO<SourceDAO.Attribute> implements SourceDAO {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(MySQLSourceDAO.class.getName());

    /**
     * A {@code String} that is the name of the table containing data sources in Bgee.
     */
    private static final String SOURCE_TABLE_NAME = "dataSource";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this
     * {@code MySQLDAO} will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                   The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLSourceDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public SourceTOResultSet getAllDataSources(Collection<SourceDAO.Attribute> attributes) 
            throws DAOException {
        log.entry(attributes);
        return log.exit(this.getDataSources(null, false, attributes));
    }

    @Override
    public SourceTOResultSet getDisplayableDataSources(Collection<SourceDAO.Attribute> attributes) 
            throws DAOException {
        log.entry(attributes);
        return log.exit(this.getDataSources(null, true, attributes));
    }

    @Override
    public SourceTO getDataSourceById(String dataSourceId, Collection<SourceDAO.Attribute> attributes)
            throws DAOException, IllegalStateException {
        log.entry(dataSourceId, attributes);
        List<SourceTO> tos = this.getDataSources(dataSourceId, false, attributes).getAllTOs();
        if (tos == null || tos.size() != 1) {
            throw log.throwing(new IllegalStateException("Shoud get only 1 element: " + tos));
        }
        return log.exit(tos.iterator().next());
    }
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code SourceTO}s.
     * 
     * @param attributes        A {@code Set} of {@code Attribute}s defining the
     *                          columns/information the query should retrieve.
     * @param sourceTableName   A {@code String} defining the name of the source table used.
     * @return                  A {@code String} containing the SELECT clause for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Collection<SourceDAO.Attribute> attributes, String sourceTableName)
            throws IllegalArgumentException{
        log.entry(attributes, sourceTableName);
        
        String sql = "";
        EnumSet<SourceDAO.Attribute> clonedAttrs = Optional.ofNullable(attributes)
                .map(e -> e.isEmpty()? null: EnumSet.copyOf(e)).orElse(null);
        if (clonedAttrs == null || clonedAttrs.isEmpty()) {
            sql += "SELECT DISTINCT " + sourceTableName + ".*";
        } else {
            for (SourceDAO.Attribute attribute: clonedAttrs) {
                if (sql.isEmpty()) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                String label = null;
                if (attribute.equals(SourceDAO.Attribute.ID)) {
                    label = "dataSourceId";
                } else if (attribute.equals(SourceDAO.Attribute.NAME)) {
                    label = "dataSourceName";
                } else if (attribute.equals(SourceDAO.Attribute.DESCRIPTION)) {
                    label = "dataSourceDescription";
                } else if (attribute.equals(SourceDAO.Attribute.XREF_URL)) {
                    label = "XRefUrl";
                } else if (attribute.equals(SourceDAO.Attribute.EXPERIMENT_URL)) {
                    label = "experimentUrl";
                } else if (attribute.equals(SourceDAO.Attribute.EVIDENCE_URL)) {
                    label = "evidenceUrl";
                } else if (attribute.equals(SourceDAO.Attribute.BASE_URL)) {
                    label = "baseUrl";
                } else if (attribute.equals(SourceDAO.Attribute.RELEASE_DATE)) {
                    label = "releaseDate";
                } else if (attribute.equals(SourceDAO.Attribute.RELEASE_VERSION)) {
                    label = "releaseVersion";
                } else if (attribute.equals(SourceDAO.Attribute.TO_DISPLAY)) {
                    label = "toDisplay";
                } else if (attribute.equals(SourceDAO.Attribute.CATEGORY)) {
                    label = "category";
                } else if (attribute.equals(SourceDAO.Attribute.DISPLAY_ORDER)) {
                    label = "displayOrder";
                } else {
                    throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                            attribute.toString() + ") is unknown for " + SourceDAO.class.getName()));
                }
                sql += sourceTableName + "." + label;
            }
        }
        return log.exit(sql);
    }

    /** 
     * Return sources used in Bgee from data source, according to {@code dataSourceId},
     * {@code displayableOnly}, and {@code attributes}.
     * 
     * @param dataSourceId      A {@code String} representing the ID of the data source to retrieve.
     * @param displayableOnly   A {@code Boolean} defining whether retrieved data source should be
     *                          displayable on the page listing data sources.
     * @param attributes        A {@code Collection} of {@code SourceDAO.Attribute}s defining the
     *                          attributes to populate in the returned {@code SourceTO}s.
     *                          If {@code null} or empty, all attributes are populated. 
     * @return                  A {@code SourceTOResultSet} containing sources used in Bgee, 
     *                          according to {@code dataSourceId}, {@code displayableOnly},
     *                          and {@code attributes}.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    private SourceTOResultSet getDataSources(String dataSourceId, boolean displayableOnly, 
            Collection<SourceDAO.Attribute> attributes) throws DAOException {
        log.entry(dataSourceId, displayableOnly, attributes);
        
        boolean isIdFilter = dataSourceId != null && !dataSourceId.isEmpty();

        // Construct sql query
        String sql = this.generateSelectClause(attributes, SOURCE_TABLE_NAME);

        sql += " FROM " + SOURCE_TABLE_NAME;

        if (isIdFilter || displayableOnly) {
            sql += " WHERE ";
        }
        
        if (isIdFilter) {
            sql += "dataSourceId = ?";
        }

        if (isIdFilter && displayableOnly) {
            sql += " AND ";
        }

        if (displayableOnly) {
            sql += "toDisplay = ?";            
        }
            
        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            
            if (isIdFilter) {
                stmt.setString(1, dataSourceId);
            }
            if (displayableOnly) {
                int offsetParamIndex = (isIdFilter? 2: 1);
                stmt.setBoolean(offsetParamIndex, displayableOnly);
            }

            return log.exit(new MySQLSourceTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code SourceTO}, allowing to fetch results 
     * of queries performed by this {@code MySQLSourceDAO}, to populate {@code SourceTO}s.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Mar. 2016
     * @since   Bgee 13
     */
    class MySQLSourceTOResultSet extends MySQLDAOResultSet<SourceTO> implements SourceTOResultSet {
        
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement         The {@code BgeePreparedStatement} to be executed.
         */
        private MySQLSourceTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected SourceTO getNewTO() {
            String sourceId = null, sourceName = null, sourceDescription = null, xRefUrl = null,
                    experimentUrl = null, evidenceUrl = null, baseUrl = null, releaseVersion = null;
            java.sql.Date releaseDate = null;
            Boolean toDisplay = null;
            SourceCategory category = null;
            Integer displayOrder = null;

            for (Map.Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    String columnName = column.getValue();

                    if (columnName.equals("dataSourceId")) {
                        sourceId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("dataSourceName")) {
                        sourceName = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("dataSourceDescription")) {
                        sourceDescription = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("XRefUrl")) {
                        xRefUrl = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("experimentUrl")) {
                        experimentUrl = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("evidenceUrl")) {
                        evidenceUrl = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("baseUrl")) {
                        baseUrl = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("releaseDate")) {
                        releaseDate = this.getCurrentResultSet().getDate(column.getKey());

                    } else if (columnName.equals("releaseVersion")) {
                        releaseVersion = this.getCurrentResultSet().getString(column.getKey());

                    } else if (columnName.equals("toDisplay")) {
                        toDisplay = this.getCurrentResultSet().getBoolean(column.getKey());

                    } else if (columnName.equals("category")) {
                        category = SourceCategory.convertToSourceCategory(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else if (columnName.equals("displayOrder")) {
                        displayOrder = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }

            }
            return log.exit(new SourceTO(sourceId, sourceName, sourceDescription, xRefUrl, experimentUrl,
                    evidenceUrl, baseUrl, releaseDate, releaseVersion, toDisplay, category, displayOrder));
        }
    }
}
