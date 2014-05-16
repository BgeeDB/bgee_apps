package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;

/**
 * A {@code GeneDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLGeneDAO extends MySQLDAO<GeneDAO.Attribute> implements GeneDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLGeneDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLGeneDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    // TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************

	/**
	 * Retrieve all genes present into the Bgee database.
	 * <p>
	 * The genes are retrieved and returned as a {@code Collection} of 
	 * {@code MySQLGeneTOResultSet}.
	 * 
	 * @return A {@code Collection} of {@code MySQLGeneTOResultSet}s containing all
	 *         genes present into the Bgee database.
	 * 
	 * @throws SQLException
	 */
	public MySQLGeneTOResultSet getAllGenes() throws DAOException {
		log.entry();
		
		//Construct sql query according to currents attributes
		Collection<GeneDAO.Attribute> attributes = this.getAttributes();
		StringBuilder sql = new StringBuilder("SELECT ");
		for (GeneDAO.Attribute attribute: attributes) {
			if (attribute==GeneDAO.Attribute.ID) {
				sql.append("geneId");
			} else if (attribute==GeneDAO.Attribute.NAME) {
				sql.append("geneName");
			} else if (attribute==GeneDAO.Attribute.DESCRIPTION) {
				sql.append("geneDescription");
			} else if (attribute==GeneDAO.Attribute.SPECIESID) {
				sql.append("speciesId");
			} else if (attribute==GeneDAO.Attribute.GENEBIOTYPEID) {
				sql.append("geneBioTypeId");
			} else if (attribute==GeneDAO.Attribute.OMAPARENTNODEID) {
				sql.append("OMAParentNodeId");
			} else if (attribute==GeneDAO.Attribute.ENSEMBLGENE) {
				sql.append("ensemblGene");
			}
			sql.append(", ");
		}
		sql.delete(sql.lastIndexOf(","), sql.length());
		sql.append(" FROM gene");
		sql.append(";");

		try (BgeePreparedStatement stmt = 
				this.getManager().getConnection().prepareStatement(sql.toString())) {
			MySQLGeneTOResultSet resultSet = new MySQLGeneTOResultSet(stmt); 
        	return log.exit(resultSet);
        } catch (SQLException e) {
        	throw log.throwing(new DAOException(e));
        }
	}

	/**
	 * Update the provided genes with OMA parent Node ID into the Bgee database, 
     * represented as a {@code Collection} of {@code GeneTO}s
	 * @param genes	a {@code Collection} of {@code GeneTO}s to be updated into the database.
	 * @return	a {@code int} representing the number of genes updated.
     * @throws DAOException		If a {@code SQLException} occurred while trying 
     *                          to update {@code gene}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
	 */
	public int updateOMAGroupIDs(Collection<GeneTO> genes) throws DAOException {
		log.entry(genes);
		int geneUpdatedCount = 0;
		String sql = "UPDATE gene SET OMANodeId = ? WHERE geneId = ?";

		try (BgeePreparedStatement stmt = 
				this.getManager().getConnection().prepareStatement(sql)) {
			for (GeneTO gene: genes) {
				stmt.setInt(1, gene.getOMAParentNodeId());
				stmt.setString(2, gene.getId());
				geneUpdatedCount += stmt.executeUpdate();
				stmt.clearParameters();
			}
			return log.exit(geneUpdatedCount);
		} catch (SQLException e) {
			throw log.throwing(new DAOException(e));
		}
	}

	/**
	 * A {@code MySQLDAOResultSet} specific to {@code GeneTO}.
	 * 
	 * @author Valentine Rech de Laval
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public class MySQLGeneTOResultSet extends MySQLDAOResultSet<GeneTO> {

		/**
		 * Constructor providing the first {@code BgeePreparedStatement} to execute 
		 * a query on. Note that additional {@code BgeePreparedStatement}s can be provided
		 * afterwards by calling {@link #addStatement(BgeePreparedStatement)} or 
		 * {@link #addAllStatements(List)}.
		 * 
		 * @param statement the first {@code BgeePreparedStatement} to execute 
		 *                  a query on.
		 */
		public MySQLGeneTOResultSet(BgeePreparedStatement statement) {
			super(statement);
		}

		@Override
		public GeneTO getTO() throws DAOException {
			log.entry();
			try {
				ResultSet currentResultSet = this.getCurrentResultSet();
				GeneTO geneTO = new GeneTO(
						currentResultSet.getString("geneId"),
						currentResultSet.getString("geneName"),
						currentResultSet.getString("geneDescription"),
						currentResultSet.getInt("speciesId"),
						currentResultSet.getInt("geneBioTypeId"),
						currentResultSet.getInt("OMAParentNodeId"),
						currentResultSet.getBoolean("ensemblGene"));
				return log.exit(geneTO);
			} catch (SQLException e) {
				throw log.throwing(new DAOException(e));
			}
		}
	}
}
