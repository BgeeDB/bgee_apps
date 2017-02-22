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
add foreign key (taxonId) references taxon(taxonId) on delete cascade,
add foreign key (dataSourceId) references dataSource(dataSourceId) on delete cascade;
/*!40000 ALTER TABLE `species` ENABLE KEYS */;

/*!40000 ALTER TABLE `speciesToSex` DISABLE KEYS */;
alter table speciesToSex
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `speciesToSex` ENABLE KEYS */;

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
-- GENE AND TRANSCRIPT INFO
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

/*!40000 ALTER TABLE `geneToOma` DISABLE KEYS */;
alter table geneToOma
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (OMANodeId) references OMAHierarchicalGroup(OMANodeId) on delete cascade,
add foreign key (taxonId) references taxon(taxonId) on delete cascade;
/*!40000 ALTER TABLE `geneToOma` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneNameSynonym` DISABLE KEYS */;
alter table geneNameSynonym
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade;
/*!40000 ALTER TABLE `geneNameSynonym` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneXRef` DISABLE KEYS */;
alter table geneXRef
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (dataSourceId) references dataSource(dataSourceId) on delete cascade;
/*!40000 ALTER TABLE `geneXRef` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneToTerm` DISABLE KEYS */;
alter table geneToTerm
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade;
/*!40000 ALTER TABLE `geneToTerm` ENABLE KEYS */;

/*!40000 ALTER TABLE `geneToGeneOntologyTerm` DISABLE KEYS */;
alter table geneToGeneOntologyTerm
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (goId) references geneOntologyTerm(goId) on delete cascade;
/*!40000 ALTER TABLE `geneToGeneOntologyTerm` ENABLE KEYS */;

/*!40000 ALTER TABLE `transcript` DISABLE KEYS */;
alter table transcript
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade;
/*!40000 ALTER TABLE `transcript` ENABLE KEYS */;

-- ****************************************************
-- CONDITIONS
-- ****************************************************
/*!40000 ALTER TABLE `cond` DISABLE KEYS */;
alter table cond 
add foreign key (exprMappedConditionId) references cond(conditionId) on delete cascade, 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (stageId) references stage(stageId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `cond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityCond` DISABLE KEYS */;
alter table anatEntityCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityCond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStageCond` DISABLE KEYS */;
alter table anatEntityStageCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (stageId) references stage(stageId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageCond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntitySexCond` DISABLE KEYS */;
alter table anatEntitySexCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntitySexCond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStrainCond` DISABLE KEYS */;
alter table anatEntityStrainCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStrainCond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStageSexCond` DISABLE KEYS */;
alter table anatEntityStageSexCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (stageId) references stage(stageId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageSexCond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStageStrainCond` DISABLE KEYS */;
alter table anatEntityStageStrainCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (stageId) references stage(stageId) on delete cascade, 
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageStrainCond` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntitySexStrainCond` DISABLE KEYS */;
alter table anatEntitySexStrainCond 
add foreign key (anatEntityId) references anatEntity(anatEntityId) on delete cascade,
add foreign key (speciesId) references species(speciesId) on delete cascade;
/*!40000 ALTER TABLE `anatEntitySexStrainCond` ENABLE KEYS */;

-- ****************************************************
-- EXPRESSION DATA
-- ****************************************************
/*!40000 ALTER TABLE `expression` DISABLE KEYS */;
alter table expression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (conditionId) references cond(conditionId) on delete cascade;
/*!40000 ALTER TABLE `expression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalExpression` DISABLE KEYS */;
alter table globalExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (conditionId) references cond(conditionId) on delete cascade;
/*!40000 ALTER TABLE `globalExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalExpressionToExpression` DISABLE KEYS */;
alter table globalExpressionToExpression
add foreign key (expressionId) references expression(expressionId) on delete cascade,
add foreign key (globalExpressionId) references globalExpression(globalExpressionId) on delete cascade;
/*!40000 ALTER TABLE `globalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntityExpression` DISABLE KEYS */;
alter table anatEntityExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityConditionId) references anatEntityCond(anatEntityConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntityExpression` DISABLE KEYS */;
alter table globalAnatEntityExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityConditionId) references anatEntityCond(anatEntityConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntityExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntityGlobalExpressionToExpression
add foreign key (anatEntityExpressionId) references anatEntityExpression(anatEntityExpressionId) on delete cascade,
add foreign key (globalAnatEntityExpressionId) references globalAnatEntityExpression(globalAnatEntityExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityGlobalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntityStageExpression` DISABLE KEYS */;
alter table anatEntityStageExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStageConditionId) references anatEntityStageCond(anatEntityStageConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntityStageExpression` DISABLE KEYS */;
alter table globalAnatEntityStageExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStageConditionId) references anatEntityStageCond(anatEntityStageConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntityStageExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStageGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntityStageGlobalExpressionToExpression
add foreign key (anatEntityStageExpressionId) references anatEntityStageExpression(anatEntityStageExpressionId) on delete cascade,
add foreign key (globalAnatEntityStageExpressionId) references globalAnatEntityStageExpression(globalAnatEntityStageExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageGlobalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntitySexExpression` DISABLE KEYS */;
alter table anatEntitySexExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntitySexConditionId) references anatEntitySexCond(anatEntitySexConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntitySexExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntitySexExpression` DISABLE KEYS */;
alter table globalAnatEntitySexExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntitySexConditionId) references anatEntitySexCond(anatEntitySexConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntitySexExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntitySexGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntitySexGlobalExpressionToExpression
add foreign key (anatEntitySexExpressionId) references anatEntitySexExpression(anatEntitySexExpressionId) on delete cascade,
add foreign key (globalAnatEntitySexExpressionId) references globalAnatEntitySexExpression(globalAnatEntitySexExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntitySexGlobalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntityStrainExpression` DISABLE KEYS */;
alter table anatEntityStrainExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStrainConditionId) references anatEntityStrainCond(anatEntityStrainConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStrainExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntityStrainExpression` DISABLE KEYS */;
alter table globalAnatEntityStrainExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStrainConditionId) references anatEntityStrainCond(anatEntityStrainConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntityStrainExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStrainGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntityStrainGlobalExpressionToExpression
add foreign key (anatEntityStrainExpressionId) references anatEntityStrainExpression(anatEntityStrainExpressionId) on delete cascade,
add foreign key (globalAnatEntityStrainExpressionId) references globalAnatEntityStrainExpression(globalAnatEntityStrainExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStrainGlobalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntityStageSexExpression` DISABLE KEYS */;
alter table anatEntityStageSexExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStageSexConditionId) references anatEntityStageSexCond(anatEntityStageSexConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageSexExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntityStageSexExpression` DISABLE KEYS */;
alter table globalAnatEntityStageSexExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStageSexConditionId) references anatEntityStageSexCond(anatEntityStageSexConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntityStageSexExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStageSexGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntityStageSexGlobalExpressionToExpression
add foreign key (anatEntityStageSexExpressionId) references anatEntityStageSexExpression(anatEntityStageSexExpressionId) on delete cascade,
add foreign key (globalAnatEntityStageSexExpressionId) references globalAnatEntityStageSexExpression(globalAnatEntityStageSexExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageSexGlobalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntityStageStrainExpression` DISABLE KEYS */;
alter table anatEntityStageStrainExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStageStrainConditionId) references anatEntityStageStrainCond(anatEntityStageStrainConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageStrainExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntityStageStrainExpression` DISABLE KEYS */;
alter table globalAnatEntityStageStrainExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntityStageStrainConditionId) references anatEntityStageStrainCond(anatEntityStageStrainConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntityStageStrainExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntityStageStrainGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntityStageStrainGlobalExpressionToExpression
add foreign key (anatEntityStageStrainExpressionId) references anatEntityStageStrainExpression(anatEntityStageStrainExpressionId) on delete cascade,
add foreign key (globalAnatEntityStageStrainExpressionId) references globalAnatEntityStageStrainExpression(globalAnatEntityStageStrainExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntityStageStrainGlobalExpressionToExpression` ENABLE KEYS */;


/*!40000 ALTER TABLE `anatEntitySexStrainExpression` DISABLE KEYS */;
alter table anatEntitySexStrainExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntitySexStrainConditionId) references anatEntitySexStrainCond(anatEntitySexStrainConditionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntitySexStrainExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `globalAnatEntitySexStrainExpression` DISABLE KEYS */;
alter table globalAnatEntitySexStrainExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (anatEntitySexStrainConditionId) references anatEntitySexStrainCond(anatEntitySexStrainConditionId) on delete cascade;
/*!40000 ALTER TABLE `globalAnatEntitySexStrainExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `anatEntitySexStrainGlobalExpressionToExpression` DISABLE KEYS */;
alter table anatEntitySexStrainGlobalExpressionToExpression
add foreign key (anatEntitySexStrainExpressionId) references anatEntitySexStrainExpression(anatEntitySexStrainExpressionId) on delete cascade,
add foreign key (globalAnatEntitySexStrainExpressionId) references globalAnatEntitySexStrainExpression(globalAnatEntitySexStrainExpressionId) on delete cascade;
/*!40000 ALTER TABLE `anatEntitySexStrainGlobalExpressionToExpression` ENABLE KEYS */;

-- ****************************************************
-- DIFFERENTIAL EXPRESSION DATA
-- ****************************************************
/*!40000 ALTER TABLE `differentialExpression` DISABLE KEYS */;
alter table differentialExpression
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (conditionId) references cond(conditionId) on delete cascade;
/*!40000 ALTER TABLE `differentialExpression` ENABLE KEYS */;

/*!40000 ALTER TABLE `differentialExpressionAnalysis` DISABLE KEYS */;
alter table differentialExpressionAnalysis
add foreign key (microarrayExperimentId) references microarrayExperiment(microarrayExperimentId) on delete cascade,
add foreign key (rnaSeqExperimentId) references rnaSeqExperiment(rnaSeqExperimentId) on delete cascade;
/*!40000 ALTER TABLE `differentialExpressionAnalysis` ENABLE KEYS */;

/*!40000 ALTER TABLE `deaSampleGroup` DISABLE KEYS */;
alter table deaSampleGroup
add foreign key (deaId) references differentialExpressionAnalysis(deaId) on delete cascade,
add foreign key (conditionId) references cond(conditionId) on delete cascade;
/*!40000 ALTER TABLE `deaSampleGroup` ENABLE KEYS */;

-- ****************************************************
-- RAW EST DATA
-- ****************************************************
/*!40000 ALTER TABLE `estLibrary` DISABLE KEYS */;
alter table estLibrary
add foreign key (conditionId) references cond(conditionId) on delete cascade,
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
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (expressionId) references expression(expressionId) on delete set null;
/*!40000 ALTER TABLE `expressedSequenceTag` ENABLE KEYS */;

/*!40000 ALTER TABLE `estLibraryExpression` DISABLE KEYS */;
alter table estLibraryExpression
add foreign key (expressionId) references expression(expressionId) on delete cascade,
add foreign key (estLibraryId) references estLibrary(estLibraryId) on delete cascade;
/*!40000 ALTER TABLE `estLibraryExpression` ENABLE KEYS */;
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
add foreign key (conditionId) references cond(conditionId) on delete cascade;
/*!40000 ALTER TABLE `affymetrixChip` ENABLE KEYS */;

/*!40000 ALTER TABLE `affymetrixProbeset` DISABLE KEYS */;
alter table affymetrixProbeset
add foreign key (bgeeAffymetrixChipId) references affymetrixChip(bgeeAffymetrixChipId) on delete cascade,
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (expressionId) references expression(expressionId) on delete set null;
/*!40000 ALTER TABLE `affymetrixProbeset` ENABLE KEYS */;

/*!40000 ALTER TABLE `microarrayExperimentExpression` DISABLE KEYS */;
alter table microarrayExperimentExpression
add foreign key (expressionId) references expression(expressionId) on delete cascade,
add foreign key (microarrayExperimentId) references microarrayExperiment(microarrayExperimentId) on delete cascade;
/*!40000 ALTER TABLE `microarrayExperimentExpression` ENABLE KEYS */;

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
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
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
add foreign key (conditionId) references cond(conditionId) on delete cascade,
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (expressionId) references expression(expressionId) on delete set null;
/*!40000 ALTER TABLE `inSituSpot` ENABLE KEYS */;

/*!40000 ALTER TABLE `inSituExperimentExpression` DISABLE KEYS */;
alter table inSituExperimentExpression
add foreign key (expressionId) references expression(expressionId) on delete cascade,
add foreign key (inSituExperimentId) references inSituExperiment(inSituExperimentId) on delete cascade;
/*!40000 ALTER TABLE `inSituExperimentExpression` ENABLE KEYS */;

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
add foreign key (conditionId) references cond(conditionId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqLibrary` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqRun` DISABLE KEYS */;
alter table rnaSeqRun
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqRun` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqResult` DISABLE KEYS */;
alter table rnaSeqResult
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade,
add foreign key (bgeeGeneId) references gene(bgeeGeneId) on delete cascade,
add foreign key (expressionId) references expression(expressionId) on delete set null;
/*!40000 ALTER TABLE `rnaSeqResult` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqTranscriptResult` DISABLE KEYS */;
alter table rnaSeqTranscriptResult
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade,
add foreign key (bgeeTranscriptId) references transcript(bgeeTranscriptId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqTranscriptResult` ENABLE KEYS */;

/*!40000 ALTER TABLE `rnaSeqExperimentExpression` DISABLE KEYS */;
alter table rnaSeqExperimentExpression
add foreign key (expressionId) references expression(expressionId) on delete cascade,
add foreign key (rnaSeqExperimentId) references rnaSeqExperiment(rnaSeqExperimentId) on delete cascade;
/*!40000 ALTER TABLE `rnaSeqExperimentExpression` ENABLE KEYS */;

-- ****** for diff expression ********

/*!40000 ALTER TABLE `deaSampleGroupToRnaSeqLibrary` DISABLE KEYS */;
alter table deaSampleGroupToRnaSeqLibrary
add foreign key (deaSampleGroupId) references deaSampleGroup(deaSampleGroupId) on delete cascade,
add foreign key (rnaSeqLibraryId) references rnaSeqLibrary(rnaSeqLibraryId) on delete cascade;
/*!40000 ALTER TABLE `deaSampleGroupToRnaSeqLibrary` ENABLE KEYS */;

/*!40000 ALTER TABLE `deaRNASeqSummary` DISABLE KEYS */;
alter table deaRNASeqSummary
add foreign key (geneSummaryId) references rnaSeqResult(bgeeGeneId) on delete cascade,
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


