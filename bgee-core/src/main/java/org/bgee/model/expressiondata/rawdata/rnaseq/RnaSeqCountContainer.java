package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Objects;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainerWithExperiment;

/**
 * A {@code RawDataCountContainerWithExperiment} for RNA-Seq data.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RnaSeqCountContainer extends RawDataCountContainerWithExperiment {

    private final Integer libraryCount;

    public RnaSeqCountContainer(Integer experimentCount, Integer libraryCount,
            Integer assayCount, Integer callCount) {
        super(experimentCount, assayCount, callCount, libraryCount != null && libraryCount > 0);

        if (libraryCount != null && libraryCount < 0) {
            throw new IllegalArgumentException("libraryCount cannot be negative");
        }
        this.libraryCount = libraryCount;
    }

    /**
     * @return  An {@code Integer} that is the number of {@code RnaSeqLibrary}s that were found.
     *          If {@code null}, it means that this information was not requested.
     *          If equals to 0, it means that there was no result based on query parameters.
     */
    public Integer getLibraryCount() {
        return libraryCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(libraryCount);
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
        RnaSeqCountContainer other = (RnaSeqCountContainer) obj;
        return Objects.equals(libraryCount, other.libraryCount);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RnaSeqCountContainer [")
                .append("getExperimentCount()=").append(getExperimentCount())
                .append(", libraryCount=").append(libraryCount)
                .append(", getAssayCount()=").append(getAssayCount())
                .append(", getCallCount()=").append(getCallCount())
                .append(", isResultFound()=").append(isResultFound())
                .append("]");
        return builder.toString();
    }
}