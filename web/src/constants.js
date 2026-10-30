import {
  Wallet,
  Laptop,
  TrendingUp,
  GraduationCap,
  Coins,
  PlusCircle,
  Utensils,
  Car,
  Home,
  ShoppingCart,
  Receipt,
  RefreshCw,
  TrendingDown,
  HeartPulse,
  Film,
  BookOpen,
  MoreHorizontal,
  Sparkles,
  Star,
  Heart,
  Wrench,
  Luggage,
  PawPrint,
  Plane,
  HelpCircle,
} from 'lucide-react';

/**
 * Transaction Types
 */
export const TRANSACTION_TYPES = {
  INCOME: 'INCOME',
  EXPENSE: 'EXPENSE',
};

/**
 * Income Category Keys
 */
export const INCOME_CATEGORIES = [
  'SALARY',
  'FREELANCE',
  'INVESTMENT_INCOME',
  'SCHOLARSHIP',
  'POCKET_MONEY',
  'OTHER_INCOME',
];

/**
 * Expense Category Keys
 */
export const EXPENSE_CATEGORIES = [
  'FOOD',
  'TRANSPORT',
  'RENT',
  'SHOPPING',
  'BILLS',
  'SUBSCRIPTION',
  'INVESTMENT_EXPENSE',
  'HEALTH',
  'ENTERTAINMENT',
  'EDUCATION',
  'OTHER_EXPENSE',
  'CUSTOM',
];

/**
 * Custom category icon mapping (matches Android CategoryIconMapper.kt)
 */
export const CUSTOM_ICONS = {
  Star: Star,
  Favorite: Heart,
  ShoppingCart: ShoppingCart,
  Home: Home,
  Build: Wrench,
  CardTravel: Luggage,
  Pets: PawPrint,
  Flight: Plane,
};

/**
 * Categories configuration with Turkish labels, icons and types
 */
export const CATEGORIES = {
  // --- Income Categories ---
  SALARY: {
    key: 'SALARY',
    label: 'Maaş',
    type: 'INCOME',
    icon: Wallet,
    iconName: 'Wallet',
    color: '#00E676',
  },
  FREELANCE: {
    key: 'FREELANCE',
    label: 'Serbest Çalışma',
    type: 'INCOME',
    icon: Laptop,
    iconName: 'Laptop',
    color: '#00E676',
  },
  INVESTMENT_INCOME: {
    key: 'INVESTMENT_INCOME',
    label: 'Yatırım Getirisi',
    type: 'INCOME',
    icon: TrendingUp,
    iconName: 'TrendingUp',
    color: '#00E676',
  },
  SCHOLARSHIP: {
    key: 'SCHOLARSHIP',
    label: 'Burs',
    type: 'INCOME',
    icon: GraduationCap,
    iconName: 'GraduationCap',
    color: '#00E676',
  },
  POCKET_MONEY: {
    key: 'POCKET_MONEY',
    label: 'Harçlık',
    type: 'INCOME',
    icon: Coins,
    iconName: 'Coins',
    color: '#00E676',
  },
  OTHER_INCOME: {
    key: 'OTHER_INCOME',
    label: 'Diğer Gelir',
    type: 'INCOME',
    icon: PlusCircle,
    iconName: 'PlusCircle',
    color: '#00E676',
  },

  // --- Expense Categories ---
  FOOD: {
    key: 'FOOD',
    label: 'Yiyecek & İçecek',
    type: 'EXPENSE',
    icon: Utensils,
    iconName: 'Utensils',
    color: '#FF5252',
  },
  TRANSPORT: {
    key: 'TRANSPORT',
    label: 'Ulaşım',
    type: 'EXPENSE',
    icon: Car,
    iconName: 'Car',
    color: '#FF5252',
  },
  RENT: {
    key: 'RENT',
    label: 'Kira',
    type: 'EXPENSE',
    icon: Home,
    iconName: 'Home',
    color: '#FF5252',
  },
  SHOPPING: {
    key: 'SHOPPING',
    label: 'Alışveriş',
    type: 'EXPENSE',
    icon: ShoppingCart,
    iconName: 'ShoppingCart',
    color: '#FF5252',
  },
  BILLS: {
    key: 'BILLS',
    label: 'Faturalar',
    type: 'EXPENSE',
    icon: Receipt,
    iconName: 'Receipt',
    color: '#FF5252',
  },
  SUBSCRIPTION: {
    key: 'SUBSCRIPTION',
    label: 'Abonelik',
    type: 'EXPENSE',
    icon: RefreshCw,
    iconName: 'RefreshCw',
    color: '#FF5252',
  },
  INVESTMENT_EXPENSE: {
    key: 'INVESTMENT_EXPENSE',
    label: 'Yatırım',
    type: 'EXPENSE',
    icon: TrendingDown,
    iconName: 'TrendingDown',
    color: '#FF5252',
  },
  HEALTH: {
    key: 'HEALTH',
    label: 'Sağlık',
    type: 'EXPENSE',
    icon: HeartPulse,
    iconName: 'HeartPulse',
    color: '#FF5252',
  },
  ENTERTAINMENT: {
    key: 'ENTERTAINMENT',
    label: 'Eğlence',
    type: 'EXPENSE',
    icon: Film,
    iconName: 'Film',
    color: '#FF5252',
  },
  EDUCATION: {
    key: 'EDUCATION',
    label: 'Eğitim',
    type: 'EXPENSE',
    icon: BookOpen,
    iconName: 'BookOpen',
    color: '#FF5252',
  },
  OTHER_EXPENSE: {
    key: 'OTHER_EXPENSE',
    label: 'Diğer Gider',
    type: 'EXPENSE',
    icon: MoreHorizontal,
    iconName: 'MoreHorizontal',
    color: '#FF5252',
  },
  CUSTOM: {
    key: 'CUSTOM',
    label: 'Özel Kategori',
    type: 'EXPENSE',
    icon: Sparkles,
    iconName: 'Sparkles',
    color: '#FFB300',
  },
};

/**
 * Currency Definitions (TL, USD, EUR, GOLD)
 */
export const CURRENCIES = {
  TL: {
    code: 'TRY',
    symbol: '₺',
    label: 'Türk Lirası (₺)',
    name: 'Türk Lirası',
  },
  USD: {
    code: 'USD',
    symbol: '$',
    label: 'Amerikan Doları ($)',
    name: 'Amerikan Doları',
  },
  EUR: {
    code: 'EUR',
    symbol: '€',
    label: 'Euro (€)',
    name: 'Euro',
  },
  GOLD: {
    code: 'GOLD',
    symbol: 'gr',
    label: 'Gram Altın (gr)',
    name: 'Altın',
  },
};

/**
 * Check if a category is an income category
 * @param {string} category
 * @returns {boolean}
 */
export function isIncomeCategory(category) {
  return INCOME_CATEGORIES.includes(category);
}

/**
 * Format currency amount according to Turkish conventions and currency rules
 * TRY uses ₺, USD uses $, EUR uses €, GOLD uses "gr" suffix
 * @param {number|string} amount
 * @param {string} currency - 'TL', 'USD', 'EUR', 'GOLD' or 'TRY'
 * @returns {string} Formatted currency string
 */
export function formatCurrency(amount = 0, currency = 'TL') {
  const num = typeof amount === 'number' ? amount : parseFloat(amount) || 0;
  const formatted = num.toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  const currKey = (typeof currency === 'string' ? currency.toUpperCase() : 'TL');
  const info = CURRENCIES[currKey] || (currKey === 'TRY' ? CURRENCIES.TL : CURRENCIES.TL);

  if (currKey === 'GOLD') {
    return `${formatted} ${info.symbol}`;
  }
  return `${info.symbol}${formatted}`;
}

/**
 * Get display label for a category
 * @param {string} category - Category key
 * @param {string} [customCategoryName] - Optional custom name if category is CUSTOM
 * @returns {string} Turkish label
 */
export function getCategoryLabel(category, customCategoryName = null) {
  if (category === 'CUSTOM' && customCategoryName) {
    return customCategoryName;
  }
  return CATEGORIES[category]?.label || category || 'Diğer';
}

/**
 * Get icon component for a category
 * @param {string} category - Category key
 * @param {string} [customCategoryIcon] - Optional custom icon key (e.g. 'Star', 'Pets')
 * @returns {React.ComponentType} Lucide icon component
 */
export function getCategoryIcon(category, customCategoryIcon = null) {
  if (category === 'CUSTOM' && customCategoryIcon && CUSTOM_ICONS[customCategoryIcon]) {
    return CUSTOM_ICONS[customCategoryIcon];
  }
  if (customCategoryIcon && CUSTOM_ICONS[customCategoryIcon]) {
    return CUSTOM_ICONS[customCategoryIcon];
  }
  return CATEGORIES[category]?.icon || HelpCircle;
}

/**
 * Month names in Turkish
 */
export const TURKISH_MONTHS = [
  'Ocak',
  'Şubat',
  'Mart',
  'Nisan',
  'Mayıs',
  'Haziran',
  'Temmuz',
  'Ağustos',
  'Eylül',
  'Ekim',
  'Kasım',
  'Aralık',
];

/**
 * Format timestamp into Turkish readable date (e.g. 5 Eylül 2026)
 * @param {number|Date} dateInput
 * @returns {string}
 */
export function formatDateTurkish(dateInput) {
  if (!dateInput) return '';
  const date = typeof dateInput === 'number' ? new Date(dateInput) : new Date(dateInput);
  if (isNaN(date.getTime())) return '';
  const day = date.getDate();
  const month = TURKISH_MONTHS[date.getMonth()];
  const year = date.getFullYear();
  return `${day} ${month} ${year}`;
}

export default {
  TRANSACTION_TYPES,
  INCOME_CATEGORIES,
  EXPENSE_CATEGORIES,
  CUSTOM_ICONS,
  CATEGORIES,
  CURRENCIES,
  isIncomeCategory,
  formatCurrency,
  getCategoryLabel,
  getCategoryIcon,
  TURKISH_MONTHS,
  formatDateTurkish,
};
