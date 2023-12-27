#!/bin/bash
#
# Start script for extensions-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "extensions-api.jar"
