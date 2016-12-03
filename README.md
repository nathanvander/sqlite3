# sqlite3
An implementation of sqlite3 in Java.

This depends on JNA available at https://github.com/java-native-access/jna.

To compile, include the following jar files in the classpath:
	\jna\win32-x86-64.jar
	\jna\jna-platform.jar
	\jna\jna.jar
	
Sqlite3 is available at https://github.com/mackyle/sqlite
or https://www.sqlite.org/download.html.  Get the amalgamation.

Changelog:
==========
11/8/2015   The current version is v4.  I changed the base package name to "apollo", because I am not following the
sqlite code as closely, and I want to provide a little separation from it.

11/25/2015. v9. I have decided just to do a wrapper around sqlite. Compare to sqlite4java available at:
https://bitbucket.org/almworks/sqlite4java/

Update 12/2/2016.  This project is obsolete and I am abandoning it.  Look at the "apollo" project at 
https://github.com/nathanvander/apollo for current improvements.  This was a very interesting learning experience,
however, rewriting SQLite in its entirety is a massive task.  I will delete this at some point.
