package org.bgee.model.expressiondata.rawdata;

public abstract class RawDataCount {
    //GROUP BY ATTRIBUTES:
    //Condition
    //Gene
    //SummaryCallType
    //SummaryQual
    //Species?
    
    public static final class RawDataAffymetrixCount extends RawDataCount {
        //int probeset
        //int chip
        //int exp
    }
}
