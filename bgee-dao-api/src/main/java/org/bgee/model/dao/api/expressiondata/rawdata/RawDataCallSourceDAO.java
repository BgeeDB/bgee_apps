package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.TransferObject;

public class RawDataCallSourceDAO {

    public static class CallSourceTO<T extends Comparable<T>> extends TransferObject {
        private static final long serialVersionUID = -7947051666248235602L;

        private final T assayId;

        public CallSourceTO(T assayId) {
            this.assayId = assayId;
        }

        public T getAssayId() {
            return this.assayId;
        }
    }
}
