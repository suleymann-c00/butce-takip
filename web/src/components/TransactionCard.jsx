import React from 'react';
import {
  getCategoryIcon,
  getCategoryLabel,
  formatCurrency,
  isIncomeCategory,
  formatDateTurkish,
} from '../constants';

/**
 * TransactionCard Component
 * Displays an individual transaction with category icon, title, category label,
 * date, and formatted +/- amount colored based on transaction type.
 *
 * @param {object} props
 * @param {object} props.transaction - The transaction object
 * @param {string} [props.currency] - Default fallback currency code
 * @param {Function} [props.onClick] - Optional click handler
 * @param {Function} [props.onDelete] - Optional delete handler
 */
export default function TransactionCard({
  transaction,
  currency = 'TL',
  onClick,
  onDelete,
}) {
  if (!transaction) return null;

  const isIncome =
    transaction.type === 'INCOME' || isIncomeCategory(transaction.category);
  const Icon = getCategoryIcon(
    transaction.category,
    transaction.customCategoryIcon
  );
  const categoryLabel = getCategoryLabel(
    transaction.category,
    transaction.customCategoryName
  );
  const txCurrency = transaction.currency || currency || 'TL';
  const amountFormatted = formatCurrency(
    Math.abs(transaction.amount || 0),
    txCurrency
  );

  return (
    <div
      onClick={onClick}
      className={`group relative bg-cardDark/60 border border-white/10 rounded-xl p-3.5 sm:p-4 flex items-center justify-between gap-3 sm:gap-4 backdrop-blur-md hover:bg-cardDark/80 hover:border-white/20 transition-all duration-200 ${
        onClick ? 'cursor-pointer' : ''
      }`}
    >
      {/* Left: Category Icon Box */}
      <div className="flex items-center gap-3 sm:gap-3.5 min-w-0 flex-1">
        <div
          className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 border transition-transform duration-200 group-hover:scale-105 ${
            isIncome
              ? 'bg-incomeGreen/10 border-incomeGreen/20 text-incomeGreen'
              : 'bg-expenseRed/10 border-expenseRed/20 text-expenseRed'
          }`}
        >
          <Icon className="w-5 h-5" />
        </div>

        {/* Middle: Title & Category Label */}
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <h4 className="text-sm sm:text-base font-semibold text-white truncate">
              {transaction.title || 'İsimsiz İşlem'}
            </h4>
            {transaction.isRecurring && (
              <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 font-medium border border-indigo-500/30 shrink-0">
                Düzenli
              </span>
            )}
          </div>

          <div className="flex items-center gap-2 text-xs text-gray-400 mt-0.5">
            <span className="truncate">{categoryLabel}</span>
            {transaction.date && (
              <>
                <span className="w-1 h-1 rounded-full bg-gray-500 shrink-0" />
                <span className="shrink-0 text-gray-400">
                  {formatDateTurkish(transaction.date)}
                </span>
              </>
            )}
          </div>

          {transaction.note && (
            <p className="text-[11px] text-gray-500 mt-0.5 truncate max-w-sm">
              {transaction.note}
            </p>
          )}
        </div>
      </div>

      {/* Right: Amount with +/- prefix */}
      <div className="text-right shrink-0">
        <div
          className={`text-sm sm:text-base font-bold tracking-tight font-mono ${
            isIncome ? 'text-incomeGreen' : 'text-expenseRed'
          }`}
        >
          {isIncome ? '+' : '-'}
          {amountFormatted}
        </div>
      </div>
    </div>
  );
}
