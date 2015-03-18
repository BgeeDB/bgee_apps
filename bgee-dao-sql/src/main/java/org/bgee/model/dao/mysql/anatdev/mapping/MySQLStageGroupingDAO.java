package org.bgee.model.dao.mysql.anatdev.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

public class MySQLStageGroupingDAO extends MySQLDAO implements StageGroupingDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLStageGroupingDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLStageGroupingDAO(MySQLDAOManager manager)
            throws IllegalArgumentException {
        super(manager);
    }

    @Override
    //TODO: integration test
    public GroupToStageTOResultSet getGroupToStage(String ancestralTaxonId, 
            Set<String> speciesIds) throws DAOException {
        log.entry(ancestralTaxonId, speciesIds);
        //as of Bgee 13, there is no mapping between stages, so we basically 
        //simply retrieve grouping stages existing in all the provided species, 
        //we don't use the ancestralTaxonId.
        boolean hasSpecies = speciesIds != null && !speciesIds.isEmpty();
        
        //As there is currently no mapping, we simply use the stageId as groupId
        String sql = "SELECT DISTINCT t1.stageId AS stageGroupId, t1.stageId "
                + "FROM stage AS t1 ";
        if (hasSpecies) {
            sql += "INNER JOIN stageTaxonConstraint AS t2 ON t1.stageId = t2.stageId "
                //count the number of requested species where the stage exists
                + "LEFT OUTER JOIN "
                + "(SELECT t10.stageId, COUNT(DISTINCT t11.speciesId) AS speciesCount "
                + "FROM stage AS t10 "
                + "INNER JOIN stageTaxonConstraint AS t11 ON t10.stageId = t11.stageId "
                + "WHERE t11.speciesId IN (" + 
                BgeePreparedStatement.generateParameterizedQueryString(speciesIds.size()) 
                + ") GROUP BY t10.stageId) AS stageSpeciesCountTable "
                + "ON t1.stageId = stageSpeciesCountTable.stageId ";
        }
        sql += "WHERE t1.groupingStage = 1 ";
        if (hasSpecies) {
            sql += "AND (t2.speciesId IS NULL "
                    + "OR (stageSpeciesCountTable.stageId IS NOT NULL "
                    + "AND stageSpeciesCountTable.speciesCount = ?))";
        }
        
      //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (hasSpecies) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
                stmt.setInt(1 + speciesIds.size(), speciesIds.size());
            }
            
            return log.exit(new MySQLGroupToStageTOResultSet(stmt));
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
        
//        query if we were to use the ancestral taxon ID
//        String sql = "SELECT DISTINCT t1.stageId AS stageGroupId, t1.stageId "
//                + "FROM stage AS t1 "
//                + "INNER JOIN stageTaxonConstraint AS t2 ON t1.stageId = t2.stageId "
//                //count the number of species member of the taxon where the stage exists
//                + "LEFT OUTER JOIN "
//                + "(SELECT t10.stageId, COUNT(DISTINCT t11.speciesId) AS speciesCount "
//                + "FROM stage AS t10 "
//                + "INNER JOIN stageTaxonConstraint AS t11 ON t10.stageId = t11.stageId "
//                + "INNER JOIN species AS t12 ON t11.speciesId = t12.speciesId "
//                + "INNER JOIN taxon AS t13 ON t12.taxonId = t13.taxonId "
//                + "INNER JOIN taxon AS t14 ON t14.taxonLeftBound <= t13.taxonLeftBound "
//                    + "AND t14.taxonRightBound >= t13.taxonRightBound "
//                + "WHERE t14.taxonId = ? GROUP BY t10.stageId) AS stageSpeciesCountTable "
//                + "ON t1.stageId = stageSpeciesCountTable.stageId "
//                //get stages that exist in all species, or that exist in all species 
//                //member of the taxon
//                + "WHERE t1.groupingStage = 1 AND "
//                + "(t2.speciesId IS NULL "
//                + "OR (stageSpeciesCountTable.stageId IS NOT NULL "
//                + "AND stageSpeciesCountTable.speciesCount ="
//                //get number of species part of the taxon
//                + "(SELECT COUNT(DISTINCT t22.speciesId) AS speciesCount FROM taxon AS t20 "
//                + "INNER JOIN taxon AS t21 ON t21.taxonLeftBound >= t20.taxonLeftBound "
//                    + "AND t21.taxonRightBound <= t20.taxonRightBound "
//                + "INNER JOIN species AS t22 ON t21.taxonId = t22.taxonId "
//                + "WHERE t20.taxonId = ?)))";
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code GroupToStageTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2013
     * @since Bgee 13
     */
    public class MySQLGroupToStageTOResultSet extends MySQLDAOResultSet<GroupToStageTO> 
                implements GroupToStageTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLGroupToStageTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        
        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement, 
         * int, int, int)} super constructor.
         * 
         * @param statement             The first {@code BgeePreparedStatement} to execute 
         *                              a query on.
         * @param offsetParamIndex      An {@code int} that is the index of the parameter 
         *                              defining the offset argument of a LIMIT clause, 
         *                              in the SQL query hold by {@code statement}.
         * @param rowCountParamIndex    An {@code int} that is the index of the parameter 
         *                              specifying the maximum number of rows to return 
         *                              in a LIMIT clause, in the SQL query 
         *                              hold by {@code statement}.
         * @param rowCount              An {@code int} that is the maximum number of rows to use 
         *                              in a LIMIT clause, in the SQL query 
         *                              hold by {@code statement}.
         * @param filterDuplicates      A {@code boolean} defining whether equal 
         *                              {@code TransferObject}s returned by different queries should 
         *                              be filtered: when {@code true}, only one of them will be 
         *                              returned. This implies that all {@code TransferObject}s 
         *                              returned will be stored, implying potentially 
         *                              great memory usage.
         */
        private MySQLGroupToStageTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }

        @Override
        protected GroupToStageTO getNewTO() throws DAOException {
            log.entry();

            String groupId = null, stageId = null;

            ResultSet currentResultSet = this.getCurrentResultSet();
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("stageGroupId")) {
                        groupId = currentResultSet.getString(column.getKey());
                        
                    } else if (column.getValue().equals("stageId")) {
                        stageId = currentResultSet.getString(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.exit(new GroupToStageTO(groupId, stageId));
        }
    }
}
