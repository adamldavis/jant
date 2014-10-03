/**
 * Copyright Adam L. Davis 2014. Distributed under Apache 2.0 license.
 */
package jant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Does bare minimum dependency resolution and downloading. Does not resolve
 * transitive dependencies. For that use a "real" build system.
 * 
 * @author adavis
 *
 */
public interface SimpleM2JavaBuilder extends JavaBuilder {

	static final String ARTIFACT_SEP = ":";
	static final String DEFAULT_PCKG = "jar";
	static final String JCENTER = "http://jcenter.bintray.com/";
	static final String MAVEN = "https://search.maven.org/remotecontent?filepath=";
	static final String DEFAULT_REP = JCENTER;

	enum Phase {
		compile, test, provided, runtime;
	}

	class Dependency {
		final Phase phase;
		final String groupId, artifactId, version, packaging;

		Dependency(Phase phase, String str) {
			this(phase, str.split(ARTIFACT_SEP));
		}

		Dependency(Phase phase, String[] split) {
			this(phase, split[0], split[1], split[2],
					split.length > 3 ? split[3] : DEFAULT_PCKG);
		}

		Dependency(Phase phase, String groupId, String artifactId,
				String version, String packaging) {
			super();
			this.phase = phase;
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.packaging = packaging;
		}

		URL getUrl(String baseUrl) throws MalformedURLException {
			return new URL(baseUrl + groupId.replace('.', '/') + "/"
					+ artifactId + "/" + version + "/" + getFilename());
		}

		String getFilename() {
			return artifactId + "-" + version + "." + packaging;
		}

	}

	class Repository {
		final String url;

		Repository(String url) {
			super();
			this.url = url;
		}

		void resolve(Dependency dep) {
			final Path path = Paths.get(state.libDirectory, dep.getFilename());
			if (!Files.exists(path)) {
				try (InputStream stream = dep.getUrl(url).openStream()) {
					Files.copy(stream, path);
					System.out.println("Downloaded: " + dep.getUrl(url));
					System.out.println("Size: " + Files.size(path));
					// TODO: print/check sha1sum ?
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	static final List<Dependency> dependencies = new ArrayList<>();
	static final List<Repository> repositories = new ArrayList<>();

	default void repositories(Consumer<SimpleM2JavaBuilder> r) {
		r.accept(this);
	}

	default void dependencies(Consumer<SimpleM2JavaBuilder> r) {
		r.accept(this);
	}

	/** Adds a compile-time dependency. */
	default void compile(String dep) {
		dependencies.add(new Dependency(Phase.compile, dep));
	}

	/** Adds a test-time dependency. */
	default void test(String dep) {
		dependencies.add(new Dependency(Phase.test, dep));
	}

	/** Adds a runtime dependency. */
	default void runtime(String dep) {
		dependencies.add(new Dependency(Phase.runtime, dep));
	}

	/** Adds a provided dependency. */
	default void provided(String dep) {
		dependencies.add(new Dependency(Phase.provided, dep));
	}

	/** Adds a repository. */
	default void repository(String repo) {
		repositories.add(new Repository(repo));
	}

	default void jcenter() {
		repositories.add(new Repository(JCENTER));
	}

	default void mavenCentral() {
		repositories.add(new Repository(MAVEN));
	}

	default void resolveDependencies() {
		resolveDependencies(null);
	}

	default void resolveDependencies(Phase phase) {
		if (repositories.isEmpty()) {
			repository(DEFAULT_REP);
		}
		Paths.get(state.libDirectory).toFile().mkdirs();

		repositories.forEach(rep -> {
			getDependencies(phase).forEach(rep::resolve);
		});
	}

	default Stream<Dependency> getDependencies(Phase phase) {
		return dependencies.stream().filter(
				dep -> phase == null || dep.phase.equals(phase));
	}

	default void compileSources() {
		resolveDependencies(Phase.compile);
		resolveDependencies(Phase.provided);
		javac(state.srcDirectory);
	}

	default void compileTests() {
		resolveDependencies(Phase.compile);
		resolveDependencies(Phase.provided);
		resolveDependencies(Phase.test);
		javac(state.testDirectory);
	}

	/** Resolves and then lists the jar files for given phase(s). */
	default Stream<File> listDependencyFiles(Phase ... phases) {
		return Arrays.stream(phases)
				.flatMap(phase -> 
					getDependencies(phase).map(d ->
						new File(state.libDirectory + File.separator + d.getFilename())));
	}
	
}
