JAVA_HOME ?= /usr/lib/jvm/java-8-openjdk-amd64
export JAVA_HOME

build:
	cd fabric-1.14.4;./gradlew build
	cd fabric-1.15.2;./gradlew build
	cd fabric-1.16.x;./gradlew build
	cd forge-1.14.4;./gradlew build
	cd forge-1.15.2;./gradlew build
	rm -rf dist
	mkdir -p dist
	cp fabric-*/build/libs/*.jar dist/
	cp forge-*/build/libs/*.jar dist/
	rm dist/*-dev.jar

.PHONY: screenshots
screenshots:
	# Collect screenshots
	mkdir -p screenshots
	mv fabric*/run/screenshots/* screenshots/ || true
	mv forge*/run/screenshots/* screenshots/ || true

clean:
	cd fabric-1.14.4;./gradlew clean
	cd fabric-1.15.2;./gradlew clean
	cd fabric-1.16.x;./gradlew clean
	cd forge-1.14.4;./gradlew clean
	cd forge-1.15.2;./gradlew clean
	rm -rf dist

