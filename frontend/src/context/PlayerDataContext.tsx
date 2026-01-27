import { createContext, useState, useContext, useCallback, useEffect, useMemo } from 'react';
import type { ReactNode } from 'react';
import type { PlayerStats, Achievement } from '../types.ts';
import { authFetch } from '../utils/api.ts';

interface PlayerData {
  user: { id: number; username: string; email: string; } | null;
  stats: PlayerStats | null;
  achievements: Achievement[];
}

interface PlayerDataContextType {
  playerData: PlayerData | null;
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

const PlayerDataContext = createContext<PlayerDataContextType | undefined>(undefined);

export const usePlayerData = () => {
  const context = useContext(PlayerDataContext);
  if (!context) {
    throw new Error('usePlayerData must be used within a PlayerDataProvider');
  }
  return context;
};

export const PlayerDataProvider = ({ children }: { children: ReactNode }) => {
    const [playerData, setPlayerData] = useState<PlayerData | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchData = useCallback(async (retryCount = 0) => {
        setLoading(true);
        setError(null);

        if (retryCount === 0) {
            await new Promise(resolve => setTimeout(resolve, 250));
        }

        try {
            console.log(`[PlayerData] Attempt ${retryCount + 1}: Starting authentication check...`);

            // 1. First check if user is authenticated using authFetch
            const authRes = await authFetch('/user/me');

            console.log(`[PlayerData] Auth check response:`, {
                status: authRes.status,
                ok: authRes.ok,
                headers: Object.fromEntries(authRes.headers.entries())
            });

            if (!authRes.ok) {
                setPlayerData(null);
                setLoading(false);
                return;
            }

            console.log('Auth check passed, fetching profile...');

            const res = await authFetch('/user/profile', {
                headers: {
                    'Cache-Control': 'no-cache, no-store, must-revalidate',
                    'Pragma': 'no-cache',
                    'Expires': '0'
                },
            });

            console.log(`[PlayerData] Profile fetch response:`, {
                status: res.status,
                ok: res.ok,
                headers: Object.fromEntries(res.headers.entries())
            });

            if (!res.ok) {
                if (res.status === 401) {
                    setPlayerData(null);
                    return;
                }
                const errorData = await res.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to fetch profile');
            }

            const data = await res.json();

      const transformedStats: PlayerStats = {
        ...data.stats,
        totalPlayTime: data.stats.totalPlayTime || "0h 0m",
      };

            setPlayerData({
                user: data.user,
                stats: transformedStats,
                achievements: data.achievements,
            });

        } catch (err) {
            setError(err instanceof Error ? err.message : 'An unknown error occurred');
            console.error("Error fetching player data:", err);
        } finally {
            setLoading(false);
        }
    }, []);

  useEffect(() => {
    console.log('[PlayerData] Provider mounted, waiting for explicit fetch call');
  }, []);

  const value = useMemo(() => ({
    playerData,
    loading,
    error,
    refetch: fetchData
  }), [playerData, loading, error, fetchData]);

  return (
    <PlayerDataContext.Provider value={value}>
      {children}
    </PlayerDataContext.Provider>
  );
};
export default PlayerDataContext;