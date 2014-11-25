-- Procedure to insert data in SELECT/UPDATE databases used for integration tests.
DROP PROCEDURE IF EXISTS populateTestDBs
;

CREATE PROCEDURE populateTestDBs()
BEGIN
    
    DECLARE CheckExists int;
    SET CheckExists = 0;

    SELECT count(*) INTO CheckExists FROM geneBioType;

    IF (CheckExists = 0) THEN 

        INSERT INTO geneBioType (geneBioTypeId,geneBioTypeName) 
        VALUES (12,'geneBioTypeName12');
        
        INSERT INTO dataSource (dataSourceId,dataSourceName,XRefUrl,experimentUrl,evidenceUrl,baseUrl,releaseDate,releaseVersion,dataSourceDescription,toDisplay,category,displayOrder)
        VALUES (1,'First DataSource','XRefUrl','experimentUrl','evidenceUrl','baseUrl',NOW(),'1.0','My custom data source',1,'Genomics database',1);

        INSERT INTO taxon (taxonId,taxonScientificName,taxonCommonName,taxonLeftBound,taxonRightBound,taxonLevel,bgeeSpeciesLCA) 
        VALUES (111,'taxSName111','taxCName111',1,10,1,1),
               (211,'taxSName211','taxCName211',2,3,2,0),
               (311,'taxSName311','taxCName311',4,9,2,0),
               (411,'taxSName411','taxCName411',5,6,1,1),
               (511,'taxSName511','taxCName511',7,8,1,1);

        INSERT INTO OMAHierarchicalGroup (OMANodeId,OMAGroupId,OMANodeLeftBound,OMANodeRightBound,taxonId) 
        VALUES (1,'HOG:NAILDQY',1,8,111),
               (2,'HOG:NAILDQY',2,3,211),
               (3,'HOG:NAILDQY',4,7,311),
               (4,'HOG:NAILDQY',5,6,411),
               (5,'HOG:VALEWID',9,14,111),
               (6,'HOG:VALEWID',10,13,211),
               (7,'HOG:VALEWID',11,12,511);

        INSERT INTO species (speciesId,genus,species,speciesCommonName,taxonId,genomeFilePath,genomeSpeciesId,fakeGeneIdPrefix) 
        VALUES (11,'gen11','sp11','spCName11',111,'path/genome11',0,''),
               (21,'gen21','sp21','spCName21',211,'path/genome21',52,'FAKEPREFIX'),
               (31,'gen31','sp31','spCName31',311,'path/genome31',0,'');

        INSERT INTO gene (geneId,geneName,geneDescription,speciesId,geneBioTypeId,OMAParentNodeId,ensemblGene) 
        VALUES ('ID1','genN1','genDesc1',11,12,2,true),
               ('ID2','genN2','genDesc2',21,null,null,true),
               ('ID3','genN3','genDesc3',31,null,3,false);
               
--               --1 Stage_id1 36 ----------------------------------------------------------------------------------------------------
--              /            |                                                         \                                              \
-- 2 Stage_id2 7             8 Stage_id5 17--------------                            18 Stage_id10 25-------------                    26 Stage_id14 35-----------
--       |       \                      |                 \                            /          |               \                           |                   \
-- 3 Stage_id3 4   5 Stage_id4 6      9 Stage_id6 10    11 Stage_id7 16    19 Stage_id11 20    21 Stage_id12 22    23 Stage_id13 24    27 Stage_id15 32-------   33 Stage_id18 34
--                                                        /         \                                                                         |               \
--                                              12 Stage_id8 13    14 Stage_id9 15                                                     28 Stage_id16 29    30 Stage_id17 31

        INSERT INTO stage (stageId, stageName, stageDescription, stageLeftBound, 
        stageRightBound, stageLevel, tooGranular, groupingStage) 
        VALUES ('Stage_id1', 'stageN1', 'stage Desc 1', 1, 36, 1, 0, 1),
               ('Stage_id2', 'stageN2', 'stage Desc 2', 2, 7, 2, 0, 0), 
               ('Stage_id3', 'stageN3', 'stage Desc 3', 3, 4, 3, 1, 0), 
               ('Stage_id4', 'stageN4', 'stage Desc 4', 5, 6, 3, 0, 0), 
               ('Stage_id5', 'stageN5', 'stage Desc 5', 8, 17, 2, 0, 0), 
               ('Stage_id6', 'stageN6', 'stage Desc 6', 9, 10, 3, 0, 0), 
               ('Stage_id7', 'stageN7', 'stage Desc 7', 11, 16, 3, 0, 0), 
               ('Stage_id8', 'stageN8', 'stage Desc 8', 12, 13, 4, 0, 1), 
               ('Stage_id9', 'stageN9', 'stage Desc 9', 14, 15, 4, 0, 0), 
               ('Stage_id10', 'stageN10', 'stage Desc 10', 18, 25, 2, 0, 1), 
               ('Stage_id11', 'stageN11', 'stage Desc 11', 19, 20, 3, 0, 1), 
               ('Stage_id12', 'stageN12', 'stage Desc 12', 21, 22, 3, 0, 1), 
               ('Stage_id13', 'stageN13', 'stage Desc 13', 23, 24, 3, 0, 1), 
               ('Stage_id14', 'stageN14', 'stage Desc 14', 26, 35, 2, 0, 0), 
               ('Stage_id15', 'stageN15', 'stage Desc 15', 27, 32, 3, 0, 1), 
               ('Stage_id16', 'stageN16', 'stage Desc 16', 28, 29, 4, 0, 0), 
               ('Stage_id17', 'stageN17', 'stage Desc 17', 30, 31, 4, 0, 0), 
               ('Stage_id18', 'stageN18', 'stage Desc 18', 33, 34, 3, 0, 0);
               
        INSERT INTO stageTaxonConstraint (stageId, speciesId)
        VALUES ('Stage_id1',  null),
               ('Stage_id2',  null),
               ('Stage_id3',  21), 
               ('Stage_id4',  31), 
               ('Stage_id5',  11), 
               ('Stage_id5',  21), 
               ('Stage_id6',  11), 
               ('Stage_id6',  21), 
               ('Stage_id7',  11), 
               ('Stage_id7',  21), 
               ('Stage_id8',  11), 
               ('Stage_id9',  21), 
               ('Stage_id10', 21), 
               ('Stage_id11', 21), 
               ('Stage_id12', 21), 
               ('Stage_id13', 21), 
               ('Stage_id14', null), 
               ('Stage_id15', null), 
               ('Stage_id16', 11), 
               ('Stage_id17', 31), 
               ('Stage_id18', 11);

        INSERT INTO anatEntity(anatEntityId,anatEntityName,anatEntityDescription,startStageId,endStageId,nonInformative)
        VALUES ('Anat_id1','anatStruct','anatStruct desc','Stage_id1','Stage_id2',true),
               ('Anat_id2','organ','organ desc','Stage_id10','Stage_id18',false),
               ('Anat_id3','heart','heart desc','Stage_id16','Stage_id18',false),
               ('Anat_id4','gill','gill desc','Stage_id12','Stage_id18',false),
               ('Anat_id5','brain','brain desc','Stage_id11','Stage_id17',false),
               ('Anat_id6','embryoStruct','embryoStruct desc','Stage_id2','Stage_id5',false),
               ('Anat_id7','ectoderm','ectoderm desc','Stage_id6','Stage_id13',false),
               ('Anat_id8','neuralTube','neuralTube desc','Stage_id8','Stage_id17',false),
               ('Anat_id9','forebrain','forebrain desc','Stage_id8','Stage_id17',false),
               ('Anat_id10','hindbrain','hindbrain desc','Stage_id8','Stage_id17',true),
               ('Anat_id11','cerebellum','cerebellum desc','Stage_id9','Stage_id13',false),
               ('Anat_id12','anat12','unused anatE 12','Stage_id1','Stage_id13',false),
               ('Anat_id13','anat13','unused anatE 13','Stage_id9','Stage_id10',true);

        INSERT INTO anatEntityTaxonConstraint(anatEntityId,speciesId)
        VALUES ('Anat_id1',null),
               ('Anat_id2',11),
               ('Anat_id2',21),
               ('Anat_id3',31),
               ('Anat_id4',31),
               ('Anat_id5',21),
               ('Anat_id6',null),
               ('Anat_id7',21),
               ('Anat_id7',31),
               ('Anat_id8',11),
               ('Anat_id9',21),
               ('Anat_id10',31),
               ('Anat_id11',null),
               ('Anat_id12',21),
               ('Anat_id13',null);
               
        INSERT INTO anatEntityRelation(anatEntityRelationId,anatEntitySourceId,anatEntityTargetId,relationType,relationStatus)
        VALUES (1,'Anat_id1','Anat_id1','is_a part_of','reflexive'),
               (2,'Anat_id2','Anat_id2','is_a part_of','reflexive'),
               (3,'Anat_id3','Anat_id3','is_a part_of','reflexive'),
               (4,'Anat_id4','Anat_id4','is_a part_of','reflexive'),
               (5,'Anat_id5','Anat_id5','is_a part_of','reflexive'),
               (6,'Anat_id6','Anat_id6','is_a part_of','reflexive'),
               (7,'Anat_id7','Anat_id7','is_a part_of','reflexive'),
               (8,'Anat_id8','Anat_id8','is_a part_of','reflexive'),
               (9,'Anat_id9','Anat_id9','is_a part_of','reflexive'),
               (10,'Anat_id10','Anat_id10','is_a part_of','reflexive'),
               (11,'Anat_id11','Anat_id11','is_a part_of','reflexive'),
               (12,'Anat_id2','Anat_id1','is_a part_of','direct'),
               (13,'Anat_id3','Anat_id2','is_a part_of','direct'),
               (14,'Anat_id4','Anat_id2','is_a part_of','direct'),
               (15,'Anat_id5','Anat_id2','is_a part_of','direct'),
               (16,'Anat_id5','Anat_id7','develops_from','indirect'),
               (17,'Anat_id5','Anat_id8','develops_from','direct'),
               (18,'Anat_id6','Anat_id1','is_a part_of','direct'),
               (19,'Anat_id7','Anat_id6','develops_from','direct'),
               (20,'Anat_id8','Anat_id7','is_a part_of','direct'),
               (21,'Anat_id9','Anat_id5','is_a part_of','direct'),
               (22,'Anat_id10','Anat_id5','is_a part_of','direct'),
               (23,'Anat_id11','Anat_id10','is_a part_of','direct');
               
        INSERT INTO anatEntityRelationTaxonConstraint(anatEntityRelationId,speciesId)
        VALUES (1,null),
               (2,11),(2,21),
               (3,31),
               (4,11),
               (5,null),
               (6,31),
               (7,21),(7,31),
               (8,11),
               (9,31),
               (10,null),
               (11,21),
               (12,21),(12,11),
               (13,31),
               (14,11),
               (15,null),
               (16,31),
               (17,31),
               (18,11),(18,21),
               (19,null),
               (20,31),
               (21,21),
               (22,31),
               (23,null);

        INSERT INTO expression(expressionId,geneId,anatEntityId,stageId,estData,affymetrixData,inSituData,rnaSeqData)
        VALUES (1,'ID3','Anat_id1','Stage_id1','no data','poor quality','high quality','high quality'), 
               (10,'ID1','Anat_id6','Stage_id8','high quality','high quality','no data','no data'),
               (2,'ID1','Anat_id6','Stage_id6','high quality','poor quality','high quality','poor quality'),
               (3,'ID1','Anat_id6','Stage_id7','no data','no data','no data','poor quality'),
               (4,'ID2','Anat_id2','Stage_id18','high quality','high quality','high quality','high quality'),
               (5,'ID1','Anat_id7','Stage_id10','poor quality','poor quality','poor quality','poor quality'),
               (6,'ID2','Anat_id11','Stage_id12','poor quality','high quality','no data','high quality'),
               (7,'ID2','Anat_id11','Stage_id13','high quality','no data','poor quality','no data'),
               (8,'ID3','Anat_id3','Stage_id1','high quality','no data','poor quality','no data'),
               (9,'ID2','Anat_id1','Stage_id9','poor quality','high quality','no data','high quality');

        INSERT INTO globalExpression(globalExpressionId,geneId,anatEntityId,stageId,estData,affymetrixData,inSituData,rnaSeqData,originOfLine)
        VALUES (1,'ID3','Anat_id1','Stage_id1','no data','poor quality','high quality','high quality','self'),
               (2,'ID1','Anat_id6','Stage_id6','high quality','poor quality','high quality','poor quality','self'),
               (3,'ID1','Anat_id6','Stage_id7','no data','no data','no data','poor quality','self'),
               (4,'ID2','Anat_id2','Stage_id18','high quality','high quality','high quality','high quality','self'),
               (5,'ID1','Anat_id7','Stage_id10','poor quality','poor quality','poor quality','poor quality','both'),
               (6,'ID2','Anat_id11','Stage_id12','poor quality','high quality','no data','high quality','self'),
               (7,'ID2','Anat_id11','Stage_id13','high quality','no data','poor quality','no data','self'),
               (8,'ID3','Anat_id2','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (9,'ID3','Anat_id3','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (10,'ID3','Anat_id4','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (11,'ID3','Anat_id5','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (12,'ID3','Anat_id6','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (13,'ID3','Anat_id9','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (14,'ID3','Anat_id10','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (15,'ID3','Anat_id11','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (23,'ID2','Anat_id1','Stage_id1','poor quality','high quality','no data','high quality','descent'),
               (16,'ID2','Anat_id3','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (17,'ID2','Anat_id4','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (18,'ID2','Anat_id5','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (19,'ID2','Anat_id9','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (20,'ID2','Anat_id10','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (21,'ID2','Anat_id11','Stage_id18','high quality','high quality','high quality','high quality','descent'), 
               (22,'ID1','Anat_id6','Stage_id8','high quality','high quality','no data','no data', 'self'), 
               (24,'ID1','Anat_id1','Stage_id8','high quality','high quality','no data','no data', 'descent');

        INSERT INTO globalExpressionToExpression (expressionId,globalExpressionId) 
        VALUES (1, 1),
               (1, 8),
               (1, 9),
               (1, 10),
               (1, 11),
               (1, 12),
               (1, 13),
               (1, 14),
               (1, 15),
               (1, 23),
               (4, 4),
               (4, 16),
               (4, 17),
               (4, 18),
               (4, 19),
               (4, 20),
               (4, 21),
               (10, 22);

        INSERT INTO noExpression(noExpressionId,geneId,anatEntityId,stageId,noExpressionAffymetrixData,noExpressionInSituData,noExpressionRnaSeqData)
        VALUES (1,'ID2','Anat_id5','Stage_id13','poor quality','high quality','high quality'),
               (2,'ID1','Anat_id1','Stage_id1','high quality','high quality','poor quality'),
               (3,'ID3','Anat_id6','Stage_id6','no data','no data','poor quality'),
               (4,'ID2','Anat_id11','Stage_id11','high quality','high quality','high quality'),
               (5,'ID3','Anat_id8','Stage_id10','poor quality','poor quality','poor quality'),
               (6,'ID3','Anat_id6','Stage_id7','poor quality','no data','high quality'),
               (7,'ID3','Anat_id5','Stage_id6','high quality','no data','high quality'),
               (8,'ID3','Anat_id5','Stage_id14','poor quality','high quality','no data');

        INSERT INTO globalNoExpression(globalNoExpressionId,geneId,anatEntityId,stageId,noExpressionAffymetrixData,noExpressionInSituData,noExpressionRnaSeqData,noExpressionOriginOfLine)
        VALUES (1,'ID2','Anat_id5','Stage_id13','poor quality','high quality','high quality','self'),
               (2,'ID2','Anat_id2','Stage_id13','poor quality','high quality','high quality','both'),
               (3,'ID2','Anat_id1','Stage_id13','poor quality','high quality','high quality','parent'),
               (4,'ID3','Anat_id6','Stage_id6','no data','no data','poor quality','self'),
               (5,'ID3','Anat_id5','Stage_id6','high quality','no data','high quality','both'),
               (6,'ID3','Anat_id1','Stage_id6','no data','no data','poor quality','parent'),
               (7,'ID2','Anat_id11','Stage_id11','high quality','high quality','high quality','self'),
               (8,'ID2','Anat_id10','Stage_id11','high quality','high quality','high quality','parent'),
               (9,'ID2','Anat_id1','Stage_id11','high quality','high quality','high quality','parent'),
               (10,'ID3','Anat_id8','Stage_id10','poor quality','poor quality','poor quality','self'),
               (11,'ID3','Anat_id7','Stage_id10','poor quality','poor quality','poor quality','parent'),
               (12,'ID3','Anat_id6','Stage_id7','poor quality','no data','high quality','self'),
               (13,'ID3','Anat_id1','Stage_id7','poor quality','no data','high quality','parent');

        INSERT INTO globalNoExpressionToNoExpression (noExpressionId,globalNoExpressionId) 
        VALUES (1,1),
               (1,2),
               (1,3),
               (3,4),
               (3,5),
               (3,6),
               (4,7),
               (4,8),
               (4,9),
               (5,10),
               (5,11),
               (6,12),
               (6,13);

        INSERT INTO estLibrary (estLibraryId,estLibraryName,estLibraryDescription,anatEntityId,stageId,dataSourceId)
        VALUES ('424', 'DKFZphamy1', 'DescDKFZ', 'Anat_id4', 'Stage_id4', 1), 
               ('437', 'NCI_CGAP_Ov6', 'DescNCI', 'Anat_id13', 'Stage_id5', 1);

        INSERT INTO expressedSequenceTag (estId, estId2, estLibraryId, geneId, UniGeneClusterId, expressionId, estData)
        VALUES ('AA000001.1', 'g1392161', '424', 'ID3', 'Hs.528780', 8, 'high quality'),
               ('AA000012.1', 'g1435877', '437', 'ID1', 'Mm.276405', 3, 'poor quality');

        INSERT INTO microarrayExperiment (microarrayExperimentId, microarrayExperimentName, microarrayExperimentDescription, dataSourceId)
        VALUES ('E-AFMX-1', 'microName1', 'microarrayDesc1', 1);

        INSERT INTO chipType (chipTypeId, chipTypeName, cdfName, isCompatible, qualityScoreThreshold, percentPresentThreshold)
        VALUES ('A-AFFY-1', 'U95Av2', 'HG_U95Av2', true, 45759.68, 22.76);

        INSERT INTO affymetrixChip (bgeeAffymetrixChipId, affymetrixChipId, microarrayExperimentId, chipTypeId, scanDate, normalizationType, detectionType, anatEntityId, stageId, qualityScore, percentPresent)
        VALUES (12359, 'h4a', 'E-AFMX-1', 'A-AFFY-1', '03/18/03 15:00:03', 'gcRMA', 'Schuster', 'Anat_id7', 'Stage_id2', 52229.94, 45.00),
               (12361, 'h6a', 'E-AFMX-1', 'A-AFFY-1', '03/18/03 17:03:23', 'gcRMA', 'Schuster', 'Anat_id11', 'Stage_id15', 51850.62, 48.92);

        INSERT INTO affymetrixProbeset (affymetrixProbesetId, bgeeAffymetrixChipId, geneId, normalizedSignalIntensity, detectionFlag, expressionId, noExpressionId, affymetrixData, reasonForExclusion)
        VALUES ('1006_at', 12359, 'ID2', 2.21, 'absent', NULL, 1, 'high quality', 'not excluded'),
               ('1007_s_at', 12359, 'ID3', 9.08, 'present', 1, NULL, 'high quality', 'not excluded'),
               ('1041_at', 12361, 'ID1', 2.21, 'absent', NULL, NULL, 'high quality', 'pre-filtering'),
               ('1041_xx', 12361, 'ID2', 2.24, 'absent', NULL, 4, 'high quality', 'not excluded'),
               ('32233_at', 12361, 'ID2', 2.66, 'marginal', NULL, NULL, 'poor quality', 'undefined');

        INSERT INTO inSituExperiment (inSituExperimentId, inSituExperimentName, inSituExperimentDescription, dataSourceId)
        VALUES ('MGI:3041492', 'name1', '', 1),
               ('MGI:2677299', 'name2', 'desc2', 1),
               ('BDGP_IP07646', '', '', 1),
               ('FBrf0219073', '', 'desc4', 1);

        INSERT INTO inSituEvidence (inSituEvidenceId, inSituExperimentId, evidenceDistinguishable, inSituEvidenceUrlPart)
        VALUES ('MGI:3041492.9', 'MGI:3041492', true, ''),
               ('MGI:2677299.7', 'MGI:2677299', true, '5V_id'),
               ('BDGP_140958', 'BDGP_IP07646', true, ''),
               ('FBrf0219073.2', 'FBrf0219073', false, 'url2');
               

        INSERT INTO inSituSpot (inSituSpotId, inSituEvidenceId, inSituExpressionPatternId, anatEntityId, stageId, geneId, detectionFlag, expressionId, noExpressionId, inSituData, reasonForExclusion)
        VALUES ('mgi-1118', 'MGI:3041492.9', '', 'Anat_id1', 'Stage_id1', 'ID3', 'present', 8, NULL, 'high quality', 'not excluded'),
               ('mgi-1061', 'MGI:2677299.7', '', 'Anat_id10', 'Stage_id2', 'ID3', 'absent', NULL, 2, 'poor quality', 'not excluded'),
               ('BDGP-10000', 'BDGP_140958', '', 'Anat_id11', 'Stage_id8', 'ID1', 'absent', NULL, 2, 'poor quality', 'not excluded'),
               ('flybase-10', 'FBrf0219073.2', '', 'Anat_id13', 'Stage_id5', 'ID2', 'undefined', NULL, NULL, 'high quality', 'undefined');

        INSERT INTO rnaSeqExperiment (rnaSeqExperimentId, rnaSeqExperimentName, rnaSeqExperimentDescription, dataSourceId)
        VALUES ('GSE41338', 'ExpName1', 'ExpDesc1', 1);

        INSERT INTO rnaSeqPlatform (rnaSeqPlatformId, rnaSeqPlatformDescription)
        VALUES ('Illumina HiSeq 2000', '');

        INSERT INTO rnaSeqLibrary(rnaSeqLibraryId, rnaSeqSecondaryLibraryId, rnaSeqExperimentId, rnaSeqPlatformId, anatEntityId, stageId, log2RPKThreshold, allGenesPercentPresent, proteinCodingGenesPercentPresent, intergenicRegionsPercentPresent, allReadsCount, leftMappedReadsCount, rightMappedReadsCount, minReadLength, maxReadLength, libraryType, libraryOrientation)
        VALUES ('GSM1015161', 'SRX191160', 'GSE41338', 'Illumina HiSeq 2000', 'Anat_id11', 'Stage_id1', 1.000000, 68.27, 58.96, 15.85, 91641467, 33352222, 32332998, 75, 75, 'paired', 'unstranded'),
               ('GSM1015164', 'SRX191163', 'GSE41338', 'Illumina HiSeq 2000', 'Anat_id13', 'Stage_id18', 1.000000, 43.85, 37.72, 6.23, 81401754, 28408829, 28299304, 75, 75, 'paired', 'unstranded'),
               ('GSM1015162', 'SRX191161', 'GSE41338', 'Illumina HiSeq 2000', 'Anat_id10', 'Stage_id4', 1.000000, 60.13, 51.96, 12.61, 81401754, 43858614, 10260750, 75, 75, 'paired', 'unstranded');

       INSERT INTO rnaSeqResult (rnaSeqLibraryId, geneId, log2RPK, readsCount, expressionId, noExpressionId, detectionFlag, rnaSeqData, reasonForExclusion)
        VALUES ('GSM1015164', 'ID1', 0.780113, 117, 2, NULL, 'present', 'high quality', 'not excluded'),
               ('GSM1015161', 'ID1', -26.575425, 0, NULL, NULL, 'absent', 'high quality', 'pre-filtering'),
               ('GSM1015161', 'ID2', -1.687530, 31, NULL, 4, 'absent', 'high quality', 'not excluded'),
               ('GSM1015162', 'ID3', -2.462678, 31, NULL, 8, 'absent', 'poor quality', 'not excluded');
    END IF; 
END
;

-- Procedure to empty table in UPDATE/INSERT databases used for integration tests.
DROP PROCEDURE IF EXISTS emptyTestDBs
;

CREATE PROCEDURE emptyTestDBs()
BEGIN
    
-- SUMMARY NO-EXPRESSION CALLS
DELETE FROM differentialExpression;
DELETE FROM globalNoExpressionToNoExpression;
DELETE FROM globalNoExpression;
DELETE FROM noExpression;

-- SUMMARY EXPRESSION CALLS
DELETE FROM hogExpressionSummary;
DELETE FROM hogExpressionToExpression;
DELETE FROM hogExpression;
DELETE FROM globalExpressionToExpression;
DELETE FROM globalExpression;
DELETE FROM expression;

-- RAW DIFFERENTIAL EXPRESSION ANALYSES
DELETE FROM deaRNASeqSummary;
DELETE FROM deaAffymetrixProbesetSummary;
DELETE FROM deaSampleGroupToRnaSeqLibrary;
DELETE FROM deaSampleGroupToAffymetrixChip;
DELETE FROM deaSampleGroup;
DELETE FROM differentialExpressionAnalysis;

-- RNA-SEQ DATA
DELETE FROM rnaSeqResult;
DELETE FROM rnaSeqRun;
DELETE FROM rnaSeqLibrary;
DELETE FROM rnaSeqPlatform;
DELETE FROM rnaSeqExperimentToKeyword;
DELETE FROM rnaSeqExperiment;

-- IN SITU HYBRIDIZATION DATA
DELETE FROM inSituSpot;
DELETE FROM inSituEvidence;
DELETE FROM inSituExperimentToKeyword;
DELETE FROM inSituExperiment;

-- RAW AFFYMETRIX DATA
DELETE FROM affymetrixProbeset;
DELETE FROM affymetrixChip;
DELETE FROM chipType;
DELETE FROM microarrayExperimentToKeyword;
DELETE FROM microarrayExperiment;

-- RAW EST DATA
DELETE FROM expressedSequenceTag;
DELETE FROM estLibraryToKeyword;
DELETE FROM estLibrary;

-- GENE
DELETE FROM geneToGeneOntologyTerm;
DELETE FROM geneToTerm;
DELETE FROM geneXRef;
DELETE FROM geneNameSynonym;
DELETE FROM gene;
DELETE FROM geneBioType;
DELETE FROM geneOntologyRelation;
DELETE FROM geneOntologyTermAltId;
DELETE FROM geneOntologyTerm;
DELETE FROM OMAHierarchicalGroup;

-- ANATOMY AND DEVELOPMENT
DELETE FROM anatEntityRelationTaxonConstraint;
DELETE FROM anatEntityRelation;
DELETE FROM anatEntityNameSynonym;
DELETE FROM anatEntityXRef;
DELETE FROM anatEntityTaxonConstraint;
DELETE FROM anatEntity;
DELETE FROM stageXRef;
DELETE FROM stageNameSynonym;
DELETE FROM stageTaxonConstraint;
DELETE FROM stage;

-- TAXONOMY
DELETE FROM species;
DELETE FROM taxon;

-- GENERAL
DELETE FROM keyword;
DELETE FROM author;
DELETE FROM dataSource;

END
;
