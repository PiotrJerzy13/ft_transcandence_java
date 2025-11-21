### POST /api/auth/register

**Purpose**: Create a new user account

**Request Body**:
- username: string (required)
- email: string (required)
- password: string (required)

**Response** (201 Created):
{
"id": 1,
"username": "john_doe",
"email": "john@example.com",
"token": "eyJhbGc..."
}

**Error Responses**:
- 400: Invalid input (missing fields, invalid email)
- 409: Username or email already exists

**Auth Required**: No
✅ Authentication:

# **POST /api/auth/register**

curl -X 'POST' \
'https://localhost:3000/api/auth/register' \
-H 'accept: application/json' \
-H 'Content-Type: application/json' \
-d '{
"username": "string",
"email": "user@example.com",
"password": "string"
}'


# POST /api/auth/logout

# POST /api/auth/login

✅ User:

# GET /api/user/me


✅ Pong:

# POST /api/pong/score

# GET /api/pong/history


✅ Arkanoid:

# POST /api/arkanoid/score

# GET /api/arkanoid/history


✅ Leaderboard (check if frontend actually calls this):

GET /api/leaderboard