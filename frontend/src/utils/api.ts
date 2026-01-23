// utils/api.ts

const DEFAULT_API_BASE_URL = "http://localhost:8080/api";
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL;

export function buildApiUrl(endpoint: string): string {
    return `${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;
}

interface AuthFetchOptions extends RequestInit {
    credentials?: 'omit';
}

export async function authFetch(endpoint: string, options: AuthFetchOptions = {}): Promise<Response> {
    const token = localStorage.getItem('jwtToken');

    // Build headers object simply
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };

    // Add any existing headers from options
    if (options.headers) {
        const existingHeaders = new Headers(options.headers);
        existingHeaders.forEach((value, key) => {
            headers[key] = value;
        });
    }

    // Add Authorization header if token exists
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    } else {
        console.warn(`Attempting to fetch ${endpoint} without a JWT token.`);
    }

    // Combine the base URL with the endpoint (including query params)
    const url = buildApiUrl(endpoint);

    console.log('🌐 Fetching:', url);
    console.log('🔑 Has token:', !!token);

    return fetch(url, {
        ...options,
        headers: headers,
        credentials: 'omit'
    });
}
