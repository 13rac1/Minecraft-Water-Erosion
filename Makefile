# Expecting Java 17 in $PATH

.PHONY: build
build: forge neoforge fabric

.PHONY: forge
forge: dist
	# runData generates JSON worldgen configurations
	#cd forge;./gradlew runData
	cd forge;./gradlew build
	cp forge/build/libs/*.jar dist/

.PHONY: neoforge
neoforge: dist
	cd neoforge;./gradlew build
	cp neoforge/build/libs/*.jar dist/

.PHONY: fabric
fabric: dist
	cd fabric;./gradlew build
	cp fabric/build/libs/*.jar dist/

dist:
	mkdir -p dist

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
	mv neoforge/runs/client/screenshots/* screenshots/ || true

.PHONY: clean
clean:
	cd fabric;./gradlew clean
	cd forge;./gradlew clean
	cd neoforge;./gradlew clean
