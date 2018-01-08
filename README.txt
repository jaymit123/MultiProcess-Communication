Multi Process Communication using Berkley’s Algorithm and Vector Clocks

This projects is divided into 3 Parts:

Part1: Berkeley Project : Demonstrates how time between processes can be synchronized using Berkeley Algorithm.

Part2: Berkeley & Vector Clock Project: Shows use of berkeley algorithm for the initial communication and vector clocks is used for subsequent communication between processes inorder to get partial ordering of messages / events.

Part3 : Shows how Centralized Algorithm is used to prevent concurrent write access to a file. One process is created as Coordinator.

PART1 : INSTRUCTIONS FOR BERKELEY PROJECT
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

PART 2: INSTRUCTIONS FOR BERKELEY_VECTOR_CLOCKS PROJECT

Step 1: Go to Berkeley_Vector_Clocks folder
Step 2: Repeat the same the steps as Berkeley Project from Step 2.

PART 3: INSTRUCTIONS FOR BONUS PROJECT

Step 1: Go to Bonus folder
Step 2: Compile the program -> javac -cp . project2/CoordinatorProcess.java 
Step 3: Run -> java project2.CoordinatorProcess 
Step 4: Enter the complete path of the file: <path_till_Bonus_Folder>/Bonus/Counter.property

<<PLEASE DO NOT CREATE YOUR OWN Counter.property file>>
<<Because the encoding format used by Unix to create file makes java unable to read the property file>>
<<USE THE Counter.property provided in the Bonus folder>>

Step 5: Repeat the same the steps as Berkeley Project from Step 4.
