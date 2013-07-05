package org.bgee.model.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bgee.model.data.common.DAOFactory;
import org.bgee.model.data.common.TransferObject;
import org.bgee.model.data.common.source.SourceDAO;
import org.bgee.model.data.common.source.SourceTO;

/**
 * Factory of <code>DataSource</code> objects.
 * 
 * This class proposes methods to retrieve a <code>DataSource</code> object, 
 * or a <code>Collection</code> of <code>DataSource</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee11, June 2012
 * @see DataSource
 * @since Bgee11
 *
 */
public class SourceFactory 
{
	/**
	 * Default constructor.
	 */
    public SourceFactory()
    {
    	
    }
    
    /**
     * Retrieve from the data source (e.g., the Bgee database) 
     * a data source (e.g., ArrayExpress) by its ID.
     * 
     * @param dataSourceId 	a <code>String</code> representing the ID of the data source to retrieve
     * @return				a <code>DataSoure</code> corresponding to <code>dataSourceId</code>
     */
    public DataSource getDataSourceById(String dataSourceId)
    {
    	DAOFactory factory = DAOFactory.getDAOFactory();
    	SourceDAO sourceDAO = factory.getDataSourceDAO();
    	return this.createDataSource(sourceDAO.getDataSourceById(dataSourceId));
    }
    
    /**
     * Return all the data sources (e.g., ArrayExpress, ZFIN) used in Bgee, 
     * retrieved from a data source (e.g., the Bgee database), 
     * as a <code>Collection</code> of <code>DataSource</code>.
     * 
     * @return 	a <code>Collection</code> of <code>DataSource</code> 
     * 			representing the data sources used in Bgee.
     */
    public Collection<DataSource> getAllDataSources()
    {
    	DAOFactory factory = DAOFactory.getDAOFactory();
    	SourceDAO sourceDAO = factory.getDataSourceDAO();
    	return this.getDataSources(sourceDAO.getAllDataSources());
    }

    /**
     * Return data sources (e.g., ArrayExpress, ZFIN) used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from, 
     * as a <code>Collection</code> of <code>DataSource</code> ordered by their category.
     * This method can be used for instance to display Bgee data origins and version details.
     * 
     * @return 	a <code>Collection</code> of <code>DataSource</code>,  
     * 			ordered by their category, 
     * 			representing the data sources where some Bgee data actually come from.
     */
	public Collection<DataSource> getDisplayableDataSources() 
	{
		DAOFactory factory = DAOFactory.getDAOFactory();
    	SourceDAO sourceDAO = factory.getDataSourceDAO();
    	return this.getDataSources(sourceDAO.getDisplayableDataSources());
	}
    
    
    /**
     * Return a <code>Collection</code> of <code>DataSource</code> objects, 
     * created using a <code>Collection</code> of <code>TransferObject</code>, 
     * castable to <code>SourceTO</code> objects.
     * <code>SourceTO</code> objects are used to hold values retrieved from the data source, 
     * related to <code>DataSource</code>. Work as a bridge between the data source and the <code>model</code>.
     * 
     * @param dataSourceTO 	a <code>Collection</code> of of <code>TransferObject</code>, 
     * 						castable to <code>SourceTO</code>, 
     * 						handling values retrieved from a data source, 
     * 						to populate a <code>Collection</code> of new <code>DataSource</code> objects.
     * @return 				a <code>Collection</code> of <code>DataSource</code> objects, 
     * 						with arguments populated using the <code>SourceTO</code> objects 
     * 						coming from <code>toCollection</code>. 
     * 						An empty <code>Collection</code> if <code>toCollection</code> was empty.
     */
    private Collection<DataSource> getDataSources(Collection<TransferObject> toCollection)
    {
    	Collection<DataSource> dataSourceList = new ArrayList<DataSource>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		dataSourceList.add(this.createDataSource((SourceTO) iterator.next()));
    	}
    	return dataSourceList;
    }
    
    /**
     * Return a <code>DataSource</code> object created using a <code>SourceTO</code> object.
     * <code>SourceTO</code> objects are used to hold values retrieved from the data source, 
     * related to <code>DataSource</code>. Work as a bridge between the data source and the <code>model</code>.
     * 
     * @param dataSourceTO 	a <code>SourceTO</code>, handling values retrieved from a data source, 
     * 						to populate a new <code>DataSource</code> object.
     * @return 				a <code>DataSource</code>, with arguments populated using the <code>SourceTO</code>. 
     * 						<code>null</code> if <code>dataSourceTO</code> was <code>null</code>.
     */
    private DataSource createDataSource(SourceTO sourceTO)
    {
    	DataSource dataSource = null;
    	
    	if (sourceTO != null) {
    		dataSource = new DataSource();
    		dataSource.setId(sourceTO.id);
    		dataSource.setName(sourceTO.name);
    		dataSource.setDescription(sourceTO.dataSourceDescription);
    		
    		dataSource.setXRefUrl(sourceTO.xRefUrl);
    		dataSource.setExperimentUrl(sourceTO.experimentUrl);
    		dataSource.setEvidenceUrl(sourceTO.evidenceUrl);
    		dataSource.setBaseUrl(sourceTO.baseUrl);
    		
    		dataSource.setReleaseDate(sourceTO.releaseDate);
    		dataSource.setReleaseVersion(sourceTO.releaseVersion);
    		
    		dataSource.setToDisplay(sourceTO.toDisplay);
    		dataSource.setCategory(sourceTO.category);
    	}
    	
    	return dataSource;
    }
}
