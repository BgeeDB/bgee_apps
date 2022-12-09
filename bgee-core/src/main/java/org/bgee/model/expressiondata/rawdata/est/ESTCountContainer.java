package org.bgee.model.expressiondata.rawdata.est;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;

/**
 * A {@code RawDataCountContainer} for EST.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 */
public class ESTCountContainer extends RawDataCountContainer {

    public ESTCountContainer(Integer assayCount, Integer callCount) {
        super(assayCount, callCount);
    }
}
