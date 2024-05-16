package org.bgee.model.expressiondata.rawdata.baseelements;

import org.bgee.model.NamedEntity;
import org.bgee.model.XRef;
import org.bgee.model.source.Source;

/**
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 14
 *
 * @param <T>   The type of ID of this {@code Experiment}
 */
public abstract class Experiment<T extends Comparable<T>> extends NamedEntity<T> implements RawDataWithDataSource {

    private final Source dataSource;
    private final XRef xRef;
    private final int assayCount;
    private final String dOI;

    protected Experiment(T id, String name, String description, String dOI, Source dataSource,
            int assayCount) {
        this(id, id, name, description, dOI, dataSource, assayCount);
    }
    protected Experiment(T id, T xRefId, String name, String description, String dOI, Source dataSource,
            int assayCount) {
        super(id, name, description);
        this.dataSource = dataSource;
        this.dOI = dOI;
        if (dataSource != null) {
            this.xRef = new XRef(xRefId.toString(), name, dataSource, dataSource.getExperimentUrl());
        } else {
            this.xRef = null;
        }
        this.assayCount = assayCount;
    }

    public Source getDataSource() {
        return this.dataSource;
    }
    public int getAssayCount() {
        return assayCount;
    }
    @Override
    public XRef getXRef() {
        return this.xRef;
    }
    public String getdOI() {
        return dOI;
    }
    @Override
    public String toString() {
        return "Experiment [dataSource=" + dataSource + ", xRef=" + xRef + ", assayCount=" + assayCount + ", dOI=" + dOI
                + ", getName()=" + getName() + ", getDescription()=" + getDescription() + ", getId()=" + getId() + "]";
    }

    //hashCode/equals based on the ID, using hashCode/equals methods of Entity class

    

}
