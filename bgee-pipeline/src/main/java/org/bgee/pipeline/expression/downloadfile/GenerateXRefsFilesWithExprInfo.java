package org.bgee.pipeline.expression.downloadfile;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.expressiondata.ConditionGraphService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.CommandRunner;
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
public class GenerateXRefsFilesWithExprInfo {

    private final static Logger log = LogManager.getLogger(GenerateXRefsFilesWithExprInfo.class.getName());
    
    private final Supplier<ServiceFactory> serviceFactorySupplier;
    
    private enum XrefsFileType {
    	UNIPROT(1, new HashSet<>(Arrays.asList(7237)), "XRefBgee.txt"),
    	GENE_CARDS(3, new HashSet<>(Arrays.asList(7897)), "GeneCards_XRefBgee.tsv"),
    	WIKIDATA(10, new HashSet<>(Arrays.asList(7237,7897)), "wikidataBotInput.txt");
    
    	private final Integer numberOfAnatEntitiesToWrite;
    	private final Set<Integer> speciesIds;
    	private final String fileName;
    
    	private XrefsFileType(Integer numerOfAnatEntitiesToWrite, Set<Integer> speciesIds,
    			String fileName) {
    		this.numberOfAnatEntitiesToWrite = numerOfAnatEntitiesToWrite;
    		this.speciesIds = speciesIds;
    		this.fileName = fileName;
    	}
        	
    	public Integer getNumberOfAnatEntitiesToWrite () {
    		return this.numberOfAnatEntitiesToWrite;
    	}
    	
    	public Set<Integer> getSpeciesIds() {
    		return this.speciesIds;
    	}
    	
    	public String getFileName() {
    		return this.fileName;
    	}
    }
    

    /**
     * Default constructor. 
     */
    public GenerateXRefsFilesWithExprInfo() {
        this(ServiceFactory::new);
    }

    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
     *                                      to be able to provide one to each thread.
     */
    public GenerateXRefsFilesWithExprInfo(Supplier<ServiceFactory> serviceFactorySupplier) {
        this.serviceFactorySupplier = serviceFactorySupplier;
    }
    
    // XXX: Use service when it will be implemented
    /**
     * Main method to generate UniProtKB Xrefs file with expression information from
     * the Bgee database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the input file containing XRefs UniProtKB - geneId
     * <li>path to the file where to write Xrefs with expression information into.
     * <li>list of xrefs files to generate
     * </ol>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length != 4) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments."));
        }

        GenerateXRefsFilesWithExprInfo expressionInfoGenerator = new GenerateXRefsFilesWithExprInfo();
        Set<String> wikidataUberonClasses = getWikidataUberonClasses(args[2]);
        expressionInfoGenerator.generate(args[0], args[1], wikidataUberonClasses, 
        		CommandRunner.parseListArgument(args[3]));
        
        log.traceExit();
    }

    /**
     * Generate UniProtKB Xrefs file with expression information from the Bgee database. 
     *
     * @param inputFileName     		A {@code String} that is the path to the file containing 
     *                          		XRefs UniProtKB - geneId mapping.
     * @param outputDir		    		A {@code String} that is the path to the directory where XRefs files
     * 									will be created.
     * @param wikidataUberonClasses		A {@code Set} that contains all uberon terms inserted in wikidata.
     * @param xrefsFileType     		A {@code List} of {@code String} corresponding to string representation of
     * 									xrefs files to generate
     */
    public void generate(String inputFileName, String outputDir, Set<String> wikidataUberonClasses, 
    		List<String> xrefsFileType) {
        log.traceEntry("{}, {}, {}, {}",inputFileName, outputDir, wikidataUberonClasses, xrefsFileType);
         
        
       // detect requested xrefs file types
        Set<XrefsFileType> requestedXrefFileTypes = this.getXrefFileType(xrefsFileType);
        
        // detect species subset required to generate all xrefs files
        Set<Integer> speciesIds = retrieveSpeciesIds(requestedXrefFileTypes);

        Map<Integer, Map<String, Set<String>>> uniprotXrefByGeneIdBySpeciesId = 
        		requestedXrefFileTypes.contains(XrefsFileType.UNIPROT) ? 
        				loadXrefFileWithoutExprInfo(inputFileName, speciesIds) : null;
        ServiceFactory serviceFactory = serviceFactorySupplier.get();
        GeneService geneService = serviceFactory.getGeneService();
        //TODO for now all genes of a species are retrieved even if this species is only used
        // for Uniprot Xrefs. We should filter based on uniprotXrefByGeneIdBySpeciesId for
        // species only used to generate UniPort XRefs
        Set<GeneFilter> geneFilters = speciesIds.stream().map(sp -> new GeneFilter(sp)).collect(Collectors.toSet());
        Set<Gene> genes = geneService.loadGenes(geneFilters, false, false, true).collect(Collectors.toSet());
        serviceFactory.close();
        
        //retrieve gene expression
        //Map<String, Set<String>> ensemblIdToXrefLines = this.generateXrefExpressonInfo(xrefList, speciesIds);
        
        // generate lines with expression info
        Map<XrefsFileType, Map<String, List<String>>> geneIdToXrefLinesbyXrefsFileType = 
        		this.generateXrefs(genes, speciesIds, requestedXrefFileTypes, uniprotXrefByGeneIdBySpeciesId,
        				wikidataUberonClasses);

        // write XRef file
        this.writeXrefWithExpressionInfo(requestedXrefFileTypes, geneIdToXrefLinesbyXrefsFileType, 
        		outputDir);

        log.traceExit();
    }
    
    private static Set<String> getWikidataUberonClasses(String filePath) {
    	Set<String> wikidataUberonClasses = new HashSet<>();
    	try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
    		stream.forEach(ub -> {
    			wikidataUberonClasses.add(ub);
    		});
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return wikidataUberonClasses;
    }
    
    private Set<XrefsFileType> getXrefFileType (List<String> wantedXrefsFileTypes) {
    	log.traceEntry("{}",wantedXrefsFileTypes);
    	if(!wantedXrefsFileTypes.containsAll(wantedXrefsFileTypes)) {
    		throw log.throwing(new IllegalArgumentException("some xrefs file types does not exist"));
    	}
    	Set<XrefsFileType> xrefsFileTypes = EnumSet.allOf(XrefsFileType.class)
    	.stream()
    			.filter(x -> wantedXrefsFileTypes.contains(x.name()))
    			.collect(Collectors.toSet());
    	return log.traceExit(xrefsFileTypes);
    }
    
    private Set<Integer> retrieveSpeciesIds(Set<XrefsFileType> xrefsFileTypes) {
    	ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
    	SpeciesService speService = serviceFactory.getSpeciesService();
    	// retrieve Set of species required to generate Xref files. Create an empty Set if all 
    	// species are necessary
    	log.debug(xrefsFileTypes.iterator().next().getSpeciesIds());
    	Set<Integer> speciesIds = (xrefsFileTypes.stream().filter(x -> x.speciesIds == null).count() > 0) ?
    		new HashSet<Integer>() :
    		xrefsFileTypes.stream().map(XrefsFileType::getSpeciesIds).flatMap(Set::stream)
    		.collect(Collectors.toSet());
    	return log.traceExit(speService.loadSpeciesByIds(speciesIds, false).stream().map(s -> s.getId())
    			.collect(Collectors.toSet()));
    }

    //XXX Could load uniprot XRefs from the database rather than from a file generated previously.
    /**
     * Read the UniProtKB Xref file without expression information and store lines
     * into a {@code List} of {@code XrefUniprotBean}s.
     * 
     * @param file  A {@code String} that is the name of the file that contains
     *              all UniProtKB Xrefs without expression information.
     * @return      A {@code Map} where keys are speciesIds and values are a {@code Map} where keys are
     * 				a gene ID and values are a {@code Set} of uniprot IDs
     * @throws UncheckedIOException If an error occurred while trying to read the {@code file}.
     */
    public static Map<Integer,Map<String,Set<String>>> loadXrefFileWithoutExprInfo(String file, Set<Integer> speciesIds) {
        log.traceEntry("{}, {}", file, speciesIds);
        
        Map<Integer, Map<String, Set<String>>> xrefsBySpeciesIdAndGeneId = new HashMap<>();
        
        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(file), 
        		CsvPreference.TAB_PREFERENCE)) {
            final String[] header = beanReader.getHeader(false);
            final CellProcessor[] processors = new CellProcessor[] { 
                    new NotNull(), // uniprotXrefId
                    new NotNull(), // geneId
                    new NotNull(new ParseInt()) // speciesId
            };
            
            XrefUniprotBean xrefBean;
            while ((xrefBean = beanReader.read(XrefUniprotBean.class, header, processors)) != null) {
            	if(speciesIds.contains(xrefBean.getSpeciesId())) {
            		
            		xrefsBySpeciesIdAndGeneId.computeIfAbsent(xrefBean.getSpeciesId(), k -> new HashMap<>());
            		xrefsBySpeciesIdAndGeneId.get(xrefBean.getSpeciesId())
            			.computeIfAbsent(xrefBean.getGeneId(), k -> new HashSet<>());
            		xrefsBySpeciesIdAndGeneId.get(xrefBean.getSpeciesId()).get(xrefBean.getGeneId())
            			.add(xrefBean.getUniprotId());
//	                Set <String> uniprotXrefs = new HashSet<String>();
//	                if( xrefsBySpeciesIdAndGeneId.containsKey(xrefBean.speciesId) && 
//	                		xrefsBySpeciesIdAndGeneId.get(xrefBean.speciesId)
//	                		.containsKey(xrefBean.getGeneId())) {
//	                	uniprotXrefs = xrefsBySpeciesIdAndGeneId.get(xrefBean.speciesId)
//	                			.get(xrefBean.getGeneId());
//	
//	                } 
//	                uniprotXrefs.add(xrefBean.getUniprotId());
//	                xrefsBySpeciesIdAndGeneId.put(xrefBean.speciesId, 
//	                		new HashMap<>(Map.of(xrefBean.getGeneId(), uniprotXrefs)));
	            }
            }
            
        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can not read file " + file, e));
        }
        return log.traceExit(xrefsBySpeciesIdAndGeneId);
    }

    /**
     * Generate UniprotKB XRef lines with expression information for one gene. These lines contains:
     * - number of anatomical entities where this gene is expressed;
     * - name of the anatomical entity where this gene has the higher expression level.
     * 
     * @param xrefList  A {@code List} of {@code XrefUniprotBean}s containing information retrieved 
     *                  from Bgee database and needed to create uniprot cross-references.
     * @return          The {@code Map} where keys correspond to gene IDs and each
     *                  value corresponds to one well formatted UniProtKB Xref line.
     */
    private Map<XrefsFileType, Map<String, List<String>>> generateXrefs(Set<Gene> genes, 
    		Set<Integer> speciesIds, Set<XrefsFileType> requestedXrefFileTypes, 
    		Map<Integer, Map<String, Set<String>>> uniprotXrefs, Set<String> wikidataUberonClasses) {
        log.traceEntry("{}, {}, {}, {}, {}", genes, speciesIds, requestedXrefFileTypes, uniprotXrefs,
        		wikidataUberonClasses);
        
        Instant start = Instant.now();

        //First, we build the ConditionGraph needed for filtering calls as on the gene page,
        //for each species present in the xref list.
        //This will avoid creating a new graph for each gene, to create it only once per required species.
        final ServiceFactory serviceFactory = serviceFactorySupplier.get();
        final ConditionGraphService condGraphService = serviceFactory.getConditionGraphService();
        final EnumSet<CallService.Attribute> allCondParams = CallService.Attribute.getAllConditionParameters();
        final Map<Integer, ConditionGraph> condGraphBySpeId = Collections.unmodifiableMap(speciesIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> condGraphService.loadConditionGraphFromSpeciesIds(
                                Collections.singleton(id), null, allCondParams))));
        //Release resources before launching analyses in several threads
        serviceFactory.close();
        
        // as key and the corresponding xref as value.
        Map<XrefsFileType, Map<String, List<String>>> xrefsLinesByFileTypeByGene = new HashMap<>();
        
        //retrieve expression information for each xref (unique geneId, speciesId, uniprotId)
        genes.parallelStream().forEach(g -> {
        	
        	Integer speciesId = g.getSpecies().getId();
        	String geneId = g.getGeneId();
        	
        	// Retrieve expression calls
            ServiceFactory threadSpeServiceFactory = serviceFactorySupplier.get();
            CallService callService = threadSpeServiceFactory.getCallService();
            
            // keep only the expressionCall at anat entity level. Comming from a LinkedHashMap they
            // are already ordered by expressionlevel.
            //XXX If in the future we plan to add more information than just the anat. entity, it will
            // then be mandatory to keep calls at condition level ordered by anat. entity
            List<ExpressionCall> callsByAnatEntity = new ArrayList<>(
                    callService.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                            new GeneFilter(speciesId , geneId),
                            condGraphBySpeId.get(speciesId)).keySet());
            
            // If no expression for this gene in Bgee
            if (callsByAnatEntity == null || callsByAnatEntity.isEmpty()) {
                log.info("No expression data for gene " + geneId);
            } else {
            	if(requestedXrefFileTypes.contains(XrefsFileType.UNIPROT)
            			&& XrefsFileType.UNIPROT.getSpeciesIds().contains(speciesId)) {
            		Set<String> filteredUniProtIds = uniprotXrefs.containsKey(speciesId) && 
            				uniprotXrefs.get(speciesId).containsKey(geneId) ?
            				uniprotXrefs.get(speciesId).get(geneId) : null;
            		if(filteredUniProtIds != null) {
            			xrefsLinesByFileTypeByGene.computeIfAbsent(XrefsFileType.UNIPROT, k -> new HashMap<>())
            			.putAll(generateXrefLineUniProt(g, callsByAnatEntity, filteredUniProtIds));
            		}
            	}
            	if(requestedXrefFileTypes.contains(XrefsFileType.GENE_CARDS)
            			&& XrefsFileType.GENE_CARDS.getSpeciesIds().contains(speciesId)) {
            		xrefsLinesByFileTypeByGene.computeIfAbsent(XrefsFileType.GENE_CARDS, k -> new HashMap<>())
            		.putAll(generateXrefLineGeneCards(g, callsByAnatEntity));
            	}
            	if(requestedXrefFileTypes.contains(XrefsFileType.WIKIDATA)
            			&& XrefsFileType.WIKIDATA.getSpeciesIds().contains(speciesId)) {
            		xrefsLinesByFileTypeByGene.computeIfAbsent(XrefsFileType.WIKIDATA, k -> new HashMap<>())
            		.putAll(generateXrefLineWikidata(g, callsByAnatEntity, wikidataUberonClasses));
            	}
            }
        });
        
        Instant end = Instant.now();
        log.info("Time needed to retrieve expressionSummary of {} genes is {} hours", genes.size(),
                Duration.between(start, end).toHours());
        
        return log.traceExit(xrefsLinesByFileTypeByGene);
        
    }
    
    private Map<String, List<String>> generateXrefLineUniProt(Gene gene, 
    		List<ExpressionCall> callsByAnatEntity, Set<String> uniprotIds) {
        
    	List<String> XRefLines = uniprotIds.stream().map(uid -> {
	    	// Create String representation of the XRef with expression information
	    	StringBuilder sb = new StringBuilder(uid)
	                .append("   DR   Bgee; ")
	                .append(gene.getGeneId())
	                .append(";")
	                .append(" Expressed in ");
	    	int numberAnatEntityToWrite = XrefsFileType.UNIPROT.getNumberOfAnatEntitiesToWrite();
	    	
	    	sb.append(String.join(", ", callsByAnatEntity.stream()
	    			.limit(numberAnatEntityToWrite)
	    			.map(c -> c.getCondition().getAnatEntity().getName())
	    			.collect(Collectors.toList())));
	
		    if (callsByAnatEntity.size() > numberAnatEntityToWrite ) {
		        sb.append(" and ")
		        .append(callsByAnatEntity.size()-numberAnatEntityToWrite)
		        .append(" other tissue").append(callsByAnatEntity.size() > (numberAnatEntityToWrite + 1)? "s": "");
		    }
		    sb.append(".");
		    return sb.toString();
		})
    	.collect(Collectors.toList());
    	return Map.of(gene.getGeneId(), XRefLines);
    }
    
    //TODO: quick and dirty version. Should use SuperCSV
    private Map<String, List<String>> generateXrefLineGeneCards(Gene gene, 
    		List<ExpressionCall> callsByAnatEntity) {
        
    	String geneId = gene.getGeneId();
    	
    	String geneCardsURL = "https://www.genecards.org/cgi-bin/carddisp.pl?gene=";
    	String bgeeURL = "https://bgee.org/?page=gene&gene_id=";
    	// Create String representation of the XRef with expression information
    	StringBuilder sb = new StringBuilder(geneId);
    	int numberAnatEntityToWrite = XrefsFileType.GENE_CARDS.getNumberOfAnatEntitiesToWrite();
    	sb.append("\tExpressed in ");
    	sb.append(String.join(", ", callsByAnatEntity.stream()
    			.limit(numberAnatEntityToWrite)
    			.map(c -> c.getCondition().getAnatEntity().getName())
    			.collect(Collectors.toList())));

	    if (callsByAnatEntity.size() > numberAnatEntityToWrite ) {
	        sb.append(" and ")
	        .append(callsByAnatEntity.size()-numberAnatEntityToWrite)
	        .append(" other tissue").append(callsByAnatEntity.size() > (numberAnatEntityToWrite + 1)? "s": "");
	    }
	    sb.append(".");
	    sb.append("\t" + geneCardsURL + geneId);
	    sb.append("\t" + bgeeURL + geneId);
    	return Map.of(geneId, List.of(sb.toString()));
    }
    
  //TODO: quick and dirty version. Could use SuperCSV
    private Map<String, List<String>> generateXrefLineWikidata(Gene gene, 
    		List<ExpressionCall> callsByAnatEntity, Set<String> wikidataUberonClasses) {
        int uberonClassesWritten = 0;
        Iterator<ExpressionCall> callsIterator = callsByAnatEntity.iterator();
        List<String> wikidataLines= new ArrayList<>();
        while (uberonClassesWritten < 10 && callsIterator.hasNext()) {
        	String uberonId = callsIterator.next().getCondition().getAnatEntityId();
        	if(wikidataUberonClasses.contains(uberonId)) {
        		String modifiedUberonId = uberonId.contains("UBERON:") ? uberonId.substring(7) : 
        			uberonId.replace(":", "_");
        		wikidataLines.add(ensemblId + "\t" + modifiedUberonId);
        		uberonClassesWritten++;
        	}
        }
    	return Collections.singletonMap(geneId, wikidataLines);
    }
    
    /**
     * Sort Xrefs by gene IDs.
     * 
     * @param geneIdToXrefLines  A {@code Map} where keys correspond to gene IDs 
     *                              and each value corresponds to UniProtKB Xref line.
     * @return                      The {@code List} where each element is {@code String} representing one well
     *                              formatted Uniprot XRef
     */
    private static List<String> sortXrefByGeneId(Map<String, List<String>> geneIdToXrefLines) {
        log.traceEntry("{}",geneIdToXrefLines);
        return log.traceExit(ensemblIdToXrefLines.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                // to remove genes having no uniprot IDs
                .filter(e -> e.getValue() != null)
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList()));
    }

    /**
     * Write an XRef file according to Uniprot format. Each line contains: -
     * Uniprot ID - gene ID used in Bgee - summary of the expression Ex:
     * H9G366 DR BGEE; ENSACAG00000000002; Expressed in brain and 3 other tissues.
     * 
     * @param file              A {@code String} that is the path of the output file.
     * @param outputXrefLines   A {@code Collection} of {@code String} corresponding to all
     *                          Bgee Xrefs in UniProtKB.
     */
    private void writeXrefWithExpressionInfo(Set<XrefsFileType> requestedXrefFileTypes, 
    		Map<XrefsFileType, Map<String, List<String>>> outputXrefLines, String outputDir) {
    	
        log.traceEntry("{}, {}, {}", requestedXrefFileTypes, outputXrefLines, outputDir);
        for(XrefsFileType xrefFileType : requestedXrefFileTypes) {
            // sort Xrefs by gene ID
        	List<String> sortedGeneIdToXrefLines = GenerateXRefsFilesWithExprInfo
                  .sortXrefByGeneId(outputXrefLines.get(xrefFileType));
	    	try {
	            Files.write(Paths.get(outputDir, xrefFileType.getFileName()), sortedGeneIdToXrefLines, 
	            		Charset.forName("UTF-8"));
	        } catch (IOException e) {
	            throw log.throwing(new UncheckedIOException("Can't write file " + 
	            		xrefFileType.getFileName(), e));
	        }
        }
    }

    public static class XrefUniprotBean {

        private String uniprotId;
        private String geneId;
        private Integer speciesId;

        public XrefUniprotBean() {
        }

        public XrefUniprotBean(String uniprotId, String geneId, Integer speciesId) {
            this.uniprotId = uniprotId;
            this.geneId = geneId;
            this.speciesId = speciesId;
        }

        public String getUniprotId() {
            return uniprotId;
        }

        public void setUniprotId(String uniprotId) {
            this.uniprotId = uniprotId;
        }

        public String getGeneId() {
            return geneId;
        }

        public void setGeneId(String geneId) {
            this.geneId = geneId;
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
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
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
            if (geneId == null) {
                if (other.geneId != null)
                    return false;
            } else if (!geneId.equals(other.geneId))
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
            return "XrefUniprotBean [uniprotId=" + uniprotId + ", geneId=" + geneId 
            		+ ", speciesId=" + speciesId
                    + "]";
        }

    }

}
