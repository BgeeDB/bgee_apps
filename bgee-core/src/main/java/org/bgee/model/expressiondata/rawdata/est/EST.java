package org.bgee.model.expressiondata.rawdata.est;

//import model.dataSource.DataSource;
import org.bgee.model.expressiondata.rawdata.CallSourceRawData;

/**
 * Class related to expressed sequence tags. 
 * Is contained by a {@code ESTLibrary}. 
 * Hold expression data for a gene 
 * (child class of {@code CallSourceRawData}). 
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see ESTLibrary
 * @since Bgee 01
 */
public class EST extends CallSourceRawData
{
	/**
	 * The {@code ESTLibrary} this object belongs to.
	 * It is the "container" used for the methods 
	 * {@code #getDataSourceFromContainer()} and {@code #getDataSourceIdFromContainer()}.
	 * @see #getDataSourceFromContainer()
	 * @see #getDataSourceIdFromContainer()
	 */
	private ESTLibrary eSTLibrary;
	/**
	 * A {@code String} corresponding to the ID 
	 * of the EST library this EST belongs to. 
	 * This attribute is useful when the {@code eSTLibrary} is not set. 
	 * When both are set, this {@code estLibraryId} matches 
	 * the {@code id} attribute of the {@code ESTLibrary}. 
	 * @see microarrayExperiment
	 */
	private String estLibraryId;
	/**
	 * EST have two IDs in some libraries. This is the second one.
	 */
	private String id2;

	
	/**
     * Default constructor.
     */
	public EST()
	{
		super();
		
		this.setId2(null);
		
		this.setEstLibraryId(null);
		this.setEstLibrary(null);
	}
//	
//	/**
//	 * Try to obtain the {@code DataSource} object where these expression data come from, 
//	 * from the {@code ESTLibrary} container of this {@code EST}, 
//	 * and eventually from its own container, etc.
//	 * See {@code getDataSource()} for more details.
//	 * 
//	 * @return 	a {@code DataSource} object where these expression data come from, 
//	 * 			obtained from the {@code ESTLibrary} container of this {@code EST}. 
//	 * 			{@code null} if it was not possible to retrieve a {@code DataSource} object  
//	 * 			from the {@code ESTLibrary} container.
//	 * @see #eSTLibrary
//	 * @see #getDataSource()
//	 */
//	@Override
//	public DataSource getDataSourceFromContainer()
//	{
//		if (this.getEstLibrary() != null) { 
//			return this.getEstLibrary().getDataSource();
//		}
//		return null;
//	}
//	
//	/**
//	 * Try to obtain the ID of the data source where these expression data come from, 
//	 * from the {@code AffymetrixChip} "container". 
//	 * 
//	 * @return 	a {@code String} corresponding to the ID of the data source 
//	 * 			where these expression data come from, 
//	 * 			obtained from the {@code AffymetrixChip} "container". 
//	 * 			Empty {@code String} if it was not possible to retrieve the ID 
//	 * 			from the container.
//	 * @see #eSTLibrary
//	 * @see #getDataSourceId()
//	 */
//	@Override
//	public String getDataSourceIdFromContainer()
//	{
//		if (this.getEstLibrary() != null) { 
//			return this.getEstLibrary().getDataSourceId();
//		}
//		return "";
//	}
//
//	public void loadEstLibrary() 
//	{
//		ESTLibraryFactory loader = new ESTLibraryFactory();
//		this.setEstLibrary(loader.getEstLibraryById(this.getEstLibraryId()));
//	}

//	 ***************************************************
//	  GETTERS AND SETTERS
//	 ***************************************************
	/**
	 * Set the second ID of this EST.
	 * 
	 * @param id 		the String to set the second ID of this EST.
	 */
	public void setId2(String id)
	{
		this.id2 = id;
	}
	/**
	 * Get the second ID of this EST.
	 * 
	 * @return id2		the {@code String} corresponding to the second ID of this EST.
	 */
	public String getId2()
	{
		return this.id2;
	}
    //---------------------------------
	/**
	 * Define the {@code ESTLibrary} that contains this EST.
	 * 
	 * @param eSTLibrary 	the {@code ESTLibrary} that contains this EST.
	 * @see ESTLibrary
	 */
	public void setEstLibrary(ESTLibrary eSTLibrary)
	{
		this.eSTLibrary = eSTLibrary;
	}
	/**
	 * Get the {@code ESTLibrary} that contains this EST.
	 * 
	 * @return 		the {@code ESTLibrary} that contains this EST.
	 * @see ESTLibrary
	 */
	public ESTLibrary getEstLibrary()
	{
		return this.eSTLibrary;
	}
    //---------------------------------
	/**
	 * Set the ID of the {@code ESTLibrary} that contains this EST.
	 * 
	 * @param estLibraryId 		the String corresponding to the ID of the {@code ESTLibrary} that contains this EST.
	 * @see ESTLibrary
	 */
	public void setEstLibraryId(String estLibraryId)
	{
		this.estLibraryId = estLibraryId;
	}
//	/**
//	 * Returns either the value of {@code estLibraryId}, 
//	 * or the of the {@code id} of the {@code ESTLibrary} 
//	 * stored in {@code eSTLibrary}, depending on which one is set. 
//	 * 
//	 * @return 	the ID of the EST library this EST belongs to. 
//	 * @see 	#estLibraryId
//	 * @see 	#eSTLibrary
//	 * @see 	#getIdByEntityOrId(Entity, String)
//	 */
//	public String getEstLibraryId()
//	{
//		return this.getIdByEntityOrId(this.getEstLibrary(), this.estLibraryId);
//	}
}
