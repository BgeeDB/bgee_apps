package org.bgee.model.expressiondata.rawdata;

import java.math.BigDecimal;

//XXX: should it rather be "RawCallWithRank"?
public interface RawCallSourceWithRank {
    public BigDecimal getRank();
}
