package org.bgee.model.file;

/**
 * A file (available for download), providing information such as size, category.
 * @author Philippe Moret
 * @version Bgee 13
 * @since Bgee 13
 */
public class DownloadFile {

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
        AFFY_ROOT("affy_root",false),
        RNASEQ_ANNOT("rnaseq_annot",false),
        RNASEQ_DATA("rnaseq_data",false),
        RNASEQ_ROOT("rnaseq_root",false);


        CategoryEnum(String stringRepresentation, boolean isDiffExpr) {
            this.stringRepresentation = stringRepresentation;
            this.isDiffExpr = isDiffExpr;
        }

        public String getStringRepresentation() {
            return stringRepresentation;
        }

        private String stringRepresentation;

        public static CategoryEnum getById(String rep){
            for (CategoryEnum e : values()){
                if (e.getStringRepresentation().equals(rep))
                    return e;
            }
            throw new IllegalArgumentException("Could not recognize representation:"+rep);
        }

        private boolean isDiffExpr;

        public boolean isDiffExpr() {
            return isDiffExpr;
        }
    }

    private final String path;
    private final String name;
    private final CategoryEnum category;
    private String speciesDataGroupId;
    public long size;

    public DownloadFile(String path, String name, String category, long size, String speciesDataGroupId){
        this.path = path;
        this.name = name;
        this.size = size;
        this.category = CategoryEnum.getById(category);
        this.speciesDataGroupId = speciesDataGroupId;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public String getSpeciesDataGroupId() {
        return speciesDataGroupId;
    }

    public long getSize() {
        return size;
    }

    public boolean isDiffExpr(){
        return category.isDiffExpr();
    }
}
