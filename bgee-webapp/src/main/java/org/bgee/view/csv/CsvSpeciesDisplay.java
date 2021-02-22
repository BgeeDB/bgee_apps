package org.bgee.view.csv;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.view.SpeciesDisplay;
import org.bgee.view.ViewFactory;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;

/**
 * Implementation of {@code SpeciesDisplay} for CSV/TSV rendering.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 13, Sep. 2016
 */
public class CsvSpeciesDisplay extends CsvParentDisplay implements SpeciesDisplay {
    private final static Logger log = LogManager.getLogger(CsvSpeciesDisplay.class.getName());

    protected CsvSpeciesDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory factory, Delimiter delimiter) 
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory, delimiter);
    }

    @Override
    public void displaySpeciesHomePage(List<Species> speciesList) {
        throw log.throwing(new UnsupportedOperationException("Not available for CSV display"));
    }

    @Override
    public void sendSpeciesResponse(List<Species> species) {
        log.entry(species);
        
        try (final ICsvMapWriter mapWriter = new CsvMapWriter(this.getOut(), this.csvPref)) {
            
            //Create header and SuperCSV Processors
            int colCount = 4 + DataType.values().length;
            String[] header = new String[colCount];
            CellProcessor[] processors = new CellProcessor[colCount];
            Map<DataType, Integer> dataTypeToHeader = new HashMap<>();
            header[0] = "ID";
            processors[0] = new NotNull(new Unique());
            header[1] = "GENUS";
            processors[1] = new NotNull();
            header[2] = "SPECIES_NAME";
            processors[2] = new NotNull();
            header[3] = "COMMON_NAME";
            processors[3] = null;
            //create one column per data type, to inform about data types available fo each species
            int i = 4;
            for (DataType type: DataType.values()) {
                header[i] = type.name();
                processors[i] = new NotNull(new FmtBool("T", "F"));
                dataTypeToHeader.put(type, i);
                i++;
            }
            
            this.startDisplay();
            mapWriter.writeHeader(header);
            
            for (Species spe: species) {
                //get data types available for this species
                Set<DataType> dataTypes = spe.getDataTypesByDataSourcesForData().values().stream()
                        .flatMap(c -> c.stream()).collect(Collectors.toSet());
                
                //start creating a Species Map for rendering
                final Map<String, Object> speMap = new HashMap<String, Object>();
                speMap.put(header[0], spe.getId());
                speMap.put(header[1], spe.getGenus());
                speMap.put(header[2], spe.getSpeciesName());
                speMap.put(header[3], spe.getName());
                //data type columns
                for (Entry<DataType, Integer> dataTypeIndex: dataTypeToHeader.entrySet()) {
                    //do we have data of this type for this species?
                    speMap.put(header[dataTypeIndex.getValue()], dataTypes.contains(dataTypeIndex.getKey()));
                }
                
                mapWriter.write(speMap, header, processors);
            }
            //warning, it is needed to flush the writer before calling this.endDisplay()
            mapWriter.flush();
            
            this.endDisplay();
        } catch (IOException e) {
            log.catching(e);
            throw log.throwing(new IllegalStateException("Cannot write CSV response", e));
        }
        
        log.traceExit();
    }

    @Override
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup) {
        throw log.throwing(new UnsupportedOperationException("Not available for CSV display"));
    }
}
