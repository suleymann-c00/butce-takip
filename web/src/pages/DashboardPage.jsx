import React, { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import useTransactions from '../hooks/useTransactions';
import useAuth from '../hooks/useAuth';
import constants, {
  formatCurrency,
  getCategoryLabel,
  getCategoryIcon,
  isIncomeCategory,
  formatDateTurkish,
} from '../constants';
import BalanceCard from '../components/BalanceCard';
import TransactionCard from '../components/TransactionCard';
import AddTransactionModal from '../components/AddTransactionModal';
import {
  TrendingUp,
  TrendingDown,
  Plus,
  ArrowRight,
  PieChart,
  Calendar,
  Sparkles,
  Loader2,
  AlertCircle,
} from 'lucide-react';

/**
 * Currency pills definition
 */
const CURRENCY_PILLS = [
  { key: 'TL', label: 'TRY', symbol: '₺' },
  { key: 'USD', label: 'USD', symbol: '$' },
  { key: 'EUR', label: 'EUR', symbol: '€' },
  { key: 'GOLD', label: 'GOLD', symbol: 'gr' },
];

/**
 * Date filter chips definition
 */
const DATE_FILTERS = [
  { key: 'DAILY', label: 'Günlük', shortLabel: 'Bugün' },
  { key: 'WEEKLY', label: 'Haftalık', shortLabel: 'Haftalık' },
  { key: 'MONTHLY', label: 'Aylık', shortLabel: 'Aylık' },
  { key: 'ALL', label: 'Tümü', shortLabel: 'Tüm Zamanlar' },
];

/**
 * Helper to match currency values safely
 */
function isMatchingCurrency(txCurrency, targetCurrency) {
  const t = (txCurrency || 'TL').toUpperCase();
  const target = (targetCurrency || 'TL').toUpperCase();
  if (target === 'TL' || target === 'TRY') {
    return t === 'TL' || t === 'TRY';
  }
  return t === target;
}

/**
 * Helper to check date filter match
 */
function isDateInFilter(timestamp, filter) {
  if (filter === 'ALL') return true;
  if (!timestamp) return true;

  const txDate = new Date(timestamp);
  const now = new Date();

  if (filter === 'DAILY') {
    const dayAgo = new Date();
    dayAgo.setDate(now.getDate() - 1);
    return timestamp >= dayAgo.getTime();
  }

  if (filter === 'WEEKLY') {
    const weekAgo = new Date();
    weekAgo.setDate(now.getDate() - 7);
    return timestamp >= weekAgo.getTime();
  }

  if (filter === 'MONTHLY') {
    const monthAgo = new Date();
    monthAgo.setMonth(now.getMonth() - 1);
    return timestamp >= monthAgo.getTime();
  }

  return true;
}

/**
 * Main Dashboard Page
 */
export default function DashboardPage() {
  const { user } = useAuth();
  const { transactions, loading, error, addTransaction } = useTransactions();

  // State
  const [selectedCurrency, setSelectedCurrency] = useState('TL');
  const [dateFilter, setDateFilter] = useState('MONTHLY');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  // User Greeting
  const userGreetingName =
    user?.displayName || user?.email?.split('@')[0] || '';
  const greeting = userGreetingName
    ? `Hoş Geldiniz, ${userGreetingName}`
    : 'Hoş Geldiniz';

  // Active date filter object
  const activeDateFilterObj =
    DATE_FILTERS.find((df) => df.key === dateFilter) || DATE_FILTERS[2];

  // 1. Compute cumulative balances across all currencies
  const balances = useMemo(() => {
    const totals = { tl: 0, usd: 0, eur: 0, gold: 0 };
    if (!transactions || !Array.isArray(transactions)) return totals;

    transactions.forEach((tx) => {
      const rawAmt =
        typeof tx.amount === 'number' ? tx.amount : parseFloat(tx.amount) || 0;
      const isIncome =
        tx.type === 'INCOME' || isIncomeCategory(tx.category);
      const net = isIncome ? rawAmt : -rawAmt;

      const curr = (tx.currency || 'TL').toUpperCase();
      if (curr === 'TL' || curr === 'TRY') {
        totals.tl += net;
      } else if (curr === 'USD') {
        totals.usd += net;
      } else if (curr === 'EUR') {
        totals.eur += net;
      } else if (curr === 'GOLD') {
        totals.gold += net;
      }
    });

    return totals;
  }, [transactions]);

  // 2. Filter transactions by selected date filter AND selected currency
  const filteredPeriodTransactions = useMemo(() => {
    if (!transactions || !Array.isArray(transactions)) return [];
    return transactions.filter((tx) => {
      const matchCurr = isMatchingCurrency(tx.currency, selectedCurrency);
      const matchDate = isDateInFilter(tx.date, dateFilter);
      return matchCurr && matchDate;
    });
  }, [transactions, selectedCurrency, dateFilter]);

  // 3. Compute income, expense, and delta for filtered transactions
  const { periodIncome, periodExpense, periodDelta } = useMemo(() => {
    let income = 0;
    let expense = 0;

    filteredPeriodTransactions.forEach((tx) => {
      const rawAmt =
        typeof tx.amount === 'number' ? tx.amount : parseFloat(tx.amount) || 0;
      const isIncome =
        tx.type === 'INCOME' || isIncomeCategory(tx.category);
      if (isIncome) {
        income += rawAmt;
      } else {
        expense += rawAmt;
      }
    });

    return {
      periodIncome: income,
      periodExpense: expense,
      periodDelta: income - expense,
    };
  }, [filteredPeriodTransactions]);

  // 4. Compute Top Categories (max 4 with percentages)
  const { topExpenses, topIncomes } = useMemo(() => {
    const expenseGroups = {};
    const incomeGroups = {};
    let totalExpenses = 0;
    let totalIncomes = 0;

    filteredPeriodTransactions.forEach((tx) => {
      const rawAmt = typeof tx.amount === 'number' ? tx.amount : parseFloat(tx.amount) || 0;
      const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
      const catKey = tx.category || (isIncome ? 'OTHER_INCOME' : 'OTHER_EXPENSE');
      
      const item = {
        categoryKey: catKey,
        label: getCategoryLabel(catKey, tx.customCategoryName),
        icon: getCategoryIcon(catKey, tx.customCategoryIcon),
        total: 0,
      };

      if (isIncome) {
        if (!incomeGroups[catKey]) incomeGroups[catKey] = { ...item };
        incomeGroups[catKey].total += rawAmt;
        totalIncomes += rawAmt;
      } else {
        if (!expenseGroups[catKey]) expenseGroups[catKey] = { ...item };
        expenseGroups[catKey].total += rawAmt;
        totalExpenses += rawAmt;
      }
    });

    const processGroup = (group, total) => {
      return Object.values(group)
        .sort((a, b) => b.total - a.total)
        .slice(0, 4)
        .map(item => ({
          ...item,
          percentage: total > 0 ? Math.round((item.total / total) * 100) : 0,
          formattedAmount: formatCurrency(item.total, selectedCurrency),
        }));
    };

    return {
      topExpenses: processGroup(expenseGroups, totalExpenses),
      topIncomes: processGroup(incomeGroups, totalIncomes)
    };
  }, [filteredPeriodTransactions, selectedCurrency]);

  // 5. Last 5 transactions
  const recentTransactions = useMemo(() => {
    if (!transactions || !Array.isArray(transactions)) return [];
    return [...transactions]
      .sort((a, b) => (b.date || 0) - (a.date || 0))
      .slice(0, 5);
  }, [transactions]);

  // Is a specific currency pill active?
  const isPillActive = (pillKey) => {
    const sel = (selectedCurrency || 'TL').toUpperCase();
    const pill = pillKey.toUpperCase();
    if (sel === 'TL' || sel === 'TRY') {
      return pill === 'TL' || pill === 'TRY';
    }
    return sel === pill;
  };

  return (
    <div className="min-h-screen bg-deepNight text-gray-100 pb-24 pt-6 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-6 sm:space-y-8">
        {/* Top Header & Greeting */}
        <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              {greeting}
            </h1>
            <p className="text-xs sm:text-sm text-gray-400 mt-0.5">
              Finansal durumunuzun genel özeti ve son hareketler
            </p>
          </div>

          {/* Currency Selector Pills */}
          <div className="inline-flex items-center gap-1 p-1 rounded-2xl bg-cardDark/90 border border-white/10 backdrop-blur-md self-start sm:self-auto shadow-lg">
            {CURRENCY_PILLS.map((pill) => {
              const active = isPillActive(pill.key);
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
        </header>

        {/* Date Filter Chips */}
        <div className="flex items-center justify-between gap-2 overflow-x-auto pb-1 scrollbar-none">
          <div className="flex items-center gap-2">
            {DATE_FILTERS.map((chip) => {
              const isSelected = dateFilter === chip.key;
              return (
                <button
                  key={chip.key}
                  type="button"
                  onClick={() => setDateFilter(chip.key)}
                  className={`px-4 py-1.5 rounded-xl text-xs sm:text-sm font-semibold transition-all duration-200 border cursor-pointer ${
                    isSelected
                      ? 'bg-white/15 border-white/25 text-white shadow-sm backdrop-blur-md'
                      : 'bg-cardDark/40 border-white/5 text-gray-400 hover:text-gray-200 hover:bg-white/5'
                  }`}
                >
                  {chip.label}
                </button>
              );
            })}
          </div>

          <span className="hidden sm:inline-flex text-xs text-gray-500 items-center gap-1">
            <Calendar className="w-3.5 h-3.5" />
            {formatDateTurkish(Date.now())}
          </span>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="rounded-2xl border border-expenseRed/30 bg-expenseRed/10 p-4 text-sm text-expenseRed flex items-center gap-3">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Balance Card Component */}
        <BalanceCard
          balances={balances}
          selectedCurrency={selectedCurrency}
          delta={periodDelta}
          dateFilterLabel={activeDateFilterObj.shortLabel}
        />

        {/* Income / Expense Summary Cards */}
        <section className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Income Card */}
          <div className="relative overflow-hidden rounded-2xl border border-incomeGreen/20 bg-cardDark/70 p-5 sm:p-6 backdrop-blur-md shadow-lg group hover:border-incomeGreen/40 transition-all duration-200">
            <div className="pointer-events-none absolute -right-8 -top-8 h-28 w-28 rounded-full bg-incomeGreen/10 blur-2xl" />
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                Toplam Gelir
              </span>
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-incomeGreen/10 text-incomeGreen border border-incomeGreen/20">
                <TrendingUp className="h-5 w-5" />
              </div>
            </div>
            <div className="mt-4">
              <span className="text-2xl sm:text-3xl font-extrabold text-incomeGreen font-mono tracking-tight">
                {formatCurrency(periodIncome, selectedCurrency)}
              </span>
            </div>
            <div className="mt-2 text-xs text-gray-500">
              {activeDateFilterObj.shortLabel} dönemi toplamı
            </div>
          </div>

          {/* Expense Card */}
          <div className="relative overflow-hidden rounded-2xl border border-expenseRed/20 bg-cardDark/70 p-5 sm:p-6 backdrop-blur-md shadow-lg group hover:border-expenseRed/40 transition-all duration-200">
            <div className="pointer-events-none absolute -right-8 -top-8 h-28 w-28 rounded-full bg-expenseRed/10 blur-2xl" />
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-400">
                Toplam Gider
              </span>
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-expenseRed/10 text-expenseRed border border-expenseRed/20">
                <TrendingDown className="h-5 w-5" />
              </div>
            </div>
            <div className="mt-4">
              <span className="text-2xl sm:text-3xl font-extrabold text-expenseRed font-mono tracking-tight">
                {formatCurrency(periodExpense, selectedCurrency)}
              </span>
            </div>
            <div className="mt-2 text-xs text-gray-500">
              {activeDateFilterObj.shortLabel} dönemi toplamı
            </div>
          </div>
        </section>

        {/* Category Breakdown Progress Bars */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Expenses Breakdown */}
          <section className="rounded-3xl border border-white/10 bg-cardDark/60 p-5 sm:p-7 backdrop-blur-md shadow-lg space-y-4">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-xl bg-expenseRed/10 text-expenseRed border border-expenseRed/20">
                  <TrendingDown className="w-4 h-4" />
                </div>
                <h3 className="text-base sm:text-lg font-bold text-white">
                  Gider Dağılımı
                </h3>
              </div>
            </div>

            {topExpenses.length === 0 ? (
              <div className="text-center py-8 text-gray-400 text-sm">
                Bu dönemde henüz gider kaydı bulunmuyor.
              </div>
            ) : (
              <div className="space-y-4 pt-2">
                {topExpenses.map((item) => {
                  const CatIcon = item.icon;
                  return (
                    <div key={item.categoryKey} className="space-y-1.5">
                      <div className="flex items-center justify-between text-xs sm:text-sm">
                        <div className="flex items-center gap-2">
                          <div className="w-6 h-6 rounded-lg bg-white/5 flex items-center justify-center text-gray-300">
                            <CatIcon className="w-3.5 h-3.5" />
                          </div>
                          <span className="font-medium text-gray-200">
                            {item.label}
                          </span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-semibold text-white font-mono">
                            {item.formattedAmount}
                          </span>
                          <span className="text-xs font-bold text-expenseRed w-10 text-right font-mono">
                            %{item.percentage}
                          </span>
                        </div>
                      </div>
                      <div className="w-full h-2 rounded-full bg-white/10 overflow-hidden">
                        <div
                          className="h-full rounded-full bg-expenseRed transition-all duration-500"
                          style={{ width: `${Math.min(item.percentage, 100)}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {/* Incomes Breakdown */}
          <section className="rounded-3xl border border-white/10 bg-cardDark/60 p-5 sm:p-7 backdrop-blur-md shadow-lg space-y-4">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-xl bg-incomeGreen/10 text-incomeGreen border border-incomeGreen/20">
                  <TrendingUp className="w-4 h-4" />
                </div>
                <h3 className="text-base sm:text-lg font-bold text-white">
                  Gelir Dağılımı
                </h3>
              </div>
            </div>

            {topIncomes.length === 0 ? (
              <div className="text-center py-8 text-gray-400 text-sm">
                Bu dönemde henüz gelir kaydı bulunmuyor.
              </div>
            ) : (
              <div className="space-y-4 pt-2">
                {topIncomes.map((item) => {
                  const CatIcon = item.icon;
                  return (
                    <div key={item.categoryKey} className="space-y-1.5">
                      <div className="flex items-center justify-between text-xs sm:text-sm">
                        <div className="flex items-center gap-2">
                          <div className="w-6 h-6 rounded-lg bg-white/5 flex items-center justify-center text-gray-300">
                            <CatIcon className="w-3.5 h-3.5" />
                          </div>
                          <span className="font-medium text-gray-200">
                            {item.label}
                          </span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-semibold text-white font-mono">
                            {item.formattedAmount}
                          </span>
                          <span className="text-xs font-bold text-incomeGreen w-10 text-right font-mono">
                            %{item.percentage}
                          </span>
                        </div>
                      </div>
                      <div className="w-full h-2 rounded-full bg-white/10 overflow-hidden">
                        <div
                          className="h-full rounded-full bg-incomeGreen transition-all duration-500"
                          style={{ width: `${Math.min(item.percentage, 100)}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        </div>

        {/* Recent Transactions List */}
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base sm:text-lg font-bold text-white">
              Son İşlemler
            </h3>
            <Link
              to="/transactions"
              className="inline-flex items-center gap-1 text-xs sm:text-sm font-semibold text-neonGreen hover:text-neonGreen/80 transition-colors"
            >
              <span>Tümünü Gör</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12 text-gray-400 gap-2">
              <Loader2 className="w-5 h-5 animate-spin text-neonGreen" />
              <span className="text-sm">İşlemler yükleniyor...</span>
            </div>
          ) : recentTransactions.length === 0 ? (
            <div className="rounded-2xl border border-white/10 bg-cardDark/40 p-8 text-center backdrop-blur-md">
              <div className="mx-auto w-12 h-12 rounded-2xl bg-white/5 flex items-center justify-center text-gray-400 mb-3">
                <Sparkles className="w-6 h-6" />
              </div>
              <p className="text-sm font-medium text-gray-300">
                Henüz işlem kaydı bulunmuyor.
              </p>
              <p className="text-xs text-gray-500 mt-1">
                İlk gelirinizi veya giderinizi ekleyerek başlayabilirsiniz.
              </p>
              <button
                type="button"
                onClick={() => setIsAddModalOpen(true)}
                className="mt-4 inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold bg-neonGreen text-deepNight hover:bg-neonGreen/90 transition-all"
              >
                <Plus className="w-4 h-4" />
                İşlem Ekle
              </button>
            </div>
          ) : (
            <div className="space-y-2.5">
              {recentTransactions.map((tx) => (
                <TransactionCard
                  key={tx.id || `${tx.date}_${tx.title}`}
                  transaction={tx}
                  currency={selectedCurrency}
                />
              ))}
            </div>
          )}
        </section>
      </div>

      {/* Floating Add Button */}
      <button
        type="button"
        onClick={() => setIsAddModalOpen(true)}
        className="fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-neonGreen text-deepNight shadow-2xl shadow-neonGreen/40 hover:bg-neonGreen/90 hover:scale-110 active:scale-95 transition-all duration-200 cursor-pointer focus:outline-none focus:ring-4 focus:ring-neonGreen/30"
        title="Yeni İşlem Ekle"
        aria-label="Yeni İşlem Ekle"
      >
        <Plus className="h-7 w-7 stroke-[2.5]" />
      </button>

      {/* Add Transaction Modal */}
      <AddTransactionModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        addTransaction={addTransaction}
      />
    </div>
  );
}
