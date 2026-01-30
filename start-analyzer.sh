#!/bin/bash
cd "$(dirname "$0")/apps/analyzer"
poetry run uvicorn analyzer.main:app --reload --port 8001
