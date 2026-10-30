import { useState, useEffect, useCallback, useRef } from 'react';
import { doc, onSnapshot, setDoc } from 'firebase/firestore';
import { db, auth } from '../firebase';
import { useAuth } from './useAuth';
import { INCOME_CATEGORIES } from '../constants';

/**
 * Generate a unique ID (UUID v4 compatible or fallback)
 */
function generateId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `tx_${Date.now()}_${Math.random().toString(36).substring(2, 11)}`;
}

/**
 * Custom React hook for Firestore real-time transaction synchronization
 * Listens to doc('users', user.uid) with onSnapshot
 * Transactions and custom categories are stored as JSON strings in Firestore to match Android app
 *
 * @param {object} [userOverride] - Optional Firebase User object. Defaults to useAuth() user or auth.currentUser
 * @returns {{
 *   transactions: Array<object>,
 *   customCategories: Array<object>,
 *   loading: boolean,
 *   error: string|null,
 *   addTransaction: (tx: object) => Promise<object>,
 *   deleteTransaction: (id: string) => Promise<void>,
 *   clearAll: () => Promise<void>,
 *   updateTransaction: (id: string, updatedFields: object) => Promise<void>,
 *   addCustomCategory: (cat: object) => Promise<object>,
 *   deleteCustomCategory: (catId: string) => Promise<void>
 * }}
 */
export function useTransactions(userOverride = null) {
  const authContext = useAuth();
  const currentUser = userOverride || authContext?.user || auth?.currentUser;
  const uid = currentUser?.uid;

  const [transactions, setTransactions] = useState([]);
  const [customCategories, setCustomCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // References to maintain current state for async actions
  const transactionsRef = useRef(transactions);
  const customCategoriesRef = useRef(customCategories);

  useEffect(() => {
    transactionsRef.current = transactions;
  }, [transactions]);

  useEffect(() => {
    customCategoriesRef.current = customCategories;
  }, [customCategories]);

  // Real-time Firestore sync with onSnapshot
  useEffect(() => {
    if (!uid) {
      setTransactions([]);
      setCustomCategories([]);
      transactionsRef.current = [];
      customCategoriesRef.current = [];
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    const userDocRef = doc(db, 'users', uid);

    const unsubscribe = onSnapshot(
      userDocRef,
      (snapshot) => {
        if (snapshot.exists()) {
          const data = snapshot.data();

          // 1. Parse 'transactions' JSON string
          let parsedTransactions = [];
          if (data?.transactions) {
            try {
              const raw = typeof data.transactions === 'string'
                ? JSON.parse(data.transactions)
                : data.transactions;
              if (Array.isArray(raw)) {
                // Keep sorted by date descending (latest first)
                parsedTransactions = [...raw].sort((a, b) => (b.date || 0) - (a.date || 0));
              }
            } catch (err) {
              console.error('İşlemler verisi ayrıştırılırken hata oluştu:', err);
            }
          }
          setTransactions(parsedTransactions);
          transactionsRef.current = parsedTransactions;

          // 2. Parse 'custom_categories' JSON string
          let parsedCustomCategories = [];
          if (data?.custom_categories) {
            try {
              const raw = typeof data.custom_categories === 'string'
                ? JSON.parse(data.custom_categories)
                : data.custom_categories;
              if (Array.isArray(raw)) {
                parsedCustomCategories = raw;
              }
            } catch (err) {
              console.error('Özel kategoriler verisi ayrıştırılırken hata oluştu:', err);
            }
          }
          setCustomCategories(parsedCustomCategories);
          customCategoriesRef.current = parsedCustomCategories;
        } else {
          // Document does not exist yet (new user)
          setTransactions([]);
          setCustomCategories([]);
          transactionsRef.current = [];
          customCategoriesRef.current = [];
        }
        setLoading(false);
      },
      (err) => {
        console.error('Firestore onSnapshot dinleme hatası:', err);
        setError('Veriler senkronize edilirken bir hata oluştu.');
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [uid]);

  /**
   * Add a new transaction
   * Serializes to JSON string and writes to Firestore via setDoc with merge: true
   */
  const addTransaction = useCallback(
    async (tx) => {
      if (!uid) {
        throw new Error('İşlem eklemek için lütfen giriş yapın.');
      }

      // Determine transaction type
      let type = tx.type;
      if (!type) {
        type = INCOME_CATEGORIES.includes(tx.category) ? 'INCOME' : 'EXPENSE';
      }

      // Construct normalized transaction matching Android model
      const newTx = {
        id: tx.id || generateId(),
        title: (tx.title || '').trim(),
        amount: typeof tx.amount === 'number' ? tx.amount : parseFloat(tx.amount) || 0,
        category: tx.category || 'OTHER_EXPENSE',
        customCategoryName: tx.customCategoryName || null,
        customCategoryIcon: tx.customCategoryIcon || null,
        currency: tx.currency || 'TL',
        date: typeof tx.date === 'number' ? tx.date : (tx.date ? new Date(tx.date).getTime() : Date.now()),
        note: (tx.note || '').trim(),
        isRecurring: Boolean(tx.isRecurring),
        type: type,
      };

      const currentList = transactionsRef.current || [];
      const updatedTransactions = [newTx, ...currentList].sort((a, b) => (b.date || 0) - (a.date || 0));

      const userDocRef = doc(db, 'users', uid);
      await setDoc(
        userDocRef,
        {
          transactions: JSON.stringify(updatedTransactions),
          custom_categories: JSON.stringify(customCategoriesRef.current || []),
          last_updated: Date.now(),
        },
        { merge: true }
      );

      return newTx;
    },
    [uid]
  );

  /**
   * Delete a transaction by ID
   */
  const deleteTransaction = useCallback(
    async (id) => {
      if (!uid) {
        throw new Error('İşlem silmek için lütfen giriş yapın.');
      }

      const currentList = transactionsRef.current || [];
      const updatedTransactions = currentList.filter((item) => item.id !== id);

      const userDocRef = doc(db, 'users', uid);
      await setDoc(
        userDocRef,
        {
          transactions: JSON.stringify(updatedTransactions),
          custom_categories: JSON.stringify(customCategoriesRef.current || []),
          last_updated: Date.now(),
        },
        { merge: true }
      );
    },
    [uid]
  );

  /**
   * Update an existing transaction
   */
  const updateTransaction = useCallback(
    async (id, updatedFields) => {
      if (!uid) {
        throw new Error('İşlem güncellemek için lütfen giriş yapın.');
      }

      const currentList = transactionsRef.current || [];
      const updatedTransactions = currentList
        .map((item) => {
          if (item.id !== id) return item;
          const merged = { ...item, ...updatedFields };

          if (updatedFields.amount !== undefined) {
            merged.amount =
              typeof updatedFields.amount === 'number'
                ? updatedFields.amount
                : parseFloat(updatedFields.amount) || 0;
          }
          if (updatedFields.date !== undefined && typeof updatedFields.date !== 'number') {
            merged.date = new Date(updatedFields.date).getTime();
          }
          if (!merged.type) {
            merged.type = INCOME_CATEGORIES.includes(merged.category) ? 'INCOME' : 'EXPENSE';
          }
          return merged;
        })
        .sort((a, b) => (b.date || 0) - (a.date || 0));

      const userDocRef = doc(db, 'users', uid);
      await setDoc(
        userDocRef,
        {
          transactions: JSON.stringify(updatedTransactions),
          custom_categories: JSON.stringify(customCategoriesRef.current || []),
          last_updated: Date.now(),
        },
        { merge: true }
      );
    },
    [uid]
  );

  /**
   * Clear all transactions and custom categories
   */
  const clearAll = useCallback(async () => {
    if (!uid) {
      throw new Error('Verileri sıfırlamak için lütfen giriş yapın.');
    }

    const userDocRef = doc(db, 'users', uid);
    await setDoc(
      userDocRef,
      {
        transactions: JSON.stringify([]),
        custom_categories: JSON.stringify([]),
        last_updated: Date.now(),
      },
      { merge: true }
    );
  }, [uid]);

  /**
   * Add a custom category
   */
  const addCustomCategory = useCallback(
    async (categoryDef) => {
      if (!uid) {
        throw new Error('Kategori eklemek için lütfen giriş yapın.');
      }

      const newCategory = {
        id: categoryDef.id || generateId(),
        name: (categoryDef.name || '').trim(),
        iconName: categoryDef.iconName || 'Star',
        type: categoryDef.type || 'EXPENSE',
      };

      const currentCats = customCategoriesRef.current || [];
      const updatedCats = [...currentCats, newCategory];

      const userDocRef = doc(db, 'users', uid);
      await setDoc(
        userDocRef,
        {
          transactions: JSON.stringify(transactionsRef.current || []),
          custom_categories: JSON.stringify(updatedCats),
          last_updated: Date.now(),
        },
        { merge: true }
      );

      return newCategory;
    },
    [uid]
  );

  /**
   * Delete a custom category by ID
   */
  const deleteCustomCategory = useCallback(
    async (catId) => {
      if (!uid) {
        throw new Error('Kategori silmek için lütfen giriş yapın.');
      }

      const currentCats = customCategoriesRef.current || [];
      const updatedCats = currentCats.filter((c) => c.id !== catId);

      const userDocRef = doc(db, 'users', uid);
      await setDoc(
        userDocRef,
        {
          transactions: JSON.stringify(transactionsRef.current || []),
          custom_categories: JSON.stringify(updatedCats),
          last_updated: Date.now(),
        },
        { merge: true }
      );
    },
    [uid]
  );

  return {
    transactions,
    customCategories,
    loading,
    error,
    addTransaction,
    deleteTransaction,
    updateTransaction,
    clearAll,
    addCustomCategory,
    deleteCustomCategory,
  };
}

export default useTransactions;
