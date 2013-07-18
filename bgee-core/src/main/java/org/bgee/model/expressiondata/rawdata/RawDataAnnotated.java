package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.anatdev.AnatomicalEntity;
import org.bgee.model.anatdev.Stage;


/**
 * Parent class of all classes related to expression data, and mapped to ontologies 
 * (see 'see also' section). 
 * Indeed, expression data are mapped to anatomical and developmental ontologies, 
 * they are then mapped to an <code>Organ</code> and to a <code>Stage</code>. 
 * This class is the parent of the classes that hold this mapping, 
 * and is intended to hold methods and attributes related to this mapping.  
 * <p>
 * Note that <code>InSituSpot</code>s are mapped to ontologies, but as they are also 
 * mapped to genes, this class is the subclass of <code>CallSourceRawData</code>.
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see model.expressionData.estData.EstLibrary
 * @see model.expressionData.inSituHybridizationData.InSituSpot
 * @see model.expressionData.affymetrixData.AffymetrixChip
 * @see model.expressionData.rnaSeqData.RnaSeqLibrary
 * @since Bgee 09
 */
public class RawDataAnnotated extends RawData
{

	private AnatomicalEntity organ;
	private Stage stage;
	
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
	 * Set the <code>organ</code> attribute.
	 * @param org 	the <code>Organ</code> to set.
	 * @see #organ
	 */
	public void setOrgan(AnatomicalEntity org)
	{
		this.organ = org;
	}
	/**
	 * Get the <code>organ</code> attribute.
	 * @return 	the <code>Organ</code> stored in the <code>organ</code> attribute.
	 * @see #organ
	 */
	public AnatomicalEntity getOrgan()
	{
		return this.organ;
	}
	/**
	 * Set the <code>stage</code> attribute.
	 * @param sta 	the <code>Stage</code> to set.
	 * @see #stage
	 */
	public void setStage(Stage sta)
	{
		this.stage = sta;
	}
	/**
	 * Get the <code>stage</code> attribute.
	 * @return 	the <code>Stage</code> stored in the <code>stage</code> attribute.
	 * @see #stage
	 */
	public Stage getStage()
	{
		return this.stage;
	}
}
