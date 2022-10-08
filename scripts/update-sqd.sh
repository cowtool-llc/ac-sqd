#!/bin/bash -e

LAMBDA_NAME="sqd-calc"
HANDLER="com.canadiancow.sqd.SqdHandler::handleRequest"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_DIR=$SCRIPT_DIR/../scripts
ZIP_PATH="$SCRIPT_DIR/../build/libs/AC_SQD-all.jar"

cd "$SCRIPT_DIR"/..
./gradlew shadowJar

aws lambda update-function-configuration \
  --function $LAMBDA_NAME \
  --handler $HANDLER

"$SCRIPT_DIR"/update-lambda.sh $LAMBDA_NAME "$ZIP_PATH"
