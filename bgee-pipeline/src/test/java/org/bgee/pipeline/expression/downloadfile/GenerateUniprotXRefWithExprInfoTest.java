package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.expression.downloadfile.GenerateUniprotXRefWithExprInfo.XrefUniprotBean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
* Unit tests for {@link GenerateUniprotXRefWithExprInfo}.
*
* @author  Julien Wollbrett
* @version Bgee 14 Aug 2018
*/

public class GenerateUniprotXRefWithExprInfoTest extends TestAncestor {

    private static final String XREF_FILE = "/downloadfile/XRefBgee.tsv";
    private final static Logger log = LogManager.getLogger(GenerateUniprotXRefWithExprInfoTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    }
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    
    /**
    * Test {@link GenerateUniprotXRefWithExprInfo#loadXrefFileWithoutExprInfo}.
    * Tested separately because the approach used to load Xref could change
    */
   @Test
   public void shouldLoadXRef() {
       //*** create objects require for test ***
       Set<XrefUniprotBean> xrefUniprotListWanted = new HashSet<>(Arrays.asList(
               new XrefUniprotBean("I3L1T2", "ENSG00000141198", 9606),
               new XrefUniprotBean("I3L367", "ENSG00000141198", 9606),
               new XrefUniprotBean("Q15615", "ENSG00000141219", 9606),
               new XrefUniprotBean("H9G367", "ENSACAG00000000004", 28377),
               new XrefUniprotBean("G1K846", "ENSACAG00000000006", 28377)));
       
       
       ServiceFactory serviceFactory = mock(ServiceFactory.class);
       
       GenerateUniprotXRefWithExprInfo generateUniprotXrefs = new GenerateUniprotXRefWithExprInfo(() -> serviceFactory);
       Set<XrefUniprotBean> xrefUniprotListLoaded = generateUniprotXrefs
               .loadXrefFileWithoutExprInfo(this.getClass().getResource(XREF_FILE).getFile());
       assertTrue(xrefUniprotListLoaded.equals(xrefUniprotListWanted));
        
    }

    /**
     * Test {@link GenerateUniprotXRefWithExprInfo#generate}.
     */
    @Test
    public void shouldGenerateUniprotXRefFile() throws IOException {
        
      ///*** create objects require for test ***
        Species sp1 = new Species(9606);
        Species sp2 = new Species(28377);
        
        Gene g1 = new Gene("ENSG00000141198", sp1, new GeneBioType("type1"));
        Gene g2 = new Gene("ENSG00000141219", sp1, new GeneBioType("type1"));
        Gene g3 = new Gene("ENSACAG00000000004", sp2, new GeneBioType("type1"));
        Gene g4 = new Gene("ENSACAG00000000006", sp2, new GeneBioType("type1"));

        AnatEntity ae1 = new AnatEntity("anat1", "anat1Name", "anat1Desc");
        AnatEntity ae2 = new AnatEntity("anat2", "anat2Name", "anat2Desc");
        AnatEntity ae3 = new AnatEntity("anat3", "anat3Name", "anat3Desc");
        AnatEntity ae4 = new AnatEntity("anat4", "anat4Name", "anat4Desc");
        AnatEntity ae5 = new AnatEntity("anat5", "anat5Name", "anat5Desc");
        
        ExpressionCall call1 = new ExpressionCall(null, null, null, null, null, 
                null, new BigDecimal("2.0"), new BigDecimal("2.0"));
        ExpressionCall call2 = new ExpressionCall(null, null, null, null, null, 
                null, new BigDecimal("4.0"), new BigDecimal("4.0"));
        ExpressionCall call3 = new ExpressionCall(null, null, null, null, null, 
                null, new BigDecimal("6.0"), new BigDecimal("6.0"));

        List<ExpressionCall> calls1 = Arrays.asList(call1, call2, call3);
        List<ExpressionCall> calls2 = Arrays.asList(call3, call1);
        List<ExpressionCall> calls3 = Arrays.asList(call3, call2);
        List<ExpressionCall> calls4 = Arrays.asList(call2, call1);
        
        LinkedHashMap<AnatEntity, List<ExpressionCall>> callsGene1 = new LinkedHashMap<>();
        callsGene1.put(ae1, calls1);
        callsGene1.put(ae2, calls3);
        callsGene1.put(ae3, calls3);
        callsGene1.put(ae4, calls3);
        callsGene1.put(ae5, calls3);
        
        LinkedHashMap<AnatEntity, List<ExpressionCall>> callsGene2 = new LinkedHashMap<>();
        callsGene2.put(ae5, calls2);
        callsGene2.put(ae2, calls4);
        callsGene2.put(ae3, calls4);
        callsGene2.put(ae4, calls4);
        
        LinkedHashMap<AnatEntity, List<ExpressionCall>> callsGene3 = new LinkedHashMap<>();
        callsGene3.put(ae3, calls4);
        
        LinkedHashMap<AnatEntity, List<ExpressionCall>> callsGene4 = null;
        
               
        // Mock services
        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        SpeciesService speciesService = mock(SpeciesService.class);
        when(serviceFactory.getSpeciesService()).thenReturn(speciesService);
        CallService callService = mock(CallService.class);
        when(serviceFactory.getCallService()).thenReturn(callService);

        // Mock methods
        when(speciesService.loadSpeciesByIds(null, false)).thenReturn(new HashSet<>(Arrays.asList(sp1, sp2)));
        when(callService.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                new GeneFilter(g1.getSpecies().getId(), g1.getEnsemblGeneId()))).thenReturn(callsGene1);
        when(callService.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                new GeneFilter(g2.getSpecies().getId(), g2.getEnsemblGeneId()))).thenReturn(callsGene2);
        when(callService.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                new GeneFilter(g3.getSpecies().getId(), g3.getEnsemblGeneId()))).thenReturn(callsGene3);
        when(callService.loadCondCallsWithSilverAnatEntityCallsByAnatEntity(
                new GeneFilter(g4.getSpecies().getId(), g4.getEnsemblGeneId()))).thenReturn(callsGene4);
        
        String outputFile = testFolder.newFile("XRefBgee.tsv").getPath();
        
        //method to test
        GenerateUniprotXRefWithExprInfo generateUniproteXrefs = 
                new GenerateUniprotXRefWithExprInfo(() -> serviceFactory);
        generateUniproteXrefs.generate(this.getClass().getResource(XREF_FILE).getFile(), outputFile);

        //check file generation
        log.debug("Checking file {}", outputFile);
        assertTrue("File not created: " + outputFile, new File(outputFile).exists());
        
        List <String> fileLinesExpected = Arrays.asList(
                "H9G367   DR   Bgee; ENSACAG00000000004; Expressed in 1 organ, highest expression level in anat3Name",
                "I3L367   DR   Bgee; ENSG00000141198; Expressed in 5 organs, highest expression level in anat1Name",
                "I3L1T2   DR   Bgee; ENSG00000141198; Expressed in 5 organs, highest expression level in anat1Name",
                "Q15615   DR   Bgee; ENSG00000141219; Expressed in 4 organs, highest expression level in anat5Name");
        
        List <String> fileLines = Files.lines(Paths.get(outputFile)).collect(Collectors.toList());
        assertTrue("The file does not contains expected lines, expected:" + System.lineSeparator()
                + fileLinesExpected + ", but was:" + System.lineSeparator() + fileLines,
                fileLinesExpected.equals(fileLines));

    }
}
