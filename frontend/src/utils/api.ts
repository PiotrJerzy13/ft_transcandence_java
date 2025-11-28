// utils/api.ts

const API_BASE_URL = "http://localhost:8080/api";

interface AuthFetchOptions extends RequestInit {
    // We explicitly override credentials to exclude it, since we use the header
    credentials?: 'omit';
}

export async function authFetch(endpoint: string, options: AuthFetchOptions = {}): Promise<Response> {
    const token = localStorage.getItem('jwtToken');

    const headers = {
        ...options.headers,
        'Content-Type': 'application/json',
    };

    if (token) {
        // Inject the JWT into the Authorization header
        headers['Authorization'] = `Bearer ${token}`;
    } else {
        // If no token is found, we can still proceed,
        // but the backend will return 403/401 for protected routes.
        console.warn(`Attempting to fetch ${endpoint} without a JWT token.`);
    }

    // Combine the base URL with the endpoint
    const url = `${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;

    return fetch(url, {
        ...options,
        headers: headers,
        credentials: 'omit' // Ensure cookies are not primarily relied upon
    });
}