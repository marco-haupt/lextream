package de.dhbw.mh.lextream.lexpress.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.dhbw.mh.lextream.lexpress.DFA;
import de.dhbw.mh.redeggs.RecursiveDescentRedeggsParser;
import de.dhbw.mh.redeggs.RedeggsParseException;
import de.dhbw.mh.redeggs.RegularEggspression;

public class LexpressCli {

	// Constants for CLI syntax, header, and footer
	static final String CLI_SYNTAX = "java -jar lexpress.jar <lexer-spec> [source]";
	static final String CLI_HEADER = System.lineSeparator()
			+ "  Compiles regular expressions into finite automata.";
	static final String CLI_FOOTER = System.lineSeparator()
			+ "Examples:" + System.lineSeparator()
			+ "  java -jar lexpress.jar lexer-spec.json" + System.lineSeparator()
			+ "  java -jar lexpress.jar lexer-spec.json SOME_TOKEN.regex" + System.lineSeparator()
			+ System.lineSeparator()
			+ "For more information, visit: https://github.com/marco-haupt/lextream";


	private static final Options OPTIONS = new Options();
	private static final Option CLI_HELP = createOption("h", "help", "Print this help message");
	private static final Option CLI_VERBOSE = createOption("v", "verbose", "Enable verbose output");
	private static final Option CLI_COMPILE = createOption("c", "compile", "Output NFA to <file>", "file");
	private static final Option CLI_MINIMIZE = createOption("m", "minimize", "Output minimized DFA to <file>", "file");
	private static final Option CLI_DETERMINIZE = createOption("d", "determinize", "Output DFA to <file>", "file");


	private static Option createOption(String shortOpt, String longOpt, String description) {
		Option opt = Option.builder(shortOpt)
				.longOpt(longOpt)
				.hasArg(false)
				.desc(description)
				.build();
		OPTIONS.addOption(opt);
		return opt;
	}

	private static Option createOption(String shortOpt, String longOpt, String description, String argName) {
		Option opt = Option.builder(shortOpt)
				.longOpt(longOpt)
				.optionalArg(true)
				.argName(argName)
				.desc(description)
				.build();
		OPTIONS.addOption(opt);
		return opt;
	}

	public static class CliConfig {
		public boolean showHelp;
		public boolean verbose;
		public Optional<FileWrapper> input = Optional.empty();
		public Optional<FileWrapper> nfaFile = Optional.empty();
		public Optional<FileWrapper> dfaFile = Optional.empty();
		public Optional<FileWrapper> mdfaFile = Optional.empty();
	}

	public static CliConfig parseArgs(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(OPTIONS, args);

		CliConfig config = new CliConfig();
		String[] positionalArgs = cmd.getArgs();
		if(positionalArgs.length >= 1) {
			config.input = Optional.of(new FileWrapper(positionalArgs[0]));
			String inputFileName = removeFileExtension(config.input.get().getFileName(), true);
			handleFileOptions(cmd, config, inputFileName);
		}
		config.showHelp = cmd.hasOption(CLI_HELP);
		config.verbose = cmd.hasOption(CLI_VERBOSE);

		return config;
	}


	private static void handleFileOptions(CommandLine cmd, CliConfig config, String inputFileName) {
		config.nfaFile = getFileOption(cmd, CLI_COMPILE, inputFileName, ".nfa");
		config.dfaFile = getFileOption(cmd, CLI_DETERMINIZE, inputFileName, ".dfa");
		config.mdfaFile = getFileOption(cmd, CLI_MINIMIZE, inputFileName, ".mdfa");
	}


	private static Optional<FileWrapper> getFileOption(CommandLine cmd, Option option, String inputFileName, String extension) {
		if (!cmd.hasOption(option)) {
			return Optional.empty();
		}
		String fileName = cmd.getOptionValue(option);
		return Optional.ofNullable(fileName)
				.map(FileWrapper::new)
				.or(() -> Optional.of(new FileWrapper(inputFileName + extension)));
	}


	public static void runWith(CliConfig config, RecursiveDescentRedeggsParser parser) throws IOException, RedeggsParseException {
		if(config.showHelp) {
			printHelp();
			return;
		}

		String regex = config.input.get().read();
		RegularEggspression ast = parser.parse(regex);
		ThompsonAutomaton automaton = ast.accept(new ThompsonsConstruction());

		if (config.nfaFile.isPresent()) {
			// TODO: Export NFA file
		}

		handleFileExport(config, automaton);
	}


	private static void handleFileExport(CliConfig config, ThompsonAutomaton automaton) throws IOException {
		if (config.dfaFile.isEmpty() && config.mdfaFile.isEmpty()) {
			return;
		}

		DFA dfa = PowerSetConstruction.on(automaton);
		exportFile(config.dfaFile, dfa, config.verbose);

		if (config.mdfaFile.isEmpty()) {
			return;
		}

		SimpleDfa simpleDfa = new SimpleDfa(dfa);
		DFA mdfa = Minimizer.applyOn(simpleDfa);

		if (config.mdfaFile.isPresent()) {
			exportFile(config.mdfaFile, mdfa, config.verbose);
		}
	}


	private static void exportFile(Optional<FileWrapper> file, DFA dfa, boolean verbose) throws IOException {
		if(file.isPresent()) {
			file.get().persist(DfaMapping.mapOntoDfaModel(dfa).asJson());
			if(verbose) {
				System.out.printf("Results written to: %s%n", file.get().getPath());
			}
		}
	}


	/**
	 * Prints the CLI usage information.
	 */
	private static void printHelp() {
		new HelpFormatter().printHelp(CLI_SYNTAX, CLI_HEADER, OPTIONS, CLI_FOOTER, true);
	}


	public static String removeFileExtension(String filename, boolean removeAllExtensions) {
		if (filename == null || filename.isEmpty()) {
			return filename;
		}
		String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
		return filename.replaceAll(extPattern, "");
	}


	public static void validate(CliConfig config, Path path) {
		if (!config.showHelp && config.input.isEmpty()) {
			throw new IllegalArgumentException("You must provide an input file.");
		}
		if (config.input.isPresent() && !config.input.get().exists()) {
			throw new IllegalArgumentException("File does not exist: " + config.input.get().getPath());
		}
		String fileName = "";
		fileName = removeFileExtension(fileName, true);

		checkFileExistence(config.nfaFile, path);
		checkFileExistence(config.dfaFile, path);
		checkFileExistence(config.mdfaFile, path);
	}


	private static void checkFileExistence(Optional<FileWrapper> file, Path path) {
		if(file.isPresent() && file.get().exists()) {
			throw new IllegalArgumentException(String.format("The file '%s' does already exist.", file.get().getPath()));
		}
	}

}
