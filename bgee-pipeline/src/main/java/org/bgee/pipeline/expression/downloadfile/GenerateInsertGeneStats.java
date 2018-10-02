package org.bgee.pipeline.expression.downloadfile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/**
 * Class used to generate statistics about expression data of genes and gene biotypes.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Sep. 2018
 * @since Bgee 14 Sep. 2018
 */
public class GenerateInsertGeneStats {
    private final static Logger log = LogManager.getLogger(GenerateInsertGeneStats.class.getName());

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
                                    // XXX: do we need DATA_QUALITY?
                                    CallService.Attribute.DATA_QUALITY, CallService.Attribute.GLOBAL_MEAN_RANK,
                                    CallService.Attribute.EXPERIMENT_COUNTS),
                            null)
                    .collect(Collectors.toSet());

            LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering =
                    new LinkedHashMap<>();
            serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
            EnumSet<CallService.Attribute> attrs = Arrays.stream(CallService.Attribute.values())
                    .filter(a -> a.isConditionParameter())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CallService.Attribute.class)));
            // XXX: do we need DATA_QUALITY?
            attrs.add(CallService.Attribute.DATA_QUALITY);
            attrs.add(CallService.Attribute.GLOBAL_MEAN_RANK);
            attrs.add(CallService.Attribute.EXPERIMENT_COUNTS);
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
}
