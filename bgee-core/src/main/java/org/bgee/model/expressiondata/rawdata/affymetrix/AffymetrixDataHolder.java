package org.bgee.model.expressiondata.rawdata.affymetrix;

import org.bgee.model.expressiondata.rawdata.RawDataHolder;

/**
 * This class can hold any Affymetrix-related data, and count of data, used in Bgee. 
 * This {@code DiffAffyDataHolder} should likely be itself hold by 
 * a {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder}.
 * <p>
 * For AFfymetrix data specifically used to generate differential expression calls, 
 * see {@link org.bgee.model.expressiondata.rawdata.diffexpression.affymetrix.DiffAffyDataHolder}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AffymetrixDataHolder implements RawDataHolder {

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
