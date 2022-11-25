package org.bgee.model.expressiondata.rawdata.baseelements;

/**
 * An assay allows to generate {@link RawCallSource}s. When it is part of an {@code Experiment},
 * it is an {@link AssayPartOfExp}.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Jul. 2018
 * @since Bgee 14 Jul. 2018
 *
 */
//Assays do not always have an ID, as for RnaSeqLibraryAnnotatedSample,
//so we removed the getId() method from this interface
public interface Assay {
}
