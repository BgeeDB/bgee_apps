package org.bgee.model.expressiondata.rawdata.est;

import org.bgee.model.NamedEntity;
import org.bgee.model.XRef;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataPipelineSummary;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataWithDataSource;
import org.bgee.model.source.Source;

public class ESTLibrary extends NamedEntity<String> implements Assay, RawDataWithDataSource {

    private final RawDataAnnotation annotation;
    private final Source dataSource;
    private final XRef xRef;
    private final RawDataPipelineSummary pipelineSummary;


    public ESTLibrary(String id, String name, String description, RawDataAnnotation annotation,
            Source dataSource, RawDataPipelineSummary pipelineSummary)
                    throws IllegalArgumentException {
        super(id, name, description);
        this.annotation = annotation;
        this.dataSource = dataSource;
        if (dataSource != null) {
            this.xRef = new XRef(id.toString(), name, dataSource, dataSource.getExperimentUrl());
        } else {
            this.xRef = null;
        }
        this.pipelineSummary = pipelineSummary;
    }

    @Override
    public RawDataAnnotation getAnnotation() {
        return this.annotation;
    }
    @Override
    public Source getDataSource() {
        return this.dataSource;
    }
    @Override
    public XRef getXRef() {
        return this.xRef;
    }

    public RawDataPipelineSummary getPipelineSummary() {
        return pipelineSummary;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ESTLibrary [id=").append(getId())
               .append(", name=").append(getName())
               .append(", description=").append(getDescription())
               .append(", annotation=").append(annotation)
               .append(", dataSource=").append(this.dataSource)
               .append(", xRef=").append(this.xRef)
               .append(", pipelineSummary=").append(this.pipelineSummary)
               .append("]");
        return builder.toString();
    }
}
