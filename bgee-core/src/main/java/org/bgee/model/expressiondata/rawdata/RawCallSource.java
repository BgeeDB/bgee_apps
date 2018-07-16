package org.bgee.model.expressiondata.rawdata;

/**
 * An interface for classes describing sources of {@code RawCall}s (see 'see also' section).
 * <p>
 * Implementation notes: this is an interface and not an class, because some sources of {@code RawCall}s
 * have a formal ID (for instance, {@code AffymetrixProbeset}), while some other sources do not (for instance,
 * {@code ESTCount}). Sources of {@code RawCall}s with a formal ID need to extend the {@code Entity} class.
 * We could have created a class {@code RawCallSource}, and then a class {@code RawCallSourceWithId} extending
 * {@code RawCallSource}, but then we would need to reimplement the management of the ID, as in the {@code Entity} class.
 * <p>
 * Implementation notes: because some sources of {@code RawCall}s have a formal ID and others do not, this is also why
 * we request this interface to extends {@code Comparable}: classes with a formal ID will allow sorting based on it;
 * classes with no formal ID will provide a different mechanism. This is needed to be able to sort the {@code RawCallSource}s
 * at instantiation of the container {@code Assay}.
 *
 * @author Frederic Bastian
 * @since Bgee 14 Jul. 2018
 * @version Bgee 14 Jul. 2018
 *
 * @param <T>   The type of {@code Assay} containing this {@code RawCallSource}
 */
public interface RawCallSource<T extends Assay<?>> {

    public T getAssay();
    public RawCall getRawCall();
}
