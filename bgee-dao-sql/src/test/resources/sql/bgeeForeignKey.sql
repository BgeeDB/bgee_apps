-- this file contains the foreign key constraints. 

-- ****************************************************
-- GENERAL
-- ****************************************************
/*!40000 ALTER TABLE `dataSourceToSpecies` DISABLE KEYS */;
alter table dataSourceToSpecies 
add foreign key (dataSourceId) references dataSource(dataSourceId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `dataSourceToSpecies` ENABLE KEYS */;

--  ****************************************************
--  TAXONOMY
--  ****************************************************

/*!40000 ALTER TABLE `species` DISABLE KEYS */;
alter table species 
add foreign key (taxonId) references taxon(taxonId) on delete cascade;
/*!40000 ALTER TABLE `species` ENABLE KEYS */;

/*!40000 ALTER TABLE `speciesToKeyword` DISABLE KEYS */;
alter table speciesToKeyword 
add foreign key (speciesId) references species(speciesId) on delete cascade, 
add foreign key (keywordId) references keyword(keywordId) on delete cascade;
/*!40000 ALTER TABLE `speciesToKeyword` ENABLE KEYS */;

--  ****************************************************
--  CONFIDENCE AND EVIDENCE ONTOLOGIES
--  ****************************************************

--  ****************************************************
--  ANATOMY AND DEVELOPMENT
--  ****************************************************
/*!40000 ALTER TABLE `stageTaxonConstraint` DISABLE KEYS */;
alter table stageTaxonConstraint 
add foreign key (stageId) references stage(stageId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `stageTaxonConstraint` ENABLE KEYS */;

/*!40000 ALTER TABLE `stageNameSynonym` DISABLE KEYS */;
alter table stageNameSynonym 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `stageNameSynonym` ENABLE KEYS */;

/*!40000 ALTER TABLE `stageXRef` DISABLE KEYS */;
alter table stageXRef 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `stageXRef` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntity` DISABLE KEYS */;
alter table anatEntity 
add foreign key (startStageId) references stage(stageId), 
add foreign key (endStageId) references stage(stageId);
/*!40000 ALTER TABLE `anatEntity` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityTaxonConstraint` DISABLE KEYS */;
alter table anatEntityTaxonConstraint 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityTaxonConstraint` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityNameSynonym` DISABLE KEYS */;
alter table anatEntityNameSynonym 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityNameSynonym` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityXRef` DISABLE KEYS */;
alter table anatEntityXRef 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityXRef` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityRelation` DISABLE KEYS */;
alter table anatEntityRelation 
add foreign key (anatEntitySourceId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (anatEntityTargetId) references anatEntity(anatEntityId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityRelation` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityRelationTaxonConstraint` DISABLE KEYS */;
alter table anatEntityRelationTaxonConstraint 
add foreign key (anatEntityRelationId) references anatEntityRelation(anatEntityRelationId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityRelationTaxonConstraint` ENABLE KEYS */;

--  ****************************************************
--  SIMILARITY ANNOTATIONS
--  ****************************************************
/*!40000 ALTER TABLE `summarySimilarityAnnotation` DISABLE KEYS */;
alter table summarySimilarityAnnotation 
add foreign key (taxonId) references taxon(taxonId) on delete cascade, 
add foreign key (CIOId) references CIOStatement(CIOId) on delete cascade;
/*!40000 ALTER TABLE `summarySimilarityAnnotation` ENABLE KEYS */;

/*!40000 ALTER TABLE `similarityAnnotationToAnatEntityId` DISABLE KEYS */;
alter table similarityAnnotationToAnatEntityId 
add foreign key (summarySimilarityAnnotationId) references summarySimilarityAnnotation(summarySimilarityAnnotationId) on delete cascade,
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade;
/*!40000 ALTER TABLE `similarityAnnotationToAnatEntityId` ENABLE KEYS */;

/*!40000 ALTER TABLE `rawSimilarityAnnotation` DISABLE KEYS */;
alter table rawSimilarityAnnotation 
add foreign key (summarySimilarityAnnotationId) references summarySimilarityAnnotation(summarySimilarityAnnotationId) on delete cascade,
add foreign key (ECOId) references evidenceOntology(ECOId) on delete cascade, 
add foreign key (CIOId) references CIOStatement(CIOId) on delete cascade;
/*!40000 ALTER TABLE `rawSimilarityAnnotation` ENABLE KEYS */;

-- ****************************************************
-- GENE
-- ****************************************************
/*!40000 ALTER TABLE `OMAHierarchicalGroup` DISABLE KEYS */;
alter table OMAHierarchicalGroup 
add foreign key (taxonId) references taxon(taxonId) on delete set null;
/*!40000 ALTER TABLE `OMAHierarchicalGroup` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneOntologyTermAltId` DISABLE KEYS */;
alter table geneOntologyTermAltId 
add foreign key (goId) references geneOntologyTerm(goId) on delete cascade;
/*!40000 ALTER TABLE `geneOntologyTermAltId` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneOntologyRelation` DISABLE KEYS */;
alter table geneOntologyRelation 
add foreign key (goAllTargetId) references geneOntologyTerm(goId) on delete cascade, 
add foreign key (goAllSourceId) references geneOntologyTerm(goId) on delete cascade;
/*!40000 ALTER TABLE `geneOntologyRelation` ENABLE KEYS */;

/*!40000 ALTER TABLE `gene` DISABLE KEYS */;
alter table gene 
add foreign key (speciesId) references species(speciesId) on delete cascade, 
add foreign key (geneBioTypeId) references geneBioType(geneBioTypeId) on delete set null, 
add foreign key (OMAParentNodeId) references OMAHierarchicalGroup(OMANodeId) on delete set null;
/*!40000 ALTER TABLE `gene` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneNameSynonym` DISABLE KEYS */;
alter table geneNameSynonym  
add foreign key (geneId) references gene(geneId) on delete cascade;
/*!40000 ALTER TABLE `geneNameSynonym` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneXRef` DISABLE KEYS */;
alter table geneXRef  
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (dataSourceId) references dataSource(dataSourceId) on delete cascade;
/*!40000 ALTER TABLE `geneXRef` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneToTerm` DISABLE KEYS */;
alter table geneToTerm  
add foreign key (geneId) references gene(geneId) on delete cascade;
/*!40000 ALTER TABLE `geneToTerm` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneToGeneOntologyTerm` DISABLE KEYS */;
alter table geneToGeneOntologyTerm 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (goId) references geneOntologyTerm(goId) on delete cascade;
/*!40000 ALTER TABLE `geneToGeneOntologyTerm` ENABLE KEYS */;

-- ****************************************************
-- EXPRESSION DATA
-- ****************************************************
/*!40000 ALTER TABLE `expression` DISABLE KEYS */;
alter table expression 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade, 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `expression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalExpression` DISABLE KEYS */;
alter table globalExpression 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade, 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `globalExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalExpressionToExpression` DISABLE KEYS */;
alter table globalExpressionToExpression  
add foreign key (expressionId) references expression(expressionId) on delete cascade, 
add foreign key (globalExpressionId) references globalExpression(globalExpressionId) on delete cascade;
/*!40000 ALTER TABLE `globalExpressionToExpression` ENABLE KEYS */;

-- ****************************************************
-- NO EXPRESSION DATA
-- ****************************************************
/*!40000 ALTER TABLE `noExpression` DISABLE KEYS */;
alter table noExpression 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade, 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `noExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalNoExpression` DISABLE KEYS */;
alter table globalNoExpression 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade, 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `globalNoExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalNoExpressionToNoExpression` DISABLE KEYS */;
alter table globalNoExpressionToNoExpression  
add foreign key (noExpressionId) references noExpression(noExpressionId) on delete cascade, 
add foreign key (globalNoExpressionId) references globalNoExpression(globalNoExpressionId) on delete cascade;
/*!40000 ALTER TABLE `globalNoExpressionToNoExpression` ENABLE KEYS */;

-- ****************************************************
-- DIFFERENTIAL EXPRESSION DATA
-- ****************************************************
/*!40000 ALTER TABLE `differentialExpression` DISABLE KEYS */;
alter table differentialExpression  
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade, 
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `differentialExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `differentialExpressionAnalysis` DISABLE KEYS */;
alter table differentialExpressionAnalysis 
add foreign key (microarrayExperimentId) references microarrayExperiment(microarrayExperimentId) on delete cascade,
add foreign key (rnaSeqExperimentId) references rnaSeqExperiment(rnaSeqExperimentId) on delete cascade;
/*!40000 ALTER TABLE `differentialExpressionAnalysis` ENABLE KEYS */;

/*!40000 ALTER TABLE `deaSampleGroup` DISABLE KEYS */;
alter table deaSampleGroup 
add foreign key (deaId) references differentialExpressionAnalysis(deaId) on delete cascade,
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (stageId) references stage(stageId) on delete cascade;
/*!40000 ALTER TABLE `deaSampleGroup` ENABLE KEYS */;

-- ****************************************************
-- RAW EST DATA
-- ****************************************************
/*!40000 ALTER TABLE `estLibrary` DISABLE KEYS */;
alter table estLibrary 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete set null, 
add foreign key (stageId) references stage(stageId) on delete set null, 
add foreign key (dataSourceId) references dataSource(dataSourceId);
/*!40000 ALTER TABLE `estLibrary` ENABLE KEYS */;

/*!40000 ALTER TABLE `estLibraryToKeyword` DISABLE KEYS */;
alter table estLibraryToKeyword 
add foreign key (estLibraryId) references estLibrary(estLibraryId) on delete cascade, 
add foreign key (keywordId) references keyword(keywordId) on delete cascade;
/*!40000 ALTER TABLE `estLibraryToKeyword` ENABLE KEYS */;

/*!40000 ALTER TABLE `expressedSequenceTag` DISABLE KEYS */;
alter table expressedSequenceTag 
add foreign key (estLibraryId) references estLibrary(estLibraryId) on delete cascade, 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (expressionId) references expression(expressionId) on delete set null;
/*!40000 ALTER TABLE `expressedSequenceTag` ENABLE KEYS */;

/*!40000 ALTER TABLE `estRank` DISABLE KEYS */;
alter table estRank
add foreign key (estLibraryId) references estLibrary(estLibraryId) on delete cascade,
add foreign key (geneId) references gene(geneId) on delete cascade;
/*!40000 ALTER TABLE `estRank` ENABLE KEYS */;
--  ****************************************************
--  RAW AFFYMETRIX DATA
--  ****************************************************
/*!40000 ALTER TABLE `microarrayExperiment` DISABLE KEYS */;
alter table microarrayExperiment 
add foreign key (dataSourceId) references dataSource(dataSourceId);
/*!40000 ALTER TABLE `microarrayExperiment` ENABLE KEYS */;

/*!40000 ALTER TABLE `microarrayExperimentToKeyword` DISABLE KEYS */;
alter table microarrayExperimentToKeyword 
add foreign key (microarrayExperimentId) references microarrayExperiment(microarrayExperimentId) on delete cascade, 
add foreign key (keywordId) references keyword(keywordId) on delete cascade;
/*!40000 ALTER TABLE `microarrayExperimentToKeyword` ENABLE KEYS */;

/*!40000 ALTER TABLE `affymetrixChip` DISABLE KEYS */;
alter table affymetrixChip 
add foreign key (microarrayExperimentId) references microarrayExperiment(microarrayExperimentId) on delete cascade, 
add foreign key (chipTypeId) references chipType(chipTypeId) on delete set null, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete set null, 
add foreign key (stageId) references stage(stageId) on delete set null;
/*!40000 ALTER TABLE `affymetrixChip` ENABLE KEYS */;

/*!40000 ALTER TABLE `affymetrixProbeset` DISABLE KEYS */;
alter table affymetrixProbeset 
add foreign key (bgeeAffymetrixChipId) references affymetrixChip(bgeeAffymetrixChipId) on delete cascade, 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (expressionId) references expression(expressionId) on delete set null, 
add foreign key (noExpressionId) references noExpression(noExpressionId) on delete set null;
/*!40000 ALTER TABLE `affymetrixProbeset` ENABLE KEYS */;

-- ****** for diff expression ********

/*!40000 ALTER TABLE `deaSampleGroupToAffymetrixChip` DISABLE KEYS */;
alter table deaSampleGroupToAffymetrixChip 
add foreign key (deaSampleGroupId) references deaSampleGroup(deaSampleGroupId) on delete cascade,
add foreign key (bgeeAffymetrixChipId) references affymetrixChip(bgeeAffymetrixChipId) on delete cascade;
/*!40000 ALTER TABLE `deaSampleGroupToAffymetrixChip` ENABLE KEYS */;

/*!40000 ALTER TABLE `deaAffymetrixProbesetSummary` DISABLE KEYS */;
alter table deaAffymetrixProbesetSummary 
add foreign key (deaAffymetrixProbesetSummaryId) references affymetrixProbeset(affymetrixProbesetId) on delete cascade, 
add foreign key (deaSampleGroupId) references deaSampleGroup(deaSampleGroupId) on delete cascade, 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (differentialExpressionId) references differentialExpression(differentialExpressionId) on delete set null;
/*!40000 ALTER TABLE `deaAffymetrixProbesetSummary` ENABLE KEYS */;

--  ****************************************************
--  RAW IN SITU DATA
--  ****************************************************
/*!40000 ALTER TABLE `inSituExperiment` DISABLE KEYS */;
alter table inSituExperiment 
add foreign key (dataSourceId) references dataSource(dataSourceId);
/*!40000 ALTER TABLE `inSituExperiment` ENABLE KEYS */;

/*!40000 ALTER TABLE `inSituExperimentToKeyword` DISABLE KEYS */;
alter table inSituExperimentToKeyword 
add foreign key (inSituExperimentId) references inSituExperiment(inSituExperimentId) on delete cascade, 
add foreign key (keywordId) references keyword(keywordId) on delete cascade;
/*!40000 ALTER TABLE `inSituExperimentToKeyword` ENABLE KEYS */;

/*!40000 ALTER TABLE `inSituEvidence` DISABLE KEYS */;
alter table inSituEvidence 
add foreign key (inSituExperimentId) references inSituExperiment(inSituExperimentId) on delete cascade;
/*!40000 ALTER TABLE `inSituEvidence` ENABLE KEYS */;

/*!40000 ALTER TABLE `inSituSpot` DISABLE KEYS */;
alter table inSituSpot 
add foreign key (inSituEvidenceId) references inSituEvidence(inSituEvidenceId) on delete cascade,  
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete set null, 
add foreign key (stageId) references stage(stageId) on delete set null, 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (expressionId) references expression(expressionId) on delete set null, 
add foreign key (noExpressionId) references noExpression(noExpressionId) on delete set null;
/*!40000 ALTER TABLE `inSituSpot` ENABLE KEYS */;

--  ****************************************************
--  RAW RNA-SEQ DATA
--  ****************************************************
/*!40000 ALTER TABLE `rnaSeqExperiment` DISABLE KEYS */;
alter table rnaSeqExperiment 
add foreign key (dataSourceId) references dataSource(dataSourceId);
/*!40000 ALTER TABLE `rnaSeqExperiment` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqExperimentToKeyword` DISABLE KEYS */;
alter table rnaSeqExperimentToKeyword 
add foreign key (rnaSeqExperimentId) references rnaSeqExperiment(rnaSeqExperimentId) on delete cascade, 
add foreign key (keywordId) references keyword(keywordId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqExperimentToKeyword` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqLibrary` DISABLE KEYS */;
alter table rnaSeqLibrary 
add foreign key (rnaSeqExperimentId) references rnaSeqExperiment(rnaSeqExperimentId) on delete cascade, 
add foreign key (rnaSeqPlatformId) references rnaSeqPlatform(rnaSeqPlatformId) on delete cascade,  
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete set null, 
add foreign key (stageId) references stage(stageId) on delete set null;
/*!40000 ALTER TABLE `rnaSeqLibrary` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqRun` DISABLE KEYS */;
alter table rnaSeqRun 
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqRun` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqRunDiscarded` DISABLE KEYS */;
alter table rnaSeqRunDiscarded 
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqRunDiscarded` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqResult` DISABLE KEYS */;
alter table rnaSeqResult 
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade, 
add foreign key (geneId) references gene(geneId) on delete cascade, 
add foreign key (expressionId) references expression(expressionId) on delete set null, 
add foreign key (noExpressionId) references noExpression(noExpressionId) on delete set null;
/*!40000 ALTER TABLE `rnaSeqResult` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqFeatureLength` DISABLE KEYS */;
alter table rnaSeqFeatureLength 
add foreign key (geneId) references gene(geneId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqFeatureLength` ENABLE KEYS */;

-- ****** for diff expression ********

/*!40000 ALTER TABLE `deaSampleGroupToRnaSeqLibrary` DISABLE KEYS */;
alter table deaSampleGroupToRnaSeqLibrary 
add foreign key (deaSampleGroupId) references deaSampleGroup(deaSampleGroupId) on delete cascade,
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade;
/*!40000 ALTER TABLE `deaSampleGroupToRnaSeqLibrary` ENABLE KEYS */;

/*!40000 ALTER TABLE `deaRNASeqSummary` DISABLE KEYS */;
alter table deaRNASeqSummary 
add foreign key (geneSummaryId) references rnaSeqResult(geneId) on delete cascade, 
add foreign key (deaSampleGroupId) references deaSampleGroup(deaSampleGroupId) on delete cascade, 
add foreign key (differentialExpressionId) references differentialExpression(differentialExpressionId) on delete set null;
/*!40000 ALTER TABLE `deaRNASeqSummary` ENABLE KEYS */;

/*!40000 ALTER TABLE `downloadFile` DISABLE KEYS */;
alter table downloadFile
add foreign key (speciesDataGroupId) references speciesDataGroup(speciesDataGroupId) on delete cascade;
/*!40000 ALTER TABLE `downloadFile` ENABLE KEYS */;

/*!40000 ALTER TABLE `speciesToDataGroup` DISABLE KEYS */;
alter table speciesToDataGroup
add foreign key (speciesId) references species(speciesId) on delete cascade,
add foreign key (speciesDataGroupId) references speciesDataGroup(speciesDataGroupId) on delete cascade;
/*!40000 ALTER TABLE `speciesToDataGroup` ENABLE KEYS */;


