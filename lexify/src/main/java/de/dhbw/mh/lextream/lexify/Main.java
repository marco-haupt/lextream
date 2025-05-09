package de.dhbw.mh.lextream.lexify;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.dhbw.mh.lextream.lexport.ExitCode;
import de.dhbw.mh.lextream.lexport.GlobPatternResolver;
import de.dhbw.mh.lextream.lexport.LexerSpecification;

/**
 * Entry point for the Lexify CLI tool.
 * 
 * <p>This tool reads a lexer specification (in JSON format) from a file and
 * applies it to a source input (from file or standard input), producing a
 * stream of tokens on standard output.</p>
 *
 * Example usage:
 * <pre>
 *   java -jar lexify.jar lexer-spec.json input.txt
 *   java -jar lexify.jar lexer-spec.json
 * </pre>
 * If no input is provided, the tool reads from standard input.
 */
public final class Main {

	private static final String CLI_SYNTAX = "java -jar lexify.jar [options] <lexer-spec> [source]";
	private static final String CLI_HEADER = System.lineSeparator()
			+ "Tokenizes input using a lexer specification generated by Lexemble.";
	private static final String CLI_FOOTER = System.lineSeparator()
			+ "Examples:" + System.lineSeparator()
			+ "  java -jar lexify.jar lexer-spec.json" + System.lineSeparator()
			+ "  java -jar lexify.jar lexer-spec.json input.txt" + System.lineSeparator()
			+ System.lineSeparator()
			+ "For more information, visit: https://github.com/marco-haupt/lextream";


	private Main() {
		// prevent instantiation
	}


	public static void main(String... args) {
		Options options = new Options();
		Option verbose = new Option("v", "verbose", false, "Enable verbose output");
		Option help = new Option("h", "help", false, "Print this help text");
		options.addOption(verbose);
		options.addOption(help);

		CommandLine cli = parseArguments(args, options);
		String[] positionalArgs = cli.getArgs();

		if (cli.hasOption(help)) {
			printHelp(options);
			return;
		}
		boolean isVerbose = cli.hasOption(verbose);
		if (positionalArgs.length < 1) {
			System.err.println("Error: A lexer specification file is required.");
			printHelp(options);
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		}

		Path lexerSpecPath = resolveSingleFile(positionalArgs[0], "lexer specification");
		LexerSpecification lexerSpec = parseLexerSpecification(lexerSpecPath);

		String input = (positionalArgs.length < 2)
				? readFromStdin()
				: readFile(resolveSingleFile(positionalArgs[1], "input"), "input");

		if (isVerbose) {
			System.out.printf("received '%s'%n", input);
		}

		runLexer(lexerSpec, input);
	}


	/**
	 * Parses command-line arguments using Apache Commons CLI.
	 */
	private static CommandLine parseArguments(String[] args, Options options) {
		try {
			return new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error: Invalid arguments.");
			printHelp(options);
			System.exit(ExitCode.ERROR_INVALID_ARGS);
			return null; // unreachable, but required for compilation
		}
	}


	/**
	 * Prints the CLI usage information.
	 */
	private static void printHelp(Options cliOptions) {
		new HelpFormatter().printHelp(CLI_SYNTAX, CLI_HEADER, cliOptions, CLI_FOOTER, true);
	}


	/**
	 * Reads and parses the lexer specification JSON from file.
	 */
	private static LexerSpecification parseLexerSpecification(Path path) {
		String content = readFile(path, "lexer specification");
		LexerSpecification spec = LexerSpecification.fromJson(content);

		if (!spec.isValid()) {
			System.err.printf("The lexer specification in '%s' is invalid.%n", path);
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		}

		return spec;
	}


	/**
	 * Reads the entire content of a file with error handling.
	 */
	private static String readFile(Path file, String description) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.printf("Error reading %s '%s':%n", description, file);
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
			return ""; // unreachable, but required for compilation
		}
	}


	private static String readFromStdin() {
		try (InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8)) {
			StringBuilder builder = new StringBuilder();
			int ch;
			while ((ch = reader.read()) != -1) {
				builder.append((char) ch);
			}
			return builder.toString();
		} catch (IOException e) {
			System.err.println("Error reading from standard input:");
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
			return ""; // unreachable
		}
	}


	/**
	 * Resolves a single file from a glob pattern. Exits if not exactly one match.
	 */
	private static Path resolveSingleFile(String pattern, String description) {
		List<Path> matches;
		try {
			matches = GlobPatternResolver.resolveGlob(Paths.get("").toAbsolutePath(), pattern);
		} catch (IOException e) {
			System.err.printf("Error resolving %s files:%n", description);
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
			return null; // unreachable
		}

		if (matches.size() != 1) {
			System.err.printf("Expected 1 %s file, but found %d.%n", description, matches.size());
			System.err.println("Matching files:");
			matches.forEach(path -> System.err.println(" - " + path));
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		}

		return matches.get(0);
	}


	/**
	 * Applies the lexer specification to the input and prints tokens to stdout.
	 */
	private static void runLexer(LexerSpecification spec, String input) {
		Lexer lexer = Lexer.from(spec);
		Lexer.Instance instance = lexer.newInstance(input);

		while (!instance.completed()) {
			instance.advance();
			Lexer.Token token = instance.getToken();
			System.out.printf("%s('%s', %d-%d)%n", token.type, token.lexeme, token.startOffset, token.endOffset);
			System.out.flush();
		}
	}

}
