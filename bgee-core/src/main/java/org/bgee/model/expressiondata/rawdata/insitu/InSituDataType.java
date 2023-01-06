package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;

/**
 * A {@code RawDataDataType} specific to in situ data.
 * <p>
 * The typical way to obtain an object from this class is to use the public static attribute
 * {@link org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType#IN_SITU
 * RawDataDataType#IN_SITU}, in order to mimic an {@code enum}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 * @see org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType RawDataDataType
 */
public class InSituDataType extends RawDataDataType<InSituContainer, InSituCountContainer> {
    public InSituDataType() {
        super(DataType.IN_SITU, InSituContainer.class, InSituCountContainer.class);
    }

    @Override
    public String getAssayId(Assay a) throws IllegalArgumentException {
        if (!(a instanceof InSituEvidence)) {
            throw new IllegalArgumentException("Assay is not an InSituEvidence");
        }
        return ((InSituEvidence) a).getId();
    }
    @Override
    public String getAssayName(Assay a) throws IllegalArgumentException {
        if (!(a instanceof InSituEvidence)) {
            throw new IllegalArgumentException("Assay is not an InSituEvidence");
        }
        return ((InSituEvidence) a).getId();
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
        return false;
    }
}
