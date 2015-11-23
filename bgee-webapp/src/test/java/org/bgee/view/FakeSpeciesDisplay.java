package org.bgee.view;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.species.Species;

/**
 * Fake view used for tests related to species display. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov 2015
 * @since   Bgee 13
 */
public class FakeSpeciesDisplay extends FakeParentDisplay implements SpeciesDisplay {

    public FakeSpeciesDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void sendSpeciesResponse(Set<Species> species) {
        this.out.println("Test topAnat container");
    }

}
