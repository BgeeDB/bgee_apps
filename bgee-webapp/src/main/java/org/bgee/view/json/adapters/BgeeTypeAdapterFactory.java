package org.bgee.view.json.adapters;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.CommandData.ExpressionCallResponse;
import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.expressiondata.call.MultiGeneExprAnalysis;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.search.SearchMatch;
import org.bgee.model.search.SearchMatchResult;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * A {@code TypeAdapterFactory} notably used when we need to provide the {@code Gson} object
 * to a custom {@code TypeAdapter}.
 *
 * @author Frederic Bastian
 * @version Bgee 15 Oct. 2021
 * @since Bgee 13 Nov. 2015
 * @see StreamTypeAdapter
 * @see GeneMatchAdapter
 *
 */
public class BgeeTypeAdapterFactory implements TypeAdapterFactory {
    private final Function<String, String> urlEncodeFunction;
    private static final Logger log = LogManager.getLogger(BgeeTypeAdapterFactory.class.getName());
    /**
     * The {@code RequestParameters} corresponding to the current request to the webapp.
     */
//    private final RequestParameters requestParameters;
    private final Supplier<RequestParameters> rpSupplier;
    private final TypeAdaptersUtils utils;

    public BgeeTypeAdapterFactory(Function<String, String> urlEncodeFunction,
            Supplier<RequestParameters> rpSupplier, TypeAdaptersUtils utils) {
        this.urlEncodeFunction = urlEncodeFunction;
//        this.requestParameters = requestParameters;
        this.rpSupplier = rpSupplier;
        this.utils = utils;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        log.traceEntry("{}, {}", gson, typeToken);

        final Class<? super T> rawClass = typeToken.getRawType();

        if (Stream.class.isAssignableFrom(rawClass)) {
            //it is mandatory to cast the returned factory, the test isAssignableFrom
            //is not enough for the warning to disappear. Note that this is also the case
            //in Gson factory implementations
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new StreamTypeAdapter<>(gson);
            return log.traceExit(result);
        }
        if (Ontology.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new OntologyTypeAdapter<>(gson);
            return log.traceExit(result);
        }
        if (SearchMatchResult.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new SearchMatchResultTypeAdapter(gson);
            return log.traceExit(result);
        }
        if (SearchMatch.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new SearchMatchTypeAdapter<>(gson);
            return log.traceExit(result);
        }
        if (GeneHomologs.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new GeneHomologsTypeAdapter(gson, this.rpSupplier,
                    this.utils);
            return log.traceExit(result);
        }
        if (Gene.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new GeneTypeAdapter(gson, urlEncodeFunction,
                    this.utils);
            return log.traceExit(result);
        }
        if (GeneExpressionResponse.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new GeneExpressionResponseTypeAdapter(this.utils);
            return log.traceExit(result);
        }
        if (MultiGeneExprAnalysis.class.isAssignableFrom(rawClass) ) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new MultiGeneExprAnalysisTypeAdapter(this.utils);
            return log.traceExit(result);
        }
        if (AnatEntitySimilarityAnalysis.class.isAssignableFrom(rawClass)) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new AnatEntitySimilarityAnalysisTypeAdapter(gson, this.utils);
            return log.traceExit(result);
        }
        if (AffymetrixChip.class.isAssignableFrom(rawClass)) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new AffymetrixChipTypeAdapter(gson, this.utils);
            return log.traceExit(result);
        }
        if (AffymetrixProbeset.class.isAssignableFrom(rawClass)) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new AffymetrixProbesetTypeAdapter(gson);
            return log.traceExit(result);
        }
        if (ExpressionCallResponse.class.isAssignableFrom(rawClass)) {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new ExpressionCallResponseTypeAdapter(gson, this.utils);
            return log.traceExit(result);
        }

        //let Gson find somebody else
        return log.traceExit((TypeAdapter<T>) null);
    }
}
