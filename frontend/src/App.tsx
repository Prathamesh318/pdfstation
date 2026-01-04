import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import CompressPage from './pages/CompressPage';
import MergePage from './pages/MergePage';
import SplitPage from './pages/SplitPage';
import ProtectPage from './pages/ProtectPage';
import RemoveProtectionPage from './pages/RemoveProtectionPage';
import PdfToWordPage from './pages/PdfToWordPage';
import './index.css';

import { ThemeProvider } from './context/ThemeContext';

function App() {
  return (
    <ThemeProvider>
      <Router>
        <div className="min-h-screen bg-gray-50 dark:bg-slate-950 transition-colors duration-300">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/compress" element={<CompressPage />} />
            <Route path="/merge" element={<MergePage />} />
            <Route path="/split" element={<SplitPage />} />
            <Route path="/protect" element={<ProtectPage />} />
            <Route path="/remove-protection" element={<RemoveProtectionPage />} />
            <Route path="/pdf-to-word" element={<PdfToWordPage />} />
          </Routes>
        </div>
      </Router>
    </ThemeProvider>
  );
}

export default App;
