package org.bgee.model.dao.mysql.source;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code SourceToSpeciesDAO} for MySQL.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, June 2016
 * @since   Bgee 13, June 2016
 * @see     org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO
 */
public class MySQLSourceToSpeciesDAO extends MySQLDAO<SourceToSpeciesDAO.Attribute> implements SourceToSpeciesDAO {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(MySQLSourceToSpeciesDAO.class.getName());

    private static final String SOURCE_TO_SPECIES_TABLE_NAME = "dataSourceToSpecies";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this
     * {@code MySQLDAO} will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager
     *            The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException
     *             If {@code manager} is {@code null}.
     */
    public MySQLSourceToSpeciesDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public SourceToSpeciesTOResultSet getAllSourceToSpecies(
            Collection<SourceToSpeciesDAO.Attribute> attibutes) throws DAOException {
        log.entry(attibutes);
        return log.traceExit(this.getSourceToSpecies(null, null, null, null, attibutes));
        
    }

    @Override
    public SourceToSpeciesTOResultSet getSourceToSpecies(Collection<Integer> dataSourceIds,
            Collection<Integer> speciesIds, Collection<DAODataType> dataTypes, Collection<InfoType> infoTypes,
            Collection<SourceToSpeciesDAO.Attribute> attributes) throws DAOException {
        log.entry(dataSourceIds, speciesIds, dataTypes, infoTypes, attributes);

        Set<SourceToSpeciesDAO.Attribute> attributesToUse = attributes == null || attributes.isEmpty()? 
                EnumSet.allOf(SourceToSpeciesDAO.Attribute.class) :
                new HashSet<SourceToSpeciesDAO.Attribute>(attributes);

        String sql = "";
        for (SourceToSpeciesDAO.Attribute attribute : attributesToUse) {
            if (sql.isEmpty()) {
                sql += "SELECT DISTINCT ";
            } else {
                sql += ", ";
            }
            sql += SOURCE_TO_SPECIES_TABLE_NAME + ".";
            if (attribute.equals(SourceToSpeciesDAO.Attribute.DATASOURCE_ID)) {
                sql += "dataSourceId";
            } else if (attribute.equals(SourceToSpeciesDAO.Attribute.SPECIES_ID)) {
                sql += "speciesId";
            } else if (attribute.equals(SourceToSpeciesDAO.Attribute.DATA_TYPE)) {
                sql += "dataType";
            } else if (attribute.equals(SourceToSpeciesDAO.Attribute.INFO_TYPE)) {
                sql += "infoType";
            } else {
                throw log.throwing(new IllegalArgumentException(
                        "The attribute provided (" + attribute.toString() + ") is unknown for " + 
                                SourceToSpeciesDAO.class.getName()));
            }
        }
        sql += " FROM " + SOURCE_TO_SPECIES_TABLE_NAME;

        boolean filterByDataSources = dataSourceIds != null && !dataSourceIds.isEmpty();
        boolean filterBySpecies = speciesIds != null && !speciesIds.isEmpty();
        boolean filterByDataTypes = dataTypes != null && !dataTypes.isEmpty();
        boolean filterByInfoTypes = infoTypes != null && !infoTypes.isEmpty();

        if (filterByDataSources || filterBySpecies || filterByDataTypes || filterByInfoTypes) {
            sql += " WHERE ";
        }
        if (filterByDataSources) {
            sql += SOURCE_TO_SPECIES_TABLE_NAME + ".dataSourceId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(dataSourceIds.size()) + ")";
        }
        if (filterByDataSources && filterBySpecies) {
            sql += " AND ";
        }
        if (filterBySpecies) {
            sql += SOURCE_TO_SPECIES_TABLE_NAME + ".speciesId IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(speciesIds.size()) + ")";
        }
        if ((filterByDataSources || filterBySpecies) && filterByDataTypes) {
            sql += " AND ";
        }
        if (filterByDataTypes) {
            sql += SOURCE_TO_SPECIES_TABLE_NAME + ".dataType IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(dataTypes.size()) + ")";
        }
        if ((filterByDataSources || filterBySpecies || filterByDataTypes) && filterByInfoTypes) {
            sql += " AND ";
        }
        if (filterByInfoTypes) {
            sql += SOURCE_TO_SPECIES_TABLE_NAME + ".infoType IN (" + 
                    BgeePreparedStatement.generateParameterizedQueryString(infoTypes.size()) + ")";
        }
        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (filterByDataSources) {
                stmt.setIntegers(1, dataSourceIds, true);
            }
            int offsetParamIndex = (filterByDataSources ? dataSourceIds.size() + 1 : 1);
            if (filterBySpecies) {
                stmt.setIntegers(offsetParamIndex, speciesIds, true);
                offsetParamIndex += speciesIds.size();
            }
            if (filterByDataTypes) {
                stmt.setEnumDAOFields(offsetParamIndex, dataTypes, true);
                offsetParamIndex += dataSourceIds.size();
            }
            if (filterByInfoTypes) {
                stmt.setEnumDAOFields(offsetParamIndex, infoTypes, true);
            }
            return log.traceExit(new MySQLSourceToSpeciesTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code SourceToSpeciesTO}.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, June 2016
     * @since   Bgee 13, June 2016
     */
    public class MySQLSourceToSpeciesTOResultSet extends MySQLDAOResultSet<SourceToSpeciesTO>
        implements SourceToSpeciesTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLSourceToSpeciesTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected SourceToSpeciesTO getNewTO() {
            log.traceEntry();
            Integer dataSourceId = null, speciesId = null;
            DAODataType dataType = null;
            InfoType infoType = null;
            
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("dataSourceId")) {
                        dataSourceId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("dataType")) {
                        dataType = DAODataType.convertToDataType(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else if (column.getValue().equals("infoType")) {
                        infoType = InfoType.convertToInfoType(
                                this.getCurrentResultSet().getString(column.getKey()));

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set SourceToSpeciesTO
            return log.traceExit(new SourceToSpeciesTO(dataSourceId, speciesId, dataType, infoType));
        }
    }
}
