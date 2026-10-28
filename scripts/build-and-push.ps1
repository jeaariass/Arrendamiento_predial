# Construye la imagen de arriendaya y la sube a GHCR, etiquetada con el commit corto.
# El build nunca ocurre en el VPS: Windows -> GHCR -> Portainer descarga.
#
# Uso:
#   docker login ghcr.io -u jeaariass          (una sola vez, token con scope write:packages)
#   .\scripts\build-and-push.ps1
#
# Despues, en el VPS: poner TAG=<commit7> en /srv/stacks/arriendaya/.env y correr
#   /srv/scripts/portainer-stack.sh arriendaya --pull

$ErrorActionPreference = "Stop"

$commit = (git rev-parse --short=7 HEAD).Trim()
$image = "ghcr.io/jeaariass/arriendaya-api:$commit"

Write-Host "Construyendo $image ..."
docker build -t $image -f (Join-Path $PSScriptRoot "..\Dockerfile") (Join-Path $PSScriptRoot "..")

Write-Host "Subiendo $image a GHCR..."
docker push $image

Write-Host ""
Write-Host "Listo. TAG a usar en el VPS: $commit"
