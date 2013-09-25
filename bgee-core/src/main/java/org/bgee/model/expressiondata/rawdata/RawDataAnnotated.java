package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.anatdev.core.AnatEntity;
import org.bgee.model.anatdev.core.DevStage;


/**
 * Parent class of all classes related to expression data, and mapped to ontologies 
 * (see 'see also' section). 
 * Indeed, expression data are mapped to anatomical and developmental ontologies, 
 * they are then mapped to an {@code Organ} and to a {@code DevStage}. 
 * This class is the parent of the classes that hold this mapping, 
 * and is intended to hold methods and attributes related to this mapping.  
 * <p>
 * Note that {@code InSituSpot}s are mapped to ontologies, but as they are also 
 * mapped to genes, this class is the subclass of {@code CallSourceRawData}.
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see model.expressionData.estData.EstLibrary
 * @see model.expressionData.inSituHybridizationData.InSituSpot
 * @see model.expressionData.affymetrixData.AffymetrixChip
 * @see model.expressionData.rnaSeqData.RnaSeqLibrary
 * @since Bgee 09
 */
@Deprecated
public class RawDataAnnotated extends RawData
{

	private AnatEntity organ;
	private DevStage devStage;
	
	/**
	 * Default constructor. 
	 */
	public RawDataAnnotated(String id)
    {
    	super(id);
    	
    	this.setOrgan(null);
    	this.setStage(null);
    }
	
	/**
	 * Set the {@code organ} attribute.
	 * @param org 	the {@code Organ} to set.
	 * @see #organ
	 */
	public void setOrgan(AnatEntity org)
	{
		this.organ = org;
	}
	/**
	 * Get the {@code organ} attribute.
	 * @return 	the {@code Organ} stored in the {@code organ} attribute.
	 * @see #organ
	 */
	public AnatEntity getOrgan()
	{
		return this.organ;
	}
	/**
	 * Set the {@code devStage} attribute.
	 * @param sta 	the {@code DevStage} to set.
	 * @see #devStage
	 */
	public void setStage(DevStage sta)
	{
		this.devStage = sta;
	}
	/**
	 * Get the {@code devStage} attribute.
	 * @return 	the {@code DevStage} stored in the {@code devStage} attribute.
	 * @see #devStage
	 */
	public DevStage getStage()
	{
		return this.devStage;
	}
}
