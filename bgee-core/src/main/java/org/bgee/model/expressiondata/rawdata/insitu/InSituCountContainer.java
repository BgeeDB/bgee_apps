package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainerWithExperiment;

/**
 * A {@code RawDataCountContainerWithExperiment} for in situ data.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 */
public class InSituCountContainer extends RawDataCountContainerWithExperiment {

    public InSituCountContainer(Integer experimentCount, Integer assayCount, Integer callCount) {
        super(experimentCount, assayCount, callCount);
    }
}