import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  ArrowLeftRight,
  BarChart3,
  Settings,
  Wallet,
  LogOut,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

/**
 * Sidebar Navigation Component
 * Provides responsive navigation for Bütçe Takip web app:
 * - Desktop: Vertical sidebar with glassmorphic aesthetic (w-64)
 * - Mobile: Fixed bottom navigation bar & compact top header with user avatar/logout
 */
export default function Sidebar({ onSignOut, user: userProp }) {
  const location = useLocation();
  const authContext = useAuth();
  const user = userProp || authContext?.user;

  const handleSignOut = async () => {
    try {
      if (onSignOut) {
        await onSignOut();
      } else if (authContext?.signOut) {
        await authContext.signOut();
      }
    } catch (err) {
      console.error('Çıkış yapılırken hata oluştu:', err);
    }
  };

  const navItems = [
    {
      label: 'Ana Sayfa',
      path: '/',
      icon: LayoutDashboard,
    },
    {
      label: 'İşlemler',
      path: '/transactions',
      icon: ArrowLeftRight,
    },
    {
      label: 'İstatistikler',
      path: '/stats',
      icon: BarChart3,
    },
    {
      label: 'Ayarlar',
      path: '/settings',
      icon: Settings,
    },
  ];

  const isItemActive = (path) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(path);
  };

  // Get user avatar letter and display name
  const userEmail = user?.email || '';
  const avatarLetter = (userEmail?.[0] || user?.displayName?.[0] || 'K').toUpperCase();
  const displayName = user?.displayName || (userEmail ? userEmail.split('@')[0] : 'Kullanıcı');

  return (
    <>
      {/* ========================================================
          MOBILE TOP BAR (Logo + User Profile + Logout)
          ======================================================== */}
      <header className="md:hidden fixed top-0 left-0 right-0 z-40 bg-cardDark/90 backdrop-blur-xl border-b border-white/10 px-4 py-3 flex items-center justify-between select-none">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-neonGreen/10 border border-neonGreen/30 flex items-center justify-center shadow-[0_0_12px_rgba(0,230,118,0.2)]">
            <Wallet className="w-4 h-4 text-neonGreen stroke-[2.2]" />
          </div>
          <span className="text-base font-bold text-white tracking-tight">Bütçe Takip</span>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <div
              className="w-8 h-8 rounded-full bg-neonGreen/20 text-neonGreen border border-neonGreen/30 font-bold flex items-center justify-center text-xs shadow-[0_0_8px_rgba(0,230,118,0.15)]"
              title={userEmail}
            >
              {avatarLetter}
            </div>
            {userEmail && (
              <span className="text-xs text-white/80 max-w-[120px] truncate hidden sm:inline">
                {userEmail}
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={handleSignOut}
            title="Çıkış Yap"
            aria-label="Çıkış Yap"
            className="p-1.5 text-white/50 hover:text-expenseRed hover:bg-expenseRed/10 rounded-lg transition-colors cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </header>

      {/* ========================================================
          DESKTOP SIDEBAR (w-64, Vertical, Sticky)
          ======================================================== */}
      <aside className="hidden md:flex flex-col w-64 h-screen sticky top-0 bg-cardDark/80 backdrop-blur-xl border-r border-white/10 z-30 select-none flex-shrink-0">
        {/* Brand Header */}
        <div className="flex items-center gap-3 px-6 py-6 border-b border-white/10">
          <div className="w-10 h-10 rounded-xl bg-neonGreen/10 border border-neonGreen/30 flex items-center justify-center shadow-[0_0_15px_rgba(0,230,118,0.2)] group-hover:scale-105 transition-transform">
            <Wallet className="w-5 h-5 text-neonGreen stroke-[2.2]" />
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white flex items-center gap-1.5">
              Bütçe Takip
            </h1>
            <p className="text-[11px] text-white/40">Finans Yönetimi</p>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-6 space-y-1.5 overflow-y-auto">
          {navItems.map((item) => {
            const active = isItemActive(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 group ${
                  active
                    ? 'bg-neonGreen/10 text-neonGreen border border-neonGreen/20 shadow-[0_0_15px_rgba(0,230,118,0.08)]'
                    : 'text-white/60 hover:text-white hover:bg-white/5 border border-transparent'
                }`}
              >
                <Icon
                  className={`w-5 h-5 flex-shrink-0 transition-transform duration-200 group-hover:scale-110 ${
                    active ? 'text-neonGreen' : 'text-white/60 group-hover:text-white'
                  }`}
                />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* User Profile & Logout Bottom Area */}
        <div className="p-4 border-t border-white/10 bg-black/20">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-3 min-w-0">
              <div
                className="w-10 h-10 rounded-full bg-neonGreen/20 text-neonGreen border border-neonGreen/30 font-bold flex items-center justify-center text-sm shadow-[0_0_12px_rgba(0,230,118,0.15)] flex-shrink-0"
                title={userEmail}
              >
                {avatarLetter}
              </div>
              <div className="min-w-0">
                <p className="text-xs font-semibold text-white/90 truncate" title={userEmail}>
                  {displayName}
                </p>
                <p className="text-[11px] text-white/40 truncate" title={userEmail}>
                  {userEmail || 'Giriş yapıldı'}
                </p>
              </div>
            </div>

            <button
              type="button"
              onClick={handleSignOut}
              title="Çıkış Yap"
              aria-label="Çıkış Yap"
              className="p-2 text-white/50 hover:text-expenseRed hover:bg-expenseRed/10 rounded-xl transition-all flex-shrink-0 cursor-pointer"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </aside>

      {/* ========================================================
          MOBILE BOTTOM NAVIGATION BAR (Fixed Bottom)
          ======================================================== */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-cardDark/90 backdrop-blur-xl border-t border-white/10 px-2 py-1.5 select-none">
        <div className="grid grid-cols-4 gap-1">
          {navItems.map((item) => {
            const active = isItemActive(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex flex-col items-center justify-center py-2 px-1 rounded-xl text-xs transition-all duration-200 ${
                  active
                    ? 'bg-neonGreen/10 text-neonGreen font-semibold shadow-[0_0_10px_rgba(0,230,118,0.1)]'
                    : 'text-white/60 hover:text-white hover:bg-white/5'
                }`}
              >
                <Icon
                  className={`w-5 h-5 mb-1 transition-transform ${
                    active ? 'text-neonGreen scale-110' : 'text-white/60'
                  }`}
                />
                <span className="text-[11px] leading-none tracking-tight">{item.label}</span>
              </Link>
            );
          })}
        </div>
      </nav>
    </>
  );
}
