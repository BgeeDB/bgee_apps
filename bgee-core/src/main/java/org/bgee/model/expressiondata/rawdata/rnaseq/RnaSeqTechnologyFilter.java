package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bgee.model.expressiondata.rawdata.baseelements.CellCompartment;
import org.bgee.model.expressiondata.rawdata.baseelements.SequencedTranscriptPart;
import org.bgee.model.expressiondata.rawdata.baseelements.Strand;

/**
 * A filter to parameterize queries using rnaseq technology. 
 * each values of a same attribute of this filter correspond to a OR and
 * values of different attributes correspond to a AND 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 15, Jui. 2022
 * @since   Bgee 15, Jui. 2022
 */
public class RnaSeqTechnologyFilter implements Predicate<RnaSeqTechnology>{
    
    private final Set<String> protocolName;
    
    private final EnumSet<Strand> strands;
    
    private final EnumSet<CellCompartment> cellCompartments;
    
    private final EnumSet<SequencedTranscriptPart> sequencedTranscriptParts;
    
    private final Boolean fragmentation;
    
    public RnaSeqTechnologyFilter(Set<String> protocolName, EnumSet<Strand> strands,
            EnumSet<CellCompartment> cellCompartments,
            EnumSet<SequencedTranscriptPart> sequencedTranscriptParts, Boolean fragmentation) {
        this.protocolName = protocolName == null? new HashSet<>(): protocolName;
        this.strands = strands == null? EnumSet.noneOf(Strand.class) : strands;
        this.cellCompartments = cellCompartments == null? EnumSet.noneOf(CellCompartment.class)
                : cellCompartments;
        this.sequencedTranscriptParts = sequencedTranscriptParts == null?
                EnumSet.noneOf(SequencedTranscriptPart.class): sequencedTranscriptParts;
        this.fragmentation = fragmentation;
    }
    
    public Set<String> getProtocolName() {
        return protocolName;
    }
    public EnumSet<Strand> getStrands() {
        return strands;
    }
    public EnumSet<CellCompartment> getCellCompartments() {
        return cellCompartments;
    }
    public EnumSet<SequencedTranscriptPart> getSequencedTranscriptParts() {
        return sequencedTranscriptParts;
    }
    public Boolean getFragmentation() {
        return fragmentation;
    }

    @Override
    public boolean test(RnaSeqTechnology arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
