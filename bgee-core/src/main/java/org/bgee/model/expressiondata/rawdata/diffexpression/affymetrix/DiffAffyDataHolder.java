package org.bgee.model.expressiondata.rawdata.diffexpression.affymetrix;

import org.bgee.model.expressiondata.rawdata.RawDataHolder;

/**
 * This class can hold any Affymetrix data, and count of data, used for differentiel 
 * expression analyses. This {@code DiffAffyDataHolder} should likely be itself 
 * hold by a {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder}.
 * <p>
 * To hold generic Affymetrix data, see 
 * {@link org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffAffyDataHolder implements RawDataHolder {

    @Override
    public boolean hasData() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasDataCount() {
        // TODO Auto-generated method stub
        return false;
    }

}
