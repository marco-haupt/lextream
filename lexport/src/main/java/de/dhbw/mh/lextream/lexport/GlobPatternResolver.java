package de.dhbw.mh.lextream.lexport;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class GlobPatternResolver {

	/**
	 * Resolves a glob pattern to a list of matching file paths.
	 *
	 * @param baseDir   The base directory from which to start the search.
	 * @param pattern   The glob pattern
	 * @return          A list of Paths that match the glob pattern.
	 * @throws IOException if an I/O error occurs during file traversal.
	 */
	public static List<Path> resolveGlob(Path baseDir, String pattern) throws IOException {
		List<Path> matchedPaths = new ArrayList<>();

		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

		Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				Path relativePath = baseDir.relativize(file);
				if (matcher.matches(relativePath)) {
					matchedPaths.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		return matchedPaths;
	}

}
