#!/usr/bin/env bash
set -euo pipefail

: "${ELASTIC_PASSWORD:?ELASTIC_PASSWORD is required}"
: "${KIBANA_SYSTEM_PASSWORD:?KIBANA_SYSTEM_PASSWORD is required}"

ES_URL="${ELASTICSEARCH_URL:-http://elasticsearch:9200}"

until curl -fsS -u "elastic:${ELASTIC_PASSWORD}" \
    "${ES_URL}/_cluster/health?wait_for_status=yellow&timeout=5s" >/dev/null; do
  sleep 5
done

curl -fsS -u "elastic:${ELASTIC_PASSWORD}" \
  -H "Content-Type: application/json" \
  -X POST "${ES_URL}/_security/user/kibana_system/_password" \
  -d "{\"password\":\"${KIBANA_SYSTEM_PASSWORD}\"}" >/dev/null

curl -fsS -u "elastic:${ELASTIC_PASSWORD}" \
  -H "Content-Type: application/json" \
  -X PUT "${ES_URL}/_ilm/policy/sitiopro-observability-30d" \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "actions": {
            "rollover": {
              "max_age": "1d",
              "max_primary_shard_size": "512mb"
            }
          }
        },
        "delete": {
          "min_age": "30d",
          "actions": {
            "delete": {}
          }
        }
      }
    }
  }' >/dev/null

curl -fsS -u "elastic:${ELASTIC_PASSWORD}" \
  -H "Content-Type: application/json" \
  -X PUT "${ES_URL}/_index_template/sitiopro-logs-template" \
  -d '{
    "index_patterns": [
      "logs-sitiopro.*-*",
      "logs-nginx.access-*",
      "logs-mssql.log-*"
    ],
    "priority": 250,
    "data_stream": {},
    "template": {
      "settings": {
        "index.lifecycle.name": "sitiopro-observability-30d"
      }
    },
    "_meta": {
      "description": "Retencao padrao dos logs tecnicos do SitioPro"
    }
  }' >/dev/null

curl -fsS -u "elastic:${ELASTIC_PASSWORD}" \
  -H "Content-Type: application/json" \
  -X PUT "${ES_URL}/_index_template/sitiopro-docker-metrics-template" \
  -d '{
    "index_patterns": [
      "metrics-docker.*-*",
      "metrics-system.*-*",
      "metrics-apm.*-*"
    ],
    "priority": 250,
    "data_stream": {},
    "template": {
      "settings": {
        "index.lifecycle.name": "sitiopro-observability-30d"
      }
    },
    "_meta": {
      "description": "Retencao padrao das metricas tecnicas do SitioPro"
    }
  }' >/dev/null

echo "Elastic bootstrap completed."
