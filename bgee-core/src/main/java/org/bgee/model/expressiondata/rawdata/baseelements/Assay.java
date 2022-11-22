package org.bgee.model.expressiondata.rawdata.baseelements;

/**
 * An assay allows to generate {@link RawCallSource}s. When it is part of an {@code Experiment},
 * it is an {@link AssayPartOfExp}.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Jul. 2018
 * @since Bgee 14 Jul. 2018
 *
 * @param <T>   The type of ID of this assay.
 */
public interface Assay<T extends Comparable<T>> {
    public T getId();
}
