package org.bgee.model.expressiondata.rawdata.baseelements;

/**
 * An interface for classes describing sources of {@code RawCall}s (see 'see also' section).
 * <p>
 * Implementation notes: this is an interface and not an class, because some sources of {@code RawCall}s
 * have a formal ID (for instance, {@code AffymetrixProbeset}), while some other sources do not (for instance,
 * {@code ESTCount}). Sources of {@code RawCall}s with a formal ID need to extend the {@code Entity} class.
 * We could have created a class {@code RawCallSource}, and then a class {@code RawCallSourceWithId} extending
 * {@code RawCallSource}, but then we would need to reimplement the management of the ID, as in the {@code Entity} class.
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
