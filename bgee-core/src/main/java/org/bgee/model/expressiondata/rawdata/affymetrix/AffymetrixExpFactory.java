package org.bgee.model.expressiondata.rawdata.affymetrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A {@code Factory} responsible for instantiating {@code AffymetrixExp} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixExp
 * @since Bgee 01
 */
public class AffymetrixExpFactory //extends Factory
{
//	/**
//	 * A {@code AffymetrixExpDAO} used to query a data source, 
//	 * to obtain {@code AffymetrixExpTO} objects, 
//	 * used to instantiate and populate {@code AffymetrixExp} objects 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * <p>
//	 * This {@code AffymetrixExpDAO} is obtained using a {@code DAOFactory} 
//	 * returned by the {@code #getDAOFactory()} method of the parent class. 
//	 * 
//	 * @see Factory#getDAOFactory()
//	 */
//	private final AffymetrixExpDAO dao;
//	
//	/**
//	 * Default constructor. 
//	 */
//	public AffymetrixExpFactory()
//    {
//    	super();
//    	this.dao = this.getDAOFactory().getMicroarrayExperimentDAO();
//    }
//
//    /**
//	 * Get a {@code AffymetrixExp} retrieved by its ID.
//	 * 
//	 * @param experimentId 	A {@code String} representing the ID 
//	 * 						of the micrarray experiment to retrieve.
//	 * @return 				A {@code AffymetrixExp} object, 
//	 * 						corresponding to the {@code experimentId}.
//	 */
//	public AffymetrixExp getExperimentById(String microarrayExperimentId) 
//	{
//		return this.createMicroarrayExperiment(this.dao.getExperimentById(microarrayExperimentId));
//	}
//    
//	/**
//	 * Create a {@code Collection} of {@code AffymetrixExp} objects, 
//	 * using a {@code Collection} of {@code TransferObject} objects 
//	 * that must be castable to {@code AffymetrixExpTO} objects. 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * 
//	 * @param toCollection 	A {@code Collection} of {@code TransferObject} objects 
//	 * 						castable to {@code AffymetrixExpTO} objects, 
//	 * 						obtained from a data source.
//	 * @return 				A {@code Collection} of {@code AffymetrixExp} objects 	
//	 * 						corresponding to the provided {@code TransferObject}s.
//	 */
//    private Collection<AffymetrixExp> getMicroarrayExperiments(Collection<TransferObject> toCollection)
//    {
//    	Collection<AffymetrixExp> expList = new ArrayList<AffymetrixExp>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		expList.add(this.createMicroarrayExperiment((AffymetrixExpTO) iterator.next()));
//    	}
//    	return expList;
//    }
//    
//    /**
//     * Instantiate and populate the attribute of a {@code AffymetrixExp} object, 
//     * using a {@code AffymetrixExpTO} object, retrieved from a data source 
//     * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer).
//	 * 
//     * @param expTO 	A {@code AffymetrixExpTO} retrieved from a data source, 
//     * 					used to populate the attributes of a {@code AffymetrixExp} object.
//     * @return			A {@code AffymetrixExp} object newly instantiated, 
//     * 					with attributes set using the {@code AffymetrixExpTO} object. 
//     */
//    private AffymetrixExp createMicroarrayExperiment(AffymetrixExpTO expTO)
//    {
//    	AffymetrixExp exp = null;
//    	if (expTO != null) {
//    		exp = new AffymetrixExp();
//    		exp.setId(expTO.id);
//    		exp.setName(expTO.name);
//    		exp.setDescription(expTO.description);
//    		exp.setDataSourceId(expTO.dataSourceId);
//    	}
//    	return exp;
//    }
}
