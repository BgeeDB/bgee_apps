package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A <code>EntityFactory</code> responsible for instantiating <code>RNASeqResult</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqResult
 * @since Bgee 12
 */
public class RNASeqResultFactory extends EntityFactory
{
	/**
	 * A <code>RNASeqResultDAO</code> used to query a data source, 
	 * to obtain <code>RNASeqResultTO</code> objects, 
	 * used to instantiate and populate <code>RNASeqResult</code> objects 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * <p>
	 * This <code>RNASeqResultDAO</code> is obtained using a <code>DAOFactory</code> 
	 * returned by the <code>#getDAOFactory()</code> method of the parent class. 
	 * 
	 * @see EntityFactory#getDAOFactory()
	 */
	private final RNASeqResultDAO dao;
	
	/**
	 * Default constructor. 
	 */
    public RNASeqResultFactory()
    {
    	super();
    	this.dao = this.getDAOFactory().getRnaSeqResultDAO();
    }
    
    /**
	 * Obtain a <code>Collection</code> of <code>RNASeqResult</code>s, 
	 * retrieved from an expression query. 
	 * 
	 * @param expressionParam 	A <code>DataTypeTO</code> used to define the expression parameters 
	 * 							(choice of data type, data quality, etc.).
	 * @param multiSpeciesTO 	A <code>MultiSpeciesTO</code> storing the parameters of the expression query 
	 * 							related to the species, the organs, the stages. 
	 * @return 					A <code>Collection</code> of <code>RNASeqResult</code>s, 
	 * 							retrieved thanks to an expression query parameterized using 
	 * 							<code>expressionParam</code> and <code>multiSpeciesTO</code>.
	 */
    public Collection<RNASeqResult> getRnaSeqResultsByExpression(DataTypeTO expressionParam,
			MultiSpeciesTO multiSpeciesTO) 
	{
		return this.getRnaSeqResults(this.dao.getRnaSeqResultByExpression(
				expressionParam, multiSpeciesTO));
	}
    
    /**
	 * Create a <code>Collection</code> of <code>RNASeqResult</code> objects, 
	 * using a <code>Collection</code> of <code>TransferObject</code> objects 
	 * that must be castable to <code>RNASeqResultTO</code> objects. 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * 
	 * @param toCollection 	A <code>Collection</code> of <code>TransferObject</code> objects 
	 * 						castable to <code>RNASeqResultTO</code> objects, 
	 * 						obtained from a data source.
	 * @return 				A <code>Collection</code> of <code>RNASeqResult</code> objects 	
	 * 						corresponding to the provided <code>TransferObject</code>s.
	 */
    private Collection<RNASeqResult> getRnaSeqResults(Collection<TransferObject> toCollection)
    {
    	Collection<RNASeqResult> resultList = new ArrayList<RNASeqResult>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		resultList.add(this.createRnaSeqResult((RNASeqResultTO) iterator.next()));
    	}
    	return resultList;
    }
    
    /**
     * Instantiate and populate the attributes of a <code>RNASeqResult</code> object, 
     * using a <code>RNASeqResultTO</code> object, retrieved from a data source 
     * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer).
	 * 
     * @param libraryTO A <code>RNASeqResultTO</code> retrieved from a data source, 
     * 					used to populate the attributes of a <code>RNASeqResult</code> object.
     * @return			A <code>RNASeqResult</code> object newly instantiated, 
     * 					with attributes set using the <code>RNASeqResultTO</code> object. 
     */
    private RNASeqResult createRnaSeqResult(RNASeqResultTO resultTO)
    {
    	RNASeqResult result = null;
    	if (resultTO != null) {
    		result = new RNASeqResult();
    		
    		result.setId(resultTO.id);
    		result.setRnaSeqLibraryId(resultTO.rnaSeqLibraryId);
    		result.setGeneId(resultTO.geneId);
    		result.setExpressionId(resultTO.expressionId);
    		result.setExpressionConfidence(resultTO.expressionConfidence);
    		result.setDetectionFlag(resultTO.detectionFlag);
    		result.setLog2RPK(resultTO.log2RPK);
    		result.setReadsCount(resultTO.readsCount);
    	}
    	return result;
    }
}
