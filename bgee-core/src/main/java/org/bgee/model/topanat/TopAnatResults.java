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

        @Override
        public String toString() {
            return "TopAnatResultRow [anatEntitiesId=" + anatEntitiesId + ", anatEntitiesName=" + anatEntitiesName
                    + ", annotated=" + annotated + ", significant=" + significant + ", expected=" + expected
                    + ", enrich=" + enrich + ", pval=" + pval + ", fdr=" + fdr + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((anatEntitiesId == null) ? 0 : anatEntitiesId.hashCode());
            result = prime * result + ((anatEntitiesName == null) ? 0 : anatEntitiesName.hashCode());
            long temp;
            temp = Double.doubleToLongBits(annotated);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(enrich);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(expected);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((fdr == null) ? 0 : fdr.hashCode());
            result = prime * result + ((pval == null) ? 0 : pval.hashCode());
            temp = Double.doubleToLongBits(significant);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TopAnatResultRow other = (TopAnatResultRow) obj;
            if (anatEntitiesId == null) {
                if (other.anatEntitiesId != null)
                    return false;
            } else if (!anatEntitiesId.equals(other.anatEntitiesId))
                return false;
            if (anatEntitiesName == null) {
                if (other.anatEntitiesName != null)
                    return false;
            } else if (!anatEntitiesName.equals(other.anatEntitiesName))
                return false;
            if (Double.doubleToLongBits(annotated) != Double.doubleToLongBits(other.annotated))
                return false;
            if (Double.doubleToLongBits(enrich) != Double.doubleToLongBits(other.enrich))
                return false;
            if (Double.doubleToLongBits(expected) != Double.doubleToLongBits(other.expected))
                return false;
            if (fdr == null) {
                if (other.fdr != null)
                    return false;
            } else if (!fdr.equals(other.fdr))
                return false;
            if (pval == null) {
                if (other.pval != null)
                    return false;
            } else if (!pval.equals(other.pval))
                return false;
            if (Double.doubleToLongBits(significant) != Double.doubleToLongBits(other.significant))
                return false;
            return true;
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
    
    @Override
    public String toString() {
        return "TopAnatResults [topAnatParams=" + topAnatParams + ", resultFileName=" + resultFileName
                + ", resultPDFFileName=" + resultPDFFileName + ", rScriptAnalysisFileName=" + rScriptAnalysisFileName
                + ", paramsOutputFileName=" + paramsOutputFileName + ", anatEntitiesFilename=" + anatEntitiesFilename
                + ", anatEntitiesRelationshipsFileName=" + anatEntitiesRelationshipsFileName
                + ", geneToAnatEntitiesFileName=" + geneToAnatEntitiesFileName + ", rScriptConsoleFileName="
                + rScriptConsoleFileName + ", zipFileName=" + zipFileName + ", controller=" + controller + ", props="
                + props + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntitiesFilename == null) ? 0 : anatEntitiesFilename.hashCode());
        result = prime * result
                + ((anatEntitiesRelationshipsFileName == null) ? 0 : anatEntitiesRelationshipsFileName.hashCode());
        result = prime * result + ((controller == null) ? 0 : controller.hashCode());
        result = prime * result + ((geneToAnatEntitiesFileName == null) ? 0 : geneToAnatEntitiesFileName.hashCode());
        result = prime * result + ((paramsOutputFileName == null) ? 0 : paramsOutputFileName.hashCode());
        result = prime * result + ((props == null) ? 0 : props.hashCode());
        result = prime * result + ((rScriptAnalysisFileName == null) ? 0 : rScriptAnalysisFileName.hashCode());
        result = prime * result + ((rScriptConsoleFileName == null) ? 0 : rScriptConsoleFileName.hashCode());
        result = prime * result + ((resultFileName == null) ? 0 : resultFileName.hashCode());
        result = prime * result + ((resultPDFFileName == null) ? 0 : resultPDFFileName.hashCode());
        result = prime * result + ((topAnatParams == null) ? 0 : topAnatParams.hashCode());
        result = prime * result + ((zipFileName == null) ? 0 : zipFileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TopAnatResults other = (TopAnatResults) obj;
        if (anatEntitiesFilename == null) {
            if (other.anatEntitiesFilename != null)
                return false;
        } else if (!anatEntitiesFilename.equals(other.anatEntitiesFilename))
            return false;
        if (anatEntitiesRelationshipsFileName == null) {
            if (other.anatEntitiesRelationshipsFileName != null)
                return false;
        } else if (!anatEntitiesRelationshipsFileName.equals(other.anatEntitiesRelationshipsFileName))
            return false;
        if (controller == null) {
            if (other.controller != null)
                return false;
        } else if (!controller.equals(other.controller))
            return false;
        if (geneToAnatEntitiesFileName == null) {
            if (other.geneToAnatEntitiesFileName != null)
                return false;
        } else if (!geneToAnatEntitiesFileName.equals(other.geneToAnatEntitiesFileName))
            return false;
        if (paramsOutputFileName == null) {
            if (other.paramsOutputFileName != null)
                return false;
        } else if (!paramsOutputFileName.equals(other.paramsOutputFileName))
            return false;
        if (props == null) {
            if (other.props != null)
                return false;
        } else if (!props.equals(other.props))
            return false;
        if (rScriptAnalysisFileName == null) {
            if (other.rScriptAnalysisFileName != null)
                return false;
        } else if (!rScriptAnalysisFileName.equals(other.rScriptAnalysisFileName))
            return false;
        if (rScriptConsoleFileName == null) {
            if (other.rScriptConsoleFileName != null)
                return false;
        } else if (!rScriptConsoleFileName.equals(other.rScriptConsoleFileName))
            return false;
        if (resultFileName == null) {
            if (other.resultFileName != null)
                return false;
        } else if (!resultFileName.equals(other.resultFileName))
            return false;
        if (resultPDFFileName == null) {
            if (other.resultPDFFileName != null)
                return false;
        } else if (!resultPDFFileName.equals(other.resultPDFFileName))
            return false;
        if (topAnatParams == null) {
            if (other.topAnatParams != null)
                return false;
        } else if (!topAnatParams.equals(other.topAnatParams))
            return false;
        if (zipFileName == null) {
            if (other.zipFileName != null)
                return false;
        } else if (!zipFileName.equals(other.zipFileName))
            return false;
        return true;
    }


}

