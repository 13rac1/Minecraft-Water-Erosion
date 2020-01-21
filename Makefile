JAVA_HOME ?= /usr/lib/jvm/java-8-openjdk-amd64
export JAVA_HOME

build:
	cd fabric-1.14.4;./gradlew build
	cd fabric-1.15.2;./gradlew build
	cd forge-1.14.4;./gradlew build
	mkdir -p dist
	cp fabric-*/build/libs/water-erosion-*-fabric-[0-9].[0-9].[0-9].jar dist/
	cp forge-*/build/libs/water-erosion-*-forge-[0-9].[0-9].[0-9]-*.jar dist/

clean:
	cd fabric-1.14.4;./gradlew clean
	cd fabric-1.15.2;./gradlew clean
	cd forge-1.14.4;./gradlew clean
	rm -rf dist

