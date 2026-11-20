package org.bgee.model.topanat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;

/**
 * The format of the files holding the expression calls topAnat reads, one per species and per
 * {@link TopAnatService.CallFileType}, written and read by {@link TopAnatService}.
 * <p>
 * A call file is a gzip file made of one gzip member per developmental stage: a request, which
 * targets a single stage, only decompresses the member of that stage, while the whole file
 * remains readable by {@code zcat}.
 * <p>
 * A line of a member holds three fields separated by tabs: the ID of a stage, the ID of a gene,
 * then the conditions holding a call of that gene at that stage, separated by commas. A condition
 * is written as its index, a colon, then one letter per combination of data types, in the order
 * of the combinations of the index file: {@code N} when the combination supports no call in that
 * condition, {@code S} for a SILVER call, {@code G} for a GOLD call. The index of a condition is
 * its position among the rows of the condition file, which are sorted by condition ID, counting
 * from 0 (see {@link TopAnatService#getConditionFilePath(String, int)}): index 0 is
 * the condition of the first row after the header. The index file lists the conditions in that
 * order too. For instance, with the 7 combinations of 3 data types in the order {@code IN_SITU},
 * {@code RNA_SEQ}, {@code SC_RNA_SEQ}, {@code IN_SITU-RNA_SEQ}, {@code IN_SITU-SC_RNA_SEQ},
 * {@code SC_RNA_SEQ-RNA_SEQ} and {@code IN_SITU-SC_RNA_SEQ-RNA_SEQ} (see
 * {@link TopAnatService#dataTypeCombinations()}), the line
 * <pre>UBERON:0000104	ENSMUSG00000000001	0:NSNSNSS,12:GSNGGSG</pre>
 * tells that, at the root of the stages, the gene {@code ENSMUSG00000000001} holds calls in two
 * conditions:
 * <ul>
 * <li>in the condition of index 0, the one of the first row of the condition file, a SILVER call
 * supported by RNA-Seq: every combination including {@code RNA_SEQ} is SILVER, the other ones
 * support no call;
 * <li>in the condition of index 12, the one of its 13th row, a GOLD call supported by in situ
 * hybridization and a SILVER call supported by RNA-Seq, with no single-cell data: every
 * combination including {@code IN_SITU} is GOLD, {@code RNA_SEQ} and
 * {@code SC_RNA_SEQ-RNA_SEQ} are SILVER, and {@code SC_RNA_SEQ} alone supports no call.
 * </ul>
 * A gene being written once per stage and a condition in a few bytes, the calls of a species take
 * a small fraction of the space one row per gene, stage and condition would.
 * <p>
 * An index file, next to the call file, holds what reading a member requires: the size of
 * the call file it describes, the combinations of data types in the order of the letters,
 * the position of the member of each stage, and the IDs of the conditions in the order of their
 * indexes, which is the order of the condition IDs, the order of the condition file.
 *
 * @author  Julien Wollbrett
 * @version Bgee 16
 */
final class TopAnatCallFile {
    private final static Logger log = LogManager.getLogger(TopAnatCallFile.class.getName());

    /**
     * The extension of the call files, and the one of their index files, which replaces it.
     */
    final static String CALL_FILE_EXTENSION = ".tsv.gz";
    final static String INDEX_FILE_EXTENSION = ".index.tsv";
    /**
     * The first column of the lines of an index file, telling what the line describes.
     */
    private final static String SIZE_RECORD = "size";
    private final static String COMBINATIONS_RECORD = "combinations";
    private final static String STAGE_RECORD = "stage";
    private final static String CONDITION_RECORD = "condition";
    /**
     * The letters giving the quality of a call in a condition, one per combination of data types.
     */
    private final static char NO_CALL_LETTER = 'N';
    private final static char SILVER_LETTER = 'S';
    private final static char GOLD_LETTER = 'G';
    private final static char ENTRY_SEPARATOR = ',';
    private final static char QUALITY_SEPARATOR = ':';
    private final static int BUFFER_SIZE = 1 << 16;

    private TopAnatCallFile() {
        //Static methods and the writer only
    }

    /**
     * @param callFile  The {@code Path} of a call file.
     * @return          The {@code Path} of its index file.
     * @throws IllegalArgumentException If {@code callFile} does not have the extension of
     *                                  a call file.
     */
    static Path indexFile(Path callFile) {
        String name = callFile.getFileName().toString();
        if (!name.endsWith(CALL_FILE_EXTENSION)) {
            throw log.throwing(new IllegalArgumentException("Not a call file: " + callFile));
        }
        return callFile.resolveSibling(name.substring(0,
                name.length() - CALL_FILE_EXTENSION.length()) + INDEX_FILE_EXTENSION);
    }

    /**
     * @param quality   The {@code SummaryQuality} of a call, {@code null} when there is
     *                  no present call.
     * @return          The letter of {@code quality} in a call file.
     * @throws IllegalArgumentException If {@code quality} is a quality the files do not hold.
     */
    static char qualityLetter(SummaryQuality quality) {
        if (quality == null) {
            return NO_CALL_LETTER;
        }
        switch (quality) {
        case GOLD:
            return GOLD_LETTER;
        case SILVER:
            return SILVER_LETTER;
        default:
            throw log.throwing(new IllegalArgumentException(
                    "The call files only hold SILVER and GOLD calls: " + quality));
        }
    }
    /**
     * Append a condition to the conditions of a gene at a stage.
     *
     * @param entries           A {@code StringBuilder} holding the conditions already appended.
     * @param conditionIndex    An {@code int} that is the index of the condition.
     * @param letters           A {@code CharSequence} holding the letter of each combination of
     *                          data types, in the order of the combinations of the call file.
     */
    static void appendEntry(StringBuilder entries, int conditionIndex, CharSequence letters) {
        if (entries.length() > 0) {
            entries.append(ENTRY_SEPARATOR);
        }
        entries.append(conditionIndex).append(QUALITY_SEPARATOR).append(letters);
    }
    /**
     * @param conditionIds  A {@code List} of {@code String}s that are the IDs of conditions,
     *                      in the order of their indexes.
     * @return              A {@code Map} where keys are the IDs of the conditions, the associated
     *                      value being their index.
     * @throws IllegalArgumentException If {@code conditionIds} holds a same ID twice.
     */
    static Map<String, Integer> conditionIndexes(List<String> conditionIds) {
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < conditionIds.size(); i++) {
            if (indexes.put(conditionIds.get(i), i) != null) {
                throw log.throwing(new IllegalArgumentException(
                        "The condition " + conditionIds.get(i) + " is listed twice"));
            }
        }
        return indexes;
    }

    /**
     * Writes a call file and its index. The members are written to temporary files, one per stage,
     * concatenated into the call file once every gene has been written. The call file and its
     * index are then moved to their final path, so that an interrupted generation never leaves
     * a partial file behind that the next start would consider done.
     */
    static final class CallFileWriter {
        private final Path callFile;
        private final Path memberDirectory;
        private final List<String> combinationNames;
        private final List<String> conditionIds;
        /**
         * The members being written, by stage. Sorted, for the members to be in the order of
         * the stages in the call file.
         */
        private final Map<String, Writer> members;
        private final Map<String, Path> memberFiles;

        /**
         * @param callFile          The {@code Path} of the call file to write.
         * @param combinationNames  A {@code List} of {@code String}s that are the names of
         *                          the combinations of data types, in the order of the letters.
         * @param conditionIds      A {@code List} of {@code String}s that are the IDs of
         *                          the conditions, in the order of their indexes.
         */
        CallFileWriter(Path callFile, List<String> combinationNames, List<String> conditionIds) {
            log.traceEntry("{}, {}, {}", callFile, combinationNames, conditionIds);
            if (callFile == null || combinationNames == null || combinationNames.isEmpty() ||
                    conditionIds == null) {
                throw log.throwing(new IllegalArgumentException(
                        "A call file, combinations and conditions must be provided"));
            }
            this.callFile = callFile;
            this.memberDirectory = callFile.resolveSibling(callFile.getFileName() + ".members"
                    + TopAnatService.TMP_EXTENSION);
            this.combinationNames = List.copyOf(combinationNames);
            this.conditionIds = List.copyOf(conditionIds);
            this.members = new TreeMap<>();
            this.memberFiles = new HashMap<>();
            try {
                //Left by an interrupted generation
                deleteMembers(this.memberDirectory);
                Files.createDirectories(this.memberDirectory);
            } catch (IOException e) {
                throw log.throwing(new UncheckedIOException(
                        "Could not create the directory " + this.memberDirectory, e));
            }
            log.traceExit();
        }

        /**
         * Write the conditions of a gene at a stage. Synchronized: the gene batches are
         * propagated in parallel, into the same files.
         *
         * @param stageId   A {@code String} that is the ID of the stage.
         * @param geneId    A {@code String} that is the ID of the gene.
         * @param entries   A {@code CharSequence} holding the conditions of the gene at the stage,
         *                  see
         *                  {@link TopAnatCallFile#appendEntry(StringBuilder, int, CharSequence)}.
         */
        synchronized void write(String stageId, String geneId, CharSequence entries) {
            if (entries.length() == 0) {
                return;
            }
            try {
                Writer member = this.members.get(stageId);
                if (member == null) {
                    Path memberFile = this.memberDirectory.resolve(this.members.size() + ".gz");
                    member = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                            Files.newOutputStream(memberFile), BUFFER_SIZE),
                            StandardCharsets.UTF_8), BUFFER_SIZE);
                    this.members.put(stageId, member);
                    this.memberFiles.put(stageId, memberFile);
                }
                member.write(stageId);
                member.write(TopAnatService.COLUMN_SEPARATOR);
                member.write(geneId);
                member.write(TopAnatService.COLUMN_SEPARATOR);
                member.append(entries);
                member.write('\n');
            } catch (IOException e) {
                throw log.throwing(new UncheckedIOException(
                        "Could not write the calls of " + this.callFile, e));
            }
        }

        /**
         * Concatenate the members into the call file and write its index.
         */
        void complete() {
            log.traceEntry();
            Path tmpCallFile = TopAnatService.tmpFile(this.callFile);
            Path indexFile = indexFile(this.callFile);
            Path tmpIndexFile = TopAnatService.tmpFile(indexFile);
            List<String> stageLines = new ArrayList<>();
            try {
                for (Writer member: this.members.values()) {
                    member.close();
                }
                long offset = 0;
                try (FileChannel target = FileChannel.open(tmpCallFile, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                    for (String stageId: this.members.keySet()) {
                        Path memberFile = this.memberFiles.get(stageId);
                        long length = Files.size(memberFile);
                        try (FileChannel source = FileChannel.open(memberFile)) {
                            long transferred = 0;
                            while (transferred < length) {
                                transferred += source.transferTo(transferred,
                                        length - transferred, target);
                            }
                        }
                        stageLines.add(String.join(TopAnatService.COLUMN_SEPARATOR, STAGE_RECORD,
                                stageId, String.valueOf(offset), String.valueOf(length)));
                        offset += length;
                    }
                }
                List<String> lines = new ArrayList<>();
                lines.add(SIZE_RECORD + TopAnatService.COLUMN_SEPARATOR + offset);
                lines.add(COMBINATIONS_RECORD + TopAnatService.COLUMN_SEPARATOR
                        + String.join(TopAnatService.COLUMN_SEPARATOR, this.combinationNames));
                lines.addAll(stageLines);
                for (int i = 0; i < this.conditionIds.size(); i++) {
                    lines.add(String.join(TopAnatService.COLUMN_SEPARATOR, CONDITION_RECORD,
                            String.valueOf(i), this.conditionIds.get(i)));
                }
                Files.write(tmpIndexFile, lines, StandardCharsets.UTF_8);
                deleteMembers(this.memberDirectory);
            } catch (IOException e) {
                throw log.throwing(new UncheckedIOException(
                        "Could not write " + tmpCallFile + " and " + tmpIndexFile, e));
            }
            //The index tells its call file apart from a call file being replaced, by its size
            TopAnatService.move(tmpCallFile, this.callFile);
            TopAnatService.move(tmpIndexFile, indexFile);
            log.traceExit();
        }

        /**
         * Delete the members written, when the call file cannot be completed, rather than leave
         * them until the next generation.
         */
        void discard() {
            log.traceEntry();
            try {
                for (Writer member: this.members.values()) {
                    member.close();
                }
                deleteMembers(this.memberDirectory);
            } catch (IOException e) {
                log.catching(e);
                log.warn("Could not delete the members in {}", this.memberDirectory);
            }
            log.traceExit();
        }
    }

    private static void deleteMembers(Path memberDirectory) throws IOException {
        if (!Files.exists(memberDirectory)) {
            return;
        }
        //The files before their directory
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(memberDirectory)) {
            paths = walk.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
        for (Path path: paths) {
            Files.delete(path);
        }
    }

    /**
     * Read the calls of one stage of a call file.
     *
     * @param callFile      The {@code Path} of the call file to read.
     * @param combination   A {@code String} that is the name of the combination of data types
     *                      whose qualities are read.
     * @param goldOnly      A {@code boolean} telling whether only the GOLD calls are retained,
     *                      rather than the SILVER and GOLD ones.
     * @param stageId       A {@code String} that is the ID of the stage whose member is read.
     * @return              A {@code Stream} of {@code String[]}s holding the gene ID and
     *                      the condition ID of each retained call, empty when the file holds
     *                      no call at that stage. The member is read lazily: the {@code Stream}
     *                      must be closed.
     * @throws IOException              If the call file or its index cannot be read.
     * @throws IllegalStateException    If the index lacks {@code combination}, or does not
     *                                  describe the call file.
     */
    static Stream<String[]> readPairs(Path callFile, String combination, boolean goldOnly,
            String stageId) throws IOException {
        log.traceEntry("{}, {}, {}, {}", callFile, combination, goldOnly, stageId);
        Index index = Index.read(indexFile(callFile));
        if (Files.size(callFile) != index.size) {
            throw log.throwing(new IllegalStateException("The call file " + callFile
                    + " does not match its index, it may be being replaced"));
        }
        int position = index.combinations.indexOf(combination);
        if (position < 0) {
            throw log.throwing(new IllegalStateException("No combination " + combination
                    + " in the call file " + callFile));
        }
        long[] member = index.stages.get(stageId);
        if (member == null) {
            return log.traceExit(Stream.empty());
        }
        int letterCount = index.combinations.size();
        FileChannel channel = FileChannel.open(callFile);
        try {
            channel.position(member[0]);
            //Closing the reader closes the channel
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new BoundedInputStream(Channels.newInputStream(channel),
                            member[1]), BUFFER_SIZE), StandardCharsets.UTF_8), BUFFER_SIZE);
            return log.traceExit(reader.lines()
                    .onClose(() -> {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            throw log.throwing(new UncheckedIOException(e));
                        }
                    })
                    .flatMap(line -> pairs(line, position, letterCount, goldOnly,
                            index.conditionIds).stream()));
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }
    /**
     * @return  The gene and condition of each retained call of a line of a member.
     */
    private static List<String[]> pairs(String line, int position, int letterCount,
            boolean goldOnly, String[] conditionIds) {
        int geneStart = line.indexOf(TopAnatService.COLUMN_SEPARATOR) + 1;
        int entriesStart = line.indexOf(TopAnatService.COLUMN_SEPARATOR, geneStart) + 1;
        if (geneStart == 0 || entriesStart == 0) {
            throw log.throwing(new IllegalStateException("Incorrect line of a call file: " + line));
        }
        String geneId = line.substring(geneStart, entriesStart - 1);
        List<String[]> pairs = new ArrayList<>();
        int entryStart = entriesStart;
        while (entryStart < line.length()) {
            int separator = line.indexOf(QUALITY_SEPARATOR, entryStart);
            if (separator < 0) {
                throw log.throwing(new IllegalStateException(
                        "Incorrect line of a call file: " + line));
            }
            char letter = line.charAt(separator + 1 + position);
            if (letter == GOLD_LETTER || !goldOnly && letter == SILVER_LETTER) {
                pairs.add(new String[] {geneId,
                        conditionIds[Integer.parseInt(line, entryStart, separator, 10)]});
            }
            //The letters of every combination, then the separator of the next entry
            entryStart = separator + 1 + letterCount + 1;
        }
        return pairs;
    }

    /**
     * The content of an index file.
     */
    private static final class Index {
        private final long size;
        private final List<String> combinations;
        /**
         * The offset and the length of the member of each stage.
         */
        private final Map<String, long[]> stages;
        private final String[] conditionIds;

        private Index(long size, List<String> combinations, Map<String, long[]> stages,
                String[] conditionIds) {
            this.size = size;
            this.combinations = combinations;
            this.stages = stages;
            this.conditionIds = conditionIds;
        }

        private static Index read(Path indexFile) throws IOException {
            Long size = null;
            List<String> combinations = null;
            Map<String, long[]> stages = new HashMap<>();
            List<String> conditionIds = new ArrayList<>();
            for (String line: Files.readAllLines(indexFile, StandardCharsets.UTF_8)) {
                String[] values = line.split(TopAnatService.COLUMN_SEPARATOR, -1);
                switch (values[0]) {
                case SIZE_RECORD:
                    size = Long.parseLong(values[1]);
                    break;
                case COMBINATIONS_RECORD:
                    combinations = List.of(values).subList(1, values.length);
                    break;
                case STAGE_RECORD:
                    stages.put(values[1], new long[] {Long.parseLong(values[2]),
                            Long.parseLong(values[3])});
                    break;
                case CONDITION_RECORD:
                    //Written in the order of their indexes
                    if (Integer.parseInt(values[1]) != conditionIds.size()) {
                        throw log.throwing(new IllegalStateException(
                                "Incorrect condition indexes in " + indexFile));
                    }
                    conditionIds.add(values[2]);
                    break;
                default:
                    throw log.throwing(new IllegalStateException(
                            "Incorrect line in " + indexFile + ": " + line));
                }
            }
            if (size == null || combinations == null) {
                throw log.throwing(new IllegalStateException("Incomplete index: " + indexFile));
            }
            return new Index(size, combinations, Collections.unmodifiableMap(stages),
                    conditionIds.toArray(String[]::new));
        }
    }

    /**
     * Reads at most a given number of bytes, the length of the member of a stage.
     */
    private static final class BoundedInputStream extends InputStream {
        private final InputStream in;
        private long remaining;

        private BoundedInputStream(InputStream in, long length) {
            this.in = in;
            this.remaining = length;
        }
        @Override
        public int read() throws IOException {
            if (this.remaining <= 0) {
                return -1;
            }
            int b = this.in.read();
            if (b >= 0) {
                this.remaining--;
            }
            return b;
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (this.remaining <= 0) {
                return -1;
            }
            int read = this.in.read(b, off, (int) Math.min(len, this.remaining));
            if (read > 0) {
                this.remaining -= read;
            }
            return read;
        }
        @Override
        public void close() throws IOException {
            this.in.close();
        }
    }
}
