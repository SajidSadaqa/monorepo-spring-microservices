#!/usr/bin/env bash
set -euo pipefail

echo "[1/5] Start Postgres..."
docker compose up -d

echo "[2/5] Build & test..."
mvn -q -DskipITs=false clean verify

echo "[3/5] Run services (local profile, HTTP) ..."
# user-service on 8081, admin-service on 8082
(java -jar user-service/target/user-service-*.jar --spring.profiles.active=local >/tmp/user.log 2>&1) &
USER_PID=$!
(java -jar admin-service/target/admin-service-*.jar --spring.profiles.active=local >/tmp/admin.log 2>&1) &
ADMIN_PID=$!

sleep 8

echo "[4/5] Exercise flows..."
# signup -> login -> refresh
ACCESS=$(curl -s -X POST -H "Content-Type: application/json" -H "Accept-Language: en" \
  -d '{"username":"sajid","password":"Str0ngPass!","email":"sajid@example.com"}' \
  http://localhost:8081/api/auth/signup | jq -r .accessToken)

if [[ "$ACCESS" == "null" || -z "$ACCESS" ]]; then
  echo "Signup failed"; cat /tmp/user.log; kill $USER_PID $ADMIN_PID || true; exit 1
fi

REFRESH=$(curl -s -X POST -H "Content-Type: application/json" -H "Accept-Language: en" \
  -d '{"username":"sajid","password":"Str0ngPass!"}' \
  http://localhost:8081/api/auth/login | jq -r .refreshToken)

NEW_ACCESS=$(curl -s -X POST -H "Authorization: Bearer $REFRESH" \
  http://localhost:8081/api/auth/refresh | jq -r .accessToken)

# localized validation (Arabic)
AR_ERR=$(curl -s -X POST -H "Content-Type: application/json" -H "Accept-Language: ar" \
  -d '{"username":"!!","password":"weak","email":"bad"}' \
  http://localhost:8081/api/auth/signup | jq -r .detail)
echo "Arabic error message: $AR_ERR"

# admin login
ADMIN_ACCESS=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin#123"}' http://localhost:8082/api/admin/login | jq -r .accessToken)

# proxy list users (pageable) via admin-service (will call user-service s2s internally)
curl -s -H "Authorization: Bearer $ADMIN_ACCESS" "http://localhost:8082/api/admin/users?page=0&size=5" | jq .

echo "[5/5] OK"
kill $USER_PID $ADMIN_PID || true
