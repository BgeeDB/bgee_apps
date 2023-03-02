package org.bgee.view;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.search.SearchMatchResult;

public class FakeSearchDisplay extends FakeParentDisplay implements SearchDisplay {

    public FakeSearchDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayExpasyResult(int count, String searchTerm) {
        this.out.println("Test search summary container");
    }

    @Override
    public void displayMatchesForGeneCompletion(Collection<String> matches) {
        
    }

    @Override
    public void displayDefaultSphinxSearchResult(String searchTerm,
            SearchMatchResult<?> result) {
        this.out.println("Test search anat. entity result");
    }
}
