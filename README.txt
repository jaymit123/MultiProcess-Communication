Multi Process Communication using Berkley’s Algorithm and Vector Clocks

INSTRUCTIONS FOR BERKELEY PROJECT
Step 1: Go to Berkeley folder
Step 2: Compile the program -> javac -cp . project2/CoordinatorProcess.java 
Step 3: Run -> java project2.CoordinatorProcess 
Step 4: Enter the number of additional processes, minimum of 2. As soon as you enter the number, coordinator process will wait for the other processes.
Follow Step 5 to create new processes 

Step 5: Open new windows depending upon the number of additional processes. Example: If 2 processes are added, open two terminals.
Step 6: Go to Berkeley folder on those windows. 
Step 7: Compile the program -> javac -cp SlaveProcess.java 
Step 8: Run -> java SlaveProcess

Step 9: Check the output

INSTRUCTIONS FOR BERKELEY_VECTOR_CLOCKS PROJECT

Step 1: Go to Berkeley_Vector_Clocks folder
Step 2: Repeat the same the steps as Berkeley Project from Step 2.

INSTRUCTIONS FOR BONUS PROJECT

Step 1: Go to Bonus folder
Step 2: Compile the program -> javac -cp . project2/CoordinatorProcess.java 
Step 3: Run -> java project2.CoordinatorProcess 
Step 4: Enter the complete path of the file: <path_till_Bonus_Folder>/Bonus/Counter.property

<<PLEASE DO NOT CREATE YOUR OWN Counter.property file>>
<<Because the encoding format used by Unix to create file makes java unable to read the property file>>
<<USE THE Counter.property provided in the Bonus folder>>

Step 5: Repeat the same the steps as Berkeley Project from Step 4.
