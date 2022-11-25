package org.bgee.model.expressiondata.rawdata.baseelements;

/**
 * An {@code Assay} that is part of an {@code Experiment}.
 * <p>
 * Implementation notes: Some assays are not part of an experiment, for instance, {@code ESTLibrary}s.
 * Also, some assays are annotated (for instance, {@code AffymetrixChip}s), and some assays are not
 * (for instance, {@code InSituEvidence}). This is why we need an interface rather than a class, to be able
 * to adequately type implementing classes.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Jul. 2018
 * @since Bgee 14 Jul. 2018
 *
 * @param <T>   The type of the {@code Experiment} this {@code AssayPartOfExp} is part of.
 */
public interface AssayPartOfExp<T extends Experiment<?>> extends Assay {
    public T getExperiment();
}
