package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.source.Source;

/**
 * Annotation to a raw {@code Condition} (as opposed to a global {@code Condition},
 * for instance aggregating all raw conditions related to a given anatomical entity).
 * {@code RawDataAnnotation}s also provide information about the annotation itself
 * (about for instance, the curator creating the annotation, or the date of creation).
 *
 * @author Frederic Bastian
 * @version Bgee 14 Jul. 2018
 * @since Bgee 14 Jul. 2018
 */
public class RawDataAnnotation {
    private final static Logger log = LogManager.getLogger(RawDataAnnotation.class.getName());
    private final RawDataCondition rawDataCondition;
    private final RawDataAuthorAnnotation authorAnnotation;
    //XXX Did not create physiologicalStatus in RawDataCondition because it is not a condition
    //    parameter
    private final String physiologicalStatus;
    private final String curator;
    private final Source annotationSource;
    private final String annotationDate;

    public RawDataAnnotation(RawDataCondition rawDataCondition, RawDataAuthorAnnotation authorAnnotation,
            String physiologicalStatus, String curator, Source annotationSource, String annotationDate) {
        if (rawDataCondition == null) {
            throw log.throwing(new IllegalArgumentException("A condition must be provided"));
        }
        this.rawDataCondition = rawDataCondition;
        this.physiologicalStatus = physiologicalStatus;
        this.curator = curator;
        this.annotationSource = annotationSource;
        this.annotationDate = annotationDate;
        //author annotation can be null
        this.authorAnnotation = authorAnnotation;
    }

    public RawDataCondition getRawDataCondition() {
        return rawDataCondition;
    }
    public String getPhysiologicalStatus() {
        return physiologicalStatus;
    }
    public String getCurator() {
        return curator;
    }

    public Source getAnnotationSource() {
        return annotationSource;
    }

    public String getAnnotationDate() {
        return annotationDate;
    }

    public RawDataAuthorAnnotation getAuthorAnnotation() {
        return authorAnnotation;
    }

    

    @Override
    public int hashCode() {
        return Objects.hash(annotationDate, annotationSource, authorAnnotation, curator, physiologicalStatus,
                rawDataCondition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataAnnotation other = (RawDataAnnotation) obj;
        return Objects.equals(annotationDate, other.annotationDate)
                && Objects.equals(annotationSource, other.annotationSource)
                && Objects.equals(authorAnnotation, other.authorAnnotation) && Objects.equals(curator, other.curator)
                && Objects.equals(physiologicalStatus, other.physiologicalStatus)
                && Objects.equals(rawDataCondition, other.rawDataCondition);
    }

    @Override
    public String toString() {
        return "RawDataAnnotation [rawDataCondition=" + rawDataCondition + ", authorAnnotation=" + authorAnnotation
                + ", physiologicalStatus=" + physiologicalStatus + ", curator=" + curator + ", annotationSource="
                + annotationSource + ", annotationDate=" + annotationDate + "]";
    }
    
}
