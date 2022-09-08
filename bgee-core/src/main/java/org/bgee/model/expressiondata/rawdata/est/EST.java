package org.bgee.model.expressiondata.rawdata.est;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.RawCall;
import org.bgee.model.expressiondata.rawdata.RawCallSource;

public class EST extends Entity<String> implements RawCallSource<ESTLibrary>{

    private final static Logger log = LogManager.getLogger(EST.class);
    private final ESTLibrary assay;
    private final RawCall rawCall;

    @Override
    public ESTLibrary getAssay() {
        return this.assay;
    }

    @Override
    public RawCall getRawCall() {
        return this.rawCall;
    }

    public EST(String id, ESTLibrary assay, RawCall rawCall) throws IllegalArgumentException {
        super(id);
        if (assay == null) {
            throw log.throwing(new IllegalArgumentException("ESTLibrary cannot be null"));
        }
        this.assay = assay;
        if (rawCall == null) {
            throw log.throwing(new IllegalArgumentException("RawCall cannot be null"));
        }
        this.rawCall = rawCall;
    }

}
