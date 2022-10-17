package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

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

public class RawDataContainer {
    
    private final EnumSet<DataType> datatypes;
    private final LinkedHashSet<AffymetrixExperiment> affymetrixExperiments;
    private final LinkedHashSet<AffymetrixChip> affymetrixAssays;
    private final LinkedHashSet<AffymetrixProbeset> affymetrixCalls;

    private final Stream<RnaSeqExperiment> rnaSeqExperiments;
    private final Stream<RnaSeqLibrary> rnaSeqLibraries;
    private final Stream<RnaSeqLibraryAnnotatedSample> rnaSeqAssays;
    private final Stream<RnaSeqResultAnnotatedSample> rnaSeqCalls;
    private final Stream<InSituExperiment> inSituExperiments;
    private final Stream<InSituEvidence> inSituAssays;
    private final Stream<InSituSpot> inSituCalls;
    private final Stream<ESTLibrary> estAssays;
    private final Stream<EST> estCalls;

    public RawDataContainer(Collection<DataType> datatypes,
            Stream<AffymetrixExperiment> affymetrixExperiments, Stream<AffymetrixChip> affymetrixAssays,
            Stream<AffymetrixProbeset> affymetrixCalls, Stream<RnaSeqExperiment> rnaSeqExperiments,
            Stream<RnaSeqLibrary> rnaSeqLibraries, Stream<RnaSeqLibraryAnnotatedSample> rnaSeqAssays,
            Stream<RnaSeqResultAnnotatedSample> rnaSeqCalls, Stream<InSituExperiment> inSituExperiments,
            Stream<InSituEvidence> inSituAssays, Stream<InSituSpot> inSituCalls,
            Stream<ESTLibrary> estAssays, Stream<EST> estCalls) {
        if (datatypes == null || datatypes.isEmpty()) {
            this.datatypes = EnumSet.allOf(DataType.class);
        } else {
            this.datatypes = EnumSet.copyOf(datatypes);
        }
        this.affymetrixExperiments = affymetrixExperiments;
        this.affymetrixAssays = affymetrixAssays;
        this.affymetrixCalls = affymetrixCalls;
        this.rnaSeqExperiments = rnaSeqExperiments;
        this.rnaSeqLibraries = rnaSeqLibraries;
        this.rnaSeqAssays = rnaSeqAssays;
        this.rnaSeqCalls = rnaSeqCalls;
        this.inSituCalls = inSituCalls;
        this.inSituAssays = inSituAssays;
        this.inSituExperiments = inSituExperiments;
        this.estAssays = estAssays;
        this.estCalls = estCalls;
    }

    public Collection<RawDataDataType> getDatatypes() {
        return datatypes;
    }
    public Stream<AffymetrixExperiment> getAffymetrixExperiments() {
        return affymetrixExperiments;
    }
    public Stream<AffymetrixChip> getAffymetrixAssays() {
        return affymetrixAssays;
    }
    public Stream<AffymetrixProbeset> getAffymetrixCalls() {
        return affymetrixCalls;
    }
    public Stream<RnaSeqExperiment> getRnaSeqExperiments() {
        return rnaSeqExperiments;
    }
    public Stream<RnaSeqLibraryAnnotatedSample> getRnaSeqAssays() {
        return rnaSeqAssays;
    }
    public Stream<RnaSeqResultAnnotatedSample> getRnaSeqCalls() {
        return rnaSeqCalls;
    }
    public Stream<InSituExperiment> getInSituExperiments() {
        return inSituExperiments;
    }
    public Stream<InSituEvidence> getInSituAssays() {
        return inSituAssays;
    }
    public Stream<InSituSpot> getInSituCalls() {
        return inSituCalls;
    }
    public Stream<ESTLibrary> getEstAssays() {
        return estAssays;
    }
    public Stream<EST> getEstCalls() {
        return estCalls;
    }
    public Stream<RnaSeqLibrary> getRnaSeqLibraries() {
        return rnaSeqLibraries;
    }
    
}
