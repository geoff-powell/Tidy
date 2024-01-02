#!/bin/bash
set -x

git fetch --tags
TAGS=$(git show-ref --tags)
CURRENT_BUILD_NUMBER=$(echo "$TAGS" | grep "$GITHUB_SHA" | grep -oE "build-number-(\d+)" | grep -oE "\d+")

if [[ -z "${CURRENT_BUILD_NUMBER}" ]]; then
  echo "No build number found for current SHA $GITHUB_SHA."

  # Get the latest build number
  LATEST_BUILD_NUMBER=$(echo "$TAGS" | grep -oE "build-number-(\d+)" | sort -nr | head -1 | grep -oE "\d+")

  if [[ -z "${LATEST_BUILD_NUMBER}" ]]; then
    echo "No build number found in tags. Please create a tag with the format 'build-number-1' on main branch."
    exit 1
  fi

  CURRENT_BUILD_NUMBER="$((LATEST_BUILD_NUMBER + 1))"
  git tag "build-number-$CURRENT_BUILD_NUMBER"

  git config --local user.email "github-actions[bot]@users.noreply.github.com"
  git config --local user.name "github-actions[bot]"

  GITHUB_URL_PROTOCOL="$(echo "$GITHUB_SERVER_URL" | grep :// | sed -e's,^\(.*://\).*,\1,g')"

  REMOTE_REPO="${GITHUB_URL_PROTOCOL}//oauth2:${INPUT_GITHUB_TOKEN}@${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}.git"
  PUSH_PARAMETERS="${REMOTE_REPO}"
  echo "$GITHUB_URL_PROTOCOL"
  echo "Pushing build number tag build-number-$CURRENT_BUILD_NUMBER to Github ($GITHUB_REF)."

  git push "$PUSH_PARAMETERS" --tags
else
  echo "Found build number $CURRENT_BUILD_NUMBER for current SHA $GITHUB_SHA."
  echo "Skipping tag creation."
fi

echo "BUILD_NUMBER=$CURRENT_BUILD_NUMBER" > "$GITHUB_ENV"