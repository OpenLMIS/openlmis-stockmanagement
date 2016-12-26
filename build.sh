#!/bin/sh

# Sync with Transifex
./sync_transifex.sh

# Run Gradle build
gradle clean build
