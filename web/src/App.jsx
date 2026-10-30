import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './hooks/useAuth';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';

// Lazy loaded page components
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const TransactionsPage = lazy(() => import('./pages/TransactionsPage'));
const StatsPage = lazy(() => import('./pages/StatsPage'));
const SettingsPage = lazy(() => import('./pages/SettingsPage'));

/**
 * Centered Loading Spinner Component
 * Styled with Bütçe Takip dark aesthetic and neonGreen accents
 */
function LoadingSpinner({ message = 'Yükleniyor...' }) {
  return (
    <div className="min-h-screen bg-deepNight flex flex-col items-center justify-center p-4 select-none">
      <div className="relative flex items-center justify-center">
        {/* Ambient glow */}
        <div className="absolute w-20 h-20 bg-neonGreen/15 rounded-full blur-xl animate-pulse" />
        {/* Animated spinner ring */}
        <div className="w-14 h-14 rounded-full border-4 border-white/10 border-t-neonGreen animate-spin shadow-lg shadow-neonGreen/20" />
      </div>
      <p className="mt-5 text-sm font-medium text-white/60 tracking-wider">
        {message}
      </p>
    </div>
  );
}

/**
 * Inner App Content component that evaluates auth state
 */
function AppContent() {
  const { user, loading } = useAuth();

  // 1. If auth is determining current state, show centered spinner
  if (loading) {
    return <LoadingSpinner message="Oturum doğrulanıyor..." />;
  }

  // 2. If user is not authenticated, show LoginPage
  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  // 3. If authenticated, render Layout with nested application routes
  return (
    <Layout>
      <Suspense
        fallback={
          <div className="min-h-[60vh] flex flex-col items-center justify-center p-6">
            <div className="w-10 h-10 rounded-full border-2 border-white/10 border-t-neonGreen animate-spin" />
            <p className="mt-3 text-xs text-white/50 tracking-wide">Sayfa hazırlanıyor...</p>
          </div>
        }
      >
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
          <Route path="/stats" element={<StatsPage />} />
          <Route path="/settings" element={<SettingsPage />} />

          {/* Redirect /login and unknown routes to home */}
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </Layout>
  );
}

/**
 * Root Application Component
 * Wraps router and authentication provider
 */
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}
