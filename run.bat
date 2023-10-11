@echo off
javac OptionsMaker.java
java OptionsMaker
javac "@options"
java -Xmx20M "@runargs"