package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneHomologsDAO;


public class MySQLGeneHomologsDAO extends MySQLDAO<GeneHomologsDAO.Attribute> implements GeneHomologsDAO{
    
    public MySQLGeneHomologsDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
        // TODO Auto-generated constructor stub
    }

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(MySQLGeneHomologsDAO.class.getName());
    
    private static final String ORTHOLOGS_TABLE_NAME = "orthologousGenes";
    
    private static final String PARALOGS_TABLE_NAME = "paralogousGenes";
        
    enum HomologyType {
        ORTHOLOGS,
        PARALOGS
    }
    
    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, GeneHomologsDAO.Attribute> columnToAttributesMap;

    static {
        columnToAttributesMap = new HashMap<>();
        columnToAttributesMap.put("sourceBgeeGeneId", GeneHomologsDAO.Attribute.SOURCE_BGEE_GENE_ID);
        columnToAttributesMap.put("targetBgeeGeneId", GeneHomologsDAO.Attribute.TARGET_BGEE_GENE_ID);
        columnToAttributesMap.put("taxonId", GeneHomologsDAO.Attribute.TAXON_ID);
    }

    @Override
    public GeneHomologsTOResultSet getOrthologousGenes(Integer bgeeGeneId) {
        log.entry(bgeeGeneId);
        return log.exit(getOrthologousGenesAtTaxonLevel(bgeeGeneId, null));
    }

    @Override
    public GeneHomologsTOResultSet getOrthologousGenesAtTaxonLevel(Integer bgeeGeneId, Integer taxonId) {
        log.entry(bgeeGeneId);
        return log.exit(getOneTypeOfHomology(bgeeGeneId, taxonId, HomologyType.ORTHOLOGS));
    }

    @Override
    public GeneHomologsTOResultSet getParalogousGenes(Integer bgeeGeneId) {
        log.entry(bgeeGeneId);
        return log.exit(getParalogousGenesAtTaxonLevel(bgeeGeneId, null));
    }

    @Override
    public GeneHomologsTOResultSet getParalogousGenesAtTaxonLevel(Integer bgeeGeneId, Integer taxonId) {
        log.entry(bgeeGeneId);
        return log.exit(getOneTypeOfHomology(bgeeGeneId, taxonId, HomologyType.PARALOGS));
    }
    
    private GeneHomologsTOResultSet getOneTypeOfHomology(Integer bgeeGeneId, Integer taxonId, HomologyType homologyType) {
        log.entry(bgeeGeneId, taxonId, homologyType);
        if (bgeeGeneId == null || homologyType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "homologyType and bgeeGeneId can not be null"));
        }
        // the table to query depends of the homologyType
        String tableName;
        if(homologyType == HomologyType.PARALOGS) {
            tableName = ORTHOLOGS_TABLE_NAME;
        } else if (homologyType == HomologyType.PARALOGS) {
            tableName = PARALOGS_TABLE_NAME;
        } else {
            throw log.throwing(new IllegalArgumentException(
                    "unknown homology type"));
        }
        String sql = generateSelectClause(tableName, columnToAttributesMap, Boolean.TRUE);
        sql += " FROM " + tableName;
        
        if (taxonId != null) {
            sql += " INNER JOIN taxon AS t2 ON t2.taxonId = " + tableName + ".taxonId ";
            sql += " INNER JOIN taxon AS t3 ON t2.taxonLeftBound >= t3.taxonLeftBound AND "
                    + "t2.taxonRightBound <= t3.taxonRightBound";
        }
        sql += " WHERE (" + tableName + ".sourceBgeeGeneId = " + bgeeGeneId + " OR "
                + tableName + ".targetBgeeGeneId = ?)";
        if (taxonId != null) {
            sql += " AND t3.taxonId = ?";
        }
        
     // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setInt(1, bgeeGeneId);
            if (taxonId != null) {
                stmt.setInt(2, taxonId);
            }
            return log.exit(new MySQLGeneHomologsTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public void insertParalogs(Set<GeneHomologsTO> paralogs) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void insertOrthologs(Set<GeneHomologsTO> orthologs) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code GeneHomologsTO}.
     * 
     * @author Julien Wollbrett
     * @version Bgee 15 Sep. 2020
     * @since Bgee 15 Sep. 2020
     */
    
    public class MySQLGeneHomologsTOResultSet extends MySQLDAOResultSet<GeneHomologsTO> implements GeneHomologsTOResultSet {

        /**
         * Delegates to
         * {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement     The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLGeneHomologsTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GeneHomologsTO getNewTO() {
            log.entry();
            Integer sourceBgeeGeneId = null;
            Integer targetBgeeGeneId = null;
            Integer taxonId = null;
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("sourceBgeeGeneId")) {
                        sourceBgeeGeneId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("targetBgeeGeneId")) {
                        targetBgeeGeneId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("taxonId")) {
                        taxonId = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            // Set GeneHomologsTO
            return log.exit(new GeneHomologsTO(sourceBgeeGeneId, targetBgeeGeneId, taxonId));
        }
    }

}
