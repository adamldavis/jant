Jant: A build system for Java 8
-------------------------------

Yes, yet another build system.

Uses features of Java 8: lambdas, default methods, static members on interfaces, and Streams to create a full build system.

Inspired by all those that come before: Ant, Maven, and Gradle.

See src/example/Build.java for an example.

Usage:

* Manually create a jant.jar after compiling the sources (jar cf jant.jar -C bin .)
* Modify Build.java for your build.
* Run ./jant.sh tasks to list all tasks.
* Run ./jant.sh [task] to run the task.

