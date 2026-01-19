#!/bin/bash

BASE_URL="http://localhost:8080"
ITERATIONS=100

echo "Starting traffic generation for Cyber Pong..."
echo "This will create realistic metrics for your Grafana dashboard"
echo ""

# Function to register a user
register_user() {
    local username=$1
    local response=$(curl -s -X POST "$BASE_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"demo123\",\"email\":\"$username@demo.com\"}")
    echo "$response"
}

# Function to login and get JWT token
login_user() {
    local username=$1
    local response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"demo123\"}")
    echo "$response" | grep -o '"token":"[^"]*' | cut -d'"' -f4
}

# Function to make authenticated request
make_request() {
    local endpoint=$1
    local token=$2
    curl -s -X GET "$BASE_URL$endpoint" \
        -H "Authorization: Bearer $token" > /dev/null
}

echo "Phase 1: Registering test users..."
for i in {1..5}; do
    echo "Registering user_demo_$i"
    register_user "user_demo_$i"
    sleep 0.5
done

echo ""
echo "Phase 2: Generating traffic (login + API calls)..."
for i in $(seq 1 $ITERATIONS); do
    # Random user between 1-5
    USER_NUM=$((RANDOM % 5 + 1))
    USERNAME="user_demo_$USER_NUM"
    
    # Login
    TOKEN=$(login_user "$USERNAME")
    
    if [ ! -z "$TOKEN" ]; then
        # Make various API calls
        make_request "/api/user/profile" "$TOKEN"
        make_request "/api/leaderboard" "$TOKEN"
        make_request "/api/stats" "$TOKEN"
        
        # Random delay to simulate realistic traffic
        sleep 0.$((RANDOM % 5))
    fi
    
    # Progress indicator
    if [ $((i % 10)) -eq 0 ]; then
        echo "Progress: $i/$ITERATIONS requests"
    fi
done

echo ""
echo "Traffic generation complete!"
echo "Check your Grafana dashboard at http://localhost:3000"
echo "You should see active metrics now."
