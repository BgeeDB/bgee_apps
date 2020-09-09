package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
    
    private static final String ORTHOLOGS_TABLE_NAME = "geneOrthologs";
    
    private static final String PARALOGS_TABLE_NAME = "geneParalogs";
        
    private enum HomologyType {
        ORTHOLOGS,
        PARALOGS
    }
    
    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, GeneHomologsDAO.Attribute> columnToAttributesMap;

    static {
        columnToAttributesMap = new HashMap<>();
        columnToAttributesMap.put("bgeeGeneId", GeneHomologsDAO.Attribute.BGEE_GENE_ID);
        columnToAttributesMap.put("targetGeneId", GeneHomologsDAO.Attribute.TARGET_ENSEMBL_ID);
        columnToAttributesMap.put("taxonId", GeneHomologsDAO.Attribute.TAXON_ID);
    }

    @Override
    public GeneHomologsTOResultSet getOrthologousGenes(Set<Integer> bgeeGeneIds) {
        log.entry(bgeeGeneIds);
        return log.exit(getOrthologousGenesAtTaxonLevel(bgeeGeneIds, null, null));
    }

    @Override
    public GeneHomologsTOResultSet getOrthologousGenesAtTaxonLevel(Set<Integer> bgeeGeneIds, 
            Integer taxonId, Set<Integer> speciesIds) {
        log.entry(bgeeGeneIds);
        return log.exit(getOneTypeOfHomology(bgeeGeneIds, taxonId, speciesIds, HomologyType.ORTHOLOGS));
    }

    @Override
    public GeneHomologsTOResultSet getParalogousGenes(Set<Integer> bgeeGeneIds) {
        log.entry(bgeeGeneIds);
        return log.exit(getParalogousGenesAtTaxonLevel(bgeeGeneIds, null, null));
    }

    @Override
    public GeneHomologsTOResultSet getParalogousGenesAtTaxonLevel(Set<Integer> bgeeGeneIds, 
            Integer taxonId, Set<Integer> speciesIds) {
        log.entry(bgeeGeneIds);
        return log.exit(getOneTypeOfHomology(bgeeGeneIds, taxonId, speciesIds, HomologyType.PARALOGS));
    }
    
    private GeneHomologsTOResultSet getOneTypeOfHomology(Set<Integer> bgeeGeneIds, Integer taxonId, 
            Set<Integer> speciesIds, HomologyType homologyType) {
        log.entry(bgeeGeneIds, taxonId, homologyType, speciesIds);
        
     // Filter arguments
        Set<Integer> clonedGeneIds = Optional.ofNullable(bgeeGeneIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        Set<Integer> clonedSpeciesIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        Integer clonedTaxonId = taxonId;
        HomologyType clonedHomologyType = homologyType;
        if (clonedGeneIds == null || homologyType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "homologyType and bgeeGeneId can not be null"));
        }
        // the table to query depends of the homologyType
        String tableName;
        if (clonedHomologyType == HomologyType.PARALOGS) {
            tableName = ORTHOLOGS_TABLE_NAME;
        } else if (clonedHomologyType == HomologyType.PARALOGS) {
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
        sql += " WHERE (" + tableName + ".bgeeGeneId IN ("
                + BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size())
                + ")";
        if (clonedTaxonId != null) {
            sql += " AND t3.taxonId = ?";
        }
        if (clonedSpeciesIds != null) {
            sql += " AND " + tableName + ".speciesId IN ("
                    + BgeePreparedStatement.generateParameterizedQueryString(clonedSpeciesIds.size())
                    + ")";
        }
        
     // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setIntegers(1, bgeeGeneIds, true);
            int offsetParamIndex = clonedGeneIds.size();
            stmt.setIntegers(offsetParamIndex, clonedGeneIds, true);
            offsetParamIndex += clonedGeneIds.size();
            if (clonedTaxonId != null) {
                stmt.setInt(offsetParamIndex, clonedTaxonId);
                offsetParamIndex++;
            }
            if (clonedSpeciesIds != null) {
                stmt.setIntegers(offsetParamIndex, clonedSpeciesIds, true);
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
            Integer bgeeGeneId = null;
            Integer targetGeneId = null;
            Integer taxonId = null;
            // Get results
            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("bgeeGeneId")) {
                        bgeeGeneId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("targetGeneId")) {
                        targetGeneId = this.getCurrentResultSet().getInt(column.getKey());

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
            return log.exit(new GeneHomologsTO(bgeeGeneId, targetGeneId, taxonId));
        }
    }

}
