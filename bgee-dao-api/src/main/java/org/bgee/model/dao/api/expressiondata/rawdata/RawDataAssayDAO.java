package org.bgee.model.dao.api.expressiondata.rawdata;

public class RawDataAssayDAO {

    public static interface AssayTO<T extends Comparable<T>> {
        public T getId();
    }
    public static interface AssayPartOfExpTO<T extends Comparable<T>, U extends Comparable<U>> extends AssayTO<T> {
        public U getExperimentId();
    }
}
