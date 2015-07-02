package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bgee.model.Factory;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExpDAO;
//import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExpTO;

/**
 * A {@code Factory} responsible for instantiating {@code RNASeqExp} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqExp
 * @since Bgee 12
 */
public class RNASeqExpFactory extends Factory
{
//	/**
//	 * A {@code RNASeqExpDAO} used to query a data source, 
//	 * to obtain {@code RNASeqExpTO} objects, 
//	 * used to instantiate and populate {@code RNASeqExp} objects 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * <p>
//	 * This {@code RNASeqExpDAO} is obtained using a {@code DAOFactory} 
//	 * returned by the {@code #getDAOFactory()} method of the parent class. 
//	 * 
//	 * @see Factory#getDAOFactory()
//	 */
//	private final RNASeqExpDAO dao;
//	
//	/**
//	 * Default constructor. 
//	 */
//	public RNASeqExpFactory()
//    {
//    	super();
//    	this.dao = this.getDAOFactory().getRnaSeqExperimentDAO();
//    }
//
//	/**
//	 * Get a {@code RNASeqExp} retrieved by its ID.
//	 * 
//	 * @param experimentId 	A {@code String} representing the ID 
//	 * 						of the RNA-Seq experiment to retrieve.
//	 * @return 				A {@code RNASeqExp} object, 
//	 * 						corresponding to the {@code experimentId}.
//	 */
//	public RNASeqExp getExperimentById(String experimentId) 
//	{
//		return this.createRnaSeqExperiment(this.dao.getExperimentById(experimentId));
//	}
//    
//	/**
//	 * Create a {@code Collection} of {@code RNASeqExp} objects, 
//	 * using a {@code Collection} of {@code TransferObject} objects 
//	 * that must be castable to {@code RNASeqExpTO} objects. 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * 
//	 * @param toCollection 	A {@code Collection} of {@code TransferObject} objects 
//	 * 						castable to {@code RNASeqExpTO} objects, 
//	 * 						obtained from a data source.
//	 * @return 				A {@code Collection} of {@code RNASeqExp} objects 	
//	 * 						corresponding to the provided {@code TransferObject}s.
//	 */
//    private Collection<RNASeqExp> getRnaSeqExperiments(Collection<TransferObject> toCollection)
//    {
//    	Collection<RNASeqExp> expList = new ArrayList<RNASeqExp>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		expList.add(this.createRnaSeqExperiment((RNASeqExpTO) iterator.next()));
//    	}
//    	return expList;
//    }
//    
//    /**
//     * Instantiate and populate the attribute of a {@code RNASeqExp} object, 
//     * using a {@code RNASeqExpTO} object, retrieved from a data source 
//     * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer).
//	 * 
//     * @param expTO 	A {@code RNASeqExpTO} retrieved from a data source, 
//     * 					used to populate the attributes of a {@code RNASeqExp} object.
//     * @return			A {@code RNASeqExp} object newly instantiated, 
//     * 					with attributes set using the {@code RNASeqExpTO} object. 
//     */
//    private RNASeqExp createRnaSeqExperiment(RNASeqExpTO expTO)
//    {
//    	RNASeqExp exp = null;
//    	if (expTO != null) {
//    		exp = new RNASeqExp();
//    		exp.setId(expTO.id);
//    		exp.setName(expTO.name);
//    		exp.setDescription(expTO.description);
//    		exp.setDataSourceId(expTO.dataSourceId);
//    	}
//    	return exp;
//    }
}
