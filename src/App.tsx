import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import MobileShowcase from '@/pages/MobileShowcase'
import ToastContainer from '@/components/ui/ToastContainer'

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-dark font-body text-white">
        <Routes>
          <Route path="/" element={<MobileShowcase />} />
        </Routes>
        <ToastContainer />
      </div>
    </Router>
  )
}