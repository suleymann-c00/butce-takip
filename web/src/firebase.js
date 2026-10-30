import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyBityWi9j7YHTnPPWTas0hc3HAJnS77kQA",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "butce-takip-485ef.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "butce-takip-485ef",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "butce-takip-485ef.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "1016202490713",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:1016202490713:web:ed05834a5b41050a5b9248",
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || "G-CQ9MYDYL7H"
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);
export const googleProvider = new GoogleAuthProvider();

export default app;
