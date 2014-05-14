-- This is a dump file containing test data, that are used for the integration tests 
-- of SELECT statements.

INSERT INTO dataSource (dataSourceId, dataSourceName, XRefUrl, experimentUrl, 
    evidenceUrl, baseUrl, releaseDate, releaseVersion, dataSourceDescription, 
    toDisplay, category, displayOrder) VALUES 
    (1, 'First DataSource', 'XRefUrl', 'experimentUrl', 'evidenceUrl', 'baseUrl', 
    NOW(), '1.0', 'My custom data source', 1, 'Genomics database', 1);
    
INSERT INTO geneBioType (geneBioTypeId, geneBioTypeName) 
   	VALUES (12, 'geneBioTypeName12');

INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (1, 99, 1, 8, 111);
INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (2, 99, 2, 3, 211);
INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (3, 99, 4, 7, 311);
INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (4, 99, 5, 6, 411);
INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (5, 88, 9, 14, 111);
INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (6, 88, 10, 13, 211);
INSERT INTO OMAHierarchicalGroup (OMANodeId, OMAGroupId, OMANodeLeftBound, OMANodeRightBound, taxonId)
	VALUES (7, 88, 11, 12, 511);

INSERT INTO taxon (taxonId, taxonScientificName, taxonCommonName, taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA)
	VALUES (111, 'taxSName111', 'taxCName111', 1, 10, 1, 1);
INSERT INTO taxon (taxonId, taxonScientificName, taxonCommonName, taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA)
	VALUES (211, 'taxSName211', 'taxCName211', 2, 3, 2, 0);
INSERT INTO taxon (taxonId, taxonScientificName, taxonCommonName, taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA)
	VALUES (311, 'taxSName311', 'taxCName311', 4, 9, 2, 0);
INSERT INTO taxon (taxonId, taxonScientificName, taxonCommonName, taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA)
	VALUES (411, 'taxSName411', 'taxCName411', 5, 6, 1, 1);
INSERT INTO taxon (taxonId, taxonScientificName, taxonCommonName, taxonLeftBound, taxonRightBound, taxonLevel, bgeeSpeciesLCA)
	VALUES (511, 'taxSName511', 'taxCName511', 7, 8, 1, 1);

INSERT INTO species (speciesId, genus, species, speciesCommonName, taxonId)
	VALUES (11, 'gen11', 'sp11', 'spCName11', 111);
INSERT INTO species (speciesId, genus, species, speciesCommonName, taxonId)
	VALUES (21, 'gen21', 'sp21', 'spCName21', 211);
INSERT INTO species (speciesId, genus, species, speciesCommonName, taxonId)
	VALUES (31, 'gen31', 'sp31', 'spCName31', 311);

INSERT INTO gene (geneId, geneName, geneDescription, speciesId, geneBioTypeId, OMAParentNodeId, ensemblGene)
	VALUES ('ID1', 'genN1', 'genDesc1', 11, 12, 2, true);
INSERT INTO gene (geneId, geneName, geneDescription, speciesId, geneBioTypeId, OMAParentNodeId, ensemblGene)
	VALUES ('ID2', 'genN2', 'genDesc2', 21, null, null, true);
INSERT INTO gene (geneId, geneName, geneDescription, speciesId, geneBioTypeId, OMAParentNodeId, ensemblGene)
	VALUES ('ID3', 'genN3', 'genDesc3', 31, null, 3, false);

