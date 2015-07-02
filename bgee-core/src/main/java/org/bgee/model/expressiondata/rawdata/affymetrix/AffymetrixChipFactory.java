package org.bgee.model.expressiondata.rawdata.affymetrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * A {@code Factory} responsible for instantiating {@code AffymetrixChip} objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixChip
 * @since Bgee 01
 */
public class AffymetrixChipFactory //extends Factory
{
//	/**
//	 * An {@code AffymetrixChipDAO} used to query a data source, 
//	 * to obtain {@code AffymetrixChipTO} objects, 
//	 * used to instantiate and populate {@code AffymetrixChip} objects 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * <p>
//	 * This {@code AffymetrixChipDAO} is obtained using a {@code DAOFactory} 
//	 * returned by the {@code #getDAOFactory()} method of the parent class. 
//	 * 
//	 * @see Factory#getDAOFactory()
//	 */
//	private final AffymetrixChipDAO dao;
//	
//	/**
//	 * Default constructor. 
//	 */
//    public AffymetrixChipFactory()
//    {
//    	super();
//    	this.dao = this.getDAOFactory().getAffymetrixChipDAO();
//    }
//
//
//    /**
//	 * Get an {@code AffymetrixChip} retrieved by its ID.
//	 * 
//	 * @param bgeeAffymetrixChipId 	A {@code String} representing the ID in the Bgee database 
//	 * 								of the Affymetrix chip to retrieve.
//	 * @return 						An {@code AffymetrixChip} object, 
//	 * 								corresponding to the {@code bgeeAffymetrixChipId}.
//	 */
//	public AffymetrixChip getAffymetrixChipById(String bgeeAffymetrixChipId) 
//	{
//		return this.createAffymetrixChip(this.dao.getAffymetrixChipById(bgeeAffymetrixChipId));
//	}
//    
//	/**
//	 * Create a {@code Collection} of {@code AffymetrixChip} objects, 
//	 * using a {@code Collection} of {@code TransferObject} objects 
//	 * that must be castable to {@code AffymetrixChipTO} objects. 
//	 * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer). 
//	 * 
//	 * @param toCollection 	A {@code Collection} of {@code TransferObject} objects 
//	 * 						castable to {@code AffymetrixChipTO} objects, 
//	 * 						obtained from a data source.
//	 * @return 				A {@code Collection} of {@code AffymetrixChip} objects 	
//	 * 						corresponding to the provided {@code TransferObject}s.
//	 */
//    private Collection<AffymetrixChip> getAffymetrixChips(Collection<TransferObject> toCollection)
//    {
//    	Collection<AffymetrixChip> chipList = new ArrayList<AffymetrixChip>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		chipList.add(this.createAffymetrixChip((AffymetrixChipTO) iterator.next()));
//    	}
//    	return chipList;
//    }
//    
//    /**
//     * Instantiate and populate the attribute of an {@code AffymetrixChip} object, 
//     * using an {@code AffymetrixChipTO} object, retrieved from a data source 
//     * ({@code TransferObject}s are used to communicate between the {@code model} layer 
//	 * and the {@code model.data} layer).
//	 * 
//     * @param chipTO 	An {@code AffymetrixChipTO} retrieved from a data source, 
//     * 					used to populate the attributes of an {@code AffymetrixChip} object.
//     * @return			An {@code AffymetrixChip} object newly instantiated, 
//     * 					with attributes set using the {@code AffymetrixChipTO} object. 
//     */
//    private AffymetrixChip createAffymetrixChip(AffymetrixChipTO chipTO)
//    {
//    	AffymetrixChip chip = null;
//    	if (chipTO != null) {
//    		chip = new AffymetrixChip();
//    		chip.setId(chipTO.id);
//    		chip.setAffymetrixChipId(chipTO.affymetrixChipId);
//    		chip.setMicroarrayExperimentId(chipTO.microarrayExperimentId);
//    		chip.setChipType(chipTO.chipType);
//    		chip.setNormalizationType(chipTO.normalizationType);
//    		chip.setDetectionType(chipTO.detectionType);
//    		chip.setOrganId(chipTO.organId);
//    		chip.setStageId(chipTO.stageId);
//    	}
//    	return chip;
//    }
}
