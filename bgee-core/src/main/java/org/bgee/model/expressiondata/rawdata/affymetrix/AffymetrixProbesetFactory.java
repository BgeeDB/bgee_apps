package org.bgee.model.expressiondata.rawdata.affymetrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A <code>Factory</code> responsible for instantiating <code>AffymetrixProbeset</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixProbeset
 * @since Bgee 01
 */
public class AffymetrixProbesetFactory extends Factory
{
	/**
	 * An <code>AffymetrixProbesetDAO</code> used to query a data source, 
	 * to obtain <code>AffymetrixProbesetTO</code> objects, 
	 * used to instantiate and populate <code>AffymetrixProbeset</code> objects 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * <p>
	 * This <code>AffymetrixProbesetDAO</code> is obtained using a <code>DAOFactory</code> 
	 * returned by the <code>#getDAOFactory()</code> method of the parent class. 
	 * 
	 * @see Factory#getDAOFactory()
	 */
	private final AffymetrixProbesetDAO dao;
	
	/**
	 * Default constructor. 
	 */
    public AffymetrixProbesetFactory()
    {
    	super();
    	this.dao = this.getDAOFactory().getAffymetrixProbesetDAO();
    }

    /**
	 * Obtain a <code>Collection</code> of <code>AffymetrixProbeset</code>s, 
	 * retrieved from an expression query. 
	 * 
	 * @param expressionParam 	A <code>DataTypeTO</code> used to define the expression parameters 
	 * 							(choice of data type, data quality, etc.).
	 * @param multiSpeciesTO 	A <code>MultiSpeciesTO</code> storing the parameters of the expression query 
	 * 							related to the species, the organs, the stages. 
	 * @return 					A <code>Collection</code> of <code>AffymetrixProbeset</code>s, 
	 * 							retrieved thanks to an expression query parameterized using 
	 * 							<code>expressionParam</code> and <code>multiSpeciesTO</code>.
	 */
	public Collection<AffymetrixProbeset> getProbesetsByExpression(DataTypeTO expressionParam,
			MultiSpeciesTO multiSpeciesTO) 
	{
		return this.getAffymetrixProbesets(this.dao.getProbesetByExpression(
				expressionParam, multiSpeciesTO));
	}
    
	/**
	 * Create a <code>Collection</code> of <code>AffymetrixProbeset</code> objects, 
	 * using a <code>Collection</code> of <code>TransferObject</code> objects 
	 * that must be castable to <code>AffymetrixProbesetTO</code> objects. 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * 
	 * @param toCollection 	A <code>Collection</code> of <code>TransferObject</code> objects 
	 * 						castable to <code>AffymetrixProbesetTO</code> objects, 
	 * 						obtained from a data source.
	 * @return 				A <code>Collection</code> of <code>AffymetrixProbeset</code> objects 	
	 * 						corresponding to the provided <code>TransferObject</code>s.
	 */
    private Collection<AffymetrixProbeset> getAffymetrixProbesets(Collection<TransferObject> toCollection)
    {
    	Collection<AffymetrixProbeset> probesetList = new ArrayList<AffymetrixProbeset>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		probesetList.add(this.createAffymetrixProbeset((AffymetrixProbesetTO) iterator.next()));
    	}
    	return probesetList;
    }
    
    /**
     * Instantiate and populate the attributes of a <code>AffymetrixProbeset</code> object, 
     * using a <code>AffymetrixProbesetTO</code> object, retrieved from a data source 
     * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer).
	 * 
     * @param libraryTO A <code>AffymetrixProbesetTO</code> retrieved from a data source, 
     * 					used to populate the attributes of a <code>AffymetrixProbeset</code> object.
     * @return			A <code>AffymetrixProbeset</code> object newly instantiated, 
     * 					with attributes set using the <code>AffymetrixProbesetTO</code> object. 
     */
    private AffymetrixProbeset createAffymetrixProbeset(AffymetrixProbesetTO probesetTO)
    {
    	AffymetrixProbeset probeset = null;
    	if (probesetTO != null) {
    		probeset = new AffymetrixProbeset();
    		
    		probeset.setId(probesetTO.id);
    		probeset.setBgeeAffymetrixChipId(probesetTO.bgeeAffymetrixChipId);
    		probeset.setNormalizedSignalIntensity(probesetTO.normalizedSignalIntensity);
    		probeset.setGeneId(probesetTO.geneId);
    		probeset.setExpressionId(probesetTO.expressionId);
    		probeset.setExpressionConfidence(probesetTO.expressionConfidence);
    		probeset.setDetectionFlag(probesetTO.detectionFlag);
    	}
    	return probeset;
    }
}
