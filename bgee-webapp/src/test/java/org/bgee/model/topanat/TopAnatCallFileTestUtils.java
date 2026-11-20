package org.bgee.model.topanat;

import java.nio.file.Path;
import java.util.List;

/**
 * Writes call files of topAnat for the tests of bgee-webapp. In the package of
 * {@code TopAnatCallFile}, so that the tests can use its writer, which is not part of the API
 * of bgee-core.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
public class TopAnatCallFileTestUtils {

    /**
     * Write a call file and its index.
     *
     * @param callFile          The {@code Path} of the call file to write, see
     *                          {@link TopAnatService#getCallFilePath(String,
     *                          TopAnatService.CallFileType, int)}.
     * @param combinationNames  A {@code List} of {@code String}s that are the names of
     *                          the combinations of data types, in the order of the letters.
     * @param conditionIds      A {@code List} of {@code String}s that are the IDs of
     *                          the conditions, in the order of their indexes.
     * @param rows              A {@code List} of {@code String[]}s holding each a stage ID,
     *                          a gene ID, and the conditions of that gene at that stage as
     *                          written in a call file, for instance {@code 0:G,2:S}.
     */
    public static void writeCallFile(Path callFile, List<String> combinationNames,
            List<String> conditionIds, List<String[]> rows) {
        TopAnatCallFile.CallFileWriter writer = new TopAnatCallFile.CallFileWriter(callFile,
                combinationNames, conditionIds);
        for (String[] row: rows) {
            writer.write(row[0], row[1], row[2]);
        }
        writer.complete();
    }
}
