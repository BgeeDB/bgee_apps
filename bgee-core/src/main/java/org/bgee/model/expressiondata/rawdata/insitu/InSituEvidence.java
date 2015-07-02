package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.RawData;

/**
 * Class related to in situ evidences. 
 * Is contained by an {@code InSituExp} 
 * and is the container of {@code InSituSpot}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see InSituSpot
 * @see InSituExp
 * @since Bgee 01
 */
public class InSituEvidence extends RawData
{
    /**
	 * The {@code InSituExp} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
    private InSituExp inSituExp;
	/**
	 * A {@code String} corresponding to the ID 
	 * of the in situ experiment this evidence belongs to. 
	 * This attribute is useful when the {@code inSituExp} is not set. 
	 * When both are set, this {@code inSituExperimentId} matches 
	 * the {@code id} attribute of the {@code InSituExp}. 
	 * @see inSituExp
	 */
    private String inSituExperimentId;
	
    /**
     * Default constructor. 
     */
	public InSituEvidence()
    {//TODO
    	super(null);
    }
//	
//	/**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code InSituExp} container of this {@code InSituEvidence}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code InSituExp} container of this {@code InSituEvidence}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code InSituExp} container.
//	 * @see #inSituExp
//	 * @see #getDataSource()
//	 */
//	@Override
//	public DataSource getDataSourceFromContainer()
//	{
//		if (this.getInSituExperiment() != null) { 
//			return this.getInSituExperiment().getDataSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code InSituExp} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code InSituExp} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #inSituExp
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getDataSourceIdFromContainer()
//	{
//		if (this.getInSituExperiment() != null) { 
//			return this.getInSituExperiment().getDataSourceId();
//		}
//		return "";
//	}
//
//	/**
//     * Retrieve the {@code InSituExp} this {@code InSituEvidence} belongs to, 
//     * by using the ID provided by {@code #getInSituExperimentId()}, 
//     * and store it by using <code>#setInSituExperiment(InSituExp)<code>.
//     */
//	public void loadInSituExperiment() 
//	{
//		InSituExpFactory loader = new InSituExpFactory();
//		this.setInSituExperiment(loader.getExperimentById(this.getInSituExperimentId()));
//	}
	
	public void setInSituExperiment(InSituExp exp)
	{
		this.inSituExp = exp;
	}
	public InSituExp getInSituExperiment()
	{
		return this.inSituExp;
	}
	public void setInSituExperimentId(String expId)
	{
		this.inSituExperimentId = expId;
	}
//	/**
//	 * Returns either the value of {@code inSituExperimentId}, 
//	 * or the of the {@code id} of the {@code InSituExp} 
//	 * stored in {@code inSituExp}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the in situ experiment this evidence belongs to. 
//	 * @see 	#inSituExperimentId
//	 * @see 	#inSituExp
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getInSituExperimentId()
//	{
//		return this.getIdByEntityOrId(this.getInSituExperiment(), this.inSituExperimentId);
//	}
}
