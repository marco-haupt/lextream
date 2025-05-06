package de.dhbw.mh.lextream.lexemble;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.dhbw.mh.lextream.lexport.DfaModel;
import de.dhbw.mh.lextream.lexport.ExitCode;
import de.dhbw.mh.lextream.lexport.GlobPatternResolver;
import de.dhbw.mh.lextream.lexport.LexerSpecification;

/**
 * Entry point for the Lexemble CLI tool.
 * <p>
 * This tool generates a lexer specification (in JSON format) from a
 * set of DFA files encoded in JSON. It accepts Java-style glob patterns
 * for selecting input files and optionally writes the result to an output file.
 */
public class Main {

	private static final String CLI_SYNTAX = "java -jar lexemble.jar [options] <input-pattern> <output-file>";
	private static final String CLI_HEADER = System.lineSeparator()
			+ "Generates a lexer specification from (minimized) DFA files.";
	private static final String CLI_FOOTER = System.lineSeparator()
			+ "Example:" + System.lineSeparator()
			+ "  java -jar lexemble.jar \"automata/*.mdfa\" lexerspec.txt -v" + System.lineSeparator()
			+ System.lineSeparator()
			+ "For more information, visit: https://github.com/marco-haupt/lextream";


	public static void main(String... args) {
		Options options = new Options();
		Option verbose = new Option("v", "verbose", false, "Enable verbose output");
		Option help = new Option("h", "help", false, "Print this help text");
		options.addOption(verbose);
		options.addOption(help);

		CommandLine cli = parseArguments(args, options);
		if (cli.hasOption(help)) {
			printHelp(options);
			System.exit(ExitCode.SUCCESS);
		}
		boolean isVerbose = cli.hasOption(verbose);
		String[] positionalArgs = cli.getArgs();

		if (positionalArgs.length < 1) {
			System.err.println("Error: Input file pattern is required.");
			printHelp(options);
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		}
		String inputPattern = positionalArgs[0];
		List<Path> inputFiles = resolveInputFiles(inputPattern);
		if(inputFiles.isEmpty()) {
			System.err.printf("No files match the glob pattern '%s'%n", inputPattern);
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		}
		if (isVerbose) {
			System.out.println("Matched input files:");
			inputFiles.forEach(path -> System.out.println(" - " + path));
		}

		String lexerJson = buildLexerJson(inputFiles);

		if(positionalArgs.length < 2) {
			System.out.println(lexerJson);
			System.exit(ExitCode.SUCCESS);
		} else {
			writeOutputFile(positionalArgs[1], lexerJson, isVerbose);
		}
	}



	/**
	 * Parses command-line arguments.
	 */
	private static CommandLine parseArguments(String[] args, Options options) {
		try {
			return new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error: Invalid command-line arguments.");
			printHelp(options);
			System.exit(ExitCode.ERROR_INVALID_ARGS);
			return null; // unreachable, but required for compilation
		}
	}



	/**
	 * Resolves glob pattern to input file paths.
	 */
	private static List<Path> resolveInputFiles(String pattern) {
		try {
			return GlobPatternResolver.resolveGlob(Paths.get("").toAbsolutePath(), pattern);
		} catch (IOException e) {
			System.err.println("Error reading input files:");
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
			return Collections.emptyList(); // unreachable, but required for compilation
		}
	}


	/**
	 * Builds a lexer specification JSON string from the given files.
	 */
	private static String buildLexerJson(List<Path> inputFiles) {
		try {
			LexerSpecification lexer = new LexerSpecification();
			for (Path file : inputFiles) {
				String content = Files.readString(file, StandardCharsets.UTF_8);
				DfaModel dfa = DfaModel.fromJson(content);
				String tokenType = stripExtension(file.getFileName().toString());
				lexer.addRule(dfa, tokenType);
			}
			return lexer.asJson();
		} catch (IOException e) {
			System.err.println("Error processing DFA files:");
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
			return ""; // unreachable, but required for compilation
		}
	}


	/**
	 * Writes the lexer specification to the given output file.
	 */
	private static void writeOutputFile(String outputPath, String content, boolean isVerbose) {
		File outputFile = new File(outputPath);
		try {
			if (outputFile.exists()) {
				System.err.printf("Error: The file '%s' already exists.%n", outputFile.getCanonicalPath());
				System.exit(ExitCode.ERROR_INVALID_ARGS);
			}
			Files.writeString(outputFile.toPath(), content, StandardCharsets.UTF_8);
			if (isVerbose) {
				System.out.printf("Lexer specification written to:%n%s%n", outputFile.getCanonicalPath());
				System.out.println("Thank you for using Lexemble!");
			}
		} catch (IOException e) {
			System.err.println("Error writing output file:");
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
		}
	}


	/**
	 * Removes the file extension from a filename.
	 *
	 * @param filename The filename.
	 * @return The filename without its extension.
	 */
	private static String stripExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		return (dotIndex > 0) ? filename.substring(0, dotIndex) : filename;
	}


	/**
	 * Prints help/usage information for the CLI tool.
	 */
	private static void printHelp(Options cliOptions) {
		new HelpFormatter().printHelp(CLI_SYNTAX, CLI_HEADER, cliOptions, CLI_FOOTER, true);
	}

}
