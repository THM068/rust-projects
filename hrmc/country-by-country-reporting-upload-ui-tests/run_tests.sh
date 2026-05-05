#!/bin/bash -e
DEFAULT_BROWSER=chrome
DEFAULT_ENVIRONMENT=local
BROWSER=$1
ENVIRONMENT=$2

if [ -z "$BROWSER" ]; then
    echo "BROWSER value not set, defaulting to $DEFAULT_BROWSER..."
    echo ""
fi

if [ -z "$ENVIRONMENT" ]; then
    echo "ENVIRONMENT value not set, defaulting to $DEFAULT_ENVIRONMENT..."
    echo ""
fi

# Scalafmt checks have been separated from the test command to avoid OutOfMemoryError in Jenkins
sbt scalafmtCheckAll scalafmtSbtCheck

sbt clean -Dbrowser="${BROWSER:=$DEFAULT_BROWSER}" -Denvironment="${ENVIRONMENT:=$DEFAULT_ENVIRONMENT}" \
    -Daccessibility.assessment=true -Dsecurity.assessment=false -Dbrowser.option.headless=true \
    "testOnly uk.gov.hmrc.test.ui.cucumber.runner.Runner" testReport