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
 * Class used to generate UniProtKB Xrefs file with expression information from
 * the Bgee database.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14, July 2017
 */
// FIXME: Add unit tests
public class GenerateUniprotXRefWithExprInfo {

    private final static Logger log = LogManager.getLogger(GenerateUniprotXRefWithExprInfo.class.getName());
    
    private ServiceFactory serviceFactory;

    /**
     * Default constructor. 
     */
    public GenerateUniprotXRefWithExprInfo() {
        this(new ServiceFactory());
    }

    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactory   A {@code ServiceFactory} to use.
     */
    public GenerateUniprotXRefWithExprInfo(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
    
    // XXX: Use service when it will be implemented
    /**
     * Main method to generate UniProtKB Xrefs file with expression information from
     * the Bgee database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the input file containing XRefs UniProtKB - Ensembl
     * <li>path to the file where to write Xrefs with expression information into.
     * </ol>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length != 2) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments."));
        }

        GenerateUniprotXRefWithExprInfo expressionInfoGenerator = new GenerateUniprotXRefWithExprInfo();
        expressionInfoGenerator.generate(args[0], args[1]);
        
        log.exit();
    }

    /**
     * Generate UniProtKB Xrefs file with expression information from the Bgee database. 
     *
     * @param inputFileName     A {@code String} that is the path to the file containing 
     *                          XRefs UniProtKB - Ensembl mapping.
     * @param outputFileName    A {@code String} that is the path to the file where to write data into.
     */
    private void generate(String inputFileName, String outputFileName) {
        log.entry(inputFileName, outputFileName);
        
        // load UniProtKB Xrefs
        Set<XrefUniprotBean> xrefUniprotList = this.loadXrefFileWithoutExprInfo(inputFileName);

        // generate lines with expression info
        Map<String, String> ensemblIdToXrefLines = this.generateXrefLines(xrefUniprotList);

        // sort Xrefs by uniprotID
        Map<String, String> sortedEnsemblIdToXrefLines = GenerateUniprotXRefWithExprInfo
                .sortXrefByUniprotId(ensemblIdToXrefLines);

        // write XRef file
        this.writeXrefWithExpressionInfo(outputFileName, sortedEnsemblIdToXrefLines.values());

        log.exit();
    }

    /**
     * Read the UniProtKB Xref file without expression information and store lines
     * into a {@code List} of {@code XrefUniprotBean}s.
     * 
     * @param file  A {@code String} that is the name of the file that contains
     *              all UniProtKB Xrefs without expression information.
     * @return      The {@code List} of {@code XrefUniprotBean}s.
     * @throws UncheckedIOException If an error occurred while trying to read the {@code file}.
     */
    private Set<XrefUniprotBean> loadXrefFileWithoutExprInfo(String file) throws UncheckedIOException {
        log.entry(file);
        
        Set<XrefUniprotBean> xrefUniprotList = new HashSet<>();
        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.TAB_PREFERENCE)) {
            final String[] header = beanReader.getHeader(false);
            final CellProcessor[] processors = new CellProcessor[] { 
                    new NotNull(), // uniprotXrefId
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
     * Generate UniprotKB XRef lines with expression information for one gene. These lines contains:
     * - number of anatomical entities where this gene is expressed;
     * - name of the anatomical entity where this gene has the higher expression level.
     * 
     * @param xrefList  A {@code List} of {@code XrefUniprotBean}s containing information retrieved 
     *                  from Bgee database and needed to create uniprot cross-references.
     * @return          The {@code Map} where keys correspond to Ensembl gene IDs and each
     *                  value corresponds to one well formatted UniProtKB Xref line.
     */
    private Map<String, String> generateXrefLines(Set<XrefUniprotBean> xrefList) {
        log.entry(xrefList);
        
        Instant start = Instant.now();
        
        Map<String, String> ensemlbIdToXrefLine = new HashMap<>();
        // we can go with parallelStream and foreach because each XRef is independent 
        // and we will order them later
        xrefList.parallelStream().forEach(xref -> {
            // Retrieve Gene corresponding to the Xref
            // XXX: do we have on gene for several UniProtKB entries? 
            // If yes, should we find a way to save them to avoid useless calls (gene service and call service) to db
            Gene gene = serviceFactory.getGeneService()
                    .loadGenes(Collections.singleton(new GeneFilter(xref.getSpeciesId(), xref.getEnsemblId())))
                    .findFirst().get();

            // XXX: this part, until callsByAnatEntity, is still the same code as in CommandGene no?
            // Retrieve expression calls
            CallService callService = serviceFactory.getCallService();
            List<ExpressionCall> calls = callService.loadAllcondCallsWithSilverAnatEntityCall(gene);
            
            if (calls == null || calls.isEmpty()) {
                log.info("No expression data for gene " + xref.ensemblId);
                return;
            }

            //we need to make sure that the ExpressionCalls are ordered in exactly the same way
            //for the display and for the clustering, otherwise the display will be buggy,
            //notably for calls with equal ranks. And we need to take into account
            //relations between Conditions for filtering them, which would be difficult to achieve
            //only by a query to the data source. So, we order them anyway.
            ConditionGraph organStageGraph = new ConditionGraph(
                    calls.stream().map(ExpressionCall::getCondition).collect(Collectors.toSet()),
                    serviceFactory);
            calls = ExpressionCall.filterAndOrderCallsByRank(calls, organStageGraph);
            
            // Identify redundant organ-stage calls
            final Set<ExpressionCall> redundantCalls = ExpressionCall.identifyRedundantCalls(
                    calls, organStageGraph);
            LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity = callService
                    .groupByAnatEntAndFilterCalls(calls, redundantCalls, true);

            // Create String representation of the XRef with expression information
            String prefixLine = xref.getUniprotId() + "   DR   Bgee; " + xref.getEnsemblId() + ";";
            ensemlbIdToXrefLine.put(xref.ensemblId,
                    prefixLine + " Expressed in " + callsByAnatEntity.size()
                            + " organ(s), highest expression level in "
                            + calls.get(0).getCondition().getAnatEntity().getName() + ".");
        });
        Instant end = Instant.now();
        log.debug("Time needed to retrieve expressionSummary of {} genes is {} hours", xrefList.size(),
                Duration.between(start, end).toHours());
        return log.exit(ensemlbIdToXrefLine);

    }

    /**
     * Sort Xrefs by UniProtKB IDs.
     * 
     * @param ensemblIdToXrefLines  A {@code Map} where keys correspond to Ensembl gene IDs 
     *                              and each value corresponds to UniProtKB Xref line.
     * @return                      The {@code LinkedHashMap} <String, String>
     */
    private static LinkedHashMap<String, String> sortXrefByUniprotId(Map<String, String> ensemblIdToXrefLines) {
        log.entry(ensemblIdToXrefLines);
        return log.exit(ensemblIdToXrefLines.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        // XXX: This removes duplicates, no?
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
    }

    /**
     * Write an XRef file according to Uniprot format. Each line contains: -
     * Uniprot ID - Ensembl ID used in Bgee - summary of the expression Ex:
     * H9G366 DR BGEE; ENSACAG00000000002; Expressed in 4 organs, higher
     * expression level in brain.
     * 
     * @param file              A {@code String} that is the path of the output file.
     * @param outputXrefLines   A {@code Collection} of {@code String} corresponding to all
     *                          Bgee Xrefs in UniProtKB.
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
