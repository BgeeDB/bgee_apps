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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOConditionFilter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionFilter;
import org.bgee.model.expressiondata.call.ConditionGraph;
import org.bgee.model.expressiondata.call.ConditionGraphService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
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
 * @version Bgee 15.0, May 2021
 * @since   Bgee 13, Sept. 2015
 */
public class TopAnatAnalysis extends CommonService {

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatAnalysis.class.getName());

    /**
     * 
     */
    protected final static String FAKE_ROOT_COND_ID = "BGEE:0";
    protected final static String FAKE_ROOT_COND_NAME = "Added root";

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
    private final ConditionGraphService condGraphService;

    /**
     * 
     */
    private final GeneService geneService;

    /**
     * 
     */
    private final TopAnatController controller;
    private final CallFilter<?, ?, ConditionFilter> callFilter;

    /**
     * @param params
     * @param props
     * @param serviceFactory
     * @param rManager
     */
    public TopAnatAnalysis(TopAnatParams params, BgeeProperties props, 
            ServiceFactory serviceFactory, TopAnatRManager rManager, TopAnatController controller) {
        super(serviceFactory);
        this.params = params;
        this.condGraphService = serviceFactory.getConditionGraphService();
        this.callService = serviceFactory.getCallService();
        this.geneService = serviceFactory.getGeneService();
        this.rManager = rManager;
        this.props = props;
        this.controller = controller;
        this.callFilter = this.params.convertRawParametersToCallFilter();
    }

    /**
     * @throws IOException
     * @throws InvalidForegroundException 
     * @throws InvalidSpeciesException 
     */
    protected TopAnatResults proceedToAnalysis() throws IOException, InvalidForegroundException, 
    InvalidSpeciesGenesException, RAnalysisException {
        log.traceEntry();
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
                    controller.getReadWriteLocks().acquireReadLock(sourceFunctionFileName);
                    controller.getReadWriteLocks().acquireWriteLock(targetFunctionFile.getPath());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } finally{
                    controller.getReadWriteLocks().releaseReadLock(sourceFunctionFileName);
                    controller.getReadWriteLocks().releaseWriteLock(targetFunctionFile.getPath());
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
        return log.traceExit(new TopAnatResults(
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
        allGeneIds.removeAll(this.geneService.loadGenes(
                    new GeneFilter(this.params.getSpeciesId(), allGeneIds)
                ).map(Gene::getGeneId)
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
        log.traceEntry();

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

            controller.getReadWriteLocks().acquireReadLock(namesFileName);
            controller.getReadWriteLocks().acquireReadLock(relsFileName);
            controller.getReadWriteLocks().acquireReadLock(geneToAnatEntitiesFile);

            controller.getReadWriteLocks().acquireWriteLock(tmpFileName);
            controller.getReadWriteLocks().acquireWriteLock(fileName);

            controller.getReadWriteLocks().acquireWriteLock(tmpPdfFileName);
            controller.getReadWriteLocks().acquireWriteLock(pdfFileName);

            //check, AFTER having acquired the locks, that the final files do not 
            //already exist (maybe another thread generated the files before this one 
            //acquires the lock)
            if (Files.exists(finalFile)) {
                log.info("Result files already generated.");
                log.traceExit();return;
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
                        new File(this.getRScriptConsoleFilePath()), null)) {
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
            //and this file was not generated. Also, there is no pdf if the decorrelation type is NONE
            if (Files.exists(tmpPdfFile) & this.params.getDecorrelationType() 
                    != DecorrelationType.NONE) {
                this.move(tmpPdfFile, finalPdfFile, false);
            }

        } finally {
            Files.deleteIfExists(tmpFile);
            Files.deleteIfExists(tmpPdfFile);
            controller.getReadWriteLocks().releaseWriteLock(tmpFileName);
            controller.getReadWriteLocks().releaseWriteLock(fileName);
            controller.getReadWriteLocks().releaseWriteLock(tmpPdfFileName);
            controller.getReadWriteLocks().releaseWriteLock(pdfFileName);
            controller.getReadWriteLocks().releaseReadLock(namesFileName);
            controller.getReadWriteLocks().releaseReadLock(relsFileName);
            controller.getReadWriteLocks().releaseReadLock(geneToAnatEntitiesFile);   
        }

        log.info("Result file path: {}", this.getResultFilePath(false));

        log.traceExit();
    }

    /**
     * 
     * @throws IOException
     */
    private void generateRCodeFile() throws IOException {
        log.traceEntry();

        log.info("Generating R code file...");

        String fileName = this.getRScriptAnalysisFilePath(false);
        String tmpFileName = this.getRScriptAnalysisFilePath(true);

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalFile = Paths.get(fileName);

        try {
            controller.getReadWriteLocks().acquireWriteLock(tmpFileName);
            controller.getReadWriteLocks().acquireWriteLock(fileName);

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
            controller.getReadWriteLocks().releaseWriteLock(tmpFileName);
            controller.getReadWriteLocks().releaseWriteLock(fileName);
        }

        log.info("Rcode file path: {}", 
                this.getRScriptAnalysisFilePath(false));
        log.traceExit();
    }


    /**
     */
    private void writeRcodeFile(String RcodeFile) throws IOException {
        log.traceEntry("{}", RcodeFile);

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

        log.traceExit();
    }

    private void createWriteDirectoryIfNotExist() {
        log.traceEntry();
        //acquire the write lock before checking if the directory exists, 
        //so that we can create it immediately. 
        String dir = this.getResultDirectoryPath();
        try {
            controller.getReadWriteLocks().acquireWriteLock(dir);
            File newDir = new File(dir);
            if (!newDir.exists()) {
                newDir.mkdirs();
            }
        } finally {
            controller.getReadWriteLocks().releaseWriteLock(dir);
        }
        log.traceExit();
    }
    /**
     * 
     * @throws IOException
     */
    private void generateAnatEntitiesFiles() throws IOException {
        log.traceEntry();

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
            controller.getReadWriteLocks().acquireWriteLock(namesTmpFile.toString());
            controller.getReadWriteLocks().acquireWriteLock(finalNamesFile.toString());
            controller.getReadWriteLocks().acquireWriteLock(relsTmpFile.toString());
            controller.getReadWriteLocks().acquireWriteLock(finalRelsFile.toString());

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
            this.controller.getReadWriteLocks().releaseWriteLock(namesTmpFile.toString());
            this.controller.getReadWriteLocks().releaseWriteLock(finalNamesFile.toString());
            this.controller.getReadWriteLocks().releaseWriteLock(relsTmpFile.toString());
            this.controller.getReadWriteLocks().releaseWriteLock(finalRelsFile.toString());
        }

        log.info("anatEntitiesNamesFilePath: {} - relationshipsFilePath: {}", 
                this.getAnatEntitiesNamesFilePath(false), 
                this.getAnatEntitiesRelationshipsFilePath(false));
        log.traceExit();
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
        log.traceEntry("{}, {}", anatEntitiesNameFile, anatEntitiesRelFile);

        //we need to get the anat. entities, both for anatEntitiesNameFile, and for 
        //correct generation of the anatEntitiesRelFile
        ConditionGraph conditionGraph = this.condGraphService.loadConditionGraphFromSpeciesIds(
                Collections.singleton(this.params.getSpeciesId()),
                this.callFilter.getConditionFilters(),
                TopAnatUtils.CALL_SERVICE_ATTRIBUTES.stream().filter(a -> a.isConditionParameter())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class))));

        Condition realCondRoot = null;
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(anatEntitiesNameFile)))) {
            for (Condition c: conditionGraph.getConditions()) {
                //We try to find the real root of the condition graph
                //TODO: manage that better in ConditionGraph or Condition, e.g.,
                //isRootCondition()
                if ((c.getAnatEntity() == null ||
                        c.getAnatEntity().getId().equals(ConditionDAO.ANAT_ENTITY_ROOT_ID)) &&
                    (c.getCellType() == null ||
                        c.getCellType().getId().equals(ConditionDAO.CELL_TYPE_ROOT_ID)) &&
                    (c.getDevStage() == null ||
                        c.getDevStage().getId().equals(ConditionDAO.DEV_STAGE_ROOT_ID)) &&
                    (c.getSex() == null ||
                        c.getSex().getId().equals(ConditionDAO.SEX_ROOT_ID)) &&
                    (c.getStrain() == null ||
                        c.getStrain().getId().equals(ConditionDAO.STRAIN_ROOT_ID))) {
                    realCondRoot = c;
                }
                out.println(TopAnatUtils.COND_ID_GENERATOR.apply(c) + "\t"
                        + TopAnatUtils.COND_NAME_GENERATOR.apply(c).replaceAll("'", ""));
            }
            //We add a fake root, TopAnat doesn't manage multiple root.
            //Even if we have only one root in Bgee, since we can select only observed calls,
            //this can lead to have multiple roots.
            //We add the fake root if the real root is not present in the conds.
            //XXX: shouldn't it always be, since we never delete this root condition,
            //even if not observed?
            if (realCondRoot == null) {
                out.println(FAKE_ROOT_COND_ID + "\t" + FAKE_ROOT_COND_NAME);
            }
        }
        
        //relations
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(anatEntitiesRelFile)))) {
            //We add a fake root, and we map all orphan terms to it:
            //TopAnat don't manage multiple roots. Even if we have only one root in Bgee,
            //since we can select only observed calls, this can lead to have multiple roots.
            //
            //Search for terms never seen as child of another term.
            //We need to examine all terms, not only those present in the relations,
            //because maybe some terms have no ancestors and no descendants,
            //and will not be retrieved when retrieving relations.
            Set<Condition> allConds = new HashSet<>();
            Set<Condition> childConds = new HashSet<>();
            for (Condition cond: conditionGraph.getConditions()) {
                allConds.add(cond);
                for (Condition parentCond: conditionGraph.getAncestorConditions(cond, true)) {
                    childConds.add(cond);
                    out.println(TopAnatUtils.COND_ID_GENERATOR.apply(cond) + '\t'
                              + TopAnatUtils.COND_ID_GENERATOR.apply(parentCond));
                }
            }
            allConds.removeAll(childConds);
            log.debug("Root(s) of the graph: {}", allConds);
            if (allConds.size() > 1) {
                for (Condition root: allConds) {
                    if (realCondRoot == null ||
                            !TopAnatUtils.COND_ID_GENERATOR.apply(root).equals(
                                    TopAnatUtils.COND_ID_GENERATOR.apply(realCondRoot))) {
                        out.println(TopAnatUtils.COND_ID_GENERATOR.apply(root) + '\t'
                            + (realCondRoot != null? TopAnatUtils.COND_ID_GENERATOR.apply(realCondRoot): FAKE_ROOT_COND_ID));
                    }
                }
            }
        }

        log.traceExit();
    }

    /**
     *
     */
    private void writeToGeneToAnatEntitiesFile(String geneToAnatEntitiesFile)
            throws IOException {
        log.traceEntry("{}", geneToAnatEntitiesFile);

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                geneToAnatEntitiesFile)))) {
            this.callService.loadExpressionCalls(
                    (ExpressionCallFilter) this.callFilter,
                    TopAnatUtils.CALL_SERVICE_ATTRIBUTES,
                    null
                ).forEach(
                    call -> out.println(
                        call.getGene().getGeneId() + '\t' +
                        TopAnatUtils.COND_ID_GENERATOR.apply(call.getCondition())
                    )
                );
        }
        log.traceExit();
    }    

    /**
     */
    private void generateGenesToAnatEntitiesAssociationFile() throws IOException {
        log.traceEntry();
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
            this.controller.getReadWriteLocks().acquireWriteLock(finalGeneToAnatEntitiesFile.toString());
            this.controller.getReadWriteLocks().acquireWriteLock(tmpFile.toString());

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
            this.controller.getReadWriteLocks().releaseWriteLock(finalGeneToAnatEntitiesFile.toString());
            this.controller.getReadWriteLocks().releaseWriteLock(tmpFile.toString());
        }


        log.info("GeneToAnatEntitiesAssociationFilePath: {}", this.getGeneToAnatEntitiesFilePath(false));
        log.traceExit();
    }    

    /**
     * 
     * @param tmpFileName
     * @throws IOException
     */
    private void writeToTopAnatParamsFile(String topAnatParamsFileName) throws IOException {
        log.traceEntry("{}", topAnatParamsFileName);

        String nameValueSeparator = ":\t";
        //OS independent line return, in order to serve a file generated on our server
        //but readable on any OS.
        String lineSeparator = "\r\n";

        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(topAnatParamsFileName)))) {
            out.print("#" + lineSeparator);
            out.print("# Warning, this file contains the initial values of the parameters, "
                    + "for your information only." + lineSeparator);
            out.print("# Changing a value in this file won't affect the R script." + lineSeparator);
            out.print("#" + lineSeparator);
            out.print(lineSeparator);

            if (StringUtils.isNotBlank(this.props.getMajorVersion())) {
                out.print("Bgee version" + nameValueSeparator + this.props.getMajorVersion()
                    + lineSeparator);
            }
            out.print(this.params.toString(nameValueSeparator, lineSeparator, true) + lineSeparator);
        }

        log.traceExit();
    }

    /**
     * 
     * @throws IOException
     */
    private void generateTopAnatParamsFile() throws IOException {
        log.traceEntry();
        log.info("Generating TopAnatParams file...");

        String topAnatParamsFilePath = this.getParamsOutputFilePath(false);
        String tmpFileName = this.getParamsOutputFilePath(true);

        //we will write results into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalTopAnatParamsFile = Paths.get(topAnatParamsFilePath);

        try {
            this.controller.getReadWriteLocks().acquireWriteLock(topAnatParamsFilePath);
            this.controller.getReadWriteLocks().acquireWriteLock(tmpFileName);

            //check, AFTER having acquired the locks, that the final file does not 
            //already exist (maybe another thread generated the files before this one 
            //acquired the lock)
            if (Files.exists(finalTopAnatParamsFile)) {
                log.info("TopAnatParams file already generated.");
                log.traceExit(); return;
            }

            this.writeToTopAnatParamsFile(tmpFileName);

            this.move(tmpFile, finalTopAnatParamsFile, true);

        } finally {
            Files.deleteIfExists(tmpFile);
            this.controller.getReadWriteLocks().releaseWriteLock(topAnatParamsFilePath);
            this.controller.getReadWriteLocks().releaseWriteLock(tmpFileName);
        }

        log.info("TopAnatParamsFilePath: {}", this.getParamsOutputFilePath(false));
        log.traceExit();
    }  

    private void generateZipFile() throws IOException{
        log.traceEntry();
        log.info("Generating Zip file...");

        String zipFilePath = this.getZipFilePath(false);
        String tmpFileName = this.getZipFilePath(true);

        //we will write into a tmp file, moved at the end if everything 
        //went fine.
        Path tmpFile = Paths.get(tmpFileName);
        Path finalZipFile = Paths.get(zipFilePath);

        try {
            this.controller.getReadWriteLocks().acquireWriteLock(zipFilePath);
            this.controller.getReadWriteLocks().acquireWriteLock(tmpFileName);

            //check, AFTER having acquired the locks, that the final file does not 
            //already exist (maybe another thread generated the files before this one 
            //acquired the lock)
            if (Files.exists(finalZipFile)) {
                log.info("Zip file already generated.");
                log.traceExit(); return;
            }

            this.writeZipFile(tmpFileName);

            this.move(tmpFile, finalZipFile, true);

        } finally {
            Files.deleteIfExists(tmpFile);
            this.controller.getReadWriteLocks().releaseWriteLock(zipFilePath);
            this.controller.getReadWriteLocks().releaseWriteLock(tmpFileName);
        }

        log.info("ZIP file path: {}", getZipFilePath(false));
        log.traceExit();
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
        log.traceEntry();
        return log.traceExit(this.params.getKey() + File.separator);
    }
    protected String getResultDirectoryPath() {
        log.traceEntry();
        return log.traceExit(this.props.getTopAnatResultsWritingDirectory() + this.getResultDirectory());
    }
    
    //TODO: refactor all the getXXXName and getXXXPath methods
    /**
     * 
     */
    protected String getResultFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        String fileName = TopAnatUtils.FILE_PREFIX + "results.tsv";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    /**
     * Return the path to the result file of this analysis.
     * @param tmpFile   A {@code boolean} defining whether the path links to the definitive file 
     *                  of a completed analysis, of the temporary file of an ongoing analysis.
     * @return          A {@code String} that is the path to the result file.
     */
    protected String getResultFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getResultFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getRScriptConsoleFileName(){
        return TopAnatUtils.FILE_PREFIX + "log.R_console";
    }
    protected String getRScriptConsoleFilePath(){
        log.traceEntry();
        return log.traceExit(this.getResultDirectoryPath() + this.getRScriptConsoleFileName());
    }

    /**
     * 
     */
    protected String getResultPDFFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        String fileName = TopAnatUtils.FILE_PREFIX + "results.pdf";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getResultPDFFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getResultPDFFileName(tmpFile));
    }

    /**
     *
     */
    //TODO: unit test this logic, of different file names depending on parameters
    protected String getGeneToAnatEntitiesFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        
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
            if(params.getDecorrelationType() == DecorrelationType.NONE) {
                sb.append("_" + DecorrelationType.NONE);
            }
            
            paramsEncoded = sb.toString();
        } else {
            //custom background provided, use the hash
            paramsEncoded = this.params.getKey();
        }
        String fileName = TopAnatUtils.FILE_PREFIX + "GeneToAnatEntities_" 
            + paramsEncoded + ".tsv";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getGeneToAnatEntitiesFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getGeneToAnatEntitiesFileName(tmpFile));
    }

    /**
     * @return
     */
    protected String getAnatEntitiesNamesFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        //For now we always have only one ConditionFilter, but if that changed,
        //we would need to order them for consistent naming.
        //Also, for now we only have a few condition parameters set,
        //but if that changed we would need to use a hash rather than all parameters.
        assert this.callFilter.getConditionFilters().size() <= 1;
        DAOConditionFilter daoCondFilter = generateDAOConditionFilter(
                this.callFilter.getConditionFilters().stream().findAny().orElse(null),
                convertCondParamAttrsToCondDAOAttrs(TopAnatUtils.CALL_SERVICE_ATTRIBUTES));

        String fileName = TopAnatUtils.FILE_PREFIX + "AnatEntitiesNames_"
            + this.params.getSpeciesId()
            + (daoCondFilter != null? "_" + daoCondFilter.toParamString(): "")
            + ".tsv";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getAnatEntitiesNamesFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getAnatEntitiesNamesFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getAnatEntitiesRelationshipsFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        //For now we always have only one ConditionFilter, but if that changed,
        //we would need to order them for consistent naming.
        //Also, for now we only have a few condition parameters set,
        //but if that changed we would need to use a hash rather than all parameters.
        assert this.callFilter.getConditionFilters().size() <= 1;
        DAOConditionFilter daoCondFilter = generateDAOConditionFilter(
                this.callFilter.getConditionFilters().stream().findAny().orElse(null),
                convertCondParamAttrsToCondDAOAttrs(TopAnatUtils.CALL_SERVICE_ATTRIBUTES));

        String fileName = TopAnatUtils.FILE_PREFIX 
                + "AnatEntitiesRelationships_" + this.params.getSpeciesId()
                + (daoCondFilter != null? "_" + daoCondFilter.toParamString(): "")
                + ".tsv";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getAnatEntitiesRelationshipsFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getAnatEntitiesRelationshipsFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getRScriptAnalysisFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        String fileName = TopAnatUtils.FILE_PREFIX + "script.R";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getRScriptAnalysisFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getRScriptAnalysisFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getParamsOutputFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        String fileName = TopAnatUtils.FILE_PREFIX + "Params.txt";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getParamsOutputFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getParamsOutputFileName(tmpFile));
    }

    /**
     * 
     */
    protected String getZipFileName(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        String fileName = TopAnatUtils.FILE_PREFIX + "results.zip";
        if (tmpFile) {
            fileName += TopAnatUtils.TMP_FILE_SUFFIX;
        }
        return log.traceExit(fileName);
    }
    protected String getZipFilePath(boolean tmpFile){
        log.traceEntry("{}", tmpFile);
        return log.traceExit(this.getResultDirectoryPath() + this.getZipFileName(tmpFile));
    }

    /**
     * 
     * @return
     */
    protected boolean isAnalysisDone(){
        log.traceEntry();
        
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
        if (this.controller.getReadWriteLocks().getReadWriteLock(finalFilePath).isWriteLocked() || 
                this.controller.getReadWriteLocks().getReadWriteLock(tmpFilePath).isWriteLocked()) {
            return log.traceExit(false);
        }
        File file = new File(finalFilePath);
        //no need to acquire read lock to test for file existence, as it has been written already.
        if (file.exists()) {
            return log.traceExit(true);
        }
        return log.traceExit(false);
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
