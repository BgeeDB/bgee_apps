package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixDataHolder;
import org.bgee.model.expressiondata.rawdata.est.ESTDataHolder;
import org.bgee.model.expressiondata.rawdata.insitu.InSituDataHolder;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqDataHolder;

public interface GlobalRawDataHolder extends RawDataHolder {

	/**
	 * @return the {@code RawDataHolder} related to EST data.
	 */
	public ESTDataHolder getESTDataHolder();
	/**
	 * @param holder	the {@code RawDataHolder} related to EST data to set.
	 */
	public void setESTDataHolder(ESTDataHolder holder);
	/**
	 * @return	the {@code RawDataHolder} related to Affymetrix data.
	 */
	public AffymetrixDataHolder getAffymetrixDataHolder();
	/**
	 * @param holder	the {@code RawDataHolder} related to Affymetrix data to set.
	 */
	public void setAffymetrixDataHolder(AffymetrixDataHolder holder);
	/**
	 * @return	the {@code RawDataHolder} related to <em>in situ</code> data.
	 */
	public InSituDataHolder getInSituDataHolder();
	/**
	 * @param holder	the {@code RawDataHolder} related to <em>in situ</em> data to set.
	 */
	public void setInSituDataHolder(InSituDataHolder holder);
	/**
	 * @return	the {@code RawDataHolder} related to <em>RNA-Seq</code> data.
	 */
	public RNASeqDataHolder getRNASeqDataHolder();
	/**
	 * @param holder	the {@code RawDataHolder} related to RNA-Seq data to set.
	 */
	public void setRNASeqDataHolder(RNASeqDataHolder holder);
}
