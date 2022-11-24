package org.bgee.view.json;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.CommandData.ColumnDescription;
import org.bgee.controller.CommandData.DataFormDetails;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainerWithExperiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCountContainerWithExperiment;
import org.bgee.model.expressiondata.rawdata.RawDataPostFilter;
import org.bgee.model.species.Species;
import org.bgee.view.DataDisplay;
import org.bgee.view.JsonHelper;

/**
 * JSON implementation of {@link DataDisplay}.
 *
 * @author  Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since   Bgee 15.0, Nov. 2022
 */
public class JsonDataDisplay extends JsonParentDisplay implements DataDisplay {
    private final static Logger log = LogManager.getLogger(JsonDataDisplay.class.getName());

    public JsonDataDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, JsonHelper jsonHelper, JsonFactory factory)
                    throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails) {
        log.traceEntry("{}, {}", speciesList, formDetails);
        this.displayDataPage(speciesList, formDetails, null, null, null, null);
        log.traceExit();
    }
    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails,
            Map<DataType, List<ColumnDescription>> colDescriptions,
            Map<DataType, RawDataContainer<?, ?>> rawDataContainers,
            Map<DataType, RawDataCountContainer> rawDataCountContainers,
            Map<DataType, RawDataPostFilter> rawDataPostFilters) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", speciesList, formDetails, colDescriptions,
                rawDataContainers, rawDataCountContainers, rawDataPostFilters);

        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
        if (speciesList != null && !speciesList.isEmpty()) {
            responseMap.put("speciesList", speciesList);
        }
        if (formDetails != null && formDetails.containsAnyInformation()) {
            responseMap.put("requestDetails", formDetails);
        }
        if (colDescriptions != null && !colDescriptions.isEmpty()) {
            responseMap.put("columnDescriptions", colDescriptions);
        }

        if (rawDataContainers != null || rawDataCountContainers != null ||
                rawDataPostFilters != null) {

            //The Sets returned by rawDataContainer are backed-up by a LinkedHashSet,
            //iteration order is guaranteed
            LinkedHashMap<DataType, Set<?>> resultMap = new LinkedHashMap<>();
            //for counts
            final String expCountKey = "experimentCount";
            final String assayCountKey = "assayCount";
            final String callCountKey = "callCount";
            LinkedHashMap<DataType, LinkedHashMap<String, Integer>> resultCountMap =
                    new LinkedHashMap<>();
            //For filters
            LinkedHashMap<DataType, RawDataPostFilter> filterMap = new LinkedHashMap<>();

            for (DataType dt: EnumSet.allOf(DataType.class)) {
                //********** Raw data *************
                RawDataContainer<?, ?> rawDataContainer = rawDataContainers.get(dt);
                if (rawDataContainer != null) {
                    //It is important to start from calls, then assay, then experiments,
                    //since if calls are populated, assays and experiments are as well,
                    //and if assays are populated, experiments are as well.
                    //But we are only interested in the most precise result.
                    //And "null" means "info not requested", empty Collection means "no results".
                    if (rawDataContainer.getCalls() != null) {
                        resultMap.put(dt, rawDataContainer.getCalls());
                    } else if (rawDataContainer.getAssays() != null) {
                        resultMap.put(dt, rawDataContainer.getAssays());
                    } else if (rawDataContainer instanceof RawDataContainerWithExperiment &&
                            ((RawDataContainerWithExperiment<?, ?, ?>) rawDataContainer)
                            .getExperiments() != null) {
                        resultMap.put(dt, ((RawDataContainerWithExperiment<?, ?, ?>) rawDataContainer)
                                .getExperiments());
                    }
                }

                //********** Raw data counts *************
                RawDataCountContainer countContainer = rawDataCountContainers.get(dt);
                if (countContainer != null) {
                    LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
                    if (countContainer instanceof RawDataCountContainerWithExperiment &&
                            ((RawDataCountContainerWithExperiment) countContainer)
                            .getExperimentCount() != null) {
                        counts.put(expCountKey, ((RawDataCountContainerWithExperiment) countContainer)
                                .getExperimentCount());
                    }
                    if (countContainer.getAssayCount() != null) {
                        counts.put(assayCountKey, countContainer.getAssayCount());
                    }
                    if (countContainer.getCallCount() != null) {
                        counts.put(callCountKey, countContainer.getCallCount());
                    }
                    if (!counts.isEmpty()) {
                        resultCountMap.put(dt, counts);
                    }
                }

                //********** Post filters *************
                RawDataPostFilter postFilter = rawDataPostFilters.get(dt);
                if (postFilter != null) {
                    filterMap.put(dt, postFilter);
                }
            }

            if (!resultMap.isEmpty()) {
                responseMap.put("results", resultMap);
            }
            if (!resultCountMap.isEmpty()) {
                responseMap.put("resultCount", resultCountMap);
            }
            if (!filterMap.isEmpty()) {
                responseMap.put("filters", filterMap);
            }
        }

        this.sendResponse("Data page", responseMap);
        log.traceExit();
    }

    public void displayExperimentPage(Experiment<?> experiment, LinkedHashSet<Assay<?>> assays,
            DataType dataType, List<ColumnDescription> columnDescriptions) {
        log.traceEntry("{}, {}, {}, {}", experiment, assays, dataType, columnDescriptions);

        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
        responseMap.put("dataType", dataType);
        responseMap.put("columnDescriptions", columnDescriptions);
        responseMap.put("experiment", experiment);
        responseMap.put("assays", assays);

        this.sendResponse("Experiment page", responseMap);
        log.traceExit();
    }
}