package org.bgee.model.expressiondata.rawdata.est;

import java.util.Collection;

import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;

/**
 * A {@code RawDataContainer} for EST.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 */
public class ESTContainer extends RawDataContainer<ESTLibrary, EST> {

    public ESTContainer(Collection<ESTLibrary> assays, Collection<EST> calls) {
        super(assays, calls);
    }
}
