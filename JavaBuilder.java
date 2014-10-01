import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public interface JavaBuilder {

	public static final String JAVAS = "javas";

	class State {
		String srcDirectory = "src/main/java";
		String classesDirectory = "build/classes";
		String distDirectory = "build/dist";
		String classPath = "lib";
		String sourceRelease = "1.7";
		String targetRelease = "1.7";
	}

	// ha!
	static State state = new State();

	public default void setSrcDirectory(String v) {
		state.srcDirectory = v;
	}

	public default void setClassesDirectory(String v) {
		state.classesDirectory = v;
	}

	public default void setDistDirectory(String v) {
		state.distDirectory = v;
	}

	public default void javac() {
		try {
			List<String> list = Files.walk(Paths.get(state.srcDirectory))
					.filter(p -> p.toFile().getName().endsWith(".java"))
					.map(p -> p.toAbsolutePath().toFile().getAbsolutePath())
					.collect(Collectors.toList());
			Path javas = Paths.get(JAVAS);
			Files.write(javas, list);

			String command = "javac -d " + state.classesDirectory + " -source "
					+ state.sourceRelease + " -target " + state.targetRelease
					+ " -cp " + state.classPath + " @javas";
			executeAndWait(command);
			javas.toFile().deleteOnExit();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public default void jar(String filename) {
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

	public default void executeAndWait(String command) throws IOException {
		final Process child = Runtime.getRuntime().exec(command);
		try {
			child.waitFor(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public default void unjar(String name, String dir) {
		try (final JarFile file = new JarFile(name)) {
			final AtomicReference<IOException> ex = new AtomicReference<>();
			final File d = new File(dir);
			d.mkdirs();
			file.stream().forEach(
					je -> {
						try {
							Files.copy(file.getInputStream(je),
									Paths.get(dir, je.getName()));
						} catch (IOException ie) {
							ex.lazySet(ie);
						}
					});
			if (ex.get() != null) {
				throw ex.get();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
