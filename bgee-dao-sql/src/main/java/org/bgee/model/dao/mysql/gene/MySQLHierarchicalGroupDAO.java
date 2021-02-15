package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
/*
import org.bgee.model.dao.mysql.BgeeConnection;
import org.bgee.model.dao.mysql.BgeePreparedStatement;
import org.bgee.model.dao.sql.*;*/
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code HierarchicalGroupDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @author Julien Wollbrett
 * @version Bgee 14
 * @see org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO
 * @since Bgee 13
 */
public class MySQLHierarchicalGroupDAO extends MySQLDAO<HierarchicalGroupDAO.Attribute>
        implements HierarchicalGroupDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLHierarchicalGroupDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} will
     * use to obtain {@code BgeeConnection}s.
     * 
     * @param manager the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLHierarchicalGroupDAO(MySQLDAOManager manager)
            throws IllegalArgumentException {
        super(manager);
    }
    
    @Override
    public HierarchicalNodeToGeneTOResultSet getOMANodeToGene(Integer taxonId, 
			Collection<Integer> speciesIds) throws DAOException, IllegalArgumentException {
        log.entry(taxonId, speciesIds);

        if (taxonId <= 0) {
            throw log.throwing(new IllegalArgumentException("No taxon ID is provided"));
        }
        
        boolean hasSpecies  = speciesIds != null && !speciesIds.isEmpty();
        
        String sql = "SELECT geneToOma.OMANodeId, geneToOma.bgeeGeneId "
                + "FROM geneToOma "
                + "INNER JOIN gene AS g ON geneToOma.bgeeGeneId = g.bgeeGeneId "
                + "WHERE taxonId = ? ";
        if (hasSpecies) {
            sql += "AND g.speciesId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                            speciesIds.size()) + ")";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setInt(1, taxonId);
            if (hasSpecies) {
                stmt.setIntegers(2, speciesIds, true);
            }  
            return log.traceExit(new MySQLHierarchicalNodeToGeneTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public HierarchicalNodeTOResultSet getOMANodesFromStartingGenes(Collection<Integer> taxonIds, 
    		Integer startingSpeciesId, Set<String> startingGeneIds) throws DAOException, IllegalArgumentException{
    	log.entry(taxonIds, startingSpeciesId, startingGeneIds);
    	if (taxonIds == null || taxonIds.size() == 0) {
            throw log.throwing(new IllegalArgumentException("No taxon ID is provided"));
        }
    	if (startingSpeciesId == null) {
            throw log.throwing(new IllegalArgumentException("No starting species ID is provided"));
        }
    	if (startingGeneIds == null || startingGeneIds.size() == 0) {
            throw log.throwing(new IllegalArgumentException("No starting gene IDs are provided"));
        }
    	
    	boolean hasGenes  = startingSpeciesId != null && startingSpeciesId > 0 && startingGeneIds != null
    			&& startingGeneIds.size() > 0;
    	
        String sql = "SELECT geneToOma.OMANodeId, geneToOma.taxonId, hg.OMAGroupId "
                + "FROM OMAHierarchicalGroup as hg "
                + "INNER JOIN geneToOma ON geneToOma.OMANodeId = hg.OMANodeId"
                + "INNER JOIN gene AS g ON geneToOma.bgeeGeneId = g.bgeeGeneId "
                + "WHERE geneToOma.taxonId IN ? (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                    		taxonIds.size()) + ")";
        if (hasGenes) {
            sql += "AND g.speciesId = ?"
            		+ "AND geneID IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                    		startingGeneIds.size()) + ")"
                    + "ORDERED BY hg.OMAGroupId";
        }
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setIntegers(1, taxonIds, false);
            if (hasGenes) {
                stmt.setInt(2, startingSpeciesId);
                stmt.setStrings(3, startingGeneIds, true);
            }  
            return log.traceExit(new MySQLHierarchicalNodeTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    public HierarchicalNodeToGeneTOResultSet getGenesByNodeFromNodes(Collection<Integer> omaNodesIds,
    		Collection<Integer> speciesIds) throws DAOException, IllegalArgumentException{
    	log.entry(omaNodesIds, speciesIds);
    	if (omaNodesIds == null || omaNodesIds.size() == 0) {
            throw log.throwing(new IllegalArgumentException("No OMA node ID is provided"));
        }
    	boolean hasSpecies  = speciesIds != null && speciesIds.size() > 0;
    	String sql = "SELECT geneToOma.OMANodeId, geneToOma.geneId FROM geneToOma "
                + "INNER JOIN gene AS g ON geneToOma.bgeeGeneId = g.bgeeGeneId "
                + "WHERE geneToOma.OMANodeId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                    		omaNodesIds.size()) + ")";
    	if(hasSpecies){
    		sql += "AND g.speciesId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                    		speciesIds.size()) + ")";
    	}
    	try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            stmt.setIntegers(1, omaNodesIds, true);
            if (hasSpecies) {
                stmt.setIntegers(2, speciesIds, true);
            }  
            return log.traceExit(new MySQLHierarchicalNodeToGeneTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    	
    }

    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************
    @Override
    public int insertHierarchicalNodes(Collection<HierarchicalNodeTO> groups)
            throws DAOException, IllegalArgumentException {
        log.entry(groups);

        if (groups == null || groups.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No hierarchical group is given, then no hierarchical group is inserted"));
        }

        int groupInsertedCount = 0;

        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert terms one at a time
        String sql = "INSERT INTO OMAHierarchicalGroup " +
                     "(OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId) " +
                     "values (?, ?, ?, ?, ?)";

    	try (BgeePreparedStatement stmt = 
    			this.getManager().getConnection().prepareStatement(sql)) {
    		for (HierarchicalNodeTO group: groups) {
    			stmt.setInt(1, group.getId());
    			stmt.setString(2, group.getOMAGroupId());
    			stmt.setInt(3, group.getLeftBound());
    			stmt.setInt(4, group.getRightBound());
    			// taxonId could be null for paralogous groups
    			if (group.getTaxonId() == 0) {
    			    stmt.setNull(5, Types.INTEGER);
    			} else {
    			    stmt.setInt(5, group.getTaxonId());
    			}
    			groupInsertedCount += stmt.executeUpdate();
    			stmt.clearParameters();
    		}
    		return log.traceExit(groupInsertedCount);
    	} catch (SQLException e) {
    		throw log.throwing(new DAOException(e));
    	}
    }

    //XXX: how this was handled before?
    @Override
    public int insertHierarchicalNodeToGene(Collection<HierarchicalNodeToGeneTO> groupToGenes)
            throws DAOException, IllegalArgumentException {
        log.entry(groupToGenes);

        if (groupToGenes == null || groupToGenes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No hierarchical group to gene is given, then no group to gene mapping is inserted"));
        }

        int groupToGeneInsertedCount = 0;

        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException,
        // and because of laziness, we insert terms one at a time
        String sql = "INSERT INTO geneToOma " +
                     "(OMANodeId, bgeeGeneId, taxonId) " +
                     "values (?, ?, ?)";

    	try (BgeePreparedStatement stmt = 
    			this.getManager().getConnection().prepareStatement(sql)) {
    		
    		for (HierarchicalNodeToGeneTO groupToGene: groupToGenes) {
    			stmt.setInt(1, groupToGene.getNodeId());
    			stmt.setInt(2, groupToGene.getBgeeGeneId());
    			stmt.setInt(3, groupToGene.getTaxonId());
    			groupToGeneInsertedCount += stmt.executeUpdate();
    			stmt.clearParameters();
    		}
    		return log.traceExit(groupToGeneInsertedCount);
    	} catch (SQLException e) {
    		throw log.throwing(new DAOException(e));
    	}
    }
    
    /**
    * A {@code MySQLDAOResultSet} specific to {@code HierarchicalNodeToGeneTO}.
    * 
    * @author Frederic Bastian
    * @version Bgee 13 Mar. 2015
    * @since Bgee 13
    */
   public class MySQLHierarchicalNodeTOResultSet 
               extends MySQLDAOResultSet<HierarchicalNodeTO> 
               implements HierarchicalNodeTOResultSet {
	   /**
        * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
        * super constructor.
        * 
        * @param statement The first {@code BgeePreparedStatement} to execute a query on.
        */
       private MySQLHierarchicalNodeTOResultSet(BgeePreparedStatement statement) {
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
       private MySQLHierarchicalNodeTOResultSet(BgeePreparedStatement statement, 
               int offsetParamIndex, int rowCountParamIndex, int rowCount, 
               boolean filterDuplicates) {
           super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
       }
       
       @Override
       protected HierarchicalNodeTO getNewTO() throws DAOException {
           log.entry();

           String hogId = null;
           Integer taxonId = null, nodeId = null; 

           for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
               try {
                   if (column.getValue().equals("OMAGroupId")) {
                       hogId = this.getCurrentResultSet().getString(column.getKey());
                       
                   } else if (column.getValue().equals("bgeeGeneId")) {
                	   taxonId = this.getCurrentResultSet().getInt(column.getKey());

                   } else if (column.getValue().equals("OMANodeId")) {
                	   nodeId = this.getCurrentResultSet().getInt(column.getKey());

                   } else {
                       throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                   }

               } catch (SQLException e) {
                   throw log.throwing(new DAOException(e));
               }
           }
           return log.traceExit(new HierarchicalNodeTO(nodeId, hogId, null, null, taxonId));
       }

   }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code HierarchicalNodeToGeneTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public class MySQLHierarchicalNodeToGeneTOResultSet 
                extends MySQLDAOResultSet<HierarchicalNodeToGeneTO> 
                implements HierarchicalNodeToGeneTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLHierarchicalNodeToGeneTOResultSet(BgeePreparedStatement statement) {
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
        private MySQLHierarchicalNodeToGeneTOResultSet(BgeePreparedStatement statement, 
                int offsetParamIndex, int rowCountParamIndex, int rowCount, 
                boolean filterDuplicates) {
            super(statement, offsetParamIndex, rowCountParamIndex, rowCount, filterDuplicates);
        }
        
        @Override
        protected HierarchicalNodeToGeneTO getNewTO() throws DAOException {
            log.entry();
            Integer hogId = null, geneId = null, taxonId = null; 

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("OMANodeId")) {
                        hogId = this.getCurrentResultSet().getInt(column.getKey());
                        
                    } else if (column.getValue().equals("bgeeGeneId")) {
                        geneId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("taxonId")) {
                        taxonId = this.getCurrentResultSet().getInt(column.getKey());

                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }

                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.traceExit(new HierarchicalNodeToGeneTO(hogId, geneId, taxonId));
        }
    }

//
//    BgeeConnection connection;
//
//    /**
//     * Retrieves all the orthologous genes corresponding to the queried gene at
//     * the taxonomy level specified.
//     * <p>
//     * This method takes as parameters a {@code String} representing the
//     * gene ID, and a {@code long} representing the NCBI taxonomy ID for
//     * the taxonomy level queried. Then, the orthologous genes for the submitted
//     * gene ID at the particular taxonomy level are retrieved and returned as a
//     * {@code Collection} of {@code String}.
//     * 
//     * @param queryGene
//     *            A {@code String} representing the gene ID queried, whose
//     *            orthologous genes are to be retrieved.
//     * 
//     * @param ncbiTaxonomyId
//     *            A {@code long} representing the NCBI taxonomy ID of the
//     *            hierarchical level queried.
//     * @return A {@code Collection} of {@code String} containing all
//     *         the orthologous genes of the query gene corresponding to the
//     *         taxonomy level queried.
//     * 
//     * @throws SQLException
//     */
//    public ArrayList<String> getHierarchicalOrthologousGenes(String queryGene,
//            String ncbiTaxonomyId) throws SQLException {
//
//        log.entry();
//
//        ArrayList<String> orthologousGenes = new ArrayList<String>();
//
//        String sql = "SELECT t5.* FROM gene AS t1 "
//                + "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
//                + "INNER JOIN hierarchicalGroup AS t3 "
//                + "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
//                + "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
//                + "t3.orthologousGroupId = t2.orthologousGroupId "
//                + "INNER JOIN hierarchicalGroup AS t4 "
//                + "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
//                + "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
//                + "t4.orthologousGroupId = t3.orthologousGroupId "
//                + "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
//                + "WHERE t1.geneId =? and t3.ncbiTaxonomyId=? ;";
//
//        if (log.isDebugEnabled()) {
//            log.debug("QUERY: {}", sql);
//        }
//
//        try {
//            BgeePreparedStatement preparedStatement = connection
//                    .prepareStatement(sql);
//            preparedStatement.setString(1, queryGene);
//            preparedStatement.setString(2, ncbiTaxonomyId);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                orthologousGenes.add(resultSet.getString("geneId"));
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.toString());
//        } finally {
//            connection.close();
//        }
//
//        return log.traceExit(orthologousGenes);
//    }
//
//    /**
//     * Retrieves all the orthologous genes corresponding to the queried gene at
//     * the taxonomy level specified, belonging to a list of species.
//     * <p>
//     * This method takes as parameters a {@code String} representing the
//     * gene ID, a {@code long} representing the NCBI taxonomy ID for the
//     * taxonomy level queried, and an {@code ArrayList} representing the
//     * list of species whose genes are required. Then, the orthologous genes for
//     * the submitted gene ID at the particular taxonomy level, belonging to each
//     * of the species submitted are retrieved and returned as a
//     * {@code Collection} of {@code String}.
//     * 
//     * @param queryGene
//     *            A {@code String} representing the gene ID queried, whose
//     *            orthologous genes are to be retrieved.
//     * 
//     * @param ncbiTaxonomyId
//     *            A {@code long} representing the NCBI taxonomy ID of the
//     *            hierarchical level queried.
//     * @param speciesIds
//     *            An {@code ArrayList} representing the list of species
//     *            whose genes are required
//     * 
//     * @return A {@code Collection} of {@code String} containing all
//     *         the orthologous genes of the query gene corresponding to the
//     *         taxonomy level queried.
//     * 
//     * @throws SQLException
//     * 
//     */
//    public ArrayList<String> getHierarchicalOrthologousGenesForSpecies(
//            String queryGene, String ncbiTaxonomyId, ArrayList<Long> speciesIds)
//            throws SQLException {
//
//        log.entry();
//
//        ArrayList<String> orthologousGenes = new ArrayList<String>();
//
//        for (long speciesId : speciesIds) {
//
//            String sql = "SELECT t5.* FROM gene AS t1 "
//                    + "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
//                    + "INNER JOIN hierarchicalGroup AS t3 "
//                    + "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
//                    + "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
//                    + "t3.orthologousGroupId = t2.orthologousGroupId "
//                    + "INNER JOIN hierarchicalGroup AS t4 "
//                    + "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
//                    + "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
//                    + "t4.orthologousGroupId = t3.orthologousGroupId "
//                    + "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
//                    + "WHERE t1.geneId =? and t3.ncbiTaxonomyId=? and t5.speciesId=? ;";
//
//            if (log.isDebugEnabled()) {
//                log.debug("QUERY: {}", sql);
//            }
//
//            try {
//                BgeePreparedStatement preparedStatement = connection
//                        .prepareStatement(sql);
//                preparedStatement.setString(1, queryGene);
//                preparedStatement.setString(2, ncbiTaxonomyId);
//                preparedStatement.setLong(3, speciesId);
//                ResultSet resultSet = preparedStatement.executeQuery();
//
//                while (resultSet.next()) {
//                    orthologousGenes.add(resultSet.getString("geneId"));
//                }
//
//            } catch (SQLException e) {
//                System.out.println(e.toString());
//            } finally {
//                connection.close();
//            }
//        }
//
//        return log.traceExit(orthologousGenes);
//    }
//
//    /**
//     * 
//     * Retrieves all the within species paralogs of the queried gene.
//     * <p>
//     * This method takes as parameters a {@code String} representing the
//     * gene ID. Then, all the within species paralogs of the the submitted gene
//     * ID are retrieved and returned as a {@code Collection} of
//     * {@code String}.
//     * 
//     * @param queryGene
//     *            A {@code String} representing the gene ID queried, whose
//     *            within species paralogs are to be retrieved.
//     * 
//     * @return A {@code Collection} of {@code String} containing all
//     *         the within species paralogs genes of the query gene.
//     * 
//     * @throws SQLException
//     * 
//     */
//    public ArrayList<String> getWithinSpeciesParalogs(String queryGene)
//            throws SQLException {
//
//        log.entry();
//
//        ArrayList<String> paralogusGenes = new ArrayList<String>();
//
//        String sql = "SELECT t6.* FROM gene AS t6"
//                + "INNER JOIN hierarchicalGroup AS t5 "
//                + " ON t6.hierarchicalGroupId = t5.hierarchicalGroupId "
//                + " INNER JOIN hierarchicalGroup AS t4"
//                + " ON t4.hierarchicalGroupId = ( "
//                + "     SELECT t3.hierarchicalGroupId FROM gene AS t1 "
//                + "     INNER JOIN hierarchicalGroup AS t2  "
//                + "     ON t1.hierarchicalGroupId = t2.hierarchicalGroupId  "
//                + "     INNER JOIN hierarchicalGroup AS t3  "
//                + "     ON t2.orthologousGroupId = t3.orthologousGroupId "
//                + "     AND  t3.hierarchicalGroupLeftBound < t2.hierarchicalGroupLeftBound "
//                + "     AND  t3.hierarchicalGroupRightBound > t2.hierarchicalGroupRightBound "
//                + "     WHERE t1.geneId = 'ENSXETG00000028037' AND t3.ncbiTaxonomyId='null'  "
//        //TODO: if code reactivated, think of fix for issue #173
//                + "     ORDER BY t3.hierarchicalGroupLeftBound DESC LIMIT 1 "
//                + " )AND t5.orthologousGroupId=t4.orthologousGroupId "
//                + " AND  t5.hierarchicalGroupLeftBound > t4.hierarchicalGroupLeftBound "
//                + " AND  t5.hierarchicalGroupRightBound < t4.hierarchicalGroupRightBound; ";
//
//        if (log.isDebugEnabled()) {
//            log.debug("QUERY: {}", sql);
//        }
//
//        try {
//            BgeePreparedStatement preparedStatement = connection
//                    .prepareStatement(sql);
//
//            preparedStatement.setString(1, queryGene);
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                paralogusGenes.add(resultSet.getString("geneId"));
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.toString());
//        } finally {
//            connection.close();
//        }
//
//        return log.traceExit(paralogusGenes);
//    }
//
//    /**
//     * Retrieves all the orthologous genes corresponding to the queried gene in
//     * the closest species.
//     * <p>
//     * This method takes as parameters a {@code String} representing the
//     * gene ID. Then, the orthologous genes for the submitted gene ID belonging
//     * closest species are retrieved and returned as a {@code Collection}
//     * of {@code String}.
//     * 
//     * @param queryGene
//     *            A {@code String} representing the gene ID queried, whose
//     *            orthologous genes in it's closest species are to be retrieved.
//     * 
//     * @return A {@code Collection} of {@code String} containing all
//     *         the orthologous genes of the query gene in the closest species.
//     * 
//     * @throws SQLException
//     * 
//     */
//    public ArrayList<String> getOrthologsInClosestSpecies(String queryGene)
//            throws SQLException {
//
//        log.entry();
//
//        ArrayList<String> orthologousGenes = new ArrayList<String>();
//
//        String sql = "SELECT t6.* FROM gene AS t6"
//                + " INNER JOIN hierarchicalGroup AS t5 "
//                + " ON t6.hierarchicalGroupId = t5.hierarchicalGroupId "
//                + " INNER JOIN hierarchicalGroup AS t4"
//                + " ON t4.hierarchicalGroupId = ( "
//                + "     SELECT t3.hierarchicalGroupId FROM gene AS t1 "
//                + "     INNER JOIN hierarchicalGroup AS t2  "
//                + "     ON t1.hierarchicalGroupId = t2.hierarchicalGroupId  "
//                + "     INNER JOIN hierarchicalGroup AS t3  "
//                + "     ON t2.orthologousGroupId = t3.orthologousGroupId "
//                + "     AND  t3.hierarchicalGroupLeftBound < t2.hierarchicalGroupLeftBound "
//                + "     AND  t3.hierarchicalGroupRightBound > t2.hierarchicalGroupRightBound "
//                + "     WHERE t1.geneId = ? AND t3.ncbiTaxonomyId!='null' "
//        //TODO: if code reactivated, think of fix for issue #173 
//                + "     ORDER BY t3.hierarchicalGroupLeftBound DESC LIMIT 1 "
//                + " )AND t5.orthologousGroupId=t4.orthologousGroupId "
//                + " AND  t5.hierarchicalGroupLeftBound > t4.hierarchicalGroupLeftBound "
//                + " AND  t5.hierarchicalGroupRightBound < t4.hierarchicalGroupRightBound; ";
//
//        if (log.isDebugEnabled()) {
//            log.debug("QUERY: {}", sql);
//        }
//
//        try {
//            BgeePreparedStatement preparedStatement = connection
//                    .prepareStatement(sql);
//
//            preparedStatement.setString(1, queryGene);
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                orthologousGenes.add(resultSet.getString("geneId"));
//            }
//
//            if (log.isDebugEnabled()) {
//
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.toString());
//        } finally {
//            connection.close();
//        }
//
//        return log.traceExit(orthologousGenes);
//    }
//
//    /**
//     * Retrieves all the orthologous genes corresponding to the queried gene in a
//     * list of species.
//     * <p>
//     * This method takes as parameters a {@code String} representing the
//     * gene ID, and an {@code ArrayList} representing the list of species
//     * representing the list of species IDs. Then, the orthologous genes for the
//     * submitted gene ID belonging to these species are retrieved and returned
//     * as a {@code Collection} of {@code String}.
//     * 
//     * @param queryGene
//     *            A {@code String} representing the gene ID queried, whose
//     *            orthologous genes in it's closest species are to be retrieved.
//     * 
//     * @param speciesIds
//     *            An {@code ArrayList} representing the list of species
//     *            whose genes are required
//     * 
//     * @return A {@code Collection} of {@code String} containing all
//     *         the orthologous genes of the query gene in the closest species.
//     * 
//     * @throws SQLException
//     * 
//     */
//    public ArrayList<String> getAllHierarchicalOrthologuesForSpecies(
//            String queryGene, ArrayList<Long> speciesIds) throws SQLException {
//
//        log.entry();
//
//        ArrayList<String> orthologousGenes = new ArrayList<String>();
//
//        for (long speciesId : speciesIds) {
//
//            String sql = "SELECT t5.* FROM gene AS t1 "
//                    + "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
//                    + "INNER JOIN hierarchicalGroup AS t3 "
//                    + "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
//                    + "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
//                    + "t3.orthologousGroupId = t2.orthologousGroupId "
//                    + "INNER JOIN hierarchicalGroup AS t4 "
//                    + "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
//                    + "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
//                    + "t4.orthologousGroupId = t3.orthologousGroupId "
//                    + "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
//                    + "WHERE t1.geneId =? and t3.hierarchicalGroupLeftBound='1' and t5.speciesId=? ;";
//
//            if (log.isDebugEnabled()) {
//                log.debug("QUERY: {}", sql);
//            }
//
//            try {
//                BgeePreparedStatement preparedStatement = connection
//                        .prepareStatement(sql);
//
//                preparedStatement.setString(1, queryGene);
//                preparedStatement.setLong(2, speciesId);
//
//                ResultSet resultSet = preparedStatement.executeQuery();
//
//                while (resultSet.next()) {
//                    orthologousGenes.add(resultSet.getString("geneId"));
//                }
//
//                if (log.isDebugEnabled()) {
//
//                }
//
//            } catch (SQLException e) {
//                System.out.println(e.toString());
//            } finally {
//                connection.close();
//            }
//        }
//
//        return log.traceExit(orthologousGenes);
//
//    }

}
