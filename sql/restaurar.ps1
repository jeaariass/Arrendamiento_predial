param(
    [string]$Usuario = "postgres",
    [string]$BaseDatos = "arriendo_inmuebles"
)

$psql = "psql"
if (Test-Path "C:\Program Files\PostgreSQL\18\bin\psql.exe") {
    $psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
}

$dump = Join-Path $PSScriptRoot "arriendo_inmuebles_dump.sql"

Write-Host "Creando base de datos '$BaseDatos' (si no existe)..."
& $psql -U $Usuario -h localhost -d postgres -c "CREATE DATABASE $BaseDatos;"

Write-Host "Restaurando datos desde $dump ..."
& $psql -U $Usuario -h localhost -d $BaseDatos -f $dump

Write-Host "Listo. Ajusta application.properties con tu usuario/clave de Postgres si hace falta."
