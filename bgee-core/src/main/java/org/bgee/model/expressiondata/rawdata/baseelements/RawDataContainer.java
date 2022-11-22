package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Base {@link DataContainer} of raw data results.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 *
 * @param <T>   The type of {@code Assay} contained.
 * @param <U>   The type of {@code RawCallSource} contained.
 */
public abstract class RawDataContainer<T extends Assay<?>, U extends RawCallSource<T>>
extends DataContainerTemp {

    private final Set<T> assays;
    private final Set<U> calls;

    protected RawDataContainer(Collection<T> assays, Collection<U> calls) {
        this(assays, calls, false);
    }
    protected RawDataContainer(Collection<T> assays, Collection<U> calls, boolean resultAlreadyFound) {
        super(resultAlreadyFound || assays != null && !assays.isEmpty() ||
                calls != null && !calls.isEmpty());
        this.assays = assays == null? null: Collections.unmodifiableSet(new LinkedHashSet<>(assays));
        this.calls = calls == null? null: Collections.unmodifiableSet(new LinkedHashSet<>(calls));
    }

    /**
     * @return  A {@code Set} of {@code Assay}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<T> getAssays() {
        return assays;
    }
    /**
     * @return  A {@code Set} of {@code RawCallSource}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<U> getCalls() {
        return calls;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(assays, calls);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataContainer<?, ?> other = (RawDataContainer<?, ?>) obj;
        return Objects.equals(assays, other.assays) && Objects.equals(calls, other.calls);
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataContainer [")
               .append("assays=").append(assays)
               .append(", calls=").append(calls)
               .append(", isResultFound()=").append(isResultFound())
               .append("]");
        return builder.toString();
    }
}