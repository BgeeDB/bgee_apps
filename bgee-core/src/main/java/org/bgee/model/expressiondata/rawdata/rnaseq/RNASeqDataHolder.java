package org.bgee.model.expressiondata.rawdata.rnaseq;

import org.bgee.model.expressiondata.rawdata.RawDataHolder;

/**
 * This class can hold any RNA-Seq-related data, and count of data, used in Bgee. 
 * This {@code RNASeqDataHolder} should likely be itself hold by a 
 * {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder}.
 * <p>
 * For RNA-Seq data specifically used to generate differential expression calls, 
 * see {@link org.bgee.model.expressiondata.rawdata.diffexpression.rnaseq.DiffRNASeqDataHolder}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class RNASeqDataHolder implements RawDataHolder {

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
