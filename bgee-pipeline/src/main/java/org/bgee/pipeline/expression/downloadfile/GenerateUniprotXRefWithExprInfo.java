package org.bgee.pipeline.expression.downloadfile;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

public class GenerateUniprotXRefWithExprInfo {

    private final static Logger log = LogManager.getLogger(GenerateUniprotXRefWithExprInfo.class.getName());
    
    public static void main(String[] args) throws IOException{
        if(args.length == 2){
            String inputFileName = args[0];
            String outputFileName = args[1];
            GenerateUniprotXRefWithExprInfo expressionInfoGenerator = new GenerateUniprotXRefWithExprInfo();
            List<XrefUniprotBean> xrefUniprotList = 
                    expressionInfoGenerator.loadXrefFileWithoutExprInfo(inputFileName);
            //we can go with parallelStream and foreach because each XRef 
            //is independant and we order the xrefs later
            Map <String, String> ensemblIdToXrefLines = 
                    expressionInfoGenerator.generateExpressionInfo(xrefUniprotList);
            Map <String, String> sortedEnsemblIdToXrefLines = 
                    GenerateUniprotXRefWithExprInfo.sortXrefByUniprotId(ensemblIdToXrefLines);
            expressionInfoGenerator.writeXrefWithExpressionInfo(outputFileName, 
                    sortedEnsemblIdToXrefLines.values());
        }else{
            throw log.throwing(new IllegalArgumentException("Uncorrect number of arguments."));
        }
    }

    /**
     * Read the Uniprot Xref file without expression information and store lines into a {@code List}
     * of {@code XrefUniprotBean}
     * @param file  Name of the file that contains all Uniprot Xref without expression information
     * @return  a {@code List} of {@code XrefUniprotBean}
     * @throws IOException
     */
    private List<XrefUniprotBean> loadXrefFileWithoutExprInfo(String file) throws IOException {
        log.entry(file);
        List<XrefUniprotBean> xrefUniprotList = new ArrayList<>();
        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.TAB_PREFERENCE);
            final String[] header = beanReader.getHeader(false);
            final CellProcessor[] processors = new CellProcessor[] { 
                    new NotNull(), // uniprotXrefId
                    new NotNull(),        // ensemblGeneId
                    new ParseInt()        // speciesId
            };
            XrefUniprotBean xrefBean;
            while( (xrefBean = beanReader.read(XrefUniprotBean.class, header, processors)) != null ) {
                xrefUniprotList.add(xrefBean);
            }
        }
        finally {
            if( beanReader != null ) {
                beanReader.close();
            }
        }
        return log.exit(xrefUniprotList);
    }
    
    /**
     * Summarize expression information for one gene. This summarized information contains:
     * - number of Anatomical entities where this gene is expressed
     * - Name of the anatomical entity where this gene has the higher expression level
     * Take as input a {@code List} of {@code XrefUniprotBean} object that contains all information 
     * to call the {@code CallService}. 
     * @param xrefList  A {@code List} of {@code XrefUniprotBean} containing information retrieved 
     *                  from Bgee database and needed to create uniprot cross-references
     * @return A {@code Map} where keys correspond to ensembl gene Ids and each value corresponds 
     * to one well formated uniprot Xref
     */
    private Map<String, String> generateExpressionInfo(List <XrefUniprotBean> xrefList) {
        log.entry(xrefList);
        Instant start = Instant.now();
        Map<String, String> ensemlbIdToXrefLines = new HashMap<>();
        xrefList.parallelStream().forEach( xref -> {
            ServiceFactory serviceFactory = new ServiceFactory();
            Gene gene = serviceFactory.getGeneService().loadGenes(Collections.singleton(
                    new GeneFilter(xref.getSpeciesId(), xref.getEnsemblId()))).findFirst().get();
            CallService service = serviceFactory.getCallService();
            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                    new LinkedHashMap<>();
            //XXX: Following lines (until line 171) of code are the same than in the CommandGene. 
            //We should move this code to a shared location and call it for both CommandGene AND Uniprot XRef generation
            serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
            Map<SummaryCallType.ExpressionSummary, SummaryQuality> silverExpressedCallFilter = new HashMap<>();
            silverExpressedCallFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER);
            Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
            obsDataFilter.put(CallType.Expression.EXPRESSED, true);
            
            final List<ExpressionCall> calls = service.loadExpressionCalls(
                    new ExpressionCallFilter(silverExpressedCallFilter,
                            Collections.singleton(new GeneFilter(gene.getSpecies().getId(), gene.getEnsemblGeneId())),
                            null, null, obsDataFilter, null, null),
                    EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                               CallService.Attribute.GLOBAL_MEAN_RANK,
                               CallService.Attribute.EXPERIMENT_COUNTS),
                    serviceOrdering)
                .collect(Collectors.toList());
    
            final Set<String> organIds = calls.stream()
                    .map(c ->c .getCondition().getAnatEntityId())
                    .collect(Collectors.toSet());
            
            Map<SummaryCallType.ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter = new HashMap<>();
            summaryCallTypeQualityFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
    
            List<ExpressionCall> expressionCalls = service.loadExpressionCalls(
                    new ExpressionCallFilter(summaryCallTypeQualityFilter,
                            Collections.singleton(new GeneFilter(gene.getSpecies().getId(), gene.getEnsemblGeneId())),
                            null, null, obsDataFilter, null, null),
                    EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                            CallService.Attribute.DEV_STAGE_ID,
                            CallService.Attribute.DATA_QUALITY,
                            CallService.Attribute.GLOBAL_MEAN_RANK,
                            CallService.Attribute.EXPERIMENT_COUNTS),
                    serviceOrdering)
                .collect(Collectors.toList());
            List<ExpressionCall> orderedCalls = expressionCalls.stream()
                    .filter(c -> organIds.contains(c.getCondition().getAnatEntityId()))
                    .collect(Collectors.toList());
            if(orderedCalls == null || orderedCalls.isEmpty()){
                log.info("No expression data for gene "+xref.ensemblId);
            }else{
                try{
                    ConditionGraph organStageGraph = new ConditionGraph(
                            orderedCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
                            serviceFactory);
                    serviceFactory.close();
                    orderedCalls.sort(new ExpressionCall.RankComparator(organStageGraph));
                    final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                            orderedCalls, organStageGraph);
                    orderedCalls.removeAll(redundantCalls);
                    LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity = orderedCalls.stream()
                            //group by anat. entity
                            .collect(Collectors.groupingBy(
                                    c -> c.getCondition().getAnatEntity(), 
                                    LinkedHashMap::new, 
                                    Collectors.toList()))
                            .entrySet().stream()
                            //discard if all calls of an anat. entity are redundant
                            .filter(entry -> !redundantCalls.containsAll(entry.getValue()))
                            //reconstruct the LinkedHashMap
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), 
                                    (l1, l2) -> {
                                        throw log.throwing(new AssertionError("Not possible to have key collision"));
                                    }, 
                                    LinkedHashMap::new));
                    String prefixLine = xref.getUniprotId()+"   DR   BGEE; "+xref.getEnsemblId()+";";
                    ensemlbIdToXrefLines.put(xref.ensemblId, prefixLine+" Expressed in "+callsByAnatEntity.size() +
                            " organ(s), highest expression level in "+
                            orderedCalls.get(0).getCondition().getAnatEntity().getName()+".");
                }catch (IllegalArgumentException e) {
                    log.error("Comparison method violates its original contract. "
                            + "No XRef will be generated for gene {}", gene.getEnsemblGeneId());
                }
            }
        });
        Instant end = Instant.now();
        log.debug("Time needed to retrieve expressionSummary of {} genes is {} hours", 
                xrefList.size(), Duration.between(start, end).toHours());
        return log.exit(ensemlbIdToXrefLines);
        
    }
    
    private static Map<String, String> sortXrefByUniprotId(Map<String, String> ensemblIdToXrefLines) {
        log.entry(ensemblIdToXrefLines);
        return log.exit(ensemblIdToXrefLines.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
    }
    
    /**
     * Write an XRef file according to Uniprot format. Each line contains:
     * - Uniprot ID
     * - Ensembl ID used in Bgee
     * - summary of the expression
     * Ex: H9G366   DR   BGEE; ENSACAG00000000002; Expressed in 4 organs, higher expression level in brain.
     * 
     * @param file path to the output file
     * @param outputXrefLines a {@code Collection} of {@Code String} corresponding to all Bgee Xref in Uniprot
     * @throws IOException
     */
    private void writeXrefWithExpressionInfo(String file, Collection <String> outputXrefLines) throws IOException {
        Path filePath = Paths.get(file);
        Files.write(filePath, outputXrefLines, Charset.forName("UTF-8"));
    }
    
    /**
     * This class is used to easily import data stored in a TSV file created by the pipeline.
     *  This file contains information needed to summarize expression level in the Uniprot Xref file.
     * @author jwollbrett
     *
     */
    public static class XrefUniprotBean {

        private String uniprotId;
        private String ensemblId;
        private Integer speciesId;
   
        
        public XrefUniprotBean(){
        }
        
        public XrefUniprotBean(String uniprotId, String ensemblId, Integer speciesId){
            this.uniprotId = uniprotId;
            this.ensemblId = ensemblId;
            this.speciesId = speciesId;
        }

        public String getUniprotId() {
            return uniprotId;
        }

        public void setUniprotId(String uniprotId) {
            this.uniprotId = uniprotId;
        }

        public String getEnsemblId() {
            return ensemblId;
        }

        public void setEnsemblId(String ensemblId) {
            this.ensemblId = ensemblId;
        }

        public Integer getSpeciesId() {
            return speciesId;
        }

        public void setSpeciesId(Integer speciesId) {
            this.speciesId = speciesId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ensemblId == null) ? 0 : ensemblId.hashCode());
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            result = prime * result + ((uniprotId == null) ? 0 : uniprotId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XrefUniprotBean other = (XrefUniprotBean) obj;
            if (ensemblId == null) {
                if (other.ensemblId != null)
                    return false;
            } else if (!ensemblId.equals(other.ensemblId))
                return false;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            if (uniprotId == null) {
                if (other.uniprotId != null)
                    return false;
            } else if (!uniprotId.equals(other.uniprotId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "XrefUniprotBean [uniprotId=" + uniprotId + ", ensemblId=" + ensemblId + ", speciesId=" + 
                    speciesId + "]";
        }
        
        
    }

}
