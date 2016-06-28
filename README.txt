Instruction to run the application

All the java files related to the project are in the package utd.com. Hence, the project files must be under the folder utd->com->project files. The project is available with a launcher.sh to launch the application in various machines as mentioned in the config file.

There is a cleanup.sh uploaded along with the project to do final cleanup of any process(in case) are still running. This file is useful only in case of application has faced a fatal error and user wants to kill all processes related to the application.

How to Run the application?
1.	Copy project files under CS6378/Project1 in the folder utd/com
2.	Run the following command in Project1 folder
	javac -d ./bin/ utd/com/NodeRunner.java
3.	Place the configuration file with the file name as config.txt under Project1 folder
4.	Once the project is compiled, run the following command in Project1 folder
	./launcher.sh
5.	Now all the processes will be running as configured in configuration file. Log files will be created for each Node in Project1 folder.
6.	Once all processes becomes passive, the snapshot protocol brings all processes to halt. Output files gets created with snapshot information as mentioned in the project description under the folder Project1.
7.	In case, if the application faced any fatal error, run the following command under Project1 folder
	./cleanup.sh