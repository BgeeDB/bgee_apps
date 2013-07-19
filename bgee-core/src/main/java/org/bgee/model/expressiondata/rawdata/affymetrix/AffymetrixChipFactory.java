package org.bgee.model.expressiondata.rawdata.affymetrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * A <code>Factory</code> responsible for instantiating <code>AffymetrixChip</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixChip
 * @since Bgee 01
 */
public class AffymetrixChipFactory extends Factory
{
	/**
	 * An <code>AffymetrixChipDAO</code> used to query a data source, 
	 * to obtain <code>AffymetrixChipTO</code> objects, 
	 * used to instantiate and populate <code>AffymetrixChip</code> objects 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * <p>
	 * This <code>AffymetrixChipDAO</code> is obtained using a <code>DAOFactory</code> 
	 * returned by the <code>#getDAOFactory()</code> method of the parent class. 
	 * 
	 * @see Factory#getDAOFactory()
	 */
	private final AffymetrixChipDAO dao;
	
	/**
	 * Default constructor. 
	 */
    public AffymetrixChipFactory()
    {
    	super();
    	this.dao = this.getDAOFactory().getAffymetrixChipDAO();
    }


    /**
	 * Get an <code>AffymetrixChip</code> retrieved by its ID.
	 * 
	 * @param bgeeAffymetrixChipId 	A <code>String</code> representing the ID in the Bgee database 
	 * 								of the Affymetrix chip to retrieve.
	 * @return 						An <code>AffymetrixChip</code> object, 
	 * 								corresponding to the <code>bgeeAffymetrixChipId</code>.
	 */
	public AffymetrixChip getAffymetrixChipById(String bgeeAffymetrixChipId) 
	{
		return this.createAffymetrixChip(this.dao.getAffymetrixChipById(bgeeAffymetrixChipId));
	}
    
	/**
	 * Create a <code>Collection</code> of <code>AffymetrixChip</code> objects, 
	 * using a <code>Collection</code> of <code>TransferObject</code> objects 
	 * that must be castable to <code>AffymetrixChipTO</code> objects. 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * 
	 * @param toCollection 	A <code>Collection</code> of <code>TransferObject</code> objects 
	 * 						castable to <code>AffymetrixChipTO</code> objects, 
	 * 						obtained from a data source.
	 * @return 				A <code>Collection</code> of <code>AffymetrixChip</code> objects 	
	 * 						corresponding to the provided <code>TransferObject</code>s.
	 */
    private Collection<AffymetrixChip> getAffymetrixChips(Collection<TransferObject> toCollection)
    {
    	Collection<AffymetrixChip> chipList = new ArrayList<AffymetrixChip>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		chipList.add(this.createAffymetrixChip((AffymetrixChipTO) iterator.next()));
    	}
    	return chipList;
    }
    
    /**
     * Instantiate and populate the attribute of an <code>AffymetrixChip</code> object, 
     * using an <code>AffymetrixChipTO</code> object, retrieved from a data source 
     * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer).
	 * 
     * @param chipTO 	An <code>AffymetrixChipTO</code> retrieved from a data source, 
     * 					used to populate the attributes of an <code>AffymetrixChip</code> object.
     * @return			An <code>AffymetrixChip</code> object newly instantiated, 
     * 					with attributes set using the <code>AffymetrixChipTO</code> object. 
     */
    private AffymetrixChip createAffymetrixChip(AffymetrixChipTO chipTO)
    {
    	AffymetrixChip chip = null;
    	if (chipTO != null) {
    		chip = new AffymetrixChip();
    		chip.setId(chipTO.id);
    		chip.setAffymetrixChipId(chipTO.affymetrixChipId);
    		chip.setMicroarrayExperimentId(chipTO.microarrayExperimentId);
    		chip.setChipType(chipTO.chipType);
    		chip.setNormalizationType(chipTO.normalizationType);
    		chip.setDetectionType(chipTO.detectionType);
    		chip.setOrganId(chipTO.organId);
    		chip.setStageId(chipTO.stageId);
    	}
    	return chip;
    }
}
