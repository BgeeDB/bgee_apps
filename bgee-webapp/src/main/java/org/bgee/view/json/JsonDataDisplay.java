package org.bgee.view.json;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        this.displayDataPage(speciesList, formDetails, null, null, null);
        log.traceExit();
    }
    public void displayDataPage(List<Species> speciesList, DataFormDetails formDetails,
            RawDataContainer rawDataContainer, RawDataCountContainer rawDataCountContainer,
            Collection<RawDataPostFilter> rawDataPostFilters) {
        log.traceEntry("{}, {}, {}, {}, {}", speciesList, formDetails,
                rawDataContainer, rawDataCountContainer, rawDataPostFilters);

        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
        if (speciesList != null && !speciesList.isEmpty()) {
            responseMap.put("speciesList", speciesList);
        }
        if (formDetails != null && formDetails.containsAnyInformation()) {
            responseMap.put("requestDetails", formDetails);
        }

        if (rawDataContainer != null || rawDataCountContainer != null ||
                rawDataPostFilters != null && !rawDataPostFilters.isEmpty()) {

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
                    RawDataPostFilter postFilter = getDataTypeRawDataPostFilter(rawDataPostFilters, dt);
                    if (postFilter != null) {
                        filterMap.put(dt, postFilter);
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
            if (!filterMap.isEmpty()) {
                responseMap.put("filters", filterMap);
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

    private static RawDataPostFilter getDataTypeRawDataPostFilter(Collection<RawDataPostFilter> rawDataPostFilters,
            DataType dt) {
        log.traceEntry("{}, {}", rawDataPostFilters, dt);
        if (rawDataPostFilters == null) {
            return log.traceExit((RawDataPostFilter) null);
        }
        Set<RawDataPostFilter> dtPostFilters = rawDataPostFilters.stream()
                .filter(f -> dt == f.getRequestedDataType())
                .collect(Collectors.toSet());
        if (dtPostFilters.isEmpty()) {
            return log.traceExit((RawDataPostFilter) null);
        }
        if (dtPostFilters.size() > 1) {
            throw log.throwing(new IllegalStateException(
                    "Only one RawDataPostFilter per DataType is allowed"));
        }
        return log.traceExit(dtPostFilters.iterator().next());
    }
}
