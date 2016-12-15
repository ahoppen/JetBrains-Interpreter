package testDriver;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class attempts to mimic LLVM's lit.py tool, scanning a folder for test files and executing
 * them.
 *
 * It looks for files with an extension mentioned in <code>testFileExtensions</code> in the current
 * working directory whose name does not match a name in <code>exclude</code>.
 *
 * In each of these files, the test driver looks for a line containing the <code>RUN:</code> keyword
 * and executes the command specified after applying all substitutions specified in
 * <code>commandSubstitutions</code> and replacing <code>%s</code> by the test file path.
 */
public class TestDriver {

    /** File extensions of test files to include without the dot */
    private static Set<String> testFileExtensions = new HashSet<>();
    /** File names that should not be tested */
    private static Set<String> exclude = new HashSet<>();
    /** Substitutions to perform in the RUN command */
    private static Map<String, String> commandSubstitutions = new HashMap<>();

    static {
        // Setup the config parameters
        testFileExtensions.add("ml");
        exclude.add("devtest.ml");

        commandSubstitutions.put("%lexer", "java -ea -cp '%binDir' frontend.CommandLineDriver -lex");
        commandSubstitutions.put("%verifyLexer", "java -ea -cp '%binDir' frontend.CommandLineDriver -lex -verify '%s'");

        commandSubstitutions.put("%parser", "java -ea -cp '%binDir' frontend.CommandLineDriver -parse");
        commandSubstitutions.put("%verifyParser", "java -ea -cp '%binDir' frontend.CommandLineDriver -parse -verify '%s'");

        commandSubstitutions.put("%typeChecker", "java -ea -cp '%binDir' frontend.CommandLineDriver -typeCheck");
        commandSubstitutions.put("%verifyTypeChecker", "java -ea -cp '%binDir' frontend.CommandLineDriver -typeCheck -verify '%s'");

        commandSubstitutions.put("%interpreter", "java -ea -cp '%binDir' frontend.CommandLineDriver -evaluate");
        commandSubstitutions.put("%verifyInterpreter", "java -ea -cp '%binDir' frontend.CommandLineDriver -evaluate -verify '%s'");

        File f = new File(System.getProperty("user.dir") + "/out/production/Interpreter");

        try {
            commandSubstitutions.put("%binDir", f.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<File> testFiles = getTestFiles();

        boolean[] errorOccurred = new boolean[] {false};

        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[numberOfThreads];

        for (int j = 0; j < numberOfThreads; j++) {
            final int threadId = j;
            threads[threadId] = new Thread(() -> {
            for (int i = 0; i < testFiles.size(); i++) {
                if ((i % numberOfThreads) == threadId) {
                    File file = testFiles.get(i);
                    try {
                        errorOccurred[0] |= testFile(file);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            });
            threads[threadId].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

        System.exit(errorOccurred[0] ? 1 : 0);
    }

    /**
     * Looks for <code>RUN</code> lines in the given test file and executes them.
     * @param file The file to test
     * @return <code>false</code> if the tests succeeded, <code>true</code> otherwise
     * @throws IOException When the file could not be read
     * @throws InterruptedException When the thread running the tests has been interrupted
     */
    private static boolean testFile(@NotNull File file) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            // Find RUN-lines in the test script
            Pattern pattern = Pattern.compile("RUN:\\s*(.*)$*");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String command = matcher.group(1);
                command = performSubstitutions(command, commandSubstitutions);
                command = command.replaceAll("%s", file.getAbsolutePath());

                List<String> arguments = splitCommandLineArguments(command);

                Process process = new ProcessBuilder().command(arguments).start();

                if (process.waitFor() != 0) {
                    BufferedReader stderr = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()));

                    System.err.println("========================================");
                    System.err.println("Error in file: " + file.getAbsolutePath());
                    System.err.println("Executed command: " + command);
                    System.err.println("");

                    String errLine;

                    while ((errLine = stderr.readLine()) != null) {
                        System.err.println(errLine);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return All test files in the current working directory
     */
    private static List<File> getTestFiles() {
        List<File> testFiles = new ArrayList<>();
        gatherTestFiles(new File(System.getProperty("user.dir")), testFiles);
        return testFiles;
    }

    /**
     * Add all the test files in the given directory to the testFiles list recursively
     * @param baseDir The directory to scan for test files
     * @param testFiles Out parameter: the list to which the test files should be added
     */
    private static void gatherTestFiles(@NotNull File baseDir, @NotNull List<File> testFiles) {
        assert baseDir.isDirectory();
        File[] subFiles = baseDir.listFiles();
        assert subFiles != null;
        for (File file : subFiles) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (exclude.contains(fileName)) {
                    continue;
                }

                int extensionStart = file.getName().lastIndexOf('.');
                if (extensionStart != 1) {
                    String extension = fileName.substring(extensionStart + 1);
                    if (testFileExtensions.contains(extension)) {
                        testFiles.add(file);
                    }
                }
            } else if (file.isDirectory()) {
                gatherTestFiles(file, testFiles);
            }
        }
    }

    /**
     * Perform all the given substitutions on the given string
     * @param str The string in which the substitutions should be performed
     * @param subst The substitutions to perform
     * @return The substituted string
     */
    private static String performSubstitutions(String str, Map<String, String> subst) {
        boolean changesPerformed;
        do {
            changesPerformed = false;
            for (Map.Entry<String, String> entry : subst.entrySet()) {
                if (str.contains(entry.getKey())) {
                    str = str.replace(entry.getKey(), entry.getValue());
                    changesPerformed = true;
                }
            }
        } while (changesPerformed);
        return str;
    }

    /**
     * Split a terminal command into its arguments, keeping quoted content as one parameter
     * @param command The command to split
     * @return The arguments of the command as a list
     */
    private static List<String> splitCommandLineArguments(@NotNull String command) {
        List<String> list = new ArrayList<>();
        Matcher matcher = Pattern.compile("([^\"']\\S*|\".+?\"|\'.+?\')\\s*").matcher(command);
        while (matcher.find()) {
            String arg = matcher.group(1);
            if (arg.charAt(0) == '\"' || arg.charAt(0) == '\'') {
                // Remove quotes at the beginning and end
                arg = arg.substring(1, arg.length() - 1);
            }
            list.add(arg);
        }
        return list;
    }
}
