package org.bgee.pipeline.expression.downloadfile;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static List<XrefUniprotBean> xrefUniprotList = new ArrayList<>();
    private List<String> outputXrefLines = new ArrayList<>();
    
    public static void main(String[] args) throws IOException{
        if(args.length == 2){
            String inputFileName = args[0];
            String outputFileName = args[1];
            GenerateUniprotXRefWithExprInfo expressionInfoGenerator = new GenerateUniprotXRefWithExprInfo();
            expressionInfoGenerator.loadXrefFileWithoutExprInfo(inputFileName);
            for(XrefUniprotBean xref:xrefUniprotList){
                expressionInfoGenerator.generateExpressionInfo(xref);
            }
            expressionInfoGenerator.writeXrefWithExpressionInfo(outputFileName);
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
    private void loadXrefFileWithoutExprInfo(String file) throws IOException{
        log.entry(file);
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
    }
    
    /**
     * Summarize expression information for one gene. This summarized information contains:
     * - number of Anatomical entities where this gene is expressed
     * - Name of the anatomical entity where this gene has the higher expression level
     * Take as input a {@code XrefUniprotBean} object that contains all information to call the 
     * {@code CallService}. 
     * @param xref  A {@code XrefUniprotBean} object 
     */
    private void generateExpressionInfo(XrefUniprotBean xref){
        log.entry(xref);
        ServiceFactory serviceFactory = new ServiceFactory();
        Gene gene = serviceFactory.getGeneService().loadGenes(Collections.singleton(
                new GeneFilter(xref.getSpeciesId(), xref.getEnsemblId()))).findFirst().get();
        CallService service = serviceFactory.getCallService();
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        //XXX: Following lines (until line XXX) of code are the same than in the CommandGene. Maybe we should move this code to
        // a shared location and call it for both CommandGene AND Uniprot Xref generation
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
            ConditionGraph organStageGraph = new ConditionGraph(
                    orderedCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()), 
                    serviceFactory);
            Collections.sort(orderedCalls, new ExpressionCall.RankComparator(organStageGraph));
            final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                    orderedCalls, organStageGraph);
            orderedCalls.removeAll(redundantCalls);
        }
        serviceFactory.close();
        String prefixLine = xref.getUniprotId()+"   DR   BGEE; "+xref.getEnsemblId()+";";
        if(orderedCalls.isEmpty()){
            outputXrefLines.add( prefixLine+" -.");
        }else{
            outputXrefLines.add(prefixLine+" Expressed in "+orderedCalls.size()+" organ(s), highest expression level in "+
                    orderedCalls.get(0).getCondition().getAnatEntity().getName()+".");
        }
    }
    
    /**
     * Write an XRef file according to Uniprot format. Each line contains:
     * - Uniprot ID
     * - Ensembl ID used in Bgee
     * - summary of the expression
     * Ex: H9G366   DR   BGEE; ENSACAG00000000002; Expressed in 4 organs, higher expression level in brain.
     * 
     * @param file path to the output file
     * @throws IOException
     */
    private void writeXrefWithExpressionInfo(String file) throws IOException{
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
            return "XrefUniprotBean [uniprotId=" + uniprotId + ", ensemblId=" + ensemblId + ", speciesId=" + speciesId
                    + "]";
        }
        
        
    }

}
