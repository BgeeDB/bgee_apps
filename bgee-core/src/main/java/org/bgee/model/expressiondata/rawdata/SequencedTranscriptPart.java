package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.BgeeEnum;

public enum SequencedTranscriptPart implements BgeeEnumField {
    NA("NA"), THREEPRIME("3prime"), FIVEPRIME("5prime"), FULLLENGTH("full length");
    
    private String name;
    
    SequencedTranscriptPart(String name) {
        this.name = name;
    }
    
    @Override
    public String getStringRepresentation() {
        return this.getName();
    }
    
    /**
     * @return the {@code String} that is the name of the sequenced transcript part.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Convert the {@code String} representation of a sequenced transcript part (for instance, 
     * retrieved from request) into a {@code SequencedTranscriptPart}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code SequencedTranscriptPart} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a sequenced transcript part category.
     * @return                          A {@code SequencedTranscriptPart} corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any {@code SequencedTranscriptPart}.
     * @see #convert(Class, String)
     */
    public static final SequencedTranscriptPart convertToSequencedTranscriptPart(String representation) {
        return BgeeEnum.convert(SequencedTranscriptPart.class, representation);
    }
}
