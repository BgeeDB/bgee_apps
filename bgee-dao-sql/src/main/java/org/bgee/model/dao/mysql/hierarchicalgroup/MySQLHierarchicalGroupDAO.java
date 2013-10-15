package org.bgee.model.dao.mysql.hierarchicalgroup;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/*
import org.bgee.model.dao.common.hierarchicalGroup.HierarchicalGroupDAO;
import org.bgee.model.dao.mysql.BgeeConnection;
import org.bgee.model.dao.mysql.BgeePreparedStatement;
import org.bgee.model.dao.sql.*;*/

public class MySQLHierarchicalGroupDAO /*implements HierarchicalGroupDAO*/ {
//
//	BgeeConnection connection;
//
//	private final static Logger log = LogManager
//			.getLogger(MySQLHierarchicalGroupDAO.class.getName());
//
//	/**
//	 * Retrieves all the orthologus genes corresponding to the queried gene at
//	 * the taxonomy level specified.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID, and a {@code long} representing the NCBI taxonomy ID for
//	 * the taxonomy level queried. Then, the orthologus genes for the submitted
//	 * gene ID at the particular taxonomy level are retrieved and returned as a
//	 * {@code Collection} of {@code String}.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            orthologus genes are to be retrieved.
//	 * 
//	 * @param ncbiTaxonomyId
//	 *            A {@code long} representing the NCBI taxonomy ID of the
//	 *            hierarchical level queried.
//	 * @return A {@code Collection} of {@code String} containing all
//	 *         the orthologus genes of the query gene corresponding to the
//	 *         taxonomy level queried.
//	 * 
//	 * @throws SQLException
//	 */
//	public ArrayList<String> getHierarchicalOrthologusGenes(String queryGene,
//			String ncbiTaxonomyId) throws SQLException {
//
//		log.entry();
//
//		ArrayList<String> orthologusGenes = new ArrayList<String>();
//
//		String sql = "SELECT t5.* FROM gene AS t1 "
//				+ "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
//				+ "INNER JOIN hierarchicalGroup AS t3 "
//				+ "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
//				+ "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
//				+ "t3.orthologousGroupId = t2.orthologousGroupId "
//				+ "INNER JOIN hierarchicalGroup AS t4 "
//				+ "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
//				+ "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
//				+ "t4.orthologousGroupId = t3.orthologousGroupId "
//				+ "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
//				+ "WHERE t1.geneId =? and t3.ncbiTaxonomyId=? ;";
//
//		if (log.isDebugEnabled()) {
//			log.debug("QUERY: {}", sql);
//		}
//
//		try {
//			BgeePreparedStatement preparedStatement = connection
//					.prepareStatement(sql);
//			preparedStatement.setString(1, queryGene);
//			preparedStatement.setString(2, ncbiTaxonomyId);
//			ResultSet resultSet = preparedStatement.executeQuery();
//
//			while (resultSet.next()) {
//				orthologusGenes.add(resultSet.getString("geneId"));
//			}
//
//		} catch (SQLException e) {
//			System.out.println(e.toString());
//		} finally {
//			connection.close();
//		}
//
//		return log.exit(orthologusGenes);
//	}
//
//	/**
//	 * Retrieves all the orthologus genes corresponding to the queried gene at
//	 * the taxonomy level specified, belonging to a list of species.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID, a {@code long} representing the NCBI taxonomy ID for the
//	 * taxonomy level queried, and an {@code ArrayList} representing the
//	 * list of species whose genes are required. Then, the orthologus genes for
//	 * the submitted gene ID at the particular taxonomy level, belonging to each
//	 * of the species submitted are retrieved and returned as a
//	 * {@code Collection} of {@code String}.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            orthologus genes are to be retrieved.
//	 * 
//	 * @param ncbiTaxonomyId
//	 *            A {@code long} representing the NCBI taxonomy ID of the
//	 *            hierarchical level queried.
//	 * @param speciesIds
//	 *            An {@code ArrayList} representing the list of species
//	 *            whose genes are required
//	 * 
//	 * @return A {@code Collection} of {@code String} containing all
//	 *         the orthologus genes of the query gene corresponding to the
//	 *         taxonomy level queried.
//	 * 
//	 * @throws SQLException
//	 * 
//	 */
//	public ArrayList<String> getHierarchicalOrthologusGenesForSpecies(
//			String queryGene, String ncbiTaxonomyId, ArrayList<Long> speciesIds)
//			throws SQLException {
//
//		log.entry();
//
//		ArrayList<String> orthologusGenes = new ArrayList<String>();
//
//		for (long speciesId : speciesIds) {
//
//			String sql = "SELECT t5.* FROM gene AS t1 "
//					+ "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
//					+ "INNER JOIN hierarchicalGroup AS t3 "
//					+ "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
//					+ "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
//					+ "t3.orthologousGroupId = t2.orthologousGroupId "
//					+ "INNER JOIN hierarchicalGroup AS t4 "
//					+ "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
//					+ "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
//					+ "t4.orthologousGroupId = t3.orthologousGroupId "
//					+ "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
//					+ "WHERE t1.geneId =? and t3.ncbiTaxonomyId=? and t5.speciesId=? ;";
//
//			if (log.isDebugEnabled()) {
//				log.debug("QUERY: {}", sql);
//			}
//
//			try {
//				BgeePreparedStatement preparedStatement = connection
//						.prepareStatement(sql);
//				preparedStatement.setString(1, queryGene);
//				preparedStatement.setString(2, ncbiTaxonomyId);
//				preparedStatement.setLong(3, speciesId);
//				ResultSet resultSet = preparedStatement.executeQuery();
//
//				while (resultSet.next()) {
//					orthologusGenes.add(resultSet.getString("geneId"));
//				}
//
//			} catch (SQLException e) {
//				System.out.println(e.toString());
//			} finally {
//				connection.close();
//			}
//		}
//
//		return log.exit(orthologusGenes);
//	}
//
//	/**
//	 * 
//	 * Retrieves all the within species paralogs of the queried gene.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID. Then, all the within species paralogs of the the submitted gene
//	 * ID are retrieved and returned as a {@code Collection} of
//	 * {@code String}.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            within species paralogs are to be retrieved.
//	 * 
//	 * @return A {@code Collection} of {@code String} containing all
//	 *         the within species paralogs genes of the query gene.
//	 * 
//	 * @throws SQLException
//	 * 
//	 */
//	public ArrayList<String> getWithinSpeciesParalogs(String queryGene)
//			throws SQLException {
//
//		log.entry();
//
//		ArrayList<String> paralogusGenes = new ArrayList<String>();
//
//		String sql = "SELECT t6.* FROM gene AS t6"
//				+ "INNER JOIN hierarchicalGroup AS t5 "
//				+ " ON t6.hierarchicalGroupId = t5.hierarchicalGroupId "
//				+ " INNER JOIN hierarchicalGroup AS t4"
//				+ " ON t4.hierarchicalGroupId = ( "
//				+ " 	SELECT t3.hierarchicalGroupId FROM gene AS t1 "
//				+ " 	INNER JOIN hierarchicalGroup AS t2  "
//				+ " 	ON t1.hierarchicalGroupId = t2.hierarchicalGroupId  "
//				+ " 	INNER JOIN hierarchicalGroup AS t3  "
//				+ " 	ON t2.orthologousGroupId = t3.orthologousGroupId "
//				+ " 	AND  t3.hierarchicalGroupLeftBound < t2.hierarchicalGroupLeftBound "
//				+ " 	AND  t3.hierarchicalGroupRightBound > t2.hierarchicalGroupRightBound "
//				+ " 	WHERE t1.geneId = 'ENSXETG00000028037' AND t3.ncbiTaxonomyId='null'  "
//				+ " 	ORDER BY t3.hierarchicalGroupLeftBound DESC LIMIT 1 "
//				+ " )AND t5.orthologousGroupId=t4.orthologousGroupId "
//				+ " AND  t5.hierarchicalGroupLeftBound > t4.hierarchicalGroupLeftBound "
//				+ " AND  t5.hierarchicalGroupRightBound < t4.hierarchicalGroupRightBound; ";
//
//		if (log.isDebugEnabled()) {
//			log.debug("QUERY: {}", sql);
//		}
//
//		try {
//			BgeePreparedStatement preparedStatement = connection
//					.prepareStatement(sql);
//
//			preparedStatement.setString(1, queryGene);
//
//			ResultSet resultSet = preparedStatement.executeQuery();
//
//			while (resultSet.next()) {
//				paralogusGenes.add(resultSet.getString("geneId"));
//			}
//
//		} catch (SQLException e) {
//			System.out.println(e.toString());
//		} finally {
//			connection.close();
//		}
//
//		return log.exit(paralogusGenes);
//	}
//
//	/**
//	 * Retrieves all the orthologus genes corresponding to the queried gene in
//	 * the closest species.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID. Then, the orthologus genes for the submitted gene ID belonging
//	 * closest species are retrieved and returned as a {@code Collection}
//	 * of {@code String}.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            orthologus genes in it's closest species are to be retrieved.
//	 * 
//	 * @return A {@code Collection} of {@code String} containing all
//	 *         the orthologus genes of the query gene in the closest species.
//	 * 
//	 * @throws SQLException
//	 * 
//	 */
//	public ArrayList<String> getOrthologsInClosestSpecies(String queryGene)
//			throws SQLException {
//
//		log.entry();
//
//		ArrayList<String> orthologusGenes = new ArrayList<String>();
//
//		String sql = "SELECT t6.* FROM gene AS t6"
//				+ " INNER JOIN hierarchicalGroup AS t5 "
//				+ " ON t6.hierarchicalGroupId = t5.hierarchicalGroupId "
//				+ " INNER JOIN hierarchicalGroup AS t4"
//				+ " ON t4.hierarchicalGroupId = ( "
//				+ " 	SELECT t3.hierarchicalGroupId FROM gene AS t1 "
//				+ " 	INNER JOIN hierarchicalGroup AS t2  "
//				+ " 	ON t1.hierarchicalGroupId = t2.hierarchicalGroupId  "
//				+ " 	INNER JOIN hierarchicalGroup AS t3  "
//				+ " 	ON t2.orthologousGroupId = t3.orthologousGroupId "
//				+ " 	AND  t3.hierarchicalGroupLeftBound < t2.hierarchicalGroupLeftBound "
//				+ " 	AND  t3.hierarchicalGroupRightBound > t2.hierarchicalGroupRightBound "
//				+ " 	WHERE t1.geneId = ? AND t3.ncbiTaxonomyId!='null' "
//				+ " 	ORDER BY t3.hierarchicalGroupLeftBound DESC LIMIT 1 "
//				+ " )AND t5.orthologousGroupId=t4.orthologousGroupId "
//				+ " AND  t5.hierarchicalGroupLeftBound > t4.hierarchicalGroupLeftBound "
//				+ " AND  t5.hierarchicalGroupRightBound < t4.hierarchicalGroupRightBound; ";
//
//		if (log.isDebugEnabled()) {
//			log.debug("QUERY: {}", sql);
//		}
//
//		try {
//			BgeePreparedStatement preparedStatement = connection
//					.prepareStatement(sql);
//
//			preparedStatement.setString(1, queryGene);
//
//			ResultSet resultSet = preparedStatement.executeQuery();
//
//			while (resultSet.next()) {
//				orthologusGenes.add(resultSet.getString("geneId"));
//			}
//
//			if (log.isDebugEnabled()) {
//
//			}
//
//		} catch (SQLException e) {
//			System.out.println(e.toString());
//		} finally {
//			connection.close();
//		}
//
//		return log.exit(orthologusGenes);
//	}
//
//	/**
//	 * Retrieves all the orthologus genes corresponding to the queried gene in a
//	 * list of species.
//	 * <p>
//	 * This method takes as parameters a {@code String} representing the
//	 * gene ID, and an {@code ArrayList} representing the list of species
//	 * representing the list of species IDs. Then, the orthologus genes for the
//	 * submitted gene ID belonging to these species are retrieved and returned
//	 * as a {@code Collection} of {@code String}.
//	 * 
//	 * @param queryGene
//	 *            A {@code String} representing the gene ID queried, whose
//	 *            orthologus genes in it's closest species are to be retrieved.
//	 * 
//	 * @param speciesIds
//	 *            An {@code ArrayList} representing the list of species
//	 *            whose genes are required
//	 * 
//	 * @return A {@code Collection} of {@code String} containing all
//	 *         the orthologus genes of the query gene in the closest species.
//	 * 
//	 * @throws SQLException
//	 * 
//	 */
//	public ArrayList<String> getAllHierarchicalOrthologuesForSpecies(
//			String queryGene, ArrayList<Long> speciesIds) throws SQLException {
//
//		log.entry();
//
//		ArrayList<String> orthologusGenes = new ArrayList<String>();
//
//		for (long speciesId : speciesIds) {
//
//			String sql = "SELECT t5.* FROM gene AS t1 "
//					+ "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
//					+ "INNER JOIN hierarchicalGroup AS t3 "
//					+ "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
//					+ "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
//					+ "t3.orthologousGroupId = t2.orthologousGroupId "
//					+ "INNER JOIN hierarchicalGroup AS t4 "
//					+ "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
//					+ "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
//					+ "t4.orthologousGroupId = t3.orthologousGroupId "
//					+ "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
//					+ "WHERE t1.geneId =? and t3.hierarchicalGroupLeftBound='1' and t5.speciesId=? ;";
//
//			if (log.isDebugEnabled()) {
//				log.debug("QUERY: {}", sql);
//			}
//
//			try {
//				BgeePreparedStatement preparedStatement = connection
//						.prepareStatement(sql);
//
//				preparedStatement.setString(1, queryGene);
//				preparedStatement.setLong(2, speciesId);
//
//				ResultSet resultSet = preparedStatement.executeQuery();
//
//				while (resultSet.next()) {
//					orthologusGenes.add(resultSet.getString("geneId"));
//				}
//
//				if (log.isDebugEnabled()) {
//
//				}
//
//			} catch (SQLException e) {
//				System.out.println(e.toString());
//			} finally {
//				connection.close();
//			}
//		}
//
//		return log.exit(orthologusGenes);
//
//	}

}
