package org.bgee.model.dao.mysql.keyword;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code KeywordDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 August 2015
 * @see org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO
 * @see org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO
 * @since Bgee 13
 */
public class MySQLKeywordDAO extends MySQLDAO<KeywordDAO.Attribute> implements KeywordDAO {

    private final static Logger log = LogManager.getLogger(MySQLKeywordDAO.class.getName());
    
    /**
     * A {@code String} that is name of the MySQL table containing keywords.
     */
    private static final String KEYWORD_TABLE = "keyword";
    /**
     * A {@code String} that is name of the MySQL table containing mapping between 
     * species IDs and keyword IDs.
     */
    private static final String KEYWORD_TO_SPECIES_TABLE = "speciesToKeyword";
    
    /**
     * A {@code Map} allowing to map column names of a result set to {@code Attribute}s, 
     * see {@link org.bgee.model.dao.mysql.MySQLDAO#getAttributeFromColName(String, Map)} 
     * for more details.
     */
    private static final Map<String, KeywordDAO.Attribute> COL_NAMES_TO_ATTRS;
    
    static {
        Map<String, KeywordDAO.Attribute> tempMap = new HashMap<String, KeywordDAO.Attribute>();
        tempMap.put("keywordId", KeywordDAO.Attribute.ID);
        tempMap.put("keyword", KeywordDAO.Attribute.NAME);
        COL_NAMES_TO_ATTRS = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager   The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLKeywordDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public KeywordTOResultSet getKeywordsRelatedToSpecies(Collection<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            Set<String> filteredSpeciesIds = 
                    new HashSet<String>(speciesIds != null ? speciesIds: Arrays.asList());
            
            String sql = this.generateSelectClause(KEYWORD_TABLE, COL_NAMES_TO_ATTRS, true);
            
            sql += "FROM " + KEYWORD_TABLE 
                 + " INNER JOIN " + KEYWORD_TO_SPECIES_TABLE + " ON "
                 + KEYWORD_TABLE + ".keywordId = " + KEYWORD_TO_SPECIES_TABLE + ".keywordId";
            
            if (!filteredSpeciesIds.isEmpty()) {
                sql += " WHERE " + KEYWORD_TO_SPECIES_TABLE + ".speciesId IN (" + 
                           BgeePreparedStatement.generateParameterizedQueryString(
                                   filteredSpeciesIds.size()) + ")";
            }
            
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!filteredSpeciesIds.isEmpty()) {
                stmt.setStringsToIntegers(1, filteredSpeciesIds, true);
            }             
            return log.exit(new MySQLKeywordTOResultSet(stmt));
            
        } catch (SQLException|IllegalArgumentException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public EntityToKeywordTOResultSet getKeywordToSpecies(Collection<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            Set<String> filteredSpeciesIds = 
                    new HashSet<String>(speciesIds != null ? speciesIds : Arrays.asList());
            
            String sql = "SELECT keywordId, speciesId FROM " + KEYWORD_TO_SPECIES_TABLE;
            
            if (!filteredSpeciesIds.isEmpty()) {
                sql += " WHERE " + KEYWORD_TO_SPECIES_TABLE + ".speciesId IN (" + 
                           BgeePreparedStatement.generateParameterizedQueryString(
                                   filteredSpeciesIds.size()) + ")";
            }
            
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!filteredSpeciesIds.isEmpty()) {
                stmt.setStringsToIntegers(1, filteredSpeciesIds, true);
            }             
            return log.exit(new MySQLEntityToKeywordTOResultSet(stmt, "speciesId"));
            
        } catch (SQLException|IllegalArgumentException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code KeywordTO}, allowing to fetch results 
     * of queries performed by this {@code MySQLKeywordDAO}, to populate {@code KeywordTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 August 2015
     * @since Bgee 13
     */
    class MySQLKeywordTOResultSet extends MySQLDAOResultSet<KeywordTO> 
            implements KeywordTOResultSet {
        /**
         * @param statement The {@code BgeePreparedStatement} to be executed.
         */
        private MySQLKeywordTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected KeywordTO getNewTO() throws DAOException {
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                String id = null, name = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    KeywordDAO.Attribute attr = MySQLKeywordDAO.this.getAttributeFromColName(
                            columnName, COL_NAMES_TO_ATTRS);
                    switch (attr) {
                    case ID:
                        id = currentResultSet.getString(col.getKey());
                        break;
                    case NAME:
                        name = currentResultSet.getString(col.getKey());
                        break;
                    default:
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new KeywordTO(id, name));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code EntityToKeywordTO}, allowing to fetch results 
     * of queries performed by this {@code MySQLKeywordDAO}, to populate {@code EntityToKeywordTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 August 2015
     * @since Bgee 13
     */
    class MySQLEntityToKeywordTOResultSet extends MySQLDAOResultSet<EntityToKeywordTO> 
            implements EntityToKeywordTOResultSet {
        
        /**
         * A {@code String} that is the name of the column containing the entity IDs 
         * in the MySQL query currently executed (for instance, 'speciesId').
         */
        private final String entityIdColName;
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement         The {@code BgeePreparedStatement} to be executed.
         * @param entityIdColName   A {@code String} that is the name of the column 
         *                          containing the entity IDs in the MySQL query 
         *                          currently executed (for instance, 'speciesId').
         */
        private MySQLEntityToKeywordTOResultSet(BgeePreparedStatement statement, 
                String entityIdColName) {
            super(statement);
            //acceptable to use assert for sanity check on private method; 
            //plus, an UnrecognizedColumnException will be later thrown if the value is incorrect
            assert StringUtils.isNotBlank(entityIdColName);
            
            this.entityIdColName = entityIdColName;
        }

        @Override
        protected EntityToKeywordTO getNewTO() throws DAOException {
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                String keywordId = null, entityId = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    
                    if (columnName.equals("keywordId")) {
                        keywordId = currentResultSet.getString(col.getKey());
                    } else if (columnName.equals(this.entityIdColName)) {
                        entityId = currentResultSet.getString(col.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new EntityToKeywordTO(entityId, keywordId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
