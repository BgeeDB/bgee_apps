package org.bgee.model.topanat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.DoubleCellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

public class TopAnatResults {
    private final static Logger log = LogManager
            .getLogger(TopAnatResults.class.getName());

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

        private final double pval;

        private final double fdr;

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
            temp = Double.doubleToLongBits(fdr);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(pval);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(significant);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TopAnatResultRow other = (TopAnatResultRow) obj;
            if (anatEntitiesId == null) {
                if (other.anatEntitiesId != null) {
                    return false;
                }
            } else if (!anatEntitiesId.equals(other.anatEntitiesId)) {
                return false;
            }
            if (anatEntitiesName == null) {
                if (other.anatEntitiesName != null) {
                    return false;
                }
            } else if (!anatEntitiesName.equals(other.anatEntitiesName)) {
                return false;
            }
            if (Double.doubleToLongBits(annotated) != Double.doubleToLongBits(other.annotated)) {
                return false;
            }
            if (Double.doubleToLongBits(enrich) != Double.doubleToLongBits(other.enrich)) {
                return false;
            }
            if (Double.doubleToLongBits(expected) != Double.doubleToLongBits(other.expected)) {
                return false;
            }
            if (Double.doubleToLongBits(fdr) != Double.doubleToLongBits(other.fdr)) {
                return false;
            }
            if (Double.doubleToLongBits(pval) != Double.doubleToLongBits(other.pval)) {
                return false;
            }
            if (Double.doubleToLongBits(significant) != Double.doubleToLongBits(other.significant)) {
                return false;
            }
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
    
    //TODO: javadoc. CellProcessor needed because R returns "Inf" in case of division by 0.
    //TODO: unit tests for this CellProcessor
    //TODO: regression test that TopAnatResults can now handle such a file with 'Inf' 
    //in the columns using CustomParseDouble.
    private static class CustomParseDouble extends CellProcessorAdaptor {
        private CustomParseDouble() {
            super();
        }
        public CustomParseDouble(DoubleCellProcessor next) {
                // this constructor allows other processors to be chained after ParseDay
                super(next);
        }

        @Override
        public Object execute(Object value, CsvContext context) {
            log.entry(value, context);
            //throws an Exception if the input is null, as all CellProcessors usually do.
            validateInputNotNull(value, context);  
            
            if (value.toString().contains("-Inf")) {
                return log.exit(next.execute(Double.NEGATIVE_INFINITY, context));
            } else if (value.toString().contains("Inf")) {
                return log.exit(next.execute(Double.POSITIVE_INFINITY, context));
            }
            
            //passes result to a ParseDouble, chained with the next processor in the chain if possible
            ParseDouble parse = null;
            if (next instanceof DoubleCellProcessor) {
                parse = new ParseDouble((DoubleCellProcessor) next);
            } else {
                parse = new ParseDouble();
            } 
            return log.exit(parse.execute(value, context));
        }
    }

    /**
     * 
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<TopAnatResults.TopAnatResultRow> getRows() throws FileNotFoundException,
    IOException{
        log.entry();
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
                        new org.supercsv.cellprocessor.Optional(), // AnatEntity Name
                        new NotNull(new ParseDouble()), // Annotated
                        new NotNull(new ParseDouble()), // Significant
                        new NotNull(new CustomParseDouble()), // Expected
                        new NotNull(new CustomParseDouble()), // fold enrich
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

            return log.exit(listToReturn);
   
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
        StringBuilder builder = new StringBuilder();
        builder.append("TopAnatResults [topAnatParams=").append(topAnatParams).append(", resultFileName=")
                .append(resultFileName).append(", resultPDFFileName=").append(resultPDFFileName)
                .append(", rScriptAnalysisFileName=").append(rScriptAnalysisFileName).append(", paramsOutputFileName=")
                .append(paramsOutputFileName).append(", anatEntitiesFilename=").append(anatEntitiesFilename)
                .append(", anatEntitiesRelationshipsFileName=").append(anatEntitiesRelationshipsFileName)
                .append(", geneToAnatEntitiesFileName=").append(geneToAnatEntitiesFileName)
                .append(", rScriptConsoleFileName=").append(rScriptConsoleFileName).append(", zipFileName=")
                .append(zipFileName).append(", controller=").append(controller).append(", props=").append(props)
                .append("]");
        return builder.toString();
    }
 
}

