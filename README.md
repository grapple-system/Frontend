# Grapple-frontend

Welcome to the repository of Grapple-frontend. It's written primarily in Java.

This part of Grapple is used to handle the origin java project(*.java, *.class or *.jar) and generates graphs as input for
the backend. For details of how those graph represented and what they mean, please refer to the paper.

# Getting Started
## Required Libraries
To use this part of Grapple, you have to install soot-2.5.0 and the libraries the test java project need.
If you use a IDEA to open this part, you also need to add those libs to you lib-path.

## Compiling 
Recommend to use an IDEA(like Eclipse or IntelliJ) to construct and compile this part.

##  Running
In IDEA, just click the run button.

    run edu.zuo.setree.client.IntraMain

Remember to edit configurations. The program arguments are same with soot(like -cp *.java).
The output is under ./intraOutput (if none ,create a folder first). You can change the path in edu.zuo.setree.export.Exporter(if changed, change edu.zuo.setree.intergraph.interGraph too).
Soot may have some problems in handling *.jar, so I write a *.sh file to run a list of test file like which in the test after I open the *.jar and get what in it.

    run edu.zuo.setree.intergraph.interGraph

The output is under ./interOutput which is the input of backend.

# Questions or Comments?


# Project Contributors

