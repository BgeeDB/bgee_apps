package org.bgee.model.expressiondata.rawdata;

import org.apache.commons.lang.StringUtils;

/**
 * Parent class of all classes related to expression data (see 'see also' section). 
 * Indeed, all expression data come from a data source. This class is intended to hold 
 * attributes and methods related to data sources.
 * <p>
 * The principle of this class is that the information on the data source 
 * (<code>dataSource</code> or <code>dataSourceId</code> attributes)
 * can be either retrieved from the object itself, 
 * or from one of its container (many expression data entities are "contained" into another: 
 * <code>microarrayExperiment</code>s contain <code>AffymetrixChip</code>s, 
 * that contain <code>AffymetrixProbeset</code>s, 
 * <code>ESTLibrary</code> contain <code>EST</code>s, 
 * <code>InSituExp</code>s contain <code>InSituEvidence</code>s 
 * that contain <code>InSituSpot</code>s, 
 * <code>RNASeqExp</code> contain <code>RNASeqLibrary</code> 
 * that contain <code>RNASeqResult</code>s). 
 * <p>
 * So the getters <code>#getDataSource()</code> and <code>#getDataSourceId()</code> 
 * first try to retrieve the values from the corresponding attributes of this object, 
 * or if not set, from its container if one is defined. 
 * Classes that have a container <strong>must</strong> reimplement the methods 
 * <code>#getDataSourceFromContainer()</code> and <code>#getDataSourceIdFromContainer()</code>. 
 * <p>
 * As these methods are "chained", an object can retrieve these attributes even from an indirect container 
 * (meaning, an <code>AffymetrixProbeset</code> can obtain the information 
 * from a <code>microarrayExperiment</code>, thanks to the chaining through <code>AffymetrixChip</code>).
 * <p>
 * Also, note that <code>#loadDataSource()</code> will retrieve the data source from a <code>DAO</code> 
 * only if none of its containers (direct or indirect) hold this data source. 
 * This method can then be safely call on all entities contained by another. 
 * 
 * @author Frederic Bastian
 * @version Bgee 12 November 2012
 * @see model.expressionData.estData.EstLibrary
 * @see model.expressionData.estData.ExpressedSequenceTag
 * @see model.expressionData.inSituHybridizationData.InSituExperiment
 * @see model.expressionData.inSituHybridizationData.InSituEvidence
 * @see model.expressionData.inSituHybridizationData.InSituSpot
 * @see model.expressionData.affymetrixData.MicroarrayExperiment
 * @see model.expressionData.affymetrixData.AffymetrixChip
 * @see model.expressionData.affymetrixData.AffymetrixProbeset
 * @see model.expressionData.rnaSeqData.RnaSeqExperiment
 * @see model.expressionData.rnaSeqData.RnaSeqLibrary
 * @see model.expressionData.rnaSeqData.RnaSeqResult
 * @since Bgee 11
 *
 */
public abstract class RawData extends EntityDescription
{
	/**
	 * A <code>String</code> corresponding to the ID in Bgee of the data source 
	 * where these expression data come from. 
	 */
    private String     dataSourceId;
    /**
     * A <code>DataSource</code> corresponding to the <code>dataSourceId</code> attribute.
     * @see #dataSourceId
     */
    private DataSource dataSource;

	/**
     * Default constructor.
     */
	public RawData()
    {
    	super();
    	this.setDataSourceId("");
    	this.setDataSource(null);
    }
	

	/**
	 * Load the <code>dataSource</code> attribute of this <code>RawData</code> object, 
	 * either from a "container" with the <code>dataSource</code> attribute already set; 
	 * or from a DAO using the <code>dataSourceId</code> attribute of this object, 
	 * or the <code>dataSourceId</code> attribute of a "container".
	 * Subclasses that have "containers" MUST reimplement <code>getDataSourceFromContainer()</code> 
	 * and <code>getDataSourceIdFromContainer()</code>.
	 * Subclasses having containers, as of Bgee 12: 
	 * <ul>
	 * <li><code>model.expressionData.affymetrixData.AffymetrixChip</code> container: 
	 *     <code>model.expressionData.affymetrixData.microarrayExperiment</code>
	 * <li><code>model.expressionData.affymetrixData.AffymetrixProbeset</code> container: 
	 *     <code>model.expressionData.affymetrixData.AffymetrixChip</code>
	 * <li><code>model.expressionData.estData.ExpressedSequenceTag</code> container: 
	 *     <code>model.expressionData.estData.EstLibrary</code>
	 * <li><code>model.expressionData.inSituHybridizationData.InSituEvidence</code> container: 
	 *     <code>model.expressionData.inSituHybridizationData.InSituExperiment</code>
	 * <li><code>model.expressionData.inSituHybridizationData.InSituSpot</code> container: 
	 *     <code>model.expressionData.inSituHybridizationData.InSituEvidence</code>
	 * <li><code>model.expressionData.rnaSeqData.RnaSeqLibrary</code> container: 
	 *     <code>model.expressionData.rnaSeqData.RnaSeqExperiment</code>
	 * <li><code>model.expressionData.rnaSeqData.RnaSeqResult</code> container: 
	 *     <code>model.expressionData.rnaSeqData.RnaSeqLibrary</code>
	 * </ul>
	 * <p>
	 * First, this method checks whether the <code>DataSource</code> object can be retrieved 
	 * from the <code>dataSource</code> attribute of this object, 
	 * or from that of a "container" (see <code>getDataSource()</code>).
	 * If it failed, then try to load the <code>DataSource</code> from a DAO, by getting the 
	 * <code>dataSourceId</code> from its own attribute, or from its container 
	 * (see <code>getDataSourceId()</code>).
	 * <p>
	 * If it is not possible to retrieve any <code>dataSourceId</code>, 
	 * then the <code>dataSource</code> attribute remains unchanged.
	 * 
	 * @see #dataSource
	 * @see #getDataSource()
	 * @see #getDataSourceId()
	 */
	public void loadDataSource()
	{
		if (this.getDataSource() == null && StringUtils.isNotBlank(this.getDataSourceId())) {
		
			SourceFactory loader = new SourceFactory();
			this.setDataSource(
					loader.getDataSourceById(this.getDataSourceId()));
		}
	}
	
	/**
	 * Try to obtain the <code>DataSource</code> object where these expression data come from, 
	 * from a "container". Subclasses that have such a "container" MUST then reimplement this method 
	 * (see description of <code>loadDataSource()</code> for more details). 
	 * Subclasses that do not have such a container should not reimplement this method. 
	 * 
	 * @return 	a <code>DataSource</code> object where these expression data come from, 
	 * 			obtained from a "container". 
	 * 			<code>null</code> if it was not possible to retrieve a <code>DataSource</code> object  
	 * 			from a container.
	 * @see #loadDataSource()
	 */
	public DataSource getDataSourceFromContainer()
	{
		return null;
	}
	
	/**
	 * Try to obtain the ID of the data source where these expression data come from, 
	 * from a "container". Subclasses that have such a "container" MUST then reimplement this method 
	 * (see description of <code>loadDataSource()</code> for more details). 
	 * Subclasses that do not have such a container should not reimplement this method. 
	 * 
	 * @return 	a <code>String</code> corresponding to the ID of the data source 
	 * 			where these expression data come from, 
	 * 			obtained from a "container". 
	 * 			Empty <code>String</code> if it was not possible to retrieve the ID 
	 * 			from a container.
	 * @see #loadDataSource()
	 */
	public String getDataSourceIdFromContainer()
	{
		return "";
	}
	
	/**
	 * A helper to get the name of the data source, 
	 * if the <code>dataSource</code> attribute has been set, 
	 * of if the <code>dataSource</code> attribute of a "container" has been set 
	 * (see <code>getDataSource()</code>).
	 * Return an empty <code>String</code> if no <code>dataSource</code> was found.
	 * 
	 * @return 	a <code>String</code> corresponding to the value returned by a call to <code>getName()</code>, 
	 * 			of the <code>DataSource</code> object stored in the <code>dataSource</code> attribute 
	 * 			of this object, or of that of a "container" object. 
	 * 			Return an empty <code>String</code> if no <code>dataSource</code> was set.
	 * @see #dataSource
	 * @see #getDataSource()
	 */
	public String getDataSourceName()
	{
		if (this.getDataSource() != null) {
			return this.getDataSource().getName();
		} 
		return "";
	}
	
	/**
	 * A helper to get the ID of the data source, 
	 * if the <code>dataSourceId</code> attribute has been set, 
	 * or if the <code>dataSource</code> attribute has been set, 
	 * of if the <code>dataSourceId</code> or <code>dataSource</code> attribute of a "container" has been set 
	 * (see <code>getDataSource()</code> and <code>getDataSourceIdFromContainer()</code>).
	 * Return an empty <code>String</code> if no <code>dataSource</code> or <code>dataSourceId</code> was found.
	 * 
	 * @return 	a <code>String</code> corresponding to the ID of the data source where these expression data come from, 
	 * 			retrieved either by the  <code>dataSourceId</code> or <code>dataSource</code> attribute of this class, 
	 * 			or by those of a container.
	 * 			Return an empty <code>String</code> if no ID was found.
	 * @see #dataSource
	 * @see #getDataSource()
	 * @see #getDataSourceIdFromContainer()
	 */
	public String getDataSourceId() {
		if (StringUtils.isNotBlank(this.getIdByEntityOrId(this.getDataSource(), this.dataSourceId))) {
			return this.getIdByEntityOrId(this.getDataSource(), this.dataSourceId);
		}
		return this.getDataSourceIdFromContainer();
	}

	/**
	 * @param 	dataSourceId the dataSourceId to set
	 * @see 	#dataSourceId
	 */
	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	/**
	 * A helper to get the <code>DataSource</code>, 
	 * from the <code>dataSource</code> attribute of this object, if it has been set, 
	 * of from the <code>dataSource</code> attribute of a "container", if it has been set 
	 * (see <code>getDataSourceFromContainer()</code>).
	 * Return null if no <code>dataSource</code> was set neither in this object nor in its container.
	 * 
	 * @return 	a <code>DataSource</code> corresponding to the data source where these expression data come from, 
	 * 			retrieved either by the <code>dataSource</code> attribute of this class, 
	 * 			or by that of a container.
	 * 			Return null if no <code>dataSource</code> was set neither in this object nor in its container.
	 * @see #dataSource
	 * @see #getDataSourceFromContainer()
	 */
	public DataSource getDataSource() {
		if (this.dataSource != null) {
			return this.dataSource;
		}
		return this.getDataSourceFromContainer();
	}

	/**
	 * @param 	dataSource the dataSource to set
	 * @see 	#dataSource
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
