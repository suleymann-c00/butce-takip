import React, { useState, useMemo } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import {
  formatCurrency,
  getCategoryIcon,
  getCategoryLabel,
  CATEGORIES,
  CURRENCIES,
  isIncomeCategory,
} from '../constants';
import { Trash2 } from 'lucide-react';

const CURRENCY_PILLS = [
  { key: 'TL', label: 'TRY', symbol: '₺' },
  { key: 'USD', label: 'USD', symbol: '$' },
  { key: 'EUR', label: 'EUR', symbol: '€' },
  { key: 'GOLD', label: 'GOLD', symbol: 'gr' },
];

function isMatchingCurrency(txCurrency, targetCurrency) {
  const t = (txCurrency || 'TL').toUpperCase();
  const target = (targetCurrency || 'TL').toUpperCase();
  if (target === 'TL' || target === 'TRY') {
    return t === 'TL' || t === 'TRY';
  }
  return t === target;
}

export default function TransactionsPage() {
  const { transactions, loading, deleteTransaction } = useTransactions();

  // State
  const [filterType, setFilterType] = useState(null); // null = all, 'INCOME', 'EXPENSE'
  const [selectedCurrency, setSelectedCurrency] = useState('TL');
  const [deletingId, setDeletingId] = useState(null);

  // Filter transactions
  const filteredTransactions = useMemo(() => {
    return transactions.filter((tx) => {
      // Filter by type
      if (filterType) {
        const txIsIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
        if (filterType === 'INCOME' && !txIsIncome) return false;
        if (filterType === 'EXPENSE' && txIsIncome) return false;
      }
      // Filter by currency (same as dashboard)
      if (selectedCurrency) {
        if (!isMatchingCurrency(tx.currency, selectedCurrency)) {
          return false;
        }
      }
      return true;
    });
  }, [transactions, filterType, selectedCurrency]);

  // Group transactions by date
  // Using: new Date(timestamp).toLocaleDateString('tr-TR', { day: '2-digit', month: 'short', year: 'numeric' })
  const groupedTransactions = useMemo(() => {
    const groups = new Map();

    filteredTransactions.forEach((tx) => {
      const timestamp = tx.date || Date.now();
      const dateKey = new Date(timestamp).toLocaleDateString('tr-TR', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      });

      if (!groups.has(dateKey)) {
        groups.set(dateKey, []);
      }
      groups.get(dateKey).push(tx);
    });

    return Array.from(groups.entries());
  }, [filteredTransactions]);

  // Handle delete
  const handleDelete = async (id, e) => {
    e?.stopPropagation();
    try {
      setDeletingId(id);
      await deleteTransaction(id);
    } catch (err) {
      console.error('İşlem silinirken hata oluştu:', err);
    } finally {
      setDeletingId(null);
    }
  };

  const selectedCurrencyLabel =
    CURRENCY_PILLS.find((c) => c.key === selectedCurrency)?.label || 'TRY';

  return (
    <div className="min-h-screen bg-deepNight text-gray-100 pb-24 pt-6 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto">
      {/* Top Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-6 border-b border-white/10">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-white">
              Tüm İşlemler
            </h1>
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-cardDark border border-white/10 text-gray-300">
              {filteredTransactions.length} işlem
            </span>
          </div>
          <p className="mt-1.5 text-xs sm:text-sm text-gray-400 flex items-center gap-1.5">
            💡 Bir işlemi silmek için üzerindeki çöp kutusu ikonuna tıklayın
          </p>
        </div>

        {/* Currency Selector Pills (same as dashboard) */}
        <div className="inline-flex items-center gap-1 p-1 rounded-2xl bg-cardDark/90 border border-white/10 backdrop-blur-md self-start sm:self-auto shadow-lg">
          {CURRENCY_PILLS.map((pill) => {
            const active = isMatchingCurrency(selectedCurrency, pill.key);
            return (
              <button
                key={pill.key}
                type="button"
                onClick={() => setSelectedCurrency(pill.key)}
                className={`px-3 sm:px-4 py-1.5 rounded-xl text-xs sm:text-sm font-bold transition-all duration-200 cursor-pointer ${
                  active
                    ? 'bg-neonGreen text-deepNight shadow-md shadow-neonGreen/20'
                    : 'text-gray-400 hover:text-white hover:bg-white/5'
                }`}
              >
                {pill.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Filter Chips in a Row */}
      <div className="flex items-center gap-2 sm:gap-3 py-5 overflow-x-auto no-scrollbar">
        {/* All */}
        <button
          type="button"
          onClick={() => setFilterType(null)}
          className={`px-4 py-2 rounded-full text-sm font-medium transition-all whitespace-nowrap ${
            filterType === null
              ? 'bg-white text-black font-semibold shadow-lg shadow-white/10'
              : 'bg-cardDark/80 text-gray-400 hover:text-white border border-white/10 hover:border-white/20'
          }`}
        >
          Tümü
        </button>

        {/* Income Filter */}
        <button
          type="button"
          onClick={() => setFilterType('INCOME')}
          className={`px-4 py-2 rounded-full text-sm font-medium transition-all whitespace-nowrap flex items-center gap-1.5 ${
            filterType === 'INCOME'
              ? 'bg-incomeGreen/20 text-incomeGreen border border-incomeGreen shadow-lg shadow-incomeGreen/20 font-semibold'
              : 'bg-cardDark/80 text-gray-400 hover:text-white border border-white/10 hover:border-incomeGreen/40'
          }`}
        >
          <span>💰 Gelir</span>
        </button>

        {/* Expense Filter */}
        <button
          type="button"
          onClick={() => setFilterType('EXPENSE')}
          className={`px-4 py-2 rounded-full text-sm font-medium transition-all whitespace-nowrap flex items-center gap-1.5 ${
            filterType === 'EXPENSE'
              ? 'bg-expenseRed/20 text-expenseRed border border-expenseRed shadow-lg shadow-expenseRed/20 font-semibold'
              : 'bg-cardDark/80 text-gray-400 hover:text-white border border-white/10 hover:border-expenseRed/40'
          }`}
        >
          <span>💸 Gider</span>
        </button>
      </div>

      {/* Content Section */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20">
          <div className="w-10 h-10 border-4 border-neonGreen/20 border-t-neonGreen rounded-full animate-spin mb-4" />
          <p className="text-gray-400 text-sm">İşlemler yükleniyor...</p>
        </div>
      ) : groupedTransactions.length === 0 ? (
        /* Empty State */
        <div className="glass-card rounded-2xl p-12 text-center my-8 flex flex-col items-center justify-center border border-white/10">
          <span className="text-5xl mb-4 select-none" role="img" aria-label="Arama">
            🔍
          </span>
          <h3 className="text-lg font-semibold text-white mb-1">İşlem bulunamadı</h3>
          <p className="text-sm text-gray-400 max-w-sm">
            {filterType
              ? 'Seçili filtreleme kriterlerine uygun işlem bulunamadı.'
              : 'Henüz kayıtlı bir işlem bulunmuyor.'}
          </p>
          {(filterType !== null || selectedCurrency !== 'TL') && (
            <button
              type="button"
              onClick={() => {
                setFilterType(null);
                setSelectedCurrency('TL');
              }}
              className="mt-5 px-4 py-2 text-xs font-semibold rounded-lg bg-white/10 hover:bg-white/20 text-white transition-colors"
            >
              Filtreleri Temizle
            </button>
          )}
        </div>
      ) : (
        /* Grouped Transaction List */
        <div className="space-y-6 mt-2">
          {groupedTransactions.map(([dateString, items]) => (
            <div key={dateString} className="relative">
              {/* Sticky Date Header */}
              <div className="sticky top-0 z-10 py-2.5 px-3 mb-2 rounded-xl backdrop-blur-md bg-deepNight/90 border border-white/5 flex items-center justify-between shadow-sm">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-neonGreen/80" />
                  <span className="text-xs font-semibold uppercase tracking-wider text-gray-300">
                    {dateString}
                  </span>
                </div>
                <span className="text-xs text-gray-400 font-medium">
                  {items.length} işlem
                </span>
              </div>

              {/* Transactions in this date group */}
              <div className="space-y-2.5">
                {items.map((tx) => {
                  const Icon = getCategoryIcon(tx.category, tx.customCategoryIcon);
                  const label = getCategoryLabel(tx.category, tx.customCategoryName);
                  const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
                  const categoryConfig = CATEGORIES[tx.category];
                  const accentColor = categoryConfig?.color || (isIncome ? '#00E676' : '#FF5252');
                  const isDeleting = deletingId === tx.id;

                  return (
                    <div
                      key={tx.id}
                      className={`group relative rounded-2xl p-4 transition-all duration-200 glass-card glass-card-hover border border-white/10 flex items-center justify-between gap-3 ${
                        isDeleting ? 'opacity-40 pointer-events-none scale-95' : ''
                      }`}
                    >
                      {/* Left: Icon & Info */}
                      <div className="flex items-center gap-3.5 min-w-0 flex-1">
                        <div
                          className="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 transition-transform group-hover:scale-105"
                          style={{
                            backgroundColor: `${accentColor}18`,
                            border: `1px solid ${accentColor}35`,
                            color: accentColor,
                          }}
                        >
                          <Icon size={22} />
                        </div>

                        <div className="min-w-0 flex-1">
                          <h4 className="text-sm sm:text-base font-semibold text-white truncate group-hover:text-neonGreen transition-colors">
                            {tx.title || label}
                          </h4>
                          <div className="flex items-center gap-2 mt-0.5 text-xs text-gray-400">
                            <span className="truncate">{label}</span>
                            {tx.note && (
                              <>
                                <span>•</span>
                                <span className="truncate italic text-gray-400">
                                  {tx.note}
                                </span>
                              </>
                            )}
                          </div>
                        </div>
                      </div>

                      {/* Right: Amount & Delete Button */}
                      <div className="flex items-center gap-3 flex-shrink-0">
                        <div className="text-right">
                          <span
                            className={`text-sm sm:text-base font-bold tracking-tight block ${
                              isIncome ? 'text-incomeGreen' : 'text-expenseRed'
                            }`}
                          >
                            {isIncome ? '+' : '-'}
                            {formatCurrency(tx.amount, tx.currency || selectedCurrency)}
                          </span>
                        </div>

                        {/* Delete Button (Appears on hover, red color) */}
                        <button
                          type="button"
                          onClick={(e) => handleDelete(tx.id, e)}
                          title="İşlemi Sil"
                          aria-label="İşlemi Sil"
                          className="opacity-0 group-hover:opacity-100 focus:opacity-100 transition-all duration-200 p-2 text-gray-400 hover:text-expenseRed hover:bg-expenseRed/15 rounded-xl border border-transparent hover:border-expenseRed/30"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
