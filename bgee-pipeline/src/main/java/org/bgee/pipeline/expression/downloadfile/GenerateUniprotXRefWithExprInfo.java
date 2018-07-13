package org.bgee.pipeline.expression.downloadfile;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Class used to generate Uniprot Xrefs file with expression information from
 * the Bgee database.
 * 
 * @author Julien Wollbrett
 * @version Bgee 14, July 2017
 */

public class GenerateUniprotXRefWithExprInfo {

    private final static Logger log = LogManager.getLogger(GenerateUniprotXRefWithExprInfo.class.getName());
    private ServiceFactory serviceFactory;

    public static void main(String[] args) {
        if (args.length == 2) {
            String inputFileName = args[0];
            String outputFileName = args[1];
            GenerateUniprotXRefWithExprInfo expressionInfoGenerator = new GenerateUniprotXRefWithExprInfo();
            // load Uniprot Xrefs
            Set<XrefUniprotBean> xrefUniprotList = expressionInfoGenerator.loadXrefFileWithoutExprInfo(inputFileName);
            // generate Xrefs with expression info
            Map<String, String> ensemblIdToXrefLines = expressionInfoGenerator.generateExpressionInfo(xrefUniprotList);
            // sort Xrefs by uniprotID
            Map<String, String> sortedEnsemblIdToXrefLines = GenerateUniprotXRefWithExprInfo
                    .sortXrefByUniprotId(ensemblIdToXrefLines);
            // write XRef file
            expressionInfoGenerator.writeXrefWithExpressionInfo(outputFileName, sortedEnsemblIdToXrefLines.values());
        } else {
            throw log.throwing(new IllegalArgumentException("Uncorrect number of arguments."));
        }
    }

    public GenerateUniprotXRefWithExprInfo() {
        serviceFactory = new ServiceFactory();
    }

    /**
     * Read the Uniprot Xref file without expression information and store lines
     * into a {@code List} of {@code XrefUniprotBean}
     * 
     * @param file
     *            Name of the file that contains all Uniprot Xref without
     *            expression information
     * @return a {@code List} of {@code XrefUniprotBean}
     */
    private Set<XrefUniprotBean> loadXrefFileWithoutExprInfo(String file) {
        log.entry(file);
        Set<XrefUniprotBean> xrefUniprotList = new HashSet<>();
        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.TAB_PREFERENCE)) {
            final String[] header = beanReader.getHeader(false);
            final CellProcessor[] processors = new CellProcessor[] { new NotNull(), // uniprotXrefId
                    new NotNull(), // ensemblGeneId
                    new NotNull(new ParseInt()) // speciesId
            };
            XrefUniprotBean xrefBean;
            while ((xrefBean = beanReader.read(XrefUniprotBean.class, header, processors)) != null) {
                xrefUniprotList.add(xrefBean);
            }
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can not read file " + file, e));
        }
        return log.exit(xrefUniprotList);
    }

    /**
     * Summarize expression information for one gene. This summarized
     * information contains: - number of Anatomical entities where this gene is
     * expressed - Name of the anatomical entity where this gene has the higher
     * expression level Take as input a {@code List} of {@code XrefUniprotBean}
     * object that contains all information to call the {@code CallService}.
     * 
     * @param xrefList
     *            A {@code List} of {@code XrefUniprotBean} containing
     *            information retrieved from Bgee database and needed to create
     *            uniprot cross-references
     * @return A {@code Map} where keys correspond to ensembl gene Ids and each
     *         value corresponds to one well formated uniprot Xref
     */
    private Map<String, String> generateExpressionInfo(Set<XrefUniprotBean> xrefList) {
        log.entry(xrefList);
        Instant start = Instant.now();
        Map<String, String> ensemlbIdToXrefLines = new HashMap<>();
        // we can go with parallelStream and foreach because each XRef is
        // independant and we order them later
        xrefList.parallelStream().forEach(xref -> {
            // Retrieve Gene corresponding to the Xref
            Gene gene = serviceFactory.getGeneService()
                    .loadGenes(Collections.singleton(new GeneFilter(xref.getSpeciesId(), xref.getEnsemblId())))
                    .findFirst().get();

            // Retrieve expression calls
            CallService service = serviceFactory.getCallService();
            final List<ExpressionCall> organCalls = service.getAnatEntitySilverExpressionCalls(gene);
            final List<ExpressionCall> organStageCalls = service.getAnatEntityDevStageBronzeExpressionCalls(gene);

            final Set<String> organIds = organCalls.stream().map(c -> c.getCondition().getAnatEntityId())
                    .collect(Collectors.toSet());
            List<ExpressionCall> orderedCalls = organStageCalls.stream()
                    .filter(c -> organIds.contains(c.getCondition().getAnatEntityId())).collect(Collectors.toList());
            if (orderedCalls == null || orderedCalls.isEmpty()) {
                log.info("No expression data for gene " + xref.ensemblId);
            } else {
                ConditionGraph organStageGraph = new ConditionGraph(
                        orderedCalls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()),
                        serviceFactory);
                // Order organ-stage calls
                orderedCalls.sort(new ExpressionCall.RankComparator(organStageGraph));
                // Identify redundant organ-stage calls
                final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(orderedCalls,
                        organStageGraph);
                LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity = service
                        .groupByAnatEntAndFilterOrderedCalls(orderedCalls, redundantCalls, true);
                serviceFactory.close();

                // Create String representation of the XRef with expression information
                String prefixLine = xref.getUniprotId() + "   DR   Bgee; " + xref.getEnsemblId() + ";";
                ensemlbIdToXrefLines.put(xref.ensemblId,
                        prefixLine + " Expressed in " + callsByAnatEntity.size()
                                + " organ(s), highest expression level in "
                                + orderedCalls.get(0).getCondition().getAnatEntity().getName() + ".");
            }
        });
        Instant end = Instant.now();
        log.debug("Time needed to retrieve expressionSummary of {} genes is {} hours", xrefList.size(),
                Duration.between(start, end).toHours());
        return log.exit(ensemlbIdToXrefLines);

    }

    private static Map<String, String> sortXrefByUniprotId(Map<String, String> ensemblIdToXrefLines) {
        log.entry(ensemblIdToXrefLines);
        return log.exit(ensemblIdToXrefLines.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
                .toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
    }

    /**
     * Write an XRef file according to Uniprot format. Each line contains: -
     * Uniprot ID - Ensembl ID used in Bgee - summary of the expression Ex:
     * H9G366 DR BGEE; ENSACAG00000000002; Expressed in 4 organs, higher
     * expression level in brain.
     * 
     * @param file
     *            path to the output file
     * @param outputXrefLines
     *            a {@code Collection} of {@Code String} corresponding to all
     *            Bgee Xref in Uniprot
     */
    private void writeXrefWithExpressionInfo(String file, Collection<String> outputXrefLines) {
        try {
            Files.write(Paths.get(file), outputXrefLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can't write file " + file, e));
        }
    }

    public static class XrefUniprotBean {

        private String uniprotId;
        private String ensemblId;
        private Integer speciesId;

        public XrefUniprotBean() {
        }

        public XrefUniprotBean(String uniprotId, String ensemblId, Integer speciesId) {
            this.uniprotId = uniprotId;
            this.ensemblId = ensemblId;
            this.speciesId = speciesId;
        }

        public String getUniprotId() {
            return uniprotId;
        }

        public String getEnsemblId() {
            return ensemblId;
        }

        public Integer getSpeciesId() {
            return speciesId;
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
