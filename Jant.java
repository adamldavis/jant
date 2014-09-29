
import java.util.*;

public abstract class Jant {

    protected Map<String,Task> tasks = new HashMap<>();
    protected List<Dependency> dependencies = new ArrayList<>();
    protected List<Repository> repositories = new ArrayList<>();

    protected abstract void run();
    
    public void repositories(Runnable r) {
        r.run();
    }
    public void dependencies(Runnable r) {
        r.run();
    }
    public void task(String name, Runnable r) {
        tasks.put(name, new Task(name, r));
    }
    /** Adds a compile-time dependency. */
    public void compile(String dep) {
        println("Adding: " + dep);
    }
    /** Adds a test-time dependency. */    
    public void test(String dep) {}
    /** Adds a runtime dependency. */
    public void runtime(String dep) {}
    /** Adds a provided dependency. */
    public void provided(String dep) {}
    
    /** Adds a repository. */
    public void repository(String repo) {}
    
    public static void main(String ...a) {
        try {
            Jant j = (Jant) Class.forName("Build").newInstance();
            j.run(); //builds model
            if (a.length > 0) {
                // TODO do dependsOn stuff
                String name = a[0];
                if (!j.tasks.containsKey(name)) {
                    println("Error: " + name + " task not found");
                } else
                    j.tasks.get(name).r.run();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void println(String s) {System.out.println(s);}

}
class Task {
    public String name;
    public Runnable r;
    public Task(String name, Runnable r) {this.name=name; this.r=r;}
}

class Dependency {}

class Repository {}


