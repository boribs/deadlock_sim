.PHONY: build run grafica

all:
	@javac -encoding utf8 -d ./build/ src/*.java
	@java -classpath ./build/ src/Simulador.java

build:
	@javac -encoding utf8 -d ./build/ src/*.java

run:
	@java -classpath ./build/ src/Simulador.java
