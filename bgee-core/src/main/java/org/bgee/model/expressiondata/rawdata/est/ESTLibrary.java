package org.bgee.model.expressiondata.rawdata.est;

import org.bgee.model.NamedEntity;
import org.bgee.model.expressiondata.rawdata.Assay;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotated;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotation;
import org.bgee.model.expressiondata.rawdata.RawDataWithDataSource;
import org.bgee.model.source.Source;

public class ESTLibrary extends NamedEntity<String> implements Assay<String>, RawDataWithDataSource, 
        RawDataAnnotated{

    public ESTLibrary(String id, String name, String description, RawDataAnnotation annotation, Source dataSource
            ) throws IllegalArgumentException {
        super(id, name, description);
        this.annotation = annotation;
        this.dataSource = dataSource;
    }

    private final RawDataAnnotation annotation;
    private final Source dataSource;

    
    @Override
    public RawDataAnnotation getAnnotation() {
        return this.annotation;
    }

    @Override
    public Source getDataSource() {
        return this.dataSource;
    }

}
