package org.bgee.view;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.bgee.controller.CommandData.ColumnDescription;
import org.bgee.controller.CommandData.DataFormDetails;
import org.bgee.controller.CommandData.ExpressionCallResponse;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.call.ExpressionCallPostFilter;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;
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
            EnumMap<DataType, List<ColumnDescription>> colDescriptions,
            EnumMap<DataType, RawDataContainer<?, ?>> rawDataContainers,
            EnumMap<DataType, RawDataCountContainer> rawDataCountContainers,
            EnumMap<DataType, RawDataPostFilter> rawDataPostFilters);

    public void displayExprCallPage(List<Species> speciesList, DataFormDetails formDetails,
            List<ColumnDescription> colDescriptions, ExpressionCallResponse callresponse,
            Long callCount, ExpressionCallPostFilter postFilter);

    public void displayExperimentPage(Experiment<?> experiment, LinkedHashSet<Assay> assays,
            DataType dataType, List<ColumnDescription> columnDescriptions);
}
