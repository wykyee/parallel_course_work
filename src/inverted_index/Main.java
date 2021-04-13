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
        // add ranges of files for each directory
        /* filesMap structure: {
              file1: [start_index, end_index],
              file2: [start_index, end_index]
        }  */
        HashMap<File, List<Integer>> filesMap = new HashMap<>();
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

        ConcurrentHashMap<String, Set<String>> resultMap = new  ConcurrentHashMap<>();

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
        File file = new File(config.JSON_FILE_PATH);
        try {
            if (file.createNewFile()) {
                System.out.println("New file created");
            }
            // write hash map in .json file
            FileWriter writer = new FileWriter(config.JSON_FILE_PATH);
            JsonObject json = new Gson().toJsonTree(resultMap).getAsJsonObject();

            writer.write(String.valueOf(json));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
