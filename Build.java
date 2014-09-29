
// Example build file

public class Build extends Jant {

    public void run() {
        println("in run");
        
        task("jar", () -> {
            println("do jar stuff");
        });
        
        dependencies(this::deps);
        
        repositories(this::reps);
    }
    
    public void deps() {
        compile("com.google:guava:17.0");        
    }
    
    public void reps() {
        println("Adding repos");
    }
    
}

