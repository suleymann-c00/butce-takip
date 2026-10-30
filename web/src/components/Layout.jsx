import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import { useAuth } from '../hooks/useAuth';
import AiAssistantChat from './AiAssistantChat';

/**
 * Layout Component
 * Standard layout wrapper for authenticated views:
 * - Left sidebar on desktop (w-64)
 * - Mobile bottom bar navigation & top branding bar on mobile
 * - Deep night background & responsive main content container
 */
export default function Layout({ children }) {
  const { user, signOut } = useAuth();

  return (
    <div className="min-h-screen bg-deepNight text-white flex flex-col md:flex-row">
      {/* Sidebar Navigation */}
      <Sidebar user={user} onSignOut={signOut} />

      {/* Main Content Area */}
      <main className="flex-1 min-w-0 flex flex-col pt-16 md:pt-0 pb-20 md:pb-6 overflow-y-auto">
        <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8 flex-1">
          {children || <Outlet />}
        </div>
      </main>

      <AiAssistantChat />
    </div>
  );
}
