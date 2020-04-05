# JAVA_HOME ?= /usr/lib/jvm/java-8-openjdk-amd64
# Minecraft 1.12.2 requires Oracle Java. Later versions run on OpenJDK.
JAVA_HOME ?= ${PWD}/jdk-8u202

export JAVA_HOME

build:
	#cd fabric-1.14.4;./gradlew build
	#cd fabric-1.15.2;./gradlew build
	#cd fabric-1.16.x;./gradlew build
	#cd fabric-1.17.x;./gradlew build
	cd forge-1.12.2;./gradlew build # includes shadowJar
	#cd forge-1.14.4;./gradlew build
	#cd forge-1.15.2;./gradlew build
	#cd forge-1.16.x;./gradlew build
	rm -rf dist
	mkdir -p dist
	cp fabric-*/build/libs/*.jar dist/
	cp forge-*/build/libs/*.jar dist/
	rm dist/*-dev.jar

.PHONY: test
test:
	cd forge-1.14.4;./gradlew test jacocoTestReport
	# Test Coverage Report location:
	xdg-open ${PWD}/forge-1.14.4/build/reports/jacoco/test/html/index.html

.PHONY: test-server
test-server: build
	rm -rf ${PWD}/forge-1.12.2/tests/server/mods
	mkdir -p ${PWD}/forge-1.12.2/tests/server/mods
	cp dist/water-erosion-1.12.2-forge-*-full.jar ${PWD}/forge-1.12.2/tests/server/mods
	docker rm mc
	docker run -it -v ${PWD}/forge-1.12.2/tests/server:/data -e VERSION=1.12.2 -e TYPE=FORGE  -p 25565:25565 \
        -e EULA=TRUE --name mc itzg/minecraft-server


deps:
	# Get final Binary Code Licensed Oracle Java 8. Minecraft 1.12.2 crashes with OpenJDK 8.
	docker run --rm -it -v ${PWD}/jdk-8u202:/jdk-8u202 13rac1/alpine-java:jdk-8u202 cp -R /opt/jdk1.8.0_202/. /jdk-8u202

.PHONY: screenshots
screenshots:
	# Collect screenshots
	mkdir -p screenshots
	mv fabric*/run/screenshots/* screenshots/ || true
	mv forge*/run/screenshots/* screenshots/ || true

.PHONY: clean
clean:
	cd fabric-1.14.4;./gradlew clean
	cd fabric-1.15.2;./gradlew clean
	cd fabric-1.16.x;./gradlew clean
	cd fabric-1.17.x;./gradlew clean
	cd forge-1.14.4;./gradlew clean
	cd forge-1.15.2;./gradlew clean
	cd forge-1.16.x;./gradlew clean
	rm -rf dist

