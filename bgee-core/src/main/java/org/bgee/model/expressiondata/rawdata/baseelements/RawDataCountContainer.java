package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Objects;

/**
 * A class allowing to contain count for all potential raw data resulting
 * from a query to {@code RawDataService}.
 *
 * @author Julien Wollbrett
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public abstract class RawDataCountContainer extends DataContainerTemp {
    private final Integer assayCount;
    private final Integer callCount;

    protected RawDataCountContainer(Integer assayCount, Integer callCount) {
        this(assayCount, callCount, false);
    }
    // null if not queried and 0 if no results
    protected RawDataCountContainer(Integer assayCount, Integer callCount, boolean resultAlreadyFound) {
        super(resultAlreadyFound || assayCount != null && assayCount > 0 ||
                callCount != null && callCount > 0);
        if (assayCount != null && assayCount < 0) {
            throw new IllegalArgumentException("assayCount cannot be negative");
        }
        if (callCount != null && callCount < 0) {
            throw new IllegalArgumentException("callCount cannot be negative");
        }
        this.assayCount = assayCount;
        this.callCount = callCount;
    }

    /**
     * @return  An {@code Integer} that is the number of {@code Assay}s that were found.
     *          If {@code null}, it means that this information was not requested.
     *          If equals to 0, it means that there was no result based on query parameters.
     */
    public Integer getAssayCount() {
        return assayCount;
    }
    /**
     * @return  An {@code Integer} that is the number of {@code RawCallSource}s that were found.
     *          If {@code null}, it means that this information was not requested.
     *          If equals to 0, it means that there was no result based on query parameters.
     */
    public Integer getCallCount() {
        return callCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(assayCount, callCount);
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
        RawDataCountContainer other = (RawDataCountContainer) obj;
        return Objects.equals(assayCount, other.assayCount) && Objects.equals(callCount, other.callCount);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataCountContainer [")
               .append("assayCount=").append(assayCount)
               .append(", callCount=").append(callCount)
               .append(", isResultFound()=").append(isResultFound())
               .append("]");
        return builder.toString();
    }
}