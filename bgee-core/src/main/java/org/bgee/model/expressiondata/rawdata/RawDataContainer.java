package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.DataContainer;
import org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence;
import org.bgee.model.expressiondata.rawdata.insitu.InSituExperiment;
import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqExperiment;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibrary;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqLibraryAnnotatedSample;
import org.bgee.model.expressiondata.rawdata.rnaseq.RnaSeqResultAnnotatedSample;

/**
 * A class allowing to contain all results of a raw data query to {@link RawDataLoader}.
 *
 * @author Frederic Bastian
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see RawDataLoader
 */
public class RawDataContainer extends DataContainer {
    private final static Logger log = LogManager.getLogger(RawDataContainer.class.getName());

    private static EnumSet<DataType> computeRequestedDataTypes(
            Collection<RnaSeqExperiment> rnaSeqExperiments, Collection<RnaSeqLibrary> rnaSeqLibraries,
            Collection<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Collection<RnaSeqResultAnnotatedSample> rnaSeqCalls,
            Collection<InSituExperiment> inSituExperiments,
            Collection<InSituEvidence> inSituAssays, Collection<InSituSpot> inSituCalls) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}",
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays, inSituCalls);

        EnumSet<DataType> requestedDataTypes = EnumSet.noneOf(DataType.class);
        if (rnaSeqExperiments != null ||
                rnaSeqLibraries != null ||
                rnaSeqAssays != null ||
                rnaSeqCalls != null) {
            requestedDataTypes.add(DataType.RNA_SEQ);
        }
        if (inSituExperiments != null ||
                inSituAssays != null ||
                inSituCalls != null) {
            requestedDataTypes.add(DataType.IN_SITU);
        }

        return log.traceExit(requestedDataTypes);
    }
    private static EnumSet<DataType> computeDataTypesWithResults(
            Collection<RnaSeqExperiment> rnaSeqExperiments, Collection<RnaSeqLibrary> rnaSeqLibraries,
            Collection<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Collection<RnaSeqResultAnnotatedSample> rnaSeqCalls,
            Collection<InSituExperiment> inSituExperiments,
            Collection<InSituEvidence> inSituAssays, Collection<InSituSpot> inSituCalls) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}",
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays, inSituCalls);

        EnumSet<DataType> dataTypesWithResults = EnumSet.noneOf(DataType.class);
        if (rnaSeqExperiments != null && !rnaSeqExperiments.isEmpty() ||
                rnaSeqLibraries != null && !rnaSeqLibraries.isEmpty() ||
                rnaSeqAssays != null && !rnaSeqAssays.isEmpty() ||
                rnaSeqCalls != null && !rnaSeqCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.RNA_SEQ);
        }
        if (inSituExperiments != null && !inSituExperiments.isEmpty() ||
                inSituAssays != null && !inSituAssays.isEmpty() ||
                inSituCalls != null && !inSituCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.IN_SITU);
        }

        return log.traceExit(dataTypesWithResults);
    }

    private final Set<RnaSeqExperiment> rnaSeqExperiments;
    private final Set<RnaSeqLibrary> rnaSeqLibraries;
    private final Set<RnaSeqLibraryAnnotatedSample> rnaSeqAssays;
    private final Set<RnaSeqResultAnnotatedSample> rnaSeqCalls;

    private final Set<InSituExperiment> inSituExperiments;
    private final Set<InSituEvidence> inSituAssays;
    private final Set<InSituSpot> inSituCalls;

    public RawDataContainer(
            Collection<RnaSeqExperiment> rnaSeqExperiments, Collection<RnaSeqLibrary> rnaSeqLibraries,
            Collection<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Collection<RnaSeqResultAnnotatedSample> rnaSeqCalls, Collection<InSituExperiment> inSituExperiments,
            Collection<InSituEvidence> inSituAssays, Collection<InSituSpot> inSituCalls) {
        super(computeRequestedDataTypes(
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays, inSituCalls),
              computeDataTypesWithResults(
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays,
                inSituCalls));

        this.rnaSeqExperiments = rnaSeqExperiments == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqExperiments));
        this.rnaSeqLibraries = rnaSeqLibraries == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqLibraries));
        this.rnaSeqAssays = rnaSeqAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqAssays));
        this.rnaSeqCalls = rnaSeqCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqCalls));

        this.inSituExperiments = inSituExperiments == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(inSituExperiments));
        this.inSituAssays = inSituAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(inSituAssays));
        this.inSituCalls = inSituCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(inSituCalls));
    }

    /**
     * @return  A {@code Set} of {@code RnaSeqExperiment}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RnaSeqExperiment> getRnaSeqExperiments() {
        return rnaSeqExperiments;
    }
    /**
     * @return  A {@code Set} of {@code RnaSeqLibrary}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RnaSeqLibrary> getRnaSeqLibraries() {
        return rnaSeqLibraries;
    }
    /**
     * @return  A {@code Set} of {@code RnaSeqLibraryAnnotatedSample}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RnaSeqLibraryAnnotatedSample> getRnaSeqAssays() {
        return rnaSeqAssays;
    }
    /**
     * @return  A {@code Set} of {@code RnaSeqResultAnnotatedSample}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RnaSeqResultAnnotatedSample> getRnaSeqCalls() {
        return rnaSeqCalls;
    }

    /**
     * @return  A {@code Set} of {@code InSituExperiment}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<InSituExperiment> getInSituExperiments() {
        return inSituExperiments;
    }
    /**
     * @return  A {@code Set} of {@code InSituEvidence}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<InSituEvidence> getInSituAssays() {
        return inSituAssays;
    }
    /**
     * @return  A {@code Set} of {@code InSituSpot}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<InSituSpot> getInSituCalls() {
        return inSituCalls;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(
                inSituAssays, inSituCalls, inSituExperiments, rnaSeqAssays,
                rnaSeqCalls, rnaSeqExperiments, rnaSeqLibraries);
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
        RawDataContainer other = (RawDataContainer) obj;
        return Objects.equals(inSituAssays, other.inSituAssays)
                && Objects.equals(inSituCalls, other.inSituCalls)
                && Objects.equals(inSituExperiments, other.inSituExperiments)
                && Objects.equals(rnaSeqAssays, other.rnaSeqAssays)
                && Objects.equals(rnaSeqCalls, other.rnaSeqCalls)
                && Objects.equals(rnaSeqExperiments, other.rnaSeqExperiments)
                && Objects.equals(rnaSeqLibraries, other.rnaSeqLibraries);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataContainer [requestedDataTypes=").append(this.getRequestedDataTypes())
               .append(", dataTypesWithResults=").append(this.getDataTypesWithResults())
               .append(", rnaSeqExperiments=").append(rnaSeqExperiments)
               .append(", rnaSeqLibraries=").append(rnaSeqLibraries)
               .append(", rnaSeqAssays=").append(rnaSeqAssays)
               .append(", rnaSeqCalls=").append(rnaSeqCalls)
               .append(", inSituExperiments=").append(inSituExperiments)
               .append(", inSituAssays=").append(inSituAssays)
               .append(", inSituCalls=").append(inSituCalls).append("]");
        return builder.toString();
    }
}