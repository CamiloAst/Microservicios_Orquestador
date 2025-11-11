# ===== Makefile para Microservicios/Infra =====
# Uso rápido:
#   make up            # Levantar todo
#   make export-jenkins
#   make export-sonar
#   make backup        # Backup Jenkins + Sonar DB
#   make seed-sonar    # Re-crear gates/profiles desde export
#   make down          # Apagar (conservando volúmenes)
#   make nuke          # ⚠️ Apaga y borra volúmenes (requiere CONFIRM=NUKE)
#   make help

SHELL := /bin/bash

ENV_FILE      ?= .env
DC            := docker compose --env-file $(ENV_FILE)

# ===== Helpers =====
.PHONY: help ensure-scripts ensure-tools env

help:
	@echo "Targets disponibles:"
	@echo "  up               Levanta todos los servicios (build donde aplique)."
	@echo "  down             Apaga contenedores (no borra volúmenes)."
	@echo "  restart          Reinicia servicios."
	@echo "  ps               Lista estado de contenedores."
	@echo "  logs             Sigue logs agregados (Ctrl+C para salir)."
	@echo "  export-jenkins   Exporta plugins.txt + jenkins.yaml (JCasC) + backup del home."
	@echo "  export-sonar     Exporta plugins, quality profiles y quality gates."
	@echo "  seed-sonar       Ejecuta sidecar para recrear gates/profiles desde export."
	@echo "  backup           Backup Jenkins (volumen) y Sonar DB (pg_dump)."
	@echo "  nuke             ⚠️ down -v (borra volúmenes). Requiere CONFIRM=NUKE."
	@echo "Variables:"
	@echo "  ENV_FILE=$(ENV_FILE)"

env:
	@echo "Usando variables de $(ENV_FILE)"
	@grep -E '^[A-Za-z0-9_]+=' $(ENV_FILE) | sed 's/^/  /' || true

ensure-scripts:
	@test -f scripts/export_jenkins.sh || { echo "Falta scripts/export_jenkins.sh"; exit 1; }
	@test -f scripts/export_sonar.sh   || { echo "Falta scripts/export_sonar.sh"; exit 1; }
	@test -f sonarqube/bootstrap/seed.sh || { echo "Falta sonarqube/bootstrap/seed.sh"; exit 1; }
	@chmod +x scripts/export_jenkins.sh scripts/export_sonar.sh sonarqube/bootstrap/seed.sh || true

ensure-tools:
	@command -v curl >/dev/null 2>&1 || { echo "Necesitas curl"; exit 1; }
	@command -v jq   >/dev/null 2>&1 || { echo "Necesitas jq"; exit 1; }

# ===== Docker Compose =====
.PHONY: up down restart ps logs

up:
	$(DC) up -d --build

down:
	$(DC) down

restart:
	$(DC) down
	$(DC) up -d

ps:
	$(DC) ps

logs:
	$(DC) logs -f

# ===== Export =====
.PHONY: export-jenkins export-sonar

export-jenkins: ensure-scripts ensure-tools
	@set -euo pipefail; \
	set -o allexport; source $(ENV_FILE); set +o allexport; \
	bash scripts/export_jenkins.sh

export-sonar: ensure-scripts ensure-tools
	@set -euo pipefail; \
	set -o allexport; source $(ENV_FILE); set +o allexport; \
	: $${SONAR_URL:?Falta SONAR_URL en .env}; \
	: $${SONAR_TOKEN:?Falta SONAR_TOKEN en .env}; \
	bash scripts/export_sonar.sh

# ===== Seed SonarQube =====
.PHONY: seed-sonar
seed-sonar:
	@set -euo pipefail; \
	set -o allexport; source $(ENV_FILE); set +o allexport; \
	: $${SONAR_TOKEN:?Falta SONAR_TOKEN en .env}; \
	$(DC) run --rm sonarqube-seed

# ===== Backups =====
.PHONY: backup backup-jenkins backup-sonar

backup: backup-jenkins backup-sonar
	@echo "Backups completados."

backup-jenkins:
	@mkdir -p backups
	@docker run --rm -v jenkins_home:/data -v "$$PWD/backups":/backup alpine \
		sh -c "tar czf /backup/jenkins_home_$$(date +%F).tgz -C /data ."
	@echo "Jenkins backup -> backups/jenkins_home_$$(date +%F).tgz"

backup-sonar:
	@mkdir -p backups
	@$(DC) exec -T sonar-db pg_dump -U $${SONAR_DB_USER:-sonar} -d $${SONAR_DB_NAME:-sonarqube} | gzip > backups/sonar_$$(date +%F).sql.gz
	@echo "Sonar DB backup -> backups/sonar_$$(date +%F).sql.gz"

# ===== DANGER ZONE =====
.PHONY: nuke
nuke:
	@if [ "$(CONFIRM)" != "NUKE" ]; then \
		echo "⚠️  Esto hará: docker compose down -v (BORRA volúmenes: jenkins_home, sonar_data, grafana_data, etc.)"; \
		echo "Aborta o ejecuta: make nuke CONFIRM=NUKE"; \
		exit 1; \
	fi
	$(DC) down -v
