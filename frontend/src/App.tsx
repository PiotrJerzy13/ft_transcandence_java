import AppRouter from "./routes/Router.tsx";
import { ToastProvider } from './context/ToastContext.tsx';
import { PlayerDataProvider } from './context/PlayerDataContext.tsx'; // Import

export default function App() {
  return (
    <ToastProvider>
      <PlayerDataProvider> {/* Wrap AppRouter with the new provider */}
        <AppRouter />
      </PlayerDataProvider>
    </ToastProvider>
  );
}