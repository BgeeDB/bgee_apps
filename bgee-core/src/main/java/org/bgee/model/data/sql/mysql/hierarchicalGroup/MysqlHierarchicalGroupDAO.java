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
				+ "WHERE t1.geneId = '" + queryGene
				+ "' and t3.ncbiTaxonomyId='" + ncbiTaxonomyId + "';";

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
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
