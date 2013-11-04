package org.bgee.pipeline.species;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class responsible for inserting species and related NCBI taxonomy into 
 * the Bgee database. This class uses a file containing the IDs of the species 
 * to insert into Bgee, and a simplified version of the NCBI taxonomy stored 
 * as an ontology file. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertTaxa extends MySQLDAOUser {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertTaxa.class.getName());
    /**
     * Default constructor. 
     */
    public InsertTaxa() {
        super();
    }
}
