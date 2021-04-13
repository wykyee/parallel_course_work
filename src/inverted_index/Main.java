package inverted_index;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import configs.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final Config config = new Config();

    public static void main(String[] args) {
        IndexThread[] ThreadArray = new IndexThread[config.NUMBER_THREADS];
        ConcurrentHashMap<String, Set<String>> resultMap = new ConcurrentHashMap<>();

        HashMap<File, List<Integer>> filesMap = buildFilesMap();

        long startTime = System.currentTimeMillis();;
        // initializing and starting threads
        for (int i = 0; i < config.NUMBER_THREADS; i++) {
            ThreadArray[i] = new IndexThread(filesMap, resultMap, i, config.NUMBER_THREADS);
            ThreadArray[i].start();
        }
        for (int i = 0; i < config.NUMBER_THREADS; i++) {
            try {
                ThreadArray[i].join();
            } catch (InterruptedException e) {
                System.out.println("Error with thread-" + i);
                e.printStackTrace();
            }
        }
        System.out.println("Ended in " + (System.currentTimeMillis() - startTime) / 1000 + " s");
        writeResultInFile(resultMap);
    }

    public static HashMap<File, List<Integer>> buildFilesMap() {
        /* filesMap structure: {
              file1: [start_index, end_index],
              file2: [start_index, end_index]
        }  */
        HashMap<File, List<Integer>> filesMap = new HashMap<>();

        // add ranges of files for each directory
//        List<Integer> fileRanges1 = Arrays.asList(8250, 8500);
//        List<Integer> fileRanges2 = Arrays.asList(33000, 34000);
        List<Integer> fileRanges1 = Arrays.asList (0, 5);
        List<Integer> fileRanges2 = Arrays.asList(0, 6);

        filesMap.put(new File(config.FILES_DIRECTORY_PATH + "\\test\\neg"), fileRanges1);
        filesMap.put(new File(config.FILES_DIRECTORY_PATH + "\\train\\qqq"), fileRanges1);
        filesMap.put(new File(config.FILES_DIRECTORY_PATH + "\\test\\pos"), fileRanges1);
        filesMap.put(new File(config.FILES_DIRECTORY_PATH + "\\train\\neg"), fileRanges1);
        filesMap.put(new File(config.FILES_DIRECTORY_PATH + "\\train\\neg"), fileRanges1);
        filesMap.put(new File(config.FILES_DIRECTORY_PATH + "\\train\\unsup"), fileRanges2);

        return filesMap;
    }

    public static void writeResultInFile(ConcurrentHashMap<String, Set<String>> resultMap) {
        /* Writes result indexes map in .json file */
        File file = new File(config.JSON_FILE_PATH);
        try {
            if (file.createNewFile()) {
                System.out.println("New file created");
            }
            FileWriter writer = new FileWriter(config.JSON_FILE_PATH);
            // serialize hashmap to json object
            JsonObject json = new Gson().toJsonTree(resultMap).getAsJsonObject();

            writer.write(String.valueOf(json));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
