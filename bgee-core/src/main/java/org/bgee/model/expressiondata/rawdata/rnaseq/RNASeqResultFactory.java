package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A {@code Factory} responsible for instantiating {@code RNASeqResult} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqResult
 * @since Bgee 12
 */
public class RNASeqResultFactory //extends Factory
{
//	/**
//	 * A {@code RNASeqResultDAO} used to query a data source, 
//	 * to obtain {@code RNASeqResultTO} objects, 
//	 * used to instantiate and populate {@code RNASeqResult} objects 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * <p>
//	 * This {@code RNASeqResultDAO} is obtained using a {@code DAOFactory} 
//	 * returned by the {@code #getDAOFactory()} method of the parent class. 
//	 * 
//	 * @see Factory#getDAOFactory()
//	 */
//	private final RNASeqResultDAO dao;
//	
//	/**
//	 * Default constructor. 
//	 */
//    public RNASeqResultFactory()
//    {
//    	super();
//    	this.dao = this.getDAOFactory().getRnaSeqResultDAO();
//    }
//    
//    /**
//	 * Obtain a {@code Collection} of {@code RNASeqResult}s, 
//	 * retrieved from an expression query. 
//	 * 
//	 * @param expressionParam 	A {@code DataTypeTO} used to define the expression parameters 
//	 * 							(choice of data type, data quality, etc.).
//	 * @param multiSpeciesTO 	A {@code MultiSpeciesTO} storing the parameters of the expression query 
//	 * 							related to the species, the organs, the stages. 
//	 * @return 					A {@code Collection} of {@code RNASeqResult}s, 
//	 * 							retrieved thanks to an expression query parameterized using 
//	 * 							{@code expressionParam} and {@code multiSpeciesTO}.
//	 */
//    public Collection<RNASeqResult> getRnaSeqResultsByExpression(DataTypeTO expressionParam,
//			MultiSpeciesTO multiSpeciesTO) 
//	{
//		return this.getRnaSeqResults(this.dao.getRnaSeqResultByExpression(
//				expressionParam, multiSpeciesTO));
//	}
//    
//    /**
//	 * Create a {@code Collection} of {@code RNASeqResult} objects, 
//	 * using a {@code Collection} of {@code TransferObject} objects 
//	 * that must be castable to {@code RNASeqResultTO} objects. 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * 
//	 * @param toCollection 	A {@code Collection} of {@code TransferObject} objects 
//	 * 						castable to {@code RNASeqResultTO} objects, 
//	 * 						obtained from a data source.
//	 * @return 				A {@code Collection} of {@code RNASeqResult} objects 	
//	 * 						corresponding to the provided {@code TransferObject}s.
//	 */
//    private Collection<RNASeqResult> getRnaSeqResults(Collection<TransferObject> toCollection)
//    {
//    	Collection<RNASeqResult> resultList = new ArrayList<RNASeqResult>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		resultList.add(this.createRnaSeqResult((RNASeqResultTO) iterator.next()));
//    	}
//    	return resultList;
//    }
//    
//    /**
//     * Instantiate and populate the attributes of a {@code RNASeqResult} object, 
//     * using a {@code RNASeqResultTO} object, retrieved from a data source 
//     * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer).
//	 * 
//     * @param libraryTO A {@code RNASeqResultTO} retrieved from a data source, 
//     * 					used to populate the attributes of a {@code RNASeqResult} object.
//     * @return			A {@code RNASeqResult} object newly instantiated, 
//     * 					with attributes set using the {@code RNASeqResultTO} object. 
//     */
//    private RNASeqResult createRnaSeqResult(RNASeqResultTO resultTO)
//    {
//    	RNASeqResult result = null;
//    	if (resultTO != null) {
//    		result = new RNASeqResult();
//    		
//    		result.setId(resultTO.id);
//    		result.setRnaSeqLibraryId(resultTO.rnaSeqLibraryId);
//    		result.setGeneId(resultTO.geneId);
//    		result.setExpressionId(resultTO.expressionId);
//    		result.setExpressionConfidence(resultTO.expressionConfidence);
//    		result.setDetectionFlag(resultTO.detectionFlag);
//    		result.setLog2RPK(resultTO.log2RPK);
//    		result.setReadsCount(resultTO.readsCount);
//    	}
//    	return result;
//    }
}
