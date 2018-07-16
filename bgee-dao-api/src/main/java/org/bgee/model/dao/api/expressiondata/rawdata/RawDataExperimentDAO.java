package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.NamedEntityTO;

public class RawDataExperimentDAO {

    public static class ExperimentTO<T extends Comparable<T>> extends NamedEntityTO<T> {
        private static final long serialVersionUID = -3865618442951732061L;

        protected ExperimentTO(T id, String name, String description) {
            super(id, name, description);
            // TODO Auto-generated constructor stub
        }
        
    }
}
