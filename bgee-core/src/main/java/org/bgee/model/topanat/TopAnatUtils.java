package org.bgee.model.topanat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import java.util.function.Function;

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
            cond -> {
                StringBuilder sb = new StringBuilder();
                String cellTypeId = cond.getCellTypeId();
                if (cellTypeId != null && !cellTypeId.equals(ConditionDAO.CELL_TYPE_ROOT_ID)) {
                    sb.append(cellTypeId).append("-");
                }
                sb.append(cond.getAnatEntityId());
                return sb.toString();
            };
    public final static Function<Condition, String> COND_NAME_GENERATOR =
            cond -> {
                StringBuilder sb = new StringBuilder();
                AnatEntity cellType = cond.getCellType();
                if (cellType != null && !cellType.getId().equals(ConditionDAO.CELL_TYPE_ROOT_ID)) {
                    sb.append(cellType.getName()).append(" in ");
                }
                sb.append(cond.getAnatEntity().getName());
                return sb.toString();
            };

    public final static EnumSet<CallService.Attribute> CALL_SERVICE_ATTRIBUTES =
            EnumSet.of(CallService.Attribute.GENE, CallService.Attribute.ANAT_ENTITY_ID,
                    CallService.Attribute.CELL_TYPE_ID);

}
