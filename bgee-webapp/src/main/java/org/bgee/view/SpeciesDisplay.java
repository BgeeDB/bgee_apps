package org.bgee.view;

import java.util.List;

import org.bgee.model.species.Species;

/**
 * Interface that defines the methods a display for the species category, i.e. page=species
 * has to implements
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Sep. 2016
 * @since   Bgee 13 Nov. 15
 */
public interface SpeciesDisplay {

    /**
     * Display the species page.
     */
    public void sendSpeciesResponse(List<Species> species);
   
}