#!/bin/sh

# This script populates the database with demo data for presentational and testing purposes.
# It searches for json files in given directory and inserts the contained records into the database.
# Note: file name should refer to their target table name.

DIRECTORY=${1}
GENERATOR=${2}
OUTPUT_DIR=${DIRECTORY}/../build/demo-data
TEST_OUTPUT_DIR=${DIRECTORY}/../build/resources/test/demo-data


# Get list of JSON files in current directory
FILES=`find ${DIRECTORY} -name "*.json"`

# Run database input generation
${GENERATOR} ${FILES}

mkdir ${OUTPUT_DIR}
mkdir ${TEST_OUTPUT_DIR}

mv input.sql ${OUTPUT_DIR}/data.sql
cp ${OUTPUT_DIR}/data.sql ${TEST_OUTPUT_DIR}/data.sql

echo "Generated ${OUTPUT_DIR}/data.sql"
echo "To insert the data into database, first run the service, and then from outside of container type:"
echo "docker exec -i openlmisstockmanagement_db_1 psql -Upostgres open_lmis < ${OUTPUT_DIR}/data.sql"
