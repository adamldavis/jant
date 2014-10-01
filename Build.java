
// Example build file

public class Build extends Jant implements JavaBuilder {

    public void run() {
        println("in run");
        
        task("jar", () -> {
        	javac();
            jar("myjar.jar");
        });
        task("compile", this::javac);
        
        task("compile2", () -> {
        	setSrcDirectory("/secondary/java");
        	javac();
        });
        
        dependencies(j -> {
            compile("com.google:guava:17.0");
            test("junit:junit:4.10");
        });
    }
    
}

