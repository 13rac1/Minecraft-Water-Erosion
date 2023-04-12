# Expecting Java 17 in $PATH

build:
	cd fabric;./gradlew build
	cd forge;./gradlew build
	mkdir -p dist
	cp fabric/build/libs/*.jar dist/
	cp forge/build/libs/*.jar dist/

.PHONY: test
test:
	cd forge;./gradlew test jacocoTestReport
	# Test Coverage Report location:
	# xdg-open ${PWD}/forge/build/reports/jacoco/test/html/index.html

.PHONY: screenshots
screenshots:
	# Collect screenshots
	mkdir -p screenshots
	mv fabric/run/screenshots/* screenshots/ || true
	mv forge/run/screenshots/* screenshots/ || true

.PHONY: clean
clean:
	cd fabric;./gradlew clean
	cd forge;./gradlew clean
