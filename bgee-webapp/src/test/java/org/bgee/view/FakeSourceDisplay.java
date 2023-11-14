package org.bgee.view;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.source.Source;

public class FakeSourceDisplay extends FakeParentDisplay implements SourceDisplay {

    public FakeSourceDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

	@Override
	public void displaySources(List<Source> sources) {
        this.out.println("Test source container");
	}

}
