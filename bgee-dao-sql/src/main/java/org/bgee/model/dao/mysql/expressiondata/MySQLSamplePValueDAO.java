package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.SamplePValueDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

public class MySQLSamplePValueDAO extends MySQLDAO<SamplePValueDAO.Attribute> 
implements SamplePValueDAO  {
    private final static Logger log = LogManager.getLogger(MySQLSamplePValueDAO.class);

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    
    private final static Map<String, SamplePValueDAO.Attribute> colToAttrMap;

    static {
        log.traceEntry();
        Map<String, SamplePValueDAO.Attribute> colToAttributesMap = new HashMap<>();
        colToAttributesMap.put("expressionId", SamplePValueDAO.Attribute.EXPRESSION_ID);
        colToAttributesMap.put("pValue", SamplePValueDAO.Attribute.P_VALUE);
        colToAttributesMap.put("experimentId", SamplePValueDAO.Attribute.EXPERIMENT_ID);
        colToAttributesMap.put("sampleId", SamplePValueDAO.Attribute.SAMPLE_ID);
 
        colToAttrMap = Collections.unmodifiableMap(colToAttributesMap);
        log.traceExit();
    }
    
    private static String getJoin(String sampleTableName, String experimentTableName, 
            String sampleToExpeprimentJoinedColumn) {
        log.traceEntry("{}, {}, {}", sampleTableName, experimentTableName, 
                sampleToExpeprimentJoinedColumn);
        
        StringBuilder sb = new StringBuilder();
        sb.append(" INNER JOIN ").append(experimentTableName).append(" ON ")
          .append(experimentTableName).append(".").append(sampleToExpeprimentJoinedColumn)
          .append(" = ").append(sampleTableName).append(".")
          .append(sampleToExpeprimentJoinedColumn);
        
        return log.traceExit(sb.toString());
    }
    
    private static String getWhere(String sampleTableName, Collection<Integer> geneIds) {
        log.traceEntry("{}, {}", sampleTableName, geneIds);
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE ").append(sampleTableName)
          .append(".").append(MySQLGeneDAO.BGEE_GENE_ID).append(" IN (")
          .append(BgeePreparedStatement.generateParameterizedQueryString(geneIds.size())).append(") ");
        return log.traceExit(sb.toString());
    }
    
    private static String getOrderBy(String sampleTableName) {
        log.traceEntry();
        StringBuilder sb = new StringBuilder();
        sb.append("ORDER BY ")
          .append(sampleTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
          .append(", ").append(sampleTableName).append(".")
          .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPRESSION_ID, colToAttrMap));
        return log.traceExit(sb.toString());
    }
    
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLSamplePValueDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public SamplePValueTOResultSet<String, Integer> getAffymetrixPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);
        
        String sampleTableName = "affymetrixProbeset";
        StringBuilder sb = new StringBuilder("SELECT ")
                //geneId
                .append(MySQLGeneDAO.BGEE_GENE_ID)
                //sampleId
                .append(", ").append("bgeeAffymetrixChipId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.SAMPLE_ID, colToAttrMap))
                //expressionId
                .append(", ").append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPRESSION_ID, colToAttrMap))
                //pvalue
                .append(", ").append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.P_VALUE, colToAttrMap))
                .append(" FROM ").append(sampleTableName)
                .append(getWhere(sampleTableName, geneIds))
                .append(getOrderBy(sampleTableName));
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLSamplePValueTOResultSet<String,Integer>(stmt, String.class, Integer.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SamplePValueTOResultSet<String, String> getESTPValuesOrderedByGeneIdAndExprId(Collection<Integer> geneIds)
            throws DAOException, IllegalArgumentException {
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);
        
        String sampleTableName = "expressedSequenceTag";
        
        StringBuilder sb = new StringBuilder("SELECT ")
                //geneId
                .append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                //sampleId
                .append(", ").append("estLibraryId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.SAMPLE_ID, colToAttrMap))
                //expressionId
                .append(", ").append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPRESSION_ID, colToAttrMap))
                //pvalue
                .append(", ").append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.P_VALUE, colToAttrMap))
                .append(" FROM ").append(sampleTableName)
                .append(getWhere(sampleTableName, geneIds))
                .append(getOrderBy(sampleTableName));
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLSamplePValueTOResultSet<String,String>(stmt, String.class, String.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SamplePValueTOResultSet<String, String> getInSituPValuesOrderedByGeneIdAndExprId(Collection<Integer> geneIds)
            throws DAOException, IllegalArgumentException {
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);
        
        String sampleTableName = "inSituSpot";
        String experimentTableName = "inSituEvidence";
        
        StringBuilder sb = new StringBuilder("SELECT ")
                //geneId
                .append(sampleTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                //sampleId
                .append(", ").append(sampleTableName).append(".inSituSpotId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.SAMPLE_ID, colToAttrMap))
                //experimentId
                .append(", ").append(experimentTableName).append(".inSituExperimentId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPERIMENT_ID, colToAttrMap))
                //expressionId
                .append(", ").append(sampleTableName).append(".")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPRESSION_ID, colToAttrMap))
                // pvalue
                .append(", ").append(sampleTableName).append(".")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.P_VALUE, colToAttrMap))
                .append(" FROM ").append(sampleTableName)
                .append(getJoin(sampleTableName, experimentTableName, "inSituEvidenceId"))
                .append(getWhere(sampleTableName, geneIds))
                .append(getOrderBy(sampleTableName));
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLSamplePValueTOResultSet<String,String>(stmt, String.class, String.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SamplePValueTOResultSet<String, String> getRNASeqPValuesOrderedByGeneIdAndExprId(Collection<Integer> geneIds)
            throws DAOException, IllegalArgumentException {
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);
        
        String sampleTableName = "rnaSeqResult";
        String experimentTableName = "rnaSeqLibrary";
        
        StringBuilder sb = new StringBuilder("SELECT ")
                //geneId
                .append(sampleTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                //sampleId
                .append(", ").append(sampleTableName).append(".rnaSeqLibraryId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.SAMPLE_ID, colToAttrMap))
                //experimentId
                .append(", ").append(experimentTableName).append(".rnaSeqExperimentId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPERIMENT_ID, colToAttrMap))
                //expressionId
                .append(", ").append(sampleTableName).append(".")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPRESSION_ID, colToAttrMap))
                // pvalue
                .append(", ").append(sampleTableName).append(".")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.P_VALUE, colToAttrMap))
                .append(" FROM ").append(sampleTableName)
                .append(getJoin(sampleTableName, experimentTableName, "rnaSeqLibraryId"))
                .append(getWhere(sampleTableName, geneIds))
                .append(getOrderBy(sampleTableName));
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLSamplePValueTOResultSet<String,String>(stmt, String.class, String.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SamplePValueTOResultSet<String, String> getscRNASeqFullLengthPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);
        
        String sampleTableName = "scRnaSeqFullLengthResult";
        String experimentTableName = "scRnaSeqFullLengthLibrary";
        
        StringBuilder sb = new StringBuilder("SELECT ")
                //geneId
                .append(sampleTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                //sampleId
                .append(", ").append(sampleTableName).append(".scRnaSeqFullLengthLibraryId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.SAMPLE_ID, colToAttrMap))
                //experimentId
                .append(", ").append(experimentTableName).append(".scRnaSeqFullLengthExperimentId").append(" AS ")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPERIMENT_ID, colToAttrMap))
                //expressionId
                .append(", ").append(sampleTableName).append(".")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.EXPRESSION_ID, colToAttrMap))
                // pvalue
                .append(", ").append(sampleTableName).append(".")
                .append(getSelectExprFromAttribute(SamplePValueDAO.Attribute.P_VALUE, colToAttrMap))
                .append(" FROM ").append(sampleTableName)
                .append(getJoin(sampleTableName, experimentTableName, "scRnaSeqFullLengthLibraryId"))
                .append(getWhere(sampleTableName, geneIds))
                .append(getOrderBy(sampleTableName));
        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLSamplePValueTOResultSet<String,String>(stmt, String.class, String.class));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Implementation of the {@code MySQLSamplePValueTOResultSet}. 
     * 
     * @author Julien Wollbrett
     * @version Bgee 15.0 Mar. 2021
     * @since Bgee 15.0 Mar. 2021
     */
    class MySQLSamplePValueTOResultSet <T,U>
            extends MySQLDAOResultSet<SamplePValueDAO.SamplePValueTO<T, U>>
            implements SamplePValueTOResultSet<T, U> {
        
        private final Class<T> experimentIdCls;
        private final Class<U> sampleIdCls;
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLSamplePValueTOResultSet(BgeePreparedStatement statement, Class<T> experimentIdCls, Class<U> sampleIdCls) {
            super(statement);
            this.experimentIdCls = experimentIdCls;
            this.sampleIdCls = sampleIdCls;
        }

        @Override        
        protected SamplePValueDAO.SamplePValueTO<T, U> getNewTO() throws DAOException {
            try {
                log.traceEntry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer exprId = null;
                BigDecimal pValue = null;
                U sampleId = null;
                T experimentId = null;

                for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
                    String columnName = col.getValue();
                    if (MySQLGeneDAO.BGEE_GENE_ID.equals(columnName)) {
                        continue;
                    }
                    SamplePValueDAO.Attribute attr = getAttributeFromColName(columnName, colToAttrMap);
                    switch (attr) {
                        case EXPRESSION_ID:
                            exprId = currentResultSet.getInt(columnName);
                            break;
                        case EXPERIMENT_ID:
                                experimentId = experimentIdCls.cast(currentResultSet.getObject(columnName));

                            break;
                        case P_VALUE:
                            pValue = currentResultSet.getBigDecimal(columnName);
                            break;
                        case SAMPLE_ID:
                                sampleId = sampleIdCls.cast(currentResultSet.getObject(columnName));
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.traceExit(new SamplePValueTO<T,U>(exprId, experimentId, 
                        sampleId, pValue));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
