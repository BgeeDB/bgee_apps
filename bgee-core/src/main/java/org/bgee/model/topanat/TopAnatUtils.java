package org.bgee.model.topanat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition;

//TODO: this is a very first step to refactor code for topAnat R and web.
// all code used by both topAnat R and web should be moved here
public class TopAnatUtils {

    private final static Logger log = LogManager
            .getLogger(TopAnatUtils.class.getName());

    public final static String FILE_PREFIX = "topAnat_";
    
    public final static String TMP_FILE_SUFFIX = ".tmp";

    /**
     * 
     * @param src
     * @param dest
     * @throws IOException 
     */
    public static void move(Path src, Path dest, boolean checkSize) throws IOException{
        if(!checkSize || Files.size(src) > 0) 
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        else {
            throw log.throwing(new IllegalStateException("Empty tmp file"));
        }
    }

    //TODO: actually, these generators should be methods, so that we can know
    //what were the parameters requested, in order to work on any condition parameters
    //(work on a real condition graph)
    public final static Function<Condition, String> COND_ID_GENERATOR =
            cond -> conditionId(cond.getAnatEntityId(), cond.getCellTypeId());
    public final static Function<Condition, String> COND_NAME_GENERATOR =
            cond -> conditionName(cond.getAnatEntity(), cond.getCellType());

    /**
     * Separates the ID of the cell type from the ID of the anat. entity in the ID of
     * a condition targeting a specific cell type, see {@link #conditionId(String, String)}.
     */
    public final static String CONDITION_ID_SEPARATOR = "-";

    /**
     * Build the ID of a condition of topAnat, an anat. entity possibly restricted to a cell type.
     * It is built from the IDs alone, so that it can be built from the columns of the call files
     * as well as from the conditions themselves: this is the only place defining it.
     *
     * @param anatEntityId  A {@code String} that is the ID of the anat. entity of the condition.
     * @param cellTypeId    A {@code String} that is the ID of the cell type of the condition,
     *                      {@code null} or the root of the cell types when the condition targets
     *                      no specific cell type.
     * @return              A {@code String} that is the ID of the anat. entity, preceded by
     *                      the ID of the cell type and {@link #CONDITION_ID_SEPARATOR} when
     *                      the condition targets a specific cell type.
     * @throws IllegalArgumentException If {@code anatEntityId} is blank.
     */
    public static String conditionId(String anatEntityId, String cellTypeId) {
        if (StringUtils.isBlank(anatEntityId)) {
            throw log.throwing(new IllegalArgumentException("An anat. entity ID must be provided"));
        }
        if (!isSpecificCellType(cellTypeId)) {
            return anatEntityId;
        }
        return cellTypeId + CONDITION_ID_SEPARATOR + anatEntityId;
    }
    /**
     * Build the name of a condition of topAnat, see {@link #conditionId(String, String)}.
     *
     * @param anatEntity    The {@code AnatEntity} of the condition, with its name.
     * @param cellType      The {@code AnatEntity} that is the cell type of the condition, with
     *                      its name, {@code null} or the root of the cell types when the condition
     *                      targets no specific cell type.
     * @return              A {@code String} that is the name of the anat. entity, preceded by
     *                      the name of the cell type and "in" when the condition targets
     *                      a specific cell type.
     * @throws IllegalArgumentException If {@code anatEntity} is {@code null}.
     */
    public static String conditionName(AnatEntity anatEntity, AnatEntity cellType) {
        if (anatEntity == null) {
            throw log.throwing(new IllegalArgumentException("An anat. entity must be provided"));
        }
        if (cellType == null || !isSpecificCellType(cellType.getId())) {
            return anatEntity.getName();
        }
        return cellType.getName() + " in " + anatEntity.getName();
    }
    private static boolean isSpecificCellType(String cellTypeId) {
        return cellTypeId != null && !cellTypeId.equals(ConditionDAO.CELL_TYPE_ROOT_ID);
    }

    public final static EnumSet<CallService.Attribute> CALL_SERVICE_ATTRIBUTES =
            EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                    CallService.Attribute.CELL_TYPE_ID);

}
