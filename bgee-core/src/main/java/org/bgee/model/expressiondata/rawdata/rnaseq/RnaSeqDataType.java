package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;

/**
 * A {@code RawDataDataType} specific to RNA-Seq (bulk or single-cell).
 * <p>
 * The typical way to obtain an object from this class is to use the public static attributes
 * {@link org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType#BULK_RNA_SEQ
 * RawDataDataType#BULK_RNA_SEQ} and
 * {@link org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType#SC_RNA_SEQ
 * RawDataDataType#SC_RNA_SEQ}, in order to mimic an {@code enum}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType RawDataDataType
 */
public class RnaSeqDataType extends RawDataDataType<RnaSeqContainer, RnaSeqCountContainer> {

    public RnaSeqDataType(DataType dataType) {
        super(dataType, RnaSeqContainer.class, RnaSeqCountContainer.class);
        if (dataType == null) {
            throw new IllegalArgumentException("dataType cannot be null");
        }
        if (!dataType.equals(DataType.RNA_SEQ) && !dataType.equals(DataType.SC_RNA_SEQ)) {
            throw new IllegalArgumentException("dataType has to be either RNA_SEQ or SC_RNA_SEQ");
        }
    }

    @Override
    public String getAssayId(Assay a) throws IllegalArgumentException {
        if (!(a instanceof RnaSeqLibraryAnnotatedSample)) {
            throw new IllegalArgumentException("Assay is not a RnaSeqLibraryAnnotatedSample");
        }
        return ((RnaSeqLibraryAnnotatedSample) a).getLibrary().getId();
    }
    @Override
    public String getAssayName(Assay a) throws IllegalArgumentException {
        if (!(a instanceof RnaSeqLibraryAnnotatedSample)) {
            throw new IllegalArgumentException("Assay is not a RnaSeqLibraryAnnotatedSample");
        }
        return ((RnaSeqLibraryAnnotatedSample) a).getLibrary().getId();
    }

    @Override
    public boolean isInformativeAssayId() {
        return true;
    }
    @Override
    public boolean isInformativeAssayName() {
        return false;
    }
    @Override
    public boolean isInformativeExperimentName() {
        return true;
    }
}