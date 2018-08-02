package org.bgee.model.expressiondata.rawdata;

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;

public class RawCallSourceContainer {
    private final static Logger log = LogManager.getLogger(RawCallSourceContainer.class.getName());

    private final Stream<AffymetrixProbeset> affymetrixProbesets;

    /**
     * @return  A {@code Stream} of {@code AffymetrixProbeset}s.
     *          If the returned value is {@code null}, it means that this data type was not requested.
     *          If the {@code Stream} is not {@code null} but contains no element,
     *          it means that there were no data of this type for the requested parameters.
     */
    public Stream<AffymetrixProbeset> getAffymetrixProbesets() {
        return this.affymetrixProbesets;
    }

}
