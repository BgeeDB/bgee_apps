package org.bgee.view;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;

/**
 * Fake view used for tests related to species display. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 13
 */
public class FakeSpeciesDisplay extends FakeParentDisplay implements SpeciesDisplay {

    public FakeSpeciesDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displaySpeciesHomePage(List<Species> speciesList) {
        this.out.println("Test species container");
    }

    @Override
    public void sendSpeciesResponse(List<Species> species) {
        this.out.println("Test species container");
    }

    @Override
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup) {
        this.out.println("Test species container");
    }
}