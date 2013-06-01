package org.bgee.model.data.sql.mysql.hierarchicalGroup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bgee.model.data.common.hierarchicalGroup.HierarchicalGroupDAO;

public class MysqlHierarchicalGroupDAO implements HierarchicalGroupDAO {

	/**
	 * Retrieves all the orthologus genes corresponding to the queried gene at
	 * the taxonomy level specified.
	 * <p>
	 * This method takes as parameters a <code>String</code> representing the
	 * gene ID, and a <code>long</code> representing the NCBI taxonomy ID for
	 * the taxonomy level queried. Then, the orthologus genes for the submitted
	 * gene ID at the particular taxonomy level are retrieved and returned as a
	 * <code>Collection</code> of <code>String</code>.
	 * 
	 * @param queryGene
	 *            A <code>String</code> representing the gene ID queried, whose
	 *            orthologus genes are to be retrieved.
	 * 
	 * @param ncbiTaxonomyId
	 *            A <code>long</code> representing the NCBI taxonomy ID of the
	 *            hierarchical level queried.
	 * @return A <code>Collection</code> of <code>String</code> containing all
	 *         the orthologus genes of the query gene corresponding to the
	 *         taxonomy level queried.
	 *         
	 * @throws SQLException
	 */
	public ArrayList<String> getHierarchicalOrthologusGenes(String queryGene,
			String ncbiTaxonomyId) throws SQLException {

		ArrayList<String> orthologusGenes = new ArrayList<String>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}

		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/db", "user", "pass");

		String sql = "SELECT t5.* FROM gene AS t1 "
				+ "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
				+ "INNER JOIN hierarchicalGroup AS t3 "
				+ "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
				+ "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
				+ "t3.orthologousGroupId = t2.orthologousGroupId "
				+ "INNER JOIN hierarchicalGroup AS t4 "
				+ "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
				+ "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
				+ "t4.orthologousGroupId = t3.orthologousGroupId "
				+ "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
				+ "WHERE t1.geneId =? and t3.ncbiTaxonomyId=? ;";

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			preparedStatement.setString(1, queryGene);
			preparedStatement.setString(2, ncbiTaxonomyId);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				orthologusGenes.add(resultSet.getString("geneId"));
			}

		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			connection.close();
		}

		return orthologusGenes;
	}

	/**
	 * Retrieves all the orthologus genes corresponding to the queried gene at
	 * the taxonomy level specified, belonging to a list of species.
	 * <p>
	 * This method takes as parameters a <code>String</code> representing the
	 * gene ID, a <code>long</code> representing the NCBI taxonomy ID for the
	 * taxonomy level queried, and an <code>ArrayList</code> representing the
	 * list of species whose genes are required. Then, the orthologus genes for
	 * the submitted gene ID at the particular taxonomy level, belonging to each
	 * of the species submitted are retrieved and returned as a
	 * <code>Collection</code> of <code>String</code>.
	 * 
	 * @param queryGene
	 *            A <code>String</code> representing the gene ID queried, whose
	 *            orthologus genes are to be retrieved.
	 * 
	 * @param ncbiTaxonomyId
	 *            A <code>long</code> representing the NCBI taxonomy ID of the
	 *            hierarchical level queried.
	 * @param speciesIds
	 *            An <code>ArrayList</code> representing the list of species
	 *            whose genes are required
	 * 
	 * @return A <code>Collection</code> of <code>String</code> containing all
	 *         the orthologus genes of the query gene corresponding to the
	 *         taxonomy level queried.
	 * 
	 * @throws SQLException
	 * 
	 */
	public ArrayList<String> getHierarchicalOrthologusGenesForSpecies(
			String queryGene, String ncbiTaxonomyId, ArrayList<Long> speciesIds)
			throws SQLException {

		ArrayList<String> orthologusGenes = new ArrayList<String>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}

		for (long speciesId : speciesIds) {

			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/omaBgee", "user", "pass");

			String sql = "SELECT t5.* FROM gene AS t1 "
					+ "INNER JOIN hierarchicalGroup AS t2 ON t1.hierarchicalGroupId = t2.hierarchicalGroupId "
					+ "INNER JOIN hierarchicalGroup AS t3 "
					+ "ON t3.hierarchicalGroupLeftBound <= t2.hierarchicalGroupLeftBound AND "
					+ "t3.hierarchicalGroupRightBound >= t2.hierarchicalGroupRightBound AND "
					+ "t3.orthologousGroupId = t2.orthologousGroupId "
					+ "INNER JOIN hierarchicalGroup AS t4 "
					+ "ON t4.hierarchicalGroupLeftBound >= t3.hierarchicalGroupLeftBound AND "
					+ "t4.hierarchicalGroupRightBound <= t3.hierarchicalGroupRightBound AND "
					+ "t4.orthologousGroupId = t3.orthologousGroupId "
					+ "INNER JOIN gene AS t5 ON t5.hierarchicalGroupId = t4.hierarchicalGroupId "
					+ "WHERE t1.geneId =? and t3.ncbiTaxonomyId=? and t5.speciesId=? ;";

			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement(sql);
				preparedStatement.setString(1, queryGene);
				preparedStatement.setString(2, ncbiTaxonomyId);
				preparedStatement.setLong(3, speciesId);
				ResultSet resultSet = preparedStatement.executeQuery();

				while (resultSet.next()) {
					orthologusGenes.add(resultSet.getString("geneId"));
				}

			} catch (SQLException e) {
				System.out.println(e.toString());
			} finally {
				connection.close();
			}
		}

		return orthologusGenes;
	}

	/**
	 * 
	 * Retrieves all the within species paralogs of the queried gene.
	 * <p>
	 * This method takes as parameters a <code>String</code> representing the
	 * gene ID. Then, all the within species paralogs of the the submitted gene
	 * ID are retrieved and returned as a <code>Collection</code> of
	 * <code>String</code>.
	 * 
	 * @param queryGene
	 *            A <code>String</code> representing the gene ID queried, whose
	 *            within species paralogs are to be retrieved.
	 * 
	 * @return A <code>Collection</code> of <code>String</code> containing all
	 *         the within species paralogs genes of the query gene.
	 * 
	 * @throws SQLException
	 * 
	 */
	public ArrayList<String> getWithinSpeciesParalogs(String queryGene)
			throws SQLException {

		ArrayList<String> orthologusGenes = new ArrayList<String>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}

		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/omaBgee", "user", "pass");

		String sql = null; // TODO

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			preparedStatement.setString(1, queryGene);

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				orthologusGenes.add(resultSet.getString("geneId"));
			}

		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			connection.close();
		}

		return orthologusGenes;
	}

	/**
	 * Retrieves all the orthologus genes corresponding to the queried gene in
	 * the closest species.
	 * <p>
	 * This method takes as parameters a <code>String</code> representing the
	 * gene ID. Then, the orthologus genes for the submitted gene ID belonging
	 * closest species are retrieved and returned as a <code>Collection</code>
	 * of <code>String</code>.
	 * 
	 * @param queryGene
	 *            A <code>String</code> representing the gene ID queried, whose
	 *            orthologus genes in it's closest species are to be retrieved.
	 * 
	 * @return A <code>Collection</code> of <code>String</code> containing all
	 *         the orthologus genes of the query gene in the closest species.
	 * 
	 * @throws SQLException
	 * 
	 */
	public ArrayList<String> getOrthologsInClosestSpecies(String queryGene)
			throws SQLException {

		ArrayList<String> orthologusGenes = new ArrayList<String>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}

		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/omaBgee", "user", "pass");

		String sql = null; // TODO

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			preparedStatement.setString(1, queryGene);

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				orthologusGenes.add(resultSet.getString("geneId"));
			}

		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			connection.close();
		}

		return orthologusGenes;
		
	}

}
