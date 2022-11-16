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
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
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
     * An {@code Enum} describing columns potentially used in a where clause but for
     * which the table used can change depending on the query.
     * @author Julien Wollbrett
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     */
    public static enum AmbiguousRawDataColumn {SPECIES_ID, EXPERIMENT_ID, COND_ID,
        ASSAY_ID}

    /**
     * An {@code Enum} describing columns potentially used in a where clause but for
     * which the table can not change depending on the query.
     * @author Julien Wollbrett
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     */
    public static enum RawDataColumn {SPECIES_ID, EXPERIMENT_ID, COND_ID,
        ASSAY_ID, CALL_ID, GENE_ID}

    public RawDataFiltersToDatabaseMapping(Map<AmbiguousRawDataColumn, String> ambiguousColToTableName,
            DAODataType datatype) {
        if (datatype == null) {
            throw log.throwing(new IllegalArgumentException("datatype can not be null"));
        }
        Map<AmbiguousRawDataColumn, String> clonedAmbiguousColumns = ambiguousColToTableName == null ?
                Collections.unmodifiableMap(new HashMap<>()) :
                    Collections.unmodifiableMap(ambiguousColToTableName);
        this.colToTableName = Collections.unmodifiableMap(RawDataFiltersToDatabaseMapping
                .generateColToTableName(clonedAmbiguousColumns, datatype));
        this.colToColumnName = Collections.unmodifiableMap(RawDataFiltersToDatabaseMapping
                .generateColToColName(datatype));
        this.datatype = datatype;
        log.debug("datatype : {}", this.datatype);
        log.debug("colToColumnName : {}", this.getColToColumnName());
        log.debug("colToTableName : {}", this.getColToTableName());
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
            Map<AmbiguousRawDataColumn, String> ambiguousColToTable, DAODataType datatype) {
        Map<RawDataColumn, String> finalColToTable = new HashMap<>();
        // first add all tables that have been detected as ambiguous
        finalColToTable.putAll(ambiguousColToTable.entrySet().stream()
                .collect(Collectors.toMap(e-> fromAmbiguousColumnToColumn(e.getKey()), e-> e.getValue())));
        // then add tables that can not be ambiguous. It depends on the datatype
        if (datatype.equals(DAODataType.AFFYMETRIX)) {
            finalColToTable.put(RawDataColumn.ASSAY_ID, MySQLAffymetrixChipDAO.TABLE_NAME);
            finalColToTable.put(RawDataColumn.GENE_ID, MySQLAffymetrixProbesetDAO.TABLE_NAME);
        } else if (datatype.equals(DAODataType.RNA_SEQ)) {
            finalColToTable.put(RawDataColumn.GENE_ID, MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
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
            finalColToColName.put(RawDataColumn.EXPERIMENT_ID, MicroarrayExperimentDAO.Attribute
                    .ID.getTOFieldName());
        } else if (datatype.equals(DAODataType.RNA_SEQ)) {
            finalColToColName.put(RawDataColumn.EXPERIMENT_ID, RNASeqExperimentDAO.Attribute
                    .ID.getTOFieldName());
            // for RNA-Seq the filtering is done on library IDs
            finalColToColName.put(RawDataColumn.ASSAY_ID, RNASeqLibraryDAO.Attribute
                    .ID.getTOFieldName());
        } else {
            throw log.throwing(new IllegalStateException("not yet implemented for datatype " +
                    datatype));
        }
        return finalColToColName;
    }

    private static RawDataColumn fromAmbiguousColumnToColumn(AmbiguousRawDataColumn ambiguousColumn) {
        log.traceEntry("{}", ambiguousColumn);
        if (ambiguousColumn.equals(AmbiguousRawDataColumn.COND_ID)) {
            return log.traceExit(RawDataColumn.COND_ID);
        }
        if (ambiguousColumn.equals(AmbiguousRawDataColumn.EXPERIMENT_ID)) {
            return log.traceExit(RawDataColumn.EXPERIMENT_ID);
        }
        if (ambiguousColumn.equals(AmbiguousRawDataColumn.ASSAY_ID)) {
            return log.traceExit(RawDataColumn.ASSAY_ID);
        }
        if (ambiguousColumn.equals(AmbiguousRawDataColumn.SPECIES_ID)) {
            return log.traceExit(RawDataColumn.SPECIES_ID);
        }
        throw log.throwing(new IllegalArgumentException(ambiguousColumn + " can not be"
                + " transformed to a RawDataColumn"));
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
