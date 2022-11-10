package org.bgee.model.expressiondata.rawdata;

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
public class Experiment<T extends Comparable<T>> extends NamedEntity<T> implements RawDataWithDataSource {

    private final Source dataSource;
    private final XRef xRef;

    public Experiment(T id, String name, String description, Source dataSource) {
        super(id, name, description);
        this.dataSource = dataSource;
        if (dataSource != null) {
            this.xRef = new XRef(id.toString(), name, dataSource, dataSource.getExperimentUrl());
        } else {
            this.xRef = null;
        }
    }

    public Source getDataSource() {
        return this.dataSource;
    }
    @Override
    public XRef getXRef() {
        return this.xRef;
    }

    //hashCode/equals based on the ID, using hashCode/equals methods of Entity class

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Experiment [id=").append(getId())
               .append(", name=").append(getName())
               .append(", description=").append(getDescription())
               .append(", dataSource=").append(this.dataSource)
               .append(", xRef=").append(this.xRef)
               .append("]");
        return builder.toString();
    }
}
