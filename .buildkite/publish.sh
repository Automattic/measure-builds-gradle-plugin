#!/bin/bash

set -euo pipefail

./gradlew \
:plugin:prepareToPublishToS3 $(prepare_to_publish_to_s3_params) \
:plugin:publish
