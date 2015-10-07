package org.bgee.model.file;

import org.apache.commons.lang3.StringUtils;

/**
 * A file (available for download), providing information such as size, category.
 * @author Philippe Moret
 * @version Bgee 13
 * @since Bgee 13
 */
//TODO: javadoc, sanity checks
public class DownloadFile {

	/**
	 * This enum contains all the different categories of files:
	 * <ul>
     *   <li>{@code EXPR_CALLS_SIMPLE} a simple expression calls file</li>
     *   <li>{@code EXPR_CALLS_COMPLETE} a complete expression calls file</li>
     *   <li>{@code DIFF_EXPR_ANAT_SIMPLE} a simple differential expression across anatomy file</li>
     *   <li>{@code DIFF_EXPR_ANAT_COMPLETE} a complete differential expression across anatomy file</li>
     *   <li>{@code DIFF_EXPR_DEV_COMPLETE} a complete differential expression across developmental stages file</li>
     *   <li>{@code DIFF_EXPR_DEV_SIMPLE}a simple differential expression across developmental stages file</li>
     *   <li>{@code ORTHOLOG} corresponds to an orthologies file</li>
     *   <li>{@code AFFY_ANNOT} corresponds to an Affymetrix annoations file</li>
     *   <li>{@code AFFY_DATA} corresponds to an Affymetrix signal intensities file</li>
     *   <li>{@code RNASEQ_ANNOT} corresponds to RNA-Seq annotations file</li>
     *   <li>{@code RNASEQ_DATA} corresponds toRNA-Seq data file</li>
	 * </ul>
	 * @author Philippe Moret
	 * @version Bgee 13
	 * @since Bgee 13
	 *
	 */
    public enum CategoryEnum {
        EXPR_CALLS_SIMPLE("expr_simple",false),
        EXPR_CALLS_COMPLETE("expr_complete",false),
        DIFF_EXPR_ANAT_SIMPLE("diff_expr_anatomy_simple",true),
        DIFF_EXPR_ANAT_COMPLETE("diff_expr_anatomy_complete",true),
        DIFF_EXPR_DEV_COMPLETE("diff_expr_dev_complete",true),
        DIFF_EXPR_DEV_SIMPLE("diff_expr_dev_simple",true),
        ORTHOLOG("ortholog",false),
        AFFY_ANNOT("affy_annot",false),
        AFFY_DATA("affy_data",false),
        RNASEQ_ANNOT("rnaseq_annot",false),
        RNASEQ_DATA("rnaseq_data",false);

        /** The string representation */
        private final String stringRepresentation;

        /** a boolean that is true is the category is that of a differential expression file */
        private final boolean isDiffExpr;

        /**
         * Constructor with 2-params
         * @param stringRepresentation The {@code String reprensation}
         * @param isDiffExpr           A boolean whose value is true is the category
         *                             is that of a differential expression file
         */
        CategoryEnum(String stringRepresentation, boolean isDiffExpr) {
            this.stringRepresentation = stringRepresentation;
            this.isDiffExpr = isDiffExpr;
        }

        public String getStringRepresentation() {
            return stringRepresentation;
        }

        /**
         * Helper to get the enum value from a {@code String}
         * @param rep {@code String}
         * @return the matching {@code CategoryEnum}
         */
        public static CategoryEnum getById(String rep){
            for (CategoryEnum e : values()){
                if (e.getStringRepresentation().equals(rep))
                    return e;
            }
            throw new IllegalArgumentException("Could not recognize representation: "+rep);
        }

        /**
         * @return true if the file category is a differential expression
         */
        public boolean isDiffExpr() {
            return isDiffExpr;
        }
        
        @Override
        public String toString() {
            return getStringRepresentation();
        }
    }

    private final String path;
    private final String name;
    private final CategoryEnum category;
    private final String speciesDataGroupId;
    private final long size;

    /**
     * The constructor provides all values to create a {@code Download}
     * @param path                 A {@code String} representing the path of the containing the file, not null.
     * @param name                 A {@code String} containing the file name. Might be {@code /} 
     *                             by convention if the file represents a directory.
     * @param category             A {@code CategoryEnum} 
     * @param size                 A {@code long} representing the file size in bytes.
     * @param speciesDataGroupId   A {@code String} representing the species data group that owns this file.
     * @throws IllegalArgumentException If any of the argument is {@code null}.
     */
    public DownloadFile(String path, String name, CategoryEnum category, Long size, String speciesDataGroupId){
        if (StringUtils.isBlank(path) || StringUtils.isBlank(name) || category == null || 
                StringUtils.isBlank(speciesDataGroupId)) {
            throw new IllegalArgumentException("No argument can be null or blank.");
        }
        this.path = path;
        this.name = name;
        this.size = size;
        this.category = category;
        this.speciesDataGroupId = speciesDataGroupId;
    }

    /**
     * Get the path, relative to download files root directory.
     * @return A {@code String} containing the path where the actual file is found
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the file name
     * @return A {@code String} containing the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the file category
     * @return A {@code CategoryEnum} representing the file's category.
     */
    public CategoryEnum getCategory() {
        return category;
    }

    /**
     * Gets the species data group id
     * @return A {@code String} representation of the species data group id
     */
    public String getSpeciesDataGroupId() {
        return speciesDataGroupId;
    }

    /**
     * Gets the size
     * @return A long representing the size of the file in bytes
     */
    public Long getSize() {
        return size;
    }

    /**
     * @return {@code true} if the file is a differential expression file
     */
    public boolean isDiffExpr(){
        return category.isDiffExpr();
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (int) (size ^ (size >>> 32));
		result = prime * result + ((speciesDataGroupId == null) ? 0 : speciesDataGroupId.hashCode());
		return result;
	}


	@Override
	public String toString() {
		return "DownloadFile [path=" + path + ", name=" + name + ", category=" + category + ", speciesDataGroupId="
		        + speciesDataGroupId + ", size=" + size + "]";
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
		DownloadFile other = (DownloadFile) obj;
		if (category != other.category) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		if (speciesDataGroupId == null) {
			if (other.speciesDataGroupId != null) {
				return false;
			}
		} else if (!speciesDataGroupId.equals(other.speciesDataGroupId)) {
			return false;
		}
		return true;
	}
}
