package org.bgee.model.expressiondata.rawdata.est;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;

/**
 * A {@code RawDataDataType} specific to EST.
 * <p>
 * The typical way to obtain an object from this class is to use the public static attribute
 * {@link org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType#EST
 * RawDataDataType#EST}, in order to mimic an {@code enum}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 * @see org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType RawDataDataType
 */
public class ESTDataType extends RawDataDataType<ESTContainer, ESTCountContainer> {

    public ESTDataType() {
        super(DataType.EST, ESTContainer.class, ESTCountContainer.class);
    }

    @Override
    public String getAssayId(Assay a) throws IllegalArgumentException {
        if (!(a instanceof ESTLibrary)) {
            throw new IllegalArgumentException("Assay is not an ESTLibrary");
        }
        return ((ESTLibrary) a).getId();
    }
    @Override
    public String getAssayName(Assay a) throws IllegalArgumentException {
        if (!(a instanceof ESTLibrary)) {
            throw new IllegalArgumentException("Assay is not an ESTLibrary");
        }
        return ((ESTLibrary) a).getName();
    }

    @Override
    public boolean isInformativeAssayId() {
        return true;
    }
    @Override
    public boolean isInformativeAssayName() {
        return true;
    }
    @Override
    public boolean isInformativeExperimentName() {
        return true;
    }
}
