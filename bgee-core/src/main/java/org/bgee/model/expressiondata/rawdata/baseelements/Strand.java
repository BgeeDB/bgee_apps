package org.bgee.model.expressiondata.rawdata.baseelements;

import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.BgeeEnum;

public enum Strand implements BgeeEnumField{
    NA("NA"), FORWARD("forward"), REVERT("revert"), UNSTRANDED("unstranded");
    
    private String name;
    
    Strand(String name) {
        this.name = name;
    }
    
    @Override
    public String getStringRepresentation() {
        return this.getName();
    }
    
    /**
     * @return the {@code String} that is the name of the strand.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Convert the {@code String} representation of a strand category (for instance, 
     * retrieved from request) into a {@code Strand}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code Strand} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a strand category.
     * @return                          A {@code Strand} corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any {@code Strand}.
     * @see #convert(Class, String)
     */
    public static final Strand convertToStrand(String representation) {
        return BgeeEnum.convert(Strand.class, representation);
    }
}
