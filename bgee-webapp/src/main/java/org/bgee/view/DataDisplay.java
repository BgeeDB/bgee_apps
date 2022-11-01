package org.bgee.view;

import java.util.List;

import org.bgee.controller.CommandData.DataFormDetails;
import org.bgee.model.species.Species;

/**
 * Interface defining methods to be implemented by views related to data display.
 * 
 * @author  Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since   Bgee 15.0, Nov. 2022
 */
public interface DataDisplay {

    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails);
}
