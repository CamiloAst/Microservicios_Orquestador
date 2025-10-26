#!/bin/sh
set -eu

# =================== CONFIG ===================
JENKINS_URL="${JENKINS_URL:-http://jenkins:8080}"   # dentro de la red docker
MBP_PATH="${MBP_PATH:-/job/Auth-app}"                # /job/<NOMBRE_DEL_MULTIBRANCH>
BRANCH="${BRANCH:-main}"                             # rama a disparar (se url-encodea abajo)
USER="${JENKINS_USER}"
API_TOKEN="${JENKINS_API_TOKEN}"
PARAMS="${PARAMS:-}"                                 # "K=V&K2=V2" si tu job pide parámetros

# =================== WAIT FOR JENKINS ===================
echo "⏳ Esperando a Jenkins en $JENKINS_URL ..."
until curl -fsS "$JENKINS_URL/login" >/dev/null 2>&1; do
  sleep 3
done

# =================== BRANCH URL-ENCODE ===================
BRANCH_ENC="$(printf '%s' "$BRANCH" | sed 's|/|%2F|g')"
JOB_URL="$JENKINS_URL${MBP_PATH}/job/${BRANCH_ENC}"

# =================== GET CRUMB (CSRF) ===================
echo "🔑 Obteniendo CSRF crumb..."
CRUMB_JSON="$(curl -fsS -u "$USER:$API_TOKEN" "$JENKINS_URL/crumbIssuer/api/json")" || {
  echo "⚠️ No se pudo obtener crumb. ¿Usuario/token correctos?"
  exit 1
}
CRUMB_HEADER="$(echo "$CRUMB_JSON" | sed -n 's/.*"crumbRequestField":"\([^"]*\)".*"crumb":"\([^"]*\)".*/\1: \2/p')"

# =================== TRIGGER BUILD ===================
echo "🚀 Disparando build: $JOB_URL ..."
if [ -n "$PARAMS" ]; then
  curl -fsS -u "$USER:$API_TOKEN" -H "$CRUMB_HEADER" -X POST \
    "$JOB_URL/buildWithParameters?$PARAMS" >/dev/null
else
  curl -fsS -u "$USER:$API_TOKEN" -H "$CRUMB_HEADER" -X POST \
    "$JOB_URL/build" >/dev/null
fi

echo "✅ Build encolado para $JOB_URL"
