package org.bgee.model.expressiondata.rawdata.est;

import org.bgee.model.expressiondata.rawdata.RawDataHolder;

/**
 * This class can hold any EST-related data, and count of data, used in Bgee. 
 * This {@code ESTDataHolder} should likely be itself hold by a 
 * {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ESTDataHolder implements RawDataHolder {

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
