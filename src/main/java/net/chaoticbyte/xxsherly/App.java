package net.chaoticbyte.xxsherly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {

    public static final String usageHelp = "xxSherly.jar [options] folder1 folder2 ...";

    public static boolean doTheColorThingy = false;
    public static boolean verbose = false;

    public static void main(String[] args) throws InterruptedException {

        // CLI

        List<File> folderList = new ArrayList<>();
        boolean displayHelp = false;

        HelpFormatter helpFormatter = new HelpFormatter();

        Options commandlineOptions = new Options();
        commandlineOptions.addOption("c", "color", false, "enable colored output");
        commandlineOptions.addOption("v", "verbose", false, "more verbose output");
        commandlineOptions.addOption("h", "help", false, "show this help message");

        try {
            CommandLine arguments = new DefaultParser().parse(commandlineOptions, args, false);
            // Get folder paths
            for (String folderArgument : arguments.getArgList()) {
                File folder = new File(folderArgument);
                if (folder.isDirectory() && folder.canRead()) folderList.add(folder);
                else System.err.println(folderArgument + " is not a folder or isn't readable.");
            }
            // Get arguments & options
            doTheColorThingy = arguments.hasOption("c");
            verbose = arguments.hasOption("v");
            displayHelp = arguments.hasOption("h");
        }
        catch (ParseException | NumberFormatException e) {
            helpFormatter.printHelp(usageHelp, commandlineOptions);
            System.err.println();
            System.err.println(e.getMessage());
            return;
        }

        if (displayHelp) {
            helpFormatter.printHelp(usageHelp, commandlineOptions);
            return;
        }

        if (folderList.size() < 1) {
            System.err.println("No valid folders specified.");
            helpFormatter.printHelp(usageHelp, commandlineOptions);
            return;
        }

        if (verbose) {
            System.out.println("Arguments:");;
            System.out.println("  Folders:  " + folderList.size());
            System.out.println("  Color:    " + doTheColorThingy);
        }

        // Find all files
        List<File> files = new ArrayList<>();
        for (File folder : folderList) {
            try (Stream<Path> stream = Files.walk(folder.toPath())) {
                List<Path> filePaths = stream
                    .filter(Files::isReadable)
                    .filter(Files::isRegularFile)
                    .filter(f -> !Files.isSymbolicLink(f))
                    .collect(Collectors.toList());
                filePaths.forEach((filePath) -> {
                    files.add(filePath.toFile());
                });
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        int nFiles = files.size();
        if (verbose) System.out.println("Files: " + nFiles);

        // Calculate Hashes

        ConcurrentHashMap<String, List<File>> fileMap = new ConcurrentHashMap<>();

        files.parallelStream().forEach(file -> {

            List<File> fileArray = new ArrayList<>();
            assert fileArray != null;
            fileArray.add(file);

            // Generate Checksum
            try {
                String checksum = FileChecksum.getChecksum(file);
                if (fileMap.containsKey(checksum)) {
                    fileArray.addAll(fileMap.get(checksum));
                    fileMap.put(checksum, fileArray);
                } else {
                    fileMap.put(checksum, fileArray);
                }
            }
            catch (IOException e) {
                System.err.println("An exception occured while processing the file " + file.getPath());
                System.err.println(e.getMessage());
            }
        });

        ArrayList<String> toRemove = new ArrayList<String>();
        for (String checksum: fileMap.keySet()) {
            if (fileMap.get(checksum).size() == 1) {
                toRemove.add(checksum);
            }
        }
        fileMap.keySet().removeAll(toRemove);

        // Now everything is finished and the Filemap (hashmap with all Dups) can be printed out in a nice view

        if (fileMap.size() > 0) {
            System.out.println();
            for (String checksum: fileMap.keySet()) {
                if (doTheColorThingy) {
                    System.out.println(
                        ConsoleColors.BLUE_BOLD + checksum
                        + ConsoleColors.CYAN_BOLD + "\t--> "
                        + ConsoleColors.GREEN_BOLD + fileMap.get(checksum)
                        + ConsoleColors.RESET);
                } else System.out.println(checksum +"\t--> " + fileMap.get(checksum));
            }
            System.out.println();
        }

        // Count redundant files and bytes

        int toBeDeleted = 0;
        long bytes = 0;
        for (String checksum: fileMap.keySet()) {
            fileMap.get(checksum).remove(0);
            for (File file: fileMap.get(checksum)) {
                if (file != null) bytes += file.length();
            }
            toBeDeleted++;
        }

        if (doTheColorThingy) {
            String color = ConsoleColors.RED_BOLD;
            if (toBeDeleted < 1) color = ConsoleColors.GREEN_BOLD;
            System.out.println(color + (bytes / 1000000.0) + " redundant MB in " + toBeDeleted + " file(s) found." + ConsoleColors.RESET);
        } else System.out.println((bytes / 1000000.0) + " redundant MB in " + toBeDeleted + " file(s) found.");
    }
}
