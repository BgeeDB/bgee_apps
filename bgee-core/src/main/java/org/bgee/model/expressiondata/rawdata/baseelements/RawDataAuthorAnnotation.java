package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Objects;

/**
 * Free text annotation provided by the authors and used by Bgee annotators to
 * manually annotate raw data conditions.
 *
 * @author Julien Wollbrett
 * @version Bgee 15.2 Mar. 2024
 * @since Bgee 15.2  Mar. 2024
 */
public class RawDataAuthorAnnotation {
    //private final static Logger log = LogManager.getLogger(RawDataAuthorAnnotation.class.getName());
    private final String anatEntityAuthorAnnotation;
    private final String stageAuthorAnnotation;
    private final String cellTypeAuthorAnnotation;
    private final Float time;
    private final String timeUnit;
    
    public RawDataAuthorAnnotation(String anatEntityAuthorAnnotation,
            String cellTypeAuthorAnnotation, String stageAuthorAnnotation,
            Float time, String timeUnit) {
        this.anatEntityAuthorAnnotation = anatEntityAuthorAnnotation;
        this.cellTypeAuthorAnnotation = cellTypeAuthorAnnotation;
        this.stageAuthorAnnotation = stageAuthorAnnotation;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public String getAnatEntityAuthorAnnotation() {
        return anatEntityAuthorAnnotation;
    }
    public String getStageAuthorAnnotation() {
        return stageAuthorAnnotation;
    }
    public String getCellTypeAuthorAnnotation() {
        return cellTypeAuthorAnnotation;
    }
    public Float getTime() {
        return time;
    }
    public String getTimeUnit() {
        return timeUnit;
    }


    @Override
    public int hashCode() {
        return Objects.hash(anatEntityAuthorAnnotation, cellTypeAuthorAnnotation, stageAuthorAnnotation,
                time, timeUnit);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataAuthorAnnotation other = (RawDataAuthorAnnotation) obj;
        return Objects.equals(anatEntityAuthorAnnotation, other.anatEntityAuthorAnnotation)
                && Objects.equals(cellTypeAuthorAnnotation, other.cellTypeAuthorAnnotation)
                && Objects.equals(stageAuthorAnnotation, other.stageAuthorAnnotation)
                && Objects.equals(time, other.time) && Objects.equals(timeUnit, other.timeUnit);
    }

    @Override
    public String toString() {
        return "RawDataAuthorAnnotation [anatEntityAuthorAnnotation=" + anatEntityAuthorAnnotation
                + ", stageAuthorAnnotation=" + stageAuthorAnnotation + ", cellTypeAuthorAnnotation="
                + cellTypeAuthorAnnotation + ", time=" + time + ", timeUnit=" + timeUnit + "]";
    }

}
