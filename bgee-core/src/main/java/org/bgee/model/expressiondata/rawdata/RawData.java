package org.bgee.model.expressiondata.rawdata;

import org.apache.commons.lang3.StringUtils;
import org.bgee.model.Entity;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceFactory;

/**
 * Parent class of all classes related to expression data (see 'see also' section). 
 * Indeed, all expression data come from a data source. This class is intended to hold 
 * attributes and methods related to data sources.
 * <p>
 * The principle of this class is that the information on the data source 
 * ({@code dataSource} or {@code dataSourceId} attributes)
 * can be either retrieved from the object itself, 
 * or from one of its container (many expression data entities are "contained" into another: 
 * {@code microarrayExperiment}s contain {@code AffymetrixChip}s, 
 * that contain {@code AffymetrixProbeset}s, 
 * {@code ESTLibrary} contain {@code EST}s, 
 * {@code InSituExp}s contain {@code InSituEvidence}s 
 * that contain {@code InSituSpot}s, 
 * {@code RNASeqExp} contain {@code RNASeqLibrary} 
 * that contain {@code RNASeqResult}s). 
 * <p>
 * So the getters {@code #getSource()} and {@code #getSourceId()} 
 * first try to retrieve the values from the corresponding attributes of this object, 
 * or if not set, from its container if one is defined. 
 * Classes that have a container <strong>must</strong> reimplement the methods 
 * {@code #getSourceFromContainer()} and {@code #getSourceIdFromContainer()}. 
 * <p>
 * As these methods are "chained", an object can retrieve these attributes even from an indirect container 
 * (meaning, an {@code AffymetrixProbeset} can obtain the information 
 * from a {@code AffymetrixExp}, thanks to the chaining through {@code AffymetrixChip}).
 * <p>
 * Also, note that {@code #loadSource()} will retrieve the data source from a {@code DAO} 
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
@Deprecated
public abstract class RawData extends Entity
{
	/**
	 * A {@code String} corresponding to the ID in Bgee of the data source 
	 * where these expression data come from. 
	 */
    private String     dataSourceId;
    /**
     * A {@code Source} corresponding to the {@code dataSourceId} attribute.
     * @see #dataSourceId
     */
    private Source dataSource;

	/**
     * Default constructor.
     * @param id	A {@code String} representing the ID of this {@code RqwData}.
     */
	public RawData(String id)
    {
    	super(id);
    	this.setSourceId("");
    	this.setSource(null);
    }
	

	/**
	 * Load the {@code dataSource} attribute of this {@code RawData} object, 
	 * either from a "container" with the {@code dataSource} attribute already set; 
	 * or from a DAO using the {@code dataSourceId} attribute of this object, 
	 * or the {@code dataSourceId} attribute of a "container".
	 * Subclasses that have "containers" MUST reimplement {@code getSourceFromContainer()} 
	 * and {@code getSourceIdFromContainer()}.
	 * Subclasses having containers, as of Bgee 12: 
	 * <ul>
	 * <li>{@code model.expressionData.affymetrixData.AffymetrixChip} container: 
	 *     {@code model.expressionData.affymetrixData.microarrayExperiment}
	 * <li>{@code model.expressionData.affymetrixData.AffymetrixProbeset} container: 
	 *     {@code model.expressionData.affymetrixData.AffymetrixChip}
	 * <li>{@code model.expressionData.estData.ExpressedSequenceTag} container: 
	 *     {@code model.expressionData.estData.EstLibrary}
	 * <li>{@code model.expressionData.inSituHybridizationData.InSituEvidence} container: 
	 *     {@code model.expressionData.inSituHybridizationData.InSituExperiment}
	 * <li>{@code model.expressionData.inSituHybridizationData.InSituSpot} container: 
	 *     {@code model.expressionData.inSituHybridizationData.InSituEvidence}
	 * <li>{@code model.expressionData.rnaSeqData.RnaSeqLibrary} container: 
	 *     {@code model.expressionData.rnaSeqData.RnaSeqExperiment}
	 * <li>{@code model.expressionData.rnaSeqData.RnaSeqResult} container: 
	 *     {@code model.expressionData.rnaSeqData.RnaSeqLibrary}
	 * </ul>
	 * <p>
	 * First, this method checks whether the {@code Source} object can be retrieved 
	 * from the {@code dataSource} attribute of this object, 
	 * or from that of a "container" (see {@code getSource()}).
	 * If it failed, then try to load the {@code Source} from a DAO, by getting the 
	 * {@code dataSourceId} from its own attribute, or from its container 
	 * (see {@code getSourceId()}).
	 * <p>
	 * If it is not possible to retrieve any {@code dataSourceId}, 
	 * then the {@code dataSource} attribute remains unchanged.
	 * 
	 * @see #dataSource
	 * @see #getSource()
	 * @see #getSourceId()
	 */
	public void loadSource()
	{
	    //TODO
//		if (this.getSource() == null && StringUtils.isNotBlank(this.getSourceId())) {
//		
//			SourceFactory loader = new SourceFactory();
//			this.setSource(
//					loader.getSourceById(this.getSourceId()));
//		}
	}
	
	/**
	 * Try to obtain the {@code Source} object where these expression data come from, 
	 * from a "container". Subclasses that have such a "container" MUST then reimplement this method 
	 * (see description of {@code loadSource()} for more details). 
	 * Subclasses that do not have such a container should not reimplement this method. 
	 * 
	 * @return 	a {@code Source} object where these expression data come from, 
	 * 			obtained from a "container". 
	 * 			{@code null} if it was not possible to retrieve a {@code Source} object  
	 * 			from a container.
	 * @see #loadSource()
	 */
	public Source getSourceFromContainer()
	{
		return null;
	}
	
	/**
	 * Try to obtain the ID of the data source where these expression data come from, 
	 * from a "container". Subclasses that have such a "container" MUST then reimplement this method 
	 * (see description of {@code loadSource()} for more details). 
	 * Subclasses that do not have such a container should not reimplement this method. 
	 * 
	 * @return 	a {@code String} corresponding to the ID of the data source 
	 * 			where these expression data come from, 
	 * 			obtained from a "container". 
	 * 			Empty {@code String} if it was not possible to retrieve the ID 
	 * 			from a container.
	 * @see #loadSource()
	 */
	public String getSourceIdFromContainer()
	{
		return "";
	}
	
	/**
	 * A helper to get the name of the data source, 
	 * if the {@code dataSource} attribute has been set, 
	 * of if the {@code dataSource} attribute of a "container" has been set 
	 * (see {@code getSource()}).
	 * Return an empty {@code String} if no {@code dataSource} was found.
	 * 
	 * @return 	a {@code String} corresponding to the value returned by a call to {@code getName()}, 
	 * 			of the {@code Source} object stored in the {@code dataSource} attribute 
	 * 			of this object, or of that of a "container" object. 
	 * 			Return an empty {@code String} if no {@code dataSource} was set.
	 * @see #dataSource
	 * @see #getSource()
	 */
	public String getSourceName()
	{
		if (this.getSource() != null) {
			return this.getSource().getName();
		} 
		return "";
	}
	
	/**
	 * A helper to get the ID of the data source, 
	 * if the {@code dataSourceId} attribute has been set, 
	 * or if the {@code dataSource} attribute has been set, 
	 * of if the {@code dataSourceId} or {@code dataSource} attribute of a "container" has been set 
	 * (see {@code getSource()} and {@code getSourceIdFromContainer()}).
	 * Return an empty {@code String} if no {@code dataSource} or {@code dataSourceId} was found.
	 * 
	 * @return 	a {@code String} corresponding to the ID of the data source where these expression data come from, 
	 * 			retrieved either by the  {@code dataSourceId} or {@code dataSource} attribute of this class, 
	 * 			or by those of a container.
	 * 			Return an empty {@code String} if no ID was found.
	 * @see #dataSource
	 * @see #getSource()
	 * @see #getSourceIdFromContainer()
	 */
	public String getSourceId() {
		//if (StringUtils.isNotBlank(this.getIdByEntityOrId(this.getSource(), this.dataSourceId))) {
			//return this.getIdByEntityOrId(this.getSource(), this.dataSourceId);
		//}//TODO
		return this.getSourceIdFromContainer();
	}

	/**
	 * @param 	dataSourceId the dataSourceId to set
	 * @see 	#dataSourceId
	 */
	public void setSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	/**
	 * A helper to get the {@code Source}, 
	 * from the {@code dataSource} attribute of this object, if it has been set, 
	 * of from the {@code dataSource} attribute of a "container", if it has been set 
	 * (see {@code getSourceFromContainer()}).
	 * Return null if no {@code dataSource} was set neither in this object nor in its container.
	 * 
	 * @return 	a {@code Source} corresponding to the data source where these expression data come from, 
	 * 			retrieved either by the {@code dataSource} attribute of this class, 
	 * 			or by that of a container.
	 * 			Return null if no {@code dataSource} was set neither in this object nor in its container.
	 * @see #dataSource
	 * @see #getSourceFromContainer()
	 */
	public Source getSource() {
		if (this.dataSource != null) {
			return this.dataSource;
		}
		return this.getSourceFromContainer();
	}

	/**
	 * @param 	dataSource the dataSource to set
	 * @see 	#dataSource
	 */
	public void setSource(Source dataSource) {
		this.dataSource = dataSource;
	}
}
