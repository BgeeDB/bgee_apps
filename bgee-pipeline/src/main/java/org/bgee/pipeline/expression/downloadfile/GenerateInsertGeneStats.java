package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.species.Species;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;

/**
 * Class used to generate statistics about expression data of genes and gene biotypes.
 *
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Oct. 2018
 * @since   Bgee 14 Sep. 2018
 */
public class GenerateInsertGeneStats extends MySQLDAOUser {

    private final static Logger log = LogManager.getLogger(GenerateInsertGeneStats.class.getName());

    /**
     * A {@code String} that is the extension of download files to be generated.
     */
    private final static String EXTENSION = ".tsv";

    /**
     * A {@code String} that is the name of the column containing gene IDs, in the download file.
     */
    public final static String GENE_ID_COLUMN_NAME = "Gene ID";
    public final static String GENE_NAME_COLUMN_NAME = "Gene name";
    public final static String BIO_TYPE_NAME_COLUMN_NAME = "Biotype";
    public final static String PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME = "Count of \"present\" expression calls per anat. entity, " +
            "bronze quality";
    public final static String PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME = "Count of \"present\" expression calls per anat. entity, " +
            "silver quality";
    public final static String PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME = "Count of \"present\" expression calls per anat. entity, " +
            "gold quality";
    public final static String ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME = "Count of \"absent\" expression calls per anat. entity, " +
            "bronze quality";
    public final static String ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME = "Count of \"absent\" expression calls per anat. entity, " +
            "silver quality";
    public final static String ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME = "Count of \"absent\" expression calls per anat. entity, " +
            "gold quality";
    public final static String FILTERED_PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME = "Count of \"present\" expression calls per anat. entity " +
            "as filtered in the download files, bronze quality";
    public final static String FILTERED_PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME = "Count of \"present\" expression calls per anat. entity " +
            "as filtered in the download files, silver quality";
    public final static String FILTERED_PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME = "Count of \"present\" expression calls per anat. entity " +
            "as filtered in the download files, gold quality";
    public final static String FILTERED_ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME = "Count of \"absent\" expression calls per anat. entity " +
            "as filtered in the download files, bronze quality";
    public final static String FILTERED_ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME = "Count of \"absent\" expression calls per anat. entity " +
            "as filtered in the download files, silver quality";
    public final static String FILTERED_ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME = "Count of \"absent\" expression calls per anat. entity " +
            "as filtered in the download files, gold quality";
    public final static String PRESENT_BRONZE_COND_COLUMN_NAME = "Count of \"present\" expression calls per condition, " +
            "bronze quality";
    public final static String PRESENT_SILVER_COND_COLUMN_NAME = "Count of \"present\" expression calls per condition, " +
            "silver quality";
    public final static String PRESENT_GOLD_COND_COLUMN_NAME = "Count of \"present\" expression calls per condition, " +
            "gold quality";
    public final static String ABSENT_BRONZE_COND_COLUMN_NAME = "Count of \"absent\" expression calls per condition, " +
            "bronze quality";
    public final static String ABSENT_SILVER_COND_COLUMN_NAME = "Count of \"absent\" expression calls per condition, " +
            "silver quality";
    public final static String ABSENT_GOLD_COND_COLUMN_NAME = "Count of \"absent\" expression calls per condition, " +
            "gold quality";
    public final static String FILTERED_PRESENT_BRONZE_COND_COLUMN_NAME = "Count of \"present\" expression calls per condition " +
            "as filtered in the download files, bronze quality";
    public final static String FILTERED_PRESENT_SILVER_COND_COLUMN_NAME = "Count of \"present\" expression calls per condition " +
            "as filtered in the download files, silver quality";
    public final static String FILTERED_PRESENT_GOLD_COND_COLUMN_NAME = "Count of \"present\" expression calls per condition " +
            "as filtered in the download files, gold quality";
    public final static String FILTERED_ABSENT_BRONZE_COND_COLUMN_NAME = "Count of \"absent\" expression calls per condition " +
            "as filtered in the download files, bronze quality";
    public final static String FILTERED_ABSENT_SILVER_COND_COLUMN_NAME = "Count of \"absent\" expression calls per condition " +
            "as filtered in the download files, silver quality";
    public final static String FILTERED_ABSENT_GOLD_COND_COLUMN_NAME = "Count of \"absent\" expression calls per condition " +
            "as filtered in the download files, gold quality";

    public final static String FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME =
            "Count of distinct anat. entity with \"present\" expression calls of gold and silver qualities filtered as on the Bgee website";
    public final static String MIN_RANK_COLUMN_NAME = "Minimum rank for this gene over all conditions";
    public final static String MAX_RANK_COLUMN_NAME = "Maximum rank for this gene over all condition";
    public final static String MIN_RANK_ANAT_ENTITY_COLUMN_NAME = "Anat. entity with the minimum rank over all conditions";
    public final static String FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME =
            "Anat. entity with the minimum rank, filtered as on the Bgee website";
    public final static String FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME =
            "Minimum rank over anat. entities, filtered as on the Bgee website";
    public final static String FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME =
            "Maximum rank over anat. entities, filtered as on the Bgee website";


    public final static String GENE_COUNT_COLUMN_NAME = "Number of genes";
    public final static String GENE_WITH_DATA_COLUMN_NAME = "Number of genes with data";
    public final static String GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME = "Number of genes with data of silver or gold qualities";
    public final static String PRESENT_COND_GENE_COLUMN_NAME = "Number of genes with \"present\" expression calls";
    public final static String ABSENT_COND_GENE_COLUMN_NAME = "Number of genes with \"absent\" expression calls";
    public final static String FILTERED_GENE_PAGE_GENE_COLUMN_NAME =
            "Number of genes with \"present\" expression calls of silver and gold qualities filtered as on the Bgee website";

    public final static String ANAT_ENTITY_WITH_DATA_COLUMN_NAME = "Number of distinct anat. entities with data";
    public final static String COND_WITH_DATA_COLUMN_NAME = "Number of distinct conditions with data";
    public final static String PRESENT_ANAT_ENTITY_COLUMN_NAME =
            "Number of distinct anat. entities with \"present\" expression calls";
    public final static String PRESENT_COND_COLUMN_NAME =
            "Number of distinct conditions with \"present\" expression calls";
    public final static String ABSENT_ANAT_ENTITY_COLUMN_NAME =
            "Number of distinct anat. entities with \"absent\" expression calls";
    public final static String ABSENT_COND_COLUMN_NAME =
            "Number of distinct conditions with \"absent\" expression calls";

    public enum StatFileType {
        GENE_STATS, BIO_TYPE_STATS;
    }

    public static abstract class CommonBean {
        
        private String bioTypeName;
        private int presentBronzeAnatEntity;
        private int presentSilverAnatEntity;
        private int presentGoldAnatEntity;
        private int absentBronzeAnatEntity;
        private int absentSilverAnatEntity;
        private int absentGoldAnatEntity;
        private int filteredPresentBronzeAnatEntity;
        private int filteredPresentSilverAnatEntity;
        private int filteredPresentGoldAnatEntity;
        private int filteredAbsentBronzeAnatEntity;
        private int filteredAbsentSilverAnatEntity;
        private int filteredAbsentGoldAnatEntity;
        private int presentBronzeCond;
        private int presentSilverCond;
        private int presentGoldCond;
        private int absentBronzeCond;
        private int absentSilverCond;
        private int absentGoldCond;
        private int filteredPresentBronzeCond;
        private int filteredPresentSilverCond;
        private int filteredPresentGoldCond;
        private int filteredAbsentBronzeCond;
        private int filteredAbsentSilverCond;
        private int filteredAbsentGoldCond;
        private BigDecimal minRank;
        private BigDecimal maxRank;
        private String formattedMinRank;
        private String formattedMaxRank;
        private int filteredGenePagePresentAnatEntity;

        public CommonBean() {
            this.bioTypeName = null;
            this.presentBronzeAnatEntity = 0;
            this.presentSilverAnatEntity = 0;
            this.presentGoldAnatEntity = 0;
            this.absentBronzeAnatEntity = 0;
            this.absentSilverAnatEntity = 0;
            this.absentGoldAnatEntity = 0;
            this.filteredPresentBronzeAnatEntity = 0;
            this.filteredPresentSilverAnatEntity = 0;
            this.filteredPresentGoldAnatEntity = 0;
            this.filteredAbsentBronzeAnatEntity = 0;
            this.filteredAbsentSilverAnatEntity = 0;
            this.filteredAbsentGoldAnatEntity = 0;
            this.presentBronzeCond = 0;
            this.presentSilverCond = 0;
            this.presentGoldCond = 0;
            this.absentBronzeCond = 0;
            this.absentSilverCond = 0;
            this.absentGoldCond = 0;
            this.filteredPresentBronzeCond = 0;
            this.filteredPresentSilverCond = 0;
            this.filteredPresentGoldCond = 0;
            this.filteredAbsentBronzeCond = 0;
            this.filteredAbsentSilverCond = 0;
            this.filteredAbsentGoldCond = 0;
            this.minRank = null;
            this.maxRank = null;
            this.formattedMinRank = null;
            this.formattedMaxRank = null;
            this.filteredGenePagePresentAnatEntity = 0;
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
        public int getFilteredPresentBronzeAnatEntity() {
            return filteredPresentBronzeAnatEntity;
        }
        public void setFilteredPresentBronzeAnatEntity(int filteredPresentBronzeAnatEntity) {
            this.filteredPresentBronzeAnatEntity = filteredPresentBronzeAnatEntity;
        }
        public int getFilteredPresentSilverAnatEntity() {
            return filteredPresentSilverAnatEntity;
        }
        public void setFilteredPresentSilverAnatEntity(int filteredPresentSilverAnatEntity) {
            this.filteredPresentSilverAnatEntity = filteredPresentSilverAnatEntity;
        }
        public int getFilteredPresentGoldAnatEntity() {
            return filteredPresentGoldAnatEntity;
        }
        public void setFilteredPresentGoldAnatEntity(int filteredPresentGoldAnatEntity) {
            this.filteredPresentGoldAnatEntity = filteredPresentGoldAnatEntity;
        }
        public int getFilteredAbsentBronzeAnatEntity() {
            return filteredAbsentBronzeAnatEntity;
        }
        public void setFilteredAbsentBronzeAnatEntity(int filteredAbsentBronzeAnatEntity) {
            this.filteredAbsentBronzeAnatEntity = filteredAbsentBronzeAnatEntity;
        }
        public int getFilteredAbsentSilverAnatEntity() {
            return filteredAbsentSilverAnatEntity;
        }
        public void setFilteredAbsentSilverAnatEntity(int filteredAbsentSilverAnatEntity) {
            this.filteredAbsentSilverAnatEntity = filteredAbsentSilverAnatEntity;
        }
        public int getFilteredAbsentGoldAnatEntity() {
            return filteredAbsentGoldAnatEntity;
        }
        public void setFilteredAbsentGoldAnatEntity(int filteredAbsentGoldAnatEntity) {
            this.filteredAbsentGoldAnatEntity = filteredAbsentGoldAnatEntity;
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
        public int getFilteredPresentBronzeCond() {
            return filteredPresentBronzeCond;
        }
        public void setFilteredPresentBronzeCond(int filteredPresentBronzeCond) {
            this.filteredPresentBronzeCond = filteredPresentBronzeCond;
        }
        public int getFilteredPresentSilverCond() {
            return filteredPresentSilverCond;
        }
        public void setFilteredPresentSilverCond(int filteredPresentSilverCond) {
            this.filteredPresentSilverCond = filteredPresentSilverCond;
        }
        public int getFilteredPresentGoldCond() {
            return filteredPresentGoldCond;
        }
        public void setFilteredPresentGoldCond(int filteredPresentGoldCond) {
            this.filteredPresentGoldCond = filteredPresentGoldCond;
        }
        public int getFilteredAbsentBronzeCond() {
            return filteredAbsentBronzeCond;
        }
        public void setFilteredAbsentBronzeCond(int filteredAbsentBronzeCond) {
            this.filteredAbsentBronzeCond = filteredAbsentBronzeCond;
        }
        public int getFilteredAbsentSilverCond() {
            return filteredAbsentSilverCond;
        }
        public void setFilteredAbsentSilverCond(int filteredAbsentSilverCond) {
            this.filteredAbsentSilverCond = filteredAbsentSilverCond;
        }
        public int getFilteredAbsentGoldCond() {
            return filteredAbsentGoldCond;
        }
        public void setFilteredAbsentGoldCond(int filteredAbsentGoldCond) {
            this.filteredAbsentGoldCond = filteredAbsentGoldCond;
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
        public String getFormattedMinRank() {
            return formattedMinRank;
        }
        public void setFormattedMinRank(String formattedMinRank) {
            this.formattedMinRank = formattedMinRank;
        }
        public String getFormattedMaxRank() {
            return formattedMaxRank;
        }
        public void setFormattedMaxRank(String formattedMaxRank) {
            this.formattedMaxRank = formattedMaxRank;
        }
        public int getFilteredGenePagePresentAnatEntity() {
            return filteredGenePagePresentAnatEntity;
        }
        public void setFilteredGenePagePresentAnatEntity(int filteredGenePagePresentAnatEntity) {
            this.filteredGenePagePresentAnatEntity = filteredGenePagePresentAnatEntity;
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
            result = prime * result + filteredAbsentBronzeAnatEntity;
            result = prime * result + filteredAbsentBronzeCond;
            result = prime * result + filteredAbsentGoldAnatEntity;
            result = prime * result + filteredAbsentGoldCond;
            result = prime * result + filteredAbsentSilverAnatEntity;
            result = prime * result + filteredAbsentSilverCond;
            result = prime * result + filteredGenePagePresentAnatEntity;
            result = prime * result + filteredPresentBronzeAnatEntity;
            result = prime * result + filteredPresentBronzeCond;
            result = prime * result + filteredPresentGoldAnatEntity;
            result = prime * result + filteredPresentGoldCond;
            result = prime * result + filteredPresentSilverAnatEntity;
            result = prime * result + filteredPresentSilverCond;
            result = prime * result + ((maxRank == null) ? 0 : maxRank.hashCode());
            result = prime * result + ((minRank == null) ? 0 : minRank.hashCode());
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
            if (!(obj instanceof CommonBean)) {
                return false;
            }
            CommonBean other = (CommonBean) obj;
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
            if (filteredAbsentBronzeAnatEntity != other.filteredAbsentBronzeAnatEntity) {
                return false;
            }
            if (filteredAbsentBronzeCond != other.filteredAbsentBronzeCond) {
                return false;
            }
            if (filteredAbsentGoldAnatEntity != other.filteredAbsentGoldAnatEntity) {
                return false;
            }
            if (filteredAbsentGoldCond != other.filteredAbsentGoldCond) {
                return false;
            }
            if (filteredAbsentSilverAnatEntity != other.filteredAbsentSilverAnatEntity) {
                return false;
            }
            if (filteredAbsentSilverCond != other.filteredAbsentSilverCond) {
                return false;
            }
            if (filteredGenePagePresentAnatEntity != other.filteredGenePagePresentAnatEntity) {
                return false;
            }
            if (filteredPresentBronzeAnatEntity != other.filteredPresentBronzeAnatEntity) {
                return false;
            }
            if (filteredPresentBronzeCond != other.filteredPresentBronzeCond) {
                return false;
            }
            if (filteredPresentGoldAnatEntity != other.filteredPresentGoldAnatEntity) {
                return false;
            }
            if (filteredPresentGoldCond != other.filteredPresentGoldCond) {
                return false;
            }
            if (filteredPresentSilverAnatEntity != other.filteredPresentSilverAnatEntity) {
                return false;
            }
            if (filteredPresentSilverCond != other.filteredPresentSilverCond) {
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
            builder.append("CommonBean [bioTypeName=").append(bioTypeName).append(", presentBronzeAnatEntity=")
                    .append(presentBronzeAnatEntity).append(", presentSilverAnatEntity=")
                    .append(presentSilverAnatEntity).append(", presentGoldAnatEntity=").append(presentGoldAnatEntity)
                    .append(", absentBronzeAnatEntity=").append(absentBronzeAnatEntity)
                    .append(", absentSilverAnatEntity=").append(absentSilverAnatEntity)
                    .append(", absentGoldAnatEntity=").append(absentGoldAnatEntity)
                    .append(", filteredPresentBronzeAnatEntity=").append(filteredPresentBronzeAnatEntity)
                    .append(", filteredPresentSilverAnatEntity=").append(filteredPresentSilverAnatEntity)
                    .append(", filteredPresentGoldAnatEntity=").append(filteredPresentGoldAnatEntity)
                    .append(", filteredAbsentBronzeAnatEntity=").append(filteredAbsentBronzeAnatEntity)
                    .append(", filteredAbsentSilverAnatEntity=").append(filteredAbsentSilverAnatEntity)
                    .append(", filteredAbsentGoldAnatEntity=").append(filteredAbsentGoldAnatEntity)
                    .append(", presentBronzeCond=").append(presentBronzeCond).append(", presentSilverCond=")
                    .append(presentSilverCond).append(", presentGoldCond=").append(presentGoldCond)
                    .append(", absentBronzeCond=").append(absentBronzeCond).append(", absentSilverCond=")
                    .append(absentSilverCond).append(", absentGoldCond=").append(absentGoldCond)
                    .append(", filteredPresentBronzeCond=").append(filteredPresentBronzeCond)
                    .append(", filteredPresentSilverCond=").append(filteredPresentSilverCond)
                    .append(", filteredPresentGoldCond=").append(filteredPresentGoldCond)
                    .append(", filteredAbsentBronzeCond=").append(filteredAbsentBronzeCond)
                    .append(", filteredAbsentSilverCond=").append(filteredAbsentSilverCond)
                    .append(", filteredAbsentGoldCond=").append(filteredAbsentGoldCond).append(", minRank=")
                    .append(minRank).append(", maxRank=").append(maxRank).append(", formattedMinRank=")
                    .append(formattedMinRank).append(", formattedMaxRank=").append(formattedMaxRank)
                    .append(", filteredGenePagePresentAnatEntity=").append(filteredGenePagePresentAnatEntity)
                    .append("]");
            return builder.toString();
        }
    }

    public static class GeneStatsBean extends CommonBean {
        
        private String geneId;
        private String geneName;
        private String minRankAnatEntity;
        private String filteredGenePageMinRankAnatEntity;
        private String filteredGenePageFormattedMinRank;
        private String filteredGenePageFormattedMaxRank;

        //Not used for writing in the gene TSV files, but to retrieve all unique Conditions and AnatEntities
        //for the information per biotype.
        private Set<AnatEntity> filteredGenePagePresentAnatEntities;
        private Set<AnatEntity> presentAnatEntities;
        private Set<Condition> presentConds;
        private Set<AnatEntity> absentAnatEntities;
        private Set<Condition> absentConds;

        public GeneStatsBean() {
            this.geneId = null;
            this.geneName = null;
            this.minRankAnatEntity = null;
            this.filteredGenePageMinRankAnatEntity = null;
            this.filteredGenePageFormattedMinRank = null;
            this.filteredGenePageFormattedMaxRank = null;
            this.filteredGenePagePresentAnatEntities = new HashSet<>();
            this.presentAnatEntities = new HashSet<>();
            this.presentConds = new HashSet<>();
            this.absentAnatEntities = new HashSet<>();
            this.absentConds = new HashSet<>();
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

        public String getMinRankAnatEntity() {
            return minRankAnatEntity;
        }
        public void setMinRankAnatEntity(String minRankAnatEntity) {
            this.minRankAnatEntity = minRankAnatEntity;
        }

        public String getFilteredGenePageMinRankAnatEntity() {
            return filteredGenePageMinRankAnatEntity;
        }
        public void setFilteredGenePageMinRankAnatEntity(String filteredGenePageMinRankAnatEntity) {
            this.filteredGenePageMinRankAnatEntity = filteredGenePageMinRankAnatEntity;
        }

        public String getFilteredGenePageFormattedMinRank() {
            return filteredGenePageFormattedMinRank;
        }
        public void setFilteredGenePageFormattedMinRank(String filteredGenePageFormattedMinRank) {
            this.filteredGenePageFormattedMinRank = filteredGenePageFormattedMinRank;
        }

        public String getFilteredGenePageFormattedMaxRank() {
            return filteredGenePageFormattedMaxRank;
        }
        public void setFilteredGenePageFormattedMaxRank(String filteredGenePageFormattedMaxRank) {
            this.filteredGenePageFormattedMaxRank = filteredGenePageFormattedMaxRank;
        }

        public Set<AnatEntity> getFilteredGenePagePresentAnatEntities() {
            return filteredGenePagePresentAnatEntities;
        }

        public Set<AnatEntity> getPresentAnatEntities() {
            return presentAnatEntities;
        }

        public Set<Condition> getPresentConds() {
            return presentConds;
        }

        public Set<AnatEntity> getAbsentAnatEntities() {
            return absentAnatEntities;
        }

        public Set<Condition> getAbsentConds() {
            return absentConds;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((geneName == null) ? 0 : geneName.hashCode());
            result = prime * result + ((minRankAnatEntity == null) ? 0 : minRankAnatEntity.hashCode());
            result = prime * result + ((filteredGenePageMinRankAnatEntity == null) ? 0 : filteredGenePageMinRankAnatEntity.hashCode());
            result = prime * result + ((filteredGenePageFormattedMinRank == null) ? 0 : filteredGenePageFormattedMinRank.hashCode());
            result = prime * result + ((filteredGenePageFormattedMaxRank == null) ? 0 : filteredGenePageFormattedMaxRank.hashCode());
            result = prime * result + ((filteredGenePagePresentAnatEntities == null) ? 0 : filteredGenePagePresentAnatEntities.hashCode());
            result = prime * result + ((presentAnatEntities == null) ? 0 : presentAnatEntities.hashCode());
            result = prime * result + ((presentConds == null) ? 0 : presentConds.hashCode());
            result = prime * result + ((absentAnatEntities == null) ? 0 : absentAnatEntities.hashCode());
            result = prime * result + ((absentConds == null) ? 0 : absentConds.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            
            GeneStatsBean other = (GeneStatsBean) obj;

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
            if (minRankAnatEntity == null) {
                if (other.minRankAnatEntity != null) {
                    return false;
                }
            } else if (!minRankAnatEntity.equals(other.minRankAnatEntity)) {
                return false;
            }
            if (filteredGenePageMinRankAnatEntity == null) {
                if (other.filteredGenePageMinRankAnatEntity != null) {
                    return false;
                }
            } else if (!filteredGenePageMinRankAnatEntity.equals(other.filteredGenePageMinRankAnatEntity)) {
                return false;
            }
            if (filteredGenePageFormattedMinRank == null) {
                if (other.filteredGenePageFormattedMinRank != null) {
                    return false;
                }
            } else if (!filteredGenePageFormattedMinRank.equals(other.filteredGenePageFormattedMinRank)) {
                return false;
            }
            if (filteredGenePageFormattedMaxRank == null) {
                if (other.filteredGenePageFormattedMaxRank != null) {
                    return false;
                }
            } else if (!filteredGenePageFormattedMaxRank.equals(other.filteredGenePageFormattedMaxRank)) {
                return false;
            }
            if (filteredGenePagePresentAnatEntities == null) {
                if (other.filteredGenePagePresentAnatEntities != null) {
                    return false;
                }
            } else if (!filteredGenePagePresentAnatEntities.equals(other.filteredGenePagePresentAnatEntities)) {
                return false;
            }
            if (presentAnatEntities == null) {
                if (other.presentAnatEntities != null) {
                    return false;
                }
            } else if (!presentAnatEntities.equals(other.presentAnatEntities)) {
                return false;
            }
            if (presentConds == null) {
                if (other.presentConds != null) {
                    return false;
                }
            } else if (!presentConds.equals(other.presentConds)) {
                return false;
            }
            if (absentAnatEntities == null) {
                if (other.absentAnatEntities != null) {
                    return false;
                }
            } else if (!absentAnatEntities.equals(other.absentAnatEntities)) {
                return false;
            }
            if (absentConds == null) {
                if (other.absentConds != null) {
                    return false;
                }
            } else if (!absentConds.equals(other.absentConds)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GeneStatsBean [").append(super.toString()).append("geneId=").append(geneId)
                    .append(", geneName=").append(geneName)
                    .append(", minRankAnatEntity=").append(minRankAnatEntity)
                    .append(", filteredGenePageMinRankAnatEntity=").append(filteredGenePageMinRankAnatEntity)
                    .append(", filteredGenePageFormattedMinRank=").append(filteredGenePageFormattedMinRank)
                    .append(", filteredGenePageFormattedMaxRank=").append(filteredGenePageFormattedMaxRank)
                    .append("]");
            return builder.toString();
        }
    }

    //NOTE: for each biotype of number of genes, number of genes with data,
    //number of genes with PRESENT condition calls (any quality), number with ABSENT condition calls (any quality),
    //total number of condition calls PRESENT * each quality, condition calls ABSENT * each quality.
    public static class BiotypeStatsBean extends CommonBean {

        //number of genes
        private int geneCount;
        //number of genes with data
        private int geneWithData;
        private int genePresentAbsentSilverGold;
        private int filteredGenePageGeneCount;
        private int anatEntityWithData;
        private int condWithData;

        //number of genes with PRESENT condition calls (any quality)
        private int presentCondGene;
        private int presentAnatEntityCount;
        private int presentCondCount;
        //number with ABSENT condition calls (any quality)
        private int absentCondGene;
        private int absentAnatEntityCount;
        private int absentCondCount;

        public BiotypeStatsBean() {
            geneCount = 0;
            geneWithData = 0;
            genePresentAbsentSilverGold = 0;
            filteredGenePageGeneCount = 0;
            anatEntityWithData = 0;
            condWithData = 0;
            presentCondGene = 0;
            presentAnatEntityCount = 0;
            presentCondCount = 0;
            absentCondGene = 0;
            absentAnatEntityCount = 0;
            absentCondCount = 0;
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

        public int getGenePresentAbsentSilverGold() {
            return genePresentAbsentSilverGold;
        }
        public void setGenePresentAbsentSilverGold(int genePresentAbsentSilverGold) {
            this.genePresentAbsentSilverGold = genePresentAbsentSilverGold;
        }

        public int getFilteredGenePageGeneCount() {
            return filteredGenePageGeneCount;
        }
        public void setFilteredGenePageGeneCount(int filteredGenePageGeneCount) {
            this.filteredGenePageGeneCount = filteredGenePageGeneCount;
        }

        public int getAnatEntityWithData() {
            return anatEntityWithData;
        }
        public void setAnatEntityWithData(int anatEntityWithData) {
            this.anatEntityWithData = anatEntityWithData;
        }

        public int getCondWithData() {
            return condWithData;
        }
        public void setCondWithData(int condWithData) {
            this.condWithData = condWithData;
        }

        public int getPresentCondGene() {
            return presentCondGene;
        }
        public void setPresentCondGene(int presentCondGene) {
            this.presentCondGene = presentCondGene;
        }

        public int getPresentAnatEntityCount() {
            return presentAnatEntityCount;
        }
        public void setPresentAnatEntityCount(int presentAnatEntityCount) {
            this.presentAnatEntityCount = presentAnatEntityCount;
        }

        public int getPresentCondCount() {
            return presentCondCount;
        }
        public void setPresentCondCount(int presentCondCount) {
            this.presentCondCount = presentCondCount;
        }

        public int getAbsentCondGene() {
            return absentCondGene;
        }
        public void setAbsentCondGene(int absentCondGene) {
            this.absentCondGene = absentCondGene;
        }

        public int getAbsentAnatEntityCount() {
            return absentAnatEntityCount;
        }
        public void setAbsentAnatEntityCount(int absentAnatEntityCount) {
            this.absentAnatEntityCount = absentAnatEntityCount;
        }

        public int getAbsentCondCount() {
            return absentCondCount;
        }
        public void setAbsentCondCount(int absentCondCount) {
            this.absentCondCount = absentCondCount;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            
            BiotypeStatsBean other = (BiotypeStatsBean) obj;

            if (geneCount != other.geneCount) {
                return false;
            }
            if (geneWithData != other.geneWithData) {
                return false;
            }
            if (genePresentAbsentSilverGold != other.genePresentAbsentSilverGold) {
                return false;
            }
            if (filteredGenePageGeneCount != other.filteredGenePageGeneCount) {
                return false;
            }
            if (anatEntityWithData != other.anatEntityWithData) {
                return false;
            }
            if (condWithData != other.condWithData) {
                return false;
            }
            if (presentCondGene != other.presentCondGene) {
                return false;
            }
            if (presentAnatEntityCount != other.presentAnatEntityCount) {
                return false;
            }
            if (presentCondCount != other.presentCondCount) {
                return false;
            }
            if (absentCondGene != other.absentCondGene) {
                return false;
            }
            if (absentAnatEntityCount != other.absentAnatEntityCount) {
                return false;
            }
            if (absentCondCount != other.absentCondCount) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + geneCount;
            result = prime * result + geneWithData;
            result = prime * result + genePresentAbsentSilverGold;
            result = prime * result + filteredGenePageGeneCount;
            result = prime * result + anatEntityWithData;
            result = prime * result + condWithData;
            result = prime * result + presentCondGene;
            result = prime * result + presentAnatEntityCount;
            result = prime * result + presentCondCount;
            result = prime * result + absentCondGene;
            result = prime * result + absentAnatEntityCount;
            result = prime * result + absentCondCount;
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("BiotypeStatsBean [").append(super.toString());
            sb.append(", geneCount=").append(geneCount);
            sb.append(", geneWithData=").append(geneWithData);
            sb.append(", genePresentAbsentSilverGold=").append(genePresentAbsentSilverGold);
            sb.append(", filteredGenePageGeneCount=").append(filteredGenePageGeneCount);
            sb.append(", anatEntityWithData=").append(anatEntityWithData);
            sb.append(", condWithData=").append(condWithData);
            sb.append(", presentCondGene=").append(presentCondGene);
            sb.append(", presentAnatEntityCount=").append(presentAnatEntityCount);
            sb.append(", presentCondCount=").append(presentCondCount);
            sb.append(", absentCondGene=").append(absentCondGene);
            sb.append(", absentAnatEntityCount=").append(absentAnatEntityCount);
            sb.append(", absentCondCount=").append(absentCondCount);
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
        this(ServiceFactory::new, null);
    }
    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
     *                                      to be able to provide one to each thread.
     */
    public GenerateInsertGeneStats(Supplier<ServiceFactory> serviceFactorySupplier, MySQLDAOManager manager) {
        super(manager);
        this.serviceFactorySupplier = serviceFactorySupplier;
    }

    /**
     * Main method to generate stats files. Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>path where to store the files
     * <li>suffix of the file names to store stats per biotype. Prefix will be the species name.
     * <li>suffix of the file names to store stats per gene. Prefix will be the species name.
     * <li>Optional: a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to 
     * generate download files, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}. 
     * If an empty list is provided (see {@link CommandRunner#EMPTY_LIST}), or this argument is not provided,
     * all species contained in the database will be used.
     * </ol>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If the arguments are incorrect.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length != 3 && args.length != 4) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments."));
        }

        Collection<Integer> speciesIds = new HashSet<>();
        if (args.length == 4) {
            speciesIds = CommandRunner.parseListArgumentAsInt(args[3]);
        }
        GenerateInsertGeneStats statsGenerator = new GenerateInsertGeneStats();
        statsGenerator.generate(args[0], args[1], args[2], speciesIds);
        
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
     * @param speciesIds                A {@code Collection} of {@code Integer}s that are the NCBI species IDs
     *                                  of the species for which we want to launch the computation. If {@code null}
     *                                  or empty, all species in the database are used.
     */
    public void generate(String path, String bioTypeStatsFileSuffix, String geneStatsFileSuffix, Collection<Integer> speciesIds) {
        log.entry(path, bioTypeStatsFileSuffix, geneStatsFileSuffix, speciesIds);

        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        Set<Species> allSpecies = serviceFactory.getSpeciesService().loadSpeciesByIds(speciesIds, false)
                .stream().collect(Collectors.toSet());
        //launch the computation for each species independently
        for (Species species: allSpecies) {

            log.info("Start generating of stat files for the species {}...", species.getId());
            
            //Retrieve all genes for that species
            Set<Gene> genes = serviceFactory.getGeneService().loadGenes(new GeneFilter(species.getId()))
                    .collect(Collectors.toSet());
            final Set<AnatEntity> nonInformativeAnatEntities = Collections.unmodifiableSet(
                    serviceFactory.getAnatEntityService()
                    .loadNonInformativeAnatEntitiesBySpeciesIds(Collections.singleton(species.getId()))
                    .collect(Collectors.toSet()));

            try {
                this.generatePerSpecies(species, genes, nonInformativeAnatEntities,
                        path, bioTypeStatsFileSuffix, geneStatsFileSuffix);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                // close connection to database between each species, to avoid idle
                // connection reset
                this.getManager().releaseResources();
            }

            log.info("Done generating of stat files for the species {}.", species.getId());
        }
        
        log.exit();
    }

    private void generatePerSpecies(Species species, Set<Gene> genes, final Set<AnatEntity> nonInformativeAnatEntities,
            String path, String bioTypeStatsFileSuffix, String geneStatsFileSuffix) throws IOException {
        log.entry(species, genes, nonInformativeAnatEntities, path, bioTypeStatsFileSuffix, geneStatsFileSuffix);

        //Now we use parallel streams for each gene independently
        List<GeneStatsBean> geneStatsBeans = genes.parallelStream().map(gene -> {
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
                                    CallService.Attribute.CALL_TYPE,
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
            attrs.add(CallService.Attribute.CALL_TYPE);
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
                boolean nonInformative = nonInformativeAnatEntities.contains(call.getCondition().getAnatEntity());
                if (call.getSummaryCallType().equals(ExpressionSummary.EXPRESSED)) {
                    bean.getPresentAnatEntities().add(call.getCondition().getAnatEntity());

                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setPresentBronzeAnatEntity(bean.getPresentBronzeAnatEntity() + 1);
                        if (!nonInformative) {
                            bean.setFilteredPresentBronzeAnatEntity(bean.getFilteredPresentBronzeAnatEntity() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setPresentSilverAnatEntity(bean.getPresentSilverAnatEntity() + 1);
                        if (!nonInformative) {
                            bean.setFilteredPresentSilverAnatEntity(bean.getFilteredPresentSilverAnatEntity() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setPresentGoldAnatEntity(bean.getPresentGoldAnatEntity() + 1);
                        if (!nonInformative) {
                            bean.setFilteredPresentGoldAnatEntity(bean.getFilteredPresentGoldAnatEntity() + 1);
                        }
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else if (call.getSummaryCallType().equals(ExpressionSummary.NOT_EXPRESSED)) {
                    bean.getAbsentAnatEntities().add(call.getCondition().getAnatEntity());

                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setAbsentBronzeAnatEntity(bean.getAbsentBronzeAnatEntity() + 1);
                        if (!nonInformative) {
                            bean.setFilteredAbsentBronzeAnatEntity(bean.getFilteredAbsentBronzeAnatEntity() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setAbsentSilverAnatEntity(bean.getAbsentSilverAnatEntity() + 1);
                        if (!nonInformative) {
                            bean.setFilteredAbsentSilverAnatEntity(bean.getFilteredAbsentSilverAnatEntity() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setAbsentGoldAnatEntity(bean.getAbsentGoldAnatEntity() + 1);
                        if (!nonInformative) {
                            bean.setFilteredAbsentGoldAnatEntity(bean.getFilteredAbsentGoldAnatEntity() + 1);
                        }
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
                boolean nonInformative = nonInformativeAnatEntities.contains(call.getCondition().getAnatEntity());
                if (call.getSummaryCallType().equals(ExpressionSummary.EXPRESSED)) {
                    bean.getPresentConds().add(call.getCondition());

                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setPresentBronzeCond(bean.getPresentBronzeCond() + 1);
                        if (!nonInformative) {
                            bean.setFilteredPresentBronzeCond(bean.getFilteredPresentBronzeCond() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setPresentSilverCond(bean.getPresentSilverCond() + 1);
                        if (!nonInformative) {
                            bean.setFilteredPresentSilverCond(bean.getFilteredPresentSilverCond() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setPresentGoldCond(bean.getPresentGoldCond() + 1);
                        if (!nonInformative) {
                            bean.setFilteredPresentGoldCond(bean.getFilteredPresentGoldCond() + 1);
                        }
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else if (call.getSummaryCallType().equals(ExpressionSummary.NOT_EXPRESSED)) {
                    bean.getAbsentConds().add(call.getCondition());

                    if (call.getSummaryQuality().equals(SummaryQuality.BRONZE)) {
                        bean.setAbsentBronzeCond(bean.getAbsentBronzeCond() + 1);
                        if (!nonInformative) {
                            bean.setFilteredAbsentBronzeCond(bean.getFilteredAbsentBronzeCond() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.SILVER)) {
                        bean.setAbsentSilverCond(bean.getAbsentSilverCond() + 1);
                        if (!nonInformative) {
                            bean.setFilteredAbsentSilverCond(bean.getFilteredAbsentSilverCond() + 1);
                        }
                    } else if (call.getSummaryQuality().equals(SummaryQuality.GOLD)) {
                        bean.setAbsentGoldCond(bean.getAbsentGoldCond() + 1);
                        if (!nonInformative) {
                            bean.setFilteredAbsentGoldCond(bean.getFilteredAbsentGoldCond() + 1);
                        }
                    } else {
                        throw log.throwing(new IllegalStateException(
                                "Unsupported SummaryQuality: " + call.getSummaryQuality()));
                    }
                } else {
                    throw log.throwing(new IllegalStateException(
                            "Unsupported SummaryCallType: " + call.getSummaryCallType()));
                }
            }

            if (!organCalls.isEmpty()) {
                LinkedHashMap<AnatEntity, List<ExpressionCall>> groupedCalls = callService
                        .loadCondCallsWithSilverAnatEntityCallsByAnatEntity(organCalls, conditionCalls, false);
                bean.setFilteredGenePagePresentAnatEntity(groupedCalls.size());
                bean.getFilteredGenePagePresentAnatEntities().addAll(groupedCalls.keySet());
                if (!groupedCalls.isEmpty()) {
                    //The LinkedHashMap has the same interface as Map, so we cannot easily access the last element.
                    //We thus create a List of Entries
                    List<Entry<AnatEntity, List<ExpressionCall>>> orderedEntries = new ArrayList<>(groupedCalls.entrySet());
                    Entry<AnatEntity, List<ExpressionCall>> firstEntry = orderedEntries.iterator().next();
                    bean.setFilteredGenePageMinRankAnatEntity(firstEntry.getKey().getName());
                    bean.setFilteredGenePageFormattedMinRank(firstEntry.getValue().iterator().next().getFormattedGlobalMeanRank());
                    //The max rank here could be understood as the max rank over all condition calls displayed on the gene page.
                    //But I think it's a bit confusing, the gene page mostly display information about anat. entities,
                    //so I take the min rank of the last anat. entity, as for the gene page.
                    Entry<AnatEntity, List<ExpressionCall>> lastEntry = orderedEntries.get(orderedEntries.size() - 1);
                    bean.setFilteredGenePageFormattedMaxRank(lastEntry.getValue().iterator().next().getFormattedGlobalMeanRank());
                }
            }
            if (!conditionCalls.isEmpty()) {
                ExpressionCall firstCall = conditionCalls.iterator().next();
                bean.setMinRank(firstCall.getGlobalMeanRank());
                bean.setFormattedMinRank(firstCall.getFormattedGlobalMeanRank());
                bean.setMinRankAnatEntity(firstCall.getCondition().getAnatEntity().getName());
                ExpressionCall lastCall = conditionCalls.get(conditionCalls.size() - 1);
                bean.setMaxRank(lastCall.getGlobalMeanRank());
                bean.setFormattedMaxRank(lastCall.getFormattedGlobalMeanRank());
            }
            return bean;
        })
        .sorted(Comparator.comparing(bean -> bean.getGeneId()))
        .collect(Collectors.toList());

        List<BiotypeStatsBean> biotypeStatsBeans = geneStatsBeans.stream()
                .collect(Collectors.groupingBy(GeneStatsBean::getBioTypeName))
                .entrySet().parallelStream()
                .map(e -> {
                    List<GeneStatsBean> geneBeans = e.getValue();
                    BiotypeStatsBean bsb = new BiotypeStatsBean();
                    bsb.setBioTypeName(e.getKey());

                    // number of genes
                    bsb.setGeneCount(geneBeans.size());

                    Set<AnatEntity> anatEntitiesWithData = new HashSet<>();
                    Set<Condition> condsWithData = new HashSet<>();
                    Set<AnatEntity> filteredGenePagePresentAnatEntities = new HashSet<>();
                    Set<AnatEntity> presentAnatEntities = new HashSet<>();
                    Set<Condition> presentConds = new HashSet<>();
                    Set<AnatEntity> absentAnatEntities = new HashSet<>();
                    Set<Condition> absentConds = new HashSet<>();
                    for (GeneStatsBean geneBean: geneBeans) {
                        if (getTotalCallCount(geneBean) > 0) {
                            bsb.setGeneWithData(bsb.getGeneWithData() + 1);
                        }
                        if (geneBean.getPresentGoldCond() + geneBean.getPresentSilverCond() +
                                geneBean.getAbsentGoldCond() + geneBean.getAbsentSilverCond() > 0) {
                            bsb.setGenePresentAbsentSilverGold(bsb.getGenePresentAbsentSilverGold() + 1);
                        }
                        if (geneBean.getPresentGoldCond() + geneBean.getPresentSilverCond() +
                                geneBean.getPresentBronzeCond() > 0) {
                            bsb.setPresentCondGene(bsb.getPresentCondGene() + 1);
                        }
                        if (geneBean.getAbsentGoldCond() + geneBean.getAbsentSilverCond() +
                                geneBean.getAbsentBronzeCond() > 0) {
                            bsb.setAbsentCondGene(bsb.getAbsentCondGene() + 1);
                        }
                        if (geneBean.getFilteredGenePagePresentAnatEntity() > 0) {
                            bsb.setFilteredGenePageGeneCount(bsb.getFilteredGenePageGeneCount() + 1);
                        }
                        if (geneBean.getMinRank() != null &&
                                (bsb.getMinRank() == null || bsb.getMinRank().compareTo(geneBean.getMinRank()) > 0)) {
                            bsb.setMinRank(geneBean.getMinRank());
                            bsb.setFormattedMinRank(geneBean.getFormattedMinRank());
                        }
                        if (geneBean.getMaxRank() != null &&
                                (bsb.getMaxRank() == null || bsb.getMaxRank().compareTo(geneBean.getMaxRank()) < 0)) {
                            bsb.setMaxRank(geneBean.getMaxRank());
                            bsb.setFormattedMaxRank(geneBean.getFormattedMaxRank());
                        }
                        bsb.setPresentBronzeAnatEntity(bsb.getPresentBronzeAnatEntity() + geneBean.getPresentBronzeAnatEntity());
                        bsb.setPresentSilverAnatEntity(bsb.getPresentSilverAnatEntity() + geneBean.getPresentSilverAnatEntity());
                        bsb.setPresentGoldAnatEntity(bsb.getPresentGoldAnatEntity() + geneBean.getPresentGoldAnatEntity());
                        bsb.setAbsentBronzeAnatEntity(bsb.getAbsentBronzeAnatEntity() + geneBean.getAbsentBronzeAnatEntity());
                        bsb.setAbsentSilverAnatEntity(bsb.getAbsentSilverAnatEntity() + geneBean.getAbsentSilverAnatEntity());
                        bsb.setAbsentGoldAnatEntity(bsb.getAbsentGoldAnatEntity() + geneBean.getAbsentGoldAnatEntity());
                        bsb.setFilteredPresentBronzeAnatEntity(bsb.getFilteredPresentBronzeAnatEntity()
                                + geneBean.getFilteredPresentBronzeAnatEntity());
                        bsb.setFilteredPresentSilverAnatEntity(bsb.getFilteredPresentSilverAnatEntity()
                                + geneBean.getFilteredPresentSilverAnatEntity());
                        bsb.setFilteredPresentGoldAnatEntity(bsb.getFilteredPresentGoldAnatEntity()
                                + geneBean.getFilteredPresentGoldAnatEntity());
                        bsb.setFilteredAbsentBronzeAnatEntity(bsb.getFilteredAbsentBronzeAnatEntity()
                                + geneBean.getFilteredAbsentBronzeAnatEntity());
                        bsb.setFilteredAbsentSilverAnatEntity(bsb.getFilteredAbsentSilverAnatEntity()
                                + geneBean.getFilteredAbsentSilverAnatEntity());
                        bsb.setFilteredAbsentGoldAnatEntity(bsb.getFilteredAbsentGoldAnatEntity()
                                + geneBean.getFilteredAbsentGoldAnatEntity());

                        bsb.setPresentBronzeCond(bsb.getPresentBronzeCond() + geneBean.getPresentBronzeCond());
                        bsb.setPresentSilverCond(bsb.getPresentSilverCond() + geneBean.getPresentSilverCond());
                        bsb.setPresentGoldCond(bsb.getPresentGoldCond() + geneBean.getPresentGoldCond());
                        bsb.setAbsentBronzeCond(bsb.getAbsentBronzeCond() + geneBean.getAbsentBronzeCond());
                        bsb.setAbsentSilverCond(bsb.getAbsentSilverCond() + geneBean.getAbsentSilverCond());
                        bsb.setAbsentGoldCond(bsb.getAbsentGoldCond() + geneBean.getAbsentGoldCond());
                        bsb.setFilteredPresentBronzeCond(bsb.getFilteredPresentBronzeCond()
                                + geneBean.getFilteredPresentBronzeCond());
                        bsb.setFilteredPresentSilverCond(bsb.getFilteredPresentSilverCond()
                                + geneBean.getFilteredPresentSilverCond());
                        bsb.setFilteredPresentGoldCond(bsb.getFilteredPresentGoldCond()
                                + geneBean.getFilteredPresentGoldCond());
                        bsb.setFilteredAbsentBronzeCond(bsb.getFilteredAbsentBronzeCond()
                                + geneBean.getFilteredAbsentBronzeCond());
                        bsb.setFilteredAbsentSilverCond(bsb.getFilteredAbsentSilverCond()
                                + geneBean.getFilteredAbsentSilverCond());
                        bsb.setFilteredAbsentGoldCond(bsb.getFilteredAbsentGoldCond()
                                + geneBean.getFilteredAbsentGoldCond());

                        anatEntitiesWithData.addAll(geneBean.getPresentAnatEntities());
                        anatEntitiesWithData.addAll(geneBean.getAbsentAnatEntities());
                        condsWithData.addAll(geneBean.getPresentConds());
                        condsWithData.addAll(geneBean.getAbsentConds());
                        filteredGenePagePresentAnatEntities.addAll(geneBean.getFilteredGenePagePresentAnatEntities());
                        presentAnatEntities.addAll(geneBean.getPresentAnatEntities());
                        presentConds.addAll(geneBean.getPresentConds());
                        absentAnatEntities.addAll(geneBean.getAbsentAnatEntities());
                        absentConds.addAll(geneBean.getAbsentConds());
                    }
                    bsb.setAnatEntityWithData(anatEntitiesWithData.size());
                    bsb.setCondWithData(condsWithData.size());
                    bsb.setFilteredGenePagePresentAnatEntity(filteredGenePagePresentAnatEntities.size());
                    bsb.setPresentAnatEntityCount(presentAnatEntities.size());
                    bsb.setPresentCondCount(presentConds.size());
                    bsb.setAbsentAnatEntityCount(absentAnatEntities.size());
                    bsb.setAbsentCondCount(absentConds.size());

                    return bsb;
                })
                .sorted(Comparator.comparing(bean -> bean.getBioTypeName()))
                .collect(Collectors.toList());

        this.writeFiles(StatFileType.GENE_STATS, species.getScientificName().replaceAll(" ", "_"), geneStatsBeans,
                path, geneStatsFileSuffix);
        this.writeFiles(StatFileType.BIO_TYPE_STATS, species.getScientificName().replaceAll(" ", "_"), biotypeStatsBeans,
                path, bioTypeStatsFileSuffix);

        //TODO insert the GeneBioTypeStatsTO into database
//        Set<BioTypeStatsTO> bioTypeStatsTOs = this.convertBiotypeStatsBeansToTOs(
//                biotypeStatsBeans.values().parallelStream()
//                        .flatMap(List::stream)
//                        .collect(Collectors.toSet()));
//        try {
//            this.startTransaction();
//
//            log.info("Start inserting of biotype statistics...");
//            this.geBioTypeStatsDAO().insertBioTypeStats(bioTypeStatsTOs);
//            log.info("Done inserting biotype statistics");
//            
//            this.commit();
//        } finally {
//            this.closeDAO();
//        }
        
        log.exit();
    }

//    private Set<BioTypeStatsTO> convertBiotypeStatsBeansToTOs(Set<BiotypeStatsBean> biotypeStatsBeans) {
//        log.entry(biotypeStatsBeans);
//        throw log.throwing(new UnsupportedOperationException("Mathod to be implement"));
//        return log.exit(biotypeStatsBeans.parallelStream()
//                .map(b -> new BioTypeStatsTO())
//                .collect(Collectors.toSet()));
//    }
    
    private <T extends CommonBean> void writeFiles(StatFileType fileType, String prefixFileName,
                                                   List<T> beans, String path, String fileSuffix)
            throws IOException {
        log.entry(fileType, beans, path, fileSuffix);
        
        String fileName = prefixFileName + "_" + fileSuffix + EXTENSION;
        
        File tmpFile = new File(path, fileName + ".tmp");
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        String[] headers = this.generateFileHeader(fileType);
        CellProcessor[] processors = this.generateFileCellProcessors(fileType, headers);
        Class<?> beanClass = null;
        if (fileType.equals(StatFileType.GENE_STATS)) {
            beanClass = GeneStatsBean.class;
        } else if (fileType.equals(StatFileType.BIO_TYPE_STATS)) {
            beanClass = BiotypeStatsBean.class;
        } else {
            throw log.throwing(new IllegalStateException("Unrecognized file type: " + fileType));
        }
        try (ICsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(new FileWriter(tmpFile),
                Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(headers)))) {

            beanWriter.configureBeanMapping(beanClass,
                    this.generateFileFieldMapping(fileType, headers));

            beanWriter.writeHeader(headers);
            beans.forEach(b -> {
                try {
                    beanWriter.write(b, processors);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (Exception e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw e;
        }
        File file = new File(path, fileName);
        if (tmpFile.exists()) {
            tmpFile.renameTo(file);
            log.info("New file created: {}", file.getPath());
        }

        log.exit();
    }

    private static int getTotalCallCount(GeneStatsBean geneBean) {
        log.entry(geneBean);
        
        int total = geneBean.getPresentBronzeAnatEntity() + geneBean.getPresentSilverAnatEntity() + 
                geneBean.getPresentGoldAnatEntity() + geneBean.getAbsentBronzeAnatEntity() + 
                geneBean.getAbsentSilverAnatEntity() + geneBean.getAbsentGoldAnatEntity() +
                geneBean.getPresentBronzeCond() + geneBean.getPresentSilverCond() +
                geneBean.getPresentGoldCond() + geneBean.getAbsentBronzeCond() +
                geneBean.getAbsentSilverCond() + geneBean.getAbsentGoldCond();
        return log.exit(total);
    }

    /**
     * Generates an {@code Array} of {@code String}s used to generate 
     * the header of TSV file according to provided file type.
     *
     * @return  The {@code Array} of {@code String}s used to produce the header.
     */
    private String[] generateFileHeader(StatFileType fileType) {
        log.entry(fileType);
        
        // We use an index to avoid to change hard-coded column numbers when we change columns 
        int idx = 0;

        int nbColumns;
        switch (fileType) {
            case GENE_STATS:
                nbColumns = 34;
                break;
            case BIO_TYPE_STATS:
                nbColumns = 40;
                break;
            default:
                throw new IllegalArgumentException("File type not supported: " + fileType);
        }
        String[] headers = new String[nbColumns];
        
        if (fileType.equals(StatFileType.GENE_STATS)) {
            headers[idx++] = GENE_ID_COLUMN_NAME;
            headers[idx++] = GENE_NAME_COLUMN_NAME;
        }
        headers[idx++] = BIO_TYPE_NAME_COLUMN_NAME;
        if (fileType.equals(StatFileType.BIO_TYPE_STATS)) {
            headers[idx++] = GENE_COUNT_COLUMN_NAME;
            headers[idx++] = GENE_WITH_DATA_COLUMN_NAME;
            headers[idx++] = GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME;
            headers[idx++] = PRESENT_COND_GENE_COLUMN_NAME;
            headers[idx++] = ABSENT_COND_GENE_COLUMN_NAME;
            headers[idx++] = FILTERED_GENE_PAGE_GENE_COLUMN_NAME;
            headers[idx++] = ANAT_ENTITY_WITH_DATA_COLUMN_NAME;
            headers[idx++] = PRESENT_ANAT_ENTITY_COLUMN_NAME;
            headers[idx++] = ABSENT_ANAT_ENTITY_COLUMN_NAME;
            headers[idx++] = COND_WITH_DATA_COLUMN_NAME;
            headers[idx++] = PRESENT_COND_COLUMN_NAME;
            headers[idx++] = ABSENT_COND_COLUMN_NAME;
        }

        headers[idx++] = FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = MIN_RANK_COLUMN_NAME;
        headers[idx++] = MAX_RANK_COLUMN_NAME;

        if (fileType.equals(StatFileType.GENE_STATS)) {
            headers[idx++] = MIN_RANK_ANAT_ENTITY_COLUMN_NAME;
            headers[idx++] = FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME;
            headers[idx++] = FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME;
            headers[idx++] = FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME;
        }

        headers[idx++] = PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = FILTERED_PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = FILTERED_PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = FILTERED_PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = FILTERED_ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = FILTERED_ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = FILTERED_ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME;
        headers[idx++] = PRESENT_BRONZE_COND_COLUMN_NAME;
        headers[idx++] = PRESENT_SILVER_COND_COLUMN_NAME;
        headers[idx++] = PRESENT_GOLD_COND_COLUMN_NAME;
        headers[idx++] = ABSENT_BRONZE_COND_COLUMN_NAME;
        headers[idx++] = ABSENT_SILVER_COND_COLUMN_NAME;
        headers[idx++] = ABSENT_GOLD_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_PRESENT_BRONZE_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_PRESENT_SILVER_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_PRESENT_GOLD_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_ABSENT_BRONZE_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_ABSENT_SILVER_COND_COLUMN_NAME;
        headers[idx++] = FILTERED_ABSENT_GOLD_COND_COLUMN_NAME;

        return log.exit(headers);
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process a TSV file 
     * of type {@code fileType}.
     *
     * @param fileType  The {@code FileType} of the file to be generated.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of an file.
     * @return          An {@code Array} of {@code CellProcessor}s used to process an file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateFileCellProcessors(StatFileType fileType, String[] header)
            throws IllegalArgumentException {
        log.entry(fileType, header);

        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {

            // *** CellProcessors common to all file types ***
            switch (header[i]) {
                case BIO_TYPE_NAME_COLUMN_NAME:
                    processors[i] = new StrNotNullOrEmpty();
                    break;
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
                case FILTERED_PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_PRESENT_BRONZE_COND_COLUMN_NAME:
                case FILTERED_PRESENT_SILVER_COND_COLUMN_NAME:
                case FILTERED_PRESENT_GOLD_COND_COLUMN_NAME:
                case FILTERED_ABSENT_BRONZE_COND_COLUMN_NAME:
                case FILTERED_ABSENT_SILVER_COND_COLUMN_NAME:
                case FILTERED_ABSENT_GOLD_COND_COLUMN_NAME:
                case FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME:
                    processors[i] = new LMinMax(0, Long.MAX_VALUE);
                    break;
                case MIN_RANK_COLUMN_NAME:
                case MAX_RANK_COLUMN_NAME:
                    // It's a String to be able to write values such as '3.32e4'.
                    //If there is no data for a gene, then there is no rank
                    processors[i] =  new ConvertNullTo("-");
                    break;
            }
            
            // If it was one of the column common to all file types, iterate next column name
            if (processors[i] != null) {
                continue;
            }

            if (fileType.equals(StatFileType.GENE_STATS)) {
                switch (header[i]) {
                    case GENE_ID_COLUMN_NAME:
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        processors[i] = new NotNull();
                        break;
                    case MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                    case FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME:
                    case FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME:
                    case FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                        processors[i] = new ConvertNullTo("-");
                        break;
                }
            } else if (fileType.equals(StatFileType.BIO_TYPE_STATS)) {
                switch (header[i]) {
                    case GENE_COUNT_COLUMN_NAME:
                    case GENE_WITH_DATA_COLUMN_NAME:
                    case GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME:
                    case PRESENT_COND_GENE_COLUMN_NAME:
                    case ABSENT_COND_GENE_COLUMN_NAME:
                    case ANAT_ENTITY_WITH_DATA_COLUMN_NAME:
                    case PRESENT_ANAT_ENTITY_COLUMN_NAME:
                    case ABSENT_ANAT_ENTITY_COLUMN_NAME:
                    case COND_WITH_DATA_COLUMN_NAME:
                    case PRESENT_COND_COLUMN_NAME:
                    case ABSENT_COND_COLUMN_NAME:
                    case FILTERED_GENE_PAGE_GENE_COLUMN_NAME:
                        processors[i] = new LMinMax(0, Long.MAX_VALUE);
                        break;
                }
            }
            
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " + header[i] +
                        " for file type: " + fileType));
            }
        }
        return log.exit(processors);
    }

    /**
     * Generate the field mapping for each column of the header of a TSV file
     * according to the provided file type.
     *
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a stat file.
     * @return          The {@code Array} of {@code String}s that is the field mapping, 
     *                  put in the {@code Array} at the same index as the column they 
     *                  are supposed to process.
     * @throws IllegalArgumentException If a {@code String} in {@code header} is not recognized.
     */
    private String[] generateFileFieldMapping(StatFileType fileType, String[] header) throws IllegalArgumentException {
        log.entry(fileType, header);
        
        String[] mapping = new String[header.length];
        for (int i = 0; i < header.length; i++) {

            // *** CellProcessors common to all file types ***
            switch (header[i]) {
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
                case FILTERED_PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredPresentBronzeAnatEntity";
                    break;
                case FILTERED_PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredPresentSilverAnatEntity";
                    break;
                case FILTERED_PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredPresentGoldAnatEntity";
                    break;
                case FILTERED_ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredAbsentBronzeAnatEntity";
                    break;
                case FILTERED_ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredAbsentSilverAnatEntity";
                    break;
                case FILTERED_ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredAbsentGoldAnatEntity";
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
                case FILTERED_PRESENT_BRONZE_COND_COLUMN_NAME:
                    mapping[i] = "filteredPresentBronzeCond";
                    break;
                case FILTERED_PRESENT_SILVER_COND_COLUMN_NAME:
                    mapping[i] = "filteredPresentSilverCond";
                    break;
                case FILTERED_PRESENT_GOLD_COND_COLUMN_NAME:
                    mapping[i] = "filteredPresentGoldCond";
                    break;
                case FILTERED_ABSENT_BRONZE_COND_COLUMN_NAME:
                    mapping[i] = "filteredAbsentBronzeCond";
                    break;
                case FILTERED_ABSENT_SILVER_COND_COLUMN_NAME:
                    mapping[i] = "filteredAbsentSilverCond";
                    break;
                case FILTERED_ABSENT_GOLD_COND_COLUMN_NAME:
                    mapping[i] = "filteredAbsentGoldCond";
                    break;
                case MIN_RANK_COLUMN_NAME:
                    mapping[i] = "formattedMinRank";
                    break;
                case MAX_RANK_COLUMN_NAME:
                    mapping[i] = "formattedMaxRank";
                    break;
                case FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME:
                    mapping[i] = "filteredGenePagePresentAnatEntity";
                    break;
            }

            // If it was one of the column common to all file types, iterate next column name
            if (mapping[i] != null) {
                continue;
            }

            if (fileType.equals(StatFileType.GENE_STATS)) {
                switch (header[i]) {
                    case GENE_ID_COLUMN_NAME:
                        mapping[i] = "geneId";
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        mapping[i] = "geneName";
                        break;
                    case MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                        mapping[i] = "minRankAnatEntity";
                        break;
                    case FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME:
                        mapping[i] = "filteredGenePageFormattedMinRank";
                        break;
                    case FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME:
                        mapping[i] = "filteredGenePageFormattedMaxRank";
                        break;
                    case FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                        mapping[i] = "filteredGenePageMinRankAnatEntity";
                        break;
                }
            } else if (fileType.equals(StatFileType.BIO_TYPE_STATS)) {
                switch (header[i]) {
                    case GENE_COUNT_COLUMN_NAME:
                        mapping[i] = "geneCount";
                        break;
                    case GENE_WITH_DATA_COLUMN_NAME:
                        mapping[i] = "geneWithData";
                        break;
                    case GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME:
                        mapping[i] = "genePresentAbsentSilverGold";
                        break;
                    case PRESENT_COND_GENE_COLUMN_NAME:
                        mapping[i] = "presentCondGene";
                        break;
                    case ABSENT_COND_GENE_COLUMN_NAME:
                        mapping[i] = "absentCondGene";
                        break;
                    case ANAT_ENTITY_WITH_DATA_COLUMN_NAME:
                        mapping[i] = "anatEntityWithData";
                        break;
                    case PRESENT_ANAT_ENTITY_COLUMN_NAME:
                        mapping[i] = "presentAnatEntityCount";
                        break;
                    case ABSENT_ANAT_ENTITY_COLUMN_NAME:
                        mapping[i] = "absentAnatEntityCount";
                        break;
                    case COND_WITH_DATA_COLUMN_NAME:
                        mapping[i] = "condWithData";
                        break;
                    case PRESENT_COND_COLUMN_NAME:
                        mapping[i] = "presentCondCount";
                        break;
                    case ABSENT_COND_COLUMN_NAME:
                        mapping[i] = "absentCondCount";
                        break;
                    case FILTERED_GENE_PAGE_GENE_COLUMN_NAME:
                        mapping[i] = "filteredGenePageGeneCount";
                        break;
                }
            }
            if (mapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " + header[i] +
                        " for file type: " + fileType));
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
                case GENE_COUNT_COLUMN_NAME:
                case GENE_WITH_DATA_COLUMN_NAME:
                case GENE_PRESENT_ABSENT_SILVER_GOLD_COLUMN_NAME:
                case PRESENT_COND_GENE_COLUMN_NAME:
                case ABSENT_COND_GENE_COLUMN_NAME:
                case ANAT_ENTITY_WITH_DATA_COLUMN_NAME:
                case COND_WITH_DATA_COLUMN_NAME:
                case PRESENT_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_COND_COLUMN_NAME:
                case ABSENT_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_COND_COLUMN_NAME:
                case PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_PRESENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_PRESENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_PRESENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_ABSENT_BRONZE_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_ABSENT_SILVER_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_ABSENT_GOLD_ANAT_ENTITY_COLUMN_NAME:
                case PRESENT_BRONZE_COND_COLUMN_NAME:
                case PRESENT_SILVER_COND_COLUMN_NAME:
                case PRESENT_GOLD_COND_COLUMN_NAME:
                case ABSENT_BRONZE_COND_COLUMN_NAME:
                case ABSENT_SILVER_COND_COLUMN_NAME:
                case ABSENT_GOLD_COND_COLUMN_NAME:
                case FILTERED_PRESENT_BRONZE_COND_COLUMN_NAME:
                case FILTERED_PRESENT_SILVER_COND_COLUMN_NAME:
                case FILTERED_PRESENT_GOLD_COND_COLUMN_NAME:
                case FILTERED_ABSENT_BRONZE_COND_COLUMN_NAME:
                case FILTERED_ABSENT_SILVER_COND_COLUMN_NAME:
                case FILTERED_ABSENT_GOLD_COND_COLUMN_NAME:
                case FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_GENE_PAGE_GENE_COLUMN_NAME:
                case MIN_RANK_COLUMN_NAME:
                case MAX_RANK_COLUMN_NAME:
                case FILTERED_GENE_PAGE_MIN_RANK_COLUMN_NAME:
                case FILTERED_GENE_PAGE_MAX_RANK_COLUMN_NAME:
                    quoteMode[i] = false;
                    break;
                case GENE_NAME_COLUMN_NAME:
                case BIO_TYPE_NAME_COLUMN_NAME:
                case MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                case FILTERED_GENE_PAGE_MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                    quoteMode[i] = true;
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException(
                            "Unrecognized header: " + header[i] + " for gene stat file."));
            }
        }
        return log.exit(quoteMode);
    }
}