# How to use this tutorial

This tutorial demonstrates usage of the CDC subscription in evitaDB.

## Steps to run the tutorial

1. start evitaDB
2. run the `ChangeCaptureSetup.java` to initialize the CDC subscription (as a separate process)
3. run the `Main.java` to setup new `evita-tutorial` catalog and continuously modify data in the catalog (as a separate process)

When everything is ok, the subscribers should print incoming CDC events to the console of the first process. 