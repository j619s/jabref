package org.jabref.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.Importer;
import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.importer.fileformat.BibtexParser;
import org.jabref.model.entry.BibEntry;

import org.junit.jupiter.api.Assertions;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * TODO: Make this a "public" class to enable jablib consumers benefitting from it, too.
 */
public class BibEntryAssert {

    /**
     * Reads a single entry from the resource using `getResourceAsStream` from the given class. The resource has to
     * contain a single entry
     *
     * @param clazz        the class where to call `getResourceAsStream`
     * @param resourceName the resource to read
     * @param entry        the entry to compare with
     */
    public static void assertEquals(Class<?> clazz, String resourceName, BibEntry entry)
            throws IOException {
        assertNotNull(clazz);
        assertNotNull(resourceName);
        assertNotNull(entry);
        try (InputStream shouldBeIs = clazz.getResourceAsStream(resourceName)) {
            BibEntryAssert.assertEquals(shouldBeIs, entry);
        }
    }

    /**
     * Reads a single entry from the resource using `getResourceAsStream` from the given class. The resource has to
     * contain a single entry
     *
     * @param clazz        the class where to call `getResourceAsStream`
     * @param resourceName the resource to read
     * @param asIsEntries  a list containing a single entry to compare with
     */
    public static void assertEquals(Class<?> clazz, String resourceName, List<BibEntry> asIsEntries)
            throws IOException {
        assertNotNull(clazz);
        assertNotNull(resourceName);
        assertNotNull(asIsEntries);
        try (InputStream shouldBeIs = clazz.getResourceAsStream(resourceName)) {
            BibEntryAssert.assertEquals(shouldBeIs, asIsEntries);
        }
    }

    private static List<BibEntry> getListFromInputStream(InputStream is) throws IOException {
        ParserResult result;
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            // Option A: Remove mock
            // Option B: copy class completely -- chosen option in DevCall 2025-04-21
            // Option C: create separate test module
            // Option D: Add parameter (passing through several methods)
            // Option E: Add gradle test fixtures - https://blog.cronn.de/en/testing/java/gradle/2023/04/20/gradle-testfixtures.html
            BibtexParser parser = new BibtexParser(mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS));
            result = parser.parse(reader);
        }
        assertNotNull(result);
        assertNotNull(result.getDatabase());
        assertNotNull(result.getDatabase().getEntries());
        return result.getDatabase().getEntries();
    }

    /**
     * Reads a bibtex database from the given InputStream. The list is compared with the given list.
     *
     * @param expectedInputStream the inputStream reading the entry from
     * @param actualEntries       a list containing a single entry to compare with
     */
    public static void assertEquals(InputStream expectedInputStream, List<BibEntry> actualEntries)
            throws IOException {
        assertNotNull(expectedInputStream);
        assertNotNull(actualEntries);
        // explicit reference of Assertions is needed here to disambiguate from the methods defined by this class
        List<BibEntry> expectedEntries = getListFromInputStream(expectedInputStream);
        Assertions.assertEquals(expectedEntries, actualEntries);
    }

    public static void assertEquals(List<BibEntry> expectedEntries, InputStream actualInputStream)
            throws IOException {
        assertNotNull(actualInputStream);
        assertNotNull(expectedEntries);
        // explicit reference of Assertions is needed here to disambiguate from the methods defined by this class
        Assertions.assertEquals(expectedEntries, getListFromInputStream(actualInputStream));
    }

    /**
     * Reads a bibtex database from the given InputStream. The result has to contain a single BibEntry. This entry is
     * compared to the given entry
     *
     * @param expected the inputStream reading the entry from
     * @param actual   the entry to compare with
     */
    public static void assertEquals(InputStream expected, BibEntry actual)
            throws IOException {
        assertEquals(expected, List.of(actual));
    }

    /**
     * Compares two InputStreams. For each InputStream a list will be created. expectedIs is read directly, actualIs is
     * filtered through importer to convert to a list of BibEntries.
     *
     * @param expectedIs   A BibtexImporter InputStream.
     * @param fileToImport The path to the file to be imported.
     * @param importer     The fileformat you want to use to read the passed file to get the list of expected
     *                     BibEntries
     */
    public static void assertEquals(InputStream expectedIs, Path fileToImport, Importer importer)
            throws IOException {
        assertEquals(getListFromInputStream(expectedIs), fileToImport, importer);
    }

    public static void assertEquals(InputStream expectedIs, URL fileToImport, Importer importer)
            throws URISyntaxException, IOException {
        assertEquals(expectedIs, Path.of(fileToImport.toURI()), importer);
    }

    /**
     * Compares a list of BibEntries to an InputStream. actualIs is filtered through importerForActualIs to convert to a
     * list of BibEntries.
     *
     * @param expected     A BibtexImporter InputStream.
     * @param fileToImport The path to the file to be imported.
     * @param importer     The fileformat you want to use to read the passed file to get the list of expected
     *                     BibEntries
     */
    public static void assertEquals(List<BibEntry> expected, Path fileToImport, Importer importer)
            throws IOException {
        List<BibEntry> actualEntries = importer.importDatabase(fileToImport)
                                               .getDatabase().getEntries();
        // explicit reference of Assertions is needed here to disambiguate from the methods defined by this class
        Assertions.assertEquals(expected, actualEntries);
    }

    public static void assertEquals(List<BibEntry> expected, URL fileToImport, Importer importer)
            throws URISyntaxException, IOException {
        assertEquals(expected, Path.of(fileToImport.toURI()), importer);
    }
}
