package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.expressiondata.CallDAO;
import org.bgee.model.expressiondata.rawdata.*;
import org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence;
import org.bgee.model.expressiondata.rawdata.insitu.InSituExperiment;
import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixChip;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixExperiment;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.Gene;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceCategory;
import org.bgee.model.species.Species;
import org.bgee.view.RawDataDisplay;
import org.bgee.view.ViewFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Stream;

/**
 * Controller that handles requests for the raw data page.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Sept. 2018
 * @since   Bgee 14, Aug. 2018
 */
public class CommandRawData extends CommandParent {

    private final static Logger log = LogManager.getLogger(CommandRawData.class.getName());

    /**
     * Constructor
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides the services to be used.
     */
    public CommandRawData(HttpServletResponse response, RequestParameters requestParameters,
                          BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        super(response, requestParameters, prop, viewFactory, serviceFactory);
    }

    @Override
    public void processRequest() throws Exception {

        RawDataDisplay display = viewFactory.getRawCallDisplay();
        String geneId = requestParameters.getGeneId();
        Integer speciesId = requestParameters.getSpeciesId();
        List<String> anatEntityIds = requestParameters.getAnatEntity();
        List<String> devStageIds = requestParameters.getDevStage();

        if (anatEntityIds != null && anatEntityIds.size() > 1) {
            throw(new MultipleValuesNotAllowedException(requestParameters.getUrlParametersInstance().getParamAnatEntity()));
        }
        if (devStageIds != null && devStageIds.size() > 1) {
            throw(new MultipleValuesNotAllowedException(requestParameters.getUrlParametersInstance().getParamDevStage()));
        }

        if (geneId == null && speciesId == null && (anatEntityIds == null || anatEntityIds.isEmpty())
                && (devStageIds == null || devStageIds.isEmpty())) {
            display.displayRawCallHomePage();
            log.exit();
            return;
        }
        
//        Set<GeneFilter> geneFilters = new HashSet(Collections.singletonList(new GeneFilter(speciesId, geneId)));
//
//        RawDataLoader rawDataLoader = serviceFactory.getRawDataService().getRawDataLoader(geneFilters);
//
//        Stream<AffymetrixProbeset> affymetrixProbesets = rawDataLoader.loadAffymetrixProbesets();
        Stream<AffymetrixProbeset> affymetrixProbesets = null;
        
        Species sp = new Species(9096, "human", "description", "Homo", "sapiens", null, null);
        Gene gene = new Gene("ENSG00000116062", sp, 1);

        RawCall rawCall1 = new RawCall(gene, RawCall.DetectionFlag.PRESENT,
                CallDAO.CallTO.DataState.HIGHQUALITY, RawCall.ExclusionReason.NOT_EXCLUDED);
        RawCall rawCall2 = new RawCall(gene, RawCall.DetectionFlag.ABSENT,
                CallDAO.CallTO.DataState.LOWQUALITY, RawCall.ExclusionReason.PRE_FILTERING);

        Source source = new Source(4, "GEO", "GEO desc",
                "https://www.ncbi.nlm.nih.gov/geo/[xref_id]",
                "https://www.ncbi.nlm.nih.gov/geo/[experiment_id]",
                "https://www.ncbi.nlm.nih.gov/geo/[evidence_id]",
                "https://www.ncbi.nlm.nih.gov/geo/", null, "rv:2", true, SourceCategory.AFFYMETRIX, 2);
        AffymetrixExperiment affExp1 = new AffymetrixExperiment("GSE10746",
                "Transcription profiling of human patients with AML and Chemotherapy-induced oral mucositis (CIOM))",
                "Chemotherapy may cause DNA damage within the oral mucosa of cancer patients leading to mucositis, a dose-limiting side effect for effective cancer treatment. We used whole genome gene expression analysis to identify cellular damage to the mucosal tissue occuring two days post induction chemotherapy and identified gene expression patterns that may or may not be predictive of oral mucositis. Experiment Overall Design: Punch buccal biopsies from healthy controls (HC, samples BRENC1, BRENC2, BRENC3, n=3) and five AML patients pre-chemotherapy (Pre-C, samples BREN11, BREN21, BREN41, BREN51, n=4) and (Post-C, samples BREN22, BREN32, BREN42, BREN52, n=4)(Ntotal=11) gave suitable RNA integrity to perform microarray analysis. Samples Pre-C:BREN31 and post-C:BREN12 were not suitable for microarray analysis. Human Genome U133 Plus 2.0 Array (Affymetrix, Santa Clara, CA) was used to conduct gene expression profiling.",
                source);
        AffymetrixExperiment affExp2 = new AffymetrixExperiment("GSE30784",
                "Gene expression profiling of oral squamous cell carcinoma (OSCC)",
                " is associated with substantial mortality and morbidity. To identify potential biomarkers for the early detection of invasive OSCC, we compared the gene expressions of OSCC, oral dysplasia, and normal oral tissue from patients without oral cancer or preneoplastic oral lesions (controls). Results provided models of gene expression to distinguish OSCC from controls. RNA from 167 OSCC, 17 dysplasia and 45 normal oral tissues were extracted and hybridized to Affymetrix U133 2.0 Plus GeneChip arrays. The differentially expressed genes were identified using GenePlus software and the validation was done using RT-PCR, using independent internal and external datasets.",
                source);

        RawDataCondition condition = new RawDataCondition(new AnatEntity("UBERON:0000178", "blood", "blood desc"),
                new DevStage("UBERON:0034920", "infant stage", "infant stage desc"), sp);

        RawDataAnnotation annot = new RawDataAnnotation(condition, "Curator name",
                new Source(1, "WormBase", "WormBase - Nematode Information Resource",
                        "xref_id_URL/[xref_id]",
                        "expriment_URL/[experiment_id]",
                        "evidence_URL/[evidence_id]",
                        "home:url/", null, null, null, null, null), null);

        affymetrixProbesets = Stream.of(
                new AffymetrixProbeset("153461", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153462", new AffymetrixChip("GSM245144", affExp1, annot), rawCall2),
                new AffymetrixProbeset("153466", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153468", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153471", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153472", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153475", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153473", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153478", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153479", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153480", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153482", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153485", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153486", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153487", new AffymetrixChip("GSM245144", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153463", new AffymetrixChip("GSM245150", affExp1, annot), rawCall2),
                new AffymetrixProbeset("153464", new AffymetrixChip("GSM245150", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153467", new AffymetrixChip("GSM245150", affExp1, annot), rawCall1),
                new AffymetrixProbeset("153469", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153470", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153474", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153476", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153477", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153481", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153483", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153484", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153488", new AffymetrixChip("GSM245144", affExp2, annot), rawCall1),
                new AffymetrixProbeset("153465", new AffymetrixChip("GSM245150", affExp2, annot), rawCall1));


        InSituExperiment inSituExp = new InSituExperiment("exp-id", "exp-name", "exp-descrip", source);
        Stream<InSituSpot> inSituSpots = Stream.of(
                new InSituSpot("spot-id1", new InSituEvidence("ev-id1", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id2", new InSituEvidence("ev-id1", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id3", new InSituEvidence("ev-id1", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id4", new InSituEvidence("ev-id1", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id5", new InSituEvidence("ev-id2", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id6", new InSituEvidence("ev-id2", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id7", new InSituEvidence("ev-id2", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id8", new InSituEvidence("ev-id2", inSituExp), annot, rawCall1),
                new InSituSpot("spot-id9", new InSituEvidence("ev-id3", inSituExp), annot, rawCall1));
        
        display.displayRawCallPage(affymetrixProbesets, inSituSpots);
        
        
//        if (geneId != null) {
//            Set<Gene> genes = serviceFactory.getGeneService().loadGenesByEnsemblId(geneId, true);
//            
//            if (genes.size() == 0) {
//                throw log.throwing(new PageNotFoundException("No gene corresponding to " + geneId));
//            }
//
//            if (genes.size() == 1 && speciesId != null) {
//                // FIXME Redirect
//            }
//            if (genes.size() == 1) {
//                // todo
//                if (speciesId == null || speciesId <= 0) {
//                    serviceFactory.getRawDataService().loadRawCallSourceContainers(new GeneFilter(speciesId, geneId));
//                    display.displayRawCallPage(genes);
//                    log.exit(); return;
//                }
//
//            } else {
//
//            }
//        }

        // none => search
        // species
        // experiment
        // assay
        // gene + anat entity
        // gene + stage
        // gene + anat entity + stage
    }
}
