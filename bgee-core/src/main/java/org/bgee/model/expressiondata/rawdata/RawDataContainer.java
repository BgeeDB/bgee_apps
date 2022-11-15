package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
public class RawDataContainer {

    private final EnumSet<DataType> requestedDataTypes;
    private final EnumSet<DataType> dataTypesWithResults;

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

        EnumSet<DataType> requestedDataTypes = EnumSet.noneOf(DataType.class);
        EnumSet<DataType> dataTypesWithResults = EnumSet.noneOf(DataType.class);

        this.affymetrixExperiments = affymetrixExperiments == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(affymetrixExperiments));
        this.affymetrixAssays = affymetrixAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(affymetrixAssays));
        this.affymetrixCalls = affymetrixCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(affymetrixCalls));
        if (this.affymetrixExperiments != null ||
                this.affymetrixAssays != null ||
                this.affymetrixCalls != null) {
            requestedDataTypes.add(DataType.AFFYMETRIX);
        }
        if (this.affymetrixExperiments != null && !this.affymetrixExperiments.isEmpty() ||
                this.affymetrixAssays != null && !this.affymetrixAssays.isEmpty() ||
                this.affymetrixCalls != null && !this.affymetrixCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.AFFYMETRIX);
        }

        this.rnaSeqExperiments = rnaSeqExperiments == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqExperiments));
        this.rnaSeqLibraries = rnaSeqLibraries == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqLibraries));
        this.rnaSeqAssays = rnaSeqAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqAssays));
        this.rnaSeqCalls = rnaSeqCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(rnaSeqCalls));
        if (this.rnaSeqExperiments != null ||
                this.rnaSeqLibraries != null ||
                this.rnaSeqAssays != null ||
                this.rnaSeqCalls != null) {
            requestedDataTypes.add(DataType.RNA_SEQ);
        }
        if (this.rnaSeqExperiments != null && !this.rnaSeqExperiments.isEmpty() ||
                this.rnaSeqLibraries != null && !this.rnaSeqLibraries.isEmpty() ||
                this.rnaSeqAssays != null && !this.rnaSeqAssays.isEmpty() ||
                this.rnaSeqCalls != null && !this.rnaSeqCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.RNA_SEQ);
        }

        this.inSituExperiments = inSituExperiments == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(inSituExperiments));
        this.inSituAssays = inSituAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(inSituAssays));
        this.inSituCalls = inSituCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(inSituCalls));
        if (this.inSituExperiments != null ||
                this.inSituAssays != null ||
                this.inSituCalls != null) {
            requestedDataTypes.add(DataType.IN_SITU);
        }
        if (this.inSituExperiments != null && !this.inSituExperiments.isEmpty() ||
                this.inSituAssays != null && !this.inSituAssays.isEmpty() ||
                this.inSituCalls != null && !this.inSituCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.IN_SITU);
        }

        this.estAssays = estAssays == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(estAssays));
        this.estCalls = estCalls == null? null:
            Collections.unmodifiableSet(new LinkedHashSet<>(estCalls));
        if (this.estAssays != null ||
                this.estCalls != null) {
            requestedDataTypes.add(DataType.EST);
        }
        if (this.estAssays != null && !this.estAssays.isEmpty() ||
                this.estCalls != null && !this.estCalls.isEmpty()) {
            dataTypesWithResults.add(DataType.EST);
        }

        //We will use defensive copying in the getter
        this.requestedDataTypes = requestedDataTypes;
        this.dataTypesWithResults = dataTypesWithResults;
    }

    /**
     * @return  An {@code EnumSet} of {@code DataType}s specifying the data types that were requested.
     *          This {@code EnumSet} is a copy of the attribute (defensive copying).
     */
    public EnumSet<DataType> getRequestedDataTypes() {
        //defensive copying
        return EnumSet.copyOf(this.requestedDataTypes);
    }
    /**
     * @return  An {@code EnumSet} of {@code DataType}s specifying the data types
     *          for which results exist.
     *          This {@code EnumSet} is a copy of the attribute (defensive copying).
     */
    public EnumSet<DataType> getDataTypesWithResults() {
        //defensive copying
        return EnumSet.copyOf(this.dataTypesWithResults);
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
        return Objects.hash(affymetrixAssays, affymetrixCalls, affymetrixExperiments,
                requestedDataTypes, dataTypesWithResults, estAssays, estCalls,
                inSituAssays, inSituCalls, inSituExperiments, rnaSeqAssays, rnaSeqCalls, rnaSeqExperiments,
                rnaSeqLibraries);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataContainer other = (RawDataContainer) obj;
        return Objects.equals(affymetrixAssays, other.affymetrixAssays)
                && Objects.equals(affymetrixCalls, other.affymetrixCalls)
                && Objects.equals(affymetrixExperiments, other.affymetrixExperiments)
                && Objects.equals(requestedDataTypes, other.requestedDataTypes)
                && Objects.equals(dataTypesWithResults, other.dataTypesWithResults)
                && Objects.equals(estAssays, other.estAssays)
                && Objects.equals(estCalls, other.estCalls) && Objects.equals(inSituAssays, other.inSituAssays)
                && Objects.equals(inSituCalls, other.inSituCalls)
                && Objects.equals(inSituExperiments, other.inSituExperiments)
                && Objects.equals(rnaSeqAssays, other.rnaSeqAssays) && Objects.equals(rnaSeqCalls, other.rnaSeqCalls)
                && Objects.equals(rnaSeqExperiments, other.rnaSeqExperiments)
                && Objects.equals(rnaSeqLibraries, other.rnaSeqLibraries);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataContainer [requestedDataTypes=").append(requestedDataTypes)
               .append(", dataTypesWithResults=").append(dataTypesWithResults)
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