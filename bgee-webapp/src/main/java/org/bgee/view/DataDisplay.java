package org.bgee.view;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.bgee.controller.CommandData.ColumnDescription;
import org.bgee.controller.CommandData.DataFormDetails;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.RawDataPostFilter;
import org.bgee.model.species.Species;

/**
 * Interface defining methods to be implemented by views related to data display.
 * 
 * @author  Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since   Bgee 15.0, Nov. 2022
 */
public interface DataDisplay {

    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails);

    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails,
            Map<DataType, List<ColumnDescription>> colDescriptions,
            RawDataContainer rawDataContainer, RawDataCountContainer rawDataCountContainer,
            Collection<RawDataPostFilter> rawDataPostFilters);

    public void displayExperimentPage(Experiment<?> experiment, LinkedHashSet<Assay<?>> assays,
            DataType dataType, List<ColumnDescription> columnDescriptions);
}
