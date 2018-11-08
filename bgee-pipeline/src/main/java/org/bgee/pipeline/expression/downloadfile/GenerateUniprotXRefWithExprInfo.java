package org.bgee.pipeline.expression.downloadfile;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.expressiondata.ConditionGraphService;
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
 * @author  Frederic Bastian
 * @since Bgee 14 Jul 2017
 * @version Bgee 14 Nov 2018
 */
// FIXME: Add unit tests
public class GenerateUniprotXRefWithExprInfo {

    private final static Logger log = LogManager.getLogger(GenerateUniprotXRefWithExprInfo.class.getName());
    
    private final Supplier<ServiceFactory> serviceFactorySupplier;

    /**
     * Default constructor. 
     */
    public GenerateUniprotXRefWithExprInfo() {
        this(ServiceFactory::new);
    }

    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
     *                                      to be able to provide one to each thread.
     */
    public GenerateUniprotXRefWithExprInfo(Supplier<ServiceFactory> serviceFactorySupplier) {
        this.serviceFactorySupplier = serviceFactorySupplier;
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
    public void generate(String inputFileName, String outputFileName) {
        log.entry(inputFileName, outputFileName);
        
        // load UniProtKB Xrefs
        Set<XrefUniprotBean> xrefUniprotList = loadXrefFileWithoutExprInfo(inputFileName);

        // generate lines with expression info
        Map<String, Set<String>> ensemblIdToXrefLines = this.generateXrefLines(xrefUniprotList);

        // sort Xrefs by ensembl ID
        List<String> sortedEnsemblIdToXrefLines = GenerateUniprotXRefWithExprInfo
                .sortXrefByUniprotId(ensemblIdToXrefLines);

        // write XRef file
        this.writeXrefWithExpressionInfo(outputFileName, sortedEnsemblIdToXrefLines);

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
    public static Set<XrefUniprotBean> loadXrefFileWithoutExprInfo(String file) {
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
    private Map<String, Set<String>> generateXrefLines(Set<XrefUniprotBean> xrefList) {
        log.entry(xrefList);
        
        Instant start = Instant.now();

        //First, we build the ConditionGraph needed for filtering calls as on the gene page,
        //for each species present in the xref list.
        //This will avoid creating a new graph for each gene, to create it only once per species.
        final Set<Integer> speciesIds = xrefList.stream().map(xr -> xr.getSpeciesId()).collect(Collectors.toSet());
        final ServiceFactory serviceFactory = serviceFactorySupplier.get();
        final ConditionGraphService condGraphService = serviceFactory.getConditionGraphService();
        final EnumSet<CallService.Attribute> allCondParams = CallService.Attribute.getAllConditionParameters();
        final Map<Integer, ConditionGraph> condGraphBySpeId = Collections.unmodifiableMap(speciesIds.stream()
                .collect(Collectors.toMap(id -> id, id -> condGraphService.loadConditionGraph(id, allCondParams))));
        //Release resources before launching analyses in several threads
        serviceFactory.close();
        
        //retrieve expression information for each genes
        Map<XrefUniprotBean, String> expressionInfoByGene = xrefList.parallelStream().map(xref -> {
            // Retrieve expression calls
            ServiceFactory threadSpeServiceFactory = serviceFactorySupplier.get();
            CallService callService = threadSpeServiceFactory.getCallService();
            LinkedHashMap<AnatEntity, List<ExpressionCall>> callsByAnatEntity = callService
                    .loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                            new GeneFilter(xref.getSpeciesId(), xref.getEnsemblId()),
                            condGraphBySpeId.get(xref.getSpeciesId()));
            
            // If no expression for this gene in Bgee
            if (callsByAnatEntity == null || callsByAnatEntity.isEmpty()) {
                log.info("No expression data for gene " + xref.getEnsemblId());
                // Add null expression information for this gene
                return new AbstractMap.SimpleEntry<XrefUniprotBean, String>(xref, null);
            }
            
            // Create String representation of the XRef with expression information
            StringBuilder sb = new StringBuilder(" Expressed in ")
                    .append(callsByAnatEntity.size())
                    .append(" organ").append(callsByAnatEntity.size() > 1? "s": "")
                    .append(", highest expression level in ")
                    .append(callsByAnatEntity.keySet().iterator().next().getName());
                return new AbstractMap.SimpleEntry<XrefUniprotBean, String>(xref, sb.toString());

        }).filter(e -> e.getValue() != null)
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        
        Instant end = Instant.now();
        log.debug("Time needed to retrieve expressionSummary of {} genes is {} hours", xrefList.size(),
                Duration.between(start, end).toHours());
        
        return log.exit(xrefList.parallelStream().map( xref -> {
            String expressionInfo = expressionInfoByGene.get(xref);
            if (expressionInfo == null) {
                return new AbstractMap.SimpleEntry<String, String>(xref.getEnsemblId(), null);
            }
            StringBuilder sb = new StringBuilder(xref.getUniprotId())
                    .append("   DR   Bgee; ")
                    .append(xref.getEnsemblId())
                    .append(";")
                    .append(expressionInfo);
            return new AbstractMap.SimpleEntry<String, String>(xref.getEnsemblId(), sb.toString());
        }).filter(e -> e.getValue() != null)
        .collect(Collectors.toMap(e -> e.getKey(), 
                e -> new HashSet<String>(Arrays.asList(e.getValue())), 
                (v1, v2) -> {
                    Set<String> newSet = new HashSet<>(v1);
                    newSet.addAll(v2);
                    return newSet;
                })));
    }

    /**
     * Sort Xrefs by EnsemblIDs.
     * 
     * @param ensemblIdToXrefLines  A {@code Map} where keys correspond to Ensembl gene IDs 
     *                              and each value corresponds to UniProtKB Xref line.
     * @return                      The {@code List} where each element is {@code String} representing one well
     *                              formatted Uniprot XRef
     */
    private static List<String> sortXrefByUniprotId(Map<String, Set<String>> ensemblIdToXrefLines) {
        log.entry(ensemblIdToXrefLines);
        return log.exit(ensemblIdToXrefLines.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList()));
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
