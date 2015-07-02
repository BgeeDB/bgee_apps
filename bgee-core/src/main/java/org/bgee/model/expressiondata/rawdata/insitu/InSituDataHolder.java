package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.RawDataHolder;

/**
 * This class can hold any <em>in situ</em>-related data, and count of data, used in Bgee.
 * This {@code InSituDataHolder} should likely be itself hold by a 
 * {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InSituDataHolder implements RawDataHolder {

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
