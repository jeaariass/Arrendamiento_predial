#!/usr/bin/env bash
# Uso: ./restaurar.sh [usuario_postgres] [nombre_base_datos]
set -e

USUARIO="${1:-postgres}"
BASEDATOS="${2:-arriendo_inmuebles}"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Creando base de datos '$BASEDATOS' (si no existe)..."
psql -U "$USUARIO" -h localhost -d postgres -c "CREATE DATABASE $BASEDATOS;" || true

echo "Restaurando datos desde $DIR/arriendo_inmuebles_dump.sql ..."
psql -U "$USUARIO" -h localhost -d "$BASEDATOS" -f "$DIR/arriendo_inmuebles_dump.sql"

echo "Listo. Ajusta application.properties con tu usuario/clave de Postgres si hace falta."
