#!/bin/sh
set -eu

if [ -z "${REDIS_USERNAME:-}" ] || [ -z "${REDIS_PASSWORD:-}" ]; then
  echo "REDIS_USERNAME e REDIS_PASSWORD devem ser definidos para iniciar o cache opcional."
  exit 1
fi

case "$REDIS_USERNAME" in
  *[!A-Za-z0-9_-]*)
    echo "REDIS_USERNAME contém caracteres inválidos."
    exit 1
    ;;
esac

password_hash="$(printf '%s' "$REDIS_PASSWORD" | sha256sum | awk '{print $1}')"
umask 077
acl_file="$(mktemp /tmp/sitiopro-users.XXXXXX)"
printf 'user default off\nuser %s on #%s ~sitiopro:* +@read +@write -@dangerous +keys +@connection\n' \
  "$REDIS_USERNAME" "$password_hash" > "$acl_file"
chown redis:redis "$acl_file"

exec /usr/local/bin/docker-entrypoint.sh redis-server \
  --save "" \
  --appendonly no \
  --aclfile "$acl_file"
