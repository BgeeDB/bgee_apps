package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.EntityTO;

public class RawDataAssayDAO {

    public static class AssayTO<T extends Comparable<T>> extends EntityTO<T> {
        private static final long serialVersionUID = -5570797689549107085L;

        public AssayTO(T id) {
            super(id);
            // TODO Auto-generated constructor stub
        }
    }
    public static interface AssayPartOfExpTO<T extends Comparable<T>> {
        public T getExperimentId();
    }
}
