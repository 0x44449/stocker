#!/bin/bash
cd "$(dirname "$0")/apps/ingest"
./gradlew bootRun --console=plain
