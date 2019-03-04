# Grapple-frontend

This part of Grapple is used to handle the origin java project(*.java, *.class or *.jar).To use this part of Grapple, you need to install soot-2.5.0 and the libraries the object under test need in lib.
If you use a IDE to open the part, you also need to add those libs to you lib-path. Then

1）run edu.zuo.setree.client.IntraMain.

The args is same with soot(like -cp *.java).
The output is under ./intraOutput (if none ,create a folder first). You can change the path in edu.zuo.setree.export.Exporter(if changed, change edu.zuo.setree.intergraph.interGraph too).
Soot may have some problems in handle *.jar, so I write a *.sh file to run a list of test file like which in the test after I open the *.jar and get what in it.

2)run edu.zuo.setree.intergraph.interGraph.

The output is under ./interOutput which is the input of backend.