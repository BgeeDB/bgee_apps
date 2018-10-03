package org.bgee.pipeline.expression.downloadfile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;

/**
 * Class used to generate statistics about expression data of genes and gene biotypes.
 *
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Oct. 2018
 * @since   Bgee 14 Sep. 2018
 */
public class GenerateInsertGeneStats {

    private final static Logger log = LogManager.getLogger(GenerateInsertGeneStats.class.getName());

    /**
     * A {@code String} that is the name of the column containing gene IDs, in the download file.
     */
    public final static String GENE_ID_COLUMN_NAME = "Gene ID";
    public final static String GENE_NAME_COLUMN_NAME = "Gene name";
    public final static String BIO_TYPE_NAME_COLUMN_NAME = "Biotype";
    public final static String PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME = "Count of calls per anat. entity " +
            "showing expression of this gene with a bronze quality";
    public final static String PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME = "Count of calls per anat. entity " +
            "showing expression of this gene with a silver quality";
    public final static String PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME = "Count of calls per anat. entity " +
            "showing expression of this gene with a gold quality";
    public final static String ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME = "Count of calls per anat. entity " +
            "showing absence of expression of this gene with a bronze quality";
    public final static String ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME = "Count of calls per anat. entity " +
            "showing absence of expression of this gene with a silver quality";
    public final static String ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME = "Count of calls per anat. entity " +
            "showing absence of expression of this gene with a gold quality";
    public final static String PRESENT_BRONZE_COND_COLUMN_NAME = "Count of calls per condition " +
            "showing expression of this gene with a bronze quality";
    public final static String PRESENT_SILVER_COND_COLUMN_NAME = "Count of calls per condition " +
            "showing expression of this gene with a silver quality";
    public final static String PRESENT_GOLD_COND_COLUMN_NAME = "Count of calls per condition " +
            "showing expression of this gene with a gold quality";
    public final static String ABSENT_BRONZE_COND_COLUMN_NAME = "Count of calls per condition " +
            "showing absence of expression of this gene with a bronze quality";
    public final static String ABSENT_SILVER_COND_COLUMN_NAME = "Count of calls per condition " +
            "showing absence of expression of this gene with a silver quality";
    public final static String ABSENT_GOLD_COND_COLUMN_NAME = "Count of calls per condition " +
            "showing absence of expression of this gene with a gold quality";
    // FIXME fix the text
    public final static String FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME =
            "FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY";
    public final static String MIN_RANK_COLUMN_NAME = "Minimum rank for this gene";
    public final static String MAX_RANK_COLUMN_NAME = "Maximum rank for this gene";
    public final static String MIN_RANK_ANAT_ENTITY_COLUMN_NAME = "Anat. entity the minimum rank for this gene";

    public static class GeneStatsBean {
        private String geneId;
        private String geneName;
        private String bioTypeName;
        private int presentBronzeAnatEntity;
        private int presentSilverAnatEntity;
        private int presentGoldAnatEntity;
        private int absentBronzeAnatEntity;
        private int absentSilverAnatEntity;
        private int absentGoldAnatEntity;
        private int presentBronzeCond;
        private int presentSilverCond;
        private int presentGoldCond;
        private int absentBronzeCond;
        private int absentSilverCond;
        private int absentGoldCond;
        private int filteredGenePagePresentAnatEntity;
        private BigDecimal minRank;
        private BigDecimal maxRank;
        private AnatEntity minRankAnatEntity;

        public GeneStatsBean() {
            this.geneId = null;
            this.geneName = null;
            this.bioTypeName = null;
            this.presentBronzeAnatEntity = 0;
            this.presentSilverAnatEntity = 0;
            this.presentGoldAnatEntity = 0;
            this.absentBronzeAnatEntity = 0;
            this.absentSilverAnatEntity = 0;
            this.absentGoldAnatEntity = 0;
            this.presentBronzeCond = 0;
            this.presentSilverCond = 0;
            this.presentGoldCond = 0;
            this.absentBronzeCond = 0;
            this.absentSilverCond = 0;
            this.absentGoldCond = 0;
            this.filteredGenePagePresentAnatEntity = 0;
            this.minRank = null;
            this.maxRank = null;
            this.minRankAnatEntity = null;
        }

        public String getGeneId() {
            return geneId;
        }
        public void setGeneId(String geneId) {
            this.geneId = geneId;
        }

        public String getGeneName() {
            return geneName;
        }
        public void setGeneName(String geneName) {
            this.geneName = geneName;
        }

        public String getBioTypeName() {
            return bioTypeName;
        }
        public void setBioTypeName(String bioTypeName) {
            this.bioTypeName = bioTypeName;
        }

        public int getPresentBronzeAnatEntity() {
            return presentBronzeAnatEntity;
        }
        public void setPresentBronzeAnatEntity(int presentBronzeAnatEntity) {
            this.presentBronzeAnatEntity = presentBronzeAnatEntity;
        }

        public int getPresentSilverAnatEntity() {
            return presentSilverAnatEntity;
        }
        public void setPresentSilverAnatEntity(int presentSilverAnatEntity) {
            this.presentSilverAnatEntity = presentSilverAnatEntity;
        }

        public int getPresentGoldAnatEntity() {
            return presentGoldAnatEntity;
        }
        public void setPresentGoldAnatEntity(int presentGoldAnatEntity) {
            this.presentGoldAnatEntity = presentGoldAnatEntity;
        }

        public int getAbsentBronzeAnatEntity() {
            return absentBronzeAnatEntity;
        }
        public void setAbsentBronzeAnatEntity(int absentBronzeAnatEntity) {
            this.absentBronzeAnatEntity = absentBronzeAnatEntity;
        }

        public int getAbsentSilverAnatEntity() {
            return absentSilverAnatEntity;
        }
        public void setAbsentSilverAnatEntity(int absentSilverAnatEntity) {
            this.absentSilverAnatEntity = absentSilverAnatEntity;
        }

        public int getAbsentGoldAnatEntity() {
            return absentGoldAnatEntity;
        }
        public void setAbsentGoldAnatEntity(int absentGoldAnatEntity) {
            this.absentGoldAnatEntity = absentGoldAnatEntity;
        }

        public int getPresentBronzeCond() {
            return presentBronzeCond;
        }
        public void setPresentBronzeCond(int presentBronzeCond) {
            this.presentBronzeCond = presentBronzeCond;
        }

        public int getPresentSilverCond() {
            return presentSilverCond;
        }
        public void setPresentSilverCond(int presentSilverCond) {
            this.presentSilverCond = presentSilverCond;
        }

        public int getPresentGoldCond() {
            return presentGoldCond;
        }
        public void setPresentGoldCond(int presentGoldCond) {
            this.presentGoldCond = presentGoldCond;
        }

        public int getAbsentBronzeCond() {
            return absentBronzeCond;
        }
        public void setAbsentBronzeCond(int absentBronzeCond) {
            this.absentBronzeCond = absentBronzeCond;
        }

        public int getAbsentSilverCond() {
            return absentSilverCond;
        }
        public void setAbsentSilverCond(int absentSilverCond) {
            this.absentSilverCond = absentSilverCond;
        }

        public int getAbsentGoldCond() {
            return absentGoldCond;
        }
        public void setAbsentGoldCond(int absentGoldCond) {
            this.absentGoldCond = absentGoldCond;
        }

        public int getFilteredGenePagePresentAnatEntity() {
            return filteredGenePagePresentAnatEntity;
        }
        public void setFilteredGenePagePresentAnatEntity(int filteredGenePagePresentAnatEntity) {
            this.filteredGenePagePresentAnatEntity = filteredGenePagePresentAnatEntity;
        }

        public BigDecimal getMinRank() {
            return minRank;
        }
        public void setMinRank(BigDecimal minRank) {
            this.minRank = minRank;
        }

        public BigDecimal getMaxRank() {
            return maxRank;
        }
        public void setMaxRank(BigDecimal maxRank) {
            this.maxRank = maxRank;
        }

        public AnatEntity getMinRankAnatEntity() {
            return minRankAnatEntity;
        }
        public void setMinRankAnatEntity(AnatEntity minRankAnatEntity) {
            this.minRankAnatEntity = minRankAnatEntity;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + absentBronzeAnatEntity;
            result = prime * result + absentBronzeCond;
            result = prime * result + absentGoldAnatEntity;
            result = prime * result + absentGoldCond;
            result = prime * result + absentSilverAnatEntity;
            result = prime * result + absentSilverCond;
            result = prime * result + ((bioTypeName == null) ? 0 : bioTypeName.hashCode());
            result = prime * result + filteredGenePagePresentAnatEntity;
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((geneName == null) ? 0 : geneName.hashCode());
            result = prime * result + ((maxRank == null) ? 0 : maxRank.hashCode());
            result = prime * result + ((minRank == null) ? 0 : minRank.hashCode());
            result = prime * result + ((minRankAnatEntity == null) ? 0 : minRankAnatEntity.hashCode());
            result = prime * result + presentBronzeAnatEntity;
            result = prime * result + presentBronzeCond;
            result = prime * result + presentGoldAnatEntity;
            result = prime * result + presentGoldCond;
            result = prime * result + presentSilverAnatEntity;
            result = prime * result + presentSilverCond;
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
            GeneStatsBean other = (GeneStatsBean) obj;
            if (absentBronzeAnatEntity != other.absentBronzeAnatEntity) {
                return false;
            }
            if (absentBronzeCond != other.absentBronzeCond) {
                return false;
            }
            if (absentGoldAnatEntity != other.absentGoldAnatEntity) {
                return false;
            }
            if (absentGoldCond != other.absentGoldCond) {
                return false;
            }
            if (absentSilverAnatEntity != other.absentSilverAnatEntity) {
                return false;
            }
            if (absentSilverCond != other.absentSilverCond) {
                return false;
            }
            if (bioTypeName == null) {
                if (other.bioTypeName != null) {
                    return false;
                }
            } else if (!bioTypeName.equals(other.bioTypeName)) {
                return false;
            }
            if (filteredGenePagePresentAnatEntity != other.filteredGenePagePresentAnatEntity) {
                return false;
            }
            if (geneId == null) {
                if (other.geneId != null) {
                    return false;
                }
            } else if (!geneId.equals(other.geneId)) {
                return false;
            }
            if (geneName == null) {
                if (other.geneName != null) {
                    return false;
                }
            } else if (!geneName.equals(other.geneName)) {
                return false;
            }
            if (maxRank == null) {
                if (other.maxRank != null) {
                    return false;
                }
            } else if (!maxRank.equals(other.maxRank)) {
                return false;
            }
            if (minRank == null) {
                if (other.minRank != null) {
                    return false;
                }
            } else if (!minRank.equals(other.minRank)) {
                return false;
            }
            if (minRankAnatEntity == null) {
                if (other.minRankAnatEntity != null) {
                    return false;
                }
            } else if (!minRankAnatEntity.equals(other.minRankAnatEntity)) {
                return false;
            }
            if (presentBronzeAnatEntity != other.presentBronzeAnatEntity) {
                return false;
            }
            if (presentBronzeCond != other.presentBronzeCond) {
                return false;
            }
            if (presentGoldAnatEntity != other.presentGoldAnatEntity) {
                return false;
            }
            if (presentGoldCond != other.presentGoldCond) {
                return false;
            }
            if (presentSilverAnatEntity != other.presentSilverAnatEntity) {
                return false;
            }
            if (presentSilverCond != other.presentSilverCond) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GeneStatsBean [geneId=").append(geneId).append(", geneName=").append(geneName)
                    .append(", bioTypeName=").append(bioTypeName).append(", presentBronzeAnatEntity=")
                    .append(presentBronzeAnatEntity).append(", presentSilverAnatEntity=")
                    .append(presentSilverAnatEntity).append(", presentGoldAnatEntity=").append(presentGoldAnatEntity)
                    .append(", absentBronzeAnatEntity=").append(absentBronzeAnatEntity)
                    .append(", absentSilverAnatEntity=").append(absentSilverAnatEntity)
                    .append(", absentGoldAnatEntity=").append(absentGoldAnatEntity).append(", presentBronzeCond=")
                    .append(presentBronzeCond).append(", presentSilverCond=").append(presentSilverCond)
                    .append(", presentGoldCond=").append(presentGoldCond).append(", absentBronzeCond=")
                    .append(absentBronzeCond).append(", absentSilverCond=").append(absentSilverCond)
                    .append(", absentGoldCond=").append(absentGoldCond).append(", filteredGenePagePresentAnatEntity=")
                    .append(filteredGenePagePresentAnatEntity).append(", minRank=").append(minRank).append(", maxRank=")
                    .append(maxRank).append(", minRankAnatEntity=").append(minRankAnatEntity).append("]");
            return builder.toString();
        }
    }

    //NOTE: for each biotype of number of genes, number of genes with data,
    //number of genes with PRESENT condition calls (any quality), number with ABSENT condition calls (any quality),
    //total number of condition calls PRESENT * each quality, condition calls ABSENT * each quality.
    public static class BiotypeStatsBean {

        private String bioTypeName;
        private int geneCount;
        //number of genes with data
        private int geneWithData; 
        //number of genes with PRESENT condition calls (any quality)
        private int presentCondGene;
        //number with ABSENT condition calls (any quality)
        private int absentCondGene;

        //total number of anat. entity calls PRESENT * each quality,
        private int presentBronzeAnatEntityCalls;
        private int presentSilverAnatEntityCalls;
        private int presentGoldAnatEntityCalls;
        
        //total number of anat. entity calls ABSENT * each quality.
        private int absentBronzeAnatEntityCalls;
        private int absentSilverAnatEntityCalls;
        private int absentGoldAnatEntityCalls;
        
        //total number of condition calls PRESENT * each quality,
        private int presentBronzeCondCalls;
        private int presentSilverCondCalls;
        private int presentGoldCondCalls;
        
        //total number of condition calls ABSENT * each quality,
        private int absentBronzeCondCalls;
        private int absentSilverCondCalls;
        private int absentGoldCondCalls;

        // FIXME add ranks?

        public BiotypeStatsBean() {
            bioTypeName = null;
            geneCount = 0;
            geneWithData = 0;
            presentCondGene = 0;
            absentCondGene = 0;
            presentBronzeAnatEntityCalls = 0;
            presentSilverAnatEntityCalls = 0;
            presentGoldAnatEntityCalls = 0;
            absentBronzeAnatEntityCalls = 0;
            absentSilverAnatEntityCalls = 0;
            absentGoldAnatEntityCalls = 0;
            presentBronzeCondCalls = 0;
            presentSilverCondCalls = 0;
            presentGoldCondCalls = 0;
            absentBronzeCondCalls = 0;
            absentSilverCondCalls = 0;
            absentGoldCondCalls = 0;
        }

        public String getBioTypeName() {
            return bioTypeName;
        }
        public void setBioTypeName(String bioTypeName) {
            this.bioTypeName = bioTypeName;
        }

        public int getGeneCount() {
            return geneCount;
        }
        public void setGeneCount(int geneCount) {
            this.geneCount = geneCount;
        }

        public int getGeneWithData() {
            return geneWithData;
        }
        public void setGeneWithData(int geneWithData) {
            this.geneWithData = geneWithData;
        }

        public int getPresentCondGene() {
            return presentCondGene;
        }
        public void setPresentCondGene(int presentCondGene) {
            this.presentCondGene = presentCondGene;
        }

        public int getAbsentCondGene() {
            return absentCondGene;
        }
        public void setAbsentCondGene(int absentCondGene) {
            this.absentCondGene = absentCondGene;
        }

        public int getPresentBronzeAnatEntityCalls() {
            return presentBronzeAnatEntityCalls;
        }
        public void setPresentBronzeAnatEntityCalls(int presentBronzeAnatEntityCalls) {
            this.presentBronzeAnatEntityCalls = presentBronzeAnatEntityCalls;
        }

        public int getPresentSilverAnatEntityCalls() {
            return presentSilverAnatEntityCalls;
        }
        public void setPresentSilverAnatEntityCalls(int presentSilverAnatEntityCalls) {
            this.presentSilverAnatEntityCalls = presentSilverAnatEntityCalls;
        }

        public int getPresentGoldAnatEntityCalls() {
            return presentGoldAnatEntityCalls;
        }
        public void setPresentGoldAnatEntityCalls(int presentGoldAnatEntityCalls) {
            this.presentGoldAnatEntityCalls = presentGoldAnatEntityCalls;
        }

        public int getAbsentBronzeAnatEntityCalls() {
            return absentBronzeAnatEntityCalls;
        }
        public void setAbsentBronzeAnatEntityCalls(int absentBronzeAnatEntityCalls) {
            this.absentBronzeAnatEntityCalls = absentBronzeAnatEntityCalls;
        }

        public int getAbsentSilverAnatEntityCalls() {
            return absentSilverAnatEntityCalls;
        }
        public void setAbsentSilverAnatEntityCalls(int absentSilverAnatEntityCalls) {
            this.absentSilverAnatEntityCalls = absentSilverAnatEntityCalls;
        }

        public int getAbsentGoldAnatEntityCalls() {
            return absentGoldAnatEntityCalls;
        }
        public void setAbsentGoldAnatEntityCalls(int absentGoldAnatEntityCalls) {
            this.absentGoldAnatEntityCalls = absentGoldAnatEntityCalls;
        }

        public int getPresentBronzeCondCalls() {
            return presentBronzeCondCalls;
        }
        public void setPresentBronzeCondCalls(int presentBronzeCondCalls) {
            this.presentBronzeCondCalls = presentBronzeCondCalls;
        }

        public int getPresentSilverCondCalls() {
            return presentSilverCondCalls;
        }
        public void setPresentSilverCondCalls(int presentSilverCondCalls) {
            this.presentSilverCondCalls = presentSilverCondCalls;
        }

        public int getPresentGoldCondCalls() {
            return presentGoldCondCalls;
        }
        public void setPresentGoldCondCalls(int presentGoldCondCalls) {
            this.presentGoldCondCalls = presentGoldCondCalls;
        }

        public int getAbsentBronzeCondCalls() {
            return absentBronzeCondCalls;
        }
        public void setAbsentBronzeCondCalls(int absentBronzeCondCalls) {
            this.absentBronzeCondCalls = absentBronzeCondCalls;
        }

        public int getAbsentSilverCondCalls() {
            return absentSilverCondCalls;
        }
        public void setAbsentSilverCondCalls(int absentSilverCondCalls) {
            this.absentSilverCondCalls = absentSilverCondCalls;
        }

        public int getAbsentGoldCondCalls() {
            return absentGoldCondCalls;
        }
        public void setAbsentGoldCondCalls(int absentGoldCondCalls) {
            this.absentGoldCondCalls = absentGoldCondCalls;
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
            BiotypeStatsBean other = (BiotypeStatsBean) obj;

            if (bioTypeName == null) {
                if (other.bioTypeName != null) {
                    return false;
                }
            } else if (!bioTypeName.equals(other.bioTypeName)) {
                return false;
            }
            if (geneCount != other.geneCount) {
                return false;
            }
            if (geneWithData != other.geneWithData) {
                return false;
            }
            if (presentCondGene != other.presentCondGene) {
                return false;
            }
            if (absentCondGene != other.absentCondGene) {
                return false;
            }
            if (presentBronzeAnatEntityCalls != other.presentBronzeAnatEntityCalls) {
                return false;
            }
            if (presentSilverAnatEntityCalls != other.presentSilverAnatEntityCalls) {
                return false;
            }
            if (presentGoldAnatEntityCalls != other.presentGoldAnatEntityCalls) {
                return false;
            }
            if (absentBronzeAnatEntityCalls != other.absentBronzeAnatEntityCalls) {
                return false;
            }
            if (absentSilverAnatEntityCalls != other.absentSilverAnatEntityCalls) {
                return false;
            }
            if (absentGoldAnatEntityCalls != other.absentGoldAnatEntityCalls) {
                return false;
            }
            if (presentBronzeCondCalls != other.presentBronzeCondCalls) {
                return false;
            }
            if (presentSilverCondCalls != other.presentSilverCondCalls) {
                return false;
            }
            if (presentGoldCondCalls != other.presentGoldCondCalls) {
                return false;
            }
            if (absentBronzeCondCalls != other.absentBronzeCondCalls) {
                return false;
            }
            if (absentSilverCondCalls != other.absentSilverCondCalls) {
                return false;
            }
            if (absentGoldCondCalls != other.absentGoldCondCalls) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((bioTypeName == null) ? 0 : bioTypeName.hashCode());
            result = prime * result + geneCount;
            result = prime * result + geneWithData;
            result = prime * result + presentCondGene;
            result = prime * result + absentCondGene;
            result = prime * result + presentBronzeAnatEntityCalls;
            result = prime * result + presentSilverAnatEntityCalls;
            result = prime * result + presentGoldAnatEntityCalls;
            result = prime * result + absentBronzeAnatEntityCalls;
            result = prime * result + absentSilverAnatEntityCalls;
            result = prime * result + absentGoldAnatEntityCalls;
            result = prime * result + presentBronzeCondCalls;
            result = prime * result + presentSilverCondCalls;
            result = prime * result + presentGoldCondCalls;
            result = prime * result + absentBronzeCondCalls;
            result = prime * result + absentSilverCondCalls;
            result = prime * result + absentGoldCondCalls;
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("BiotypeStatsBean [");
            sb.append("bioTypeName='").append(bioTypeName).append('\'');
            sb.append(", geneCount=").append(geneCount);
            sb.append(", geneWithData=").append(geneWithData);
            sb.append(", presentCondGene=").append(presentCondGene);
            sb.append(", absentCondGene=").append(absentCondGene);
            sb.append(", presentBronzeAnatEntityCalls=").append(presentBronzeAnatEntityCalls);
            sb.append(", presentSilverAnatEntityCalls=").append(presentSilverAnatEntityCalls);
            sb.append(", presentGoldAnatEntityCalls=").append(presentGoldAnatEntityCalls);
            sb.append(", absentBronzeAnatEntityCalls=").append(absentBronzeAnatEntityCalls);
            sb.append(", absentSilverAnatEntityCalls=").append(absentSilverAnatEntityCalls);
            sb.append(", absentGoldAnatEntityCalls=").append(absentGoldAnatEntityCalls);
            sb.append(", presentBronzeCondCalls=").append(presentBronzeCondCalls);
            sb.append(", presentSilverCondCalls=").append(presentSilverCondCalls);
            sb.append(", presentGoldCondCalls=").append(presentGoldCondCalls);
            sb.append(", absentBronzeCondCalls=").append(absentBronzeCondCalls);
            sb.append(", absentSilverCondCalls=").append(absentSilverCondCalls);
            sb.append(", absentGoldCondCalls=").append(absentGoldCondCalls);
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;

    /**
     * Default constructor. 
     */
    public GenerateInsertGeneStats() {
        this(ServiceFactory::new);
    }
    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
     *                                      to be able to provide one to each thread.
     */
    public GenerateInsertGeneStats(Supplier<ServiceFactory> serviceFactorySupplier) {
        this.serviceFactorySupplier = serviceFactorySupplier;
    }

    /**
     * Main method to generate stats files. Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>path where to store the files
     * <li>suffix of the file names to store stats per biotype. Prefix will be the species name.
     * <li>suffix of the file names to store stats per gene. Prefix will be the species name.
     * </ol>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If the arguments are incorrect.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length != 3) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments."));
        }

        GenerateInsertGeneStats statsGenerator = new GenerateInsertGeneStats();
        statsGenerator.generate(args[0], args[1], args[2]);
        
        log.exit();
    }

    /**
     * Generate statistics about expression data, with one file for stats about gene biotypes per species,
     * and one file for stats about genes per species.
     *
     * @param path                      A {@code String} that is the path where to store the files.
     * @param bioTypeStatsFileSuffix    A {@code String} that is the suffix of the file names to store stats
     *                                  per biotype. Prefix will be the species name.
     * @param geneStatsFileSuffix       A {@code String} that is the suffix of the file names to store stats
     *                                  per gene. Prefix will be the species name.
     */
    public void generate(String path, String bioTypeStatsFileSuffix, String geneStatsFileSuffix) {
        log.entry(path, bioTypeStatsFileSuffix, geneStatsFileSuffix);

        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        Set<Species> allSpecies = serviceFactory.getSpeciesService().loadSpeciesByIds(null, false)
                .stream().collect(Collectors.toSet());
        //launch the computation for each species independently
        for (Species species: allSpecies) {
            //Retrieve all genes for that species
            Set<Gene> genes = serviceFactory.getGeneService().loadGenes(new GeneFilter(species.getId()))
                    .collect(Collectors.toSet());

            this.generatePerSpecies(species, genes);
        }
        
        log.exit();
    }

    private void generatePerSpecies(Species species, Set<Gene> genes) {
        log.entry(species, genes);

        //Now we use parallel streams for each gene independently
        List<GeneStatsBean> orderedBeans = genes.parallelStream().map(gene -> {
            //We need one ServiceFactory per thread
            ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
            CallService callService = serviceFactory.getCallService();

            //For each gene, we retrieve: "present" expression calls (min qual bronze) per anat. entity, and per condition,
            //and "absent" expression calls (min qual bronze) per anat. entity, and per condition.
            //Plus also the number of anat. entities with expression filtered as on the gene page
            GeneStatsBean bean = new GeneStatsBean();
            bean.setGeneName(gene.getName());
            bean.setGeneId(gene.getEnsemblGeneId());
            bean.setBioTypeName(gene.getGeneBioType().getName());

            GeneFilter geneFilter = new GeneFilter(gene.getSpecies().getId(), gene.getEnsemblGeneId());
            Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
            obsDataFilter.put(null, true);

            Set<ExpressionCall> organCalls = callService
                    .loadExpressionCalls(
                            new ExpressionCallFilter(null,
                                    Collections.singleton(geneFilter),
                                    null, null, obsDataFilter, null, null),
                            EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID,
                                    CallService.Attribute.DATA_QUALITY),
                            null)
                    .collect(Collectors.toSet());

            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
                    new LinkedHashMap<>();
            serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
            EnumSet<CallService.Attribute> attrs = Arrays.stream(CallService.Attribute.values())
                    .filter(a -> a.isConditionParameter())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class)));
            attrs.add(CallService.Attribute.DATA_QUALITY);
            attrs.add(CallService.Attribute.GLOBAL_MEAN_RANK);
            List<ExpressionCall> conditionCalls = callService
                    .loadExpressionCalls(
                            new ExpressionCallFilter(null,
                                    Collections.singleton(geneFilter),
                                    null, null, obsDataFilter, null, null),
                            attrs,
                            serviceOrdering)
                    .collect(Collectors.toList());

            for (ExpressionCall call: organCalls) {
                if (call.getSummaryCallType().equals(ExpressionSummary.EXPRESSED)) {
                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setPresentBronzeAnatEntity(bean.getPresentBronzeAnatEntity() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setPresentSilverAnatEntity(bean.getPresentSilverAnatEntity() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setPresentGoldAnatEntity(bean.getPresentGoldAnatEntity() + 1);
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else if (call.getSummaryCallType().equals(ExpressionSummary.NOT_EXPRESSED)) {
                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setAbsentBronzeAnatEntity(bean.getAbsentBronzeAnatEntity() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setAbsentSilverAnatEntity(bean.getAbsentSilverAnatEntity() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setAbsentGoldAnatEntity(bean.getAbsentGoldAnatEntity() + 1);
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else {
                    throw log.throwing(new IllegalStateException(
                            "Unsupported SummaryCallType: " + call.getSummaryCallType()));
                }
            }
            for (ExpressionCall call: conditionCalls) {
                if (call.getSummaryCallType().equals(ExpressionSummary.EXPRESSED)) {
                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setPresentBronzeCond(bean.getPresentBronzeCond() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setPresentSilverCond(bean.getPresentSilverCond() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setPresentGoldCond(bean.getPresentGoldCond() + 1);
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else if (call.getSummaryCallType().equals(ExpressionSummary.NOT_EXPRESSED)) {
                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setAbsentBronzeCond(bean.getAbsentBronzeCond() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setAbsentSilverCond(bean.getAbsentSilverCond() + 1);
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setAbsentGoldCond(bean.getAbsentGoldCond() + 1);
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else {
                    throw log.throwing(new IllegalStateException(
                            "Unsupported SummaryCallType: " + call.getSummaryCallType()));
                }
            }

            //TODO: manage in all this method cases where there is no calls
            //TODO generate GeneBioTypeStatsBeans
            //TODO insert the GeneBioTypeStatsTO into database
            //TODO generate files for biotypes
            //TODO generate files for genes
            bean.setFilteredGenePagePresentAnatEntity(callService
                    .loadCondCallsWithSilverAnatEntityCallsByAnatEntity(organCalls, conditionCalls, false)
                    .size());
            ExpressionCall firstCall = conditionCalls.iterator().next();
            bean.setMinRank(firstCall.getGlobalMeanRank());
            bean.setMinRankAnatEntity(firstCall.getCondition().getAnatEntity());
            bean.setMaxRank(conditionCalls.listIterator().previous().getGlobalMeanRank());

            return bean;
        })
        .sorted(Comparator.comparing(bean -> bean.getGeneId()))
        .collect(Collectors.toList());
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate 
     * the header of a gene stat TSV file.
     *
     * @return  The {@code Array} of {@code String}s used to produce the header.
     */
    private String[] generateFileHeader() {
        log.entry();
        
        // We use an index to avoid to change hard-coded column numbers when we change columns 
        int idx = 0;

        String[] headers = new String[19];
        headers[idx++] = GENE_ID_COLUMN_NAME;
        headers[idx++] = GENE_NAME_COLUMN_NAME;
        headers[idx++] = BIO_TYPE_NAME_COLUMN_NAME;
        headers[idx++] = PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = PRESENT_BRONZE_COND_COLUMN_NAME;
        headers[idx++] = PRESENT_SILVER_COND_COLUMN_NAME;
        headers[idx++] = PRESENT_GOLD_COND_COLUMN_NAME;
        headers[idx++] = ABSENT_BRONZE_COND_COLUMN_NAME;
        headers[idx++] = ABSENT_SILVER_COND_COLUMN_NAME;
        headers[idx++] = ABSENT_GOLD_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = MIN_RANK_COLUMN_NAME;
        headers[idx++] = MAX_RANK_COLUMN_NAME;
        headers[idx++] = MIN_RANK_ANAT_ENTITY_COLUMN_NAME;

        return log.exit(headers);
    }

    /**
     * Generate the field mapping for each column of the header of a gene stat TSV file.
     *
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a gene stat file.
     * @return          The {@code Array} of {@code String}s that is the field mapping, 
     *                  put in the {@code Array} at the same index as the column they 
     *                  are supposed to process.
     * @throws IllegalArgumentException If a {@code String} in {@code header} is not recognized.
     */
    private String[] generateFieldMapping(String[] header) throws IllegalArgumentException {
        log.entry((Object[]) header);

        //to do a sanity check on species columns in simple files

        String[] mapping = new String[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
                case GENE_ID_COLUMN_NAME:
                    mapping[i] = "geneId";
                    break;
                case GENE_NAME_COLUMN_NAME:
                    mapping[i] = "geneName";
                    break;
                case BIO_TYPE_NAME_COLUMN_NAME:
                    mapping[i] = "bioTypeName";
                    break;
                case PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "presentBronzeAnatEntity";
                    break;
                case PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "presentSilverAnatEntity";
                    break;
                case PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "presentGoldAnatEntity";
                    break;
                case ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "absentBronzeAnatEntity";
                    break;
                case ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "absentSilverAnatEntity";
                    break;
                case ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "absentGoldAnatEntity";
                    break;
                case PRESENT_BRONZE_COND_COLUMN_NAME:
                    mapping[i] = "presentBronzeCond";
                    break;
                case PRESENT_SILVER_COND_COLUMN_NAME:
                    mapping[i] = "presentSilverCond";
                    break;
                case PRESENT_GOLD_COND_COLUMN_NAME:
                    mapping[i] = "presentGoldCond";
                    break;
                case ABSENT_BRONZE_COND_COLUMN_NAME:
                    mapping[i] = "absentBronzeCond";
                    break;
                case ABSENT_SILVER_COND_COLUMN_NAME:
                    mapping[i] = "absentSilverCond";
                    break;
                case ABSENT_GOLD_COND_COLUMN_NAME:
                    mapping[i] = "absentGoldCond";
                    break;
                case FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredGenePagePresentAnatEntity";
                    break;
                case MIN_RANK_COLUMN_NAME:
                    mapping[i] = "minRank";
                    break;
                case MAX_RANK_COLUMN_NAME:
                    mapping[i] = "maxRank";
                    break;
                case MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "minRankAnatEntity";
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + header[i] + " for gene stat file."));
            }
        }
        return log.exit(mapping);
    }
    
    /**
     * Generate {@code Array} of {@code booleans} (one per TSV column) indicating 
     * whether each column should be quoted or not.
     *
     * @param header   An {@code Array} of {@code String}s representing the names of the columns.
     * @return          The {@code Array} of {@code booleans} (one per TSV column) indicating 
     *                  whether each column should be quoted or not.
     */
    private boolean[] generateQuoteMode(String[] header) {
        log.entry((Object[]) header);
        
        boolean[] quoteMode = new boolean[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
                case GENE_ID_COLUMN_NAME:
                case PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_BRONZE_COND_COLUMN_NAME:
                case PRESENT_SILVER_COND_COLUMN_NAME:
                case PRESENT_GOLD_COND_COLUMN_NAME:
                case ABSENT_BRONZE_COND_COLUMN_NAME:
                case ABSENT_SILVER_COND_COLUMN_NAME:
                case ABSENT_GOLD_COND_COLUMN_NAME:
                case FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME:
                case MIN_RANK_COLUMN_NAME:
                case MAX_RANK_COLUMN_NAME:
                    quoteMode[i] = false;
                    break;
                case GENE_NAME_COLUMN_NAME:
                case BIO_TYPE_NAME_COLUMN_NAME:
                case MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                    quoteMode[i] = true;
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + header[i] + " for gene stat file."));
            }
        }
        return log.exit(quoteMode);
    }

    private void writeRows(ICsvDozerBeanWriter writer, CellProcessor[] processors, String[] headers,
                           List<GeneStatsBean> beans) {
        log.entry(writer, processors, headers);
        
        beans.forEach(c -> {
            try {
                writer.write(beans, processors);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        log.exit();
    }
}