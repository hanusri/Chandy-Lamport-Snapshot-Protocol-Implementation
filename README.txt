Project Description
-------------------

This project consists of three parts.
1.1 Part 1
----------
Implement a distributed system consisting of n nodes, numbered 0 to n − 1, arranged in a certain
topology. The topology and information about other parameters is provided in a configuration file.
All channels in the system are bidirectional, reliable and satisfy the first-in-first-out (FIFO)
property. You can implement a channel using a reliable socket connection (with TCP or SCTP).
For each channel, the socket connection should be created at the beginning of the program and
should stay intact until the end of the program. All messages between neighboring nodes are
exchanged over these connections.
All nodes execute the following protocol:
• Initially, each node in the system is either active or passive. At least one node must be
active at the beginning of the protocol.
• While a node is active, it sends anywhere from minPerActive to maxPerActive messages, and
then turns passive. For each message, it makes a uniformly random selection of one of its
neighbors as the destination. Also, if the node stays active after sending a message, then it
waits for at least minSendDelay time units before sending the next message.
• Only an active node can send a message.
• A passive node, on receiving a message, becomes active if it has sent fewer than maxNumber
messages (summed over all active intervals). Otherwise, it stays passive.
We refer to the protocol described above as the MAP protocol.

1.2 Part 2
----------
Implement the Chandy and Lamport’s protocol for recording a consistent global snapshot as dis-
cussed in the class. Assume that the snapshot protocol is always initiated by node 0 and all
channels in the topology are bidirectional. Use the snapshot protocol to detect the termination of
the MAP protocol described in Part 1. The MAP protocol terminates when all nodes are passive
and all channels are empty. To detect termination of the MAP protocol, augment the Chandy
and Lamport’s snapshot protocol to collect the information recorded at each node at node 0 using
a converge-cast operation over a spanning tree. The tree can be built once in the beginning or
on-the-fly for an instance using MARKER messages.
Note that, in this project, the messages exchanged by the MAP protocol are application mes-
sages and the messages exchanged by the snapshot protocol are control messages. The rules of the
MAP protocol (described in Part 1) only apply to application messages. They do not apply to
control messages.
Testing Correctness of the Snapshot Protocol Implementation
To test that your implementation of the Chandy and Lamport’s snapshot protocol is correct,
implement Fidge/Mattern’s vector clock protocol described in the class. The vector clock of a node
is part of the local state of the node and its value is also recorded whenever a node records its local
state. Node 0, on receiving the information recorded by all the nodes, uses these vector timestamps
to verify that the snapshot is indeed consistent. Note that only application messages will carry
vector timestamps.
1.3 Part 3
----------
Once node 0 detects that the MAP protocol has terminated, it broadcasts a FINISH message to all
processes. A process, on receiving a FINISH message, stops executing. Eventually, all processes
stop executing and the entire system is brought to a halt.


Project Files
-------------
All the java files related to the project are in the package utd.com. Hence, the project files must be under the folder utd->com->project files. The project is available with a launcher.sh to launch the application in various machines as mentioned in the config file.

There is a cleanup.sh uploaded along with the project to do final cleanup of any process(in case) are still running. This file is useful only in case of application has faced a fatal error and user wants to kill all processes related to the application.

How to Run the application
--------------------------
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