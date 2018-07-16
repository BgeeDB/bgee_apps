package org.bgee.model.expressiondata.rawdata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;

public class Assay<T extends Comparable<T>> extends Entity<T> {
    private final static Logger log = LogManager.getLogger(Assay.class.getName());

    public Assay(T id) throws IllegalArgumentException {
        super(id);
    }
}
