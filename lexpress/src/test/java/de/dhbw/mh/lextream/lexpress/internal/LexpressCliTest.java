package de.dhbw.mh.lextream.lexpress.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.cli.ParseException;
import org.assertj.core.api.SoftAssertionsProvider.ThrowingRunnable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import de.dhbw.mh.lextream.lexpress.DFA;
import de.dhbw.mh.redeggs.CodePointRange;
import de.dhbw.mh.redeggs.RecursiveDescentRedeggsParser;
import de.dhbw.mh.redeggs.RedeggsParseException;
import de.dhbw.mh.redeggs.RegularEggspression;
import de.dhbw.mh.redeggs.SymbolFactory;

class LexpressCliTest {

	@Nested
	class ValidationTests {

		@Test
		void validatesSuccessfullyWithInputAndOutput(@TempDir Path tempDir) throws IOException {
			Path input = createFile(tempDir, "input.regex");
			Path output = tempDir.resolve("minimal.automaton");

			LexpressCli.CliConfig config = new LexpressCli.CliConfig();
			config.input = Optional.of(new FileWrapper(input));
			config.mdfaFile = Optional.of(new FileWrapper(output));

			assertThatCode(() ->
				LexpressCli.validate(config, tempDir)
			).doesNotThrowAnyException();
		}


		@Test
		void throwsIfInputFileIsMissing(@TempDir Path tempDir) {
			LexpressCli.CliConfig config = new LexpressCli.CliConfig();
			config.input = Optional.of(new FileWrapper("this/path/does/not/exist.txt"));

			assertThatThrownBy(() -> LexpressCli.validate(config, tempDir))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("File does not exist");
		}


		@ParameterizedTest(name = "should fail when {0} file already exists")
		@MethodSource("existingFileProvider")
		void failsWhenOutputFileAlreadyExists(
			String fileType,
			String extension,
			BiConsumer<LexpressCli.CliConfig, FileWrapper> fileSetter,
			@TempDir Path tempDir
		) throws IOException {
			Path inputDir = Files.createDirectory(tempDir.resolve("subdir"));
			Path input = createFile(inputDir, "example.regex");
			Path existing = createFile(tempDir, "example" + extension);

			LexpressCli.CliConfig config = new LexpressCli.CliConfig();
			config.input = Optional.of(new FileWrapper(input));
			fileSetter.accept(config, new FileWrapper(existing));
			
			assertThatThrownBy(() -> LexpressCli.validate(config, tempDir))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("The file '%s' does already exist.", existing.toAbsolutePath());
		}


		static Stream<Arguments> existingFileProvider() {
			return Stream.of(
				Arguments.of("NFA", ".nfa", (BiConsumer<LexpressCli.CliConfig, FileWrapper>) (c, f) -> c.nfaFile = Optional.of(f)),
				Arguments.of("DFA", ".dfa", (BiConsumer<LexpressCli.CliConfig, FileWrapper>) (c, f) -> c.dfaFile = Optional.of(f)),
				Arguments.of("MDFA", ".mdfa", (BiConsumer<LexpressCli.CliConfig, FileWrapper>) (c, f) -> c.mdfaFile = Optional.of(f))
			);
		}

	}


	@Nested
	class ParsingTests {

		@Test
		void provideABetterName(@TempDir Path tempDir) throws ParseException {
			String[] args = {"--help"};
			
			LexpressCli.CliConfig config = LexpressCli.parseArgs(args);
			
			assertThat(config.showHelp).isTrue();
		}

		@Test
		void provideABetterName2(@TempDir Path tempDir) throws ParseException {
			String[] args = {"--verbose"};
			
			LexpressCli.CliConfig config = LexpressCli.parseArgs(args);
			
			assertThat(config.verbose).isTrue();
		}

		@ParameterizedTest(name = "should assign {0} file correctly from CLI args")
		@MethodSource("outputFileArguments")
		void assignsOutputFileCorrectly(
			String fileType,
			String[] args,
			Function<LexpressCli.CliConfig, Optional<FileWrapper>> extractor,
			String expectedPath
		) throws ParseException {
			LexpressCli.CliConfig config = LexpressCli.parseArgs(args);

			assertThat(extractor.apply(config))
				.as("Expected %s to be set to '%s'", fileType, expectedPath)
				.hasValueSatisfying(actual ->
					assertThat(actual.getPath()).isEqualTo(new FileWrapper(expectedPath).getPath())
				);

			long setCount = Stream.of(config.nfaFile, config.dfaFile, config.mdfaFile)
					.filter(Optional::isPresent)
					.count();

			assertThat(setCount)
				.as("Exactly one output option should be set")
				.isEqualTo(1);
		}


		static Stream<Arguments> outputFileArguments() {
			return Stream.of(
				Arguments.of("default NFA", new String[]{"input.regex", "-c"}, 
						(Function<LexpressCli.CliConfig, Optional<FileWrapper>>) c -> c.nfaFile, 
						"input.nfa"),
				Arguments.of("default DFA", new String[]{"input.regex", "-d"}, 
						(Function<LexpressCli.CliConfig, Optional<FileWrapper>>) c -> c.dfaFile, 
						"input.dfa"),
				Arguments.of("default MDFA", new String[]{"input.regex", "-m"}, 
						(Function<LexpressCli.CliConfig, Optional<FileWrapper>>) c -> c.mdfaFile, 
						"input.mdfa"),
				
				Arguments.of("custom NFA", new String[]{"input.regex", "-c", "custom.nfa"}, 
						(Function<LexpressCli.CliConfig, Optional<FileWrapper>>) c -> c.nfaFile, 
						"custom.nfa"),
				Arguments.of("custom DFA", new String[]{"input.regex", "-d", "custom.dfa"}, 
						(Function<LexpressCli.CliConfig, Optional<FileWrapper>>) c -> c.dfaFile, 
						"custom.dfa"),
				Arguments.of("custom MDFA", new String[]{"input.regex", "-m", "custom.mdfa"}, 
						(Function<LexpressCli.CliConfig, Optional<FileWrapper>>) c -> c.mdfaFile, 
						"custom.mdfa")
			);
		}

	}


	@Test
	void printsHelpMessageIfShowHelpFlagIsSet() throws Exception {
		LexpressCli.CliConfig config = new LexpressCli.CliConfig();
		config.showHelp = true;

		String output = captureSystemOut(() -> {
			LexpressCli.runWith(config, null);
		});

		assertThat(output)
			.contains(LexpressCli.CLI_SYNTAX)
			.contains(LexpressCli.CLI_HEADER)
			.contains("--help")
			.contains("--compile")
			.contains("--minimize")
			.contains("--determinize")
			.contains(LexpressCli.CLI_FOOTER);
	}


	@Test
	void shouldCallPowerSetConstruction(@TempDir Path tempDir) throws RedeggsParseException, IOException {
		SymbolFactory factory = new ConcreteSymbolFactory();
		RecursiveDescentRedeggsParser parser = new RecursiveDescentRedeggsParser(factory) {
			@Override
			public RegularEggspression parse(String regex) throws RedeggsParseException {
				return new RegularEggspression.EmptyWord();
			}
		};

		LexpressCli.CliConfig config = new LexpressCli.CliConfig();
		config.input = Optional.of(new FileWrapper(tempDir) {
			@Override
			public String read() {
				return "";
			}
		});
		config.dfaFile = Optional.of(new FileWrapper(tempDir.resolve("test.dfa")));
		
		DFA.State[] states = new DFA.State[] {new DFA.State()};
		Map<DFA.State, Map<CodePointRange,DFA.State>> transitions = new HashMap<>();
		transitions.put(states[0], new HashMap<>());
		DFA dfa = new DeterministicStateMachine(
				states,
				new HashSet<CodePointRange>(),
				states[0],
				transitions,
				new HashSet<>()
		);
		
		try (
			MockedStatic<PowerSetConstruction> mockedPc = mockStatic(PowerSetConstruction.class);
			MockedStatic<Minimizer> mockedMin = mockStatic(Minimizer.class);
		) {
			mockedPc.when(() -> PowerSetConstruction.on(any())).thenReturn(dfa);

			LexpressCli.runWith(config, parser);

			mockedPc.verify(() -> PowerSetConstruction.on(any()));
			mockedMin.verify(() -> Minimizer.applyOn(any()), times(0));
		}
	}


	// ──────────────────────────── Helpers ─────────────────────────────


	private static Path createFile(Path dir, String name) throws IOException {
		Path file = dir.resolve(name);
		return Files.createFile(file);
	}

	static String captureSystemOut(ThrowingRunnable runnable) throws Exception {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		try (PrintStream testOut = new PrintStream(outContent)) {
			System.setOut(testOut);
			runnable.run();
		} finally {
			System.setOut(originalOut);
		}
		return outContent.toString();
	}

}
