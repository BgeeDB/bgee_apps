package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * The MySQL implementation of {@code GeneNameSynonymDAO}
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Jun. 2019
 * @since   Bgee 13, Feb. 2016
 *
 */
public class MySQLGeneNameSynonymDAO extends MySQLDAO<GeneNameSynonymDAO.Attribute> implements GeneNameSynonymDAO {

    private static final Logger log = LogManager.getLogger(MySQLGeneNameSynonymDAO.class.getName());

    /**
     * The table name
     */
    private static final String GENE_NAME_SYNONYM_TABLE = "geneNameSynonym";

    /**
     * Constructor providing the manager
     * 
     * @param manager A {@code MySQLDAOManager} instance
     * @throws IllegalArgumentException If an error occurs
     */
    public MySQLGeneNameSynonymDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    /**
     * The {@code Map} of column names to {@link GeneNameSynonymDAO.Attribute}
     */
    private static final Map<String, GeneNameSynonymDAO.Attribute> COL_NAMES_TO_ATTRS;

    static {
        Map<String, GeneNameSynonymDAO.Attribute> tempMap = new HashMap<>();
        tempMap.put("bgeeGeneId", GeneNameSynonymDAO.Attribute.GENE_ID);
        tempMap.put("geneNameSynonym", GeneNameSynonymDAO.Attribute.GENE_NAME_SYNONYM);
        COL_NAMES_TO_ATTRS = Collections.unmodifiableMap(tempMap);
    }

    @Override
    public GeneNameSynonymTOResultSet getGeneNameSynonyms(Collection<Integer> bgeeGeneIds) {
        log.traceEntry("{}", bgeeGeneIds);
        Set<Integer> clonedIds = Collections.unmodifiableSet(
                bgeeGeneIds == null? new HashSet<>(): new HashSet<>(bgeeGeneIds));

        // Construct sql query
        String sql = this.generateSelectClause(GENE_NAME_SYNONYM_TABLE, COL_NAMES_TO_ATTRS, true);
        sql += " FROM " + GENE_NAME_SYNONYM_TABLE;
        if (!clonedIds.isEmpty()) {
            sql += " WHERE bgeeGeneId IN ("
                   + BgeePreparedStatement.generateParameterizedQueryString(clonedIds.size()) + ")";
        }

        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!clonedIds.isEmpty()) {
                stmt.setIntegers(1, bgeeGeneIds, true);
            }
            // we don't use a try-with-resource, because we return a pointer to the
            // results,
            // not the actual results, so we should not close this
            // BgeePreparedStatement.
            return log.traceExit(new MySQLGeneNameSynonymTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    public GeneNameSynonymTOResultSet getGeneNameSynonyms(Collection<String> geneIds,
            Collection<Integer> speciesIds) {
        log.traceEntry("{}, {}", geneIds, speciesIds);
        Set<String> clonedGeneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        Set<Integer> clonedSpeciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));

        // Construct sql query
        String sql = this.generateSelectClause("t1", COL_NAMES_TO_ATTRS, true);
        sql += " FROM " + GENE_NAME_SYNONYM_TABLE + " AS t1";
        if (!clonedGeneIds.isEmpty() || !clonedSpeciesIds.isEmpty()) {
            sql += " INNER JOIN gene AS t2 ON t1.bgeeGeneId = t2.bgeeGeneId WHERE ";
            if (!clonedGeneIds.isEmpty()) {
                sql += "t2.geneId IN ("
                        + BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size()) + ")";
            }
            if (!clonedSpeciesIds.isEmpty()) {
                if (!clonedGeneIds.isEmpty()) {
                    sql += " AND ";
                }
                sql += "t2.speciesId IN ("
                        + BgeePreparedStatement.generateParameterizedQueryString(clonedSpeciesIds.size()) + ")";
            }
        }

        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int index = 1;
            if (!clonedGeneIds.isEmpty()) {
                stmt.setStrings(index, clonedGeneIds, true);
                index += clonedGeneIds.size();
            }
            if (!clonedSpeciesIds.isEmpty()) {
                stmt.setIntegers(index, clonedSpeciesIds, true);
            }
            // we don't use a try-with-resource, because we return a pointer to the
            // results,
            // not the actual results, so we should not close this
            // BgeePreparedStatement.
            return log.traceExit(new MySQLGeneNameSynonymTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * The MySQL implementation of {@link org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTOResultSet}
     * 
     * @version Bgee 13.2
     * @author  Philippe Moret
     * @since   Bgee 13.2
     */
    public class MySQLGeneNameSynonymTOResultSet extends MySQLDAOResultSet<GeneNameSynonymTO>
            implements GeneNameSynonymTOResultSet {

        protected MySQLGeneNameSynonymTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GeneNameSynonymTO getNewTO() throws DAOException, UnrecognizedColumnException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer geneId = null;
                String geneNameSynonym = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    GeneNameSynonymDAO.Attribute attr = MySQLGeneNameSynonymDAO.this.getAttributeFromColName(columnName,
                            COL_NAMES_TO_ATTRS);
                    switch (attr) {
                    case GENE_ID:
                        geneId = currentResultSet.getInt(col.getKey());
                        break;
                    case GENE_NAME_SYNONYM:
                        geneNameSynonym = currentResultSet.getString(col.getKey());
                        break;
                    default:
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.traceExit(new GeneNameSynonymTO(geneId, geneNameSynonym));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

    }
}