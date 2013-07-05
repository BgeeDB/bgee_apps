package org.bgee.model.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bgee.model.EntityFactory;
import org.bgee.model.data.common.DAOFactory;
import org.bgee.model.data.common.TransferObject;
import org.bgee.model.data.common.source.SourceDAO;
import org.bgee.model.data.common.source.SourceTO;

/**
 * Factory of <code>Source</code> objects.
 * 
 * This class proposes methods to retrieve a <code>Source</code> object, 
 * or a <code>Collection</code> of <code>Source</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee11
 * @see Source
 * @since Bgee11
 *
 */
public class SourceFactory extends EntityFactory
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
     * @param SourceId 	a <code>String</code> representing the ID of the data source to retrieve
     * @return				a <code>DataSoure</code> corresponding to <code>SourceId</code>
     */
    public Source getSourceById(String SourceId)
    {
    	DAOFactory factory = DAOFactory.getDAOFactory();
    	SourceDAO sourceDAO = factory.getSourceDAO();
    	return this.createSource(sourceDAO.getSourceById(SourceId));
    }
    
    /**
     * Return all the data sources (e.g., ArrayExpress, ZFIN) used in Bgee, 
     * retrieved from a data source (e.g., the Bgee database), 
     * as a <code>Collection</code> of <code>Source</code>.
     * 
     * @return 	a <code>Collection</code> of <code>Source</code> 
     * 			representing the data sources used in Bgee.
     */
    public Collection<Source> getAllSources()
    {
    	DAOFactory factory = DAOFactory.getDAOFactory();
    	SourceDAO sourceDAO = factory.getSourceDAO();
    	return this.getSources(sourceDAO.getAllSources());
    }

    /**
     * Return data sources (e.g., ArrayExpress, ZFIN) used in Bgee, that are not used only for xrefs purpose, 
     * but where some Bgee data actually come from, 
     * as a <code>Collection</code> of <code>Source</code> ordered by their category.
     * This method can be used for instance to display Bgee data origins and version details.
     * 
     * @return 	a <code>Collection</code> of <code>Source</code>,  
     * 			ordered by their category, 
     * 			representing the data sources where some Bgee data actually come from.
     */
	public Collection<Source> getDisplayableSources() 
	{
		DAOFactory factory = DAOFactory.getDAOFactory();
    	SourceDAO sourceDAO = factory.getSourceDAO();
    	return this.getSources(sourceDAO.getDisplayableSources());
	}
    
    
    /**
     * Return a <code>Collection</code> of <code>Source</code> objects, 
     * created using a <code>Collection</code> of <code>TransferObject</code>, 
     * castable to <code>SourceTO</code> objects.
     * <code>SourceTO</code> objects are used to hold values retrieved from the data source, 
     * related to <code>Source</code>. Work as a bridge between the data source and the <code>model</code>.
     * 
     * @param SourceTO 	a <code>Collection</code> of of <code>TransferObject</code>, 
     * 						castable to <code>SourceTO</code>, 
     * 						handling values retrieved from a data source, 
     * 						to populate a <code>Collection</code> of new <code>Source</code> objects.
     * @return 				a <code>Collection</code> of <code>Source</code> objects, 
     * 						with arguments populated using the <code>SourceTO</code> objects 
     * 						coming from <code>toCollection</code>. 
     * 						An empty <code>Collection</code> if <code>toCollection</code> was empty.
     */
    private Collection<Source> getSources(Collection<TransferObject> toCollection)
    {
    	Collection<Source> SourceList = new ArrayList<Source>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		SourceList.add(this.createSource((SourceTO) iterator.next()));
    	}
    	return SourceList;
    }
    
    /**
     * Return a <code>Source</code> object created using a <code>SourceTO</code> object.
     * <code>SourceTO</code> objects are used to hold values retrieved from the data source, 
     * related to <code>Source</code>. Work as a bridge between the data source and the <code>model</code>.
     * 
     * @param SourceTO 	a <code>SourceTO</code>, handling values retrieved from a data source, 
     * 						to populate a new <code>Source</code> object.
     * @return 				a <code>Source</code>, with arguments populated using the <code>SourceTO</code>. 
     * 						<code>null</code> if <code>SourceTO</code> was <code>null</code>.
     */
    private Source createSource(SourceTO sourceTO)
    {
    	Source Source = null;
    	
    	if (sourceTO != null) {
    		Source = new Source();
    		Source.setId(sourceTO.id);
    		Source.setName(sourceTO.name);
    		Source.setDescription(sourceTO.SourceDescription);
    		
    		Source.setXRefUrl(sourceTO.xRefUrl);
    		Source.setExperimentUrl(sourceTO.experimentUrl);
    		Source.setEvidenceUrl(sourceTO.evidenceUrl);
    		Source.setBaseUrl(sourceTO.baseUrl);
    		
    		Source.setReleaseDate(sourceTO.releaseDate);
    		Source.setReleaseVersion(sourceTO.releaseVersion);
    		
    		Source.setToDisplay(sourceTO.toDisplay);
    		Source.setCategory(sourceTO.category);
    	}
    	
    	return Source;
    }
}
