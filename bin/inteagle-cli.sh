#!/bin/bash
# Inteagle Cloud SDK CLI Wrapper
# Usage: ./inteagle-cli.sh [options] [command]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$SCRIPT_DIR/inteagle-cli.jar"

# 加载 .env 配置文件
if [ -f "$SCRIPT_DIR/.env" ]; then
    set -a
    source "$SCRIPT_DIR/.env"
    set +a
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "CLI JAR not found. Building..."
    cd "$PROJECT_DIR"
    mvn test-compile assembly:single -DskipTests -q
fi

java -jar "$JAR_FILE" "$@"
