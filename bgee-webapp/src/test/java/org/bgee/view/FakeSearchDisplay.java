package org.bgee.view;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.gene.GeneMatch;

public class FakeSearchDisplay extends FakeParentDisplay implements SearchDisplay {

    public FakeSearchDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

	@Override
	public void displayGeneCompletionByGeneList(Collection<GeneMatch> geneMatches, String searchTerm) {
        this.out.println("Test search container");
	}

}
