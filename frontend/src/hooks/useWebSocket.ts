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
};

function buildWsUrl(): string {
    const base = API_BASE_URL.replace(/\/api\/?$/, "");
    const wsBase = base.replace(/^https:\/\//i, 'wss://').replace(/^http:\/\//i, 'ws://');
    return `${wsBase}/ws`;
}

export function useWebSocket(options: UseWebSocketOptions = {}) {
    const {
        url = import.meta.env.VITE_WS_URL || buildWsUrl(),
        autoConnect = true,
    } = options;

    const clientRef = useRef<Client | null>(null);
    const reconnectAttemptRef = useRef(0);

    const [isConnected, setIsConnected] = useState(false);
    const [isConnecting, setIsConnecting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [onlineUsers, setOnlineUsers] = useState<string[]>([]);

    const connect = useCallback(() => {
        if (clientRef.current?.active) {
            return;
        }

        const token = localStorage.getItem("jwtToken");

        if (!token) {
            console.error("No JWT token found");
            setError("Authentication token not found");
            return;
        }

        const client = new Client({
            webSocketFactory: () => new WebSocket(url),
            connectHeaders: { Authorization: `Bearer ${token}` },
            debug: (str) => console.log('STOMP:', str),
            reconnectDelay: 0,
            heartbeatIncoming: 0,
            heartbeatOutgoing: 0,
            onConnect: () => {
                console.log("✅ WebSocket connected");
                reconnectAttemptRef.current = 0;
                setIsConnected(true);
                setIsConnecting(false);
                setError(null);

                // Subscribe to public chat messages
                client.subscribe("/topic/public", (msg: IMessage) => {
                    console.log("📨 Received message:", msg.body);
                    try {
                        const parsed = JSON.parse(msg.body) as ChatMessage;
                        console.log("✅ Parsed:", parsed);
                        setMessages((prev) => [...prev, parsed]);
                    } catch (err) {
                        console.error("❌ Parse error:", err);
                    }
                });

                // Subscribe to chat history
                client.subscribe("/user/queue/history", (msg: IMessage) => {
                    console.log("📜 Received history:", msg.body);
                    try {
                        const parsed = JSON.parse(msg.body);
                        const history = Array.isArray(parsed) ? parsed : [parsed];
                        console.log("✅ Loaded history:", history.length, "messages");
                        setMessages(history);
                    } catch (err) {
                        console.error("❌ History parse error:", err);
                    }
                });

                // Subscribe to online users updates
                client.subscribe("/topic/users", (msg: IMessage) => {
                    console.log("👥 Received online users:", msg.body);
                    try {
                        const users = JSON.parse(msg.body);
                        const userList = Array.isArray(users) ? users : [];
                        console.log("✅ Online users:", userList);
                        setOnlineUsers(userList);
                    } catch (err) {
                        console.error("❌ Users parse error:", err);
                    }
                });

                // Subscribe to user-specific queue for initial users list
                client.subscribe("/user/queue/users", (msg: IMessage) => {
                    console.log("👥 Received initial users list:", msg.body);
                    try {
                        const users = JSON.parse(msg.body);
                        const userList = Array.isArray(users) ? users : [];
                        console.log("✅ Initial online users:", userList);
                        setOnlineUsers(userList);
                    } catch (err) {
                        console.error("❌ Initial users parse error:", err);
                    }
                });

                // Request initial data (history and users)
                client.publish({
                    destination: "/app/chat.addUser",
                    body: JSON.stringify({ type: "JOIN" }),
                });
            },
            onWebSocketClose: () => {
                console.log("🔌 WebSocket disconnected");
                setIsConnected(false);
                setIsConnecting(false);
                setOnlineUsers([]); // Clear online users on disconnect

                const attempt = reconnectAttemptRef.current;
                const delay = Math.min(30000, 1000 * Math.pow(2, attempt));
                reconnectAttemptRef.current += 1;

                console.log(`🔄 Reconnecting in ${delay}ms (attempt ${attempt + 1})`);

                setTimeout(() => {
                    if (clientRef.current && clientRef.current !== client) {
                        return;
                    }
                    client.activate();
                }, delay);
            },
            onStompError: (frame) => {
                console.error("❌ STOMP error:", frame);
                setError(frame.headers["message"] || "WebSocket error");
            },
        });

        clientRef.current = client;
        setIsConnecting(true);
        client.activate();
    }, [url]);

    const disconnect = useCallback(() => {
        console.log("🔌 Disconnecting WebSocket");
        setIsConnected(false);
        setIsConnecting(false);
        setOnlineUsers([]);
        clientRef.current?.deactivate();
        clientRef.current = null;
    }, []);

    const sendMessage = useCallback(
        (content: string, type: string = "CHAT") => {
            const client = clientRef.current;
            if (!client || !client.connected) {
                console.error("❌ Cannot send: not connected");
                setError("Not connected");
                return;
            }

            console.log("📤 Sending message:", { content, type });
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
    }, [autoConnect, connect, disconnect]);

    return {
        isConnected,
        isConnecting,
        error,
        messages,
        onlineUsers,
        connect,
        disconnect,
        sendMessage,
    };
}