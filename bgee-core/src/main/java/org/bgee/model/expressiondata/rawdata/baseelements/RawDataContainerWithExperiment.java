package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link RawDataContainer} for data types having {@code Experiment}s.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 * @see RawDataContainer
 *
 * @param <T>   The type of {@code Experiment} contained.
 * @param <U>   The type of {@code AssayPartOfExp} contained.
 * @param <V>   The type of {@code RawCallSource} contained.
 */
public abstract class RawDataContainerWithExperiment<T extends Experiment<?>,
U extends AssayPartOfExp<T>, V extends RawCallSource<U>>
extends RawDataContainer<U, V> {

    private final Set<T> experiments;

    protected RawDataContainerWithExperiment(Collection<T> experiments, Collection<U> assays,
            Collection<V> calls) {
        this(experiments, assays, calls, false);
    }
    protected RawDataContainerWithExperiment(Collection<T> experiments, Collection<U> assays,
            Collection<V> calls, boolean resultAlreadyFound) {
        super(assays, calls,
                resultAlreadyFound || experiments != null && !experiments.isEmpty());
        this.experiments = experiments == null? null: Collections.unmodifiableSet(
                new LinkedHashSet<>(experiments));
    }

    /**
     * @return  A {@code Set} of {@code Experiment}s that were requested.
     *          If {@code null}, it means that this information was not requested.
     *          If empty, it means that there was no result based on query parameters.
     *          When non-null, the underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<T> getExperiments() {
        return experiments;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(experiments);
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
        RawDataContainerWithExperiment<?, ?, ?> other =
                (RawDataContainerWithExperiment<?, ?, ?>) obj;
        return Objects.equals(experiments, other.experiments);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataContainerWithExperiment [")
               .append("experiments=").append(experiments)
               .append(", getAssays()=").append(getAssays())
               .append(", getCalls()=").append(getCalls())
               .append(", isResultFound()=").append(isResultFound())
               .append("]");
        return builder.toString();
    }
}