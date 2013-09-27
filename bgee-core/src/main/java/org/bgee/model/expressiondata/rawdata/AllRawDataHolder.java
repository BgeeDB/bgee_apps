package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder;
import org.bgee.model.expressiondata.rawdata.est.ESTDataHolder;
import org.bgee.model.expressiondata.rawdata.insitu.InSituDataHolder;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder;

/**
 * This class can hold any raw data used in Bgee to generate data calls
 * (see {@link org.bgee.model.expressiondata.DataParameters.CallType} for a list 
 * of data calls available). Basically, it can hold all other {@code RawDataHolder}s 
 * specific to a data type (for instance, an {@code AffymetrixDataHolder}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AllRawDataHolder implements RawDataHolder {
	/**
	 * {@code ESTDataHolder} to hold EST-related data.
	 */
	private ESTDataHolder estDataHolder;
	/**
	 * {@code ESTDataHolder} to hold Affymetrix-related data.
	 */
	private AffymetrixDataHolder affyDataHolder;
	/**
	 * {@code ESTDataHolder} to hold <em>in situ</em>-related data.
	 */
	private InSituDataHolder inSituDataHolder;
	/**
	 * {@code RNASeqDataHolder} to hold RNA-Seq-related data.
	 */
	private RNASeqDataHolder rnaSeqDataHolder;
    
	/**
	 * Default constructor.
	 */
    public AllRawDataHolder() {
        this.setAffymetrixDataHolder(new AffymetrixDataHolder());
        this.setESTDataHolder(new ESTDataHolder());
        this.setInSituDataHolder(new InSituDataHolder());
        this.setRNASeqDataHolder(new RNASeqDataHolder());
    }
	
	
	@Override
	public boolean hasData() {
		if ((this.getESTDataHolder() != null && this.getESTDataHolder().hasData()) || 
		    (this.getAffymetrixDataHolder() != null && 
		        this.getAffymetrixDataHolder().hasData()) || 
		    (this.getInSituDataHolder() != null && this.getInSituDataHolder().hasData()) || 
		    (this.getRNASeqDataHolder() != null && this.getRNASeqDataHolder().hasData())) {
			return true;
		}
		return false;
	}
	@Override
	public boolean hasDataCount() {
	    if ((this.getESTDataHolder() != null && 
	                this.getESTDataHolder().hasDataCount()) || 
	            (this.getAffymetrixDataHolder() != null && 
	                this.getAffymetrixDataHolder().hasDataCount()) || 
	            (this.getInSituDataHolder() != null && 
	                this.getInSituDataHolder().hasDataCount()) || 
	            (this.getRNASeqDataHolder() != null && 
	                this.getRNASeqDataHolder().hasDataCount())) {
	            return true;
	        }
	        return false;
	}
	/**
     * @return the {@code RawDataHolder} related to EST data.
     */
	public ESTDataHolder getESTDataHolder() {
		return estDataHolder;
	}
	/**
     * @param holder    the {@code RawDataHolder} related to EST data to set.
     */
	public void setESTDataHolder(ESTDataHolder estDataHolder) {
		this.estDataHolder = estDataHolder;
	}
	/**
     * @return  the {@code RawDataHolder} related to Affymetrix data.
     */
	public AffymetrixDataHolder getAffymetrixDataHolder() {
		return affyDataHolder;
	}
	/**
     * @param holder    the {@code RawDataHolder} related to Affymetrix data to set.
     */
	public void setAffymetrixDataHolder(AffymetrixDataHolder affyDataHolder) {
		this.affyDataHolder = affyDataHolder;
	}
	/**
     * @return  the {@code RawDataHolder} related to <em>in situ</code> data.
     */
	public InSituDataHolder getInSituDataHolder() {
		return inSituDataHolder;
	}
	/**
     * @param holder    the {@code RawDataHolder} related to <em>in situ</em> data to set.
     */
	public void setInSituDataHolder(InSituDataHolder inSituDataHolder) {
		this.inSituDataHolder = inSituDataHolder;
	}
	/**
     * @return  the {@code RawDataHolder} related to <em>RNA-Seq</code> data.
     */
	public RNASeqDataHolder getRNASeqDataHolder() {
		return rnaSeqDataHolder;
	}
	/**
     * @param holder    the {@code RawDataHolder} related to RNA-Seq data to set.
     */
	public void setRNASeqDataHolder(RNASeqDataHolder rnaSeqDataHolder) {
		this.rnaSeqDataHolder = rnaSeqDataHolder;
	}
}
