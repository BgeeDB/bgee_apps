package org.bgee.model.dao.mysql.expressiondata.call;

import java.util.EnumSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

public abstract class MySQLCallDAO <T extends Enum<T> & DAO.Attribute> extends MySQLDAO<T> {

    protected final static String GLOBAL_EXPR_ID_FIELD = "globalExpressionId";
    protected final static String GLOBAL_MEAN_RANK_FIELD = "meanRank";
    protected final static String GLOBAL_P_VALUE_FIELD_START = "pVal";
    protected final static String GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START = "pValBestDescendant";
    protected final static String GLOBAL_SELF_OBS_COUNT_PREFIX = "selfObsCount";
    protected final static String GLOBAL_DESCENDANT_OBS_COUNT_PREFIX = "descObsCount";

    private final static Logger log = LogManager.getLogger(MySQLCallDAO.class.getName());

    public MySQLCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    protected static String getFieldNamePartFromDataTypes(EnumSet<DAODataType> dataTypes) {
        log.traceEntry("{}", dataTypes);
        //We iterate an EnumSet to have a predictable order of iteration to generate fieldNamePart
        return log.traceExit(dataTypes.stream()
                .map(dt -> dt.getFieldNamePart())
                .collect(Collectors.joining()));
    }

}
