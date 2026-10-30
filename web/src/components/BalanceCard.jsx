import React from 'react';
import { formatCurrency } from '../constants';
import { Wallet, TrendingUp, TrendingDown } from 'lucide-react';

/**
 * BalanceCard Component
 * Large card with gradient background displaying total assets for the selected currency,
 * net period change (delta) with visual status indicator, and secondary currency balances.
 *
 * @param {object} props
 * @param {object} props.balances - Balance amounts { tl, usd, eur, gold }
 * @param {string} props.selectedCurrency - Currently selected currency ('TL', 'TRY', 'USD', 'EUR', 'GOLD')
 * @param {number} props.delta - Net change in period (Income - Expense)
 * @param {string} [props.dateFilterLabel] - Label of the active date filter (e.g. 'Aylık', 'Bugün')
 */
export default function BalanceCard({
  balances = {},
  selectedCurrency = 'TL',
  delta = 0,
  dateFilterLabel = 'Aylık',
}) {
  // Helper to extract balance value
  const getBalance = (currKey) => {
    if (!balances) return 0;
    const k = (currKey || '').toLowerCase();
    if (k === 'tl' || k === 'try') {
      return balances.tl ?? balances.try ?? 0;
    }
    return balances[k] ?? 0;
  };

  const isPositiveDelta = (delta || 0) >= 0;
  const currentBalance = getBalance(selectedCurrency);
  const formattedMainBalance = formatCurrency(currentBalance, selectedCurrency);
  const formattedDelta = formatCurrency(Math.abs(delta || 0), selectedCurrency);

  // List of all tracked currencies
  const allCurrencies = [
    { key: 'TL', code: 'TRY', label: 'TRY', symbol: '₺' },
    { key: 'USD', code: 'USD', label: 'USD', symbol: '$' },
    { key: 'EUR', code: 'EUR', label: 'EUR', symbol: '€' },
    { key: 'GOLD', code: 'GOLD', label: 'Altın', symbol: 'gr' },
  ];

  // Exclude current selected currency from secondary list
  const isSelected = (currencyKey) => {
    const sel = (selectedCurrency || 'TL').toUpperCase();
    const cur = currencyKey.toUpperCase();
    if (sel === 'TL' || sel === 'TRY') {
      return cur === 'TL' || cur === 'TRY';
    }
    return sel === cur;
  };

  const secondaryCurrencies = allCurrencies.filter((c) => !isSelected(c.key));

  return (
    <div className="relative overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-neonGreen/10 via-cardDark/80 to-cardDark p-6 sm:p-8 backdrop-blur-xl shadow-2xl">
      {/* Ambient background glow */}
      <div className="pointer-events-none absolute -right-16 -top-16 h-48 w-48 rounded-full bg-neonGreen/15 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-16 -left-16 h-48 w-48 rounded-full bg-neonGreen/5 blur-3xl" />

      {/* Top row: Label & Date Filter Indicator */}
      <div className="relative flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-neonGreen/15 text-neonGreen border border-neonGreen/20 shadow-sm">
            <Wallet className="h-5 w-5" />
          </div>
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-400">
              Toplam Varlıklar
            </span>
          </div>
        </div>

        {dateFilterLabel && (
          <span className="inline-flex items-center rounded-full bg-white/5 px-3 py-1 text-xs font-medium text-gray-300 border border-white/10 backdrop-blur-md">
            {dateFilterLabel}
          </span>
        )}
      </div>

      {/* Main big balance number */}
      <div className="relative mt-5 sm:mt-6">
        <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-white font-mono drop-shadow-sm transition-all duration-300">
          {formattedMainBalance}
        </h2>
      </div>

      {/* Net Delta Badge */}
      <div className="relative mt-4 flex items-center gap-2">
        <div
          className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs sm:text-sm font-semibold border backdrop-blur-md transition-colors ${
            isPositiveDelta
              ? 'bg-incomeGreen/10 border-incomeGreen/30 text-incomeGreen'
              : 'bg-expenseRed/10 border-expenseRed/30 text-expenseRed'
          }`}
        >
          {isPositiveDelta ? (
            <TrendingUp className="h-4 w-4" />
          ) : (
            <TrendingDown className="h-4 w-4" />
          )}
          <span>
            {isPositiveDelta ? '+' : '-'}
            {formattedDelta}
          </span>
        </div>
        <span className="text-xs text-gray-400">Dönem Net Değişimi</span>
      </div>

      {/* Secondary Currencies Row */}
      <div className="relative mt-6 pt-5 border-t border-white/10">
        <div className="text-[11px] font-medium uppercase tracking-wider text-gray-400 mb-2.5">
          Diğer Varlıklar
        </div>
        <div className="grid grid-cols-3 gap-2.5 sm:gap-3">
          {secondaryCurrencies.map((item) => {
            const val = getBalance(item.key);
            return (
              <div
                key={item.key}
                className="group rounded-2xl bg-white/5 border border-white/5 p-2.5 sm:p-3 transition-all hover:bg-white/10 hover:border-white/15"
              >
                <div className="text-[10px] sm:text-xs font-medium text-gray-400">
                  {item.label} ({item.symbol})
                </div>
                <div className="mt-0.5 text-xs sm:text-sm font-bold text-white truncate font-mono">
                  {formatCurrency(val, item.key)}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
