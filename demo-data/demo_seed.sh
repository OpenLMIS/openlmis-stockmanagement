#!/bin/bash

# This script creates a sql input file that can be used to populate the database with demo data.
# It searches for json files in given directory and then merges them into input file.
# Note: each file's name should refer to its target table name.

INPUT_DIR=${1}
OUTPUT_DIR=${2}
GENERATOR=${3}

# Get list of JSON files in input directory
FILES=`find ${INPUT_DIR} -name "*.json"`

# Run database input generation
node ${GENERATOR} ${FILES}

# Generate a single SQL file out of all sources, because Flyway expects only one afterMigrate.sql file.
CAT_ARGS=()

# Function adds a file to CAT_ARGS only if it exists.
function addCatArg {
  if [ -f $1 ]; then
    CAT_ARGS+=($1)
  fi
}

addCatArg ${INPUT_DIR}/demo_sql_header.txt
addCatArg /app/src/main/resources/db/starter/afterMigrate.sql
addCatArg input.sql
addCatArg ${INPUT_DIR}/demo_sql_footer.txt

cat ${CAT_ARGS[@]} > result.sql

# Move the generated file into output directory
OUTPUT_FILE=${OUTPUT_DIR}/afterMigrate.sql

mkdir -p ${OUTPUT_DIR}
mv result.sql ${OUTPUT_FILE}
rm input.sql

# Print instructions to insert the data into the database
echo "=============================="
echo "Generated ${OUTPUT_FILE}"
echo "To insert the data into database, you should copy this file to Flyway's migration directory, by default:"
echo "cp ${OUTPUT_FILE} /app/build/resources/main/demo-data/"
echo "The demo data will be inserted automatically while the application starts."
