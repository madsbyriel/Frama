@echo off
javac OptionsMaker.java
java OptionsMaker
javac "@options"
java "@runargs"