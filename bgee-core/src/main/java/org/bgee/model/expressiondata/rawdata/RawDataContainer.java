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
import org.bgee.model.expressiondata.rawdata.est.EST;
import org.bgee.model.expressiondata.rawdata.est.ESTLibrary;
import org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence;
import org.bgee.model.expressiondata.rawdata.insitu.InSituExperiment;
import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
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
            Collection<AffymetrixExperiment> affymetrixExperiments,
            Collection<AffymetrixChip> affymetrixAssays, Collection<AffymetrixProbeset> affymetrixCalls,
            Collection<RnaSeqExperiment> rnaSeqExperiments, Collection<RnaSeqLibrary> rnaSeqLibraries,
            Collection<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Collection<RnaSeqResultAnnotatedSample> rnaSeqCalls,
            Collection<InSituExperiment> inSituExperiments,
            Collection<InSituEvidence> inSituAssays, Collection<InSituSpot> inSituCalls,
            Collection<ESTLibrary> estAssays, Collection<EST> estCalls) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                affymetrixExperiments, affymetrixAssays, affymetrixCalls,
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays, inSituCalls,
                estAssays, estCalls);

        EnumSet<DataType> requestedDataTypes = EnumSet.noneOf(DataType.class);
        if (affymetrixExperiments != null ||
                affymetrixAssays != null ||
                affymetrixCalls != null) {
            requestedDataTypes.add(DataType.AFFYMETRIX);
        }
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
        if (estAssays != null ||
                estCalls != null) {
            requestedDataTypes.add(DataType.EST);
        }

        return log.traceExit(requestedDataTypes);
    }
    private static EnumSet<DataType> computeDataTypesWithResults(
            Collection<AffymetrixExperiment> affymetrixExperiments,
            Collection<AffymetrixChip> affymetrixAssays, Collection<AffymetrixProbeset> affymetrixCalls,
            Collection<RnaSeqExperiment> rnaSeqExperiments, Collection<RnaSeqLibrary> rnaSeqLibraries,
            Collection<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Collection<RnaSeqResultAnnotatedSample> rnaSeqCalls,
            Collection<InSituExperiment> inSituExperiments,
            Collection<InSituEvidence> inSituAssays, Collection<InSituSpot> inSituCalls,
            Collection<ESTLibrary> estAssays, Collection<EST> estCalls) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                affymetrixExperiments, affymetrixAssays, affymetrixCalls,
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays, inSituCalls,
                estAssays, estCalls);

        EnumSet<DataType> dataTypesWithResults = EnumSet.noneOf(DataType.class);
        if (affymetrixExperiments != null && !affymetrixExperiments.isEmpty() ||
                affymetrixAssays != null && !affymetrixAssays.isEmpty() ||
                affymetrixCalls != null && !affymetrixCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.AFFYMETRIX);
        }
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
        if (estAssays != null && !estAssays.isEmpty() ||
                estCalls != null && !estCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.EST);
        }

        return log.traceExit(dataTypesWithResults);
    }

    private final Set<AffymetrixExperiment> affymetrixExperiments;
    private final Set<AffymetrixChip> affymetrixAssays;
    private final Set<AffymetrixProbeset> affymetrixCalls;

    private final Set<RnaSeqExperiment> rnaSeqExperiments;
    private final Set<RnaSeqLibrary> rnaSeqLibraries;
    private final Set<RnaSeqLibraryAnnotatedSample> rnaSeqAssays;
    private final Set<RnaSeqResultAnnotatedSample> rnaSeqCalls;

    private final Set<InSituExperiment> inSituExperiments;
    private final Set<InSituEvidence> inSituAssays;
    private final Set<InSituSpot> inSituCalls;

    private final Set<ESTLibrary> estAssays;
    private final Set<EST> estCalls;

    public RawDataContainer(Collection<AffymetrixExperiment> affymetrixExperiments,
            Collection<AffymetrixChip> affymetrixAssays, Collection<AffymetrixProbeset> affymetrixCalls,
            Collection<RnaSeqExperiment> rnaSeqExperiments, Collection<RnaSeqLibrary> rnaSeqLibraries,
            Collection<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Collection<RnaSeqResultAnnotatedSample> rnaSeqCalls, Collection<InSituExperiment> inSituExperiments,
            Collection<InSituEvidence> inSituAssays, Collection<InSituSpot> inSituCalls,
            Collection<ESTLibrary> estAssays, Collection<EST> estCalls) {
        super(computeRequestedDataTypes(
                affymetrixExperiments, affymetrixAssays, affymetrixCalls,
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays,
                inSituCalls, estAssays, estCalls),
              computeDataTypesWithResults(
                affymetrixExperiments, affymetrixAssays, affymetrixCalls,
                rnaSeqExperiments, rnaSeqLibraries, rnaSeqAssays, rnaSeqCalls,
                inSituExperiments, inSituAssays,
                inSituCalls, estAssays, estCalls));

        this.affymetrixExperiments = affymetrixExperiments == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(affymetrixExperiments));
        this.affymetrixAssays = affymetrixAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(affymetrixAssays));
        this.affymetrixCalls = affymetrixCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(affymetrixCalls));

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

        this.estAssays = estAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(estAssays));
        this.estCalls = estCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(estCalls));
    }

    /**
     * @return  A {@code Set} of {@code AffymetrixExperiment}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AffymetrixExperiment> getAffymetrixExperiments() {
        return affymetrixExperiments;
    }
    /**
     * @return  A {@code Set} of {@code AffymetrixChip}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AffymetrixChip> getAffymetrixAssays() {
        return affymetrixAssays;
    }
    /**
     * @return  A {@code Set} of {@code AffymetrixProbeset}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AffymetrixProbeset> getAffymetrixCalls() {
        return affymetrixCalls;
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

    /**
     * @return  A {@code Set} of {@code ESTLibrary}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<ESTLibrary> getEstAssays() {
        return estAssays;
    }
    /**
     * @return  A {@code Set} of {@code EST}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<EST> getEstCalls() {
        return estCalls;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(
                affymetrixAssays, affymetrixCalls, affymetrixExperiments, estAssays,
                estCalls, inSituAssays, inSituCalls, inSituExperiments, rnaSeqAssays,
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
        return Objects.equals(affymetrixAssays, other.affymetrixAssays)
                && Objects.equals(affymetrixCalls, other.affymetrixCalls)
                && Objects.equals(affymetrixExperiments, other.affymetrixExperiments)
                && Objects.equals(estAssays, other.estAssays)
                && Objects.equals(estCalls, other.estCalls)
                && Objects.equals(inSituAssays, other.inSituAssays)
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
               .append(", affymetrixExperiments=").append(affymetrixExperiments)
               .append(", affymetrixAssays=").append(affymetrixAssays)
               .append(", affymetrixCalls=").append(affymetrixCalls)
               .append(", rnaSeqExperiments=").append(rnaSeqExperiments)
               .append(", rnaSeqLibraries=").append(rnaSeqLibraries)
               .append(", rnaSeqAssays=").append(rnaSeqAssays)
               .append(", rnaSeqCalls=").append(rnaSeqCalls)
               .append(", inSituExperiments=").append(inSituExperiments)
               .append(", inSituAssays=").append(inSituAssays)
               .append(", inSituCalls=").append(inSituCalls)
               .append(", estAssays=").append(estAssays)
               .append(", estCalls=").append(estCalls).append("]");
        return builder.toString();
    }
}