package org.bgee.model.expressiondata.baseelements;

public enum StatisticTest {
    
    FISHER ("fisher"), 
    KS ("ks"), 
    T ("t"), 
    GLOBALTEST ("globaltest"),
    SUM ("sum"),
    KS_TIE("ks.ties");
    
    private final String code;
    
    StatisticTest(String code){
        this.code = code;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }
    
}
