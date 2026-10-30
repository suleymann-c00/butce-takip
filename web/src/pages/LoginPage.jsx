import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Wallet,
  Mail,
  Lock,
  Eye,
  EyeOff,
  Loader2,
  AlertCircle,
  CheckCircle2,
  X,
  ArrowLeft,
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export default function LoginPage() {
  const navigate = useNavigate();
  const {
    user,
    loading: authLoading,
    error: authError,
    signInWithEmail,
    signUpWithEmail,
    signInWithGoogle,
    resetPassword,
    clearError,
  } = useAuth();

  // Mode: 'login' | 'register' | 'forgot'
  const [mode, setMode] = useState('login');

  // Form states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Status states
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [toast, setToast] = useState(null);

  // Redirect if already authenticated
  useEffect(() => {
    if (user && !authLoading) {
      navigate('/', { replace: true });
    }
  }, [user, authLoading, navigate]);

  // Sync auth errors from hook
  useEffect(() => {
    if (authError) {
      setToast({ type: 'error', message: authError });
    }
  }, [authError]);

  // Auto-dismiss toast after 6 seconds
  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => {
      setToast(null);
    }, 6000);
    return () => clearTimeout(timer);
  }, [toast]);

  const switchMode = (newMode) => {
    setMode(newMode);
    setToast(null);
    clearError?.();
    setPassword('');
    setConfirmPassword('');
  };

  // Form submit handler
  const handleSubmit = async (e) => {
    e.preventDefault();
    setToast(null);
    clearError?.();

    if (!email.trim()) {
      setToast({ type: 'error', message: 'Lütfen e-posta adresinizi girin.' });
      return;
    }

    // Forgot password flow
    if (mode === 'forgot') {
      try {
        setIsSubmitting(true);
        await resetPassword(email);
        setToast({
          type: 'success',
          message: 'Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen gelen kutunuzu kontrol edin.',
        });
      } catch (err) {
        setToast({
          type: 'error',
          message: err.friendlyMessage || err.message || 'Şifre sıfırlama e-postası gönderilemedi.',
        });
      } finally {
        setIsSubmitting(false);
      }
      return;
    }

    // Common password validation
    if (!password) {
      setToast({ type: 'error', message: 'Lütfen şifrenizi girin.' });
      return;
    }

    if (password.length < 6) {
      setToast({ type: 'error', message: 'Şifre en az 6 karakter olmalıdır.' });
      return;
    }

    // Register flow
    if (mode === 'register') {
      if (password !== confirmPassword) {
        setToast({ type: 'error', message: 'Şifreler birbiriyle eşleşmiyor.' });
        return;
      }

      try {
        setIsSubmitting(true);
        await signUpWithEmail(email, password);
        navigate('/');
      } catch (err) {
        setToast({
          type: 'error',
          message: err.friendlyMessage || err.message || 'Kayıt işlemi başarısız oldu.',
        });
      } finally {
        setIsSubmitting(false);
      }
      return;
    }

    // Login flow
    if (mode === 'login') {
      try {
        setIsSubmitting(true);
        await signInWithEmail(email, password);
        navigate('/');
      } catch (err) {
        setToast({
          type: 'error',
          message: err.friendlyMessage || err.message || 'Giriş yapılamadı.',
        });
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  // Google sign-in handler
  const handleGoogleSignIn = async () => {
    setToast(null);
    clearError?.();
    try {
      setIsSubmitting(true);
      await signInWithGoogle();
      navigate('/');
    } catch (err) {
      setToast({
        type: 'error',
        message: err.friendlyMessage || err.message || 'Google ile giriş başarısız oldu.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const isLoading = isSubmitting || authLoading;

  return (
    <div className="min-h-screen w-full bg-deepNight flex items-center justify-center p-4 sm:p-6 relative overflow-hidden select-none font-sans">
      {/* Background ambient neon glow effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-neonGreen/5 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute top-10 right-10 w-72 h-72 bg-emerald-500/5 rounded-full blur-2xl pointer-events-none" />

      {/* Animated Wave Effects at bottom */}
      <div className="absolute bottom-0 left-0 right-0 h-44 sm:h-56 pointer-events-none overflow-hidden z-0">
        {/* Wave 1 */}
        <div className="animate-wave-1 absolute -bottom-10 -left-1/2 w-[200%] h-36 sm:h-48 text-[#00E676] opacity-15">
          <svg
            viewBox="0 0 1200 120"
            preserveAspectRatio="none"
            className="w-full h-full fill-current"
          >
            <path d="M0,0 C150,90 350,-40 500,60 C650,160 900,10 1200,40 L1200,120 L0,120 Z" />
          </svg>
        </div>

        {/* Wave 2 */}
        <div className="animate-wave-2 absolute -bottom-6 -left-1/2 w-[200%] h-40 sm:h-52 text-[#00E676] opacity-10">
          <svg
            viewBox="0 0 1200 120"
            preserveAspectRatio="none"
            className="w-full h-full fill-current"
          >
            <path d="M0,30 C200,110 450,10 700,70 C950,130 1100,20 1200,50 L1200,120 L0,120 Z" />
          </svg>
        </div>

        {/* Wave 3 */}
        <div className="animate-wave-3 absolute -bottom-2 -left-1/2 w-[200%] h-44 sm:h-56 text-[#00E676] opacity-20">
          <svg
            viewBox="0 0 1200 120"
            preserveAspectRatio="none"
            className="w-full h-full fill-current"
          >
            <path d="M0,60 C300,10 600,100 800,40 C1000,-20 1150,80 1200,60 L1200,120 L0,120 Z" />
          </svg>
        </div>
      </div>

      {/* Centered Login Card */}
      <div className="w-full max-w-md relative z-10 my-auto">
        {/* Toast-like notification */}
        {toast && (
          <div
            className={`mb-4 p-4 rounded-xl border backdrop-blur-xl flex items-start justify-between gap-3 shadow-2xl transition-all duration-300 animate-in fade-in slide-in-from-top-2 ${
              toast.type === 'error'
                ? 'bg-red-500/15 border-red-500/30 text-red-200'
                : 'bg-emerald-500/15 border-emerald-500/30 text-emerald-200'
            }`}
            role="alert"
          >
            <div className="flex items-start gap-3">
              {toast.type === 'error' ? (
                <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
              ) : (
                <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
              )}
              <span className="text-sm font-medium leading-relaxed">{toast.message}</span>
            </div>
            <button
              type="button"
              onClick={() => setToast(null)}
              className="text-gray-400 hover:text-white p-1 rounded-lg hover:bg-white/10 transition flex-shrink-0 cursor-pointer"
              aria-label="Kapat"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Card Body */}
        <div className="bg-cardDark/80 backdrop-blur-xl border border-white/10 rounded-2xl p-6 sm:p-8 shadow-2xl shadow-black/50">
          {/* Header */}
          <div className="text-center mb-6">
            {/* Green wallet icon inside rounded gradient box */}
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-neonGreen/25 via-neonGreen/10 to-transparent border border-neonGreen/30 shadow-lg shadow-neonGreen/10 flex items-center justify-center mx-auto mb-4 group hover:scale-105 transition-transform duration-300">
              <Wallet className="w-8 h-8 text-neonGreen stroke-[2.2]" />
            </div>

            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Bütçe Takip
            </h1>
            <p className="text-sm text-gray-400 mt-1">
              Finanslarını kontrol altına al
            </p>
          </div>

          {/* Segmented Control / Pill Selector (Login vs Register) */}
          {mode !== 'forgot' ? (
            <div className="p-1 bg-black/40 border border-white/10 rounded-xl flex items-center mb-6">
              <button
                type="button"
                onClick={() => switchMode('login')}
                className={`flex-1 py-2.5 text-xs sm:text-sm font-semibold rounded-lg transition-all duration-200 cursor-pointer ${
                  mode === 'login'
                    ? 'bg-neonGreen text-deepNight shadow-md shadow-neonGreen/20'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Giriş Yap
              </button>
              <button
                type="button"
                onClick={() => switchMode('register')}
                className={`flex-1 py-2.5 text-xs sm:text-sm font-semibold rounded-lg transition-all duration-200 cursor-pointer ${
                  mode === 'register'
                    ? 'bg-neonGreen text-deepNight shadow-md shadow-neonGreen/20'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Kayıt Ol
              </button>
            </div>
          ) : (
            <div className="flex items-center justify-between mb-6 pb-2 border-b border-white/10">
              <button
                type="button"
                onClick={() => switchMode('login')}
                className="flex items-center gap-1.5 text-xs font-medium text-gray-400 hover:text-white transition cursor-pointer"
              >
                <ArrowLeft className="w-4 h-4 text-neonGreen" />
                <span>Giriş Yapmaya Dön</span>
              </button>
              <span className="text-xs font-medium text-neonGreen">Şifre Sıfırlama</span>
            </div>
          )}

          {/* Forgot Password Notice */}
          {mode === 'forgot' && (
            <div className="mb-5 text-left">
              <h2 className="text-base font-semibold text-white">Şifrenizi mi unuttunuz?</h2>
              <p className="text-xs text-gray-400 mt-1">
                Kayıtlı e-posta adresinizi girin. Size şifrenizi sıfırlamanız için güvenli bir bağlantı göndereceğiz.
              </p>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email Field */}
            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                E-posta
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="adiniz@ornek.com"
                  disabled={isLoading}
                  autoComplete="email"
                  className="w-full pl-10 pr-4 py-3 bg-white/5 border border-white/10 focus:border-neonGreen focus:ring-1 focus:ring-neonGreen rounded-xl text-white placeholder-gray-500 outline-none transition text-sm disabled:opacity-50"
                />
              </div>
            </div>

            {/* Password Field (for Login and Register modes) */}
            {mode !== 'forgot' && (
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-xs font-semibold text-gray-300">
                    Şifre
                  </label>
                  {mode === 'login' && (
                    <button
                      type="button"
                      onClick={() => switchMode('forgot')}
                      className="text-xs text-neonGreen hover:underline focus:outline-none transition cursor-pointer"
                    >
                      Şifremi Unuttum
                    </button>
                  )}
                </div>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    disabled={isLoading}
                    autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                    className="w-full pl-10 pr-11 py-3 bg-white/5 border border-white/10 focus:border-neonGreen focus:ring-1 focus:ring-neonGreen rounded-xl text-white placeholder-gray-500 outline-none transition text-sm disabled:opacity-50"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-400 hover:text-gray-200 focus:outline-none cursor-pointer"
                    tabIndex={-1}
                    aria-label={showPassword ? 'Şifreyi gizle' : 'Şifreyi göster'}
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>
            )}

            {/* Confirm Password Field (Register mode only) */}
            {mode === 'register' && (
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Şifre Tekrar
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
                    <Lock className="w-4 h-4" />
                  </div>
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="••••••••"
                    disabled={isLoading}
                    autoComplete="new-password"
                    className="w-full pl-10 pr-11 py-3 bg-white/5 border border-white/10 focus:border-neonGreen focus:ring-1 focus:ring-neonGreen rounded-xl text-white placeholder-gray-500 outline-none transition text-sm disabled:opacity-50"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-400 hover:text-gray-200 focus:outline-none cursor-pointer"
                    tabIndex={-1}
                    aria-label={showConfirmPassword ? 'Şifreyi gizle' : 'Şifreyi göster'}
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>
            )}

            {/* Main Action Button */}
            <div className="pt-2">
              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3.5 px-4 bg-neonGreen hover:bg-[#00c968] active:scale-[0.99] text-deepNight font-bold rounded-xl transition duration-200 flex items-center justify-center gap-2 shadow-lg shadow-neonGreen/20 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer text-sm sm:text-base"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    <span>Lütfen bekleyin...</span>
                  </>
                ) : mode === 'login' ? (
                  'Giriş Yap'
                ) : mode === 'register' ? (
                  'Kayıt Ol'
                ) : (
                  'Sıfırlama Bağlantısı Gönder'
                )}
              </button>
            </div>
          </form>

          {/* Social Sign-in section (Only on login / register) */}
          {mode !== 'forgot' && (
            <>
              {/* Divider with 'veya' */}
              <div className="relative my-6 flex items-center justify-center">
                <div className="w-full border-t border-white/10" />
                <span className="bg-[#181829] px-3 text-xs uppercase tracking-wider text-gray-400 absolute">
                  veya
                </span>
              </div>

              {/* Google Sign-in Button */}
              <button
                type="button"
                onClick={handleGoogleSignIn}
                disabled={isLoading}
                className="w-full py-3 px-4 bg-white/5 hover:bg-white/10 border border-white/15 hover:border-white/25 rounded-xl text-white font-medium text-sm transition duration-200 flex items-center justify-center gap-3 active:scale-[0.99] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
              >
                <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                  />
                </svg>
                <span>Google ile Giriş Yap</span>
              </button>
            </>
          )}

          {/* Footer note */}
          <p className="text-center text-xs text-gray-500 mt-6">
            Bütçe Takip © {new Date().getFullYear()} — Tüm hakları saklıdır
          </p>
        </div>
      </div>
    </div>
  );
}
