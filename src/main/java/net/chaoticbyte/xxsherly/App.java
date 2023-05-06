package net.chaoticbyte.xxsherly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {

    public static final String usageHelp = "xxSherly.jar [options] folder1 folder2 ...";

    public static int completedThreads = 0;
    public static int progress = 0;
    public static HashMap<String, List<File>> fileMap = new HashMap<>();

    public static boolean doTheColorThingy = false;

    public static void main(String[] args) throws InterruptedException {

        // Arguments
        List<File> folderList = new ArrayList<>();
        boolean showProgress = false;
        boolean deleteDups = false;
        boolean verbose = false;
        boolean noInput = false;
        boolean displayHelp = false;
        int requestedThreads = 0;

        // CLI

        HelpFormatter helpFormatter = new HelpFormatter();

        Options commandlineOptions = new Options();
        commandlineOptions.addOption("c", "color", false, "enable colored output");
        commandlineOptions.addOption("t", "threads", true, "override default thread number (defaults to the number of cores)");
        commandlineOptions.addOption("p", "progress", false, "enable progress indicator");
        commandlineOptions.addOption("d", "delete", false, "delete all dups except one, without asking first");
        commandlineOptions.addOption("n", "noinput", false, "skip all user input");
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
            showProgress = arguments.hasOption("p");
            deleteDups = arguments.hasOption("d");
            verbose = arguments.hasOption("v");
            noInput = arguments.hasOption("n");
            displayHelp = arguments.hasOption("h");
            requestedThreads = Integer.parseInt(arguments.getOptionValue("t", "0"));
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
            System.out.println("  Delete:   " + deleteDups);
            System.out.println("  Progress: " + showProgress);
        }

        // Calculations for multithreading
        // The number of Cores or better said Threads that can be used
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int nThreads = availableProcessors;
        if (requestedThreads > 0) nThreads = requestedThreads;
        if (verbose) System.out.println("Threads: " + nThreads);

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

        // Every Thread that is going to be started gets a range of files
        // They are seperated and are called sections
        int sections = nFiles / nThreads;
        for (int i = 1; i <= nThreads; i++) {
            List<File> sectionedList = new ArrayList<>();
            // Here the different Threads are being started
            // Usually the separation gives the first threads the same number of files to be working on and the last one is given all the files that could not be separetated
            if (i == nThreads) for (int x = (sections * i) - (sections); x < nFiles; x++) {
                sectionedList.add(files.get(x));
            } else for (int x = (sections * i) - (sections); x < (sections * i); x++) {
                sectionedList.add(files.get(x));
            }
            // Start Multithreading
            // sectionedList gives the thread their Assigned Part of Files
            ThreadedCompare threadedCompare = new ThreadedCompare(sectionedList);
            threadedCompare.start();
        }

        // This updates if necessary the Progress bar and checks for Finished threads
        while (completedThreads < nThreads) {
            TimeUnit.MILLISECONDS.sleep(250);
            if (showProgress && doTheColorThingy) {
                System.out.print(ConsoleColors.BLUE_BOLD + "Progress: " + ConsoleColors.GREEN_BOLD + progress + " / " + nFiles + " | " + (progress * 100 / nFiles) + "%" + ConsoleColors.RESET + "\r");
            } else if (showProgress) {
                System.out.print("Progress: " + progress + " / " + nFiles + " | " + (progress * 100 / nFiles) + "%" + "\r");
            }
        }

        ArrayList<String> toRemove = new ArrayList<String>();
        for (String checksum: fileMap.keySet()) {
            if (App.fileMap.get(checksum).size() == 1) {
                toRemove.add(checksum);
            }
        }
        fileMap.keySet().removeAll(toRemove);

        // Now everything is finished and the Filemap (hashmap with all Dups) can be printed out in a nice view

        if (fileMap.size() > 0) System.out.println();
        for (String checksum: fileMap.keySet()) {
            if (doTheColorThingy) {
                System.out.println(
                    ConsoleColors.BLUE_BOLD + checksum
                    + ConsoleColors.CYAN_BOLD + "\t--> "
                    + ConsoleColors.GREEN_BOLD + fileMap.get(checksum)
                    + ConsoleColors.RESET);
            } else System.out.println(checksum +"\t--> " + fileMap.get(checksum));
        }
        if (fileMap.size() > 0) System.out.println();

        List<File> toBeDeleted = new ArrayList<>();
        long bytes = 0;
        for (String checksum: fileMap.keySet()) {
            App.fileMap.get(checksum).remove(0);
            for (File file: App.fileMap.get(checksum)) {
                if (file != null) bytes += file.length();
            }
            toBeDeleted.addAll(App.fileMap.get(checksum));
        }

        if (doTheColorThingy) {
            String color = ConsoleColors.RED_BOLD;
            if (fileMap.size() < 1) color = ConsoleColors.GREEN_BOLD;
            System.out.println(color + (bytes / 1000000.0) + " unnecessary MB in " + toBeDeleted.size() + " file(s) found." + ConsoleColors.RESET);
        } else System.out.println((bytes / 1000000.0) + " unnecessary MB in " + toBeDeleted.size() + " file(s) found.");

        // Don't go further if there is nothing to delete
        if (fileMap.size() < 1) return;

        if (deleteDups) {
            System.out.println();
            delete(toBeDeleted);
        } else if (!noInput) {
            // Ask if the user wants to delete the file
            Scanner input = new Scanner(System.in);
            while (true) {
                if (doTheColorThingy) System.out.print(ConsoleColors.RED_BOLD + "Do you want to delete them? [y/n] " + ConsoleColors.RESET);
                else System.out.print("Do you want to delete them? [y/n] ");
                String answer = input.next();
                if (answer.toLowerCase().contains("y")) {
                    System.out.println();
                    delete(toBeDeleted);
                    break;
                }
                else if (answer.toLowerCase().contains("n")) break;
            }
            input.close();
        }
    }

    public static void delete(List<File> fileList) {
        for (File file : fileList) if (file != null) {
            if (file.delete()) {
                if (doTheColorThingy) System.out.println(ConsoleColors.RED_BOLD + "Deleted " + file.toPath() + ConsoleColors.RESET);
                else System.out.println("Deleted " + file.toPath());
            }
            else {
                if (doTheColorThingy) System.err.println(ConsoleColors.RED_BOLD + "Couldn't delete " + ConsoleColors.RESET + file.toPath());
                else System.err.println("Couldn't delete " + file.toPath());
            }
        }
    }
}
