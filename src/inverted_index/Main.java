package inverted_index;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static int NUMBER_THREADS = 2;

    public static void main(String[] args) throws InterruptedException {
        IndexThread[] ThreadArray = new IndexThread[NUMBER_THREADS];
        // add ranges of files for each directory
        /* filesMap structure: {
              file1: [start_index, end_index],
              file2: [start_index, end_index]
        }  */
        HashMap<File, List<Integer>> filesMap = new HashMap<>();
//        List<Integer> fileRanges1 = Arrays.asList(8250, 8500);
//        List<Integer> fileRanges2 = Arrays.asList(33000, 34000);
//        List<Integer> fileRanges1 = Arrays.asList (0, 3);
        List<Integer> fileRanges2 = Arrays.asList(0, 6);

//        filesMap.put(new File("C:\\Users\\wykyee\\Desktop\\datasets\\aclImdb\\test\\neg"), fileRanges1);
//        filesMap.put(new File("C:\\Users\\wykyee\\Desktop\\datasets\\aclImdb\\train\\qqq"), fileRanges1);
//        filesMap.put(new File("C:\\Users\\wykyee\\Desktop\\datasets\\aclImdb\\test\\pos"), fileRanges1);
//        filesMap.put(new File("C:\\Users\\wykyee\\Desktop\\datasets\\aclImdb\\train\\neg"), fileRanges1);
//        filesMap.put(new File("C:\\Users\\wykyee\\Desktop\\datasets\\aclImdb\\train\\neg"), fileRanges1);
        filesMap.put(new File("C:\\Users\\wykyee\\Desktop\\datasets\\aclImdb\\train\\unsup"), fileRanges2);

        ConcurrentHashMap<String, Set<String>> resultMap = new  ConcurrentHashMap<>();

        // initializing and starting threads
        for (int i = 0; i < NUMBER_THREADS; i++) {
            ThreadArray[i] = new IndexThread(filesMap, resultMap, i, NUMBER_THREADS);
            ThreadArray[i].start();
        }
        Thread.sleep(2000);
        resultMap.forEach((key, value) -> System.out.println(key + " " + value));
    }
}
