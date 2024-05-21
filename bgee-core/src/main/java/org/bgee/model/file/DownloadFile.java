package org.bgee.model.file;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * A file (available for download), providing information such as size and path.
 *
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.2, May 2024
 * @since   Bgee 13
 */
public abstract class DownloadFile {

    private final String path;
    private final String fileName;
    private final String title;
    private final long size;

    /**
     * @param path                  A {@code String} representing the path containing the file.
     * @param fileName              A {@code String} representing the file name. Might be {@code /}
     *                              by convention if the file represents a directory.
     * @param title                 A {@code String} representing a user friendly title for the download file.
     *                              Can be {@code null} or empty.
     * @param size                  A {@code Long} representing the file size in bytes.
     * @throws IllegalArgumentException If {@code path} or {@code fileName} are {@code null} or empty,
     *                                  or if {@code size} is less than 0.
     */
    public DownloadFile(String path, String fileName, String title, Long size) {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("Path and fileName must be provided.");
        }
        if (size < 0L) {
            throw new IllegalArgumentException("Size cannot be less than 0.");
        }
        this.path = path;
        this.fileName = fileName;
        this.title = title;
        this.size = size;
    }

    /**
     * Get the path, relative to download files root directory.
     *
     * @return A {@code String} containing the path where the actual file is found.
     */
    public String getPath() {
        return path;
    }
    /**
     * Get the file fileName.
     *
     * @return A {@code String} containing the file fileName.
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @return  A {@code String} representing a user friendly title for the download file.
     */
    public String getTitle() {
        return title;
    }
    /**
     * Gets the size.
     *
     * @return A {@code Long} representing the size of the file in bytes.
     */
    public Long getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, path, size, title);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DownloadFile other = (DownloadFile) obj;
        return Objects.equals(fileName, other.fileName) && Objects.equals(path, other.path)
                && size == other.size && Objects.equals(title, other.title);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DownloadFile [")
               .append("path=").append(path)
               .append(", fileName=").append(fileName)
               .append(", title=").append(title)
               .append(", size=").append(size)
               .append("]");
        return builder.toString();
    }
}
