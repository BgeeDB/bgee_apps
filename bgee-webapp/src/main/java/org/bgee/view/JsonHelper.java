package org.bgee.view;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandGene.GeneExpressionResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.model.NamedEntity;
import org.bgee.model.XRef;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExpressionLevelInfo;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.multispecies.MultiSpeciesCondition;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.MultiGeneExprAnalysis;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneHomologs;
import org.bgee.model.gene.GeneMatch;
import org.bgee.model.gene.GeneXRef;
import org.bgee.model.job.Job;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.model.topanat.TopAnatResults.TopAnatResultRow;
import org.bgee.view.json.JsonParentDisplay;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles the serialization of objects to JSON.
 * Current implementation rely on the google gson library.
 *
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Oct. 2015
 */
public class JsonHelper {
    
    private static final Logger log = LogManager.getLogger(JsonHelper.class.getName());
    
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
    private static class BgeeTypeAdapterFactory implements TypeAdapterFactory {
        private final Function<String, String> urlEncodeFunction;
        /**
         * The {@code RequestParameters} corresponding to the current request to the webapp.
         */
        private final RequestParameters requestParameters;
        private final Supplier<RequestParameters> rpSupplier;

        public BgeeTypeAdapterFactory(Function<String, String> urlEncodeFunction,
                RequestParameters requestParameters, Supplier<RequestParameters> rpSupplier) {
            this.urlEncodeFunction = urlEncodeFunction;
            this.requestParameters = requestParameters;
            this.rpSupplier = rpSupplier;
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
            if (GeneMatch.class.isAssignableFrom(rawClass) ) {
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new GeneMatchAdapter(gson);
                return log.traceExit(result);
            }
            if (GeneHomologs.class.isAssignableFrom(rawClass) ) {
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new GeneHomologsAdapter(gson, this.rpSupplier);
                return log.traceExit(result);
            }
            if (Gene.class.isAssignableFrom(rawClass) ) {
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new GeneAdapter(gson, urlEncodeFunction);
                return log.traceExit(result);
            }
            if (GeneExpressionResponse.class.isAssignableFrom(rawClass) ) {
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new GeneExpressionResponseAdapter(gson);
                return log.traceExit(result);
            }
            if (MultiGeneExprAnalysis.class.isAssignableFrom(rawClass) ) {
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new MultiGeneExprAnalysisAdapter(gson);
                return log.traceExit(result);
            }

            //let Gson find somebody else
            return log.traceExit((TypeAdapter<T>) null);
        }
    }
    /**
     * A {@code TypeAdapter} for {@code Stream}s, capable of correctly dumping 
     * the iterated elements, by retrieving the correct {@code TypeAdapter} corresponding to 
     * their actual type. Recursion is not a problem ({@code Stream} of {@code Stream}s).
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13 Nov. 2015
     * @see BgeeTypeAdapterFactory
     *
     * @param <T>   The type of the elements of the {@code Stream} to be dumped. 
     */
    private static final class StreamTypeAdapter<T> extends TypeAdapter<Stream<T>> {
        /**
         * The {@code Gson} object used to provide the appropriate {@code TypeAdapter}s 
         * for the elements of the {@code Stream} to dump.
         */
        private final Gson gson;
        
        //see https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/Gson.html#getDelegateAdapter%28com.google.gson.TypeAdapterFactory,%20com.google.gson.reflect.TypeToken%29
        //see https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapterFactory.html
        private StreamTypeAdapter(Gson gson) {
            this.gson = gson;
        }
        
        @Override
        public void write(JsonWriter out, Stream<T> stream) throws IOException {
            log.traceEntry("{}, {}", out, stream);
            if (stream == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            log.trace("Start writing Stream elements.");
            out.beginArray();
            
            //Use the Stream Iterator to be able to throw checked Exceptions
            Iterator<T> iterator = stream.iterator();
            while (iterator.hasNext()) {
                T e = iterator.next();
                if (e == null) {
                    out.nullValue();
                    continue;
                }
                
                //We need to retrieve the correct TypeAdapter at each iteration.
                //See the javadoc of BgeeTypeAdapterFactory for the motivations.
                //Note that we could find the underlying Adapter only at first iteration, 
                //but maybe the Stream contains elements of mix-types, 
                //and we can't use the generic type declaration to decide which Adapter to use. 
                //So, we always use the Adapter corresponding to the actual type of the element, 
                //not to its declared type.
                
                //it is a mandatory to cast the returned factory, note that this is also the case 
                //in Gson factory implementations
                @SuppressWarnings("unchecked")
                TypeAdapter<T> typeAdapter = (TypeAdapter<T>) gson.getAdapter(e.getClass());
                typeAdapter.write(out, e);
            }
            
            log.trace("End writing Stream elements.");
            out.endArray();
            log.traceExit();
        }
        
        @Override
        public Stream<T> read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Stream."));
        } 
    }
    
    /**
     * A {@code TypeAdapter} to read/write {@code DownloadFile}s in JSON. This adapter 
     * is needed to prepend the actual download file root directory to relative paths 
     * returned by {@link DownloadFile#getPath()}, and to correctly manage 
     * {@link DownloadFile.CategoryEnum}.
     * <p>
     * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
     * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
     * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
     */
    private static final class DownloadFileTypeAdapter extends TypeAdapter<DownloadFile> {
        /**
         * The {@code BgeeProperties} to retrieve parameters from.
         */
        private final BgeeProperties props;
        /**
         * @param props The {@code BgeeProperties} to retrieve parameters from.
         */
        private DownloadFileTypeAdapter(BgeeProperties props) {
            assert props != null;
            this.props = props;
        }
        @Override
        public void write(JsonWriter out, DownloadFile value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();
            
            //values with no modifications
            out.name("name").value(value.getName());
            out.name("size").value(value.getSize());
            out.name("speciesDataGroupId").value(value.getSpeciesDataGroupId());
            //values with modifications
            //rewrite path to point to root directory of download files
            out.name("path").value(this.props.getDownloadRootDirectory() + value.getPath());
            //write stringRepresentation of Category
            out.name("category").value(value.getCategory() == null? "": 
                value.getCategory().getStringRepresentation());
            out.name("conditionParameters");
            out.beginArray();
            EnumSet<CallService.Attribute> condParams = value.getConditionParameters().isEmpty()?
                    CallService.Attribute.getAllConditionParameters():
                        EnumSet.copyOf(value.getConditionParameters());
            for (CallService.Attribute condParam: condParams) {
                out.value(condParam.getCondParamName());
            }
            out.endArray();
            out.endObject();
            log.traceExit();
        }
        
        @Override
        public DownloadFile read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for DownloadFile."));
        } 
    }

    /**
     * A {@code TypeAdapter} to read/write {@code DownloadFile}s in JSON. This adapter 
     * is needed to prepend the actual download file root directory to relative paths 
     * returned by {@link DownloadFile#getPath()}, and to correctly manage 
     * {@link DownloadFile.CategoryEnum}.
     * <p>
     * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
     * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
     * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
     */
    private static final class RequestParametersTypeAdapter extends TypeAdapter<RequestParameters> {
        private RequestParametersTypeAdapter() {
            
        }
        @Override
        public void write(JsonWriter out, RequestParameters rqParams) throws IOException {
            log.traceEntry("{}, {}", out, rqParams);
            if (rqParams == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            log.trace("Start writing object RequestParameters.");
            out.beginObject();
            //Stream not used because the out methods throw checked Exceptions, 
            //not following functional interface signatures. 
            URLParameters.Parameter<?> displayRpParam = rqParams.getUrlParametersInstance()
                    .getParamDisplayRequestParams();
            for (URLParameters.Parameter<?> param: rqParams.getUrlParametersInstance().getList()) {
                log.trace("Iterating parameter {}", param);
                //we don't display the parameter requesting to display the parameters, 
                //will always be true :p
                if (param.equals(displayRpParam)) {
                    log.trace("Skipping parameter {}", displayRpParam);
                    continue;
                }
                
                List<?> values = rqParams.getValues(param);
                if (values == null) {
                    log.trace("No value stored.");
                    continue;
                }
                out.name(param.getName());
                log.trace("Printing parameter name {}", param.getName());
                if (param.allowsMultipleValues() || param.allowsSeparatedValues()) {
                    log.trace("Allows multiple or separated values, start printing Array.");
                    out.beginArray();
                }
                boolean hasValue = false;
                for (Object value: values) {
                    if (value == null) {
                        log.trace("Skip null value.");
                        continue;
                    }
                    log.trace("Printing parameter value {}", value.toString());
                    out.value(value.toString());
                    hasValue = true;
                }
                if (param.allowsMultipleValues() || param.allowsSeparatedValues()) {
                    log.trace("Allows multiple or separated values, end printing Array.");
                    out.endArray();
                } else if (!hasValue) {
                    out.nullValue();
                }
            }

            log.trace("End writing object RequestParameters.");
            out.endObject();
            log.traceExit();
        }
        
        @Override
        public RequestParameters read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RequestParameters."));
        } 
    }
    
    private final class TopAnatResultsTypeAdapter extends TypeAdapter<TopAnatResults> {
        /**
         * The {@code RequestParameters} corresponding to the current request to the webapp.
         */
        private final RequestParameters requestParameters;
        /**
         * @param requestParameters The {@code RequestParameters} corresponding to the current request 
         *                          to the webapp.
         */
        private TopAnatResultsTypeAdapter(RequestParameters requestParameters) {
            if (requestParameters == null) {
                this.requestParameters = null;
            } else {
                this.requestParameters = requestParameters.cloneWithStorableParameters();
                this.requestParameters.setPage(RequestParameters.PAGE_TOP_ANAT);
                this.requestParameters.setAction(RequestParameters.ACTION_TOP_ANAT_DOWNLOAD);
            }
        }
        @Override
        public void write(JsonWriter out, TopAnatResults results) throws IOException {
            log.traceEntry("{}, {}", out, results);
            if (results == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            if (this.requestParameters == null) {
                throw log.throwing(new IllegalStateException("It is not possible to determine "
                        + "the URL for downloading result file."));
            }
            
            log.trace("Start writing object TopAnatResults.");
            out.beginObject();
            
            RequestParameters clonedRps = this.requestParameters.cloneWithAllParameters();
            clonedRps.addValue(this.requestParameters.getUrlParametersInstance().getParamAnalysisId(), 
                    results.getTopAnatParams().getKey());
            out.name("zipFile").value(clonedRps.getRequestURL());
            out.name("devStageId").value(results.getTopAnatParams().getDevStageId());
            out.name("callType").value(results.getTopAnatParams().getCallType().toString());
            
            out.name("results");
            out.beginArray();
            
            for (TopAnatResultRow row: results.getRows()) {
                out.beginObject();
                
                out.name("anatEntityId").value(row.getAnatEntitiesId());
                out.name("anatEntityName").value(row.getAnatEntitiesName());
                out.name("annotated").value(row.getAnnotated());
                out.name("significant").value(row.getSignificant());
                out.name("expected");
                if (Double.isInfinite(row.getExpected()) || Double.isNaN(row.getExpected())) {
                    out.value("NA");
                } else {
                    out.value(row.getExpected());
                }
                out.name("foldEnrichment");
                if (Double.isInfinite(row.getEnrich()) || Double.isNaN(row.getEnrich())) {
                    out.value("NA");
                } else {
                    out.value(row.getEnrich());
                }
                out.name("pValue").value(row.getPval());
                out.name("FDR").value(row.getFdr());
                
                out.endObject();
            }
            
            out.endArray();

            log.trace("End writing object TopAnatResults.");
            out.endObject();
            log.traceExit();
        }

        @Override
        public TopAnatResults read(JsonReader in) throws IOException {
          //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for TopAnatResults."));
        }
        
    }

    /**
     * A {@code TypeAdapter} to read/write {@code Job}s in JSON. This adapter 
     * is needed to not display some information, notably about the running {@code Thread} 
     * or the {@code Job} pool.
     * <p>
     * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
     * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
     * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
     */
    private static final class JobTypeAdapter extends TypeAdapter<Job> {
        
        @Override
        public void write(JsonWriter out, Job value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();
            
            //values with no modifications
            out.name("id").value(value.getId());
            out.name("name").value(value.getName());
            out.name("userId").value(value.getUserId());
            out.name("started").value(value.isStarted());
            out.name("terminated").value(value.isTerminated());
            out.name("successful").value(value.isSuccessful());
            out.name("interruptRequested").value(value.isInterruptRequested());
            out.name("released").value(value.isReleased());
            out.name("taskCount").value(value.getTaskCount());
            out.name("currentTaskIndex").value(value.getCurrentTaskIndex());
            out.name("currentTaskName").value(value.getCurrentTaskName());
            
            
            out.endObject();
            log.traceExit();
        }
        
        @Override
        public Job read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Job."));
        } 
    }

    /**
     * A {@code TypeAdapter} to read/write {@code GeneMatch}s in JSON. It is needed because
     * the {@code getTerm()} method of {@code GeneMatch} is not always filled, and this is the attribute
     * that GSON would use by default. With this {@code TypeAdapter} we can always use the method
     * {@code getMatch()}.
     */
    private static final class GeneMatchAdapter extends TypeAdapter<GeneMatch> {
        private final Gson gson;

        private GeneMatchAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, GeneMatch value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();

            out.name("gene");
            this.gson.getAdapter(Gene.class).write(out, value.getGene());
            out.name("match").value(value.getMatch());
            out.name("matchSource").value(value.getMatchSource().toString().toLowerCase());

            out.endObject();
            log.traceExit();
        }

        @Override
        public GeneMatch read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for GeneMatch."));
        }
    }

    /**
     * A {@code TypeAdapter} to read/write {@code GeneHomologs}s in JSON. It is notably to be able
     * to call the method {@code getXRefUrl()}.
     */
    private static final class GeneXRefAdapter extends TypeAdapter<GeneXRef> {

        private final Function<String, String> urlEncodeFunction;
        private GeneXRefAdapter(Function<String, String> urlEncodeFunction) {
            this.urlEncodeFunction = urlEncodeFunction;
        }

        @Override
        public void write(JsonWriter out, GeneXRef value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();

            writeSimplifiedXRef(out, value, urlEncodeFunction);

            //Simplified display of Source inside XRefs
            out.name("source");
            out.beginObject();
            writeSimplifiedSource(out, value.getSource());
            out.endObject();

            out.endObject();
            log.traceExit();
        }

        @Override
        public GeneXRef read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for GeneXRef."));
        }
    }

    /**
     * A {@code TypeAdapter} to read/write {@code GeneHomologs}s in JSON. It is a complex object
     * notably with {@code Map}s.
     */
    private static final class GeneHomologsAdapter extends TypeAdapter<GeneHomologs> {
        //TODO: refactor with comparator in HtmlGeneDisplay
        private final static Comparator<Gene> GENE_HOMOLOGY_COMPARATOR = Comparator
                .<Gene, Integer>comparing(x -> x.getSpecies().getPreferredDisplayOrder(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(x -> x.getGeneId(), Comparator.nullsLast(String::compareTo));

        private final Gson gson;
        private final Supplier<RequestParameters> rpSupplier;

        private GeneHomologsAdapter(Gson gson, Supplier<RequestParameters> rpSupplier) {
            this.gson = gson;
            this.rpSupplier = rpSupplier;
        }

        @Override
        public void write(JsonWriter out, GeneHomologs value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();

            out.name("gene");
            writeSimplifiedGene(out, value.getGene(), false, null);

            out.name("orthologsByTaxon");
            this.writeHomologsByTaxon(out, value.getGene(), value.getOrthologsByTaxon());
            out.name("paralogsByTaxon");
            this.writeHomologsByTaxon(out, value.getGene(), value.getParalogsByTaxon());

            out.name("orthologyXRef");
            this.gson.getAdapter(GeneXRef.class).write(out, value.getOrthologyXRef());
            out.name("paralogyXRef");
            this.gson.getAdapter(GeneXRef.class).write(out, value.getParalogyXRef());

            out.endObject();
            log.traceExit();
        }

        @Override
        public GeneHomologs read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for GeneHomologs."));
        }

        private void writeHomologsByTaxon(JsonWriter out, Gene targetGene,
                LinkedHashMap<Taxon, Set<Gene>> homologsByTaxon) throws IOException {
            log.traceEntry("{}, {}, {}", out, targetGene, homologsByTaxon);
            out.beginArray();
            // all homologs of one taxon
            // We will display to each taxon level all genes from more recent taxon
            Set<Gene> allGenes = new HashSet<>();
            for (Entry<Taxon, Set<Gene>> e: homologsByTaxon.entrySet()) {
                out.beginObject();

                out.name("taxon");
                this.gson.getAdapter(Taxon.class).write(out, e.getKey());

                allGenes.addAll(e.getValue());
                // sort genes
                List<Gene> orderedHomologsWithDescendant = allGenes.stream()
                        .sorted(GENE_HOMOLOGY_COMPARATOR)
                        .collect(Collectors.toList());
                out.name("genes");
                out.beginArray();
                for (Gene gene: orderedHomologsWithDescendant) {
                    writeSimplifiedGene(out, gene, false, null);
                }
                out.endArray();

                //provide the parameters to produce a link to an expression comparison
                RequestParameters rp = this.rpSupplier.get();
                List<String> genesToCompare = orderedHomologsWithDescendant.stream()
                        .map(Gene::getGeneId).collect(Collectors.toList());
                genesToCompare.add(targetGene.getGeneId());
                rp.setGeneList(genesToCompare);
                out.name(JsonParentDisplay.STORABLE_PARAMS_INFO);
                this.gson.getAdapter(LinkedHashMap.class)
                .write(out, JsonParentDisplay.getStorableParamsInfo(rp));

                out.endObject();
            }
            out.endArray();
            log.traceExit();
        }
    }

    /**
     * A {@code TypeAdapter} to read/write {@code Gene}s in JSON. It is notably to group XRefs
     * per data source in the display.
     */
    private static final class GeneAdapter extends TypeAdapter<Gene> {
        //TODO: refactor with comparator in HtmlGeneDisplay
        private final static Comparator<XRef> X_REF_COMPARATOR = Comparator
                .<XRef, Integer>comparing(x -> x.getSource().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(x -> x.getSource().getName(), Comparator.nullsLast(String::compareTo))
                .thenComparing((XRef::getXRefId), Comparator.nullsLast(String::compareTo));

        private final Function<String, String> urlEncodeFunction;
        private final Gson gson;

        private GeneAdapter(Gson gson, Function<String, String> urlEncodeFunction) {
            this.gson = gson;
            this.urlEncodeFunction = urlEncodeFunction;
        }

        @Override
        public void write(JsonWriter out, Gene value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();

            out.name("geneId").value(value.getGeneId());
            out.name("name").value(value.getName());
            out.name("description").value(value.getDescription());
            out.name("synonyms");
            this.gson.getAdapter(Set.class).write(out, value.getSynonyms());
            out.name("xRefs");
            this.writeXRefsBySource(out, value.getXRefs());
            out.name("species");
            this.gson.getAdapter(Species.class).write(out, value.getSpecies());
            out.name("geneBioType");
            this.gson.getAdapter(GeneBioType.class).write(out, value.getGeneBioType());
            out.name("geneMappedToSameGeneIdCount").value(value.getGeneMappedToSameGeneIdCount());

            out.endObject();
            log.traceExit();
        }

        @Override
        public Gene read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Gene."));
        }

        private void writeXRefsBySource(JsonWriter out, Set<XRef> xRefs)
                throws IOException {
            log.traceEntry("{}, {}", out, xRefs);
            out.beginArray();

            LinkedHashMap<Source, List<XRef>> xRefsBySource = xRefs.stream()
                    .filter(x -> StringUtils.isNotBlank(x.getSource().getXRefUrl()))
                    .sorted(X_REF_COMPARATOR)
                    .collect(Collectors.groupingBy(XRef::getSource,
                            LinkedHashMap::new,
                            Collectors.toList()));
            for (Entry<Source, List<XRef>> e: xRefsBySource.entrySet()) {
                out.beginObject();

                out.name("source");
                out.beginObject();
                writeSimplifiedSource(out, e.getKey());
                out.endObject();

                out.name("xRefs");
                out.beginArray();
                for (XRef xRef: e.getValue()) {
                    out.beginObject();
                    writeSimplifiedXRef(out, xRef, urlEncodeFunction);
                    out.endObject();
                }
                out.endArray();

                out.endObject();
            }
            out.endArray();
            log.traceExit();
        }
    }

    /**
     * A {@code TypeAdapter} to read/write {@code GeneExpressionResponse}s in JSON. It is needed because
     * of the complexity of the object and because we want to fine tune the response.
     */
    private static final class GeneExpressionResponseAdapter extends TypeAdapter<GeneExpressionResponse> {
        private final Gson gson;

        private GeneExpressionResponseAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, GeneExpressionResponse value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();
            //Retrieve requested condition parameters
            EnumSet<CallService.Attribute> condParams = value.getCondParams();

            out.name("requestedDataTypes");
            out.beginArray();
            //For the gene page, for now we always consider all data types
            for (DataType d: EnumSet.allOf(DataType.class)) {
                out.value(d.getStringRepresentation());
            }
            out.endArray();

            out.name("requestedConditionParameters");
            out.beginArray();
            for (CallService.Attribute a: condParams) {
                out.value(a.getDisplayName());
            }
            out.endArray();

            EnumSet<DataType> dataTypesWithData = EnumSet.noneOf(DataType.class);
            out.name("calls");
            out.beginArray();
            for (ExpressionCall call: value.getCalls()) {
                Set<DataType> dataTypes = call.getCallData().stream().map(ExpressionCallData::getDataType)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
                dataTypesWithData.addAll(dataTypes);
                boolean highQualScore = false;
                if (!SummaryQuality.BRONZE.equals(call.getSummaryQuality()) && 
                        (dataTypes.contains(DataType.AFFYMETRIX) ||
                        dataTypes.contains(DataType.RNA_SEQ) ||
                        dataTypes.contains(DataType.FULL_LENGTH) ||
                        call.getMeanRank().compareTo(BigDecimal.valueOf(20000)) < 0)) {
                    highQualScore = true;
                }

                out.beginObject();

                out.name("condition");
                writeSimplifiedCondition(out, call.getCondition(), condParams);

                out.name("expressionScore");
                out.beginObject();
                out.name("expressionScore").value(call.getFormattedExpressionScore());
                out.name("expressionScoreConfidence");
                if (highQualScore) {
                    out.value("high");
                } else {
                    out.value("low");
                }
                out.endObject();

                //For the gene page, for now we always consider all data types, so we retrieve the FDR
                //computed by taking into account all data types
                String fdr = call.getPValueWithEqualDataTypes(EnumSet.allOf(DataType.class))
                        .getFormatedFDRPValue();
                out.name("fdr").value(fdr);

                out.name("dataTypesWithData");
//                EnumSet<DataType> dtWithData = call.getCallData().stream()
//                        .filter(c -> c.getDataPropagation() != null &&
//                            c.getDataPropagation().getCondParamCombinations().stream()
//                            .anyMatch(comb -> c.getDataPropagation().getTotalObservationCount(comb) > 0))
//                        .map(c -> c.getDataType())
//                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(DataType.class)));
                out.beginArray();
                for (DataType d: dataTypes) {
                    out.value(d.getStringRepresentation());
                }
                out.endArray();
                
                out.name("expressionState").value(call.getSummaryCallType().toString().toLowerCase());
                out.name("expressionQuality").value(call.getSummaryQuality().toString().toLowerCase());
                out.name("clusterIndex").value(value.getClustering().get(call));

                out.endObject();
            }
            out.endArray();

            if (!value.getCalls().isEmpty()) {
                assert !dataTypesWithData.isEmpty();
                out.name("gene");
                writeSimplifiedGene(out, value.getCalls().iterator().next().getGene(),
                        true, dataTypesWithData);
            }

            out.endObject();
            log.traceExit();
        }

        @Override
        public GeneExpressionResponse read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException(
                    "No custom JSON reader for GeneExpressionResponse."));
        }
    }

    /**
     * A {@code TypeAdapter} to read/write {@code MultiGeneExprAnalysis}s in JSON. It is needed because
     * of the complexity of the object and because we want to fine tune the response.
     */
    private static final class MultiGeneExprAnalysisAdapter extends TypeAdapter<MultiGeneExprAnalysis<?>> {
        private final Gson gson;

        private MultiGeneExprAnalysisAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, MultiGeneExprAnalysis<?> value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginArray();
            for (Entry<?, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts:
                value.getCondToCounts().entrySet()) {

                writeCondToCounts(out, condToCounts);
            }
            out.endArray();

            log.traceExit();
        }

        @Override
        public MultiGeneExprAnalysis<?> read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException(
                    "No custom JSON reader for MultiGeneExprAnalysis."));
        }

        private static void writeCondToCounts(JsonWriter out,
                Entry<?, MultiGeneExprAnalysis.MultiGeneExprCounts> condToCounts) throws IOException {
            log.traceEntry("{}, {}", out, condToCounts);
            out.beginObject();

            //Condition
            Object cond = condToCounts.getKey();
            if (cond instanceof MultiSpeciesCondition) {
                out.name("multiSpeciesCondition");
                writeSimplifiedMultiSpeciesCondition(out, (MultiSpeciesCondition) cond);
            } else if (cond instanceof Condition) {
                out.name("condition");
                //For now, we only use anat. entity and cell type for comparison
                writeSimplifiedCondition(out, (Condition) cond, EnumSet.of(
                        CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.CELL_TYPE_ID));
            } else {
                throw log.throwing(new IllegalStateException("Unrecognized class: "
                        + cond.getClass().getSimpleName()));
            }

            //Conservation score
            Map<ExpressionSummary, Set<Gene>> callTypeToGenes = condToCounts.getValue().getCallTypeToGenes();
            Set<Gene> expressedGenes = callTypeToGenes.get(ExpressionSummary.EXPRESSED);
            if (expressedGenes == null) {
                expressedGenes = new HashSet<>();
            }
            Set<Gene> notExpressedGenes = callTypeToGenes.get(ExpressionSummary.NOT_EXPRESSED);
            if (notExpressedGenes == null) {
                notExpressedGenes = new HashSet<>();
            }
            //We need to cast to double explicitly otherwise the result of dividing is incorrect
            @SuppressWarnings("cast")
            double score = (double) (expressedGenes.size() - notExpressedGenes.size())
                    / ((double) expressedGenes.size() + notExpressedGenes.size());
            out.name("conservationScore");
            out.value(String.format(Locale.US, "%.2f", score));

            // Max expression score
            Optional<ExpressionLevelInfo> collect = condToCounts.getValue().getGeneToExprLevelInfo().values().stream()
                    .filter(eli -> eli != null && eli.getExpressionScore() != null)
                    .max(Comparator.comparing(ExpressionLevelInfo::getExpressionScore,
                            Comparator.nullsFirst(BigDecimal::compareTo)));
            out.name("maxExpressionScore");
            out.value(collect.isPresent()? collect.get().getFormattedExpressionScore(): "NA");

            // Genes
            out.name("genesExpressionPresent");
            writeGenes(out, expressedGenes);
            out.name("genesExpressionAbsent");
            writeGenes(out, notExpressedGenes);
            out.name("genesNoData");
            writeGenes(out, condToCounts.getValue().getGenesWithNoData());

            out.endObject();
            log.traceExit();
        }

        private static void writeGenes(JsonWriter out, Set<Gene> genes) throws IOException {
            log.traceEntry("{}, {}", out, genes);

            out.beginArray();
            if (genes != null) {
                List<Gene> sortedGenes = genes.stream().sorted(Comparator.comparing(Gene::getGeneId))
                        .collect(Collectors.toList());
                for (Gene gene: sortedGenes) {
                    writeSimplifiedGene(out, gene, false, null);
                }
            }
            out.endArray();

            log.traceExit();
        }
    }

    private static final class Strategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            if (field.getDeclaringClass() == Species.class) {
                if (field.getName().equals("dataTypesByDataSourcesForData") ||
                        field.getName().equals("dataTypesByDataSourcesForAnnotation")) {
                    return true;
                }
            }
            //XXX: we mask the ID because it is not stable between releases. This should go away once
            //we redefine SpeciesDataGroup for not having an ID. In the interface, for single-species data groups,
            //we should use the ID of the species.
            //XXX: When we'll have multi-species files, we will need to define an Adapter for SpeciesDataGroup
            //in order to provide an ID composed of the IDs of the species members.
            //XXX: undoing this because it misght still be used by the current HTML Bgee code.
            //To remove once the new website is ready.
//            else if (field.getDeclaringClass() == SpeciesDataGroup.class && field.getName().equals("id")) {
//                return true;
//            }
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }


    private static void writeSimplifiedCondition(JsonWriter out, Condition cond,
            EnumSet<CallService.Attribute> condParams) throws IOException {
        log.traceEntry("{}, {}, {}", out, cond, condParams);
        out.beginObject();

        // Anat entity ID and Anat entity cells
        AnatEntity anatEntity = cond.getAnatEntity();
        out.name("anatEntity");
        if (anatEntity != null && condParams.contains(CallService.Attribute.ANAT_ENTITY_ID)) {
            assert condParams.contains(CallService.Attribute.CELL_TYPE_ID):
                "Anat. entity and cell type are requested together for the gene page";
            writeSimplifiedNamedEntity(out, anatEntity);
        } else {
            out.nullValue();
        }

        AnatEntity cellType = cond.getCellType();
        out.name("cellType");
        // post-composition if not the root of cell type
        if (cellType != null && condParams.contains(CallService.Attribute.CELL_TYPE_ID) &&
                !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
            assert condParams.contains(CallService.Attribute.ANAT_ENTITY_ID):
                "Anat. entity and cell type are requested together for the gene page";
            writeSimplifiedNamedEntity(out, cellType);
        } else {
            out.nullValue();
        }

        // Dev stage
        DevStage stage = cond.getDevStage();
        out.name("devStage");
        if (stage != null && condParams.contains(CallService.Attribute.DEV_STAGE_ID)) {
            writeSimplifiedNamedEntity(out, stage);
        } else {
            out.nullValue();
        }

        // Sexes
        Sex sex = cond.getSex();
        out.name("sex");
        if (sex != null && condParams.contains(CallService.Attribute.SEX_ID)) {
            out.value(sex.getName());
        } else {
            out.nullValue();
        }

        // Strains
        Strain strain = cond.getStrain();
        out.name("strain");
        if (strain != null && condParams.contains(CallService.Attribute.STRAIN_ID)) {
            out.value(strain.getName());
        } else {
            out.nullValue();
        }
        out.endObject();
        log.traceExit();
    }

    private static void writeSimplifiedMultiSpeciesCondition(JsonWriter out, MultiSpeciesCondition cond)
            throws IOException {
        log.traceEntry("{}, {}", out, cond);
        out.beginObject();

        out.name("anatEntities");
        out.beginArray();
        for (AnatEntity ae: cond.getAnatSimilarity().getSourceAnatEntities()) {
            writeSimplifiedNamedEntity(out, ae);
        }
        out.endArray();

        out.name("cellTypes");
        out.beginArray();
        for (AnatEntity cellType: cond.getCellTypeSimilarity().getSourceAnatEntities()) {
            if (cellType != null && !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
                writeSimplifiedNamedEntity(out, cellType);
            }
        }
        out.endArray();

        //TODO: stageSimilarity and Sex

        out.endObject();
        log.traceExit();
    }

    private static void writeSimplifiedSource(JsonWriter out, Source source) throws IOException {
        log.traceEntry("{}, {}", out, source);
        out.name("name").value(source.getName());
        out.name("description").value(source.getDescription());
        out.name("baseUrl").value(source.getBaseUrl());
        log.traceExit();
    }
    private static void writeSimplifiedXRef(JsonWriter out, XRef xRef,
            Function<String, String> urlEncodeFunction) throws IOException {
        log.traceEntry("{}, {}", out, xRef, urlEncodeFunction);
        out.name("xRefId").value(xRef.getXRefId());
        out.name("xRefName").value(xRef.getXRefName());
        out.name("xRefURL").value(xRef.getXRefUrl(false, urlEncodeFunction));
        log.traceExit();
    }
    private static void writeSimplifiedGene(JsonWriter out, Gene gene, boolean withSpeciesDataSource,
            EnumSet<DataType> allowedDataTypes)
            throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, gene, withSpeciesDataSource, allowedDataTypes);
        out.beginObject();
        out.name("geneId").value(gene.getGeneId());
        out.name("name").value(gene.getName());

        //Simplified display of Species
        out.name("species");
        out.beginObject();
        out.name("id").value(gene.getSpecies().getId());
        out.name("name").value(gene.getSpecies().getName());
        out.name("genus").value(gene.getSpecies().getGenus());
        out.name("speciesName").value(gene.getSpecies().getSpeciesName());
        out.name("preferredDisplayOrder").value(gene.getSpecies().getPreferredDisplayOrder());
        if (withSpeciesDataSource) {
            out.name("sourcesOfDataPerDataType");
            writeSourcesPerDataType(out, gene.getSpecies().getDataSourcesForDataByDataTypes(),
                    allowedDataTypes);
            out.name("sourcesOfAnnotationsPerDataType");
            writeSourcesPerDataType(out, gene.getSpecies().getDataSourcesForAnnotationByDataTypes(),
                    allowedDataTypes);
        }
        out.endObject();

        out.name("geneMappedToSameGeneIdCount").value(gene.getGeneMappedToSameGeneIdCount());
        out.endObject();
        log.traceExit();
    }
    private static void writeSourcesPerDataType(JsonWriter out, Map<DataType, Set<Source>> map,
            EnumSet<DataType> allowedDataTypes) throws IOException {
        log.traceEntry("{}, {}, {}", out, map, allowedDataTypes);
        // We order the Map by DataType and Source alphabetical name order
        LinkedHashMap<DataType, List<Source>> dsByDataTypes = map.entrySet().stream()
                .filter(e -> allowedDataTypes == null || allowedDataTypes.contains(e.getKey()))
                .sorted(Comparator.comparing(e -> e.getKey()))
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().sorted(Comparator.comparing(s -> s.getName()))
                                         .collect(Collectors.toList()),
                        (v1, v2) -> {throw new AssertionError("Impossible collision");},
                        LinkedHashMap::new));
        out.beginArray();
        for (Entry<DataType, List<Source>> e: dsByDataTypes.entrySet()) {
            out.beginObject();
            out.name("dataType").value(e.getKey().getStringRepresentation());
            out.name("sources");
            out.beginArray();
            for (Source s: e.getValue()) {
                out.beginObject();
                writeSimplifiedSource(out, s);
                out.endObject();
            }
            out.endArray();  // end List value
            out.endObject(); // end Entry
        }
        out.endArray(); // end Map
        log.traceExit();
    }
    private static void writeSimplifiedNamedEntity(JsonWriter out, NamedEntity<String> namedEntity)
            throws IOException {
        log.traceEntry("{}, {}", out, namedEntity);
        if (namedEntity == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();
        out.name("id").value(namedEntity.getId());
        out.name("name").value(namedEntity.getName());
        out.endObject();
        log.traceExit();
    }
    /**
     * The {@code Gson} used to dump JSON
     */
    private final Gson gson;
    /**
     * A {@code BgeeProperties} to retrieve parameters from.
     */
    private final BgeeProperties props;
    /**
     * A {@code RequestParameters} corresponding to the current request to the webapp.
     */
    private final RequestParameters requestParameters;
    /**
     * A {@code String} defining the character encoding for encoding query strings.
     */
    private final String charEncoding;
    
    /**
     * Default constructor delegating to {@link #JsonHelper(BgeeProperties)} with null arguments.
     * 
     * @see #JsonHelper(BgeeProperties)
     */
    public JsonHelper() {
        this(null, null);
    }
    /**
     * @param props The {@code BgeeProperties} to retrieve parameters from. If {@code null}, 
     *              the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     */
    public JsonHelper(BgeeProperties props) {
        this(props, null);
    }
    /**
     * @param props             The {@code BgeeProperties} to retrieve parameters from. 
     *                          If {@code null}, the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     * @param requestParameters The {@code RequestParameters} corresponding to the current request to the webapp.
     */
    public JsonHelper(BgeeProperties props, RequestParameters requestParameters) {
        if (props == null) {
            this.props = BgeeProperties.getBgeeProperties();
        } else {
            this.props = props;
        }
        if (requestParameters == null) {
            this.requestParameters = null;
            this.charEncoding = null;
        } else {
            this.requestParameters = requestParameters.cloneWithAllParameters();
            this.charEncoding = this.requestParameters.getCharacterEncoding();
        }
        
        //we do not allow the Gson object to be injected, so that signatures of this class 
        //are not dependent of a specific JSON library. 
        this.gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new Strategy())
                .registerTypeAdapter(DownloadFile.class, new DownloadFileTypeAdapter(this.props))
                .registerTypeAdapter(RequestParameters.class, new RequestParametersTypeAdapter())
                .registerTypeAdapter(TopAnatResults.class, new TopAnatResultsTypeAdapter(this.requestParameters))
                .registerTypeAdapter(Job.class, new JobTypeAdapter())
                .registerTypeAdapter(GeneXRef.class, new GeneXRefAdapter(s -> this.urlEncode(s)))
                .registerTypeAdapterFactory(new BgeeTypeAdapterFactory(s -> this.urlEncode(s),
                        this.requestParameters, () -> getNewRequestParameters()))
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    /**
     * Creates JSON from the given {@code Object}, returning it immediately. 
     * This method should not be used for objects potentially producing very large outputs, 
     * that should not be put into memory.
     * 
     * @param object An {@code Object} to be dumped into JSON.
     * @return       The {@code String} containing the JSON representation of the given object.
     * @see #toJson(LinkedHashMap, Appendable)
     */
    public String toJson(Object object) {
        log.traceEntry("{}", object);
        return log.traceExit(gson.toJson(object));
    }
    
    /**
     * Dump the provided {@code Map} into JSON and print it immediately using {@code out}. 
     * Such a {@code Map} is typically used to print Bgee server responses in JSON. 
     * It is a {@code LinkedHashMap} to allow the generation of predictable responses. 
     * 
     * @param response  A {@code LinkedHashMap} where keys are {@code String}s that are 
     *                  the name for dumping in JSON of the associated value {@code Object}.
     * @param out       An {@code Appendable} used to print the generated JSON.
     */
    public void toJson(LinkedHashMap<String, Object> response, Appendable out) {
        log.traceEntry("{}, {}", response, out);
        gson.toJson(response, out);
        log.traceExit();
    }

    /**
     * URL encode the provided {@code String}, with the character encoding used to generate URLs.
     *
     * @param stringToWrite A {@code String} to be encoded.
     * @return              The encoded {@code String}.
     */
    private String urlEncode(String stringToWrite) {
        log.traceEntry("{}", stringToWrite);
        try {
            return log.traceExit(java.net.URLEncoder.encode(stringToWrite, this.charEncoding));
        } catch (Exception e) {
            log.catching(e);
            return log.traceExit("");
        }
    }
    /**
     * Return a new {@code RequestParameters} object to be used to generate URLs.
     * This new {@code RequestParameters} will use the same {@code URLParameters}
     * as those returned by {@link #requestParameters} when calling
     * {@link RequestParameters#getUrlParametersInstance()},
     * and the {@code BgeeProperties} {@link #props}.
     * Also, parameters will be URL encoded, and parameter separator will be {@code &}.
     *
     * @return  A newly created RequestParameters object.
     */
    private RequestParameters getNewRequestParameters() {
        log.traceEntry();
        return log.traceExit(new RequestParameters(
                this.requestParameters.getUrlParametersInstance(),
                this.props, true, "&"));
    }
}
