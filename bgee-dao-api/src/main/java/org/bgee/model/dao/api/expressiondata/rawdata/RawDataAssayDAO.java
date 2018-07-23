package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.NamedEntityTO;

public class RawDataAssayDAO {

    public static interface AssayTO<T extends Comparable<T>> {
        public T getId();
    }
    public static interface AssayPartOfExpTO<T extends Comparable<T>, U extends Comparable<U>> extends AssayTO<T> {
        public U getExperimentId();
    }
    public static class EntityAssayTO<T extends Comparable<T>> extends EntityTO<T> implements AssayTO<T> {
        private static final long serialVersionUID = 6698645263299277299L;

        public EntityAssayTO(T id) {
            super(id);
        }
    }
    public static class NamedEntityAssayTO<T extends Comparable<T>> extends NamedEntityTO<T> implements AssayTO<T> {
        private static final long serialVersionUID = 6081060013742657317L;

        public NamedEntityAssayTO(T id, String name, String description) {
            super(id, name, description);
        }
    }
}
