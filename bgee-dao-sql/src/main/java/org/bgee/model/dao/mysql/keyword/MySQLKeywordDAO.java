package org.bgee.model.dao.mysql.keyword;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.keyword.KeywordDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code KeywordDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 August 2015
 * @see org.bgee.model.dao.api.gene.KeywordDAO.KeywordTO
 * @see org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO
 * @since Bgee 13
 */
public class MySQLKeywordDAO extends MySQLDAO<KeywordDAO.Attribute> implements KeywordDAO {

    private final static Logger log = LogManager.getLogger(MySQLKeywordDAO.class.getName());
    
    /**
     * A {@code String} that is name of the MySQL table containing keywords.
     */
    private static final String keywordTable = "keyword";
    /**
     * A {@code String} that is name of the MySQL table containing mapping between 
     * species IDs and keyword IDs.
     */
    private static final String keywordToSpeciesTable = "speciesToKeyword";
    
    /**
     * A {@code Map} allowing to map column names of a result set to {@code Attribute}s, 
     * see {@link org.bgee.model.dao.mysql.MySQLDAO#getAttributeFromColName(String, Map)} 
     * for more details.
     */
    private static final Map<String, KeywordDAO.Attribute> colNamesToAttributes;
    
    static {
        Map<String, KeywordDAO.Attribute> tempMap = new HashMap<String, KeywordDAO.Attribute>();
        tempMap.put("keywordId", KeywordDAO.Attribute.ID);
        tempMap.put("keyword", KeywordDAO.Attribute.NAME);
        colNamesToAttributes = Collections.unmodifiableMap(tempMap);
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
        return null;
    }

    @Override
    public EntityToKeywordTOResultSet getKeywordToSpecies(Collection<String> speciesIds) 
            throws DAOException {
        log.entry(speciesIds);
        return null;
    }

}
