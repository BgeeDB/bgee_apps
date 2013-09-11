package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder;
import org.bgee.model.expressiondata.rawdata.est.ESTDataHolder;
import org.bgee.model.expressiondata.rawdata.insitu.InSituDataHolder;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder;

/**
 * This class can hold any raw data used in Bgee to generate data calls
 * (see {@link org.bgee.model.expressiondata.DataParameters.CallType} for a list 
 * of data calls available). Basically, it can hold all other <code>RawDataHolder</code>s 
 * specific to a data type (for instance, an <code>AffymetrixDataHolder</code>).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnyRawDataHolder implements GlobalRawDataHolder {
	
	/**
	 * <code>ESTDataHolder</code> to hold EST-related data.
	 */
	private ESTDataHolder estDataHolder;
	/**
	 * <code>ESTDataHolder</code> to hold Affymetrix-related data.
	 */
	private AffymetrixDataHolder affyDataHolder;
	/**
	 * <code>ESTDataHolder</code> to hold <em>in situ</em>-related data.
	 */
	private InSituDataHolder inSituDataHolder;
	/**
	 * <code>RNASeqDataHolder</code> to hold RNA-Seq-related data.
	 */
	private RNASeqDataHolder rnaSeqDataHolder;
	
	
	@Override
	public boolean hasData() {
		if (this.getESTDataHolder().hasData() || 
				this.getAffymetrixDataHolder().hasData() || 
				this.getInSituDataHolder().hasData() || 
				this.getRNASeqDataHolder().hasData()) {
			return true;
		}
		return false;
	}
	@Override
	public boolean hasDataCount() {
		if (this.getESTDataHolder().hasDataCount() || 
				this.getAffymetrixDataHolder().hasDataCount() || 
				this.getInSituDataHolder().hasDataCount() || 
				this.getRNASeqDataHolder().hasDataCount()) {
			return true;
		}
		return false;
	}
	
	@Override
	public ESTDataHolder getESTDataHolder() {
		return estDataHolder;
	}
	@Override
	public void setESTDataHolder(ESTDataHolder estDataHolder) {
		this.estDataHolder = estDataHolder;
	}
	@Override
	public AffymetrixDataHolder getAffymetrixDataHolder() {
		return affyDataHolder;
	}
	@Override
	public void setAffymetrixDataHolder(AffymetrixDataHolder affyDataHolder) {
		this.affyDataHolder = affyDataHolder;
	}
	@Override
	public InSituDataHolder getInSituDataHolder() {
		return inSituDataHolder;
	}
	@Override
	public void setInSituDataHolder(InSituDataHolder inSituDataHolder) {
		this.inSituDataHolder = inSituDataHolder;
	}
	@Override
	public RNASeqDataHolder getRNASeqDataHolder() {
		return rnaSeqDataHolder;
	}
	@Override
	public void setRNASeqDataHolder(RNASeqDataHolder rnaSeqDataHolder) {
		this.rnaSeqDataHolder = rnaSeqDataHolder;
	}
}
