// frontend/src/context/AuthContext.tsx
import { createContext, useState, useContext, useCallback, useEffect, useMemo } from 'react';
import type { ReactNode } from 'react';
import { authFetch } from '../utils/api.ts';

interface User {
    id: number;
    username: string;
    email: string;
}

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (username: string, password: string) => Promise<boolean>;
    logout: () => Promise<void>;
    checkAuth: () => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const checkAuth = useCallback(async (): Promise<boolean> => {
        try {
            console.log('[Auth] Checking authentication...');

            // Check if token exists
            const token = localStorage.getItem('jwtToken');
            if (!token) {
                console.log('[Auth] No token found');
                setUser(null);
                return false;
            }

            const response = await authFetch('/user/me');

            if (response.ok) {
                const userData = await response.json();
                setUser(userData);
                console.log('[Auth] Authentication check passed:', userData);
                return true;
            } else {
                setUser(null);
                // If token is invalid, remove it
                if (response.status === 401 || response.status === 403) {
                    localStorage.removeItem('jwtToken');
                    console.log('[Auth] Invalid token removed');
                }
                console.log('[Auth] Authentication check failed');
                return false;
            }
        } catch (error) {
            console.error('[Auth] Authentication check error:', error);
            setUser(null);
            return false;
        }
    }, []);

    const login = useCallback(async (username: string, password: string): Promise<boolean> => {
        try {
            console.log('[Auth] Attempting login...');

            // Don't use authFetch for login - it's not an authenticated endpoint
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'omit', // Not using cookies
                body: JSON.stringify({ username, password }),
            });

            if (response.ok) {
                const data = await response.json();
                console.log('[Auth] Login successful:', data);

                // Store the JWT token
                if (data.token) {
                    localStorage.setItem('jwtToken', data.token);
                    console.log('[Auth] JWT token stored');
                } else {
                    console.error('[Auth] No token in login response');
                    return false;
                }

                // Verify authentication is working
                const authVerified = await checkAuth();
                if (authVerified) {
                    console.log('[Auth] Login and auth verification complete');
                    return true;
                } else {
                    console.warn('[Auth] Login succeeded but auth verification failed');
                    localStorage.removeItem('jwtToken');
                    return false;
                }
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.log('[Auth] Login failed:', errorData.message || 'Unknown error');
                return false;
            }
        } catch (error) {
            console.error('[Auth] Login error:', error);
            return false;
        }
    }, [checkAuth]);

    const logout = useCallback(async (): Promise<void> => {
        try {
            // Call logout endpoint (optional - depends on your backend)
            await authFetch('/auth/logout', {
                method: 'POST',
            }).catch(() => {
                // Ignore errors - we're logging out anyway
            });

            // Remove token and clear user
            localStorage.removeItem('jwtToken');
            setUser(null);
            console.log('[Auth] Logout successful');
        } catch (error) {
            console.error('[Auth] Logout error:', error);
            // Still clear local state even if API call fails
            localStorage.removeItem('jwtToken');
            setUser(null);
        }
    }, []);

    useEffect(() => {
        const initializeAuth = async () => {
            await checkAuth();
            setIsLoading(false);
        };

        initializeAuth();
    }, [checkAuth]);

    const value = useMemo(() => ({
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        checkAuth,
    }), [user, isLoading, login, logout, checkAuth]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthContext;