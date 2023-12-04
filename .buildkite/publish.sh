#!/bin/bash

set -euo pipefail

./gradlew \
:measure-builds:prepareToPublishToS3 $(prepare_to_publish_to_s3_params) \
:measure-builds:publish
