package de.dhbw.mh.lextream.lexpress.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

class FileWrapper {

	private final Path filePath;

	FileWrapper(String pathname) {
		this(Paths.get(pathname));
	}

	public FileWrapper(Path path) {
		this.filePath = path;
	}

	public boolean exists() {
		return Files.exists(filePath);
	}

	public String getFileName() {
		return filePath.getFileName().toString();
	}

	public String getPath() {
		return filePath.toAbsolutePath().toString();
	}

	public void persist(String contents) throws IOException {
		Files.writeString(filePath, contents, StandardOpenOption.CREATE_NEW);
	}

	public String read() throws IOException {
		return Files.readAllLines(filePath)
				.stream().collect(Collectors.joining(System.lineSeparator()));
	}

}
