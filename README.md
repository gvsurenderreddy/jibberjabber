jibberjabber
============

Jabber (XMPP) bridge for shell scripts.

License: LGPL
Incorporates Smack API (Open Source Apache License): http://www.igniterealtime.org/projects/smack/
JarJar (Apache License 2.0): https://code.google.com/p/jarjar/
LGPL Source from http://devdaily.com/java/java-processbuilder-process-system-exec


This is a simple Java application that lets you run scripts VIA Jabber (XMPP, or Google Talk). It makes it very simple to expose scripts behind a firewall, through Jabber. Reasonable attempts have been made to prevent attacks, but use at your own risk.

One example of using JibberJabber is for an interface to TaskWarrior. Create a script like this:

cat task.sh

 #!/bin/sh
 yes | /usr/local/bin/task $@


You can then do this from Google Talk:

 task add due:tomorrow Something

JibberJabber treats the first word as the script name. Every other word (space delimited) is passed as a command line option. STDOUT and STDERR will be returned to you in Google Talk.

This makes it easy to expose scripts and access them from anywhere Google Talk works (phone, web interface, etc). 

To provide reasonable security, script names are stripped using a regular expression, and have an extension appended to them (to prevent directory traversal, etc). Scripts will only be run from a designated path. Furthermore, the entire body of the XMPP message can be cleaned using a paranoia regular expression.

This application does NOT check the from address of the XMPP packet. It is assumed that the server (Google Talk) will control who can message your account. You need to log into to Google Talk or your XMPP server and control your permissions there, first. In other words, test with an IM client before using JibberJabber to make sure your IMs get through.


Building
--------

ant all


Note: Although written in Java, it has ONLY been tested under Linux. It should work under UNIX(like) operating systems. 


Usage
-----

Look at jibberjabber.properties.example, edit appropriately, and save as jibberjabber.properties.

java -jar jibberjabber.jar


