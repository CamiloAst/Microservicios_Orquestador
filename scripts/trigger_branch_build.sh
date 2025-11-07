#!/bin/sh
set -eu

# =================== CONFIG ===================
JENKINS_URL="${JENKINS_URL:-http://jenkins:8080}"     # dentro de la red docker
MBP_PATH="${MBP_PATH:-/job/Pruebas-Automatizadas}"     # /job/<NOMBRE_DEL_MULTIBRANCH>
BRANCH="${BRANCH:-main}"                               # rama a disparar
USER="${JENKINS_USER:?Falta JENKINS_USER}"
API_TOKEN="${JENKINS_API_TOKEN:?Falta JENKINS_API_TOKEN}"
PARAMS="${PARAMS:-}"                                   # "K=V&K2=V2" si tu job pide parámetros

# =================== WAIT FOR JENKINS ===================
echo "⏳ Esperando a Jenkins en $JENKINS_URL ..."
until curl -fsS "$JENKINS_URL/login" >/dev/null 2>&1; do
  sleep 3
done

# =================== BRANCH URL-ENCODE ===================
BRANCH_ENC="$(printf '%s' "$BRANCH" | sed 's|/|%2F|g')"
JOB_URL="$JENKINS_URL${MBP_PATH}/job/${BRANCH_ENC}"

# =================== GET CRUMB (CSRF) ===================
echo "🔑 Obteniendo CSRF crumb (si aplica)..."
CRUMB_HEADER=""
if CRUMB_JSON="$(curl -fsS -u "$USER:$API_TOKEN" "$JENKINS_URL/crumbIssuer/api/json" 2>/dev/null)"; then
  CRUMB_HEADER="$(echo "$CRUMB_JSON" | sed -n 's/.*"crumbRequestField":"\([^"]*\)".*"crumb":"\([^"]*\)".*/\1: \2/p')"
fi

# =================== TRIGGER BUILD ===================
TRIGGER_URL="$JOB_URL/build"
[ -n "$PARAMS" ] && TRIGGER_URL="$JOB_URL/buildWithParameters?$PARAMS"

echo "🚀 Disparando build: $TRIGGER_URL"
if [ -n "$CRUMB_HEADER" ]; then
  curl -fsS -u "$USER:$API_TOKEN" -H "$CRUMB_HEADER" -X POST "$TRIGGER_URL" >/dev/null
else
  curl -fsS -u "$USER:$API_TOKEN" -X POST "$TRIGGER_URL" >/dev/null
fi

echo "✅ Build encolado para $JOB_URL"
echo "ℹ️  Revisa la cola: $JENKINS_URL/queue/"
