#!/usr/bin/env bash
set -euo pipefail

SONAR_URL=${SONAR_URL:-http://localhost:9000}
SONAR_TOKEN=${SONAR_TOKEN:?Falta SONAR_TOKEN}
auth_hdr="Authorization: Bearer $SONAR_TOKEN"

OUT=./sonarqube/export
mkdir -p "$OUT"

echo "[Sonar] plugins instalados…"
curl -s -H "$auth_hdr" "$SONAR_URL/api/plugins/installed" > "$OUT/plugins_installed.json"

echo "[Sonar] quality profiles (índice)…"
curl -s -H "$auth_hdr" "$SONAR_URL/api/qualityprofiles/search" > "$OUT/qualityprofiles_search.json"

echo "[Sonar] lenguajes…"
LANGS=$(curl -s -H "$auth_hdr" "$SONAR_URL/api/languages/list" | jq -r '.languages[].key' || true)
[ -z "${LANGS:-}" ] && LANGS="java js ts py cs go cpp"

echo "[Sonar] exportando perfiles por lenguaje…"
for lang in $LANGS; do
  curl -sf -H "$auth_hdr" "$SONAR_URL/api/qualityprofiles/backup?language=$lang" \
    -o "$OUT/qualityprofile_${lang}.xml" || true
done

echo "[Sonar] quality gates (lista + detalle)…"
curl -s -H "$auth_hdr" "$SONAR_URL/api/qualitygates/list" > "$OUT/qualitygates_list.json"
for name in $(jq -r '.qualityGates[].name' "$OUT/qualitygates_list.json"); do
  enc=$(python - <<PY
import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1]))
PY
"$name")
  safe="${name// /_}"
  curl -s -H "$auth_hdr" "$SONAR_URL/api/qualitygates/show?name=$enc" \
    > "$OUT/qualitygate_${safe}.json"
done

echo "[Sonar] Export listo ✅ ($OUT)"
