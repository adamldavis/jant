/**
 * Copyright Adam L. Davis 2014. Distributed under Apache 2.0 license.
 */
package jant;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public interface JavaBuilder {

	static final String JAVAS = "javas";

	class State {
		String srcDirectory = "src/main/java";
		String testDirectory = "src/test/java";
		String classesDirectory = "build/classes";
		String distDirectory = "build/dist";
		String libDirectory = "lib";
		String sourceRelease = "1.8";
		String targetRelease = "1.8";
	}

	// ha!
	static State state = new State();

	default void setSrcDirectory(String dir) {
		state.srcDirectory = dir;
	}

	default void setClassesDirectory(String dir) {
		state.classesDirectory = dir;
	}

	default void setDistDirectory(String dir) {
		state.distDirectory = dir;
	}

	default void setSourceRelease(String v) {
		state.sourceRelease = v;
	}

	default void setTargetRelease(String v) {
		state.targetRelease = v;
	}

	default void setLibDirectory(String dir) {
		state.libDirectory = dir;
	}

	default void javac() {
		javac(state.srcDirectory);
	}

	default void java(String mainClass, String... args) {
		try {
			executeAndWait("java -cp " + getClasspath() + " " + mainClass + " "
					+ stream(args).collect(joining(" ")));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	default void javac(String srcDirectory) {
		try {
			final List<String> list = Files.walk(Paths.get(srcDirectory))
					.filter(p -> p.toFile().getName().endsWith(".java"))
					.map(p -> p.toAbsolutePath().toFile().getAbsolutePath())
					.collect(Collectors.toList());
			Path javas = Paths.get(JAVAS);
			Files.write(javas, list);
			new File(state.classesDirectory).mkdirs();

			String command = "javac -d " + state.classesDirectory + " -source "
					+ state.sourceRelease + " -target " + state.targetRelease
					+ " -cp " + getClasspath() + " @javas";
			executeAndWait(command);
			javas.toFile().deleteOnExit();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	default String getClasspath() throws IOException {
		return Files.walk(Paths.get(state.libDirectory))
				.filter(p -> p.toFile().getName().endsWith(".jar"))
				.map(p -> p.toAbsolutePath().toFile().getAbsolutePath())
				.collect(joining(File.pathSeparator));
	}

	default void jar(String filename) {
		new File(state.distDirectory).mkdirs();
		String command = "jar cf " + state.distDirectory + File.separator
				+ filename + " -C " + state.classesDirectory + " .";
		System.out.println(command);
		try {
			executeAndWait(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default void executeAndWait(String command) throws IOException {
		final Process child = Runtime.getRuntime().exec(command);
		try {
			Thread t = new Thread(() -> copyBytes(child.getErrorStream(),
					System.err));
			t.run();
			child.waitFor(30, TimeUnit.SECONDS);
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	default void unjar(String name, String dir) {
		try (final JarFile file = new JarFile(name)) {
			final File d = new File(dir);
			d.mkdirs();
			file.stream().forEach(
					fixie((JarEntry je) -> {
						Files.copy(file.getInputStream(je),
								Paths.get(dir, je.getName()));
					}));
		} catch (IOException | RuntimeException ex) {
			ex.printStackTrace();
		}
	}

	// static HELPER methods
	static void copyBytes(InputStream input, PrintStream out) {
		int n = 0;
		byte[] arr = new byte[1024];
		try {
			while (-1 != (n = input.read(arr)))
				out.write(arr, 0, n);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FunctionalInterface
	public interface IOExceptionThrowingConsumer<T> {
		void apply(T t) throws IOException;
	}

	// Helper method to wrap IOException in RuntimeException in a Consumer
	public static <T> Consumer<T> fixie(IOExceptionThrowingConsumer<T> f) {
		return t -> {
			try {
				f.apply(t);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		};
	}

}
