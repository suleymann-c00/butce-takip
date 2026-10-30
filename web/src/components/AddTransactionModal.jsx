import React, { useState, useEffect } from 'react';
import {
  CATEGORIES,
  INCOME_CATEGORIES,
  EXPENSE_CATEGORIES,
  getCategoryIcon,
  getCategoryLabel,
  CURRENCIES,
} from '../constants';
import { X, Check, Loader2, Sparkles } from 'lucide-react';

/**
 * Generate a UUID v4 or random fallback ID
 */
function generateId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `tx_${Date.now()}_${Math.random().toString(36).substring(2, 11)}`;
}

/**
 * AddTransactionModal Component
 * Modal overlay for adding income and expense transactions.
 *
 * @param {object} props
 * @param {boolean} props.isOpen - Whether modal is visible
 * @param {Function} props.onClose - Modal close handler
 * @param {Function} props.addTransaction - Function from useTransactions to persist transaction
 */
export default function AddTransactionModal({ isOpen, onClose, addTransaction }) {
  // Form States
  const [type, setType] = useState('EXPENSE'); // 'INCOME' | 'EXPENSE'
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('TRY');
  const [category, setCategory] = useState('FOOD');
  const [customCategoryName, setCustomCategoryName] = useState('');
  const [note, setNote] = useState('');
  const [isRecurring, setIsRecurring] = useState(false);

  // Status States
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Currency options matching constants
  const currencyPills = [
    { key: 'TRY', label: 'TRY (₺)' },
    { key: 'USD', label: 'USD ($)' },
    { key: 'EUR', label: 'EUR (€)' },
    { key: 'GOLD', label: 'Altın (gr)' },
  ];

  // Reset form when modal opens or closes
  useEffect(() => {
    if (isOpen) {
      setType('EXPENSE');
      setTitle('');
      setAmount('');
      setCurrency('TRY');
      setCategory('FOOD');
      setCustomCategoryName('');
      setNote('');
      setIsRecurring(false);
      setErrorMessage('');
      setSubmitting(false);
    }
  }, [isOpen]);

  // Handle ESC key press to close modal
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  // Change type and adjust default category
  const handleTypeChange = (newType) => {
    setType(newType);
    if (newType === 'INCOME') {
      setCategory(INCOME_CATEGORIES[0]); // 'SALARY'
    } else {
      setCategory(EXPENSE_CATEGORIES[0]); // 'FOOD'
    }
    setErrorMessage('');
  };

  const categoriesList =
    type === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;

  // Submission handler
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setErrorMessage('Lütfen işlem adını giriniz.');
      return;
    }

    const parsedAmount = parseFloat(amount.toString().replace(',', '.'));
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setErrorMessage('Lütfen 0\'dan büyük geçerli bir tutar giriniz.');
      return;
    }

    try {
      setSubmitting(true);

      const transactionPayload = {
        id: generateId(),
        title: trimmedTitle,
        amount: parsedAmount,
        type: type,
        category: category,
        customCategoryName:
          category === 'CUSTOM' ? customCategoryName.trim() : null,
        currency: currency === 'TRY' ? 'TL' : currency,
        date: Date.now(),
        note: note.trim(),
        isRecurring: Boolean(isRecurring),
      };

      await addTransaction(transactionPayload);
      onClose();
    } catch (err) {
      console.error('İşlem kaydedilirken hata oluştu:', err);
      setErrorMessage(
        err?.message || 'İşlem kaydedilirken bir hata oluştu. Lütfen tekrar deneyin.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm overflow-y-auto"
      onClick={(e) => {
        if (e.target === e.currentTarget && !submitting) {
          onClose();
        }
      }}
    >
      <div className="relative w-full max-w-lg overflow-hidden rounded-2xl border border-white/10 bg-cardDark shadow-2xl transition-all my-8">
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-white/10 px-6 py-4">
          <h3 className="text-lg font-bold text-white">Yeni İşlem Ekle</h3>
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-white/10 hover:text-white transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Error Banner */}
          {errorMessage && (
            <div className="rounded-xl border border-expenseRed/30 bg-expenseRed/10 p-3 text-xs text-expenseRed font-medium">
              {errorMessage}
            </div>
          )}

          {/* Segmented Control: Gelir / Gider */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">
              İşlem Türü
            </label>
            <div className="grid grid-cols-2 gap-2 rounded-xl bg-deepNight p-1 border border-white/5">
              <button
                type="button"
                onClick={() => handleTypeChange('EXPENSE')}
                className={`flex items-center justify-center py-2.5 rounded-lg text-sm font-semibold transition-all ${
                  type === 'EXPENSE'
                    ? 'bg-expenseRed text-white shadow-lg shadow-expenseRed/20'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Gider
              </button>
              <button
                type="button"
                onClick={() => handleTypeChange('INCOME')}
                className={`flex items-center justify-center py-2.5 rounded-lg text-sm font-semibold transition-all ${
                  type === 'INCOME'
                    ? 'bg-neonGreen text-deepNight shadow-lg shadow-neonGreen/20 font-bold'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Gelir
              </button>
            </div>
          </div>

          {/* Title Input */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">
              İşlem Adı <span className="text-expenseRed">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="Örn: Market Alışverişi, Maaş, Kira..."
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-deepNight/80 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:border-neonGreen focus:outline-none focus:ring-1 focus:ring-neonGreen transition-all"
            />
          </div>

          {/* Amount & Currency */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Amount */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">
                Tutar <span className="text-expenseRed">*</span>
              </label>
              <input
                type="number"
                step="any"
                min="0.01"
                required
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full rounded-xl border border-white/10 bg-deepNight/80 px-4 py-2.5 text-sm text-white placeholder-gray-500 font-mono focus:border-neonGreen focus:outline-none focus:ring-1 focus:ring-neonGreen transition-all"
              />
            </div>

            {/* Currency Selector Pills */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">
                Para Birimi
              </label>
              <div className="grid grid-cols-2 gap-1.5">
                {currencyPills.map((pill) => {
                  const isCurrSelected = currency === pill.key;
                  return (
                    <button
                      key={pill.key}
                      type="button"
                      onClick={() => setCurrency(pill.key)}
                      className={`px-2 py-2 rounded-lg text-xs font-semibold border transition-all text-center ${
                        isCurrSelected
                          ? 'bg-neonGreen text-deepNight border-neonGreen shadow-sm'
                          : 'bg-deepNight/60 text-gray-300 border-white/10 hover:bg-white/5'
                      }`}
                    >
                      {pill.label}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Category Selector Grid */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">
              Kategori
            </label>
            <div className="grid grid-cols-3 sm:grid-cols-4 gap-2 max-h-44 overflow-y-auto pr-1">
              {categoriesList.map((catKey) => {
                const isCatSelected = category === catKey;
                const IconComponent = getCategoryIcon(catKey);
                const label = getCategoryLabel(catKey);

                return (
                  <button
                    key={catKey}
                    type="button"
                    onClick={() => setCategory(catKey)}
                    className={`flex flex-col items-center justify-center p-2 rounded-xl border text-center transition-all ${
                      isCatSelected
                        ? 'bg-neonGreen/15 border-neonGreen text-white shadow-sm'
                        : 'bg-deepNight/60 border-white/5 text-gray-400 hover:bg-white/5 hover:text-gray-200'
                    }`}
                  >
                    <IconComponent
                      className={`h-5 w-5 mb-1 ${
                        isCatSelected ? 'text-neonGreen' : 'text-gray-400'
                      }`}
                    />
                    <span className="text-[11px] leading-tight line-clamp-1 w-full truncate">
                      {label}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Custom category name input if category is CUSTOM */}
          {category === 'CUSTOM' && (
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">
                Özel Kategori Adı
              </label>
              <input
                type="text"
                placeholder="Örn: Evcil Hayvan, Hobi..."
                value={customCategoryName}
                onChange={(e) => setCustomCategoryName(e.target.value)}
                className="w-full rounded-xl border border-white/10 bg-deepNight/80 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:border-neonGreen focus:outline-none focus:ring-1 focus:ring-neonGreen transition-all"
              />
            </div>
          )}

          {/* Note input */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">
              Not (İsteğe Bağlı)
            </label>
            <input
              type="text"
              placeholder="Ek açıklama veya not..."
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full rounded-xl border border-white/10 bg-deepNight/80 px-4 py-2.5 text-sm text-white placeholder-gray-500 focus:border-neonGreen focus:outline-none focus:ring-1 focus:ring-neonGreen transition-all"
            />
          </div>

          {/* Recurring Checkbox */}
          <div className="flex items-center gap-3 pt-1">
            <input
              id="isRecurring"
              type="checkbox"
              checked={isRecurring}
              onChange={(e) => setIsRecurring(e.target.checked)}
              className="h-4 w-4 rounded border-gray-700 bg-deepNight text-neonGreen focus:ring-neonGreen focus:ring-offset-deepNight accent-neonGreen cursor-pointer"
            />
            <label
              htmlFor="isRecurring"
              className="text-xs sm:text-sm font-medium text-gray-300 cursor-pointer select-none"
            >
              Düzenli İşlem (Her ay tekrarlanan sabit işlem)
            </label>
          </div>

          {/* Footer Buttons */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-white/10">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2.5 rounded-xl text-sm font-medium text-gray-300 hover:text-white bg-white/5 hover:bg-white/10 border border-white/10 transition-colors"
            >
              İptal
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl text-sm font-bold bg-neonGreen hover:bg-neonGreen/90 text-deepNight shadow-lg shadow-neonGreen/20 transition-all disabled:opacity-50 cursor-pointer"
            >
              {submitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Kaydediliyor...</span>
                </>
              ) : (
                <>
                  <Check className="h-4 w-4 stroke-[3]" />
                  <span>Kaydet</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
