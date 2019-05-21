package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.NamedEntity;
import org.bgee.model.source.Source;

/**
 *
 * @author Frederic Bastian
 * @version Bgee 14
 * @since Bgee 14
 *
 * @param <T>   The type of ID of this {@code Experiment}
 */
public class Experiment<T extends Comparable<T>> extends NamedEntity<T> implements RawDataWithDataSource {

    private final Source dataSource;

    public Experiment(T id, String name, String description, Source dataSource) {
        super(id, name, description);
        this.dataSource = dataSource;
    }

    public Source getDataSource() {
        return this.dataSource;
    }

    //hashCode/equals based on the ID, using hashCode/equals methods of Entity class

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Experiment [id=").append(getId()).append(", name=").append(getName())
                .append(", description=").append(getDescription()).append(", dataSource=").append(this.dataSource)
                .append("]");
        return builder.toString();
    }
}
