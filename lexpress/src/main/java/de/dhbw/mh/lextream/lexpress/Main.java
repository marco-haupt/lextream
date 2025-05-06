package de.dhbw.mh.lextream.lexpress;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.cli.ParseException;

import de.dhbw.mh.lextream.lexport.ExitCode;
import de.dhbw.mh.lextream.lexpress.internal.ConcreteSymbolFactory;
import de.dhbw.mh.lextream.lexpress.internal.LexpressCli;
import de.dhbw.mh.redeggs.RecursiveDescentRedeggsParser;
import de.dhbw.mh.redeggs.RedeggsParseException;
import de.dhbw.mh.redeggs.SymbolFactory;

public class Main {

	public static void main(String[] args) {
		try {
			LexpressCli.CliConfig config = LexpressCli.parseArgs(args);
			LexpressCli.validate(config, Path.of("."));

			SymbolFactory factory = new ConcreteSymbolFactory();
			RecursiveDescentRedeggsParser parser = new RecursiveDescentRedeggsParser(factory);

			LexpressCli.runWith(config, parser);
		} catch (ParseException e) {
			System.err.println("Failed to parse command-line arguments:");
			e.printStackTrace();
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		} catch(IllegalArgumentException e) {
			System.err.printf("Error: %s%n", e.getMessage());
			System.exit(ExitCode.ERROR_INVALID_ARGS);
		} catch (IOException | RedeggsParseException e) {
			System.err.println("Execution failed:");
			e.printStackTrace();
			System.exit(ExitCode.ERROR_IO);
		}
	}

}
