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
