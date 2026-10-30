import React, { useState, useMemo } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import {
  formatCurrency,
  getCategoryIcon,
  getCategoryLabel,
  CATEGORIES,
  isIncomeCategory,
} from '../constants';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import {
  TrendingUp,
  TrendingDown,
  PieChart as PieChartIcon,
  ArrowUpRight,
  ArrowDownLeft,
  Wallet,
} from 'lucide-react';

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

export default function StatsPage() {
  const { transactions, loading } = useTransactions();

  // State
  const [selectedCurrency, setSelectedCurrency] = useState('TL');
  const [dateFilter, setDateFilter] = useState('1 Ay'); // 'Tümü', '1 Hafta', '1 Ay', '1 Yıl'

  // Date filters list
  const dateFilterOptions = ['Tümü', '1 Hafta', '1 Ay', '1 Yıl'];

  const cutoff = useMemo(() => {
    const now = Date.now();
    if (dateFilter === '1 Hafta') return now - 7 * 24 * 60 * 60 * 1000;
    if (dateFilter === '1 Ay') return now - 30 * 24 * 60 * 60 * 1000;
    if (dateFilter === '1 Yıl') return now - 365 * 24 * 60 * 60 * 1000;
    return 0;
  }, [dateFilter]);

  // Filter transactions by date range and selected currency
  const filteredTransactions = useMemo(() => {
    return transactions.filter((tx) => {
      const txDate = tx.date || 0;
      // Date cutoff check
      if (cutoff > 0 && txDate < cutoff) {
        return false;
      }
      // Currency check
      if (selectedCurrency) {
        if (!isMatchingCurrency(tx.currency, selectedCurrency)) {
          return false;
        }
      }
      return true;
    });
  }, [transactions, cutoff, selectedCurrency]);

  // Income, Expense, Delta computations
  const { totalIncome, totalExpense, delta } = useMemo(() => {
    let income = 0;
    let expense = 0;

    filteredTransactions.forEach((tx) => {
      const amount = Number(tx.amount) || 0;
      const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
      if (isIncome) {
        income += amount;
      } else {
        expense += amount;
      }
    });

    return {
      totalIncome: income,
      totalExpense: expense,
      delta: income - expense,
    };
  }, [filteredTransactions]);

  // Daily Chart Data Generator
  const generateChartData = (type) => {
    let startDate, endDate;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    endDate = today.getTime();

    if (dateFilter === '1 Hafta') {
      startDate = endDate - 6 * 86400000;
    } else if (dateFilter === '1 Ay') {
      startDate = endDate - 29 * 86400000;
    } else if (dateFilter === '1 Yıl') {
      // Sample over 12 months for 1 Year to prevent 365 points overcrowding
      startDate = endDate - 364 * 86400000;
    } else {
      // 'Tümü'
      const currencyTx = transactions.filter(tx => isMatchingCurrency(tx.currency, selectedCurrency));
      if (currencyTx.length > 0) {
        const earliest = Math.min(...currencyTx.map(tx => tx.date || 0));
        startDate = new Date(earliest).setHours(0, 0, 0, 0);
      } else {
        startDate = endDate - 6 * 86400000; // Default to 1 week if no data
      }
    }

    const daysMap = new Map();
    let current = startDate;
    while (current <= endDate) {
      const d = new Date(current);
      let dateKey;
      if (dateFilter === '1 Yıl') {
          dateKey = d.toLocaleDateString('tr-TR', { month: 'short', year: '2-digit' });
      } else {
          dateKey = d.toLocaleDateString('tr-TR', { day: '2-digit', month: 'short' });
      }
      if (!daysMap.has(dateKey)) {
        daysMap.set(dateKey, 0);
      }
      current += 86400000; // Next day
    }

    if (type === 'ASSET') {
      // Initial balance before startDate
      let initialBalance = 0;
      transactions.forEach((tx) => {
        if (isMatchingCurrency(tx.currency, selectedCurrency) && (tx.date || 0) < startDate) {
          const amount = Number(tx.amount) || 0;
          const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
          if (isIncome) initialBalance += amount;
          else initialBalance -= amount;
        }
      });

      // Group daily changes within range
      const dailyChanges = new Map();
      transactions.forEach((tx) => {
        if (isMatchingCurrency(tx.currency, selectedCurrency)) {
          const txDate = tx.date || 0;
          if (txDate >= startDate && txDate <= endDate + 86400000 - 1) {
            const d = new Date(txDate);
            const dateKey = dateFilter === '1 Yıl' 
                ? d.toLocaleDateString('tr-TR', { month: 'short', year: '2-digit' }) 
                : d.toLocaleDateString('tr-TR', { day: '2-digit', month: 'short' });
            const amount = Number(tx.amount) || 0;
            const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
            const change = isIncome ? amount : -amount;
            dailyChanges.set(dateKey, (dailyChanges.get(dateKey) || 0) + change);
          }
        }
      });

      let runningBalance = initialBalance;
      const result = [];
      daysMap.forEach((_, dateKey) => {
        runningBalance += (dailyChanges.get(dateKey) || 0);
        result.push({ date: dateKey, amount: Math.round(runningBalance * 100) / 100 });
      });
      return result;

    } else {
      const typeTx = transactions.filter(tx => {
        if (!isMatchingCurrency(tx.currency, selectedCurrency)) return false;
        const txDate = tx.date || 0;
        if (txDate < startDate || txDate > endDate + 86400000 - 1) return false;
        
        const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
        return type === 'INCOME' ? isIncome : !isIncome;
      });

      typeTx.forEach((tx) => {
        const txDate = new Date(tx.date || Date.now());
        const dateKey = dateFilter === '1 Yıl' 
            ? txDate.toLocaleDateString('tr-TR', { month: 'short', year: '2-digit' }) 
            : txDate.toLocaleDateString('tr-TR', { day: '2-digit', month: 'short' });
        if (daysMap.has(dateKey)) {
          daysMap.set(dateKey, daysMap.get(dateKey) + (Number(tx.amount) || 0));
        }
      });

      const result = [];
      daysMap.forEach((amount, dateKey) => {
         result.push({ date: dateKey, amount: Math.round(amount * 100) / 100 });
      });
      return result;
    }
  };

  const expenseChartData = useMemo(() => generateChartData('EXPENSE'), [transactions, dateFilter, selectedCurrency]);
  const incomeChartData = useMemo(() => generateChartData('INCOME'), [transactions, dateFilter, selectedCurrency]);
  const assetChartData = useMemo(() => generateChartData('ASSET'), [transactions, dateFilter, selectedCurrency]);


  // Category Breakdown for expenses
  const expenseBreakdown = useMemo(() => {
    const categoryTotals = {};
    filteredTransactions
      .filter((tx) => tx.type === 'EXPENSE' || (!tx.type && !isIncomeCategory(tx.category)))
      .forEach((tx) => {
        const catKey = tx.category || 'OTHER_EXPENSE';
        const amt = Number(tx.amount) || 0;
        if (!categoryTotals[catKey]) {
          categoryTotals[catKey] = { category: catKey, customName: tx.customCategoryName, customIcon: tx.customCategoryIcon, total: 0 };
        }
        categoryTotals[catKey].total += amt;
      });
    const items = Object.values(categoryTotals).sort((a, b) => b.total - a.total);
    return items.map((item) => ({ ...item, percentage: parseFloat((totalExpense > 0 ? ((item.total / totalExpense) * 100).toFixed(1) : '0')) }));
  }, [filteredTransactions, totalExpense]);

  // Category Breakdown for incomes
  const incomeBreakdown = useMemo(() => {
    const categoryTotals = {};
    filteredTransactions
      .filter((tx) => tx.type === 'INCOME' || (!tx.type && isIncomeCategory(tx.category)))
      .forEach((tx) => {
        const catKey = tx.category || 'OTHER_INCOME';
        const amt = Number(tx.amount) || 0;
        if (!categoryTotals[catKey]) {
          categoryTotals[catKey] = { category: catKey, customName: tx.customCategoryName, customIcon: tx.customCategoryIcon, total: 0 };
        }
        categoryTotals[catKey].total += amt;
      });
    const items = Object.values(categoryTotals).sort((a, b) => b.total - a.total);
    return items.map((item) => ({ ...item, percentage: parseFloat((totalIncome > 0 ? ((item.total / totalIncome) * 100).toFixed(1) : '0')) }));
  }, [filteredTransactions, totalIncome]);


  // Custom Recharts Tooltip
  const CustomTooltip = ({ active, payload, label, color }) => {
    if (active && payload && payload.length) {
      return (
        <div className="rounded-xl bg-cardDark/95 border border-white/15 px-3.5 py-2 shadow-2xl backdrop-blur-md">
          <p className="text-xs text-gray-400 font-medium mb-0.5">{label}</p>
          <p className="text-sm font-bold" style={{ color }}>
            {formatCurrency(payload[0].value, selectedCurrency)}
          </p>
        </div>
      );
    }
    return null;
  };

  const renderChart = (title, subtitle, icon, data, dataKey, color, loading) => {
    return (
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl flex-1 min-w-[300px]">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ backgroundColor: `${color}26`, color: color }}>
              {icon}
            </div>
            <div>
              <h3 className="text-base font-bold text-white">{title}</h3>
              <p className="text-xs text-gray-400">{subtitle}</p>
            </div>
          </div>
          <span className="text-xs text-gray-400 bg-white/5 px-2.5 py-1 rounded-lg border border-white/10">
            {data.length} Veri Noktası
          </span>
        </div>

        {loading ? (
          <div className="h-64 flex items-center justify-center">
            <div className="w-8 h-8 border-3 border-neonGreen/20 border-t-neonGreen rounded-full animate-spin" />
          </div>
        ) : data.length === 0 || data.every((d) => d.amount === 0) ? (
          <div className="h-64 flex flex-col items-center justify-center text-center p-6">
            <span className="text-3xl mb-2">📉</span>
            <p className="text-sm text-gray-400">Bu dönemde kayıt bulunmuyor.</p>
          </div>
        ) : (
          <div className="w-full h-64 pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255, 255, 255, 0.07)" vertical={false} />
                <XAxis dataKey="date" stroke="#9CA3AF" fontSize={11} tickLine={false} axisLine={{ stroke: 'rgba(255, 255, 255, 0.1)' }} />
                <YAxis stroke="#9CA3AF" fontSize={11} tickLine={false} axisLine={{ stroke: 'rgba(255, 255, 255, 0.1)' }} tickFormatter={(val) => (val >= 1000 ? `${(val / 1000).toFixed(0)}k` : val)} />
                <Tooltip content={<CustomTooltip color={color} />} />
                <Line type="monotone" dataKey="amount" stroke={color} strokeWidth={2.5} dot={{ fill: color, r: 3, strokeWidth: 0 }} activeDot={{ fill: color, r: 6, stroke: '#FFFFFF', strokeWidth: 2 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    );
  };

  const renderBreakdown = (title, subtitle, data, typeColor) => {
    return (
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl flex-1 min-w-[300px]">
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2.5">
            <div className={`w-8 h-8 rounded-lg flex items-center justify-center`} style={{ backgroundColor: `${typeColor}26`, color: typeColor }}>
              <PieChartIcon size={18} />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">{title}</h3>
              <p className="text-xs text-gray-400">{subtitle}</p>
            </div>
          </div>
          <span className="text-xs font-semibold text-gray-300">
            {data.length} Kategori
          </span>
        </div>

        {data.length === 0 ? (
          <div className="py-8 text-center text-gray-400 text-sm">
            Bu dönemde kayıt bulunmuyor.
          </div>
        ) : (
          <div className="space-y-4">
            {data.map((item) => {
              const Icon = getCategoryIcon(item.category, item.customIcon);
              const label = getCategoryLabel(item.category, item.customName);
              const categoryConfig = CATEGORIES[item.category];
              const color = typeColor; // Default to type color, could use category color if needed

              return (
                <div key={item.category} className="space-y-1.5">
                  <div className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div
                        className="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0"
                        style={{ backgroundColor: `${color}20`, color: color }}
                      >
                        <Icon size={15} />
                      </div>
                      <span className="font-medium text-gray-200 truncate">{label}</span>
                    </div>
                    <div className="flex items-center gap-3 flex-shrink-0">
                      <span className="text-xs font-semibold text-gray-400">
                        %{item.percentage}
                      </span>
                      <span className="font-bold text-white text-sm">
                        {formatCurrency(item.total, selectedCurrency)}
                      </span>
                    </div>
                  </div>
                  <div className="w-full bg-white/5 h-2 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-500"
                      style={{ width: `${Math.max(item.percentage, 1.5)}%`, backgroundColor: color }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  };


  return (
    <div className="min-h-screen bg-deepNight text-gray-100 pb-24 pt-6 px-4 sm:px-6 lg:px-8 max-w-5xl mx-auto space-y-6">
      {/* Header & Currency Selector */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-white/10">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-white">
            İstatistikler
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-gray-400">
            Gelir ve gider eğilimlerinizi, kategori dağılımınızı inceleyin
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

      {/* Quick Date Filter Chips */}
      <div className="flex items-center gap-2 sm:gap-3 overflow-x-auto no-scrollbar py-1">
        {dateFilterOptions.map((option) => (
          <button
            key={option}
            type="button"
            onClick={() => setDateFilter(option)}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-all whitespace-nowrap ${
              dateFilter === option
                ? 'bg-neonGreen text-black font-bold shadow-lg shadow-neonGreen/20 scale-102'
                : 'bg-cardDark/80 text-gray-400 hover:text-white border border-white/10 hover:border-white/20'
            }`}
          >
            {option}
          </button>
        ))}
      </div>

      {/* Net Status Card (Glass Card) */}
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl relative overflow-hidden">
        {/* Subtle background glow */}
        <div
          className={`absolute -right-16 -top-16 w-48 h-48 rounded-full blur-3xl pointer-events-none opacity-20 ${
            delta >= 0 ? 'bg-incomeGreen' : 'bg-expenseRed'
          }`}
        />

        <div className="relative z-10">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs sm:text-sm font-semibold uppercase tracking-wider text-gray-400">
              Net Durum (Delta)
            </span>
            <span className="text-xs px-2.5 py-1 rounded-full bg-white/5 border border-white/10 text-gray-300">
              {dateFilter}
            </span>
          </div>

          {/* Big Delta Number */}
          <div className="my-3">
            <div
              className={`text-3xl sm:text-5xl font-black tracking-tight ${
                delta >= 0 ? 'text-incomeGreen' : 'text-expenseRed'
              }`}
            >
              {delta >= 0 ? '+' : ''}
              {formatCurrency(delta, selectedCurrency)}
            </div>
            <p className="text-xs text-gray-400 mt-1">
              {delta >= 0 ? 'Net kâr / birikim durumu' : 'Geliri aşan net harcama'}
            </p>
          </div>

          {/* Income and Expense Side by Side */}
          <div className="grid grid-cols-2 gap-4 pt-4 mt-4 border-t border-white/10">
            {/* Total Income */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-incomeGreen/15 border border-incomeGreen/30 flex items-center justify-center text-incomeGreen flex-shrink-0">
                <ArrowDownLeft size={20} />
              </div>
              <div className="min-w-0">
                <p className="text-xs text-gray-400 font-medium truncate">Toplam Gelir</p>
                <p className="text-sm sm:text-lg font-bold text-incomeGreen truncate">
                  +{formatCurrency(totalIncome, selectedCurrency)}
                </p>
              </div>
            </div>

            {/* Total Expense */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-expenseRed/15 border border-expenseRed/30 flex items-center justify-center text-expenseRed flex-shrink-0">
                <ArrowUpRight size={20} />
              </div>
              <div className="min-w-0">
                <p className="text-xs text-gray-400 font-medium truncate">Toplam Gider</p>
                <p className="text-sm sm:text-lg font-bold text-expenseRed truncate">
                  -{formatCurrency(totalExpense, selectedCurrency)}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 3 Charts Grid */}
      <div className="flex flex-col xl:flex-row gap-6">
        <div className="flex flex-col gap-6 xl:w-2/3">
           {renderChart('Varlık Trendi', 'Mevcut Para Eğilimi', <Wallet size={18} />, assetChartData, 'amount', '#4F9CF9', loading)}
           {renderChart('Gider Trendi', 'Günlük harcama değişimi', <TrendingDown size={18} />, expenseChartData, 'amount', '#FF5252', loading)}
           {renderChart('Gelir Trendi', 'Günlük gelir değişimi', <TrendingUp size={18} />, incomeChartData, 'amount', '#00E676', loading)}
        </div>
        
        {/* Category Breakdowns */}
        <div className="flex flex-col gap-6 xl:w-1/3">
          {renderBreakdown('Gider Dağılımı', 'Giderlerin kategorilere göre yüzdeleri', expenseBreakdown, '#FF5252')}
          {renderBreakdown('Gelir Dağılımı', 'Gelirlerin kategorilere göre yüzdeleri', incomeBreakdown, '#00E676')}
        </div>
      </div>

      {/* Filtered Transaction List at the Bottom */}
      <div className="glass-card rounded-2xl p-6 border border-white/10 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-white">Dönem İşlemleri</h3>
          <span className="text-xs text-gray-400">
            {filteredTransactions.length} kayıt listeleniyor
          </span>
        </div>

        {filteredTransactions.length === 0 ? (
          <div className="py-8 text-center text-gray-400 text-sm">
            Bu dönem için filtrelenmiş işlem bulunamadı.
          </div>
        ) : (
          <div className="divide-y divide-white/5 max-h-96 overflow-y-auto pr-1">
            {filteredTransactions.map((tx) => {
              const Icon = getCategoryIcon(tx.category, tx.customCategoryIcon);
              const label = getCategoryLabel(tx.category, tx.customCategoryName);
              const isIncome = tx.type === 'INCOME' || isIncomeCategory(tx.category);
              const dateStr = new Date(tx.date || Date.now()).toLocaleDateString('tr-TR', {
                day: '2-digit',
                month: 'short',
                year: 'numeric',
              });

              return (
                <div
                  key={tx.id}
                  className="py-3 flex items-center justify-between gap-3 hover:bg-white/5 px-2 rounded-xl transition-colors"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 ${
                        isIncome
                          ? 'bg-incomeGreen/15 text-incomeGreen'
                          : 'bg-expenseRed/15 text-expenseRed'
                      }`}
                    >
                      <Icon size={18} />
                    </div>
                    <div className="min-w-0">
                      <p className="text-sm font-semibold text-white truncate">
                        {tx.title || label}
                      </p>
                      <p className="text-xs text-gray-400 flex items-center gap-2">
                        <span>{label}</span>
                        <span>•</span>
                        <span>{dateStr}</span>
                      </p>
                    </div>
                  </div>

                  <span
                    className={`text-sm font-bold flex-shrink-0 ${
                      isIncome ? 'text-incomeGreen' : 'text-expenseRed'
                    }`}
                  >
                    {isIncome ? '+' : '-'}
                    {formatCurrency(tx.amount, tx.currency || selectedCurrency)}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
