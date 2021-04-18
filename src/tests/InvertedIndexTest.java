package tests;

import com.google.gson.Gson;
import configs.Config;
import inverted_index.IndexThread;
import inverted_index.InvertedIndexCreator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InvertedIndexTest {
    InvertedIndexCreator invertedIndex;
    HashMap<File, List<Integer>> testFilesMap;
    Config config = new Config();
    HashMap<String, ArrayList<String>> map = null;
    IndexThread testIndexThread = new IndexThread(null, null, 0, 0);

    @BeforeAll
    public void setUp() {
        // set files, which will be parsed
        testFilesMap = new HashMap<>();
        List<Integer> testRanges = Arrays.asList(0, 5);
        testFilesMap.put(new File(config.TEST_FILES_DIRECTORY_PATH), testRanges);

        invertedIndex = new InvertedIndexCreator(testFilesMap);
    }

    @Test
    void testIndexCreation() {
        invertedIndex.start();
        try {
            invertedIndex.join();
        } catch (InterruptedException e) {
            // if index creation fails
            assertNotEquals("", e.getMessage());
        }
    }

    @Test
    void testInvertedIndex() {
        try {
            map = new Gson().fromJson(
                new FileReader(config.JSON_FILE_PATH),
                HashMap.class
            );
        } catch (FileNotFoundException e) {
            // if file doesn't exist
            assertNotEquals("", e.getMessage());
            return;
        }

        try {
            assert map != null;
        } catch (AssertionError e) {
            assertNotEquals("", e.getMessage());
            return;
        }

        assertEquals(11, map.size());
        List<String> actual = map.get("you");
        List<String> expected = Arrays.asList(
                "tests/files/0_1.txt", "tests/files/1_1.txt",
                "tests/files/2_1.txt", "tests/files/3_1.txt"
        );
        assertEquals(4, actual.size());
        assertTrue(expected.containsAll(actual));

        assertNull( map.get("SomethingThatDoesntExist"));
    }

    @Test
    void testPrepareWord() {
        String testString = "qwerty";
        assertEquals(testString, testIndexThread.prepareWord("qwerty"));
        assertEquals(testString, testIndexThread.prepareWord(".qwerty"));
        assertEquals(testString, testIndexThread.prepareWord("    qwe  rt  y"));
        assertEquals(testString, testIndexThread.prepareWord("._--qwe16-++r_ty--"));
        assertNotEquals(testString, testIndexThread.prepareWord(".q.w. 00w00rty"));
    }

    @Test
    void testGetFileDirectory() {
        File testFile = new File(config.TEST_FILES_DIRECTORY_PATH + "0_1.txt");
        assertEquals("tests/files/0_1.txt", testIndexThread.getFileDirectory(testFile));
    }
}
