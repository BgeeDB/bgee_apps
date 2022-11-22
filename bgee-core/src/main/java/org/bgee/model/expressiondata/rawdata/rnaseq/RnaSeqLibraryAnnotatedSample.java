package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.expressiondata.rawdata.baseelements.AssayPartOfExp;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;

// This object will merge information present in RNASeqLibraryTO and RNASeqLibraryAnnotatedSampleTO
// from the DAO. The ID of RNASeqLibraryAnnotatedSampleTO is internal to Bgee, it is not meant to
// be available in bgee-core. 
public class RnaSeqLibraryAnnotatedSample extends Entity<String>
        implements AssayPartOfExp<String, RnaSeqExperiment>, RawDataAnnotated{
    private final static Logger log = LogManager.getLogger(RnaSeqLibraryAnnotatedSample.class.getName());

    private final RnaSeqExperiment experiment;
    private final RawDataAnnotation annotation;
    private final RnaSeqLibraryPipelineSummary pipelineSummary;
    private final String barcode;
    private final String genotype;

    public RnaSeqLibraryAnnotatedSample (String libraryId, RnaSeqExperiment experiment,
            RawDataAnnotation annotation, RnaSeqLibraryPipelineSummary pipelineSummary,
            String barcode, String genotype) {
        super(libraryId);
        if (StringUtils.isBlank(libraryId)) {
            throw log.throwing(new IllegalArgumentException("library can not be blank"));
        }
        this.experiment = experiment;
        this.annotation = annotation;
        this.pipelineSummary = pipelineSummary;
        this.barcode = barcode;
        this.genotype = genotype;
    }

    @Override
    public RnaSeqExperiment getExperiment() {
        return this.experiment;
    }
    @Override
    public RawDataAnnotation getAnnotation() {
        return this.annotation;
    }
    public RnaSeqLibraryPipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }
    public String getBarcode() {
        return barcode;
    }
    public String getGenotype() {
        return genotype;
    }

    //TODO: check if the hashcode/equals are not outdated. For now we consider an annotated
    // sample library to be unique for one libraryId with same annotated condition and technology
    // (including barcode)
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        RawDataCondition condition = annotation != null? annotation.getRawDataCondition():null;
        result = prime * result + Objects.hash(condition);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RnaSeqLibraryAnnotatedSample other = (RnaSeqLibraryAnnotatedSample) obj;
        RawDataCondition OtherCond = other.getAnnotation() != null? other.getAnnotation()
                .getRawDataCondition():null;
        RawDataCondition condition = annotation != null? annotation.getRawDataCondition():null;
        return Objects.equals(condition, OtherCond);
    }

    @Override
    public String toString() {
        return "RnaSeqLibraryAnnotatedSample [libraryId=" + super.getId() + ", experiment=" + experiment + ", annotation=" + annotation
                + ", pipelineSummary=" + pipelineSummary + ", barcode=" + barcode + ", genotype=" + genotype + "]";
    }

}
