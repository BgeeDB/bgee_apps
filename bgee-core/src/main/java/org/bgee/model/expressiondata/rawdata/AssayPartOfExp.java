package org.bgee.model.expressiondata.rawdata;

/**
 * An {@code Assay} that is part of an {@code Experiment}. All classes implementing {@code AssayPartOfExp}
 * should also extends the class {@code Assay}. See below for explanation about why {@code AssayPartOfExp}
 * is an interface rather than a class.
 * <p>
 * Implementation notes: Some assays are not part of an experiment, for instance, {@code ESTLibrary}s.
 * Also, some assays are annotated (for instance, {@code AffymetrixChip}s), and some assays are not
 * (for instance, {@code InSituEvidence}). This is why we need an interface rather than a class, to be able
 * to adequately type implementing classes.
 *
 * @author Frederic Bastian
 * @version Bgee14 Jul. 2018
 * @since Bgee14 Jul. 2018
 *
 * @param <T>   The type of ID of this {@code AssayPartOfExp}. This is needed to be able to sort them based on their ID.
 * @param <U>   The type of the {@code Experiment} this {@code AssayPartOfExp} is part of.
 */
public interface AssayPartOfExp<T extends Comparable<T>, U extends Experiment<?>> {

    public T getId();
    public U getExperiment();
}
