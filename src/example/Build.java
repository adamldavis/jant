
import jant.*;

// Example build file

public class Build extends Jant implements SimpleM2JavaBuilder {

	// this method is used as a task
	public void compile() {
		setSrcDirectory("java");
    	println("compiling...");
    	compileSources();
	}
	
	// this does all the configuration
	@Override
    public void run() {
        println("in run");
        
        task("jar", () -> {
        	compile(); // just call methods instead of dependsOn
            jar("myjar.jar");
        });
        task("compile", this::compile); // method references as tasks
        task("javac", this::javac); // this one does not resolve dependencies 
        
        dependencies(j -> {
            compile("com.google.guava:guava:17.0");
            test("junit:junit:4.11");
        });
        repositories(j -> {
        	jcenter();
        	mavenCentral();
        });
    }
    
}

