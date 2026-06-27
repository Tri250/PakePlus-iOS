/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        hasselblad: {
          50: '#FFF3EB', 100: '#FFE0CC', 200: '#FFC199', 300: '#FFA266',
          400: '#FF8344', 500: '#FF6B2B', 600: '#E55A1B', 700: '#BF4513',
          800: '#99360F', 900: '#7A2B0E', 950: '#431507',
        },
        dark: {
          DEFAULT: '#0A0A0A', 50: '#252525', 100: '#1E1E1E',
          200: '#1A1A1A', 300: '#151515', 400: '#101010',
        },
      },
      fontFamily: {
        display: ['"DM Sans"', 'sans-serif'],
        body: ['"Noto Sans SC"', 'sans-serif'],
      },
    },
  },
  plugins: [],
}