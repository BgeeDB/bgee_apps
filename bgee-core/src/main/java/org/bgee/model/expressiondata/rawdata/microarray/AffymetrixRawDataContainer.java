package org.bgee.model.expressiondata.rawdata.microarray;

import java.util.Collection;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;

/**
 * A {@code RawDataContainerWithExperiment} for Affymetrix.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class AffymetrixRawDataContainer
extends RawDataContainerWithExperiment<AffymetrixExperiment, AffymetrixChip, AffymetrixProbeset> {

    public AffymetrixRawDataContainer(Collection<AffymetrixExperiment> experiments,
            Collection<AffymetrixChip> assays, Collection<AffymetrixProbeset> calls) {
        super(experiments, assays, calls);
    }
}