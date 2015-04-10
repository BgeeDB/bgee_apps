package org.bgee.pipeline.expression.downloadfile;

import java.util.List;

/**
 * This interface provides convenient common methods that generate multi-species TSV 
 * download files from the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public interface GenerateMultiSpeciesDownloadFile {

    /**
     * A {@code String} that is the name of the column containing OMA IDs, in the download file.
     */
    public final static String OMA_ID_COLUMN_NAME = "OMA ID";

//    /**
//     * A {@code String} that is the name of the column containing OMA descriptions,
//     * in the download file.
//     */
//    public final static String OMA_DESC_COLUMN_NAME = "OMA ID";

    /**
     * A {@code String} that is the name of the column containing list of gene IDs, 
     * in the download file.
     */
    public final static String GENE_ID_LIST_COLUMN_NAME = "Gene IDs";
    /**
     * A {@code String} that is the name of the column containing list of gene names, 
     * in the download file.
     */
    public final static String GENE_NAME_LIST_COLUMN_NAME = "Gene names";

    /**
     * A {@code String} that is the name of the column containing lists of anatomical entity IDs, 
     * in the download file.
     */
    public final static String ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME = "Anatomical entity IDs";
    /**
     * A {@code String} that is the name of the column containing lists of anatomical entity names, 
     * in the download file.
     */
    public final static String ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME = "Anatomical entity names";
    /**
     * A {@code String} that is the name of the column containing CIO statement IDs, 
     * in the download file.
     */
    public final static String CIO_ID_COLUMN_NAME = "CIO ID";
    /**
     * A {@code String} that is the name of the column containing CIO statement names, 
     * in the download file.
     */
    public final static String CIO_NAME_ID_COLUMN_NAME = "CIO name";
    /**
     * A {@code String} that is the name of the column containing number of expressed genes, 
     * in the download file.
     */
    public final static String NB_EXPR_GENE_COLUMN_NAME = "Expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of not expressed genes, 
     * in the download file.
     */
    public final static String NB_NO_EXPR_GENES_COLUMN_NAME = "Not expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of genes without data, 
     * in the download file.
     */
    public final static String NB_NA_GENES_COLUMN_NAME = "N/A gene count";
    /**
     * A {@code String} that is the name of the column containing number of over-expressed genes, 
     * in the download file.
     */
    public final static String NB_OVER_EXPR_GENES_COLUMN_NAME = "Over-expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of under-expressed genes, 
     * in the download file.
     */
    public final static String NB_UNDER_EXPR_GENES_COLUMN_NAME = "Under-expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of not diff. expressed 
     * genes, in the download file.
     */
    public final static String NB_NO_DIFF_EXPR_GENES_COLUMN_NAME = "Not diff. expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of not diff. expressed 
     * genes, in the download file.
     */
    public final static String NB_NOT_EXPR_GENES_COLUMN_NAME = "Not expressed gene count";
    /**
     * A {@code String} that is the name of the column containing latin species names, 
     * in the download file.
     */
    public final static String SPECIES_LATIN_NAME_COLUMN_NAME = "Latin species name";

    /**
     * Class parent of bean storing multi-species expression and differential expression calls, 
     * holding parameters common to all of them.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 13 Apr. 2015
     * @since   Bgee 13
     */
    public static abstract class MultiSpeciesFileBean {

        /**
         * @see #getOmaId()
         */
        private String omaId;
        /**
         * @see #getOmaDescription()
         */
        private String omaDescription;
        /**
         * @see #getEntityIds()
         */
        private List<String> entityIds;
        /**
         * @see #getEntityNames()
         */
        private List<String> entityNames;
        /**
         * @see #getStageIds()
         */
        private List<String> stageIds;
        /**
         * @see #getStageNames()
         */
        private List<String> stageNames;

        /**
         * 0-argument constructor of the bean.
         */
        private MultiSpeciesFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param omaDescription    See {@link #getOmaDescription()}.         
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         */
        private MultiSpeciesFileBean(String omaId, String omaDescription, List<String> entityIds, 
                List<String> entityNames, List<String> stageIds, List<String> stageNames) {
            this.omaId = omaId;
            this.omaDescription = omaDescription;
            this.entityIds = entityIds;
            this.entityNames = entityNames;
            this.stageIds = stageIds;
            this.stageNames = stageNames;
        }

        /** 
         * @return  the {@code String} that is the ID of ancestral OMA node of the gene.
         */
        public String getOmaId() {
            return omaId;
        }
        /**
         * @param omaId A {@code String} that is the ID of ancestral OMA node of the gene.
         * @see #getOmaId()
         */
        public void setOmaId(String omaId) {
            this.omaId = omaId;
        }

        /** 
         * @return  the {@code String} that is the description of ancestral OMA node.
         */
        public String getOmaDescription() {
            return omaDescription;
        }
        /** 
         * @param omaDescription    A {@code String} that is the description of ancestral OMA node.
         * @see #getOmaDescription()
         */
        public void setOmaDescription(String omaDescription) {
            this.omaDescription = omaDescription;
        }

        /**
         * @return  the {@code List} of {@code String}s that are the IDs of the anatomical entities.
         *          There is more than one entity only in multi-species files.
         *          When several are targeted, they are provided in alphabetical order.
         */
        public List<String> getEntityIds() {
            return entityIds;
        }
        /** 
         * @param entityIds A {@code List} of {@code String}s that are the IDs of the 
         *                  anatomical entities.
         * @see #getEntityIds()
         */
        public void setEntityIds(List<String> entityIds) {
            this.entityIds = entityIds;
        }

        /**
         * @return  the {@code List} of {@code String}s that are the names of the anatomical
         *          entities. There is more than one entity only in multi-species files.
         *          When there is several, they are returned in the same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getEntityNames() {
            return entityNames;
        }
        /**
         * @param entityNames   A {@code List} of {@code String}s that are the names of the
         *                      anatomical entities.
         * @see #getEntityNames()
         */
        public void setEntityNames(List<String> entityNames) {
            this.entityNames = entityNames;
        }
        
        /** 
         * @return  the {@code List} of {@code String}s that are the IDs of stages.
         */
        public List<String> getStageIds() {
            return stageIds;
        }
        /**
         * @param stageIds   A {@code List} of {@code String}s that are the IDs of stages.
         * @see #getStageIds()
         */
        public void setStageIds(List<String> stageIds) {
            this.stageIds = stageIds;
        }
        
        /** 
         * @return  the {@code List} of {@code String}s that are the names of stages.
         */
        public List<String> getStageNames() {
            return stageNames;
        }
        /**
         * @param stageNames A {@code List} of {@code String}s that are the names of stages.
         * @see #getStageNames()
         */
        public void setStageNames(List<String> stageNames) {
            this.stageNames = stageNames;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((omaDescription == null) ? 0 : omaDescription.hashCode());
            result = prime * result + ((omaId == null) ? 0 : omaId.hashCode());
            result = prime * result + ((entityIds == null) ? 0 : entityIds.hashCode());
            result = prime * result + ((entityNames == null) ? 0 : entityNames.hashCode());
            result = prime * result + ((stageIds == null) ? 0 : stageIds.hashCode());
            result = prime * result + ((stageNames == null) ? 0 : stageNames.hashCode());
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
            MultiSpeciesFileBean other = (MultiSpeciesFileBean) obj;
            if (omaId == null) {
                if (other.omaId != null)
                    return false;
            } else if (!omaId.equals(other.omaId))
                return false;
            if (omaDescription == null) {
                if (other.omaDescription != null)
                    return false;
            } else if (!omaDescription.equals(other.omaDescription))
                return false;
            if (entityIds == null) {
                if (other.entityIds != null)
                    return false;
            } else if (!entityIds.equals(other.entityIds))
                return false;
            if (entityNames == null) {
                if (other.entityNames != null)
                    return false;
            } else if (!entityNames.equals(other.entityNames))
                return false;
            if (stageIds == null) {
                if (other.stageIds != null)
                    return false;
            } else if (!stageIds.equals(other.stageIds))
                return false;
            if (stageNames == null) {
                if (other.stageNames != null)
                    return false;
            } else if (!stageNames.equals(other.stageNames))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return  "OMA ID: " + getOmaId() + " - OMA description: " + getOmaDescription() +
                    " - Entity IDs: " + getEntityIds() + " - Entity names: " + getEntityNames() + 
                    " - Stage IDs: " + getStageIds() + " - Stage names: " + getStageNames();
        }
    }

    /**
     * Class parent of bean storing multi-species expression and differential expression 
     * calls for a simple file, holding parameters common to all of them.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 13 Apr. 2015
     * @since   Bgee 13
     */
    //FIXME: actually, geneIds and geneNames are not used anymore, the geneIds and geneNames 
    //should be provided in simple files are they are in complete files, see fixme 
    //in GenerateMultiSpeciesDiffExprFile.filterAndWriteOMANodeRows 
    public static abstract class MultiSpeciesSimpleFileBean extends MultiSpeciesFileBean {

        /**
         * @see #getGeneIds()
         */
        private List<String> geneIds;
        /**
         * @see #getGeneNames()
         */
        private List<String> geneNames;
        
        /**
         * 0-argument constructor of the bean.
         */
        protected MultiSpeciesSimpleFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param omaDescription    See {@link #getOmaDescription()}.         
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         * @param geneIds           See {@link #getGeneIds()}.
         * @param geneNames         See {@link #getGeneNames()}.
         */
        protected MultiSpeciesSimpleFileBean(String omaId, String omaDescription, 
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, List<String> geneIds, List<String> geneNames) {
            super(omaId, omaDescription, entityIds, entityNames, stageIds, stageNames);
            this.geneIds = geneIds;
            this.geneNames = geneNames;
        }

        /** 
         * @return  the {@code List} of {@code String}s that are the IDs of the genes.
         *          There is more than one gene only in multi-species simple files.
         *          When several are targeted, they are provided in alphabetical order.
         */
        public List<String> getGeneIds() {
            return geneIds;
        }
        /** 
         * @param geneIds   A {@code List} of {@code String}s that are the IDs of the genes. 
         * @see #getGeneIds()
         */
        public void setGeneIds(List<String> geneIds) {
            this.geneIds = geneIds;
        }

        /**
         * @return  the {@code List} of {@code String}s that are the names of the genes. 
         *          There is more than one gene only in multi-species simple files. 
         *          When there is several, they are returned in the same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getGeneNames() {
            return geneNames;
        }
        /**
         * @param geneName  A {@code List} of {@code String}s that are the names of the genes.
         * @see #getGeneNames()
         */
        public void setGeneNames(List<String> geneNames) {
            this.geneNames = geneNames;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
            result = prime * result + ((geneNames == null) ? 0 : geneNames.hashCode());
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
            MultiSpeciesSimpleFileBean other = (MultiSpeciesSimpleFileBean) obj;
            if (geneIds == null) {
                if (other.geneIds != null)
                    return false;
            } else if (!geneIds.equals(other.geneIds))
                return false;
            if (geneNames == null) {
                if (other.geneNames != null)
                    return false;
            } else if (!geneNames.equals(other.geneNames))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - Gene IDs: " + geneIds + " - Gene names: " + geneNames;
        }
    }

    /**
     * Class parent of bean storing multi-species expression and differential expression 
     * calls for a complete file, holding parameters common to all of them.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 13 Apr. 2015
     * @since   Bgee 13
     */
    public static abstract class MultiSpeciesCompleteFileBean extends MultiSpeciesFileBean {
    
        /**
         * @see #getGeneId()
         */
        private String geneId;
        /**
         * @see #getGeneName()
         */
        private String geneName;
        /**
         * @see getCioId()
         */
        private String cioId;
        /**
         * @see getCioName()
         */
        private String cioName;
        /**
         * @see #getSpeciesId()
         */
        private String speciesId;
        /**
         * @see #getSpeciesName()
         */
        private String speciesName;
        
        /**
         * 0-argument constructor of the bean.
         */
        protected MultiSpeciesCompleteFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param omaDescription    See {@link #getOmaDescription()}.         
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         * @param geneId            See {@link #getGeneId()}.
         * @param geneName          See {@link #getGeneName()}.
         * @param cioId             See {@link #getCioId()}.
         * @param cioName           See {@link #getCioName()}.
         * @param speciesId         See {@link #getSpeciesId()}.
         * @param speciesName       See {@link #getSpeciesName()}.
         */
        protected MultiSpeciesCompleteFileBean(String omaId, String omaDescription, 
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, String geneId, String geneName, 
                String cioId, String cioName, String speciesId, String speciesName) {
            super(omaId, omaDescription, entityIds, entityNames, stageIds, stageNames);
            this.geneId = geneId;
            this.geneName = geneName;
            this.cioId = cioId;
            this.cioName = cioName;
            this.speciesId = speciesId;
            this.speciesName = speciesName;
        }
        
        /** 
         * @return  the {@code String} that is the ID of the gene.
         */
        public String getGeneId() {
            return geneId;
        }
        /**
         * @param geneId    A {@code String} that is the ID of the gene.
         * @see #getGeneId()
         */
        public void setGeneId(String geneId) {
            this.geneId = geneId;
        }

        /** 
         * @return  the {@code String} that is the name of the gene.
         */
        public String getGeneName() {
            return geneName;
        }
        /** 
         * @param geneName  A {@code String} that is the name of the gene.
         * @see #getGeneName()
         */
        public void setGeneName(String geneName) {
            this.geneName = geneName;
        }

        /**
         * @return  the {@code String} that is the ID of the CIO statement.
         */
        public String getCioId() {
            return cioId;
        }
        /**
         * @param cioId A {@code String} that is the ID of the CIO statement.
         * @see #getCioId()
         */
        public void setCioId(String cioId) {
            this.cioId = cioId;
        }

        /** 
         * @return  the {@code String} that is the name of the CIO statement.
         */
        public String getCioName() {
            return cioName;
        }
        /**
         * @param cioName   A {@code String} that is the name of the CIO statement.
         * @see #getCioName()
         */
        public void setCioName(String cioName) {
            this.cioName = cioName;
        }

        /** 
         * @return  the {@code String} that is the ID of the species.
         */
        public String getSpeciesId() {
            return speciesId;
        }
        /**
         * @param speciesId   A {@code String} that is the ID of the species.
         * @see #getSpeciesId()
         */
        public void setSpeciesId(String speciesId) {
            this.speciesId = speciesId;
        }

        /** 
         * @return  the {@code String} that is the name of the species.
         */
        public String getSpeciesName() {
            return speciesName;
        }
        /**
         * @param speciesName   A {@code String} that is the name of the species.
         * @see #getSpeciesName()
         */
        public void setSpeciesName(String speciesName) {
            this.speciesName = speciesName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((geneName == null) ? 0 : geneName.hashCode());
            result = prime * result + ((cioId == null) ? 0 : cioId.hashCode());
            result = prime * result + ((cioName == null) ? 0 : cioName.hashCode());
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            result = prime * result + ((speciesName == null) ? 0 : speciesName.hashCode());
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
            MultiSpeciesCompleteFileBean other = (MultiSpeciesCompleteFileBean) obj;
            if (geneId == null) {
                if (other.geneId != null)
                    return false;
            } else if (!geneId.equals(other.geneId))
                return false;
            if (geneName == null) {
                if (other.geneName != null)
                    return false;
            } else if (!geneName.equals(other.geneName))
                return false;
            if (cioId == null) {
                if (other.cioId != null)
                    return false;
            } else if (!cioId.equals(other.cioId))
                return false;
            if (cioName == null) {
                if (other.cioName != null)
                    return false;
            } else if (!cioName.equals(other.cioName))
                return false;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            if (speciesName == null) {
                if (other.speciesName != null)
                    return false;
            } else if (!speciesName.equals(other.speciesName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() +
                    " - Gene ID: " + getGeneId() + " - Gene name: " + getGeneName() + 
                    " - CIO ID: " + getCioId() + " - CIO name: " + getCioName() + 
                    " - Species ID: " + getSpeciesId() + " - Species name: " + getSpeciesName();
        }
    }
}
