package org.bgee.model.expressiondata.rawdata.baseelements;

import org.bgee.model.BgeeEnum.BgeeEnumField;

public enum RawDataSex implements BgeeEnumField {
    NOT_ANNOTATED("not annotated"), HERMAPHRODITE("hermaphrodite"), FEMALE("female"), MALE("male"),
    MIXED("mixed"), NA("NA");
    
    private final String representation;
    
    private RawDataSex(String representation) {
        this.representation = representation;
    }

    @Override
    public String getStringRepresentation() {
        return this.representation;
    }
}
