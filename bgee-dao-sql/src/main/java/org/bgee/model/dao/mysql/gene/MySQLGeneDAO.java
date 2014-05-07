package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneTO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

/**
 * A {@code GeneDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.GeneTO
 * @since Bgee 13
 */
public class MySQLGeneDAO extends MySQLDAO<GeneDAO.Attribute> implements GeneDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLGeneOntologyDAO.class.getName());

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
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************

	/**
	 * Retrieves all gene IDs present into the Bgee database.
	 * <p>
	 * The gene IDs are retrieved and returned as a {@code Collection} 
	 * of {@code String}.
	 * 
	 * @return A {@code Collection} of {@code String} containing all
	 *         the gene IDs present into the Bgee database.
	 * 
	 * @throws SQLException
	 */
	public List<GeneTO> getAllGeneIDs() throws DAOException {
		log.entry();

		List<GeneTO> geneTOs = new ArrayList<GeneTO>();
		String sql = "SELECT geneId FROM gene;";
				
		List<GeneDAO.Attribute> listAttribute = Arrays.asList(GeneDAO.Attribute.ID);
		this.setAttributesToGet(listAttribute);		      

        try (BgeePreparedStatement stmt = 
        		this.getManager().getConnection().prepareStatement(sql)) {
        	ResultSet resultSet = 
        			stmt.getRealPreparedStatement().executeQuery();

        	while (resultSet.next()) {
            	GeneTO geneTO = new GeneTO(
            			resultSet.getString(GeneDAO.Attribute.ID.toString()),
            			resultSet.getString(GeneDAO.Attribute.NAME.toString()),
            			resultSet.getInt(GeneDAO.Attribute.SPECIESID.toString()));
            	geneTOs.add(geneTO);
        	}

        	return log.exit(new ArrayList<GeneTO>());
        } catch (SQLException e) {
        	throw log.throwing(new DAOException(e));
        }
	}
	
	public int updateOMAGroupIDs(Collection<GeneTO> genes) throws DAOException {
        log.entry(genes);
        int geneUpdatedCount = 0;
        return log.exit(geneUpdatedCount);
        //TODO
        /*

			String sql = "UPDATE gene SET OMANodeId='"
					+ node.getOMANodeId() + "' WHERE ";

			for (String id : geneIds) {
				sql = sql + "geneId='" + id + "' OR ";
			}

			sql = sql + " geneId='';";


         */
	}

}
