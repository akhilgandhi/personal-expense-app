#!/usr/bin/env bash
#
# Sample usage:
#
#   HOST=localhost PORT=8081 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8080}
: ${ACCOUNT_ID_EXPS=1}
: ${ACCOUNT_ID_NOT_FOUND=13}
: ${ACCOUNT_NO_EXPS=1}

function assertCurl() {

    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval $curlCmd)
    local httpCode="${result:(-3)}"
    RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

    if [ "$httpCode" = "$expectedHttpCode" ]
    then
      if [ "$httpCode" = "200" ]
      then
        echo "Test OK (HTTP Code: $httpCode)"
      else
        echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
      fi
    else
      echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo  "- Failing command: $curlCmd"
      echo  "- Response Body: $RESPONSE"
      exit 1
    fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function testUrl() {
  url=$@
  if $url -ks -f -o /dev/null
  then
    return 0
  else
    return 1
  fi;
}

function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url
  do
    n=$((n + 1))
    if [[ $n == 100 ]]
    then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}

set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
  echo "Restarting the test environment..."
  echo "$ docker compose down --remove-orphans"
  docker compose down --remove-orphans
  echo "$ docker compose up -d"
  docker compose up -d
fi

waitForService curl http://$HOST:$PORT/dashboard/$ACCOUNT_ID_EXPS

# Verify that a normal request works, expect two expenses
assertCurl 200 "curl http://$HOST:$PORT/dashboard/$ACCOUNT_ID_EXPS -s"
assertEqual $ACCOUNT_ID_EXPS $(echo $RESPONSE | jq .accountId)
assertEqual 2 $(echo $RESPONSE | jq ".expenses | length")

# Verify that a 404 (Not Found) error is returned for a non-existing accountId ($ACCOUNT_ID_NOT_FOUND)
assertCurl 404 "curl http://$HOST:$PORT/dashboard/$ACCOUNT_ID_NOT_FOUND -s"
assertEqual "No account found for accountId: $ACCOUNT_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

# Verify that no expenses are returned for accountId $ACCOUNT_NO_EXPS
assertCurl 200 "curl http://$HOST:$PORT/dashboard/$ACCOUNT_NO_EXPS -s"
assertEqual $ACCOUNT_NO_EXPS $(echo $RESPONSE | jq .accountId)
assertEqual 2 $(echo $RESPONSE | jq ".expenses | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a accountId that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/dashboard/-1 -s"
assertEqual "\"Invalid accountId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a accountId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/dashboard/invalidProductId -s"
assertEqual "null" "$(echo $RESPONSE | jq .message)"

if [[ $@ == *"stop"* ]]
then
    echo "We are done, stopping the test environment..."
    echo "$ docker compose down"
    docker compose down
fi

echo "End, all tests OK:" `date`