#!/usr/bin/env bash
set -euo pipefail

: "${IMAGE_REGISTRY:?Set IMAGE_REGISTRY to the approved container registry}"
: "${IMAGE_REPOSITORY:?Set IMAGE_REPOSITORY to the approved image repository}"
: "${IMAGE_TAG:?Set IMAGE_TAG to an immutable release tag or commit SHA}"

source_image="edrc-land-gis-api:local"
target_image="${IMAGE_REGISTRY%/}/${IMAGE_REPOSITORY}:${IMAGE_TAG}"

docker image inspect "$source_image" >/dev/null
docker tag "$source_image" "$target_image"
echo "Prepared $target_image"
echo "Run docker push only after registry login, security approval, and release-gate sign-off."

if [[ "${PUBLISH_IMAGE:-false}" == "true" ]]; then
  docker push "$target_image"
else
  echo "PUBLISH_IMAGE is not true; image was tagged locally and not published."
fi
