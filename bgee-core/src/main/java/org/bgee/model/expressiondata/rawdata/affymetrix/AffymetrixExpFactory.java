package org.bgee.model.expressiondata.rawdata.affymetrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A <code>EntityFactory</code> responsible for instantiating <code>AffymetrixExp</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixExp
 * @since Bgee 01
 */
public class AffymetrixExpFactory extends EntityFactory
{
	/**
	 * A <code>AffymetrixExpDAO</code> used to query a data source, 
	 * to obtain <code>AffymetrixExpTO</code> objects, 
	 * used to instantiate and populate <code>AffymetrixExp</code> objects 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * <p>
	 * This <code>AffymetrixExpDAO</code> is obtained using a <code>DAOFactory</code> 
	 * returned by the <code>#getDAOFactory()</code> method of the parent class. 
	 * 
	 * @see EntityFactory#getDAOFactory()
	 */
	private final AffymetrixExpDAO dao;
	
	/**
	 * Default constructor. 
	 */
	public AffymetrixExpFactory()
    {
    	super();
    	this.dao = this.getDAOFactory().getMicroarrayExperimentDAO();
    }

    /**
	 * Get a <code>AffymetrixExp</code> retrieved by its ID.
	 * 
	 * @param experimentId 	A <code>String</code> representing the ID 
	 * 						of the micrarray experiment to retrieve.
	 * @return 				A <code>AffymetrixExp</code> object, 
	 * 						corresponding to the <code>experimentId</code>.
	 */
	public AffymetrixExp getExperimentById(String microarrayExperimentId) 
	{
		return this.createMicroarrayExperiment(this.dao.getExperimentById(microarrayExperimentId));
	}
    
	/**
	 * Create a <code>Collection</code> of <code>AffymetrixExp</code> objects, 
	 * using a <code>Collection</code> of <code>TransferObject</code> objects 
	 * that must be castable to <code>AffymetrixExpTO</code> objects. 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * 
	 * @param toCollection 	A <code>Collection</code> of <code>TransferObject</code> objects 
	 * 						castable to <code>AffymetrixExpTO</code> objects, 
	 * 						obtained from a data source.
	 * @return 				A <code>Collection</code> of <code>AffymetrixExp</code> objects 	
	 * 						corresponding to the provided <code>TransferObject</code>s.
	 */
    private Collection<AffymetrixExp> getMicroarrayExperiments(Collection<TransferObject> toCollection)
    {
    	Collection<AffymetrixExp> expList = new ArrayList<AffymetrixExp>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		expList.add(this.createMicroarrayExperiment((AffymetrixExpTO) iterator.next()));
    	}
    	return expList;
    }
    
    /**
     * Instantiate and populate the attribute of a <code>AffymetrixExp</code> object, 
     * using a <code>AffymetrixExpTO</code> object, retrieved from a data source 
     * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer).
	 * 
     * @param expTO 	A <code>AffymetrixExpTO</code> retrieved from a data source, 
     * 					used to populate the attributes of a <code>AffymetrixExp</code> object.
     * @return			A <code>AffymetrixExp</code> object newly instantiated, 
     * 					with attributes set using the <code>AffymetrixExpTO</code> object. 
     */
    private AffymetrixExp createMicroarrayExperiment(AffymetrixExpTO expTO)
    {
    	AffymetrixExp exp = null;
    	if (expTO != null) {
    		exp = new AffymetrixExp();
    		exp.setId(expTO.id);
    		exp.setName(expTO.name);
    		exp.setDescription(expTO.description);
    		exp.setDataSourceId(expTO.dataSourceId);
    	}
    	return exp;
    }
}
