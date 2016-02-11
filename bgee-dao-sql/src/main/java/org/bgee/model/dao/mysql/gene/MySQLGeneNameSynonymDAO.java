package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

public class MySQLGeneNameSynonymDAO extends MySQLDAO<GeneNameSynonymDAO.Attribute> implements GeneNameSynonymDAO {

	private static final Logger log = LogManager.getLogger(MySQLGeneDAO.class.getName());

	private static final String GENE_NAME_SYNONYM_TABLE = "geneNameSynonym";

	public MySQLGeneNameSynonymDAO(MySQLDAOManager manager) throws IllegalArgumentException {
		super(manager);
	}

	private static final Map<String, GeneNameSynonymDAO.Attribute> COL_NAMES_TO_ATTRS;

	static {
		Map<String, GeneNameSynonymDAO.Attribute> tempMap = new HashMap<String, GeneNameSynonymDAO.Attribute>();
		tempMap.put("geneId", GeneNameSynonymDAO.Attribute.GENE_ID);
		tempMap.put("geneNameSynonym", GeneNameSynonymDAO.Attribute.GENE_NAME_SYNONYM);
		COL_NAMES_TO_ATTRS = Collections.unmodifiableMap(tempMap);
	}

	public class MySQLGeneNameSynonymTOResultSet extends MySQLDAOResultSet<GeneNameSynonymTO>
	        implements GeneNameSynonymTOResultSet {

		protected MySQLGeneNameSynonymTOResultSet(BgeePreparedStatement statement) {
			super(statement);
		}

		@Override
		protected GeneNameSynonymTO getNewTO() throws DAOException, UnrecognizedColumnException {
			log.entry();
			try {
				final ResultSet currentResultSet = this.getCurrentResultSet();
				String geneId = null, geneNameSynonym = null;

				for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
					String columnName = col.getValue();
					GeneNameSynonymDAO.Attribute attr = MySQLGeneNameSynonymDAO.this.getAttributeFromColName(columnName,
					        COL_NAMES_TO_ATTRS);
					switch (attr) {
					case GENE_ID:
						geneId = currentResultSet.getString(col.getKey());
						break;
					case GENE_NAME_SYNONYM:
						geneNameSynonym = currentResultSet.getString(col.getKey());
						break;
					default:
						log.throwing(new UnrecognizedColumnException(columnName));
					}
				}
				return log.exit(new GeneNameSynonymTO(geneId, geneNameSynonym));
			} catch (SQLException e) {
				throw log.throwing(new DAOException(e));
			}
		}

	}

	@Override
	public GeneNameSynonymTOResultSet getGeneNameSynonyms(String geneId) {
		log.entry(geneId);
		// Construct sql query
		String sql = this.generateSelectClause(GENE_NAME_SYNONYM_TABLE, COL_NAMES_TO_ATTRS, true);
		sql += " FROM " + GENE_NAME_SYNONYM_TABLE;
		sql += " WHERE geneId = ? ";
		
		// we don't use a try-with-resource, because we return a pointer to the
		// results,
		// not the actual results, so we should not close this
		// BgeePreparedStatement.
		try {
			BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
			
			stmt.setString(1, geneId);

			return log
			        .exit(new MySQLGeneNameSynonymTOResultSet(stmt));
		} catch (SQLException e) {
			throw log.throwing(new DAOException(e));
		}
	}

}
