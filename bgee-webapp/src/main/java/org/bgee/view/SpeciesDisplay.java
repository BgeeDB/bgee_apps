package org.bgee.view;

import java.util.List;

import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;

/**
 * Interface that defines the methods a display for the species category, i.e. page=species
 * has to implements
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 13, Nov. 15
 */
public interface SpeciesDisplay {

    /**
     * Display the species home page.
     */
    public void displaySpeciesHomePage(List<Species> speciesList);

    /**
     * Display the page for several species.
     */
    public void sendSpeciesResponse(List<Species> species);
   
    /**
     * Display the one species page.
     */
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup);

}