package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;

/**
 * A {@code RawDataContainerWithExperiment} for RNA-Seq data.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class RnaSeqContainer extends RawDataContainerWithExperiment<RnaSeqExperiment,
RnaSeqLibraryAnnotatedSample, RnaSeqResultAnnotatedSample> {

    private final Set<RnaSeqLibrary> libraries;

    public RnaSeqContainer(Collection<RnaSeqExperiment> experiments,
            Collection<RnaSeqLibraryAnnotatedSample> assays, Collection<RnaSeqLibrary> libraries,
            Collection<RnaSeqResultAnnotatedSample> calls) {
        super(experiments, assays, calls, libraries != null && !libraries.isEmpty());

        this.libraries = libraries == null? null: Collections.unmodifiableSet(
                new LinkedHashSet<>(libraries));
    }

    /**
     * @return  A {@code Set} of {@code RnaSeqLibrary}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RnaSeqLibrary> getLibraries() {
        return libraries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(libraries);
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
        RnaSeqContainer other = (RnaSeqContainer) obj;
        return Objects.equals(libraries, other.libraries);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RnaSeqContainer [")
               .append("getExperiments()=").append(getExperiments())
               .append(", libraries=").append(libraries)
               .append(", getAssays()=").append(getAssays())
               .append(", getCalls()=").append(getCalls())
               .append(", isResultFound()=").append(isResultFound())
               .append("]");
        return builder.toString();
    }
}