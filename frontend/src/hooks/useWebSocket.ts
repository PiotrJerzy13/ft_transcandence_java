import { useCallback, useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import type { IMessage } from "@stomp/stompjs";
import { API_BASE_URL } from "../utils/api.ts";

export type ChatMessage = {
    sender: string;
    content: string;
    timestamp?: string;
    type?: string;
};

type UseWebSocketOptions = {
    url?: string;
    autoConnect?: boolean;
    historyTimeoutMs?: number;
};

const DEFAULT_HISTORY_TIMEOUT_MS = 1500;

function buildWsUrl(): string {
    const base = API_BASE_URL.replace(/\/api\/?$/, "");
    // Use wss:// for https://, ws:// for http://
    const wsBase = base.replace(/^https:\/\//i, 'wss://').replace(/^http:\/\//i, 'ws://');
    return `${wsBase}/ws`;
}

export function useWebSocket(options: UseWebSocketOptions = {}) {
    const {
        url = import.meta.env.VITE_WS_URL || buildWsUrl(),
        autoConnect = true,
        historyTimeoutMs = DEFAULT_HISTORY_TIMEOUT_MS,
    } = options;

    const clientRef = useRef<Client | null>(null);
    const reconnectAttemptRef = useRef(0);
    const historyTimerRef = useRef<number | null>(null);
    const pendingMessagesRef = useRef<ChatMessage[]>([]);

    const [isConnected, setIsConnected] = useState(false);
    const [isConnecting, setIsConnecting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [historyLoaded, setHistoryLoaded] = useState(false);

    const clearHistoryTimer = useCallback(() => {
        if (historyTimerRef.current !== null) {
            window.clearTimeout(historyTimerRef.current);
            historyTimerRef.current = null;
        }
    }, []);

    const flushPending = useCallback(() => {
        if (pendingMessagesRef.current.length > 0) {
            setMessages((prev) => [...prev, ...pendingMessagesRef.current]);
            pendingMessagesRef.current = [];
        }
    }, []);

    const handleHistoryTimeout = useCallback(() => {
        if (!historyLoaded) {
            setHistoryLoaded(true);
            flushPending();
        }
    }, [flushPending, historyLoaded]);

    const scheduleHistoryTimeout = useCallback(() => {
        clearHistoryTimer();
        historyTimerRef.current = window.setTimeout(handleHistoryTimeout, historyTimeoutMs);
    }, [clearHistoryTimer, handleHistoryTimeout, historyTimeoutMs]);

    const handleIncomingMessage = useCallback(
        (message: ChatMessage) => {
            if (!historyLoaded) {
                pendingMessagesRef.current.push(message);
                return;
            }
            setMessages((prev) => [...prev, message]);
        },
        [historyLoaded],
    );

    const handleHistory = useCallback(
        (payload: ChatMessage[] | ChatMessage) => {
            const history = Array.isArray(payload) ? payload : [payload];
            setMessages(history);
            setHistoryLoaded(true);
            flushPending();
        },
        [flushPending],
    );

    const connect = useCallback(() => {
        if (clientRef.current?.active) {
            return;
        }

        const token = localStorage.getItem("jwtToken");
        const client = new Client({
            webSocketFactory: () => new WebSocket(url),
            connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
            debug: (str) => console.log('STOMP:', str),
            reconnectDelay: 0,
            heartbeatIncoming: 0,
            heartbeatOutgoing: 0,
            onConnect: () => {
                reconnectAttemptRef.current = 0;
                setIsConnected(true);
                setIsConnecting(false);
                setError(null);
                setHistoryLoaded(false);
                scheduleHistoryTimeout();

                client.subscribe("/topic/public", (msg: IMessage) => {
                    const parsed = JSON.parse(msg.body) as ChatMessage;
                    handleIncomingMessage(parsed);
                });

                client.subscribe("/queue/history", (msg: IMessage) => {
                    const parsed = JSON.parse(msg.body) as ChatMessage[] | ChatMessage;
                    handleHistory(parsed);
                });
            },
            onWebSocketClose: () => {
                setIsConnected(false);
                setIsConnecting(false);
                clearHistoryTimer();
                const attempt = reconnectAttemptRef.current;
                const delay = Math.min(30000, 1000 * Math.pow(2, attempt));
                reconnectAttemptRef.current += 1;
                window.setTimeout(() => {
                    if (clientRef.current && clientRef.current !== client) {
                        return;
                    }
                    client.activate();
                }, delay);
            },
            onStompError: (frame) => {
                setError(frame.headers["message"] || "WebSocket error");
            },
        });

        clientRef.current = client;
        setIsConnecting(true);
        client.activate();
    }, [clearHistoryTimer, handleHistory, handleIncomingMessage, scheduleHistoryTimeout, url]);

    const disconnect = useCallback(() => {
        clearHistoryTimer();
        setIsConnected(false);
        setIsConnecting(false);
        clientRef.current?.deactivate();
        clientRef.current = null;
    }, [clearHistoryTimer]);

    const sendMessage = useCallback(
        (content: string, type: string = "CHAT") => {
            const client = clientRef.current;
            if (!client || !client.connected) {
                setError("Not connected");
                return;
            }
            client.publish({
                destination: "/app/chat.sendMessage",
                body: JSON.stringify({ content, type }),
            });
        },
        [],
    );

    useEffect(() => {
        if (autoConnect) {
            connect();
        }
        return () => {
            disconnect();
        };
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [autoConnect]);

    return {
        isConnected,
        isConnecting,
        error,
        messages,
        historyLoaded,
        connect,
        disconnect,
        sendMessage,
    };
}
