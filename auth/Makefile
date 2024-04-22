PROJECT_NAME=auth
GITHUB_TOKEN=

.DEFAULT_GOAL := test

docker-pull:
	docker-compose --project-name ${PROJECT_NAME} -f deployments/docker-compose-test.yaml build --pull
	docker-compose --project-name ${PROJECT_NAME} -f deployments/docker-compose-test.yaml pull

docker-restart: docker-stop docker-start

docker-start:
	docker-compose --project-name ${PROJECT_NAME} -f deployments/docker-compose-test.yaml up -d

docker-stop:
	docker-compose --project-name ${PROJECT_NAME} -f deployments/docker-compose-test.yaml down

new-migration: export BUN_TIMESTAMP=$(shell date +%Y%m%d%H%M%S | head -c 14)
new-migration:
	touch database/migrations/${BUN_TIMESTAMP}_new.go
	cat database/migrations/migration.go.tmpl > database/migrations/${BUN_TIMESTAMP}_new.go
	sed -i 's/CHANGEME/${BUN_TIMESTAMP}/g' database/migrations/${BUN_TIMESTAMP}_new.go

sonar: export PWD=$(shell pwd)
sonar:
	-rm coverage.out test-report.out
	go test -race -json -coverprofile=coverage.out -covermode=atomic ./... > test-report.out
	docker run \
		--rm \
		-e SONAR_HOST_URL="${SONAR_HOST_URL}" \
		-e SONAR_TOKEN="${SONAR_TOKEN}" \
		-v "$(PWD):/usr/src" \
		sonarsource/sonar-scanner-cli

test:
	go test -cover ./...

.PHONY: docker-pull docker-restart docker-start docker-stop new-migration sonar test
