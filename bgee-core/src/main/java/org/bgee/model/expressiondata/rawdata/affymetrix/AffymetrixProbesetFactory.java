package org.bgee.model.expressiondata.rawdata.affymetrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A {@code Factory} responsible for instantiating {@code AffymetrixProbeset} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixProbeset
 * @since Bgee 01
 */
public class AffymetrixProbesetFactory //extends Factory
{
//	/**
//	 * An {@code AffymetrixProbesetDAO} used to query a data source, 
//	 * to obtain {@code AffymetrixProbesetTO} objects, 
//	 * used to instantiate and populate {@code AffymetrixProbeset} objects 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * <p>
//	 * This {@code AffymetrixProbesetDAO} is obtained using a {@code DAOFactory} 
//	 * returned by the {@code #getDAOFactory()} method of the parent class. 
//	 * 
//	 * @see Factory#getDAOFactory()
//	 */
//	private final AffymetrixProbesetDAO dao;
//	
//	/**
//	 * Default constructor. 
//	 */
//    public AffymetrixProbesetFactory()
//    {
//    	super();
//    	this.dao = this.getDAOFactory().getAffymetrixProbesetDAO();
//    }
//
//    /**
//	 * Obtain a {@code Collection} of {@code AffymetrixProbeset}s, 
//	 * retrieved from an expression query. 
//	 * 
//	 * @param expressionParam 	A {@code DataTypeTO} used to define the expression parameters 
//	 * 							(choice of data type, data quality, etc.).
//	 * @param multiSpeciesTO 	A {@code MultiSpeciesTO} storing the parameters of the expression query 
//	 * 							related to the species, the organs, the stages. 
//	 * @return 					A {@code Collection} of {@code AffymetrixProbeset}s, 
//	 * 							retrieved thanks to an expression query parameterized using 
//	 * 							{@code expressionParam} and {@code multiSpeciesTO}.
//	 */
//	public Collection<AffymetrixProbeset> getProbesetsByExpression(DataTypeTO expressionParam,
//			MultiSpeciesTO multiSpeciesTO) 
//	{
//		return this.getAffymetrixProbesets(this.dao.getProbesetByExpression(
//				expressionParam, multiSpeciesTO));
//	}
//    
//	/**
//	 * Create a {@code Collection} of {@code AffymetrixProbeset} objects, 
//	 * using a {@code Collection} of {@code TransferObject} objects 
//	 * that must be castable to {@code AffymetrixProbesetTO} objects. 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * 
//	 * @param toCollection 	A {@code Collection} of {@code TransferObject} objects 
//	 * 						castable to {@code AffymetrixProbesetTO} objects, 
//	 * 						obtained from a data source.
//	 * @return 				A {@code Collection} of {@code AffymetrixProbeset} objects 	
//	 * 						corresponding to the provided {@code TransferObject}s.
//	 */
//    private Collection<AffymetrixProbeset> getAffymetrixProbesets(Collection<TransferObject> toCollection)
//    {
//    	Collection<AffymetrixProbeset> probesetList = new ArrayList<AffymetrixProbeset>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		probesetList.add(this.createAffymetrixProbeset((AffymetrixProbesetTO) iterator.next()));
//    	}
//    	return probesetList;
//    }
//    
//    /**
//     * Instantiate and populate the attributes of a {@code AffymetrixProbeset} object, 
//     * using a {@code AffymetrixProbesetTO} object, retrieved from a data source 
//     * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer).
//	 * 
//     * @param libraryTO A {@code AffymetrixProbesetTO} retrieved from a data source, 
//     * 					used to populate the attributes of a {@code AffymetrixProbeset} object.
//     * @return			A {@code AffymetrixProbeset} object newly instantiated, 
//     * 					with attributes set using the {@code AffymetrixProbesetTO} object. 
//     */
//    private AffymetrixProbeset createAffymetrixProbeset(AffymetrixProbesetTO probesetTO)
//    {
//    	AffymetrixProbeset probeset = null;
//    	if (probesetTO != null) {
//    		probeset = new AffymetrixProbeset();
//    		
//    		probeset.setId(probesetTO.id);
//    		probeset.setBgeeAffymetrixChipId(probesetTO.bgeeAffymetrixChipId);
//    		probeset.setNormalizedSignalIntensity(probesetTO.normalizedSignalIntensity);
//    		probeset.setGeneId(probesetTO.geneId);
//    		probeset.setExpressionId(probesetTO.expressionId);
//    		probeset.setExpressionConfidence(probesetTO.expressionConfidence);
//    		probeset.setDetectionFlag(probesetTO.detectionFlag);
//    	}
//    	return probeset;
//    }
}
