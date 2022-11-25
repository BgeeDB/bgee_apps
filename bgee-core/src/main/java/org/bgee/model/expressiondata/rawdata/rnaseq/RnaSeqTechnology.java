package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Comparator;
import java.util.Objects;

import org.bgee.model.expressiondata.rawdata.baseelements.CellCompartment;
import org.bgee.model.expressiondata.rawdata.baseelements.SequencedTranscriptPart;
import org.bgee.model.expressiondata.rawdata.baseelements.Strand;

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
            .thenComparing(t -> t.getFragmentation(), Comparator.nullsLast(Comparator.naturalOrder()));
    
    
    private final String protocolName;
    private final String sequencingPlatfomName;
    private final Strand strand;
    private final SequencedTranscriptPart sequencedTranscriptPart;
    private final CellCompartment cellCompartment;
    private final boolean isSampleMultiplexing;
    private final boolean isLibraryMultiplexing;
    private final Integer fragmentation;
    //TODO: create an enum as for RNASeqLibraryDAO.LibraryType
    private final String libraryType;
    
    public RnaSeqTechnology(String protocolName, String sequencingPlatformName,
            Strand strand, SequencedTranscriptPart sequencedTranscriptPart,
            CellCompartment cellCompartment, boolean isSampleMultiplexing, 
            boolean isLibraryMultiplexing, Integer fragmentation, String libraryType) {
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
    public Integer getFragmentation() {
        return fragmentation;
    }
    public String getProtocolName() {
        return protocolName;
    }
    public String getLibraryType() {
        return libraryType;
    }
    public String getSequencingPlatfomName() {
        return sequencingPlatfomName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellCompartment, fragmentation, isLibraryMultiplexing,
                isSampleMultiplexing, libraryType,
                protocolName, sequencedTranscriptPart, sequencingPlatfomName, strand);
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
        return cellCompartment == other.cellCompartment
                && Objects.equals(fragmentation, other.fragmentation)
                && isLibraryMultiplexing == other.isLibraryMultiplexing
                && isSampleMultiplexing == other.isSampleMultiplexing
                && Objects.equals(libraryType, other.libraryType)
                && Objects.equals(protocolName, other.protocolName)
                && sequencedTranscriptPart == other.sequencedTranscriptPart
                && Objects.equals(sequencingPlatfomName, other.sequencingPlatfomName)
                && strand == other.strand;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RnaSeqTechnology [")
               .append("protocolName=").append(protocolName)
               .append(", sequencingPlatfomName=").append(sequencingPlatfomName)
               .append(", strand=").append(strand)
               .append(", sequencedTranscriptPart=").append(sequencedTranscriptPart)
               .append(", cellCompartment=").append(cellCompartment)
               .append(", isSampleMultiplexing=").append(isSampleMultiplexing)
               .append(", isLibraryMultiplexing=").append(isLibraryMultiplexing)
               .append(", fragmentation=").append(fragmentation)
               .append(", libraryType=").append(libraryType)
               .append("]");
        return builder.toString();
    }
}