import React, { useState, useEffect, useContext, createContext, useCallback, createElement } from 'react';
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInWithPopup,
  signOut as firebaseSignOut,
  sendPasswordResetEmail,
  onAuthStateChanged,
} from 'firebase/auth';
import { auth, googleProvider } from '../firebase';

/**
 * Translate Firebase Auth error codes into friendly Turkish messages
 */
export function getAuthErrorMessage(errorCode) {
  switch (errorCode) {
    case 'auth/invalid-email':
      return 'Geçersiz e-posta adresi biçimi.';
    case 'auth/user-disabled':
      return 'Bu kullanıcı hesabı devre dışı bırakılmış.';
    case 'auth/user-not-found':
      return 'Bu e-posta adresine kayıtlı bir kullanıcı bulunamadı.';
    case 'auth/wrong-password':
    case 'auth/invalid-credential':
      return 'E-posta veya şifre hatalı.';
    case 'auth/email-already-in-use':
      return 'Bu e-posta adresi zaten kullanımda.';
    case 'auth/weak-password':
      return 'Şifre çok zayıf. En az 6 karakter olmalıdır.';
    case 'auth/popup-closed-by-user':
      return 'Giriş penceresi kullanıcı tarafından kapatıldı.';
    case 'auth/cancelled-popup-request':
      return 'Giriş işlemi iptal edildi.';
    case 'auth/network-request-failed':
      return 'Ağ bağlantı hatası. Lütfen internet bağlantınızı kontrol edin.';
    case 'auth/too-many-requests':
      return 'Çok fazla başarısız deneme yapıldı. Lütfen biraz bekleyip tekrar deneyin.';
    case 'auth/requires-recent-login':
      return 'Bu işlem için tekrar giriş yapmanız gerekmektedir.';
    case 'auth/operation-not-allowed':
      return 'Bu giriş yöntemi henüz etkinleştirilmedi.';
    default:
      return 'İşlem sırasında bir hata oluştu. Lütfen tekrar deneyin.';
  }
}

/**
 * Internal hook logic for Firebase Auth
 */
function useProvideAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Listen for Firebase Auth state changes
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(
      auth,
      (currentUser) => {
        setUser(currentUser);
        setLoading(false);
      },
      (err) => {
        console.error('Firebase Auth State Error:', err);
        setError(getAuthErrorMessage(err.code));
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  // Sign in with email and password
  const signInWithEmail = useCallback(async (email, password) => {
    setError(null);
    try {
      const result = await signInWithEmailAndPassword(auth, email.trim(), password);
      return result.user;
    } catch (err) {
      const friendlyMsg = getAuthErrorMessage(err.code) || err.message;
      setError(friendlyMsg);
      err.friendlyMessage = friendlyMsg;
      throw err;
    }
  }, []);

  // Sign up with email and password
  const signUpWithEmail = useCallback(async (email, password) => {
    setError(null);
    try {
      const result = await createUserWithEmailAndPassword(auth, email.trim(), password);
      return result.user;
    } catch (err) {
      const friendlyMsg = getAuthErrorMessage(err.code) || err.message;
      setError(friendlyMsg);
      err.friendlyMessage = friendlyMsg;
      throw err;
    }
  }, []);

  // Sign in with Google popup
  const signInWithGoogle = useCallback(async () => {
    setError(null);
    try {
      const result = await signInWithPopup(auth, googleProvider);
      return result.user;
    } catch (err) {
      const friendlyMsg = getAuthErrorMessage(err.code) || err.message;
      setError(friendlyMsg);
      err.friendlyMessage = friendlyMsg;
      throw err;
    }
  }, []);

  // Sign out
  const signOut = useCallback(async () => {
    setError(null);
    try {
      await firebaseSignOut(auth);
      setUser(null);
    } catch (err) {
      const friendlyMsg = getAuthErrorMessage(err.code) || err.message;
      setError(friendlyMsg);
      err.friendlyMessage = friendlyMsg;
      throw err;
    }
  }, []);

  // Send password reset email
  const resetPassword = useCallback(async (email) => {
    setError(null);
    try {
      await sendPasswordResetEmail(auth, email.trim());
    } catch (err) {
      const friendlyMsg = getAuthErrorMessage(err.code) || err.message;
      setError(friendlyMsg);
      err.friendlyMessage = friendlyMsg;
      throw err;
    }
  }, []);

  return {
    user,
    loading,
    error,
    clearError,
    signInWithEmail,
    signUpWithEmail,
    signInWithGoogle,
    signOut,
    resetPassword,
  };
}

const AuthContext = createContext(null);

/**
 * Optional AuthProvider component to wrap app and share auth state globally
 */
export function AuthProvider({ children }) {
  const authState = useProvideAuth();
  return createElement(AuthContext.Provider, { value: authState }, children);
}

/**
 * Custom React hook for Firebase Authentication
 * Can be used with or without AuthProvider
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context) {
    return context;
  }
  return useProvideAuth();
}

export default useAuth;
