package org.bgee.model.expressiondata.rawdata.baseelements;

import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.BgeeEnum;

public enum CellCompartment implements BgeeEnumField{
    NA("NA"), NUCLEUS("nucleus"), CELL("cell");
    
    private String name;
    
    CellCompartment(String name) {
        this.name = name;
    }
    
    @Override
    public String getStringRepresentation() {
        return this.getName();
    }
    
    /**
     * @return the {@code String} that is the name of the cell compartment.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Convert the {@code String} representation of a cell compartment (for instance, 
     * retrieved from request) into a {@code CellCompartment}.
     * Operation performed by calling {@link BgeeEnum#convert(Class, String)} with 
     * {@code CellCompartment} as the {@code Class} argument, and {@code representation} 
     * as the {@code String} argument.
     * 
     * @param representation            A {@code String} representing a cell compartment category.
     * @return                          A {@code CellCompartment} corresponding to {@code representation}.
     * @throws IllegalArgumentException If {@code representation} does not correspond 
     *                                  to any {@code CellCompartment}.
     * @see #convert(Class, String)
     */
    public static final CellCompartment convertToCellCompartment(String representation) {
        return BgeeEnum.convert(CellCompartment.class, representation);
    }
}
