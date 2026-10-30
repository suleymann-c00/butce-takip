/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        deepNight: '#0A0A0F',
        neonGreen: '#00E676',
        cardDark: '#1A1A2E',
        glassBg: 'rgba(30, 30, 36, 0.6)',
        glassBorder: 'rgba(255, 255, 255, 0.1)',
        incomeGreen: '#00E676',
        expenseRed: '#FF5252',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      keyframes: {
        wave: {
          '0%': { transform: 'translateX(0) translateZ(0) scaleY(1)' },
          '50%': { transform: 'translateX(-25%) translateZ(0) scaleY(0.8)' },
          '100%': { transform: 'translateX(-50%) translateZ(0) scaleY(1)' },
        },
        pulseGlow: {
          '0%, 100%': { opacity: '0.4' },
          '50%': { opacity: '0.8' },
        },
      },
      animation: {
        wave: 'wave 15s cubic-bezier(0.36, 0.45, 0.63, 0.53) infinite',
        pulseGlow: 'pulseGlow 3s ease-in-out infinite',
      },
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}
