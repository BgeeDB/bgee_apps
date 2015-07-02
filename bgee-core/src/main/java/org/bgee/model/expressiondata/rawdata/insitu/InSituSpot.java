package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.CallSource;
import org.bgee.model.expressiondata.rawdata.DataAnnotated;

/**
 * Class related to in situ spots. 
 * Is contained by a {@code InSituEvidence}. 
 * Hold expression data for a gene 
 * (child class of {@code CallSourceRawData}). 
 * In situ spots are also mapped to anatomical and developmental ontologies, 
 * so that this class could also be a subclass of {@code ExpressionDataMappedToOntology}, 
 * but we had to choose. This class then reimplement methods and attributes 
 * of {@code ExpressionDataMappedToOntology}.
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see InSituEvidence
 * @see model.data.expressionData.ExpressionDataMappedToOntology
 * @since Bgee 01
 */
public class InSituSpot extends CallSource implements DataAnnotated
{
    /**
	 * The {@code InSituEvidence} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
    private InSituEvidence inSituEvidence;
	/**
	 * A {@code String} corresponding to the ID 
	 * of the in situ evidence this spot belongs to. 
	 * This attribute is useful when the {@code inSituEvidence} is not set. 
	 * When both are set, this {@code inSituEvidenceId} matches 
	 * the {@code id} attribute of the {@code InSituEvidence}. 
	 * @see microarrayExperiment
	 */
    private String inSituEvidenceId;
    
    private String organId;
	//private Organ organ;
	private String stageId;
	//private DevStage devStage;
	private String detectionFlag;
	
	public InSituSpot()
    {//TODO
    	super(null);
    	this.setDetectionFlag("undefined");
    }
//	
//	/**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code InSituEvidence} container of this {@code InSituSpot}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code InSituEvidence} container of this {@code InSituSpot}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code InSituEvidence} container.
//	 * @see #inSituEvidence
//	 * @see #getDataSource()
//	 */
//	@Override
//	public DataSource getDataSourceFromContainer()
//	{
//		if (this.getInSituEvidence() != null) { 
//			return this.getInSituEvidence().getDataSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code InSituEvidence} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code InSituEvidence} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #inSituEvidence
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getDataSourceIdFromContainer()
//	{
//		if (this.getInSituEvidence() != null) { 
//			return this.getInSituEvidence().getDataSourceId();
//		}
//		return "";
//	}
//
//	/**
//     * Retrieve the {@code InSituEvidence} this {@code InSituSpot} belongs to, 
//     * by using the ID provided by {@code #getInSituEvidenceId()}, 
//     * and store it by using <code>#setInSituEvidence(InSituEvidence)<code>.
//     */
//	public void loadInSituEvidence() 
//	{
//		InSituEvidenceFactory loader = new InSituEvidenceFactory();
//		this.setInSituEvidence(loader.getEvidenceById(this.getInSituEvidenceId()));
//	}
	
	public void setInSituEvidence(InSituEvidence evidence)
	{
		this.inSituEvidence = evidence;
	}
	public InSituEvidence getInSituEvidence()
	{
		return this.inSituEvidence;
	}
	public void setInSituEvidenceId(String evidenceId)
	{
		this.inSituEvidenceId = evidenceId;
	}
//	/**
//	 * Returns either the value of {@code inSituEvidenceId}, 
//	 * or the of the {@code id} of the {@code InSituEvidence} 
//	 * stored in {@code inSituEvidence}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the in situ experiment this evidence belongs to. 
//	 * @see 	#inSituEvidenceId
//	 * @see 	#inSituEvidence
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getInSituEvidenceId()
//	{
//		return this.getIdByEntityOrId(this.getInSituEvidence(), this.inSituEvidenceId);
//	}
//	
//	public void setOrgan(Organ org)
//	{
//		this.organ = org;
//	}
//	public Organ getOrgan()
//	{
//		return this.organ;
//	}
	public void setOrganId(String orgId)
	{
		this.organId = orgId;
	}
//	/**
//	 * Returns either the value of {@code organId}, 
//	 * or the of the {@code id} of the {@code Organ} 
//	 * stored in {@code organ}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the organ for which this spot reports expression. 
//	 * @see 	#organId
//	 * @see 	#organ
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getOrganId()
//	{
//		return this.getIdByEntityOrId(this.getOrgan(), this.organId);
//	}
//	
//	public void setStage(DevStage sta)
//	{
//		this.devStage = sta;
//	}
//	public DevStage getStage()
//	{
//		return this.devStage;
//	}
//	public void setStageId(String staId)
//	{
//		this.stageId = staId;
//	}
//	/**
//	 * Returns either the value of {@code stageId}, 
//	 * or the of the {@code id} of the {@code DevStage} 
//	 * stored in {@code devStage}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the devStage for which this spot reports expression. 
//	 * @see 	#stageId
//	 * @see 	#devStage
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getStageId()
//	{
//		return this.getIdByEntityOrId(this.getStage(), this.stageId);
//	}


	public void setDetectionFlag(String detectFlag) 
	{
		this.detectionFlag = detectFlag;
	}
	public String getDetectionFlag()
	{
		return this.detectionFlag;
	}
	public String getExpressedNotExpressed()
	{
		if (this.detectionFlag.equals("present")) {
			return "expressed";
		}
		return "not expressed";
	}

    @Override
    public void loadSampleAssay() {
        // TODO Auto-generated method stub
        
    }
}
