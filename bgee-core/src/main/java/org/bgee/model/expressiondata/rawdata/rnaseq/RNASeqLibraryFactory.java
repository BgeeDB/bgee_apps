package org.bgee.model.expressiondata.rawdata.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A <code>Factory</code> responsible for instantiating <code>RNASeqLibrary</code> objects.
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqLibrary
 * @since Bgee 12
 */
public class RNASeqLibraryFactory extends Factory
{
	/**
	 * A <code>RNASeqLibraryDAO</code> used to query a data source, 
	 * to obtain <code>RNASeqLibraryTO</code> objects, 
	 * used to instantiate and populate <code>RNASeqLibrary</code> objects 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * <p>
	 * This <code>RNASeqLibraryDAO</code> is obtained using a <code>DAOFactory</code> 
	 * returned by the <code>#getDAOFactory()</code> method of the parent class. 
	 * 
	 * @see Factory#getDAOFactory()
	 */
	private final RNASeqLibraryDAO dao;
	
	/**
	 * Default constructor. 
	 */
    public RNASeqLibraryFactory()
    {
    	super();
    	this.dao = this.getDAOFactory().getRnaSeqLibraryDAO();
    }
    
    /**
	 * Get a <code>RNASeqLibrary</code> retrieved by its ID.
	 * 
	 * @param rnaSeqLibraryId 	A <code>String</code> representing the ID 
	 * 							of the RNA-Seq library to retrieve.
	 * @return 					A <code>RNASeqLibrary</code> object, 
	 * 							corresponding to the <code>rnaSeqlibraryId</code>.
	 */
	public RNASeqLibrary getRnaSeqLibraryById(String rnaSeqLibraryId) 
	{
		return this.createRnaSeqLibrary(this.dao.getRnaSeqLibraryById(rnaSeqLibraryId));
	}
    
    /**
	 * Create a <code>Collection</code> of <code>RNASeqLibrary</code> objects, 
	 * using a <code>Collection</code> of <code>TransferObject</code> objects 
	 * that must be castable to <code>RNASeqLibraryTO</code> objects. 
	 * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer). 
	 * 
	 * @param toCollection 	A <code>Collection</code> of <code>TransferObject</code> objects 
	 * 						castable to <code>RNASeqLibraryTO</code> objects, 
	 * 						obtained from a data source.
	 * @return 				A <code>Collection</code> of <code>RNASeqLibrary</code> objects 	
	 * 						corresponding to the provided <code>TransferObject</code>s.
	 */
    private Collection<RNASeqLibrary> getRnaSeqLibraries(Collection<TransferObject> toCollection)
    {
    	Collection<RNASeqLibrary> libList = new ArrayList<RNASeqLibrary>();
        Iterator<TransferObject> iterator = toCollection.iterator();
    	
    	while (iterator.hasNext()) {
    		libList.add(this.createRnaSeqLibrary((RNASeqLibraryTO) iterator.next()));
    	}
    	return libList;
    }
    
    /**
     * Instantiate and populate the attributes of a <code>RNASeqLibrary</code> object, 
     * using a <code>RNASeqLibraryTO</code> object, retrieved from a data source 
     * (<code>TransferObject</code>s are used to communicate between the <code>model</code> layer 
	 * and the <code>model.data</code> layer).
	 * 
     * @param libraryTO A <code>RNASeqLibraryTO</code> retrieved from a data source, 
     * 					used to populate the attributes of a <code>RNASeqLibrary</code> object.
     * @return			A <code>RNASeqLibrary</code> object newly instantiated, 
     * 					with attributes set using the <code>RNASeqLibraryTO</code> object. 
     */
    private RNASeqLibrary createRnaSeqLibrary(RNASeqLibraryTO libraryTO)
    {
    	RNASeqLibrary library = null;
    	if (libraryTO != null) {
    		library = new RNASeqLibrary();
    		library.setId(libraryTO.id);
    		library.setSecondaryLibraryId(libraryTO.secondaryLibraryId);
    		library.setRnaSeqExperimentId(libraryTO.rnaSeqExperimentId);
    		library.setPlatformId(libraryTO.platformId);
    		library.setLog2RPKThreshold(libraryTO.log2RPKThreshold);
    		library.setAllGenesPercentPresent(libraryTO.allGenesPercentPresent);
    		library.setProteinCodingGenesPercentPresent(libraryTO.proteinCodingGenesPercentPresent);
    		library.setIntronicRegionsPercentPresent(libraryTO.intronicRegionsPercentPresent);
    		library.setIntergenicRegionsPercentPresent(libraryTO.intergenicRegionsPercentPresent);
    		library.setAllReadsCount(libraryTO.allReadsCount);
    		library.setUsedReadsCount(libraryTO.usedReadsCount);
    		library.setAlignedReadsCount(libraryTO.alignedReadsCount);
    		library.setMinReadLength(libraryTO.minReadLength);
    		library.setMaxReadLength(libraryTO.maxReadLength);
    		library.setLibraryType((libraryTO.libraryType.equals("single") ? 
    				               RNASeqLibrary.LibraryType.SINGLE : RNASeqLibrary.LibraryType.PAIRED));
    		library.setOrganId(libraryTO.organId);
    		library.setStageId(libraryTO.stageId);
    	}
    	return library;
    }
}
