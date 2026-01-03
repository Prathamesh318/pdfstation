import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import CompressPage from './pages/CompressPage';
import MergePage from './pages/MergePage';
import SplitPage from './pages/SplitPage';
import ProtectPage from './pages/ProtectPage';
import RemoveProtectionPage from './pages/RemoveProtectionPage';
import './index.css';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/compress" element={<CompressPage />} />
          <Route path="/merge" element={<MergePage />} />
          <Route path="/split" element={<SplitPage />} />
          <Route path="/protect" element={<ProtectPage />} />
          <Route path="/remove-protection" element={<RemoveProtectionPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
