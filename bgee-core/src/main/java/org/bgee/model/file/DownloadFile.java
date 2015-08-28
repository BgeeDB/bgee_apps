package org.bgee.model.file;

/**
 * Describes a file (available for download), providing information such as size, category.
 * @author Philippe Moret
 *
 */
public class DownloadFile {

    enum CategoryEnum {
        EXPR_CALLS("expr_calls", false),
        DIFF_EXPR_CALLS_STAGES("diff_expr_call_stages", true),
        DIFF_EXPR_CALLS_ANAT("diff_expr_calls_anatonmy", true),
        ORTHOLOGS("orthologs",false);


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
