#!/usr/bin/env bash
set -euo pipefail

SONAR_URL=${SONAR_URL:-http://sonarqube:9000}
# usa token (mejor que user/pass)
SONAR_TOKEN=${SONAR_TOKEN:?Falta SONAR_TOKEN}
auth_hdr="Authorization: Bearer $SONAR_TOKEN"

echo "Esperando SonarQube en $SONAR_URL…"
until curl -sf "$SONAR_URL/api/system/status" | jq -e '.status|test("UP|DB_MIGRATION_NEEDED")' >/dev/null; do
  sleep 3
done
echo "SonarQube OK"

# IMPORTAR PROFILES (opcional si ya versionas XML)
if ls /seed/export/qualityprofile_*.xml >/dev/null 2>&1; then
  echo "Restaurando Quality Profiles…"
  for f in /seed/export/qualityprofile_*.xml; do
    echo "  -> $(basename "$f")"
    curl -sf -H "$auth_hdr" -F "backup=@$f" "$SONAR_URL/api/qualityprofiles/restore" >/dev/null || true
  done
fi

# RE-CREAR QUALITY GATES desde JSON exportados
if ls /seed/export/qualitygate_*.json >/dev/null 2>&1; then
  echo "Re-creando Quality Gates…"
  for f in /seed/export/qualitygate_*.json; do
    NAME=$(jq -r '.name' "$f")
    echo "  Gate: $NAME"
    curl -sf -H "$auth_hdr" -X POST "$SONAR_URL/api/qualitygates/create" -d "name=$NAME" >/dev/null || true
    jq -c '.conditions[]' "$f" | while read C; do
      METRIC=$(echo "$C" | jq -r '.metric')
      OP=$(echo "$C" | jq -r '.op')
      ERROR=$(echo "$C" | jq -r '.error // empty')
      WARN=$(echo "$C" | jq -r '.warning // empty')
      ARGS=(-d "gateName=$NAME" -d "metric=$METRIC" -d "op=$OP")
      [ -n "$ERROR" ] && ARGS+=(-d "error=$ERROR")
      [ -n "$WARN" ]  && ARGS+=(-d "warning=$WARN")
      curl -sf -H "$auth_hdr" -X POST "$SONAR_URL/api/qualitygates/create_condition" "${ARGS[@]}" >/dev/null || true
    done
  done
fi

# (Opcional) Webhook a Jenkins
if [ -n "${JENKINS_URL:-}" ]; then
  echo "Creando webhook a Jenkins…"
  curl -sf -H "$auth_hdr" -X POST "$SONAR_URL/api/webhooks/create" \
    -d name="jenkins" -d url="${JENKINS_URL%/}/sonarqube-webhook/" >/dev/null || true
fi

echo "Seed Sonar listo ✅"
