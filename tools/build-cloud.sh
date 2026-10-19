#!/bin/bash
set -euo pipefail
cd /workspace/dayerize
export JAVA_HOME=/workspace/toolchains/jdk-21.0.6+7
export GRADLE_USER_HOME=/workspace/toolchains/gradle-cache
exec /workspace/toolchains/gradle-8.11.1/bin/gradle --offline --console=plain "$@"
