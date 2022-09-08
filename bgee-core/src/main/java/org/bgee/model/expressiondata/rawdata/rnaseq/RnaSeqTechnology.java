package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Comparator;
import java.util.Objects;

import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO.LibraryType;
import org.bgee.model.expressiondata.rawdata.CellCompartment;
import org.bgee.model.expressiondata.rawdata.SequencedTranscriptPart;
import org.bgee.model.expressiondata.rawdata.Strand;

//Note: this class does not extend NamedEntity, because it does not have IDs
public class RnaSeqTechnology {
    
    //TODO check if we want to add/remove attributs to the comparator
    /**
     * A {@code Comparator} for {@code RnaSeqTechnology}s. Sort {@code RnaSeqTechnology}s based on their
     * protocol name, strand, sequenced transcript part, cell compartment and fragmentation
     */
    public static Comparator<RnaSeqTechnology> COMPARATOR = Comparator
            .<RnaSeqTechnology,String>comparing(t -> t.getProtocolName(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(t -> t.getStrand(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(t -> t.getSequencedTranscriptPart(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(t -> t.getCellCompartment(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(t -> t.isFragmentation(), Comparator.nullsLast(Comparator.naturalOrder()));
    
    
    private final String protocolName;
    private final String sequencingPlatfomName;
    private final Strand strand;
    private final SequencedTranscriptPart sequencedTranscriptPart;
    private final CellCompartment cellCompartment;
    private final boolean isSampleMultiplexing;
    private final boolean isLibraryMultiplexing;
    private final boolean fragmentation;
    private final LibraryType libraryType;
    
    public RnaSeqTechnology(String protocolName, String sequencingPlatformName,
            Strand strand, SequencedTranscriptPart sequencedTranscriptPart,
            CellCompartment cellCompartment, boolean isSampleMultiplexing, 
            boolean isLibraryMultiplexing, boolean fragmentation, LibraryType libraryType) {
        this.protocolName = protocolName;
        this.strand = strand;
        this.sequencedTranscriptPart = sequencedTranscriptPart;
        this.cellCompartment = cellCompartment;
        this.isSampleMultiplexing = isSampleMultiplexing;
        this.isLibraryMultiplexing = isLibraryMultiplexing;
        this.fragmentation = fragmentation;
        this.libraryType = libraryType;
        this.sequencingPlatfomName = sequencingPlatformName;
    }
    public Strand getStrand() {
        return strand;
    }
    public SequencedTranscriptPart getSequencedTranscriptPart() {
        return sequencedTranscriptPart;
    }
    public CellCompartment getCellCompartment() {
        return cellCompartment;
    }
    public boolean isSampleMultiplexing() {
        return isSampleMultiplexing;
    }
    public boolean isLibraryMultiplexing() {
        return isLibraryMultiplexing;
    }
    public boolean isFragmentation() {
        return fragmentation;
    }
    public String getProtocolName() {
        return protocolName;
    }
    public LibraryType getLibraryType() {
        return libraryType;
    }
    public String getSequencingPlatfomName() {
        return sequencingPlatfomName;
    }
    @Override
    public int hashCode() {
        return Objects.hash(cellCompartment, fragmentation, isLibraryMultiplexing, isSampleMultiplexing, protocolName,
                sequencedTranscriptPart, strand);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RnaSeqTechnology other = (RnaSeqTechnology) obj;
        return cellCompartment == other.cellCompartment && fragmentation == other.fragmentation
                && isLibraryMultiplexing == other.isLibraryMultiplexing
                && isSampleMultiplexing == other.isSampleMultiplexing
                && Objects.equals(protocolName, other.protocolName)
                && sequencedTranscriptPart == other.sequencedTranscriptPart && strand == other.strand;
    }

    @Override
    public String toString() {
        return "RnaSeqTechnology [protocolName=" + protocolName + ", strand=" + strand + ", sequencedTranscriptPart="
                + sequencedTranscriptPart + ", cellCompartment=" + cellCompartment + ", isSampleMultiplexing="
                + isSampleMultiplexing + ", isLibraryMultiplexing=" + isLibraryMultiplexing + ", fragmentation="
                + fragmentation + "]";
    }
}
