package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A <code>Factory</code> responsible for instantiating <code>RNASeqExp</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqExp
 * @since Bgee 12
 */
public class RNASeqExpFactory extends Factory
{
	/**
	 * A <code>RNASeqExpDAO</code> used to query a data source, 
	 * to obtain <code>RNASeqExpTO</code> objects, 
	 * used to instantiate and populate <code>RNASeqExp</code> objects 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * <p>
	 * This <code>RNASeqExpDAO</code> is obtained using a <code>DAOFactory</code> 
	 * returned by the <code>#getDAOFactory()</code> method of the parent class. 
	 * 
	 * @see Factory#getDAOFactory()
	 */
	private final RNASeqExpDAO dao;
	
	/**
	 * Default constructor. 
	 */
	public RNASeqExpFactory()
    {
    	super();
    	this.dao = this.getDAOFactory().getRnaSeqExperimentDAO();
    }

	/**
	 * Get a <code>RNASeqExp</code> retrieved by its ID.
	 * 
	 * @param experimentId 	A <code>String</code> representing the ID 
	 * 						of the RNA-Seq experiment to retrieve.
	 * @return 				A <code>RNASeqExp</code> object, 
	 * 						corresponding to the <code>experimentId</code>.
	 */
	public RNASeqExp getExperimentById(String experimentId) 
	{
		return this.createRnaSeqExperiment(this.dao.getExperimentById(experimentId));
	}
    
	/**
	 * Create a <code>Collection</code> of <code>RNASeqExp</code> objects, 
	 * using a <code>Collection</code> of <code>TransferObject</code> objects 
	 * that must be castable to <code>RNASeqExpTO</code> objects. 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * 
	 * @param toCollection 	A <code>Collection</code> of <code>TransferObject</code> objects 
	 * 						castable to <code>RNASeqExpTO</code> objects, 
	 * 						obtained from a data source.
	 * @return 				A <code>Collection</code> of <code>RNASeqExp</code> objects 	
	 * 						corresponding to the provided <code>TransferObject</code>s.
	 */
    private Collection<RNASeqExp> getRnaSeqExperiments(Collection<TransferObject> toCollection)
    {
    	Collection<RNASeqExp> expList = new ArrayList<RNASeqExp>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		expList.add(this.createRnaSeqExperiment((RNASeqExpTO) iterator.next()));
    	}
    	return expList;
    }
    
    /**
     * Instantiate and populate the attribute of a <code>RNASeqExp</code> object, 
     * using a <code>RNASeqExpTO</code> object, retrieved from a data source 
     * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer).
	 * 
     * @param expTO 	A <code>RNASeqExpTO</code> retrieved from a data source, 
     * 					used to populate the attributes of a <code>RNASeqExp</code> object.
     * @return			A <code>RNASeqExp</code> object newly instantiated, 
     * 					with attributes set using the <code>RNASeqExpTO</code> object. 
     */
    private RNASeqExp createRnaSeqExperiment(RNASeqExpTO expTO)
    {
    	RNASeqExp exp = null;
    	if (expTO != null) {
    		exp = new RNASeqExp();
    		exp.setId(expTO.id);
    		exp.setName(expTO.name);
    		exp.setDescription(expTO.description);
    		exp.setDataSourceId(expTO.dataSourceId);
    	}
    	return exp;
    }
}
