package org.bgee.model.expressiondata.rawdata.microarray;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainerWithExperiment;

/**
 * A {@code RawDataCountContainerWithExperiment} for Affymetrix.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class AffymetrixRawDataCountContainer extends RawDataCountContainerWithExperiment {

    public AffymetrixRawDataCountContainer(Integer experimentCount,
            Integer assayCount, Integer callCount) {
        super(experimentCount, assayCount, callCount);
    }
}