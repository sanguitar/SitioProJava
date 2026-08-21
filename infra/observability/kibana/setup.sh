#!/usr/bin/env bash
set -euo pipefail

: "${ELASTIC_PASSWORD:?ELASTIC_PASSWORD is required}"

KIBANA_URL="${KIBANA_URL:-http://kibana:5601}"
AUTH="elastic:${ELASTIC_PASSWORD}"

until curl -fsS -u "${AUTH}" "${KIBANA_URL}/api/status" >/dev/null; do
  sleep 5
done

kibana_json_request() {
  local method="$1"
  local endpoint="$2"
  local response_file
  local http_status
  shift 2

  response_file="$(mktemp)"
  http_status="$(
    curl -sS -o "${response_file}" -w "%{http_code}" -u "${AUTH}" \
      -H "kbn-xsrf: true" \
      -H "Content-Type: application/json" \
      -X "${method}" "${KIBANA_URL}${endpoint}" \
      "$@"
  )"

  if [[ "${http_status}" -lt 200 || "${http_status}" -ge 300 ]]; then
    echo "Kibana request failed: ${method} ${endpoint} returned ${http_status}" >&2
    cat "${response_file}" >&2
    rm -f "${response_file}"
    return 1
  fi

  rm -f "${response_file}"
}

create_data_view() {
  local id="$1"
  local title="$2"
  local name="$3"

  kibana_json_request POST "/api/data_views/data_view" -d "{
      \"override\": true,
      \"data_view\": {
        \"id\": \"${id}\",
        \"title\": \"${title}\",
        \"name\": \"${name}\",
        \"timeFieldName\": \"@timestamp\"
      }
    }"
}

upsert_dashboard() {
  local id="$1"
  local file="$2"

  kibana_json_request PUT "/api/dashboards/${id}" --data-binary "@${file}"
}

create_data_view "sitiopro-logs" \
  "logs-sitiopro.app-*,logs-nginx.access-*,logs-nginx.error-*,logs-mssql.log-*" \
  "Sítio Guaratinguetá - Logs"

create_data_view "sitiopro-apm" \
  "traces-apm-*,logs-apm.error-*,metrics-apm.*-*" \
  "Sítio Guaratinguetá - APM"

create_data_view "sitiopro-docker" \
  "metrics-docker.*-*" \
  "Sítio Guaratinguetá - Docker"

upsert_dashboard "sitiopro-system-overview" "/dashboards/system-overview.json"
upsert_dashboard "sitiopro-api" "/dashboards/api.json"
upsert_dashboard "sitiopro-external-integrations" "/dashboards/external-integrations.json"

echo "Kibana bootstrap completed."
