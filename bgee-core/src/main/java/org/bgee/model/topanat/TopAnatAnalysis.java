package org.bgee.model.topanat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneService;
import org.bgee.model.topanat.exception.InvalidForegroundException;
import org.bgee.model.topanat.exception.InvalidSpeciesGenesException;
import org.bgee.model.topanat.exception.RAnalysisException;

/**
 * TODO comment me.
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 13, Sept. 2015
 */
public class TopAnatAnalysis {

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatAnalysis.class.getName());

    /**
     * 
     */
    private final static String FILE_PREFIX = "topAnat_";
    
    private final static String TMP_FILE_SUFFIX = ".tmp";
    
    protected final static AnatEntity FAKE_ANAT_ENTITY_ROOT = new AnatEntity("BGEE:0", "root", 
            "A root added on top of all orphan terms.");

    /**
     * 
     */
    private final TopAnatParams params;

    /**
     * 
     */
    private final TopAnatRManager rManager;

    /**
     * 
     */
    private final BgeeProperties props;

    /**
     * 
     */
    private final CallService callService;

    /**
     * 
     */
    private final AnatEntityService anatEntityService;

    /**
     * 
     */
    private final GeneService geneService;

    /**
     * 
     */
    private final TopAnatController controller;

    /**
     * @param params
     * @param props
     * @param serviceFactory
     * @param rManager
     */
    public TopAnatAnalysis(TopAnatParams params, BgeeProperties props, 
            ServiceFactory serviceFactory, TopAnatRManager rManager, TopAnatController controller) {
        log.entry(params, props, serviceFactory, rManager); 
        this.params = params;
        this.anatEntityService = 
                serviceFactory.getAnatEntityService(); 
        this.callService = serviceFactory.getCallService();
        this.geneService = serviceFactory.getGeneService();
        this.rManager = rManager;
        this.props = props;
        this.controller = controller;
    }

    /**
     * @throws IOException
     * @throws InvalidForegroundException 
     * @throws InvalidSpeciesException 
     */
    protected TopAnatResults proceedToAnalysis() throws IOException, InvalidForegroundException, 
    InvalidSpeciesGenesException, RAnalysisException {
        log.entry();
        log.info("Result directory: {}", this.getResultDirectory());

        // Validate and load the gene in the foreground and background
        this.validateForegroundAndBackground();
        
        //Do the analysis/file creation only if results don't already exist
        if (!this.isAnalysisDone()) {
            //create write directory for this analysis
            this.createWriteDirectoryIfNotExist();
            
            // Generate anatomic entities data
            this.generateAnatEntitiesFiles();
            
            // Generate call data
            this.generateGenesToAnatEntitiesAssociationFile();
            
            // Write the params on the disk
            this.generateTopAnatParamsFile();
            
            // Generate R code and write it on the disk
            this.generateRCodeFile();
            
            // Copy the Rscript file to the working directory, if it doesn't already exists
            String sourceFunctionFileName = TopAnatAnalysis.class.getResource(
                    this.props.getTopAnatFunctionFile()).getPath();
            Path source = Paths.get(sourceFunctionFileName);
            File targetFunctionFile = new File(
                    this.getResultDirectoryPath() + 
                    source.getFileName());
            Path target = Paths.get(targetFunctionFile.getPath());
            if (!targetFunctionFile.exists()) {
                try{
                    this.controller.acquireReadLock(sourceFunctionFileName);
                    this.controller.acquireWriteLock(targetFunctionFile.getPath());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } finally{
                    this.controller.releaseReadLock(sourceFunctionFileName);
                    this.controller.releaseWriteLock(targetFunctionFile.getPath());
                }
            }
            
            // Run the R analysis
            this.runRcode();
            
            if(this.params.isWithZip()){
                // create the zip file
                this.generateZipFile();
            }
        }

        // return the result
        return log.exit(new TopAnatResults(
                this.params,
                this.getResultDirectory(), 
                this.getResultFileName(false),
                this.getResultPDFFileName(false),
                this.getRScriptAnalysisFileName(false),
                this.getParamsOutputFileName(false),
                this.getAnatEntitiesNamesFileName(false),
                this.getAnatEntitiesRelationshipsFileName(false),
                this.getGeneToAnatEntitiesFileName(false),
                this.getRScriptConsoleFileName(),
                this.getZipFileName(false),
                this.controller)
                );
    }

    /***
     * @throws InvalidForegroundException
     * @throws InvalidSpeciesException 
     */
    private void validateForegroundAndBackground() throws InvalidForegroundException, 
    InvalidSpeciesGenesException{
        // First check whether the foreground match the background
        if(this.params.getSubmittedBackgroundIds() != null && 
                !this.params.getSubmittedBackgroundIds().isEmpty() && 
                !this.params.getSubmittedBackgroundIds().containsAll(
                        this.params.getSubmittedForegroundIds())){

            Set<String> notIncludedIds = new HashSet<String>(this.params.getSubmittedForegroundIds());
            notIncludedIds.removeAll(this.params.getSubmittedBackgroundIds());
            throw new InvalidForegroundException("All foreground Ids are not included "
                    + "in the background",notIncludedIds);
        }
        
        //Check that there are more genes in the custom background than the node size parameter, 
        //otherwise it is impossible to get results. 
        if(this.params.getSubmittedBackgroundIds() != null && 
                !this.params.getSubmittedBackgroundIds().isEmpty() && 
                this.params.getNodeSize() > this.params.getSubmittedBackgroundIds().size()) {
            throw log.throwing(new IllegalStateException("It is impossible to obtain results "
                    + "if the node size parameter is greater than the number of genes "
                    + "in the background."));
        }

        Set<String> allGeneIds = new HashSet<String>(this.params.getSubmittedForegroundIds());
        if (this.params.getSubmittedBackgroundIds() != null) {
            allGeneIds.addAll(this.params.getSubmittedBackgroundIds());
        }
        allGeneIds.removeAll(this.geneService.loadGenesByIdsAndSpeciesIds(allGeneIds, 
                Arrays.asList(this.params.getSpeciesId())).stream()
                .map(Gene::getEnsemblGeneId)
                .collect(Collectors.toSet()));
        if (!allGeneIds.isEmpty()) {
            throw log.throwing(new InvalidSpeciesGenesException("Some gene IDs are unrecognized, "
                    + "or does not belong to the selected species",allGeneIds));
        }
    }

    /**
     * 
     * @throws IOException
     */
    private void runRcode() throws IOException, RAnalysisException {
        log.entry();

        log.info("Run R code...");

        String fileName = this.getResultFilePath(false);
        String tmpFileName = this.getResultFilePath(true);
        String pdfFileName = this.getResultPDFFilePath(false);
        String tmpPdfFileName = this.getResultPDFFilePath(true);

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalFile = Paths.get(fileName);
        Path tmpPdfFile = Paths.get(tmpPdfFileName);
        Path finalPdfFile = Paths.get(pdfFileName);

        String namesFileName = this.getAnatEntitiesNamesFilePath(false);
        String relsFileName = this.getAnatEntitiesRelationshipsFilePath(false);
        String geneToAnatEntitiesFile = this.getGeneToAnatEntitiesFilePath(false);

        try {

            this.controller.acquireReadLock(namesFileName);
            this.controller.acquireReadLock(relsFileName);
            this.controller.acquireReadLock(geneToAnatEntitiesFile);

            this.controller.acquireWriteLock(tmpFileName);
            this.controller.acquireWriteLock(fileName);

            this.controller.acquireWriteLock(tmpPdfFileName);
            this.controller.acquireWriteLock(pdfFileName);

            //check, AFTER having acquired the locks, that the final files do not 
            //already exist (maybe another thread generated the files before this one 
            //acquires the lock)
            if (Files.exists(finalFile)) {
                log.info("Result files already generated.");
                log.exit();return;
            }

            try {
                this.rManager.performRFunction(this.getRScriptConsoleFilePath());
            //TODO: This exception specific to RCaller should be encapsulated into RManager    
            } catch (com.github.rcaller.exception.ParseException e) {
                log.catching(e);
                //RCaller throws an exception when there is no result, with a message 
                //corresponding to the regex: 
                //"^.*?Can not parse output: The generated file .+? is empty.*$". 
                //The problem is, it can also throw this exception for some other types of errors. 
                //So we check the last line of the R console log: if it does not contain  
                //"No result, creating an empty result file", then we have an error. 
                //TODO: unit test a case when the ParseException is launched because of no result, 
                //and a case when it is for an actual problem (e.g., package not installable)
                try (ReversedLinesFileReader reverseReader = new ReversedLinesFileReader(
                        new File(this.getRScriptConsoleFilePath()))) {
                    String lastLine = reverseReader.readLine();
                    if (lastLine == null || !lastLine.contains(TopAnatRManager.NO_RESULT_MESSAGE_PREFIX)) {
                        throw log.throwing(new RAnalysisException("The R analysis threw "
                                + "an Exception for unknown reason. Last line of the R console: "
                                + lastLine, e));
                    }
                }
                //we don't create an empty result file: either it was created by R if there was no result, 
                //or, if there was an error, then we don't want to prevent re-runnning the analysis.

            //TODO: This exception specific to RCaller should be encapsulated into RManager    
            } catch (com.github.rcaller.exception.ExecutionException e) {
                //because RCaller throws this exception in case of InterruptedException, 
                //log as debug
                throw log.throwing(Level.DEBUG, 
                        new RAnalysisException("The R analysis threw an Exception ", e));
            }

            this.move(tmpFile, finalFile, false);
            //maybe it was not requested to generate the pdf, or there was no results 
            //and this file was not generated
            if (Files.exists(tmpPdfFile)) {
                this.move(tmpPdfFile, finalPdfFile, false);
            }

        } finally {
            Files.deleteIfExists(tmpFile);
            Files.deleteIfExists(tmpPdfFile);
            this.controller.releaseWriteLock(tmpFileName);
            this.controller.releaseWriteLock(fileName);
            this.controller.releaseWriteLock(tmpPdfFileName);
            this.controller.releaseWriteLock(pdfFileName);
            this.controller.releaseReadLock(namesFileName);
            this.controller.releaseReadLock(relsFileName);
            this.controller.releaseReadLock(geneToAnatEntitiesFile);   
        }

        log.info("Result file path: {}", this.getResultFilePath(false));

        log.exit();
    }

    /**
     * 
     * @throws IOException
     */
    private void generateRCodeFile() throws IOException {
        log.entry();

        log.info("Generating R code file...");

        String fileName = this.getRScriptAnalysisFilePath(false);
        String tmpFileName = this.getRScriptAnalysisFilePath(true);

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalFile = Paths.get(fileName);

        try {
            this.controller.acquireWriteLock(tmpFileName);
            this.controller.acquireWriteLock(fileName);

            //if the file already exists, we remove it, because we need anyway to call writeRcodeFile, 
            //to set the R code (bad design)
            if (Files.exists(finalFile)) {
                log.info("R code file already generated, removing it to reload the R code.");
                Files.delete(finalFile);
            }

            this.writeRcodeFile(tmpFileName);

            this.move(tmpFile, finalFile, true);

        } finally {
            Files.deleteIfExists(tmpFile);
            this.controller.releaseWriteLock(tmpFileName);
            this.controller.releaseWriteLock(fileName);
        }

        log.info("Rcode file path: {}", 
                this.getRScriptAnalysisFilePath(false));
        log.exit();
    }


    /**
     */
    private void writeRcodeFile(String RcodeFile) throws IOException {
        log.entry(RcodeFile);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(RcodeFile)))) {
            out.println(this.rManager.generateRCode(
                    this.getResultDirectoryPath(), 
                    this.getResultFileName(true),
                    this.getResultPDFFileName(true),
                    this.getAnatEntitiesNamesFileName(false),
                    this.getAnatEntitiesRelationshipsFileName(false),
                    this.getGeneToAnatEntitiesFileName(false),
                    this.params.getSubmittedForegroundIds()));
        }

        log.exit();
    }

    private void createWriteDirectoryIfNotExist() {
        log.entry();
        //acquire the write lock before checking if the directory exists, 
        //so that we can create it immediately. 
        String dir = this.getResultDirectoryPath();
        try {
            this.controller.acquireWriteLock(dir);
            File newDir = new File(dir);
            if (!newDir.exists()) {
                newDir.mkdirs();
            }
        } finally {
            this.controller.releaseWriteLock(dir);
        }
        log.exit();
    }
    /**
     * 
     * @throws IOException
     */
    private void generateAnatEntitiesFiles() throws IOException {
        log.entry();

        log.info("Generating AnatEntities files...");

        //These files are general and are created in the general directory to be cached, 
        //before being copied to the analysis result directory
        String namesFileName = this.getAnatEntitiesNamesFileName(false);
        String namesTmpFileName = this.getAnatEntitiesNamesFileName(true);
        String relsFileName = this.getAnatEntitiesRelationshipsFileName(false);
        String relsTmpFileName = this.getAnatEntitiesRelationshipsFileName(true);

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path namesTmpFile = Paths.get(this.props.getTopAnatResultsWritingDirectory(), 
                namesTmpFileName);
        Path finalNamesFile = Paths.get(this.props.getTopAnatResultsWritingDirectory(), 
                namesFileName);
        log.debug("Absolute path to name file: {}", namesTmpFile.toAbsolutePath());
        Path relsTmpFile = Paths.get(this.props.getTopAnatResultsWritingDirectory(), 
                relsTmpFileName);
        Path finalRelsFile = Paths.get(this.props.getTopAnatResultsWritingDirectory(), 
                relsFileName);

        try {
            this.controller.acquireWriteLock(namesTmpFile.toString());
            this.controller.acquireWriteLock(finalNamesFile.toString());
            this.controller.acquireWriteLock(relsTmpFile.toString());
            this.controller.acquireWriteLock(finalRelsFile.toString());

            //check, AFTER having acquired the locks, that the final files do not 
            //already exist (maybe another thread generated the files before this one 
            //acquires the lock)
            if (!Files.exists(finalNamesFile) || !Files.exists(finalRelsFile)) {
                log.info("AnatEntities files not already generated.");

                this.writeAnatEntitiesAndRelationsToFiles(namesTmpFile.toString(), relsTmpFile.toString());
                
                this.move(namesTmpFile, finalNamesFile, true);
                this.move(relsTmpFile, finalRelsFile, true);
            }

            //Now, we copy them to the analysis result directory, if they don't already exist
            Path namesPath = Paths.get(this.getAnatEntitiesNamesFilePath(false));
            if (!Files.exists(namesPath)) {
                Files.copy(finalNamesFile, namesPath, StandardCopyOption.REPLACE_EXISTING);
            }
            Path relsPath = Paths.get(this.getAnatEntitiesRelationshipsFilePath(false));
            if (!Files.exists(relsPath)) {
                Files.copy(finalRelsFile, relsPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            Files.deleteIfExists(namesTmpFile);
            Files.deleteIfExists(relsTmpFile);
            this.controller.releaseWriteLock(namesTmpFile.toString());
            this.controller.releaseWriteLock(finalNamesFile.toString());
            this.controller.releaseWriteLock(relsTmpFile.toString());
            this.controller.releaseWriteLock(finalRelsFile.toString());
        }

        log.info("anatEntitiesNamesFilePath: {} - relationshipsFilePath: {}", 
                this.getAnatEntitiesNamesFilePath(false), 
                this.getAnatEntitiesRelationshipsFilePath(false));
        log.exit();
    }

    /**
     * 
     * @param AnatEntitiesNameFile
     * @throws IOException
     */
    //TODO: do a regression test, with the relations between anatomical entities 
    //defining several roots to the ontology, and including anatomical entities 
    //with no relation at all (no ancestors nor descendants)
    private void writeAnatEntitiesAndRelationsToFiles(String anatEntitiesNameFile, 
            String anatEntitiesRelFile) throws IOException {
        log.entry(anatEntitiesNameFile, anatEntitiesRelFile);

        //we need to get the anat. entities, both for anatEntitiesNameFile, and for 
        //correct generation of the anatEntitiesRelFile
        Set<AnatEntity> entities = this.anatEntityService.loadAnatEntitiesBySpeciesIds(
                Arrays.asList(this.params.getSpeciesId())).collect(Collectors.toSet());
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(anatEntitiesNameFile)))) {
            entities.stream().forEach(entity 
                    -> out.println(entity.getId() + "\t" + entity.getName().replaceAll("'", "")));
            //We add a fake root, TopAnat doesn't manage multiple root
            out.println(FAKE_ANAT_ENTITY_ROOT.getId() + "\t" + FAKE_ANAT_ENTITY_ROOT.getName());
        }
        
        //relations
        Map<String, Set<String>> relations = this.anatEntityService.loadDirectIsAPartOfRelationships(
                Arrays.asList(this.params.getSpeciesId()));
        
        //We add a fake root, and we map all orphan terms to it: TopAnat don't manage multiple roots. 
        //Search for terms never seen as child of another term.
        Set<String> allChildIds = relations.values().stream()
                .flatMap(Set::stream).collect(Collectors.toSet());
        //we need to examine all terms, not only those present in the relation Map, because maybe 
        //some terms have no ancestors and no descendants, and are not in the Map.
        Set<String> roots = entities.stream()
                .map(term -> term.getId())
                .filter(termId -> !allChildIds.contains(termId))
                .collect(Collectors.toSet());
        log.trace("Roots identified in the graph: " + roots);
        assert roots.size() > 0;
        if (roots.size() > 1) {
            relations.put(FAKE_ANAT_ENTITY_ROOT.getId(), roots);
        }
        
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(anatEntitiesRelFile)))) {
            relations.forEach(
                    (id,descentIds) -> descentIds.forEach(
                            (descentId) -> out.println(descentId + '\t' + id)));
        }

        log.exit();
    }

    /**
     *
     */
    private void writeToGeneToAnatEntitiesFile(String geneToAnatEntitiesFile)
            throws IOException {

        log.entry(geneToAnatEntitiesFile); 

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                geneToAnatEntitiesFile)))) {
            this.callService.loadExpressionCalls(
                    this.params.getSpeciesId(), 
                    (ExpressionCallFilter) this.params.convertRawParametersToCallFilter(), 
                    EnumSet.of(CallService.Attribute.GENE_ID, CallService.Attribute.ANAT_ENTITY_ID), 
                    null
                ).forEach(
                    call -> out.println(
                        call.getGene().getEnsemblGeneId() + '\t' + 
                        call.getCondition().getAnatEntityId()
                    )
                );
        }
        log.exit();
    }    

    /**
     */
    private void generateGenesToAnatEntitiesAssociationFile() throws IOException {
        log.entry();
        log.info("Generating Gene to AnatEntities Association file...");

        //These files are general and are created in the general directory to be cached, 
        //before being copied to the analysis result directory.
        //We will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path finalGeneToAnatEntitiesFile = Paths.get(this.props.getTopAnatResultsWritingDirectory(), 
                this.getGeneToAnatEntitiesFileName(false));
        Path tmpFile = Paths.get(this.props.getTopAnatResultsWritingDirectory(), 
                this.getGeneToAnatEntitiesFileName(true));
        //If there is a custom background requested, then we create the file directly 
        //in the result directory, it will not be cached for reuse in the parent directory
        boolean inResultDir = false;
        if (this.params.getSubmittedBackgroundIds() != null && 
                !this.params.getSubmittedBackgroundIds().isEmpty()) {
            finalGeneToAnatEntitiesFile = Paths.get(this.getGeneToAnatEntitiesFilePath(false));
            tmpFile = Paths.get(this.getGeneToAnatEntitiesFilePath(true));
            inResultDir = true;
        }

        try {
            this.controller.acquireWriteLock(finalGeneToAnatEntitiesFile.toString());
            this.controller.acquireWriteLock(tmpFile.toString());

            //check, AFTER having acquired the locks, that the final file does not 
            //already exist (maybe another thread generated the files before this one 
            //acquired the lock)
            if (!Files.exists(finalGeneToAnatEntitiesFile)) {
                log.info("Gene to AnatEntities association file not already generated.");

                this.writeToGeneToAnatEntitiesFile(tmpFile.toString());
                //move tmp file if successful
                this.move(tmpFile, finalGeneToAnatEntitiesFile, false);
            }

            //Now, we copy the file to the analysis result directory, if it was not directly 
            //written in it.
            if (!inResultDir) {
                Path finalPath = Paths.get(this.getGeneToAnatEntitiesFilePath(false));
                if (!Files.exists(finalPath)) {
                    Files.copy(finalGeneToAnatEntitiesFile, finalPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

        } finally {
            Files.deleteIfExists(tmpFile);
            this.controller.releaseWriteLock(finalGeneToAnatEntitiesFile.toString());
            this.controller.releaseWriteLock(tmpFile.toString());
        }


        log.info("GeneToAnatEntitiesAssociationFilePath: {}", this.getGeneToAnatEntitiesFilePath(false));
        log.exit();
    }    

    /**
     * 
     * @param tmpFileName
     * @throws IOException
     */
    private void writeToTopAnatParamsFile(String topAnatParamsFileName) throws IOException {
        log.entry();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(topAnatParamsFileName)))) {
            out.println("# Warning, this file contains the initial values of the parameters for your information only.");
            out.println("# Changing a value in this file won't affect the R script.");
            out.println(this.params.toString(":\t", "\r\n", true));
        }

        log.exit();
    }

    /**
     * 
     * @throws IOException
     */
    private void generateTopAnatParamsFile() throws IOException {
        log.entry();
        log.info("Generating TopAnatParams file...");

        String topAnatParamsFilePath = this.getParamsOutputFilePath(false);
        String tmpFileName = this.getParamsOutputFilePath(true);

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalTopAnatParamsFile = Paths.get(topAnatParamsFilePath);

        try {
            this.controller.acquireWriteLock(topAnatParamsFilePath);
            this.controller.acquireWriteLock(tmpFileName);

            //check, AFTER having acquired the locks, that the final file does not 
            //already exist (maybe another thread generated the files before this one 
            //acquired the lock)
            if (Files.exists(finalTopAnatParamsFile)) {
                log.info("TopAnatParams file already generated.");
                log.exit(); return;
            }

            this.writeToTopAnatParamsFile(tmpFileName);

            this.move(tmpFile, finalTopAnatParamsFile, true);

        } finally {
            Files.deleteIfExists(tmpFile);
            this.controller.releaseWriteLock(topAnatParamsFilePath);
            this.controller.releaseWriteLock(tmpFileName);
        }

        log.info("TopAnatParamsFilePath: {}", this.getParamsOutputFilePath(false));
        log.exit();
    }  

    private void generateZipFile() throws IOException{
        log.entry();
        log.info("Generating Zip file...");

        String zipFilePath = this.getZipFilePath(false);
        String tmpFileName = this.getZipFilePath(true);

        //we will write into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalZipFile = Paths.get(zipFilePath);

        try {
            this.controller.acquireWriteLock(zipFilePath);
            this.controller.acquireWriteLock(tmpFileName);

            //check, AFTER having acquired the locks, that the final file does not 
            //already exist (maybe another thread generated the files before this one 
            //acquired the lock)
            if (Files.exists(finalZipFile)) {
                log.info("Zip file already generated.");
                log.exit(); return;
            }

            this.writeZipFile(tmpFileName);

            this.move(tmpFile, finalZipFile, true);

        } finally {
            Files.deleteIfExists(tmpFile);
            this.controller.releaseWriteLock(zipFilePath);
            this.controller.releaseWriteLock(tmpFileName);
        }

        log.info("ZIP file path: {}", getZipFilePath(false));
        log.exit();
    }

    /**
     * 99% from here: 
     * http://examples.javacodegeeks.com/
     * core-java/util/zip/create-zip-file-from-multiple-files-with-zipoutputstream/
     * @throws IOException 
     */
    private void writeZipFile(String path) throws IOException {

        String zipFile = path;

        String[] srcFiles = {this.getResultFilePath(false),
                             this.getResultPDFFilePath(false), 
                             this.getRScriptConsoleFilePath(), 
                             this.getAnatEntitiesNamesFilePath(false), 
                             this.getGeneToAnatEntitiesFilePath(false), 
                             this.getParamsOutputFilePath(false), 
                             this.getAnatEntitiesRelationshipsFilePath(false), 
                             this.getRScriptAnalysisFilePath(false),
                             this.getResultDirectoryPath() + Paths.get(TopAnatAnalysis.class.getResource(
                        this.props.getTopAnatFunctionFile()).getPath()).getFileName().toString()
        };

        // create byte buffer
        byte[] buffer = new byte[1024];

        FileOutputStream fos = new FileOutputStream(zipFile);

        ZipOutputStream zos = new ZipOutputStream(fos);

        for (int i=0; i < srcFiles.length; i++) {

            File srcFile = new File(srcFiles[i]);
            srcFile.createNewFile();

            FileInputStream fis = new FileInputStream(srcFile);

            // begin writing a new ZIP entry, positions the stream to the start of the entry data
            zos.putNextEntry(new ZipEntry(srcFile.getName()));

            int length;

            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();

            // close the InputStream
            fis.close();

        }

        // close the ZipOutputStream
        zos.close();

    }


    /**
     * @return  A {@code String} that is the path to the directory where results of this analysis 
     *          are written.
     */
    protected String getResultDirectory() {
        log.entry();
        return log.exit(this.params.getKey() + File.separator);
    }
    protected String getResultDirectoryPath() {
        log.entry();
        return log.exit(this.props.getTopAnatResultsWritingDirectory() + this.getResultDirectory());
    }
    
    //TODO: refactor all the getXXXName and getXXXPath methods
    /**
     * 
     */
    protected String getResultFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX + "results.tsv";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    /**
     * Return the path to the result file of this analysis.
     * @param tmpFile   A {@code boolean} defining whether the path links to the definitive file 
     *                  of a completed analysis, of the temporary file of an ongoing analysis.
     * @return          A {@code String} that is the path to the result file.
     */
    protected String getResultFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getResultFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getRScriptConsoleFileName(){
        return TopAnatAnalysis.FILE_PREFIX + "log.R_console";
    }
    protected String getRScriptConsoleFilePath(){
        log.entry();
        return log.exit(this.getResultDirectoryPath() + this.getRScriptConsoleFileName());
    }

    /**
     * 
     */
    protected String getResultPDFFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX + "results.pdf";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getResultPDFFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getResultPDFFileName(tmpFile));
    }

    /**
     *
     */
    //TODO: unit test this logic, of different file names depending on parameters
    protected String getGeneToAnatEntitiesFileName(boolean tmpFile){
        log.entry(tmpFile);
        
        String paramsEncoded = "";
        //for the background file, if there is no custom background requested, 
        //we take into account only some info
        if (this.params.getSubmittedBackgroundIds() == null || 
                this.params.getSubmittedBackgroundIds().isEmpty()) {
            
            //TODO: use some kind of encoding of the Strings for file name (see replacement for stage ID)
            final StringBuilder sb = new StringBuilder();
            sb.append(this.params.getSpeciesId());
            sb.append("_").append(this.params.getCallType().toString());
            Optional.ofNullable(this.params.getDevStageId())
                //replace column in IDs
                .ifPresent(e -> sb.append("_").append(e.replace(":", "_")));
            //use EnumSet for consistent ordering
            Optional.ofNullable(this.params.getDataTypes()).map(e -> EnumSet.copyOf(e))
            .orElse(EnumSet.allOf(DataType.class))
            .stream()
            .forEach(e -> sb.append("_").append(e.toString()));
            sb.append("_").append(Optional.ofNullable(this.params.getSummaryQuality()).orElse(SummaryQuality.SILVER)
                    .toString());
            
            paramsEncoded = sb.toString();
        } else {
            //custom background provided, use the hash
            paramsEncoded = this.params.getKey();
        }
        String fileName = TopAnatAnalysis.FILE_PREFIX + "GeneToAnatEntities_" 
            + paramsEncoded + ".tsv";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getGeneToAnatEntitiesFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getGeneToAnatEntitiesFileName(tmpFile));
    }

    /**
     * @return
     */
    protected String getAnatEntitiesNamesFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX + "AnatEntitiesNames_" + this.params.getSpeciesId() 
            + ".tsv";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getAnatEntitiesNamesFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getAnatEntitiesNamesFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getAnatEntitiesRelationshipsFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX 
                + "AnatEntitiesRelationships_" + this.params.getSpeciesId() + ".tsv";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getAnatEntitiesRelationshipsFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getAnatEntitiesRelationshipsFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getRScriptAnalysisFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX + "script.R";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getRScriptAnalysisFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getRScriptAnalysisFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getParamsOutputFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX + "Params.txt";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getParamsOutputFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getParamsOutputFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getZipFileName(boolean tmpFile){
        log.entry(tmpFile);
        String fileName = TopAnatAnalysis.FILE_PREFIX + "results.zip";
        if (tmpFile) {
            fileName += TMP_FILE_SUFFIX;
        }
        return log.exit(fileName);
    }
    protected String getZipFilePath(boolean tmpFile){
        log.entry(tmpFile);
        return log.exit(this.getResultDirectoryPath() + this.getZipFileName(tmpFile));
    }

    /**
     * 
     * @return
     */
    protected boolean isAnalysisDone(){
        log.entry();
        
        String finalFilePath = this.getResultFilePath(false);
        String tmpFilePath = this.getResultFilePath(true);
        //if it was requested to generate a zip, then we check for existence of the zip, 
        //that is generated last
        if (this.params.isWithZip()) {
            log.trace("Using zip file for checking for presence of results.");
            finalFilePath = this.getZipFilePath(false);
            tmpFilePath = this.getZipFilePath(true);
        }
        
        //At this point, if the analysis is being run by another thread, we don't want 
        //to wait for the lock on the file: results are not generated, period.
        if (this.controller.getReadWriteLock(finalFilePath).isWriteLocked() || 
                this.controller.getReadWriteLock(tmpFilePath).isWriteLocked()) {
            return log.exit(false);
        }
        File file = new File(finalFilePath);
        //no need to acquire read lock to test for file existence, as it has been written already.
        if (file.exists()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * 
     * @param src
     * @param dest
     * @throws IOException 
     */
    private void move(Path src, Path dest, boolean checkSize) throws IOException{
        if(!checkSize || Files.size(src) > 0) 
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        else {
            throw log.throwing(new IllegalStateException("Empty tmp file"));
        }
    }

}
