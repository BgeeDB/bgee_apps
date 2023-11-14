package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Objects;

/**
 * An extension of {@code link RawDataCountContainer} for data types having experiments.
 *
 * @author Julien Wollbrett
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see RawDataCountContainer
 */
public class RawDataCountContainerWithExperiment extends RawDataCountContainer {
    private final Integer experimentCount;

    protected RawDataCountContainerWithExperiment(Integer experimentCount, Integer assayCount,
            Integer callCount) {
        this(experimentCount, assayCount, callCount, false);
    }
    protected RawDataCountContainerWithExperiment(Integer experimentCount, Integer assayCount,
            Integer callCount, boolean resultAlreadyFound) {
        super(assayCount, callCount,
                resultAlreadyFound || experimentCount != null && experimentCount > 0);

        if (experimentCount != null && experimentCount < 0) {
            throw new IllegalArgumentException("experimentCount cannot be negative");
        }
        this.experimentCount = experimentCount;
    }

    /**
     * @return  An {@code Integer} that is the number of {@code Experiment}s that were found.
     *          If {@code null}, it means that this information was not requested.
     *          If equals to 0, it means that there was no result based on query parameters.
     */
    public Integer getExperimentCount() {
        return experimentCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(experimentCount);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataCountContainerWithExperiment other = (RawDataCountContainerWithExperiment) obj;
        return Objects.equals(experimentCount, other.experimentCount);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataCountContainerWithExperiment [")
               .append("experimentCount=").append(experimentCount)
               .append(", getAssayCount()=").append(getAssayCount())
               .append(", getCallCount()=").append(getCallCount())
               .append(", isResultFound()=").append(isResultFound())
               .append("]");
        return builder.toString();
    }
}