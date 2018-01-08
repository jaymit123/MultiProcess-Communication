#Multi Process Communication using Berkley’s Algorithm and Vector Clocks

This projects is divided into 3 Parts:

##Part1: Berkeley Project :
Demonstrates how time between processes can be synchronized using Berkeley Algorithm.

##Part2: Berkeley & Vector Clock Project:
Shows use of berkeley algorithm for the initial communication and vector clocks is used for subsequent communication between processes inorder to get partial ordering of messages / events.

##Part3 Centralized Algorithm : 
Shows how Centralized Algorithm is used to prevent concurrent write access to a file. One process is created as Coordinator.

###PART1 : INSTRUCTIONS FOR BERKELEY PROJECT
1. Go to Berkeley folder
2. Compile the program -> javac -cp . project2/CoordinatorProcess.java 
3. Run -> java project2.CoordinatorProcess 
4. Enter the number of additional processes, minimum of 2. As soon as you enter the number, coordinator process will wait      for the other processes.
   Follow Step 5 to create new processes 

5. Open new windows depending upon the number of additional processes. Example: If 2 processes are added, open two              terminals.
6. Go to Berkeley folder on those windows. 
7. Compile the program -> javac -cp SlaveProcess.java 
8. Run -> java SlaveProcess
9. Check the output

###PART 2: INSTRUCTIONS FOR BERKELEY_VECTOR_CLOCKS PROJECT
1. Go to Berkeley_Vector_Clocks folder
2. Repeat the same the steps as Berkeley Project from Step 2.

###PART 3: Centralized Algorithm

1. Go to Bonus folder
2. Compile the program -> javac -cp . project2/CoordinatorProcess.java 
3. Run -> java project2.CoordinatorProcess 
4. Enter the complete path of the file: <path_till_Bonus_Folder>/Bonus/Counter.property

<<PLEASE DO NOT CREATE YOUR OWN Counter.property file>>
<<Because the encoding format used by Unix to create file makes java unable to read the property file>>
<<USE THE Counter.property provided in the Bonus folder>>

5. Repeat the same the steps as Berkeley Project from Step 4.
