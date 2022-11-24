package org.bgee.model.expressiondata.rawdata.microarray;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;

/**
 * A {@code RawDataDataType} specific to Affymetrix.
 * <p>
 * The typical way to obtain an object from this class is to use the public static attribute
 * {@link org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType#AFFYMETRIX
 * RawDataDataType#AFFYMETRIX}, in order to mimic an {@code enum}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType RawDataDataType
 */
public class AffymetrixDataType extends RawDataDataType<AffymetrixContainer,
AffymetrixCountContainer> {

    public AffymetrixDataType() {
        super(DataType.AFFYMETRIX, AffymetrixContainer.class,
                AffymetrixCountContainer.class);
    }
}
