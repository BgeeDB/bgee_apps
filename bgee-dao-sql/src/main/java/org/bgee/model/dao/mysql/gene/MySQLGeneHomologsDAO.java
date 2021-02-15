package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;
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

/**
 * Implementation of {@code GeneHomologsDAO} for MySQL. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14.2, Feb. 2021
 * @since   Bgee 14.2, Feb. 2021
 */
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
        columnToAttributesMap.put("targetGeneId", GeneHomologsDAO.Attribute.TARGET_BGEE_GENE_ID);
        columnToAttributesMap.put("taxonId", GeneHomologsDAO.Attribute.TAXON_ID);
    }

    @Override
    public GeneHomologsTOResultSet getOrthologousGenes(Collection<Integer> bgeeGeneIds) {
        log.entry(bgeeGeneIds);
        return log.traceExit(getOrthologousGenesAtTaxonLevel(bgeeGeneIds, null, false, null));
    }

    @Override
    public GeneHomologsTOResultSet getOrthologousGenesAtTaxonLevel(Collection<Integer> bgeeGeneIds, 
            Integer taxonId, boolean withDescendantTaxon, Collection<Integer> speciesIds) {
        log.entry(bgeeGeneIds, taxonId, withDescendantTaxon, speciesIds);
        return log.traceExit(getOneTypeOfHomology(bgeeGeneIds, taxonId, withDescendantTaxon, 
                speciesIds, HomologyType.ORTHOLOGS));
    }

    @Override
    public GeneHomologsTOResultSet getParalogousGenes(Collection<Integer> bgeeGeneIds) {
        log.entry(bgeeGeneIds);
        return log.traceExit(getParalogousGenesAtTaxonLevel(bgeeGeneIds, null, false, null));
    }

    @Override
    public GeneHomologsTOResultSet getParalogousGenesAtTaxonLevel(Collection<Integer> bgeeGeneIds, 
            Integer taxonId, boolean withDescendantTaxon, Collection<Integer> speciesIds) {
        log.entry(bgeeGeneIds, taxonId, withDescendantTaxon, speciesIds);
        return log.traceExit(getOneTypeOfHomology(bgeeGeneIds, taxonId, withDescendantTaxon, 
                speciesIds, HomologyType.PARALOGS));
    }
    
    private GeneHomologsTOResultSet getOneTypeOfHomology(Collection<Integer> bgeeGeneIds, Integer taxonId, 
            boolean withDescendantTaxon, Collection<Integer> speciesIds, HomologyType homologyType) {
        log.entry(bgeeGeneIds, taxonId, homologyType, speciesIds);
        
     // Filter arguments
        Set<Integer> clonedGeneIds = Optional.ofNullable(bgeeGeneIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        Set<Integer> clonedSpeciesIds = Optional.ofNullable(speciesIds)
                .map(c -> new HashSet<>(c)).orElse(null);
        if (clonedGeneIds == null || clonedGeneIds.isEmpty() ||
                clonedGeneIds.stream().anyMatch(g -> g == null) || homologyType == null) {
            throw log.throwing(new IllegalArgumentException(
                    "homologyType and bgeeGeneId can not be null"));
        }
        // the table to query depends of the homologyType
        String tableName;
        if (homologyType == HomologyType.ORTHOLOGS) {
            tableName = ORTHOLOGS_TABLE_NAME;
        } else if (homologyType == HomologyType.PARALOGS) {
            tableName = PARALOGS_TABLE_NAME;
        } else {
            throw log.throwing(new IllegalArgumentException(
                    "unknown homology type"));
        }
        
        // Create 2 queries. One to filter bgeeGeneId on 1st column and one to filter bgeeGeneId on 2nd column
        String sqlFirstColumn = "SELECT DISTINCT " + tableName + ".bgeeGeneId as bgeeGeneId, "
                + tableName + ".targetGeneId as targetGeneId, " + tableName + ".taxonId"
                + " from " + tableName;
        String sqlSecondColumn = "SELECT DISTINCT " + tableName + ".targetGeneId as bgeeGeneId, "
                + tableName + ".bgeeGeneId as targetGeneId, " + tableName + ".taxonId"
                + " from " + tableName;
        
        if (taxonId != null && withDescendantTaxon) {
            String join = " INNER JOIN taxon AS t2 ON t2.taxonId = " + tableName + ".taxonId "
                    + "INNER JOIN taxon AS t3 ON t2.taxonLeftBound >= t3.taxonLeftBound AND "
                    + "t2.taxonRightBound <= t3.taxonRightBound";
            sqlFirstColumn += join;
            sqlSecondColumn += join;
        }
        
        if (clonedSpeciesIds != null) {
            sqlFirstColumn += " INNER JOIN gene AS t4 ON " + tableName + ".targetGeneId = t4.bgeeGeneId";
            sqlSecondColumn += " INNER JOIN gene AS t4 ON " + tableName + ".bgeeGeneId = t4.bgeeGeneId";
        }


        sqlFirstColumn += " WHERE " + tableName + ".bgeeGeneId IN ("
                + BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size())
                + ")";
        sqlSecondColumn += " WHERE " + tableName + ".targetGeneId IN ("
                + BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size())
                + ")";
        if (taxonId != null) {
            String taxonFilter;
            if(withDescendantTaxon) {
                taxonFilter = " AND t3.taxonId = ?";
            } else {
                taxonFilter = " AND " + tableName + ".taxonId = ?";
            }
            sqlFirstColumn += taxonFilter;
            sqlSecondColumn += taxonFilter;
        }
        if (clonedSpeciesIds != null) {
            String speciesFilter = " AND t4.speciesId IN ("
                    + BgeePreparedStatement.generateParameterizedQueryString(clonedSpeciesIds.size())
                    + ")";
            sqlFirstColumn += speciesFilter;
            sqlSecondColumn += speciesFilter;
        }
        
        //create the final sql query corresponding to union of the 2 previously created queries
        String sql = "(" + sqlFirstColumn + ") UNION (" + sqlSecondColumn + ")";
        
        // we don't use a try-with-resource, because we return a pointer to the results,
        // not the actual results, so we should not close this BgeePreparedStatement.
        try {
            log.debug(sql);
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setIntegers(1, clonedGeneIds, true);
            int offsetParamIndex = clonedGeneIds.size() + 1;
            if (taxonId != null) {
                stmt.setInt(offsetParamIndex, taxonId);
                offsetParamIndex++;
            }
            if (clonedSpeciesIds != null) {
                stmt.setIntegers(offsetParamIndex, clonedSpeciesIds, true);
                offsetParamIndex += clonedSpeciesIds.size();
            }
            //same parameters for union subquery requesting second column
            stmt.setIntegers(offsetParamIndex, clonedGeneIds, true);
            offsetParamIndex += clonedGeneIds.size();
            if (taxonId != null) {
                stmt.setInt(offsetParamIndex, taxonId);
                offsetParamIndex++;
            }
            if (clonedSpeciesIds != null) {
                stmt.setIntegers(offsetParamIndex, clonedSpeciesIds, true);
            }
            return log.traceExit(new MySQLGeneHomologsTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public void insertParalogs(Collection<GeneHomologsTO> paralogs) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void insertOrthologs(Collection<GeneHomologsTO> orthologs) {
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
            Integer bgeeGeneId = null, targetGeneId = null, taxonId = null;
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
            return log.traceExit(new GeneHomologsTO(bgeeGeneId, targetGeneId, taxonId));
        }
    }

}
