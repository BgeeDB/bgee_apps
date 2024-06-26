package org.bgee.model.dao.mysql.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The MySQL implementation of {@link MySQLDownloadFileDAO}.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13
 */
public class MySQLDownloadFileDAO extends MySQLDAO<DownloadFileDAO.Attribute> implements DownloadFileDAO {

    /**
     * The {@code Logger} of this class.
     */
    private static final Logger log = LogManager.getLogger(MySQLDownloadFileDAO.class.getName());

    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, DownloadFileDAO.Attribute> colToAttributesMap;

    /**
     * The underlying table name.
     */
    private static final String DOWNLOAD_FILE_TABLE = "downloadFile";

    static {
        colToAttributesMap = new HashMap<>();
        colToAttributesMap.put("downloadFileId", DownloadFileDAO.Attribute.ID);
        colToAttributesMap.put("downloadFileName", DownloadFileDAO.Attribute.NAME);
        colToAttributesMap.put("downloadFileDescription", DownloadFileDAO.Attribute.DESCRIPTION);
        colToAttributesMap.put("downloadFileRelativePath", DownloadFileDAO.Attribute.PATH);
        colToAttributesMap.put("downloadFileSize", DownloadFileDAO.Attribute.FILE_SIZE);
        colToAttributesMap.put("downloadFileCategory", DownloadFileDAO.Attribute.CATEGORY);
        colToAttributesMap.put("speciesDataGroupId", DownloadFileDAO.Attribute.SPECIES_DATA_GROUP_ID);
        colToAttributesMap.put("downloadFileConditionParameters", DownloadFileDAO.Attribute.CONDITION_PARAMETERS);
    }

    private static ConditionDAO.Attribute convertToConditionParameter(String value) {
        log.traceEntry("{}", value);
        if ("anatomicalEntity".equals(value)) {
            return log.traceExit(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        } else if ("developmentalStage".equals(value)) {
            return log.traceExit(ConditionDAO.Attribute.STAGE_ID);
        } else if ("sex".equals(value)) {
            return log.traceExit(ConditionDAO.Attribute.SEX_ID);
        } else if ("strain".equals(value)) {
            return log.traceExit(ConditionDAO.Attribute.STRAIN_ID);
        } else {
            throw log.throwing(new IllegalArgumentException("Unrecognized value"));
        }
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
    public DownloadFileTOResultSet getDownloadFiles(Collection<CategoryEnum> requestedCategories)
            throws DAOException {
        log.traceEntry("{}", requestedCategories);
        EnumSet<CategoryEnum> cats = requestedCategories == null || requestedCategories.isEmpty()?
                EnumSet.noneOf(CategoryEnum.class): EnumSet.copyOf(requestedCategories);
        String sql = generateSelectAllStatement(DOWNLOAD_FILE_TABLE, colToAttributesMap, false);
        if (!cats.isEmpty()) {
            sql += " WHERE " + DOWNLOAD_FILE_TABLE + "."
                   + getSelectExprFromAttribute(DownloadFileDAO.Attribute.CATEGORY, colToAttributesMap)
                   + " IN (" + BgeePreparedStatement.generateParameterizedQueryString(cats.size()) + ")";
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!cats.isEmpty()) {
                stmt.setEnumDAOFields(1, cats, true);
            }
            return log.traceExit(new MySQLDownloadFileTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertDownloadFiles(Collection<DownloadFileTO> fileTOs)
            throws DAOException, IllegalArgumentException {
        log.traceEntry("{}", fileTOs);
        
        if (fileTOs == null || fileTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No file is given, then no file is inserted"));
        }
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO downloadFile (")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.ID, colToAttributesMap)).append(", ")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.NAME, colToAttributesMap)).append(", ")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.DESCRIPTION, colToAttributesMap)).append(", ")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.PATH, colToAttributesMap)).append(", ")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.CATEGORY, colToAttributesMap)).append(", ")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.SPECIES_DATA_GROUP_ID, colToAttributesMap)).append(", ")
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.FILE_SIZE, colToAttributesMap))
        .append(getSelectExprFromAttribute(DownloadFileDAO.Attribute.CONDITION_PARAMETERS, colToAttributesMap))
        .append(") VALUES ");
        for (int i = 0; i < fileTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?, ?, ?, ?, ?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (DownloadFileTO fileTO: fileTOs) {
                stmt.setInt(paramIndex, fileTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, fileTO.getName());
                paramIndex++;
                stmt.setString(paramIndex, fileTO.getDescription());
                paramIndex++;
                stmt.setString(paramIndex, fileTO.getPath());
                paramIndex++;
                stmt.setEnumDAOField(paramIndex, fileTO.getCategory());
                paramIndex++;
                stmt.setInt(paramIndex, fileTO.getSpeciesDataGroupId());
                paramIndex++;
                stmt.setLong(paramIndex, fileTO.getSize());
                paramIndex++;
            }
            
            return log.traceExit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Implementation of the {@code DownloadFileTOResultSet}
     * @author Philippe Moret
     * @version Bgee 13
     * @since Bgee 13
     */
    class MySQLDownloadFileTOResultSet extends MySQLDAOResultSet<DownloadFileDAO.DownloadFileTO>
            implements DownloadFileTOResultSet {

        /**
         * Constructor passing a {@code BgeePreparedStatement} .
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLDownloadFileTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected DownloadFileDAO.DownloadFileTO getNewTO() throws DAOException {
            try {
                log.traceEntry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, speciesDataGroupId = null;
                String path = null, name = null, description = null;
                Long size = null;
                DownloadFileTO.CategoryEnum category = null;
                Set<ConditionDAO.Attribute> condParams = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    DownloadFileDAO.Attribute attr = getAttributeFromColName(columnName, colToAttributesMap);
                    switch (attr) {
                        case ID:
                            id = currentResultSet.getInt(columnName);
                            break;
                        case DESCRIPTION:
                            description = currentResultSet.getString(columnName);
                            break;
                        case PATH:
                            path = currentResultSet.getString(columnName);
                            break;
                        case NAME:
                            name = currentResultSet.getString(columnName);
                            break;
                        case FILE_SIZE:
                            size = currentResultSet.getLong(columnName);
                            break;
                        case CATEGORY:
                            category = DownloadFileTO.CategoryEnum.convertToCategoryEnum(
                                    currentResultSet.getString(columnName));
                            break;
                        case SPECIES_DATA_GROUP_ID:
                            speciesDataGroupId = currentResultSet.getInt(columnName);
                            break;
                        case CONDITION_PARAMETERS:
                            String values = currentResultSet.getString(columnName);
                            if (values != null) {
                                //XXX: create a method 'getSet' in MySQLDAOResultSet?
                                condParams = Arrays.stream(values.split(","))
                                        .map(s -> convertToConditionParameter(s))
                                        .collect(Collectors.toSet());
                            }
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.traceExit(new DownloadFileTO(id, name, description, path, size, 
                        category, speciesDataGroupId, condParams));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }


}
