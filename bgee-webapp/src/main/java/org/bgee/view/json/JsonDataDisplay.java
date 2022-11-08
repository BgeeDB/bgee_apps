package org.bgee.view.json;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

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
        if (rawDataContainer != null) {
            LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> assayMap = null;
            LinkedHashMap<String, Object> callMap = null;
            if (RequestParameters.ACTION_RAW_DATA_ANNOTS.equals(this.getRequestParameters().getAction())) {
                assayMap = new LinkedHashMap<>();
                for (DataType dt: EnumSet.allOf(DataType.class)) {
                    switch (dt) {
                    case FULL_LENGTH:
                        break;
                    case RNA_SEQ:
                        break;
                    case AFFYMETRIX:
                        if (rawDataContainer.getAffymetrixAssays() != null) {
                            assayMap.put(DataType.AFFYMETRIX.getStringRepresentation(),
                                    rawDataContainer.getAffymetrixAssays());
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
            }
            if (RequestParameters.ACTION_PROC_EXPR_VALUES.equals(this.getRequestParameters().getAction())) {
                callMap = new LinkedHashMap<>();
                for (DataType dt: EnumSet.allOf(DataType.class)) {
                    switch (dt) {
                    case FULL_LENGTH:
                        break;
                    case RNA_SEQ:
                        break;
                    case AFFYMETRIX:
                        if (rawDataContainer.getAffymetrixCalls() != null) {
                            callMap.put(DataType.AFFYMETRIX.getStringRepresentation(),
                                    rawDataContainer.getAffymetrixCalls());
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
            }
            if (assayMap != null) {
                resultMap.put("rawDataAnnotations", assayMap);
            }
            if (callMap != null) {
                resultMap.put("processedExpressionValues", callMap);
            }
            if (!resultMap.isEmpty()) {
                responseMap.put("results", resultMap);
            }
        }

        if (rawDataCountContainer != null) {
            responseMap.put("resultCount", rawDataCountContainer);
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

        this.sendResponse("Data page", responseMap);
        log.traceExit();
    }
}
