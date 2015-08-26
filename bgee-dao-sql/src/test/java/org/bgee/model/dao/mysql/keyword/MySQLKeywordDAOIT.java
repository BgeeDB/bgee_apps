package org.bgee.model.dao.mysql.keyword;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Test;

public class MySQLKeywordDAOIT extends MySQLITAncestor {
    
    private final static Logger log = LogManager.getLogger(MySQLKeywordDAOIT.class.getName());

    public MySQLKeywordDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the select method {@link MySQLKeywordDAO#getKeywordsRelatedToSpecies(Collection)}.
     */
    @Test
    public void shouldGetKeywordsRelatedToSpecies() throws SQLException {
        
        this.useSelectDB();

        MySQLKeywordDAO dao = new MySQLKeywordDAO(this.getMySQLDAOManager());
    }
    

    /**
     * Test the select method {@link MySQLKeywordDAO#getKeywordsToSpecies(Collection)}.
     */
    @Test
    public void shouldGetKeywordToSpecies() throws SQLException {
        
        this.useSelectDB();

        MySQLKeywordDAO dao = new MySQLKeywordDAO(this.getMySQLDAOManager());
    }

}
