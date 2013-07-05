package org.bgee.model.expressiondata.rawdata.insitu;

import org.bgee.model.expressiondata.rawdata.RawData;

/**
 * Class related to in situ evidences. 
 * Is contained by an <code>InSituExp</code> 
 * and is the container of <code>InSituSpot</code>s. 
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
	 * The <code>InSituExp</code> this object belongs to.
	 * It is the "container" used for the methods 
	 * <code>#getDataSourceFromContainer()</code> and <code>#getDataSourceIdFromContainer()</code>.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
    private InSituExp inSituExp;
	/**
	 * A <code>String</code> corresponding to the ID 
	 * of the in situ experiment this evidence belongs to. 
	 * This attribute is useful when the <code>inSituExp</code> is not set. 
	 * When both are set, this <code>inSituExperimentId</code> matches 
	 * the <code>id</code> attribute of the <code>InSituExp</code>. 
	 * @see inSituExp
	 */
    private String inSituExperimentId;
	
    /**
     * Default constructor. 
     */
	public InSituEvidence()
    {
    	super();
    }
	
	/**
	 * Try to obtain the <code>DataSource</code> object where these expression data come from, 
	 * from the <code>InSituExp</code> container of this <code>InSituEvidence</code>, 
	 * and eventually from its own container, etc.
	 * See <code>getDataSource()</code> for more details.
	 * 
	 * @return 	a <code>DataSource</code> object where these expression data come from, 
	 * 			obtained from the <code>InSituExp</code> container of this <code>InSituEvidence</code>. 
	 * 			<code>null</code> if it was not possible to retrieve a <code>DataSource</code> object  
	 * 			from the <code>InSituExp</code> container.
	 * @see #inSituExp
	 * @see #getDataSource()
	 */
	@Override
	public DataSource getDataSourceFromContainer()
	{
		if (this.getInSituExperiment() != null) { 
			return this.getInSituExperiment().getDataSource();
		}
		return null;
	}
	
	/**
	 * Try to obtain the ID of the data source where these expression data come from, 
	 * from the <code>InSituExp</code> "container". 
	 * 
	 * @return 	a <code>String</code> corresponding to the ID of the data source 
	 * 			where these expression data come from, 
	 * 			obtained from the <code>InSituExp</code> "container". 
	 * 			Empty <code>String</code> if it was not possible to retrieve the ID 
	 * 			from the container.
	 * @see #inSituExp
	 * @see #getDataSourceId()
	 */
	@Override
	public String getDataSourceIdFromContainer()
	{
		if (this.getInSituExperiment() != null) { 
			return this.getInSituExperiment().getDataSourceId();
		}
		return "";
	}

	/**
     * Retrieve the <code>InSituExp</code> this <code>InSituEvidence</code> belongs to, 
     * by using the ID provided by <code>#getInSituExperimentId()</code>, 
     * and store it by using <code>#setInSituExperiment(InSituExp)<code>.
     */
	public void loadInSituExperiment() 
	{
		InSituExpFactory loader = new InSituExpFactory();
		this.setInSituExperiment(loader.getExperimentById(this.getInSituExperimentId()));
	}
	
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
	/**
	 * Returns either the value of <code>inSituExperimentId</code>, 
	 * or the of the <code>id</code> of the <code>InSituExp</code> 
	 * stored in <code>inSituExp</code>, depending on which one is set. 
	 * 
	 * @return 	the ID of the in situ experiment this evidence belongs to. 
	 * @see 	#inSituExperimentId
	 * @see 	#inSituExp
	 * @see 	#getIdByEntityOrId(Entity, String)
	 */
	public String getInSituExperimentId()
	{
		return this.getIdByEntityOrId(this.getInSituExperiment(), this.inSituExperimentId);
	}
}
