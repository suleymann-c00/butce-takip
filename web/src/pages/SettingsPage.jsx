import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useTransactions } from '../hooks/useTransactions';
import {
  LogOut,
  Trash2,
  Mail,
  AlertTriangle,
  CheckCircle,
  Database,
  Smartphone,
  Shield,
  Bot,
  Key,
} from 'lucide-react';

export default function SettingsPage() {
  const { user, signOut } = useAuth();
  const { clearAll } = useTransactions();

  // Dialog & state
  const [isConfirmDialogOpen, setIsConfirmDialogOpen] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [resetSuccess, setResetSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  // AI State
  const [apiKey, setApiKey] = useState(() => localStorage.getItem('gemini_api_key') || '');
  const [isApiKeySaved, setIsApiKeySaved] = useState(!!localStorage.getItem('gemini_api_key'));
  const [apiKeyMessage, setApiKeyMessage] = useState('');

  const handleSaveApiKey = () => {
    if (apiKey.trim()) {
      localStorage.setItem('gemini_api_key', apiKey.trim());
      setIsApiKeySaved(true);
      setApiKeyMessage('API Anahtarı kaydedildi.');
    } else {
      localStorage.removeItem('gemini_api_key');
      setIsApiKeySaved(false);
      setApiKeyMessage('API Anahtarı kaldırıldı.');
    }
    setTimeout(() => setApiKeyMessage(''), 3000);
  };

  // Compute avatar initial
  const displayName = user?.displayName || 'Kullanıcı';
  const email = user?.email || 'E-posta belirtilmemiş';
  const initial = (displayName || email || 'K').trim().charAt(0).toUpperCase();

  // Handle logout
  const handleSignOut = async () => {
    try {
      await signOut();
    } catch (err) {
      console.error('Çıkış yapılırken hata:', err);
    }
  };

  // Handle reset all data
  const handleResetData = async () => {
    setIsResetting(true);
    setErrorMessage(null);
    try {
      await clearAll();
      setIsConfirmDialogOpen(false);
      setResetSuccess(true);
      setTimeout(() => setResetSuccess(false), 5000);
    } catch (err) {
      console.error('Veriler sıfırlanırken hata:', err);
      setErrorMessage('Veriler sıfırlanırken bir hata meydana geldi.');
    } finally {
      setIsResetting(false);
    }
  };

  return (
    <div className="min-h-screen bg-deepNight text-gray-100 pb-24 pt-6 px-4 sm:px-6 lg:px-8 max-w-3xl mx-auto space-y-6">
      {/* Header */}
      <div className="pb-4 border-b border-white/10">
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-white">
          Ayarlar
        </h1>
        <p className="mt-1 text-xs sm:text-sm text-gray-400">
          Profil bilgilerinizi, senkronizasyon ayarlarınızı ve verilerinizi yönetin
        </p>
      </div>

      {/* Success Notification */}
      {resetSuccess && (
        <div className="p-4 rounded-xl bg-neonGreen/15 border border-neonGreen/30 text-neonGreen text-sm flex items-center gap-3 animate-fade-in">
          <CheckCircle size={20} className="flex-shrink-0" />
          <span>Tüm verileriniz başarıyla sıfırlandı.</span>
        </div>
      )}

      {/* Error Notification */}
      {errorMessage && (
        <div className="p-4 rounded-xl bg-expenseRed/15 border border-expenseRed/30 text-expenseRed text-sm flex items-center gap-3 animate-fade-in">
          <AlertTriangle size={20} className="flex-shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Profile Card */}
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl">
        <h2 className="text-xs sm:text-sm font-semibold uppercase tracking-wider text-gray-400 mb-4">
          Kullanıcı Profili
        </h2>

        <div className="flex flex-col sm:flex-row items-center gap-5">
          {/* Gradient Avatar Circle */}
          <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-emerald-500 via-teal-400 to-green-300 text-deepNight font-black text-3xl flex items-center justify-center shadow-lg shadow-neonGreen/20 flex-shrink-0">
            {initial}
          </div>

          {/* User Details */}
          <div className="flex-1 text-center sm:text-left space-y-1">
            <h3 className="text-xl font-bold text-white tracking-tight">{displayName}</h3>
            <div className="flex items-center justify-center sm:justify-start gap-2 text-sm text-gray-300">
              <Mail size={15} className="text-gray-400" />
              <span>{email}</span>
            </div>
            {user?.uid && (
              <p className="text-xs text-gray-500 font-mono pt-1">
                Kullanıcı ID: {user.uid}
              </p>
            )}
          </div>

          {/* Sign Out Button */}
          <button
            type="button"
            onClick={handleSignOut}
            className="mt-2 sm:mt-0 flex items-center gap-2 px-4 py-2.5 rounded-xl bg-expenseRed/10 hover:bg-expenseRed text-expenseRed hover:text-white border border-expenseRed/30 font-semibold text-sm transition-all duration-200 shadow-sm"
          >
            <LogOut size={16} />
            <span>Çıkış Yap</span>
          </button>
        </div>
      </div>

      {/* Cloud Sync & Android Status Card */}
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl space-y-4">
        <h2 className="text-xs sm:text-sm font-semibold uppercase tracking-wider text-gray-400">
          Senkronizasyon Durumu
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="p-4 rounded-xl bg-white/5 border border-white/10 flex items-start gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-neonGreen/15 text-neonGreen flex items-center justify-center flex-shrink-0">
              <Database size={18} />
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white">Firestore Veritabanı</h4>
              <p className="text-xs text-gray-400 mt-0.5">
                Verileriniz gerçek zamanlı olarak buluta yedeklenir.
              </p>
              <div className="flex items-center gap-1.5 mt-2 text-xs font-semibold text-neonGreen">
                <span className="w-2 h-2 rounded-full bg-neonGreen animate-pulse" />
                <span>Bağlı ve Aktif</span>
              </div>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-white/5 border border-white/10 flex items-start gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-teal-400/15 text-teal-400 flex items-center justify-center flex-shrink-0">
              <Smartphone size={18} />
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white">Android Eşitleme</h4>
              <p className="text-xs text-gray-400 mt-0.5">
                Bütçe Takip mobil uygulaması ile anlık çift yönlü senkronizasyon.
              </p>
              <div className="flex items-center gap-1.5 mt-2 text-xs font-semibold text-teal-400">
                <Shield size={13} />
                <span>Tam Uyumlu</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      {/* AI Settings */}
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-xs sm:text-sm font-semibold uppercase tracking-wider text-gray-400 flex items-center gap-2">
              <Bot size={16} />
              Yapay Zeka (AI) Ayarları
            </h2>
            <p className="text-xs text-gray-400 mt-1">
              Akıllı finansal asistanı kullanmak için Gemini API anahtarınızı ekleyin.
            </p>
          </div>
          {isApiKeySaved && (
             <div className="flex items-center gap-1.5 text-xs font-semibold text-neonGreen">
               <CheckCircle size={14} />
               <span>Anahtar Aktif</span>
             </div>
          )}
        </div>

        <div className="space-y-3">
          <label className="text-xs font-medium text-gray-300 ml-1">Gemini API Key</label>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Key size={16} className="text-gray-500" />
              </div>
              <input
                type="password"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="AIzaSy..."
                className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-neonGreen/50 focus:ring-1 focus:ring-neonGreen/50 transition-all"
              />
            </div>
            <button
              type="button"
              onClick={handleSaveApiKey}
              className="px-4 py-2.5 bg-white/10 hover:bg-white/20 text-white rounded-xl text-sm font-medium transition-colors whitespace-nowrap"
            >
              Kaydet
            </button>
          </div>
          {apiKeyMessage && (
            <p className="text-xs text-neonGreen ml-1 animate-fade-in">{apiKeyMessage}</p>
          )}
        </div>
      </div>

      {/* Danger Zone: Reset All Data */}
      <div className="glass-card rounded-2xl p-6 border border-expenseRed/20 shadow-xl space-y-4">
        <div>
          <h2 className="text-xs sm:text-sm font-semibold uppercase tracking-wider text-expenseRed">
            Tehlikeli Bölge
          </h2>
          <p className="text-xs text-gray-400 mt-1">
            Hesabınızdaki tüm harcama ve gelir kayıtlarını sıfırlayabilirsiniz.
          </p>
        </div>

        <div className="pt-2 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="text-xs text-gray-400">
            Tüm işlemler ve özel kategoriler veritabanından kalıcı olarak silinir.
          </div>

          <button
            type="button"
            onClick={() => setIsConfirmDialogOpen(true)}
            className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-expenseRed hover:bg-red-600 text-white font-semibold text-sm transition-all duration-200 shadow-lg shadow-expenseRed/20 flex-shrink-0"
          >
            <Trash2 size={16} />
            <span>Tüm Verileri Sıfırla</span>
          </button>
        </div>
      </div>

      {/* About App */}
      <div className="p-4 rounded-2xl bg-white/5 border border-white/5 text-center text-xs text-gray-500 space-y-1">
        <p className="font-semibold text-gray-400">Bütçe Takip Web Companion</p>
        <p>Sürüm 1.0.0 • Dark Theme Edition</p>
      </div>

      {/* Reset Confirmation Dialog Modal */}
      {isConfirmDialogOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md rounded-2xl bg-cardDark border border-expenseRed/30 p-6 shadow-2xl space-y-5">
            <div className="flex items-center gap-3 text-expenseRed">
              <div className="w-11 h-11 rounded-xl bg-expenseRed/15 flex items-center justify-center flex-shrink-0">
                <AlertTriangle size={24} />
              </div>
              <div>
                <h3 className="text-lg font-bold text-white">Tüm Verileri Sıfırla</h3>
                <p className="text-xs text-expenseRed">Bu işlem geri alınamaz!</p>
              </div>
            </div>

            <p className="text-sm text-gray-300 leading-relaxed">
              Tüm gelir ve gider işlemleriniz, özel kategorileriniz ve bütçe geçmişiniz
              kalıcı olarak silinecektir. Devam etmek istediğinize emin misiniz?
            </p>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                disabled={isResetting}
                onClick={() => setIsConfirmDialogOpen(false)}
                className="px-4 py-2 rounded-xl text-sm font-medium text-gray-300 hover:text-white bg-white/5 hover:bg-white/10 transition-colors"
              >
                İptal
              </button>
              <button
                type="button"
                disabled={isResetting}
                onClick={handleResetData}
                className="px-4 py-2 rounded-xl text-sm font-bold text-white bg-expenseRed hover:bg-red-600 transition-colors shadow-lg shadow-expenseRed/30 flex items-center gap-2"
              >
                {isResetting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                    <span>Siliniyor...</span>
                  </>
                ) : (
                  <>
                    <Trash2 size={16} />
                    <span>Evet, Tümünü Sil</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
