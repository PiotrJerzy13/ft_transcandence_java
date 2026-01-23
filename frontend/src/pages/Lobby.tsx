import { useGameHistory } from '../hooks/useGameHistory.ts';
import type { ArkanoidScore, PongGame } from '../types.ts';
import { usePlayerData } from '../context/PlayerDataContext.tsx';

// Import your new components
import PlayerHeader from './components/PlayerHeader.tsx';
import GameModeSelection from './components/GameModeSelection.tsx';
import GameHistorySection from './components/GameHistorySection.tsx';
import PlayerProfileSidebar from './components/PlayerProfileSidebar.tsx';
import LobbyActions from './components/LobbyActions.tsx';
import { useNavigate } from 'react-router-dom';
import { authFetch } from "../utils/api.ts";
import { useWebSocket } from "../hooks/useWebSocket.ts";
import { useEffect, useRef, useState } from "react";

export default function GameLobby() {
  const navigate = useNavigate();
  const [messageDraft, setMessageDraft] = useState("");
  const chatContainerRef = useRef<HTMLDivElement | null>(null);
  const isAtBottomRef = useRef(true);

  // --- Data fetching is now simpler ---
  const { playerData, loading: profileLoading, error: profileError } = usePlayerData();
  const { history: pongHistory, loading: pongLoading, error: pongError, stats: pongStats } = useGameHistory<PongGame>('pong');
  const { history: arkanoidHistory, loading: arkanoidLoading, error: arkanoidError, stats: arkanoidStats } = useGameHistory<ArkanoidScore>('arkanoid');
  const { messages, sendMessage, isConnected, isConnecting, error: wsError } = useWebSocket();

  const loading = profileLoading || pongLoading || arkanoidLoading;
  const error = profileError || pongError || arkanoidError;

  useEffect(() => {
    const container = chatContainerRef.current;
    if (!container) return;
    if (isAtBottomRef.current) {
      container.scrollTop = container.scrollHeight;
    }
  }, [messages]);

  // --- Handlers stay here ---
  const handleGameModeSelect = (mode: 'pong' | 'arkanoid') => {
    navigate(mode === 'pong' ? '/game' : '/game2');
  };

    const handleLogout = async () => {
        try {
            const res = await authFetch('/auth/logout', {  // Use authFetch
                method: 'POST'
            });

            if (res.ok) {
                localStorage.removeItem('jwtToken');
                navigate('/login', { replace: true });
            } else {
                alert('Logout failed');
            }
        } catch (err) {
            console.error('Logout error:', err);
            alert('Logout error');
        }
    };

  const getRankColor = (rank: string) => {
    const colors: Record<string, string> = {
      'Novice': 'text-gray-400', 'Amateur': 'text-green-400', 'Pro': 'text-blue-400',
      'Elite': 'text-purple-400', 'Master': 'text-yellow-400', 'Legend': 'text-red-400'
    };
    return colors[rank] || 'text-gray-400';
  };

  // --- Loading and Error States ---
  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-white text-xl">Loading player data...</div>
      </div>
    );
  }
  if (error || !playerData) { // Check for playerData as well
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="bg-red-900/20 border border-red-500/30 rounded-xl p-8 max-w-md text-center">
          <h2 className="text-red-400 text-xl mb-4">Failed to Load Profile</h2>
          <p className="text-gray-300 mb-4">{error || "Could not retrieve player data. Please try logging in again."}</p>
          <div className="space-y-2">
            <button 
              onClick={() => window.location.reload()}
              className="w-full px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
            >
              Retry
            </button>
            <button 
              onClick={() => navigate('/login', { replace: true })}
              className="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              Go to Login
            </button>
          </div>
        </div>
      </div>
    );
  }

  const { stats: playerStats, achievements } = playerData;
  if (!playerStats) return null; // Should be handled by the error block above, but good for type safety

  const rankColor = getRankColor(playerStats.rank);

  const handleChatScroll = () => {
    const container = chatContainerRef.current;
    if (!container) return;
    const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
    isAtBottomRef.current = distanceFromBottom < 48;
  };

  const handleSendMessage = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmed = messageDraft.trim();
    if (!trimmed) return;
    sendMessage(trimmed, "CHAT");
    setMessageDraft("");
  };

  const formatTimestamp = (value?: string) => {
    if (!value) return "";
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    return parsed.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  // --- The clean, composed layout ---
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 p-4 sm:p-6">
      <div className="max-w-7xl mx-auto">
        <PlayerHeader stats={playerStats} rankColor={rankColor} />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 md:gap-8">
          <div className="lg:col-span-2 space-y-6">
            <GameModeSelection onSelectGame={handleGameModeSelect} />
            <GameHistorySection 
                playerStats={playerStats}
                pongHistory={pongHistory}
                pongStats={pongStats}
                arkanoidHistory={arkanoidHistory}
                arkanoidStats={arkanoidStats}
            />
            <section className="bg-black/40 border border-cyan-500/30 rounded-2xl p-4 sm:p-6 shadow-xl">
              <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                <h2 className="text-lg sm:text-xl font-bold text-cyan-200">Lobby Chat</h2>
                <div className="text-xs text-cyan-200/70">
                  {isConnecting ? "Connecting..." : isConnected ? "Connected" : "Disconnected"}
                </div>
              </div>
              {wsError && (
                <div className="mb-3 rounded-lg border border-red-500/30 bg-red-900/20 px-3 py-2 text-xs text-red-200">
                  {wsError}
                </div>
              )}
              <div
                ref={chatContainerRef}
                onScroll={handleChatScroll}
                className="h-64 sm:h-72 overflow-y-auto rounded-xl bg-black/60 border border-cyan-500/20 p-3 space-y-3"
              >
                {messages.length === 0 && (
                  <div className="text-sm text-cyan-100/60">No messages yet. Say hello!</div>
                )}
                {messages.map((message, index) => (
                  <div key={`${message.sender}-${message.timestamp}-${index}`} className="text-sm">
                    <div className="flex flex-wrap items-center gap-2 text-cyan-300">
                      <span className="font-semibold">{message.sender}</span>
                      {message.timestamp && (
                        <span className="text-xs text-cyan-100/50">{formatTimestamp(message.timestamp)}</span>
                      )}
                    </div>
                    <p className="text-cyan-100/80">{message.content}</p>
                  </div>
                ))}
              </div>
              <form onSubmit={handleSendMessage} className="mt-4 flex flex-col sm:flex-row gap-2">
                <input
                  value={messageDraft}
                  onChange={(event) => setMessageDraft(event.target.value)}
                  className="flex-1 rounded-lg border border-cyan-500/30 bg-black/50 px-3 py-2 text-sm text-cyan-100 placeholder:text-cyan-100/40 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                  placeholder={isConnected ? "Type a message..." : "Chat is offline"}
                  disabled={!isConnected}
                />
                <button
                  type="submit"
                  disabled={!isConnected || !messageDraft.trim()}
                  className="rounded-lg bg-cyan-500/80 px-4 py-2 text-sm font-semibold text-black transition disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Send
                </button>
              </form>
            </section>
          </div>

          <PlayerProfileSidebar
            playerStats={playerStats}
            achievements={achievements}
            rankColor={rankColor}
          />
        </div>

        <LobbyActions onLogout={handleLogout} />
      </div>
    </div>
  );
}
