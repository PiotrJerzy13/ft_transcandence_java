

type OnlineUsersProps = {
    users: string[];
    currentUsername?: string;
};

export default function OnlineUsers({ users, currentUsername }: OnlineUsersProps) {
    return (
        <div className="bg-black/40 border border-cyan-500/30 rounded-2xl p-4 shadow-xl">
            <div className="flex items-center gap-2 mb-3">
                <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
                <h3 className="text-sm font-semibold text-cyan-200">
                    Online Users ({users.length})
                </h3>
            </div>
            <div className="space-y-2 max-h-48 overflow-y-auto">
                {users.length === 0 ? (
                    <p className="text-xs text-cyan-100/50 italic">No users online</p>
                ) : (
                    users.map((user, index) => (
                        <div
                            key={`${user}-${index}`}
                            className="flex items-center gap-2 text-sm text-cyan-100/80 py-1"
                        >
                            <div className="w-1.5 h-1.5 rounded-full bg-green-400" />
                            <span className={user === currentUsername ? 'font-semibold text-cyan-300' : ''}>
                {user}
                                {user === currentUsername && ' (you)'}
              </span>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}