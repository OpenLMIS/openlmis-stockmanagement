#!/bin/sh

# This script initializes the Transifex client. It then uploads the source resource to the 
# Transifex project and downloads the translated resources into the build.

# Some things to note:
# - The .tx folder, if it exists, is deleted beforehand so that the 'tx init' does not need to 
#   prompt to overwrite it.
# - The .tx folder is generated each time, rather than saving the .tx/config file into version 
#   control, because a .transifexrc also needs to be generated using the username and password.
#   Since these credentials should not be in version control, the regeneration approach is used.

rm -rf .tx
tx init --host=https://www.transifex.com --user=$TRANSIFEX_USER --pass=$TRANSIFEX_PASSWORD
tx set --auto-local -r openlmis-template-service.messages \
    'src/main/resources/messages_<lang>.properties' --source-lang en --type UNICODEPROPERTIES \
    --source-file src/main/resources/messages_en.properties --execute
tx push -s
tx pull -a -f
