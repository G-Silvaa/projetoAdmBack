#!/bin/sh
# Converte a DATABASE_URL (injetada por plataformas como o PDC) nas variaveis
# DB_* que o application.yml espera. Formato esperado:
#   postgres://user:senha@host:porta/banco[?params]
# Quando DATABASE_URL nao existe, mantem os defaults locais (DB_* / yaml).
set -e

if [ -n "$DATABASE_URL" ]; then
  rest=${DATABASE_URL#*://}        # user:senha@host:porta/banco?params
  creds=${rest%@*}                 # user:senha  (divide no ULTIMO @: senha pode conter @)
  hostpart=${rest##*@}             # host:porta/banco?params
  hostport=${hostpart%%/*}         # host:porta
  pathq=${hostpart#*/}             # banco?params  (ou banco)

  user=${creds%%:*}
  case "$creds" in
    *:*) pass=${creds#*:} ;;
    *)   pass="" ;;
  esac

  host=${hostport%%:*}
  case "$hostport" in
    *:*) port=${hostport##*:} ;;
    *)   port=5432 ;;
  esac

  db=${pathq%%\?*}                 # banco (sem query string)
  query=""
  case "$pathq" in
    *\?*) query="?${pathq#*\?}" ;; # ex.: ?sslmode=require
  esac

  export DB_HOST="$host"
  export DB_PORT="$port"
  export DB_NAME="$db"
  export DB_USERNAME="$user"
  export DB_PASSWORD="$pass"

  # Se a URL trouxer parametros (ex.: sslmode=require) que o template DB_* nao
  # cobre, sobrescreve a URL completa do datasource (env tem prioridade no Spring).
  if [ -n "$query" ]; then
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${host}:${port}/${db}${query}"
    export SPRING_DATASOURCE_USERNAME="$user"
    export SPRING_DATASOURCE_PASSWORD="$pass"
  fi

  echo "[entrypoint] datasource -> ${host}:${port}/${db} (user=${user})"
fi

exec java -jar /app/liv-api.war
