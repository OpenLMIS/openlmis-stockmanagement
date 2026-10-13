#!/bin/sh

# Sync with Transifex
/transifex/sync_transifex.sh \
  --resource openlmis-stockmanagement \
  --pattern 'src/main/resources/messages_<lang>.properties' \
  --source-file src/main/resources/messages_en.properties

# Run Gradle build
gradle clean build || exit 1

# Publish to Maven (see publishing.gradle). A failure or a hang here must not stop the image from being built, so it
# only leaves a marker for the Jenkinsfile, which marks the build unstable.
timeout 1800 gradle uploadArchives || touch build/maven-publish-failed
