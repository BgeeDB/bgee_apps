package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.XRef;
import org.bgee.model.source.Source;

/**
 * Raw data having information of the data source, as for instance, {@code AffymetrixExperiment}s and {@code ESTLibrary}s.
 *
 * @author Frederic Bastian
 * @version Bgee 14
 * @since Bgee 14
 */
public interface RawDataWithDataSource {
    public Source getDataSource();
    public XRef getXRef();
}
