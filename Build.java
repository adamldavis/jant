
// Example build file

public class Build extends Jant {

    public void run() {
        println("in run");
        
        task("jar", () -> {
            println("do jar stuff");
        });
    }
    
}

