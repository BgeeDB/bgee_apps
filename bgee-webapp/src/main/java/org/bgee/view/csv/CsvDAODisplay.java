package org.bgee.view.csv;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.view.DAODisplay;
import org.bgee.view.ViewFactory;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;

/**
 * Implementation of {@code DAODisplay} for CSV rendering.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13
 */
public class CsvDAODisplay extends CsvParentDisplay implements DAODisplay {
    private final static Logger log = LogManager.getLogger(CsvDAODisplay.class.getName());

    protected CsvDAODisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory factory, Delimiter delimiter) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory, delimiter);
    }
    
    @Override
    public <T extends Enum<T> & DAO.Attribute, U extends TransferObject> void displayTOs(
            List<T> attributes, DAOResultSet<U> resultSet) throws IllegalStateException {
        log.entry(attributes, resultSet);
        
        try (final ICsvBeanWriter beanWriter = new CsvBeanWriter(this.getOut(), this.csvPref)) {
            
            String[] header = attributes.stream().map(attr -> attr.toString()).toArray(String[]::new);
            
            this.startDisplay();
            beanWriter.writeHeader(header);
            
            final String[] nameMapping = attributes.stream().map(attr -> attr.getTOFieldName())
                    .toArray(String[]::new);
            while (resultSet.next()) {
                beanWriter.write(resultSet.getTO(), nameMapping);
            }
            beanWriter.flush();
            
            this.endDisplay();
        } catch (IOException e) {
            log.catching(e);
            throw log.throwing(new IllegalStateException("Cannot write CSV response", e));
        }
        
        log.exit();
    }
}
