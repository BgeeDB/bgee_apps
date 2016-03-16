package org.bgee.view.csv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DAODisplay;
import org.bgee.view.ViewFactory;

/**
 * Implementation of {@code DAODisplay} for CSV rendering.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13
 */
public class CsvDAODisplay extends CsvParentDisplay implements DAODisplay {

    protected CsvDAODisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory factory, Delimiter delimiter) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory, delimiter);
    }
    
}
