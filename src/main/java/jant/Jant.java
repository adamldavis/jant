/**
 * Copyright Adam L. Davis 2014. Distributed under Apache 2.0 license.
 */
package jant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Jant {

    protected Map<String,Task> tasks = new HashMap<>();

    protected abstract void run();
    
    public void task(String name, Runnable r) {
        tasks.put(name, new Task(name, r));
    }
    
    public static void main(String ...a) {
        try {
            Jant j = (Jant) Class.forName("Build").newInstance();
            j.run(); //builds model
            if (a.length == 0) {
            	println("To list tasks: jant tasks\n\nTo run a task: jant <taskname>");
            }
            else {
                String name = a[0];
                println ("Running task: " + name);
                if ("tasks".equals(name)) {
                	println("Tasks" + j.tasks.keySet().toString());
                	return;
                }
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


