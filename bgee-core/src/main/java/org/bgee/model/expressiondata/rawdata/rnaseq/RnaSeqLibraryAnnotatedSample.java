package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.AssayPartOfExp;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;

// The ID of RNASeqLibraryAnnotatedSampleTO is internal to Bgee, it is not meant to
// be available in bgee-core, thus this class does not extend Entity.
public class RnaSeqLibraryAnnotatedSample
        implements AssayPartOfExp<RnaSeqExperiment>, RawDataAnnotated {
    private final static Logger log = LogManager.getLogger(RnaSeqLibraryAnnotatedSample.class.getName());

    private final RnaSeqLibrary library;
    private final RawDataAnnotation annotation;
    private final RnaSeqLibraryPipelineSummary pipelineSummary;
    private final String barcode;
    private final String genotype;

    public RnaSeqLibraryAnnotatedSample (RnaSeqLibrary library,
            RawDataAnnotation annotation, RnaSeqLibraryPipelineSummary pipelineSummary,
            String barcode, String genotype) {
        //library and the condition in the annotation are the primary key of a sample
        if (library == null) {
            throw log.throwing(new IllegalArgumentException("library can not be null"));
        }
        if (annotation == null) {
            throw log.throwing(new IllegalArgumentException("annotation can not be null"));
        }
        this.library = library;
        this.annotation = annotation;
        this.pipelineSummary = pipelineSummary;
        this.barcode = barcode;
        this.genotype = genotype;
    }

    public RnaSeqLibrary getLibrary() {
        return this.library;
    }
    @Override
    public RnaSeqExperiment getExperiment() {
        return this.library.getExperiment();
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

    //For now we consider an annotated sample library to be unique for one libraryId
    //with same condition
    @Override
    public int hashCode() {
        return Objects.hash(annotation.getRawDataCondition(), library);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RnaSeqLibraryAnnotatedSample other = (RnaSeqLibraryAnnotatedSample) obj;
        return Objects.equals(annotation.getRawDataCondition(), other.annotation.getRawDataCondition())
               && Objects.equals(library, other.library);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RnaSeqLibraryAnnotatedSample [")
               .append("library=").append(library)
               .append(", annotation=").append(annotation)
               .append(", pipelineSummary=").append(pipelineSummary)
               .append(", barcode=").append(barcode)
               .append(", genotype=").append(genotype)
               .append("]");
        return builder.toString();
    }
}