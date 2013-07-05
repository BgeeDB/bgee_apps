package org.bgee.model.expressiondata.rawdata;


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

    private String organId;
	private Organ organ;
	private String stageId;
	private Stage stage;
	
	/**
	 * Default constructor. 
	 */
	public RawDataAnnotated()
    {
    	super();
    	
    	this.setOrganId("");
    	this.setOrgan(null);
    	this.setStageId("");
    	this.setStage(null);
    }
	
	/**
	 * Set the <code>organ</code> attribute.
	 * @param org 	the <code>Organ</code> to set.
	 * @see #organ
	 */
	public void setOrgan(Organ org)
	{
		this.organ = org;
	}
	/**
	 * Get the <code>organ</code> attribute.
	 * @return 	the <code>Organ</code> stored in the <code>organ</code> attribute.
	 * @see #organ
	 */
	public Organ getOrgan()
	{
		return this.organ;
	}
	/**
	 * Set the <code>organId</code> attribute.
	 * @param orgId 	the <code>String</code> to set <code>organId</code>.
	 * @see #organId
	 */
	public void setOrganId(String orgId)
	{
		this.organId = orgId;
	}
	/**
	 * Returns either the value of <code>organId</code>, 
	 * or the of the <code>id</code> of the <code>Organ</code> 
	 * stored in <code>organ</code>, depending on which one is set. 
	 * 
	 * @return 	the ID of the organ for which this object reports expression. 
	 * @see 	#organId
	 * @see 	#organ
	 * @see 	#getIdByEntityOrId(Entity, String)
	 */
	public String getOrganId()
	{
		return this.getIdByEntityOrId(this.getOrgan(), this.organId);
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
	/**
	 * Set the <code>stageId</code> attribute.
	 * @param staId 	the <code>String</code> to set <code>stageId</code>.
	 * @see #stageId
	 */
	public void setStageId(String staId)
	{
		this.stageId = staId;
	}
	/**
	 * Returns either the value of <code>stageId</code>, 
	 * or the of the <code>id</code> of the <code>Stage</code> 
	 * stored in <code>stage</code>, depending on which one is set. 
	 * 
	 * @return 	the ID of the stage for which this object reports expression. 
	 * @see 	#stageId
	 * @see 	#stage
	 * @see 	#getIdByEntityOrId(Entity, String)
	 */
	public String getStageId()
	{
		return this.getIdByEntityOrId(this.getStage(), this.stageId);
	}
}
