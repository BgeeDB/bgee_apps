package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.NamedEntity;

/**
 * 
 * @author Frederic Bastian
 *
 * @param <T>   The type of ID of this {@code Experiment}
 */
public class Experiment<T extends Comparable<T>> extends NamedEntity<T> {

    public Experiment(T id, String name, String description) {
        super(id, name, description);
    }

    //hashCode/equals based on the ID, using hashCode/equals methods of Entity class
}
