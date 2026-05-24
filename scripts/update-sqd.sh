#!/bin/bash -e

MAIN_LAMBDA_NAME="ac-sqc"
MAIN_HANDLER="com.cowtool.acsqd.SqdHandler::handleRequest"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_DIR=$SCRIPT_DIR/../scripts
ZIP_PATH="$SCRIPT_DIR/../build/libs/AC_SQD-all.jar"

cd "$SCRIPT_DIR"/..
./gradlew shadowJar

aws lambda update-function-configuration \
  --function $MAIN_LAMBDA_NAME \
  --handler $MAIN_HANDLER

"$SCRIPT_DIR"/update-lambda.sh $MAIN_LAMBDA_NAME "$ZIP_PATH"
