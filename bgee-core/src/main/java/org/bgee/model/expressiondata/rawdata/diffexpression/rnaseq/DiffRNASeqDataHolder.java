package org.bgee.model.expressiondata.rawdata.diffexpression.rnaseq;

import org.bgee.model.expressiondata.rawdata.RawDataHolder;

/**
 * This class can hold any RNA-Seq data, and count of data, used for differentiel 
 * expression analyses. This {@code DiffRNASeqDataHolder} should likely be itself 
 * hold by a {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder}.
 * <p>
 * To hold generic RNA-Seq data, see 
 * {@link org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class DiffRNASeqDataHolder implements RawDataHolder {

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
