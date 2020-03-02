all: Threads.class threads

Threads.class: Threads.java
	javac Threads.java

threads: threads.c
	gcc -pthread threads.c -o threads
