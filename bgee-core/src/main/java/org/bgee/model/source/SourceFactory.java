package org.bgee.model.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bgee.model.Factory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.source.SourceDAO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;

/**
 * Factory of {@code Source} objects.
 * 
 * This class proposes methods to retrieve a {@code Source} object, 
 * or a {@code Collection} of {@code Source} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee11
 * @see Source
 * @since Bgee11
 *
 */
public class SourceFactory extends Factory
{
	/**
	 * Default constructor.
	 */
    public SourceFactory()
    {
    	
    }
//    
//    /**
//     * Retrieve from the data source (e.g., the Bgee database) 
//     * a data source (e.g., ArrayExpress) by its ID.
//     * 
//     * @param SourceId 	a {@code String} representing the ID of the data source to retrieve
//     * @return				a {@code DataSoure} corresponding to {@code SourceId}
//     */
//    public Source getSourceById(String sourceId)
//    {
//        DAOManager factory = DAOManager.getDAOManager();
//    	SourceDAO sourceDAO = factory.getSourceDAO();
//    	return this.createSource(sourceDAO.getDataSourceById(sourceId));
//    }
//    
//    /**
//     * Return all the data sources (e.g., ArrayExpress, ZFIN) used in Bgee, 
//     * retrieved from a data source (e.g., the Bgee database), 
//     * as a {@code Collection} of {@code Source}.
//     * 
//     * @return 	a {@code Collection} of {@code Source} 
//     * 			representing the data sources used in Bgee.
//     */
//    public Collection<Source> getAllSources()
//    {
//    	DAOFactory factory = DAOFactory.getDAOFactory();
//    	SourceDAO sourceDAO = factory.getSourceDAO();
//    	return this.getSources(sourceDAO.getAllSources());
//    }
//
//    /**
//     * Return data sources (e.g., ArrayExpress, ZFIN) used in Bgee, that are not used only for xrefs purpose, 
//     * but where some Bgee data actually come from, 
//     * as a {@code Collection} of {@code Source} ordered by their category.
//     * This method can be used for instance to display Bgee data origins and version details.
//     * 
//     * @return 	a {@code Collection} of {@code Source},  
//     * 			ordered by their category, 
//     * 			representing the data sources where some Bgee data actually come from.
//     */
//	public Collection<Source> getDisplayableSources() 
//	{
//		DAOFactory factory = DAOFactory.getDAOFactory();
//    	SourceDAO sourceDAO = factory.getSourceDAO();
//    	return this.getSources(sourceDAO.getDisplayableSources());
//	}
//    
//    
//    /**
//     * Return a {@code Collection} of {@code Source} objects, 
//     * created using a {@code Collection} of {@code TransferObject}, 
//     * castable to {@code SourceTO} objects.
//     * {@code SourceTO} objects are used to hold values retrieved from the data source, 
//     * related to {@code Source}. Work as a bridge between the data source and the {@code model}.
//     * 
//     * @param SourceTO 	a {@code Collection} of of {@code TransferObject}, 
//     * 						castable to {@code SourceTO}, 
//     * 						handling values retrieved from a data source, 
//     * 						to populate a {@code Collection} of new {@code Source} objects.
//     * @return 				a {@code Collection} of {@code Source} objects, 
//     * 						with arguments populated using the {@code SourceTO} objects 
//     * 						coming from {@code toCollection}. 
//     * 						An empty {@code Collection} if {@code toCollection} was empty.
//     */
//    private Collection<Source> getSources(Collection<TransferObject> toCollection)
//    {
//    	Collection<Source> SourceList = new ArrayList<Source>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		SourceList.add(this.createSource((SourceTO) iterator.next()));
//    	}
//    	return SourceList;
//    }
//    
//    /**
//     * Return a {@code Source} object created using a {@code SourceTO} object.
//     * {@code SourceTO} objects are used to hold values retrieved from the data source, 
//     * related to {@code Source}. Work as a bridge between the data source and the {@code model}.
//     * 
//     * @param SourceTO 	a {@code SourceTO}, handling values retrieved from a data source, 
//     * 						to populate a new {@code Source} object.
//     * @return 				a {@code Source}, with arguments populated using the {@code SourceTO}. 
//     * 						{@code null} if {@code SourceTO} was {@code null}.
//     */
//    private Source createSource(SourceTO sourceTO)
//    {
//    	Source Source = null;
//    	
//    	if (sourceTO != null) {
//    		Source = new Source();
//    		Source.setId(sourceTO.id);
//    		Source.setName(sourceTO.name);
//    		Source.setDescription(sourceTO.SourceDescription);
//    		
//    		Source.setXRefUrl(sourceTO.xRefUrl);
//    		Source.setExperimentUrl(sourceTO.experimentUrl);
//    		Source.setEvidenceUrl(sourceTO.evidenceUrl);
//    		Source.setBaseUrl(sourceTO.baseUrl);
//    		
//    		Source.setReleaseDate(sourceTO.releaseDate);
//    		Source.setReleaseVersion(sourceTO.releaseVersion);
//    		
//    		Source.setToDisplay(sourceTO.toDisplay);
//    		Source.setCategory(sourceTO.category);
//    	}
//    	
//    	return Source;
//    }
}
