import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * An executable that counts the words in a files and prints out the counts in
 * descending order.
 */
public class WordCount {
    private static FileWordReader      fileWordReader;
    private static DataCounter<String> wordCounter;

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: [-b | -a | -h] [-frequency | -num_unique] <filename>\n");
            System.out.println("-b - Use an Unbalanced BST");
            System.out.println("-a - Use an AVL Tree");
            System.out.println("-h - Use a Hashtable\n");
            System.out.println("-frequency - Print all the word/frequency pairs, " +
                               "ordered by frequency, and then by the words in" +
                               "lexicographic order.");
            System.out.println("-num_unique - Print the number of unique words in the document. " +
                               "This is the total number of distinct (different) words in the document. " +
                               "Words that appear more than once are only counted as a single word for " +
                               "this statistic");
            return;
        }

        switch(args[0]) {
            case "-b":
                wordCounter = new BinarySearchTree<>();
                break;
            case "-a":
                wordCounter = new AVLTree<>();
                break;
            case "-h":
                wordCounter = new HashTable();
                break;
            default:
                System.out.println("Invalid choice for first argument");
                return;
        }

        try {
            fileWordReader = new FileWordReader(args[2]);
        } catch(IOException e) {
            System.out.println("The file \"" + args[2] + "\" does not exist");
            return;
        }

        try {
            switch(args[1]) {
                case "-frequency":
                    countWordFrequencies();
                    break;
                case "-num_unique":
                    countUniqueWords();
                    break;
                default:
                    System.out.println("Invalid choice for second argument");
                    break;
            }
        } catch(IOException e) {
            System.out.println("An error occurred when parsing the file!:");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Counts the words in a document.
     *
     * @return An array of data couns
     * @throws IOException Thrown if there's an exception wile reading
     */
    private static DataCount<String>[] countWords() throws IOException {
        String word;
        while((word = fileWordReader.nextWord()) != null) {
            wordCounter.incCount(word);
        }

        return wordCounter.getCounts();
    }

    /**
     * Prints the word counts given an array of data counts.
     *
     * @param dataCounts Data counts
     * @param <E>        Some type
     */
    private static <E> void printWordCounts(DataCount<E>[] dataCounts) {
        Arrays.stream(dataCounts)
              .forEach(count -> {
                  System.out.format("%d %s\n", count.count, count.data);
              });
    }

    /**
     * Counts the word frequencies in a document and lists them
     * first by frequency, then lexicographically.
     *
     * @throws IOException Thrown if there's an exception while reading the file
     */
    private static void countWordFrequencies() throws IOException {
        DataCount<String>[] dataCounts = countWords();

        sort(dataCounts, (count1, count2) -> count2.count - count1.count);

        System.out.println("Ordered by Frequency:");
        printWordCounts(dataCounts);

        sort(dataCounts, (count1, count2) -> count1.data.compareTo(count2.data));

        System.out.println("\nOrdered Lexicographically:");
        printWordCounts(dataCounts);
    }

    /**
     * Prints the number of unique words in a document.
     *
     * @throws IOException Thrown if there's an exception while reading the file
     */
    private static void countUniqueWords() throws IOException {
        System.out.println("Unique words: " + countWords().length);
    }

    /**
     * Implementation of merge sort algorithm. Sorts an array of data counts
     * using a comparator.
     *
     * @param dataCounts The array of data counts
     * @param comparator The comparator to compare data counts
     * @param <E>        Some type
     */
    private static <E> void sort(DataCount<E>[] dataCounts, Comparator<DataCount<E>> comparator) {
        if(dataCounts.length > 1) {
            int mid = dataCounts.length / 2;
            DataCount<E>[] left = Arrays.copyOfRange(dataCounts, 0, mid);
            DataCount<E>[] right = Arrays.copyOfRange(dataCounts, mid, dataCounts.length);

            sort(left, comparator);
            sort(right, comparator);
            merge(dataCounts, left, right, comparator);
        }
    }

    /**
     * Merges the left and right data counts back into the original array.
     * It's assumed that {@code left + right = dataCounts}.
     *
     * @param dataCounts The original array
     * @param left       The left side of the original array
     * @param right      The right side of the original array
     * @param comparator The comparator to compare data counts
     * @param <E>        Some type
     */
    private static <E> void merge(DataCount<E>[] dataCounts,
                                  DataCount<E>[] left, DataCount<E>[] right,
                                  Comparator<DataCount<E>> comparator) {
        int i = 0, j = 0;
        for(int k = 0; k < dataCounts.length; k++) {
            if(i < left.length && j < right.length) {
                if(comparator.compare(left[i], right[j]) <= 0) {
                    dataCounts[k] = left[i++];
                } else {
                    dataCounts[k] = right[j++];
                }
            } else if(i < left.length) {
                dataCounts[k] = left[i++];
            } else if(j < right.length) {
                dataCounts[k] = right[j++];
            }
        }
    }
}
