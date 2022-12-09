package org.bgee.model.expressiondata.rawdata.insitu;

import java.util.Collection;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;

/**
 * A {@code RawDataContainerWithExperiment} for In situ data.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 */
public class InSituContainer
extends RawDataContainerWithExperiment<InSituExperiment, InSituEvidence, InSituSpot> {

    public InSituContainer(Collection<InSituExperiment> experiments,
            Collection<InSituEvidence> assays, Collection<InSituSpot> calls) {
        super(experiments, assays, calls);
    }
}