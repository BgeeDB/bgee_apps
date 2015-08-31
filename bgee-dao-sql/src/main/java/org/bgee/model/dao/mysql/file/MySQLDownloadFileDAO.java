package org.bgee.model.dao.mysql.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The MySQL implementation of {@link MySQLDownloadFileDAO}.
 * @author Philippe Moret
 */
public class MySQLDownloadFileDAO extends MySQLDAO<DownloadFileDAO.Attribute> implements DownloadFileDAO {

    /**
     * The {@code Logger} of this class.
     */
    private static final Logger log = LogManager.getLogger(MySQLDownloadFileDAO.class.getName());

    private static final Map<String, DownloadFileDAO.Attribute> colToAttributesMap;

    /**
     * The underlying table name.
     */
    public static final String DOWNLOAD_FILE_TABLE = "downloadFile";

    static {
        colToAttributesMap = new HashMap<>();
        colToAttributesMap.put("downloadFileId", DownloadFileDAO.Attribute.ID);
        colToAttributesMap.put("downloadFileName", DownloadFileDAO.Attribute.NAME);
        colToAttributesMap.put("downloadFileDescription", DownloadFileDAO.Attribute.DESCRIPTION);
        colToAttributesMap.put("path", DownloadFileDAO.Attribute.PATH);
        colToAttributesMap.put("downloadFileSize", DownloadFileDAO.Attribute.FILE_SIZE);
        colToAttributesMap.put("downloadFileCategory", DownloadFileDAO.Attribute.CATEGORY);
        colToAttributesMap.put("speciesDataGroupId", DownloadFileDAO.Attribute.SPECIES_DATA_GROUP_ID);
    }

    /**
     * Finds the {@link DownloadFileDAO.Attribute} from a column name.
     * @param columnName
     * @return The {@link DownloadFileDAO.Attribute} corresponding to the column name
     * @throws IllegalArgumentException If the columnName doesn't match any attributes.
     */
    private static DownloadFileDAO.Attribute getAttributeByColumnName(String columnName){
        log.entry(columnName);
        DownloadFileDAO.Attribute attribute = colToAttributesMap.get(columnName);
        if (attribute == null) {
            throw log.throwing(new IllegalArgumentException("Unknown column name : " + columnName));
        } 
        return attribute;
    }

    /**
     * Default constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO}
     * will use to obtain {@code BgeeConnection}s.
     *
     * @param manager the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLDownloadFileDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public DownloadFileTOResultSet getAllDownloadFiles() throws DAOException {
        log.entry();
        final String sql = generateSelectAllStatement(DOWNLOAD_FILE_TABLE, colToAttributesMap, false);
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLDownloadFileTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Implementation of the {@code DownloadFileTOResultSet}
     */
    class MySQLDownloadFileTOResultSet extends MySQLDAOResultSet<DownloadFileDAO.DownloadFileTO>
            implements DownloadFileTOResultSet {

        /**
         * Constructor passing a {@code BgeePreparedStatement} .
         * @param statement The {@link BgeePreparedStatement}
         */
        private MySQLDownloadFileTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected DownloadFileDAO.DownloadFileTO getNewTO() throws DAOException {
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                String id = null, path = null, name = null, size = null,
                        description = null, speciesDataGroupId = null;
                DownloadFileTO.CategoryEnum category = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    String currentValue = currentResultSet.getString(columnName);
                    DownloadFileDAO.Attribute attr = getAttributeByColumnName(columnName);
                    switch (attr) {
                        case ID:
                            id = currentValue;
                            break;
                        case DESCRIPTION:
                            description = currentValue;
                            break;
                        case PATH:
                            path = currentValue;
                            break;
                        case NAME:
                            name = currentValue;
                            break;
                        case FILE_SIZE:
                            size = currentValue;
                            break;
                        case CATEGORY:
                            category = DownloadFileTO.CategoryEnum.convertToCategoryEnum(currentValue);
                            break;
                        case SPECIES_DATA_GROUP_ID:
                            speciesDataGroupId = currentValue;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new DownloadFileTO(id, name, description, path, size, category, speciesDataGroupId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }


}
