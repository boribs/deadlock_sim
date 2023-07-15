.PHONY: build run grafica

all:
	@javac -encoding utf8 -d ./build/ *.java
	@java -classpath ./build/ Simulador.java

build:
	@javac -encoding utf8 -d ./build/ *.java

run:
	@java -classpath ./build/ Simulador.java

grafica:
	@javac -encoding utf8 -d ./build/ *.java
	@java -classpath ./build/ Graficador.java
