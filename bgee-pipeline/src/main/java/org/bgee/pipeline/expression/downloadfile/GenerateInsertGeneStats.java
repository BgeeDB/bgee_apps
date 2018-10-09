package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
     * A {@code String} that is the extension of download files to be generated.
     */
    private final static String EXTENSION = ".tsv";

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


    public final static String GENE_COUNT_COLUMN_NAME = "Number of genes";
    public final static String GENE_WITH_DATA_COLUMN_NAME = "Number of genes with data";
    public final static String PRESENT_COND_GENE_COLUMN_NAME = "Count of showing expression";
    public final static String ABSENT_COND_GENE_COLUMN_NAME = "Count of showing absence of expression";

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
        private int presentBronzeCond;
        private int presentSilverCond;
        private int presentGoldCond;
        private int absentBronzeCond;
        private int absentSilverCond;
        private int absentGoldCond;

        public CommonBean() {
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((bioTypeName == null) ? 0 : bioTypeName.hashCode());
            result = prime * result + absentBronzeAnatEntity;
            result = prime * result + absentBronzeCond;
            result = prime * result + absentGoldAnatEntity;
            result = prime * result + absentGoldCond;
            result = prime * result + absentSilverAnatEntity;
            result = prime * result + absentSilverCond;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
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
                    .append(", absentGoldAnatEntity=").append(absentGoldAnatEntity).append(", presentBronzeCond=")
                    .append(presentBronzeCond).append(", presentSilverCond=").append(presentSilverCond)
                    .append(", presentGoldCond=").append(presentGoldCond).append(", absentBronzeCond=")
                    .append(absentBronzeCond).append(", absentSilverCond=").append(absentSilverCond)
                    .append(", absentGoldCond=").append(absentGoldCond).append("]");
            return builder.toString();
        }
    }

    public static class GeneStatsBean extends CommonBean {
        
        private String geneId;
        private String geneName;
        private int filteredGenePagePresentAnatEntity;
        private BigDecimal minRank;
        private BigDecimal maxRank;
        private AnatEntity minRankAnatEntity;

        public GeneStatsBean() {
            this.geneId = null;
            this.geneName = null;
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
            int result = super.hashCode();
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((geneName == null) ? 0 : geneName.hashCode());
            result = prime * result + filteredGenePagePresentAnatEntity;
            result = prime * result + ((maxRank == null) ? 0 : maxRank.hashCode());
            result = prime * result + ((minRank == null) ? 0 : minRank.hashCode());
            result = prime * result + ((minRankAnatEntity == null) ? 0 : minRankAnatEntity.hashCode());
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
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("GeneStatsBean [").append(super.toString()).append("geneId=").append(geneId)
                    .append(", geneName=").append(geneName)
                    .append(", filteredGenePagePresentAnatEntity=").append(filteredGenePagePresentAnatEntity)
                    .append(", minRank=").append(minRank).append(", maxRank=").append(maxRank)
                    .append(", minRankAnatEntity=").append(minRankAnatEntity).append("]");
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
        
        //number of genes with PRESENT condition calls (any quality)
        private int presentCondGene;
        //number with ABSENT condition calls (any quality)
        private int absentCondGene;

        // FIXME add ranks?

        public BiotypeStatsBean() {
            geneCount = 0;
            geneWithData = 0;
            presentCondGene = 0;
            absentCondGene = 0;
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
            if (presentCondGene != other.presentCondGene) {
                return false;
            }
            if (absentCondGene != other.absentCondGene) {
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
            result = prime * result + presentCondGene;
            result = prime * result + absentCondGene;
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("BiotypeStatsBean [").append(super.toString());
            sb.append(", geneCount=").append(geneCount);
            sb.append(", geneWithData=").append(geneWithData);
            sb.append(", presentCondGene=").append(presentCondGene);
            sb.append(", absentCondGene=").append(absentCondGene);
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

            log.info("Start generating of stat files for the species {}...", species.getId());
            
            //Retrieve all genes for that species
            Set<Gene> genes = serviceFactory.getGeneService().loadGenes(new GeneFilter(species.getId()))
                    .collect(Collectors.toSet());

                this.generatePerSpecies(species, genes, path, bioTypeStatsFileSuffix, geneStatsFileSuffix);

            log.info("Done generating of stat files for the species {}.", species.getId());
        }
        
        log.exit();
    }

    private void generatePerSpecies(Species species, Set<Gene> genes, String path,
                                    String bioTypeStatsFileSuffix, String geneStatsFileSuffix)
            throws IOException {
        log.entry(species, genes, path, bioTypeStatsFileSuffix, geneStatsFileSuffix);

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

            if (organCalls.size() > 0) {
                bean.setFilteredGenePagePresentAnatEntity(callService
                        .loadCondCallsWithSilverAnatEntityCallsByAnatEntity(organCalls, conditionCalls, false)
                        .size());
            }
            if (conditionCalls.size() > 0) {
                ExpressionCall firstCall = conditionCalls.iterator().next();
                bean.setMinRank(firstCall.getGlobalMeanRank());
                bean.setMinRankAnatEntity(firstCall.getCondition().getAnatEntity());
                bean.setMaxRank(conditionCalls.get(conditionCalls.size() - 1).getGlobalMeanRank());
            }
            return bean;
        })
        // We don't need to order them as we group them by ID latter.
        //.sorted(Comparator.comparing(bean -> bean.getGeneId()))
        .collect(Collectors.toList());

        Map<String, List<GeneStatsBean>> geneBeansByBiotype = geneStatsBeans.stream()
                .collect(Collectors.groupingBy(GeneStatsBean::getBioTypeName));

        Map<String, List<BiotypeStatsBean>> biotypeStatsBeans = geneBeansByBiotype.entrySet().parallelStream()
                .map(e -> {
                    List<GeneStatsBean> geneBeans = e.getValue();
                    BiotypeStatsBean bsb = new BiotypeStatsBean();
                    bsb.setAbsentBronzeAnatEntity(0);
                    bsb.setBioTypeName(e.getKey());

                    // number of genes
                    bsb.setGeneCount(geneBeans.size());

                    //XXX: we go through the gene beans several times, an unique for-loop could be more efficient?
                    // number of genes with data,
                    bsb.setGeneWithData((int) geneBeans.parallelStream()
                            .filter(g -> getTotalCallCount(g) > 0)
                            .count());

                    // number of genes with PRESENT condition calls (any quality)
                    bsb.setPresentCondGene((int) geneBeans.parallelStream()
                            .filter(g -> g.getPresentGoldCond() + g.getPresentSilverCond() +
                                    g.getPresentBronzeCond() > 0)
                            .count());

                    // number with ABSENT condition calls (any quality),
                    bsb.setAbsentCondGene((int) geneBeans.parallelStream()
                            .filter(g -> g.getAbsentGoldCond() + g.getAbsentSilverCond() +
                                    g.getAbsentBronzeCond() > 0)
                            .count());

                    // total number of anat.entity calls PRESENT * each quality
                    bsb.setPresentBronzeAnatEntity(this.getSum(geneBeans, GeneStatsBean::getPresentBronzeAnatEntity));
                    bsb.setPresentSilverAnatEntity(this.getSum(geneBeans, GeneStatsBean::getPresentSilverAnatEntity));
                    bsb.setPresentGoldAnatEntity(this.getSum(geneBeans, GeneStatsBean::getPresentGoldAnatEntity));

                    // total number of anat.entity calls ABSENT * each quality.
                    bsb.setAbsentBronzeAnatEntity(this.getSum(geneBeans, GeneStatsBean::getAbsentBronzeCond));
                    bsb.setAbsentSilverAnatEntity(this.getSum(geneBeans, GeneStatsBean::getAbsentSilverAnatEntity));
                    bsb.setAbsentGoldAnatEntity(this.getSum(geneBeans, GeneStatsBean::getAbsentGoldAnatEntity));

                    // total number of condition calls PRESENT * each quality
                    bsb.setPresentBronzeCond(this.getSum(geneBeans, GeneStatsBean::getPresentBronzeCond));
                    bsb.setPresentSilverCond(this.getSum(geneBeans, GeneStatsBean::getPresentSilverCond));
                    bsb.setPresentGoldCond(this.getSum(geneBeans, GeneStatsBean::getPresentGoldCond));

                    // total number of condition calls PRESENT * each quality
                    bsb.setAbsentBronzeCond(this.getSum(geneBeans, GeneStatsBean::getAbsentBronzeCond));
                    bsb.setAbsentSilverCond(this.getSum(geneBeans, GeneStatsBean::getAbsentSilverCond));
                    bsb.setAbsentGoldCond(this.getSum(geneBeans, GeneStatsBean::getAbsentGoldCond));

                    return bsb;
                })
                .collect(Collectors.groupingBy(BiotypeStatsBean::getBioTypeName));

        
        Map<String, List<GeneStatsBean>> geneBeansById = geneStatsBeans.stream()
                .collect(Collectors.groupingBy(GeneStatsBean::getGeneId));
        for (List<GeneStatsBean> beans : geneBeansById.values()) {
            this.writeFiles(StatFileType.GENE_STATS, beans.iterator().next().getGeneId(), beans,
                    path, geneStatsFileSuffix);
        }

        for (List<BiotypeStatsBean> beans : biotypeStatsBeans.values()) {
            this.writeFiles(StatFileType.BIO_TYPE_STATS, beans.iterator().next().getBioTypeName(), beans,
                    path, bioTypeStatsFileSuffix);
        }

        //TODO insert the GeneBioTypeStatsTO into database
        
        log.exit();
    }

    
    private <T extends CommonBean> void writeFiles(StatFileType fileType, String prefixFileName,
                                                   List<T> beans, String path, String fileSuffix)
            throws IOException {
        log.entry(fileType, beans, path, fileSuffix);
        
        String fileName = path + prefixFileName + "_" + fileSuffix + EXTENSION;
        
        File tmpFile = new File(path, fileName + ".tmp");
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        String[] headers = this.generateFileHeader(fileType);
        CellProcessor[] processors = this.generateFileCellProcessors(fileType, headers);
        try {
            ICsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(new FileWriter(tmpFile),
                    Utils.getCsvPreferenceWithQuote(this.generateQuoteMode(headers)));
            beanWriter.configureBeanMapping(BiotypeStatsBean.class,
                    this.generateFileFieldMapping(fileType, headers));

            beans.forEach(c -> {
                try {
                    beanWriter.write(beans, processors);
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

    private int getSum(List<GeneStatsBean> geneBeans, Function<GeneStatsBean, Integer> getter) {
        log.entry(geneBeans);
        return log.exit(geneBeans.parallelStream()
                .map(getter)
                .mapToInt(Integer::intValue)
                .sum());
    }

    private int getTotalCallCount(GeneStatsBean geneBean) {
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
                nbColumns = 19;
                break;
            case BIO_TYPE_STATS:
                nbColumns = 17;
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
            headers[idx++] = PRESENT_COND_GENE_COLUMN_NAME;
            headers[idx++] = ABSENT_COND_GENE_COLUMN_NAME;
        }

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

        if (fileType.equals(StatFileType.GENE_STATS)) {
            headers[idx++] = FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME;
            headers[idx++] = MIN_RANK_COLUMN_NAME;
            headers[idx++] = MAX_RANK_COLUMN_NAME;
            headers[idx++] = MIN_RANK_ANAT_ENTITY_COLUMN_NAME;
        }

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
                    new LMinMax(0, Long.MAX_VALUE);
                    break;
            }
            
            // If it was one of the column common to all file types, iterate next column name
            if (processors[i] != null) {
                continue;
            }

            if (fileType.equals(StatFileType.GENE_STATS)) {
                switch (header[i]) {
                    case GENE_ID_COLUMN_NAME:
                    case MIN_RANK_ANAT_ENTITY_COLUMN_NAME:
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        processors[i] = new NotNull();
                        break;
                    case FILTERED_GENE_PAGE_PRESENT_ANAT_ENTITY_COLUMN_NAME:
                        new LMinMax(0, Long.MAX_VALUE);
                        break;
                    case MIN_RANK_COLUMN_NAME:
                    case MAX_RANK_COLUMN_NAME:
                        // It's a String to be able to write values such as '3.32e4'
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    default:
                        throw log.throwing(new IllegalArgumentException(
                                "Unrecognized header: " + header[i] + " for gene stat file."));
                }
            }

            // If it was one of the column common to all file types, iterate next column name
            if (processors[i] != null) {
                continue;
            }

            if (fileType.equals(StatFileType.BIO_TYPE_STATS)) {
                switch (header[i]) {
                    case GENE_COUNT_COLUMN_NAME:
                    case GENE_WITH_DATA_COLUMN_NAME:
                    case PRESENT_COND_GENE_COLUMN_NAME:
                    case ABSENT_COND_GENE_COLUMN_NAME:
                        new LMinMax(0, Long.MAX_VALUE);
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

            // If it was one of the column common to all file types, iterate next column name
            if (mapping[i] != null) {
                continue;
            }

            if (fileType.equals(StatFileType.BIO_TYPE_STATS)) {
                switch (header[i]) {
                    case GENE_COUNT_COLUMN_NAME:
                        mapping[i] = "geneCount";
                        break;
                    case GENE_WITH_DATA_COLUMN_NAME:
                        mapping[i] = "geneWithData";
                        break;
                    case PRESENT_COND_GENE_COLUMN_NAME:
                        mapping[i] = "presentCondGene";
                        break;
                    case ABSENT_COND_GENE_COLUMN_NAME:
                        mapping[i] = "absentCondGene";
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
                case PRESENT_COND_GENE_COLUMN_NAME:
                case ABSENT_COND_GENE_COLUMN_NAME:
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
}