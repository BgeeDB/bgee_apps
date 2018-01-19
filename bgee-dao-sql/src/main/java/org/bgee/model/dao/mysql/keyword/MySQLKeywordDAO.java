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
    public KeywordTOResultSet getKeywords(Collection<String> keywords) throws DAOException {
        log.entry(keywords);
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            Set<String> filteredKeywords = keywords == null? new HashSet<>(): new HashSet<>(keywords);
            
            String sql = this.generateSelectClause(KEYWORD_TABLE, COL_NAMES_TO_ATTRS, true);
            
            sql += "FROM " + KEYWORD_TABLE;
            
            if (!filteredKeywords.isEmpty()) {
                sql += " WHERE " + KEYWORD_TABLE + ".keyword IN (" + 
                           BgeePreparedStatement.generateParameterizedQueryString(
                                   filteredKeywords.size()) + ")";
            }
            
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!filteredKeywords.isEmpty()) {
                stmt.setStrings(1, filteredKeywords, true);
            }             
            return log.exit(new MySQLKeywordTOResultSet(stmt));
            
        } catch (SQLException|IllegalArgumentException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public KeywordTOResultSet getKeywordsRelatedToSpecies(Collection<Integer> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            Set<Integer> filteredSpeciesIds = 
                    new HashSet<Integer>(speciesIds != null ? speciesIds: Arrays.asList());
            
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
                stmt.setIntegers(1, filteredSpeciesIds, true);
            }             
            return log.exit(new MySQLKeywordTOResultSet(stmt));
            
        } catch (SQLException|IllegalArgumentException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public EntityToKeywordTOResultSet<Integer> getKeywordToSpecies(Collection<Integer> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            Set<Integer> filteredSpeciesIds = 
                    new HashSet<Integer>(speciesIds != null ? speciesIds : Arrays.asList());
            
            String sql = "SELECT keywordId, speciesId FROM " + KEYWORD_TO_SPECIES_TABLE;
            
            if (!filteredSpeciesIds.isEmpty()) {
                sql += " WHERE " + KEYWORD_TO_SPECIES_TABLE + ".speciesId IN (" + 
                           BgeePreparedStatement.generateParameterizedQueryString(
                                   filteredSpeciesIds.size()) + ")";
            }
            
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!filteredSpeciesIds.isEmpty()) {
                stmt.setIntegers(1, filteredSpeciesIds, true);
            }             
            return log.exit(new MySQLEntityToKeywordTOResultSet<Integer>(stmt, "speciesId", Integer.class));
            
        } catch (SQLException|IllegalArgumentException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    
    

    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************
    /**
     * Inserts the provided keywords into the Bgee database.
     * 
     * @param keywords  a {@code Collection} of {@code String}s to be inserted 
     *                  into the database.
     * @return          An {@code int} that is the number of keywords inserted 
     *                  as a result of this method call.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code keywords}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertKeywords(Collection<String> keywords) throws DAOException {
        log.entry(keywords);
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO " + KEYWORD_TABLE + "(keyword) values (?)");
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int inserted = 0;
            for (String keyword: keywords) {
                stmt.setString(1, keyword);
                try {
                    stmt.executeUpdate();
                    inserted++;
                } catch (SQLException e) {
                    //nothing here, the insert will fail if the keyword is already present in database
                }
            }
            
            return log.exit(inserted);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    /**
     * Inserts the provided relations between keyword IDs and species IDs into the Bgee database.
     * 
     * @param speciesToKeywords     a {@code Collection} of {@code EntityToKeywordTO}s to be inserted 
     *                              into the database.
     * @return                      An {@code int} that is the number of relations inserted 
     *                              as a result of this method call.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code EntityToKeywordTO}s. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertKeywordToSpecies(Collection<EntityToKeywordTO<Integer>> speciesToKeywords) throws DAOException {
        log.entry(speciesToKeywords);
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO " + KEYWORD_TO_SPECIES_TABLE + "(keywordId, speciesId) values ");
        for (int i = 0; i < speciesToKeywords.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (EntityToKeywordTO<Integer> speciesToKeyword: speciesToKeywords) {
                stmt.setInt(paramIndex, speciesToKeyword.getKeywordId());
                paramIndex++;
                stmt.setInt(paramIndex, speciesToKeyword.getEntityId());
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
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
    public class MySQLKeywordTOResultSet extends MySQLDAOResultSet<KeywordTO> 
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
                Integer id = null;
                String name = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    KeywordDAO.Attribute attr = MySQLKeywordDAO.this.getAttributeFromColName(
                            columnName, COL_NAMES_TO_ATTRS);
                    switch (attr) {
                    case ID:
                        id = currentResultSet.getInt(col.getKey());
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
     * 
     * @param <T> the tpe of ID of the related entity in {@code EntityToKeywordTO}.
     */
    class MySQLEntityToKeywordTOResultSet<T> extends MySQLDAOResultSet<EntityToKeywordTO<T>> 
            implements EntityToKeywordTOResultSet<T> {
        
        private final Class<T> cls;
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
                String entityIdColName, Class<T> cls) {
            super(statement);
            //acceptable to use assert for sanity check on private method; 
            //plus, an UnrecognizedColumnException will be later thrown if the value is incorrect
            assert StringUtils.isNotBlank(entityIdColName);
            
            this.entityIdColName = entityIdColName;
            this.cls = cls;
        }

        @Override
        protected EntityToKeywordTO<T> getNewTO() throws DAOException {
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer keywordId = null;
                T entityId = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    
                    if (columnName.equals("keywordId")) {
                        keywordId = currentResultSet.getInt(col.getKey());
                    } else if (columnName.equals(this.entityIdColName)) {
                        entityId = currentResultSet.getObject(col.getKey(), this.cls);
                    } else {
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new EntityToKeywordTO<T>(entityId, keywordId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
