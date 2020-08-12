## 03/12/08 modified by Adrian Alexa from topGO package

annFUN.gene2Nodes <- function(feasibleGenes = NULL, gene2Nodes) {

  ## Restrict the mappings to the feasibleGenes set  
  if(!is.null(feasibleGenes))
    gene2Nodes <- gene2Nodes[intersect(names(gene2Nodes), feasibleGenes)]

  ## Throw-up the genes which have no annotation
  if(any(is.na(gene2Nodes)))
    gene2Nodes <- gene2Nodes[!is.na(gene2Nodes)]

  gene2Nodes <- gene2Nodes[sapply(gene2Nodes, length) > 0]

  ## Get all the Terms and keep a one-to-one mapping with the genes
  allNodes <- unlist(gene2Nodes, use.names = FALSE)
  geneID <- rep(names(gene2Nodes), sapply(gene2Nodes, length))

  return(split(geneID, allNodes))
}
##xx <- annFUN.gene2Nodes(gene2Nodes = gene2Nodes)



########################   buildGraph.topology   ########################
buildGraph.topology <- function(knownNodes, parentMapping) {

  ## first build the lookUp table for the terms
  nodeLookUp <- new.env(hash = T, parent = emptyenv())

  ## warping functions for a easier acces to the lookUp table
  isNodeInDAG <- function(node) {
    return(exists(node, envir = nodeLookUp, mode = 'logical', inherits = FALSE))
  }
  setNodeInDAG <- function(node) {
    assign(node, TRUE, envir = nodeLookUp)
  }

  ## get the root of the ontology
  GENE.ONTO.ROOT <- setdiff(unique(unlist(parentMapping, use.names = FALSE)),
      names(parentMapping))

  ## we read all the database once and the access the list
  adjLookUP <- as.list(parentMapping)

  ## we use an environment of environments to store edges: (this way is faster)
  ## in the end we will coerce it to a list of list and build a graphNEL obj. 
  edgeEnv <- new.env(hash = T, parent = emptyenv())

  ## add the arc (u --> v) to edgeEnv of type :
  envAddEdge <- function(u, v) {
    assign(v, 0, envir = get(u, envir = edgeEnv))
  }

  ## recursivly build the induced graph starting from one node
  buildInducedGraph <- function(node) {
    ## if we have visited the node, there is nothing to do
    if(isNodeInDAG(node))
      return(1)

    ## we put the node in the graph and we get his parents
    setNodeInDAG(node)    # we visit the node
    assign(node, new.env(hash = T, parent = emptyenv()), envir = edgeEnv) # adj list

    if(node == GENE.ONTO.ROOT) 
      return(2)

    adjNodes <- adjLookUP[[node]]

    ## debuging point! should not happen!
    if(length(adjNodes) == 0)
      cat('\n There are no adj nodes for node: ', node, '\n')

    for(i in 1:length(adjNodes)) {
      x <- as.character(adjNodes[i])
      envAddEdge(node, x)
      buildInducedGraph(x)
    }

    return(0)
  }

  ## we start from the most specific nodes
  lapply(knownNodes, buildInducedGraph)

  ## now we must transform our env into a Graph structure
  ## for now we use lapply, later we can do it with eapply
  .graphNodes <- ls(edgeEnv)
  .edgeList <- eapply(edgeEnv,
                      function(adjEnv) {
                        aux <- as.list(adjEnv)
                        return(list(edges = match(names(aux), .graphNodes),
                                    weights = as.numeric(aux)))
                      })

  ## now we can build the graphNEL object
  return(new('graphNEL',
             nodes = .graphNodes,
             edgeL = .edgeList,
             edgemode = 'directed'))
}

##################################################

maketopGOdataObject <- function(## the child-parrent relationship 
                                parentMapping,
                                ## a named numeric or factor, the names are the genes ID
                                allGenes, 
                                ## function to select the signif. genes
                                geneSelectionFun = NULL,
                                ## minimum node size
                                nodeSize = 0,
                                ## annotation data
                                gene2Nodes,
                                ## additional parameters
                                ...) {


  ## code from new()
  ClassDef <- getClass("topGOdata", where = topenv(parent.frame()))
  ## with R > 2.3.1, PACKAGE = "base" doesn't seem to work
  ## .Object <- .Call("R_do_new_object", ClassDef, PACKAGE = "base")
  .Object <- .Call("R_do_new_object", ClassDef)

  ## some checking
  if(is.null(names(allGenes)))
    stop("allGenes must be a named vector")

  if(!is.factor(allGenes) && !is.numeric(allGenes))
    stop("allGenes should be a factor or a numeric vector")

  .Object@allGenes <- names(allGenes)

  if(is.factor(allGenes)) {
    if(length(levels(allGenes)) != 2)
      stop("allGenes must be a factor with 2 levels")
    .Object@allScores <- factor(as.character(allGenes))
    .Object@geneSelectionFun <- function(x) {
      return(as.logical(as.integer(levels(x)))[x])
    }
  }
  else {
    .Object@allScores <- as.numeric(allGenes)

    ## function to select which genes are significant
    if(is.null(geneSelectionFun))
      warning("No function to select the significant genes provided!")
    .Object@geneSelectionFun <- geneSelectionFun
  }

  ## size of the nodes which will be pruned
  .Object@nodeSize = as.integer(nodeSize)


  ## this function is returning a list of GO terms from the specified ontology
  ## whith each entry being a vector of genes
  cat("\nBuilding 'most specific' Terms .....")
  mostSpecificGOs <- annFUN.gene2Nodes(feasibleGenes = .Object@allGenes, gene2Nodes = gene2Nodes)
  cat("\t(", length(mostSpecificGOs), "Terms found. )\n")


  ## build the graph starting from the most specific terms ...
  cat("\nBuild DAG topology ..........")
  g <- buildGraph.topology(names(mostSpecificGOs), parentMapping)
  cat("\t(",  numNodes(g), "terms and", numEdges(g), "relations. )\n")

  ## probably is good to store the leves but for the moment we dont 
  .nodeLevel <- buildLevels(g, leafs2root = TRUE)

  ## annotate the nodes in the GO graph with genes
  cat("\nAnnotating nodes ...............")
  g <- mapGenes2GOgraph(g, mostSpecificGOs, nodeLevel = .nodeLevel) ## leafs2root

  ## select the feasible genes
  gRoot <- getGraphRoot(g)
  feasibleGenes <- ls(nodeData(g, n = gRoot, attr = "genes")[[gRoot]])
  cat("\t(", length(feasibleGenes), "genes annotated to the nodes. )\n")

  .Object@feasible <- .Object@allGenes %in% feasibleGenes

  cc <- .countsInNode(g, nodes(g))
  .Object@graph <- subGraph(names(cc)[cc >= .Object@nodeSize], g)

  .Object
}

###########################   mapGenes2GOgraph   ###########################
## This function builds for each node a vector containing all the genes/probes
## that can be annotated to that node.
## It starts with the nodes on the lowest level, and then pushes their genes
## to the parents/ancestors

## it returns the graph for which the attribute of each node contains a mapping
## of the genes/probes

mapGenes2GOgraph <- function(dag,
                             mostSpecificGOs,
                             nodeLevel = buildLevels(dag, leafs2root = TRUE)) {

  allNodes <- nodes(dag)
  ## just in case .....
  if((ln.allNodes <- length(allNodes)) != nodeLevel$noOfNodes)
    stop('nodeLevel is corrupt')

  geneTerms <- new.env(hash = TRUE, parent = emptyenv())
  nn <- names(mostSpecificGOs)
  lapply(allNodes,
         function(x) {
           e <- new.env(hash = TRUE, parent = emptyenv())

           if(x %in% nn)
             multiassign(mostSpecificGOs[[x]], rep(TRUE, length(mostSpecificGOs[[x]])), envir = e)

           assign(x, e, envir = geneTerms)
         })


  ## get the levels list
  levelsLookUp <- nodeLevel$level2nodes
  noOfLevels <- nodeLevel$noOfLevels

  for(i in noOfLevels:1) {
    currentNodes <- get(as.character(i), envir = levelsLookUp, mode = 'character')

    ## get all the adjacent nodes (teoreticaly nodes from level i - 1)
    adjList <- adj(dag, currentNodes)

    ## push the genes from level i to level i - 1
    lapply(currentNodes,
           function(node) {
             ## get the genes from this node
             genesID <- ls(get(node, envir = geneTerms, mode = 'environment'))

             ## debug option, just in case something goes wrong
             if(length(genesID) == 0)
               print(i)

             ## for each adiacent node mapp the genesID to them
             lapply(adjList[[node]],
                    function(destNode) {
                      destEnv <- get(destNode, envir = geneTerms, mode = 'environment')
                      multiassign(genesID, rep(FALSE, length(genesID)), envir = destEnv)
                      return(NULL)
                    })
             return(NULL)
           })
  }

  ## Assign for each node in the graph the coresponding environment
  nodeDataDefaults(dag, attr = "genes") <- emptyenv()
  nodeData(dag, allNodes, attr = "genes") <- as.list(geneTerms)[allNodes]

  return(dag)
}

################################################################################

## plots a legend for correspondance between p-values and colors in showSigNodes graphs
getLegend <- function(pval) {
  logSigNodes <- log10(sort(pval))
  sigColor <- round(logSigNodes - range(logSigNodes)[1] + 1)
  colorMap <- heat.colors(max(sigColor))

  nn <- paste('~', 10^(-(max(sigColor):1)), sep = '')
  x <- rep(1, max(sigColor))
  names(x) <- nn
  barplot(x, col = colorMap, yaxt = 'n', cex.names=2)
}

################################################################################

## define the test statistic which will detect underrepresentation
#if(!isGeneric("GOFisherTestUnder"))
#  setGeneric("GOFisherTestUnder", function(object) standardGeneric("GOFisherTestUnder"))
#setMethod("GOFisherTestUnder", "classicCount",
#          function(object) {#
#
#            contMat <- contTable(object)
#            if(all(contMat == 0))
#              p.value <- 1
#            else
#              p.value <- fisher.test(contMat, alternative = "less")$p.value
#            ## "greater" is for over-, "less" for under-, and "two-sided" is for both alternatives
#            return(p.value)
#          })


###################################################################
# returns a table of over/under expressed terms

makeTable <- function(myData, scores, cutoff, names){
  fdr <- p.adjust(p=scores, method = "fdr")
  topTerms <- sort(scores[fdr <= cutoff])
  topTerms <- as.data.frame(topTerms)

  if(nrow(topTerms) != 0){
    odds <- termStat(myData, row.names(topTerms))
				
    foldEnrichment <- odds[2]/odds[3]
		
    # Rounding off to 2 decimal places
    foldEnrichment <-format(foldEnrichment,digits=3)
    topTerms<-format(topTerms,digits=3)
    fdr[row.names(topTerms)]<-format(fdr[row.names(topTerms)],digits=3)
				    
    topTerms <- cbind(odds, foldEnrichment , topTerms, fdr[row.names(topTerms)])
    topTable <- merge(names, topTerms, by.x=0, by.y=0)
    names(topTable) <- c("OrganId", "OrganName", "Annotated", "Significant", "Expected", "foldEnrichment" , "p", "fdr")
    topTable <- topTable[order(as.numeric(topTable$p)), ]

    return(topTable)
  } else{
    print(paste("There is no significant term with a FDR threshold of ", cutoff, sep=""))
    return(NA)
  }
}






###################################################################
#Prints the graph generated to a PDF File
printTopOBOGraph <- function(object, pValRes, firstSigNodes, fileName, useInfo ,pdfSW, organNames){

  pdf(file = fileName, width = 25, height = 25)

  ## plot the graph to the specified device
  par(mai = rep(0, 4))
  gT <- generateGraph(object, pValRes, firstSigNodes = firstSigNodes,swPlot = FALSE, useInfo = useInfo, plotFunction = GOplot, organNames=organNames)
  plot(gT$complete.dag)
  dev.off()

  cat(fileName, ' --- no of nodes: ', numNodes(gT$dag), '\n') 
}



#######################################################################
generateGraph <- function(GOdata, termsP.value, firstSigNodes = 10, reverse = TRUE,
			              sigForAll = TRUE, wantedNodes = NULL, putWN = TRUE,
			              putCL = 0, type = NULL, showEdges = TRUE, swPlot = TRUE,
			              useFullNames = TRUE, oldSigNodes = NULL,
			              useInfo = c('none', 'pval', 'counts', 'def', 'np', 'all')[1],
			              plotFunction = GOplot, .NO.CHAR = 20, organNames=NULL) {

  require('Rgraphviz') || stop('package Rgraphviz is required')

  if(!is.null(firstSigNodes)) 
    sigTerms <- sort(termsP.value)[1:firstSigNodes]
  else
    sigTerms <- numeric(0)
    
  cat("P-values selected for term display: \n")
  print(sigTerms)

  if(putWN && !is.null(wantedNodes))
    baseNodes <- union(names(sigTerms), wantedNodes)
  else
    baseNodes <- names(sigTerms)
  
  cat("Base nodes based on p-values: \n")
  print(baseNodes)

  if(length(baseNodes) == 0)
    stop('No nodes were selected')

## we want to get aditional nodes
  if(putCL) {
    goDAG.r2l <- reverseArch(graph(GOdata))

    for(i in 1:putCL) {
      newNodes <- unique(unlist(adj(goDAG.r2l, baseNodes)))
      baseNodes <- union(newNodes, baseNodes)
    }
  }
  cat("Number of baseNodes for induceGraph", length(baseNodes), "\n")
  dag <- inducedGraph(graph(GOdata), baseNodes)
  cat("Induced graph: \n")
  print(dag)

  if(reverse)
    dag <- reverseArch(dag)

  termCounts <- termStat(GOdata, nodes(dag))
  cat("termCounts from termStat: \n")
  print(termCounts)

  ## we plot for each node of GO graph the pie plot showing the
  ## difference bettween all genes mapped to it and sig genes mapped to it
  if(!is.null(type)) {
    if(swPlot)
      GOplot.counts(dag, wantedNodes = wantedNodes, nodeCounts = termCounts,
                    showEdges = showEdges)
    return(dag)
  }


  pval.info <- function(whichNodes) {
    ret.val <- format.pval(termsP.value[whichNodes], digits = 3, eps = 1e-20)
    names(ret.val) <- whichNodes
    return(ret.val)
  }

  .pval = pval.info(nodes(dag))

  #Getting Organ Names
  whichTerms = nodes(dag)
  names <- organNames[whichTerms,2]
  .def <- names

  #.def = .getTermsDefinition(whichTerms = nodes(dag), ontology(GOdata), numChar = .NO.CHAR)

  .counts = apply(termCounts[, c("Significant", "Annotated")], 1, paste, collapse = " / ")
  ## more infos will be added
  nodeInfo <- switch(useInfo,
                     none = NULL,
                     pval = .pval,
                     def = .def,
                     counts = .counts,
                     np = paste(.def, .pval, sep = '\\\n'),
                     all = paste(.def, .pval, .counts, sep = '\\\n')
                    )

  ## we can plot the significance level of all nodes in the dag or for the sigNodes
  if(sigForAll)
    sigNodes <- termsP.value[nodes(dag)]
  else
    sigNodes <- sigTerms

  if(is.null(wantedNodes))
    wantedNodes <- names(sigTerms)


  complete.dag <- plotFunction(dag, sigNodes = sigNodes, genNodes = names(sigTerms),
                               wantedNodes = wantedNodes, showEdges = showEdges,
                               useFullNames = useFullNames, oldSigNodes = oldSigNodes,
                               nodeInfo = nodeInfo)

  if(swPlot && !is.null(complete.dag))
    plot(complete.dag)

## we return the obtained dag
  return(list(dag = dag, complete.dag = complete.dag))
}


#
#pdf(file = fileName, width = 10, height = 10)
#
## plot the graph to the specified device
#par(mai = rep(0, 4))
#gT <- showSigOfNodes(object, pValRes, firstSigNodes = firstSigNodes,swPlot = FALSE, useInfo = useInfo, plotFunction = GOplot)
#plot(gT$complete.dag)
#dev.off()
#
#cat(fileName, ' --- no of nodes: ', numNodes(gT$dag), '\n') 
#}

#########
# Jan2016, Author mseppey
# Perform the enrichment fisher test without TopGO
#########
runTestWithoutTopGO<-function(anatomy,geneList,test="fisher",nodeSize){
  # Count the gene of each category to generate the contingency table
  foregroundExpressed <- length(subset(names(geneList),geneList==1 & names(geneList) %in% anatomy))
  foregroundNotExpressed <- length(subset(names(geneList),geneList==1 & !(names(geneList) %in% anatomy)))
  backgroundExpressed <- length(subset(names(geneList),geneList==0 & names(geneList) %in% anatomy))
  backgroundNotExpressed <- length(subset(names(geneList),geneList==0 & !(names(geneList) %in% anatomy)))
  totalExpressed <- foregroundExpressed + backgroundExpressed
  # if the min node size is not reached, return null
  if(totalExpressed < nodeSize){
    return(NULL)
  }
  # Generate the contingency table and run the test
  data <- matrix(c(
    foregroundExpressed,
    backgroundExpressed,
    foregroundNotExpressed,
    backgroundNotExpressed),
    nrow = 2,
    dimnames =
      list(c("foreground", "background"),
           c("present", "absent")))
  if(test=="fisher"){
    fis <- fisher.test(data,alternative="greater")
  }else{
    stop("Can only use Fisher test when running topAnat with propagated data (without topGo).")
  }
  
  # Generate and return the result values
  annotated <- length(subset(names(geneList),names(geneList) %in% anatomy)) # =foreground+background
  significant <- foregroundExpressed
  expected <- (foregroundExpressed+backgroundExpressed)*(foregroundExpressed+foregroundNotExpressed)/(backgroundNotExpressed+backgroundExpressed+foregroundExpressed+foregroundNotExpressed)
  foldEnrichment <- significant/expected
  return(cbind(annotated, significant , expected, foldEnrichment, pval=fis$p.value))
}
#########
# Jan2016, Author mseppey
#########
makeTableWithoutTopGO <- function(data, cutoff, names){
  data$fdr <- p.adjust(p=data$pval, method = "fdr")
  topTerms <- data[with(data, order(fdr) & fdr <= cutoff), ]
  if(nrow(topTerms) != 0){
    # Format and return the results
    topTable <- merge(names, topTerms, by.x=0, by.y=0)
    names(topTable) <- c("OrganId", "OrganName", "Annotated", "Significant", "Expected", "foldEnrichment" , "p", "fdr")
    topTable <- topTable[order(as.numeric(topTable$p)), ]
    topTable$Expected <- format(topTable$Expected, digits = 3, nsmall = 2, drop0trailing = TRUE)
    topTable$foldEnrichment <- format(topTable$foldEnrichment, digits = 3)
    topTable$p <- format(topTable$p, digits = 3)
    topTable$fdr <- format(topTable$fdr, digits = 3)
    return(topTable)
  } else{
    print(paste("There is no significant term with a FDR threshold of ", cutoff, sep=""))
    return(NA)
  }
}