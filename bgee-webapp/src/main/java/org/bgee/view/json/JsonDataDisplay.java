package org.bgee.view.json;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.CommandData.DataFormDetails;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.Assay;
import org.bgee.model.expressiondata.rawdata.Experiment;
import org.bgee.model.expressiondata.rawdata.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.RawDataCountContainer;
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

    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails,
            RawDataContainer rawDataContainer, RawDataCountContainer rawDataCountContainer) {
        log.traceEntry("{}, {}, {}, {}", speciesList, formDetails,
                rawDataContainer, rawDataCountContainer);

        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
        if (speciesList != null && !speciesList.isEmpty()) {
            responseMap.put("speciesList", speciesList);
        }
        if (formDetails != null && formDetails.containsAnyInformation()) {
            responseMap.put("requestDetails", formDetails);
        }

        if (rawDataContainer != null || rawDataCountContainer != null) {
            //The Sets returned by rawDataContainer are backed-up by a LinkedHashSet,
            //iteration order is guaranteed
            LinkedHashMap<DataType, Set<?>> resultMap = new LinkedHashMap<>();
            LinkedHashMap<DataType, LinkedHashMap<String, Integer>> resultCountMap =
                    new LinkedHashMap<>();
            final String expCountKey = "experimentCount";
            final String assayCountKey = "assayCount";
            final String callCountKey = "callCount";
            for (DataType dt: EnumSet.allOf(DataType.class)) {
                switch (dt) {
                //It is important to start from calls, then assay, then experiments,
                //since if calls are populated, assays and experiments are as well,
                //and if assays are populated, experiments are as well.
                //But we are only interested in the most precise result.
                //And "null" means "info not requested", empty Collection means "no results".
                case FULL_LENGTH:
                    break;
                case RNA_SEQ:
                    break;
                case AFFYMETRIX:
                    if (rawDataContainer != null) {
                        if (rawDataContainer.getAffymetrixCalls() != null) {
                            resultMap.put(dt, rawDataContainer.getAffymetrixCalls());
                        } else if (rawDataContainer.getAffymetrixAssays() != null) {
                            resultMap.put(dt, rawDataContainer.getAffymetrixAssays());
                        } else if (rawDataContainer.getAffymetrixExperiments() != null) {
                            resultMap.put(dt, rawDataContainer.getAffymetrixExperiments());
                        }
                    }
                    if (rawDataCountContainer != null) {
                        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
                        if (rawDataCountContainer.getAffymetrixExperimentCount() != null) {
                            counts.put(expCountKey, rawDataCountContainer.getAffymetrixExperimentCount());
                        }
                        if (rawDataCountContainer.getAffymetrixAssayCount() != null) {
                            counts.put(assayCountKey, rawDataCountContainer.getAffymetrixAssayCount());
                        }
                        if (rawDataCountContainer.getAffymetrixCallCount() != null) {
                            counts.put(callCountKey, rawDataCountContainer.getAffymetrixCallCount());
                        }
                        if (!counts.isEmpty()) {
                            resultCountMap.put(dt, counts);
                        }
                    }
                    break;
                case EST:
                    break;
                case IN_SITU:
                    break;
                default:
                    throw log.throwing(new IllegalStateException("Unsupported data type: " + dt));
                }
            }
            if (!resultMap.isEmpty()) {
                responseMap.put("results", resultMap);
            }
            if (!resultCountMap.isEmpty()) {
                responseMap.put("resultCount", resultCountMap);
            }
        }

        this.sendResponse("Data page", responseMap);
        log.traceExit();
    }

    public void displayExperimentPage(Experiment<?> experiment, LinkedHashSet<Assay<?>> assays,
            DataType dataType) {
        log.traceEntry("{}, {}, {}", experiment, assays, dataType);

        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
        responseMap.put("dataType", dataType);
        responseMap.put("experiment", experiment);
        responseMap.put("assays", assays);

        this.sendResponse("Experiment page", responseMap);
        log.traceExit();
    }
}
