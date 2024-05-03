package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.NamedEntityTO;

public class RawDataExperimentDAO {

    public static class ExperimentTO<T extends Comparable<T>> extends NamedEntityTO<T> implements RawDataWithDataSourceTO {
        private static final long serialVersionUID = -3865618442951732061L;

        private final Integer dataSourceId;
        private final String dOI;

        protected ExperimentTO(T id, String name, String description, Integer dataSourceId) {
            super(id, name, description);
            this.dataSourceId = dataSourceId;
            this.dOI = null;
        }

        protected ExperimentTO(T id, String name, String description, Integer dataSourceId, String dOI) {
            super(id, name, description);
            this.dataSourceId = dataSourceId;
            this.dOI = dOI;
        }

        @Override
        public Integer getDataSourceId() {
            return dataSourceId;
        }
        public String getDOI() {
            return dOI;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ExperimentTO [id=").append(getId()).append(", name=").append(getName())
                    .append(", description=").append(getDescription()).append(", dataSourceId=").append(dataSourceId)
                    .append(", dOI=").append(dOI).append("]");
            return builder.toString();
        }
    }
}
