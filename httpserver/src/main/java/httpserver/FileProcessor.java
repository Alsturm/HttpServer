package httpserver;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static httpserver.Strings.SHOW_FOLDERS;


/**
 * Utility interface for methods to operate files.
 */
interface FileProcessor {
    Logger log = new Logger();

    /**
     * Reads file from disk and returns as byte array. File size should not be
     * more than 2 Gb (Integer.MAX_VALUE), or it will be truncated to 2 Gb.
     *
     * @param fileName String representation of path to the file.
     * @return byte array with raw file contents.
     */
    static byte[] readFromFile(final String fileName) {
        try {
            return Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    static void writeToFile(final Path path, final byte[] contents) {
        try {
            Files.write(path, contents);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Returns the contents of dir as the List<String>.
     *
     * @param dir Path object, represents the path to the directory to list.
     * @return List<String> with names of all files and folders in the provided dir
     * or empty ArrayList<String> if error occurs.
     */
    static List<String> directoryListing(Path dir) {
        List<String> result = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir)) {
            log.error("Provided path is not the directory");
            return result;
        }
        try {
            DirectoryStream<Path> dirList = Files.newDirectoryStream(dir);
            // Add path to html output only if it is not directory (if SHOW_FOLDERS == false)
            for (Path path : dirList) {
                if (!path.toFile().isDirectory() || SHOW_FOLDERS) result.add(path.getFileName().toString());
            }
            dirList.close();
        } catch (Exception e) {
            log.error("Exception in directoryListing method. " + e.getMessage());
            return new ArrayList<>();
        }
        return result;
    }

    static void generateIndexHtml() {
        final String body = "<html>\r\n" +
                "    <head>\r\n" +
                "        <meta charset=\"utf-8\"/>\r\n" +
                "    </head>\r\n" +
                "    <body>\r\n" +
                "        <h1>List of files:</h1>\r\n" +
                "%s" +
                "    </body>\r\n" +
                "</html>";
        StringBuffer sb = new StringBuffer();
        for (String s : directoryListing(Paths.get(Strings.PATH))) {
            String appendable = "        <a href=\"" + s + "\">" + s + "</a><br>";
            sb.append(appendable);
            sb.append(System.lineSeparator());
        }
        writeToFile(Paths.get(Strings.PATH + "/index.html"), String.format(body, sb.toString()).getBytes());
    }

    /**
     * Temporary method to conveniently print byte[]
     *
     * @param bytes byte array
     */
    static void printBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        System.out.println(sb);
    }

    /**
     * Remain here for study purposes.
     *
     * @param fileName String representation of path to the file.
     * @return byte array with raw file contents.
     */
    static byte[] getFile0(final String fileName) {
        byte[] result;
        try (FileInputStream fis = new FileInputStream(fileName);
             BufferedInputStream br = new BufferedInputStream(fis)) {
            result = new byte[(int) fis.getChannel().size()];
            int i = 0;
            int read;
            while ((read = br.read()) != -1)
                result[i++] = (byte) read;
            return result;
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

}
