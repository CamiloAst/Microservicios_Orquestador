#!/usr/bin/env bash
set -euo pipefail

JENKINS_URL=${JENKINS_URL:-http://host.docker.internal:8083}
JENKINS_USER=${JENKINS_USER:?Falta JENKINS_USER}
JENKINS_API_TOKEN=${JENKINS_API_TOKEN:?Falta JENKINS_API_TOKEN}

mkdir -p ./backups ./jenkins/jcasc

echo "[Jenkins] CSRF crumb…"
CRUMB=$(curl -s -u "$JENKINS_USER:$JENKINS_API_TOKEN" "$JENKINS_URL/crumbIssuer/api/json" | jq -r .crumb)

echo "[Jenkins] plugins.txt…"
curl -s -u "$JENKINS_USER:$JENKINS_API_TOKEN" -H "Jenkins-Crumb: $CRUMB" \
  --data-urlencode 'script=Jenkins.instance.pluginManager.plugins.each{p -> println("${p.shortName}:${p.version}") }' \
  "$JENKINS_URL/scriptText" > ./jenkins/plugins.txt

echo "[Jenkins] jenkins.yaml (JCasC)…"
curl -s -u "$JENKINS_USER:$JENKINS_API_TOKEN" \
  "$JENKINS_URL/configuration-as-code/export?pretty" > ./jenkins/jcasc/jenkins.yaml

echo "[Jenkins] backup jenkins_home…"
docker run --rm -v jenkins_home:/data -v "$PWD/backups":/backup alpine \
  sh -c "tar czf /backup/jenkins_home_$(date +%F).tgz -C /data ."

echo "[Jenkins] Export listo ✅"
