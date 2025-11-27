import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "../pages/Login.tsx";
import Register from "../pages/Register.tsx";
import Game from "../pages/Game.tsx";
import ArkanoidGame from "../pages/Game2.tsx";
import Lobby from "../pages/Lobby.tsx";
import ProtectedRoute from "../components/ProtectedRoute.tsx";
import Leaderboard from '../pages/Leaderboard.tsx';

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/lobby" element={<ProtectedRoute><Lobby /></ProtectedRoute>} />
		<Route path="/game" element={<ProtectedRoute><Game /></ProtectedRoute>} />
		<Route path="/game2" element={<ProtectedRoute><ArkanoidGame /></ProtectedRoute>} />
		<Route path="/leaderboard" element={<ProtectedRoute><Leaderboard /></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}
