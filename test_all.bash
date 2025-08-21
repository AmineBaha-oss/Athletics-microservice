#!/usr/bin/env bash
#
# Sample usage:
#   HOST=localhost PORT=8080 ./test_all.bash start stop
#

: ${HOST=localhost}
: ${PORT=8080}

allTestTeamIds=()
allTestAthleteIds=()
allTestSponsorIds=()
allTestFacilityIds=()
allTestCompetitionIds=()

RESPONSE=""

function assertCurl() {
  local expected=$1
  local cmd="$2 -w \"%{http_code}\""
  local result
  result=$(eval $cmd)
  local httpCode="${result: -3}"
  local body="${result%???}"
  RESPONSE="$body"

  [ -n "$body" ] && echo "$body" | jq .
  if [ "$httpCode" = "$expected" ]; then
    echo "OK HTTP $httpCode"
  else
    echo "FAIL: expected HTTP $expected, got $httpCode"
    echo "  cmd: $cmd"
    exit 1
  fi
}

function assertEqual() {
  if [ "$1" = "$2" ]; then
    echo "OK value: $2"
  else
    echo "FAIL: expected '$1', got '$2'"
    exit 1
  fi
}

function testUrl() {
  curl -ks -f -o /dev/null "$@" && return 0 || return 1
}

function waitForService() {
  echo -n "Waiting for API gateway… "
  until testUrl http://$HOST:$PORT/api/v1/teams; do
    sleep 2; echo -n "."
  done
  echo " up!"
}

set -e

if [[ $@ == *start* ]]; then
  docker-compose down
  docker-compose up -d
fi

waitForService

#
# HELPERS
#
function recreateTeamAggregate() {
  local teamJson=$1
  teamId=$(curl -s -X POST http://$HOST:$PORT/api/v1/teams \
    -H "Content-Type: application/json" \
    -d "$teamJson" | jq -r .teamId)
  allTestTeamIds+=("$teamId")
}

function recreateAthleteAggregate() {
  local teamId=$1
  local athleteJson=$2
  athleteId=$(curl -s -X POST http://$HOST:$PORT/api/v1/teams/$teamId/athletes \
    -H "Content-Type: application/json" \
    -d "$athleteJson" | jq -r .athleteId)
  allTestAthleteIds+=("$athleteId")
}

function recreateSponsorAggregate() {
  sponsorId=$(curl -s -X POST http://$HOST:$PORT/api/v1/sponsors \
    -H "Content-Type: application/json" \
    -d "$1" | jq -r .sponsorId)
  allTestSponsorIds+=("$sponsorId")
}

function recreateFacilityAggregate() {
  facilityId=$(curl -s -X POST http://$HOST:$PORT/api/v1/facilities \
    -H "Content-Type: application/json" \
    -d "$1" | jq -r .facilityId)
  allTestFacilityIds+=("$facilityId")
}

function recreateCompetitionAggregate() {
  local teamId=$1
  local compJson=$2
  competitionId=$(curl -s -X POST http://$HOST:$PORT/api/v1/teams/$teamId/competitions \
    -H "Content-Type: application/json" \
    -d "$compJson" | jq -r .competitionId)
  allTestCompetitionIds+=("$competitionId")
}

#
# SETUP: seed one of each aggregate
#
function setupTestdata() {
  echo "=== Creating Team ==="
  recreateTeamAggregate '{
    "teamId":"11111111-1111-1111-1111-111111111111",
    "teamName":"City Warriors",
    "coachName":"Coach Carter",
    "teamLevel":"COLLEGE"
  }'

  echo "=== Creating Athlete ==="
  recreateAthleteAggregate "11111111-1111-1111-1111-111111111111" '{
    "athleteId":"ath55555-5555-5555-5555-555555555555",
    "firstName":"LeBron",
    "lastName":"James",
    "dateOfBirth":"1990-12-30",
    "athleteCategory":"SENIOR",
    "teamId":"11111111-1111-1111-1111-111111111111"
  }'

  echo "=== Creating Sponsor ==="
  recreateSponsorAggregate '{
    "sponsorId":"spons6666-6666-6666-6666-66666666666",
    "sponsorName":"Reebok",
    "sponsorLevel":"GOLD",
    "sponsorAmount":120000.00
  }'

  echo "=== Creating Facility ==="
  recreateFacilityAggregate '{
    "facilityId":"fac66666-6666-6666-6666-666666666667",
    "facilityName":"New Stadium",
    "capacity":150,
    "location":"City, State"
  }'

  echo "=== Creating Competition ==="
  recreateCompetitionAggregate "11111111-1111-1111-1111-111111111111" '{
    "competitionId":"7256da63-c914-4820-8e0a-d0d00f3c5e21",
    "competitionName":"Spring Invitational A",
    "competitionDate":"2025-06-01",
    "competitionStatus":"SCHEDULED",
    "competitionResult":"DRAW",
    "teamId":"11111111-1111-1111-1111-111111111111",
    "sponsorId":"aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1",
    "facilityId":"fac11111-1111-1111-1111-111111111111"
  }'
}
setupTestdata

#
# TESTS
#

## Teams
echo; echo ">>> TEST: GET all teams"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams"

echo ">>> TEST: GET one team"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111"

echo ">>> TEST: UPDATE team"
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111 \
  -H 'Content-Type: application/json' \
  -d '{\"teamId\":\"11111111-1111-1111-1111-111111111111\",\"teamName\":\"Warriors Updated\",\"coachName\":\"New Coach\",\"teamLevel\":\"PROFESSIONAL\"}'"


## Athletes
echo; echo ">>> TEST: GET all athletes"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/athletes"

echo ">>> TEST: POST new athlete"
athlete_body='{
  "athleteId":"ath7777-7777-7777-7777-777777777777",
  "firstName":"Stephen",
  "lastName":"Curry",
  "dateOfBirth":"1988-03-14",
  "athleteCategory":"SENIOR",
  "teamId":"11111111-1111-1111-1111-111111111111"
}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/athletes \
  -H 'Content-Type: application/json' -d '$athlete_body'"
newAthleteId=$(echo "$RESPONSE" | jq -r .athleteId)
allTestAthleteIds+=("$newAthleteId")

echo ">>> TEST: GET one athlete"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/athletes/${allTestAthleteIds[1]}"

echo ">>> TEST: UPDATE athlete"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/athletes/${allTestAthleteIds[1]}"

update_athlete=$(echo "$RESPONSE" | jq -c '
  .firstName       = "Steph" |
  .lastName        = "Curry" |
  .athleteCategory = "VETERAN"
')

echo ">>> TEST: DELETE athlete"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/athletes/${allTestAthleteIds[0]}"

## Sponsors
echo; echo ">>> TEST: GET all sponsors"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/sponsors"

echo ">>> TEST: POST new sponsor"
sponsor_body='{
  "sponsorId":"spons6666-6666-6666-6666-66666666667",
  "sponsorName":"Reebok",
  "sponsorLevel":"GOLD",
  "sponsorAmount":120000.00
}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/sponsors \
  -H 'Content-Type: application/json' -d '$sponsor_body'"
sponsorId=$(echo "$RESPONSE" | jq -r .sponsorId)
allTestSponsorIds+=("$sponsorId")

echo ">>> TEST: GET one sponsor"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/sponsors/$sponsorId"

echo ">>> TEST: UPDATE sponsor"
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/sponsors/$sponsorId \
  -H 'Content-Type: application/json' \
  -d '{\"sponsorId\":\"$sponsorId\",\"sponsorName\":\"Adidas Updated\",\"sponsorLevel\":\"SILVER\",\"sponsorAmount\":75000.00}'"

echo ">>> TEST: DELETE sponsor"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/sponsors/$sponsorId"

## Facilities
echo; echo ">>> TEST: GET all facilities"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/facilities"

echo ">>> TEST: POST new facility"
facility_body='{
  "facilityId":"fac88888-8888-8888-8888-888888888889",
  "facilityName":"Downtown Arena",
  "capacity":20000,
  "location":"City Center"
}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/facilities \
  -H 'Content-Type: application/json' -d '$facility_body'"
facilityId=$(echo "$RESPONSE" | jq -r .facilityId)
allTestFacilityIds+=("$facilityId")

echo ">>> TEST: GET one facility"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/facilities/$facilityId"

echo ">>> TEST: UPDATE facility"
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/facilities/$facilityId \
  -H 'Content-Type: application/json' \
  -d '{\"facilityId\":\"$facilityId\",\"facilityName\":\"Arena Updated\",\"capacity\":25000,\"location\":\"City Center\"}'"

echo ">>> TEST: DELETE facility"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/facilities/$facilityId"

## Competitions
echo; echo ">>> TEST: GET all competitions (seeded so non-empty)"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/competitions"
## Competitions
echo; echo ">>> TEST: GET all competitions (seeded so non-empty)"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/competitions"

echo ">>> TEST: POST new competition"
competition_body='{
  "competitionName":   "Spring Invitational",
  "competitionDate":   "2024-06-01",
  "competitionStatus": "SCHEDULED",
  "competitionResult": "DRAW",
  "teamId":            "11111111-1111-1111-1111-111111111111",
  "sponsorId":         "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1",
  "facilityId":        "fac11111-1111-1111-1111-111111111111"
}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/competitions \
  -H 'Content-Type: application/json' \
  -d '$competition_body'"
competitionId=$(echo "$RESPONSE" | jq -r .competitionId)
allTestCompetitionIds+=("$competitionId")


echo ">>> TEST: GET one competition"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/competitions/$competitionId"
echo ">>> TEST: UPDATE competition"
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/competitions/$competitionId \
  -H 'Content-Type: application/json' \
  -d \"{\\\"competitionId\\\":\\\"$competitionId\\\",\\\"competitionName\\\":\\\"Updated Invitational\\\",\\\"competitionDate\\\":\\\"2025-07-15\\\",\\\"competitionStatus\\\":\\\"COMPLETED\\\",\\\"competitionResult\\\":\\\"WIN\\\",\\\"teamId\\\":\\\"11111111-1111-1111-1111-111111111111\\\",\\\"sponsorId\\\":\\\"aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1\\\",\\\"facilityId\\\":\\\"fac11111-1111-1111-1111-111111111111\\\"}\""

echo ">>> TEST: DELETE competition"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111/competitions/$competitionId"

## Clean up Team
echo; echo ">>> TEST: DELETE team"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/teams/11111111-1111-1111-1111-111111111111"

if [[ $@ == *stop* ]]; then
  docker-compose down
fi

echo; echo "All tests passed!"
