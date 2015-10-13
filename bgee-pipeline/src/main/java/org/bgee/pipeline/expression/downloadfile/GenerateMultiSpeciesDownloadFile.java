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
    public final static String CIO_ID_COLUMN_NAME = "Anatomy homology CIO ID";
    /**
     * A {@code String} that is the name of the column containing CIO statement names, 
     * in the download file.
     */
    public final static String CIO_NAME_ID_COLUMN_NAME = "Anatomy homology CIO name";
    /**
     * A {@code String} that is the name of the column containing number of expressed genes, 
     * in the download file.
     */
    public final static String EXPR_GENE_COUNT_COLUMN_NAME = "Expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of genes without data, 
     * in the download file.
     */
    public final static String NA_GENES_COUNT_COLUMN_NAME = "N/A gene count";
    /**
     * A {@code String} that is the name of the column containing number of over-expressed genes, 
     * in the download file.
     */
    public final static String OVER_EXPR_GENE_COUNT_COLUMN_NAME = "Over-expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of under-expressed genes, 
     * in the download file.
     */
    public final static String UNDER_EXPR_GENE_COUNT_COLUMN_NAME = "Under-expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of not diff. expressed 
     * genes, in the download file.
     */
    public final static String NO_DIFF_EXPR_GENE_COUNT_COLUMN_NAME = "Not diff. expressed gene count";
    /**
     * A {@code String} that is the name of the column containing number of not diff. expressed 
     * genes, in the download file.
     */
    public final static String NOT_EXPR_GENE_COUNT_COLUMN_NAME = "Not expressed gene count";
    /**
     * A {@code String} that is the name of the column containing latin species names, 
     * in the download file.
     */
    public final static String SPECIES_LATIN_NAME_COLUMN_NAME = "Latin species name";

    /**
     * A {@code String} that is a part of the file name of OMA files to be generated.
     */
    public final static String OMA_FILE_NAME = "orthologs";

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
         * @see #getAnatEntityIds()
         */
        private List<String> anatEntityIds;
        /**
         * @see #getAnatEntityNames()
         */
        private List<String> anatEntityNames;
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
        protected MultiSpeciesFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param anatEntityIds     See {@link #getAnatEntityIds()}.
         * @param anatEntityNames   See {@link #getAnatEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         */
        protected MultiSpeciesFileBean(String omaId, List<String> anatEntityIds, 
                List<String> anatEntityNames, List<String> stageIds, List<String> stageNames) {
            this.omaId = omaId;
            this.anatEntityIds = anatEntityIds;
            this.anatEntityNames = anatEntityNames;
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
         * @return  the {@code List} of {@code String}s that are the IDs of the anatomical entities.
         *          There is more than one entity only in multi-species files.
         *          When several are targeted, they are provided in alphabetical order.
         */
        public List<String> getAnatEntityIds() {
            return anatEntityIds;
        }
        /** 
         * @param anatEntityNames   A {@code List} of {@code String}s that are the IDs of the 
         *                          anatomical entities.
         * @see #getAnatEntityIds()
         */
        public void setEntityIds(List<String> anatEntityIds) {
            this.anatEntityIds = anatEntityIds;
        }

        /**
         * @return  the {@code List} of {@code String}s that are the names of the anatomical
         *          entities. There is more than one entity only in multi-species files.
         *          When there is several, they are returned in the same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getAnatEntityNames() {
            return anatEntityNames;
        }
        /**
         * @param anatEntityNames   A {@code List} of {@code String}s that are the names of the
         *                          anatomical entities.
         * @see #getAnatEntityNames()
         */
        public void setEntityNames(List<String> anatEntityNames) {
            this.anatEntityNames = anatEntityNames;
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
            result = prime * result + ((omaId == null) ? 0 : omaId.hashCode());
            result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
            result = prime * result + ((anatEntityNames == null) ? 0 : anatEntityNames.hashCode());
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
            if (anatEntityIds == null) {
                if (other.anatEntityIds != null)
                    return false;
            } else if (!anatEntityIds.equals(other.anatEntityIds))
                return false;
            if (anatEntityNames == null) {
                if (other.anatEntityNames != null)
                    return false;
            } else if (!anatEntityNames.equals(other.anatEntityNames))
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
            return  "OMA ID: " + getOmaId() +
                    " - Anat. entity IDs: " + getAnatEntityIds() + 
                    " - Anat. entity names: " + getAnatEntityNames() + 
                    " - Stage IDs: " + getStageIds() + " - Stage names: " + getStageNames();
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
    public static abstract class MultiSpeciesSimpleFileBean extends MultiSpeciesFileBean {
    
        /**
         * See {@link #getGeneIds()}
         */
        private List<String> geneIds;
        /**
         * See {@link #getGeneNames()}
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
         * @param geneId            See {@link #getGeneIds()}.
         * @param geneName          See {@link #getGeneNames()}.
         * @param anatEntityIds     See {@link #getAnatEntityIds()}.
         * @param anatEntityNames   See {@link #getAnatEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         */
        protected MultiSpeciesSimpleFileBean(String omaId,
                List<String> geneIds, List<String> geneNames, 
                List<String> anatEntityIds, List<String> anatEntityNames,
                List<String> stageIds, List<String> stageNames) {
            super(omaId, anatEntityIds, anatEntityNames, stageIds, stageNames);
            this.geneIds = geneIds;
            this.geneNames = geneNames;
        }
        
        /**
         * @return  the {@code List} of {@code String}s that are the IDs of the genes.
         *          When there is several genes, they are provided in alphabetical order.
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
         *          When there is several genes, they are provided in same order as their 
         *          corresponding ID, as returned by {@link #getGeneIds()}.
         */
        public List<String> getGeneNames() {
            return geneNames;
        }
        /**
         * @param geneNames A {@code List} of {@code String}s that are the names of genes.
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
            return super.toString() +
                    " - Gene IDs: " + getGeneIds() + " - Gene names: " + getGeneNames();
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
         * @param entityIds         See {@link #getAnatEntityIds()}.
         * @param entityNames       See {@link #getAnatEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         * @param geneId            See {@link #getGeneId()}.
         * @param geneName          See {@link #getGeneName()}.
         * @param cioId             See {@link #getCioId()}.
         * @param cioName           See {@link #getCioName()}.
         * @param speciesId         See {@link #getSpeciesId()}.
         * @param speciesName       See {@link #getSpeciesName()}.
         */
        protected MultiSpeciesCompleteFileBean(String omaId,
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, String geneId, String geneName, 
                String cioId, String cioName, String speciesId, String speciesName) {
            super(omaId, entityIds, entityNames, stageIds, stageNames);
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
    
    /**
     * A bean representing a row of an OMA file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 May 2015
     * @since Bgee 13
     */
    public static class OMAFileBean {

        /**
         * @see #getOmaId()
         */
        private String omaId;
        /**
         * @see #getGeneId()
         */
        private String geneId;
        /**
         * @see #getGeneName()
         */
        private String geneName;

        /**
         * 0-argument constructor of the bean.
         */
        protected OMAFileBean() {
        }

        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId     See {@link #getOmaId()}.
         * @param geneId    See {@link #getGeneId()}.
         * @param geneName  See {@link #getGeneName()}.
         */
        public OMAFileBean(String omaId, String geneId, String geneName) {
            this.omaId = omaId;
            this.geneId = geneId;
            this.geneName = geneName;
        }

        /** 
         * @return  the {@code String} that is the ID of OMA node.
         */
        public String getOmaId() {
            return omaId;
        }
        /**
         * @param omaId A {@code String} that is the ID of OMA node.
         * @see #getOmaId()
         */
        public void setOmaId(String omaId) {
            this.omaId = omaId;
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
         * @param geneName  A {@code String} that is the name of gene.
         * @see #getGeneName()
         */
        public void setGeneName(String geneName) {
            this.geneName = geneName;
        }
        

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((omaId == null) ? 0 : omaId.hashCode());
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((geneName == null) ? 0 : geneName.hashCode());
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
            OMAFileBean other = (OMAFileBean) obj;
            if (omaId == null) {
                if (other.omaId != null)
                    return false;
            } else if (!omaId.equals(other.omaId))
                return false;
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
            return true;
        }

        @Override
        public String toString() {
            return  "OMA ID: " + getOmaId() +
                    " - Gene ID: " + getGeneId() + " - Gene name: " + getGeneName();
        }
    }
}
