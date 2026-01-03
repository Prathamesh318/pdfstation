import { Link } from 'react-router-dom';
import { FileText, Merge, Zap, Scissors } from 'lucide-react';

export default function HomePage() {
    return (
        <div className="min-h-screen">
            {/* Header */}
            <header className="bg-white/80 backdrop-blur-sm shadow-sm sticky top-0 z-10">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                            <FileText className="w-8 h-8 text-primary-600" />
                            <h1 className="text-2xl font-bold text-gray-900">PDFStation</h1>
                        </div>
                        <nav className="flex space-x-6">
                            <Link to="/compress" className="text-gray-700 hover:text-primary-600 transition">
                                Compress
                            </Link>
                            <Link to="/merge" className="text-gray-700 hover:text-primary-600 transition">
                                Merge
                            </Link>
                            <Link to="/split" className="text-gray-700 hover:text-primary-600 transition">
                                Split
                            </Link>
                        </nav>
                    </div>
                </div>
            </header>

            {/* Hero Section */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
                <div className="text-center animate-fade-in">
                    <h2 className="text-5xl font-extrabold text-gray-900 mb-4">
                        Professional PDF Tools
                    </h2>
                    <p className="text-xl text-gray-600 mb-12">
                        Compress, merge, split, and optimize your PDFs with industry-standard quality
                    </p>

                    {/* Feature Cards */}
                    <div className="grid md:grid-cols-3 gap-8 mt-16">
                        {/* Compress Card */}
                        <Link
                            to="/compress"
                            className="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2"
                        >
                            <div className="absolute top-0 right-0 w-32 h-32 bg-blue-100 rounded-bl-full opacity-50 group-hover:opacity-75 transition"></div>
                            <div className="relative z-10">
                                <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-xl mb-6">
                                    <Zap className="w-8 h-8 text-white" />
                                </div>
                                <h3 className="text-2xl font-bold text-gray-900 mb-3">Compress PDF</h3>
                                <p className="text-gray-600 mb-4">
                                    Reduce file size by up to 95% while maintaining quality. Choose from multiple compression levels.
                                </p>
                                <div className="flex items-center text-blue-600 font-semibold group-hover:translate-x-2 transition-transform">
                                    Start Compressing
                                    <svg className="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </div>
                            </div>
                        </Link>

                        {/* Merge Card */}
                        <Link
                            to="/merge"
                            className="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2"
                        >
                            <div className="absolute top-0 right-0 w-32 h-32 bg-indigo-100 rounded-bl-full opacity-50 group-hover:opacity-75 transition"></div>
                            <div className="relative z-10">
                                <div className="inline-flex items-center justify-center w-16 h-16 bg-indigo-600 rounded-xl mb-6">
                                    <Merge className="w-8 h-8 text-white" />
                                </div>
                                <h3 className="text-2xl font-bold text-gray-900 mb-3">Merge PDFs</h3>
                                <p className="text-gray-600 mb-4">
                                    Combine multiple PDF files into one. Reorder pages and create a single unified document.
                                </p>
                                <div className="flex items-center text-indigo-600 font-semibold group-hover:translate-x-2 transition-transform">
                                    Start Merging
                                    <svg className="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </div>
                            </div>
                        </Link>

                        {/* Split Card */}
                        <Link
                            to="/split"
                            className="group relative bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2"
                        >
                            <div className="absolute top-0 right-0 w-32 h-32 bg-green-100 rounded-bl-full opacity-50 group-hover:opacity-75 transition"></div>
                            <div className="relative z-10">
                                <div className="inline-flex items-center justify-center w-16 h-16 bg-green-600 rounded-xl mb-6">
                                    <Scissors className="w-8 h-8 text-white" />
                                </div>
                                <h3 className="text-2xl font-bold text-gray-900 mb-3">Split PDF</h3>
                                <p className="text-gray-600 mb-4">
                                    Extract specific pages or split into multiple documents. Download as a ZIP file.
                                </p>
                                <div className="flex items-center text-green-600 font-semibold group-hover:translate-x-2 transition-transform">
                                    Start Splitting
                                    <svg className="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </div>
                            </div>
                        </Link>
                    </div>

                    {/* Features */}
                    <div className="mt-24">
                        <h3 className="text-3xl font-bold text-gray-900 mb-12">Why PDFStation?</h3>
                        <div className="grid md:grid-cols-3 gap-8">
                            <div className="text-center">
                                <div className="inline-flex items-center justify-center w-12 h-12 bg-green-100 rounded-full mb-4">
                                    <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                    </svg>
                                </div>
                                <h4 className="font-semibold text-gray-900 mb-2">Fast Processing</h4>
                                <p className="text-gray-600">Real-time job processing with instant feedback</p>
                            </div>
                            <div className="text-center">
                                <div className="inline-flex items-center justify-center w-12 h-12 bg-purple-100 rounded-full mb-4">
                                    <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                    </svg>
                                </div>
                                <h4 className="font-semibold text-gray-900 mb-2">Secure</h4>
                                <p className="text-gray-600">Your files are processed securely and deleted after download</p>
                            </div>
                            <div className="text-center">
                                <div className="inline-flex items-center justify-center w-12 h-12 bg-orange-100 rounded-full mb-4">
                                    <svg className="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
                                    </svg>
                                </div>
                                <h4 className="font-semibold text-gray-900 mb-2">Quality First</h4>
                                <p className="text-gray-600">Industry-standard compression maintains document quality</p>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            {/* Footer */}
            <footer className="bg-white/80 backdrop-blur-sm mt-24 py-8">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-gray-600">
                    <p>&copy; 2026 Prathamesh Veer. Built with ❤️ for the developer community.</p>
                </div>
            </footer>
        </div>
    );
}
