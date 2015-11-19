package org.bgee.model.topanat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bgee.model.BgeeProperties;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

public class TopAnatResults {

    public static class TopAnatResultRow{

        /**
         * 
         */
        private final String anatEntitiesId;

        private final String anatEntitiesName;

        private final double annotated;

        private final double significant;

        private final double expected;

        private final double enrich;

        private final Double pval;

        private final Double fdr;

        public TopAnatResultRow(Map<String, Object> line){
            this.anatEntitiesId = (String) line.get("OrganId");
            this.anatEntitiesName = (String) line.get("OrganName");
            this.annotated = (Double) line.get("Annotated");
            this.significant = (Double) line.get("Significant");
            this.expected = (Double) line.get("Expected");
            this.enrich = (Double) line.get("foldEnrichment");
            this.pval = (Double) line.get("p");
            this.fdr = (Double) line.get("fdr");
        }

        public String getAnatEntitiesId() {
            return anatEntitiesId;
        }

        public String getAnatEntitiesName() {
            return anatEntitiesName;
        }

        public double getAnnotated() {
            return annotated;
        }

        public double getSignificant() {
            return significant;
        }

        public double getExpected() {
            return expected;
        }

        public double getEnrich() {
            return enrich;
        }

        public double getPval() {
            return pval;
        }

        public double getFdr() {
            return fdr;
        }

    }

    private final TopAnatParams topAnatParams; 

    private final String resultFileName;

    private final String resultPDFFileName;

    private final String rScriptAnalysisFileName;

    private final String paramsOutputFileName;

    private final String anatEntitiesFilename;

    private final String anatEntitiesRelationshipsFileName;

    private final String geneToAnatEntitiesFileName;

    private final String rScriptConsoleFileName;

    private final String zipFileName;

    private final TopAnatController controller;

    private final BgeeProperties props;

    public TopAnatResults(
            TopAnatParams topAnatParams,String resultFileName,
            String resultPDFFileName, String rScriptAnalysisFileName, String  paramsOutputFileName,
            String anatEntitiesFilename, String anatEntitiesRelationshipsFileName, 
            String geneToAnatEntitiesFileName, String rScriptConsoleFileName,
            String zipFileName, TopAnatController controller){
        this.topAnatParams = topAnatParams;
        this.resultFileName = resultFileName;
        this.resultPDFFileName = resultPDFFileName;
        this.rScriptAnalysisFileName = rScriptAnalysisFileName;
        this.rScriptConsoleFileName = rScriptConsoleFileName;
        this.paramsOutputFileName = paramsOutputFileName;
        this.anatEntitiesFilename = anatEntitiesFilename;
        this.anatEntitiesRelationshipsFileName = anatEntitiesRelationshipsFileName;
        this.geneToAnatEntitiesFileName = geneToAnatEntitiesFileName;
        this.zipFileName = zipFileName;
        this.controller = controller;
        this.props = controller.getBgeeProperties();
    }

    /**
     * 
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<TopAnatResults.TopAnatResultRow> getRows() throws FileNotFoundException,
    IOException{
            File resultFile = new File(
                    this.props.getTopAnatResultsWritingDirectory(),
                    this.getResultFileName());

            this.controller.acquireReadLock(resultFile.getPath());

            List<TopAnatResults.TopAnatResultRow> listToReturn 
            = new ArrayList<TopAnatResults.TopAnatResultRow>();
            
            try (ICsvMapReader mapReader = 
                    new CsvMapReader(new FileReader(resultFile), 
                            CsvPreference.TAB_PREFERENCE)) {
                String[] header = mapReader.getHeader(true);
                CellProcessor[] processors = new CellProcessor[] { 
                        new NotNull(), // AnatEntity Id
                        new Optional(), // AnatEntity Name
                        new NotNull(new ParseDouble()), // Annotated
                        new NotNull(new ParseDouble()), // Significant
                        new NotNull(new ParseDouble()), // Expected
                        new NotNull(new ParseDouble()), // fold enrich
                        new NotNull(new ParseDouble()), // p
                        new NotNull(new ParseDouble()) // fdr
                };
                Map<String, Object> row;
                if(header != null){
                    // Stream all lines TODO
                    while( (row = mapReader.read(header, processors)) != null ) {
                        listToReturn.add(new TopAnatResults.TopAnatResultRow(row));
                    }
                }
            }

            this.controller.releaseReadLock(resultFile.getPath());

            return listToReturn;
   
    }

    public TopAnatParams getTopAnatParams() {
        return topAnatParams;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public String getResultPDFFileName() {
        return resultPDFFileName;
    }

    public String getRScriptAnalysisFileName() {
        return rScriptAnalysisFileName;
    }

    public String getParamsOutputFileName() {
        return paramsOutputFileName;
    }

    public String getAnatEntitiesFilename() {
        return anatEntitiesFilename;
    }

    public String getAnatEntitiesRelationshipsFileName() {
        return anatEntitiesRelationshipsFileName;
    }

    public String getGeneToAnatEntitiesFileName() {
        return geneToAnatEntitiesFileName;
    }

    public String getRScriptConsoleFileName() {
        return rScriptConsoleFileName;
    }

    public String getZipFileName() {
        return zipFileName;
    }

}

