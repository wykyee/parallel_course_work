package inverted_index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class IndexThread extends Thread {
    ConcurrentHashMap<String, Set<String>> indexMap;
    HashMap<File, List<Integer>> filesMap;

    public IndexThread(HashMap<File, List<Integer>> filesMap,
                       ConcurrentHashMap<String, Set<String>> map) {
        this.filesMap = filesMap;
        this.indexMap = map;
    }

    @Override
    public void run() {
        for (File key: filesMap.keySet()) {
            List<File> filesList = getFilesList(key);
            for (File file: filesList) {
                try {
                    Scanner fileData = new Scanner(file);
                    while (fileData.hasNext()) {
                        String word  = prepareWord(fileData.next());
                        indexMap.compute(word, (k, v) -> {
                            Set<String> values = v;
                            // if there is no such key, creates new set for it
                            if (values == null) {
                                values = new HashSet<>();
                            }
                            // adds value (file path) to key's (word) set
                            values.add(getFileDirectory(file));
                            return values;
                        });
                    }
                } catch (FileNotFoundException ignored) {}
            }
        }
    }

    public List<File> getFilesList(File directory) {
        /* Returns list of all files in certain range in directory */
        List<File> filesList = new ArrayList<>();
        Integer start = filesMap.get(directory).get(0),
                end = filesMap.get(directory).get(1);
        for (int i = start; i < end ; i++) {
            // add file with certain index
            String iAsString = String.valueOf(i);
            FilenameFilter filter = (dir, name) -> {
                int underscoreIndex = name.indexOf("_");
                return name.substring(0, underscoreIndex).equals(iAsString);
            };
            filesList.addAll(Arrays.asList(Objects.requireNonNull(directory.listFiles(filter))));
        }
        return filesList;
    }

    private String getFileDirectory(File file) {
        /* Return file path as dir/dir/file. It will be saved in indexMap
           and needs less place to store than File object */
        return file.getParentFile().getParentFile()
                .getName() + "/" + file.getParentFile()
                .getName() + "/" + file.getName();
    }

    private String prepareWord(String word) {
        /* Normalizes word to add it in index map.
           All words will be lowercase, without non letters symbols */
        return word.toLowerCase().replaceAll("[^a-zA-Z]", "");
    }
}
