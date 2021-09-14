include .env
SQL_MIGRATION_IMAGE="pul-sql-migration:${VERSION}"
APP_IMAGE="pul-api:${VERSION}"

.PHONY: all api

all: build

mvn-clean:
	mvn clean

mvn-package:
	mvn package -Dmaven.test.skip=true -Drevision="${VERSION}"

build-api: mvn-clean mvn-package
	docker build -t ${APP_IMAGE} .

build-sql-migration:
	cd sql-migration && docker build -t ${SQL_MIGRATION_IMAGE} .

build: build-api build-sql-migration