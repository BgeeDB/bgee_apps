package org.bgee.model.topanat;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;

import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;
import com.github.rcaller.util.Globals;

/**
 * TODO comment me.
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * 
 * @version Bgee 13, March 2016
 * @since Bgee 13
 */
public class TopAnatRManager {

    private final static Logger log = LogManager.getLogger(TopAnatRManager.class.getName());

    /**
     * A {@code String} that is the prefix of the message printed in the R console 
     * when an analysis produces no result and an empty result file is created. 
     * Because RCaller throws an exception when a result file is empty 
     * (with a message matching the regex "^.*?Can not parse output: The generated file .+? is empty.*$"), 
     * and because it also throws this exception for some real types of errors, 
     * we use this message to formally check whether the exception was caused by an absence of results.
     */
    public static final String NO_RESULT_MESSAGE_PREFIX = "No result, creating an empty result file";

    private final RCaller caller;

    private final RCode code;

    private final BgeeProperties props;

    private final TopAnatParams params;

    /**
     * 
     * @param props
     * @param topAnatParams
     */
    public TopAnatRManager(BgeeProperties props, TopAnatParams topAnatParams){
        log.entry(props, topAnatParams);
        Globals.setRscriptCurrent(props.getTopAnatRScriptExecutable());
        this.caller = RCaller.create();
        this.code = RCode.create();
        this.props = props;
        this.params = topAnatParams;
        log.traceExit();
    }

    /**
     * 
     * @param caller
     * @param code
     * @param props
     * @param params
     */
    public TopAnatRManager(RCaller caller, RCode code, BgeeProperties props, TopAnatParams params){

        log.entry(caller,code,props,params);
        Globals.setRscriptCurrent(props.getTopAnatRScriptExecutable());
        this.caller = caller;
        this.code = code;
        this.props = props;
        this.params = params;
        log.traceExit();
    }

    protected String generateRCode(String resultFilePath, 
            String resultFileName, String resultPdfFileName, 
            String anatEntitiesNamesFileName, String anatEntitiesRelationshipsFileName,
            String geneToAnatEntitiesFileName,
            Set<String> foregroundIds) {
        log.entry(resultFilePath, resultFileName, resultPdfFileName, anatEntitiesNamesFileName, 
                anatEntitiesRelationshipsFileName, geneToAnatEntitiesFileName, foregroundIds);
        
        String bioconductorRelease = this.props.getBioconductorReleaseNumber();

        code.clear();
        code.addRCode("# Check that you have installed the required packages");
        code.addRCode("if(!suppressWarnings(require(BiocManager))){");
        code.addRCode("  install.packages('BiocManager', repos='https://cloud.r-project.org')");
        code.addRCode("}");
        code.addRCode("if(!suppressWarnings(require(Rgraphviz))){");
        code.addRCode("  BiocManager::install('Rgraphviz', version='" + bioconductorRelease + "')");
        code.addRCode("}");

        code.addRCode("if(!suppressWarnings(require(Runiversal))){");
        code.addRCode("  BiocManager::install('Runiversal', version='" + bioconductorRelease + "')");
        code.addRCode("}");

        // if there is no decorrelation, do not use the topGO package
        if(this.params.getDecorrelationType() != DecorrelationType.NONE){
            code.addRCode("if(!suppressWarnings(require(topGO))){");
            code.addRCode("  BiocManager::install('topGO', version='" + bioconductorRelease + "')");
            code.addRCode("}");
        }

        code.addRCode("if(!suppressWarnings(require(rJava))){");
        code.addRCode("  BiocManager::install('rJava', version='" + bioconductorRelease + "')");
        code.addRCode("}");

        if(this.params.getDecorrelationType() != DecorrelationType.NONE){
            code.addRCode("library(topGO)");
        }
        code.addRCode("library(rJava)");
        code.addRCode("library(Runiversal)");
        code.addRCode("library(Rgraphviz)");

        code.addRCode("# Please change the working directory to match your file system:");
        code.addRCode("setwd('" + resultFilePath + "')");
        log.debug("Location of TopAnat functions file: {} - {}", this.props.getTopAnatFunctionFile(), 
                this.getClass().getResource(this.props.getTopAnatFunctionFile()).getPath());
        code.R_source(Paths.get(TopAnatAnalysis.class.getResource(
                this.props.getTopAnatFunctionFile()).getPath()).getFileName().toString());
        code.addRCode("resultExist <- FALSE");

        String[] topOBOResultFile = { resultFileName };
        code.addStringArray("topOBOResultFile", topOBOResultFile);

        String[] topOBOResultPDFFile = { resultPdfFileName };
        code.addStringArray("resultPDF", topOBOResultPDFFile);

        // Organ Relationships File
        String[] organRelationships = { anatEntitiesRelationshipsFileName };
        code.addStringArray("organRelationshipsFileName", organRelationships);
        code.addRCode("tab <- read.table(organRelationshipsFileName,header=FALSE, sep='\t')");
        code.addRCode("relations <- tapply(as.character(tab[,2]), as.character(tab[,1]), unique)");

        // Gene to Organ Relationship File
        String[] geneToOrgan = { geneToAnatEntitiesFileName };
        code.addStringArray("geneToOrganFileName", geneToOrgan);
        //maybe the background is empty because of too stringent parameters.
        //Check whether file is empty, otherwise an error would be generated
        code.addRCode("if (file.info(geneToOrganFileName)$size != 0) {");
        code.addRCode("  tab <- read.table(geneToOrganFileName,header=FALSE, sep='\t')");
        code.addRCode("  gene2anatomy <- tapply(as.character(tab[,2]), as.character(tab[,1]), unique)");
        if(this.params.getDecorrelationType() != DecorrelationType.NONE){
            code.addRCode("  gene2anatomy <- tapply(as.character(tab[,2]), as.character(tab[,1]), unique)");
        }
        else{
            code.addRCode("  anatomy2gene <- tapply(as.character(tab[,1]), as.character(tab[,2]), unique)"); 
        }

        // Organ Names File
        String[] organNames = { anatEntitiesNamesFileName };
        code.addStringArray("organNamesFileName", organNames);
        code.addRCode("  organNames <- read.table(organNamesFileName, header = FALSE, sep='\t',row.names=1)");
        code.addRCode("  names(organNames) <- organNames");

        code.addStringArray("StringIDs", foregroundIds.toArray(new String[0]));
        code.addRCode("  geneList <- factor(as.integer(names(gene2anatomy) %in% StringIDs))");
        code.addRCode("  names(geneList) <- names(gene2anatomy)");
        //maybe all submitted genes are part of the background, or none of them, 
        //in that case we cannot proceed to the tests. Or maybe there is less genes 
        //with data in the background than the threshold on node size.
        code.addRCode("  if (length(geneList) > 0 & length(levels(geneList)) == 2 & length(geneList) >= " 
                + params.getNodeSize() + ") {");

        if(this.params.getDecorrelationType() != DecorrelationType.NONE){
            code.addRCode("    myData <- maketopGOdataObject(parentMapping = relations,allGenes = geneList,nodeSize = "+ params.getNodeSize() + ",gene2Nodes = gene2anatomy)");
            //maybe make maketopGOdataObject to return an error code rather than using 'stop'?
            //then: code.addRCode("if (is.character(myData)) {...}");


            code.addRCode("    resFis <- runTest(myData, algorithm = '"
                    + this.params.getDecorrelationType().getCode() +"', statistic = '"
                    + this.params.getStatisticTest().getCode() +"')");

            //under-representation disabled
            //code.addRCode("test.stat <- new('elimCount', testStatistic = GOFisherTestUnder, name ='Elim / Fisher test / underrepresentation')");
            //code.addRCode("resFis.under <- getSigGroups(myData, test.stat)");

            code.addRCode("    tableOver <- makeTable(myData,score(resFis), "+this.params.getFdrThreshold() + " , organNames)");
            //Do NOT sort the table again here. Already sorted by makeTable
        }else{
            // Run the tests with custom methods that do not use the topGO package
            // For the moment, only fisher is supported
            code.addRCode("    resTmp <- lapply(anatomy2gene,FUN=runTestWithoutTopGO,geneList=geneList,nodeSize = "+ params.getNodeSize() + ")");
            // Filter out NULL (=below nodeSize) values
            code.addRCode("    i=1");
            code.addRCode("    while(i <= length(resTmp)){");
            code.addRCode("        if(length(resTmp[[i]])!=5){");
            code.addRCode("            resTmp[[i]]<-NULL");
            code.addRCode("        }");
            code.addRCode("        else{");
            code.addRCode("            i<-i+1");
            code.addRCode("        }");
            code.addRCode("    }");
            code.addRCode("    res <- data.frame(matrix(unlist(resTmp), nrow=length(resTmp), byrow=T))");
            code.addRCode("    colnames(res) <- c('annotated','significant','expected','foldEnrichment','pval')");
            code.addRCode("    rownames(res) <- names(resTmp)            ");
            code.addRCode("    tableOver <- makeTableWithoutTopGO(res,"+this.params.getFdrThreshold() + ", organNames)");
        }

        //if we get results, save the results and generate a graph visualization
        code.addRCode("    if(!is.null(nrow(tableOver)) && nrow(tableOver) != 0  && ncol(tableOver) == 8) {");
        code.addRCode("      write.table(tableOver, file=topOBOResultFile, sep='\t', row.names=F, col.names=T, quote=F)");
        code.addRCode("      resultExist <- TRUE");

        if(this.params.getDecorrelationType() != DecorrelationType.NONE){
            code.addRCode("      pValFis <- score(resFis)");
            code.addRCode("      pVal<-pValFis");
            code.addRCode("      pVal[pValFis==0]<- 100");
        }
        else{
            code.addRCode("      pVal<-tableOver$p");
            code.addRCode("      pVal[pVal==0]<- 100  ");       
        }

        code.addRCode("      organNames <- read.table(organNamesFileName, header = FALSE, sep='\t')");
        code.addRCode("      rownames(organNames)<-organNames[,1]");

        //get the number of terms with p-value below 0.01
        code.addRCode("      resultCount <- sum(as.numeric(tableOver[, 7]) <= "+ params.getPvalueThreshold() +")");
        //set the number of terms to be displayed (terms below p-value, but max 10)
        code.addRCode("      resultCount <- min(c(resultCount , "+ params.getNumberOfSignificantNodes() +"))");
        code.addRCode("      cat(paste('Number of nodes to display: ', resultCount, '\n'))");
        //generate the graph only if we have significant nodes
        if(this.params.getDecorrelationType() != DecorrelationType.NONE){
            code.addRCode("      if (resultCount != 0) {");
            code.addRCode("        printTopOBOGraph(myData, pVal, firstSigNodes = resultCount, fileName = resultPDF , useInfo = 'all',pdfSW = TRUE, organNames=organNames)");
            code.addRCode("      }");
        }
        code.addRCode("    }");
        code.addRCode("  }");
        code.addRCode("}");

        //if there is no result, but no error occurred, we create an empty result file
        code.addRCode("if (!resultExist) {");
        code.addRCode("  cat('" + NO_RESULT_MESSAGE_PREFIX + ": ', topOBOResultFile, '\n')");
        code.addRCode("  file.create(topOBOResultFile)");
        //we need a tableOver object for RCaller to perform the commands
        code.addRCode("  tableOver <- data.frame(1, 8)");
        code.addRCode("}");


        return log.traceExit(code.toString());

    }
    protected void performRFunction(String consoleFileName) throws FileNotFoundException {

        log.info("Running statistical tests in R...");
        if(this.code.toString().equals(RCode.create().toString())){
            throw new IllegalStateException("The R code was not set before the analysis");
        }
        caller.redirectROutputToFile(consoleFileName, true);
        caller.setRCode(code);
        caller.runAndReturnResult("tableOver");
        log.traceExit();

    }

}
