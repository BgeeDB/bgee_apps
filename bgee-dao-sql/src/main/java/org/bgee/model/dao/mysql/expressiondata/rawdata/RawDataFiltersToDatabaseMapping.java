package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqResultAnnotatedSampleDAO;

/**
 * Class used to map raw data filters to their corresponding columns and tables
 *
 * @author Julien Wollbrett
 * @version Bgee 15, Nov. 2022
 *
 */
public class RawDataFiltersToDatabaseMapping {

    private final Map<RawDataColumn, String> colToTableName;
    private final Map<RawDataColumn, String> colToColumnName;
    private final DAODataType datatype;

    private final static Logger log =
            LogManager.getLogger(RawDataFiltersToDatabaseMapping.class);


    /**
     * An {@code Enum} describing columns potentially used in a where clause.
     * @author Julien Wollbrett
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     */
    public static enum RawDataColumn {SPECIES_ID, EXPERIMENT_ID, COND_ID,
        ASSAY_ID, CALL_TABLE_ASSAY_ID, GENE_ID}

    public RawDataFiltersToDatabaseMapping(Map<RawDataColumn, String> ambiguousColToTableName,
            DAODataType datatype) {
        if (datatype == null) {
            throw log.throwing(new IllegalArgumentException("datatype can not be null"));
        }
        Map<RawDataColumn, String> clonedAmbiguousColumns = ambiguousColToTableName == null ?
                Collections.unmodifiableMap(new HashMap<>()) :
                    Collections.unmodifiableMap(ambiguousColToTableName);
        this.colToTableName = Collections.unmodifiableMap(RawDataFiltersToDatabaseMapping
                .generateColToTableName(clonedAmbiguousColumns, datatype));
        this.colToColumnName = Collections.unmodifiableMap(RawDataFiltersToDatabaseMapping
                .generateColToColName(datatype));
        this.datatype = datatype;
        log.debug(this.toString());
    }

    public Map<RawDataColumn, String> getColToTableName() {
        return colToTableName;
    }

    public Map<RawDataColumn, String> getColToColumnName() {
        return colToColumnName;
    }

    public DAODataType getDatatype() {
        return datatype;
    }

    private static Map<RawDataColumn, String> generateColToTableName(
            Map<RawDataColumn, String> ambiguousColToTable, DAODataType datatype) {
        Map<RawDataColumn, String> finalColToTable = new HashMap<>();
        // first add all tables that have been detected as ambiguous
        finalColToTable.putAll(ambiguousColToTable.entrySet().stream()
                .collect(Collectors.toMap(e-> e.getKey(), e-> e.getValue())));
        // then add tables that can not be ambiguous. It depends on the datatype
        if (datatype.equals(DAODataType.AFFYMETRIX)) {
            finalColToTable.put(RawDataColumn.ASSAY_ID, MySQLAffymetrixChipDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.GENE_ID, MySQLAffymetrixProbesetDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.CALL_TABLE_ASSAY_ID, MySQLAffymetrixProbesetDAO.TABLE_NAME);
        } else if (datatype.equals(DAODataType.RNA_SEQ)) {
            finalColToTable.put(RawDataColumn.GENE_ID, MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.CALL_TABLE_ASSAY_ID, MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
        } else if (datatype.equals(DAODataType.EST)) {
            finalColToTable.put(RawDataColumn.CALL_TABLE_ASSAY_ID, MySQLESTDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.GENE_ID, MySQLESTDAO.TABLE_NAME);
        } else if (datatype.equals(DAODataType.IN_SITU)) {
            finalColToTable.put(RawDataColumn.CALL_TABLE_ASSAY_ID, MySQLInSituSpotDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.GENE_ID, MySQLInSituSpotDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.SPECIES_ID, MySQLRawDataConditionDAO.TABLE_NAME);
        } else {
            throw log.throwing(new IllegalStateException("not yet implemented for datatype " +
                    datatype));
        }
        return finalColToTable;
    }

    private static Map<RawDataColumn, String> generateColToColName(DAODataType datatype) {
        Map<RawDataColumn, String> finalColToColName= new HashMap<>();
        // first add column names that are the same for all datatypes
        finalColToColName.put(RawDataColumn.COND_ID, RawDataConditionDAO.Attribute
                .ID.getTOFieldName());
        finalColToColName.put(RawDataColumn.GENE_ID, GeneDAO.Attribute
                .ID.getTOFieldName());
        finalColToColName.put(RawDataColumn.SPECIES_ID, SpeciesDAO.Attribute
                .ID.getTOFieldName());
        // then add column names that depend on the datatype
        if (datatype.equals(DAODataType.AFFYMETRIX)) {
            finalColToColName.put(RawDataColumn.ASSAY_ID, AffymetrixChipDAO.Attribute
                    .AFFYMETRIX_CHIP_ID.getTOFieldName());
            finalColToColName.put(RawDataColumn.CALL_TABLE_ASSAY_ID, AffymetrixProbesetDAO.Attribute
                    .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
            finalColToColName.put(RawDataColumn.EXPERIMENT_ID, MicroarrayExperimentDAO.Attribute
                    .ID.getTOFieldName());
        } else if (datatype.equals(DAODataType.RNA_SEQ)) {
            finalColToColName.put(RawDataColumn.EXPERIMENT_ID, RNASeqExperimentDAO.Attribute
                    .ID.getTOFieldName());
            finalColToColName.put(RawDataColumn.CALL_TABLE_ASSAY_ID, RNASeqResultAnnotatedSampleDAO
                    .Attribute.LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName());
            // for RNA-Seq the filtering is done on library IDs
            finalColToColName.put(RawDataColumn.ASSAY_ID, RNASeqLibraryDAO.Attribute
                    .ID.getTOFieldName());
        } else if (datatype.equals(DAODataType.EST)) {
            // for RNA-Seq the filtering is done on library IDs
            finalColToColName.put(RawDataColumn.ASSAY_ID, ESTLibraryDAO.Attribute
                    .ID.getTOFieldName());
            finalColToColName.put(RawDataColumn.CALL_TABLE_ASSAY_ID, ESTDAO.Attribute
                    .EST_LIBRARY_ID.getTOFieldName());
        } else if (datatype.equals(DAODataType.IN_SITU)) {
            finalColToColName.put(RawDataColumn.ASSAY_ID, InSituEvidenceDAO.Attribute
                    .IN_SITU_EVIDENCE_ID.getTOFieldName());
            finalColToColName.put(RawDataColumn.CALL_TABLE_ASSAY_ID, InSituSpotDAO.Attribute
                    .IN_SITU_EVIDENCE_ID.getTOFieldName());
            finalColToColName.put(RawDataColumn.EXPERIMENT_ID, InSituExperimentDAO.Attribute
                    .ID.getTOFieldName());
        } else {
            throw log.throwing(new IllegalStateException("not yet implemented for datatype " +
                    datatype));
        }
        return finalColToColName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colToColumnName, colToTableName, datatype);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataFiltersToDatabaseMapping other = (RawDataFiltersToDatabaseMapping) obj;
        return Objects.equals(colToColumnName, other.colToColumnName)
                && Objects.equals(colToTableName, other.colToTableName) && datatype == other.datatype;
    }

    @Override
    public String toString() {
        return "RawDataFiltersToDatabaseMapping [colToTableName=" + colToTableName + ", colToColumnName="
                + colToColumnName + ", datatype=" + datatype + "]";
    }

}
