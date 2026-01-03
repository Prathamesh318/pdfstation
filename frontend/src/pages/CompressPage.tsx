import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FileText, Upload, Download, Loader2, ArrowLeft } from 'lucide-react';
import { apiClient } from '../api/client';
import type { CreateJobResponse } from '../types/api';

export default function CompressPage() {
    const navigate = useNavigate();
    const [file, setFile] = useState<File | null>(null);
    const [quality, setQuality] = useState(50);
    const [isDragging, setIsDragging] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [jobId, setJobId] = useState<string | null>(null);
    const [status, setStatus] = useState<string>('');
    const [error, setError] = useState<string>('');

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
        const droppedFile = e.dataTransfer.files[0];
        if (droppedFile && droppedFile.type === 'application/pdf') {
            setFile(droppedFile);
            setError('');
        } else {
            setError('Please upload a PDF file');
        }
    };

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFile = e.target.files?.[0];
        if (selectedFile && selectedFile.type === 'application/pdf') {
            setFile(selectedFile);
            setError('');
        } else {
            setError('Please select a PDF file');
        }
    };

    const handleCompress = async () => {
        if (!file) return;

        setIsProcessing(true);
        setError('');
        setStatus('Uploading...');

        try {
            const response: CreateJobResponse = await apiClient.compressPdf(file, quality);
            setJobId(response.id);
            setStatus('Processing...');

            // Poll for status
            const pollInterval = setInterval(async () => {
                try {
                    const jobStatus = await apiClient.getJobStatus(response.id);
                    setStatus(jobStatus.status);

                    if (jobStatus.status === 'COMPLETED') {
                        clearInterval(pollInterval);
                        setIsProcessing(false);
                    } else if (jobStatus.status === 'FAILED') {
                        clearInterval(pollInterval);
                        setIsProcessing(false);
                        setError('Compression failed. Please try again.');
                    }
                } catch (err) {
                    console.error('Error polling status:', err);
                }
            }, 2000);
        } catch (err) {
            setIsProcessing(false);
            setError('Failed to compress PDF. Please try again.');
            console.error(err);
        }
    };

    const handleDownload = () => {
        if (jobId) {
            window.location.href = apiClient.getDownloadUrl(jobId);
        }
    };

    const resetUpload = () => {
        setFile(null);
        setJobId(null);
        setStatus('');
        setError('');
        setIsProcessing(false);
    };

    const formatBytes = (bytes: number) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    return (
        <div className="min-h-screen">
            {/* Header */}
            <header className="bg-white/80 backdrop-blur-sm shadow-sm sticky top-0 z-10">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center justify-between">
                        <Link to="/" className="flex items-center space-x-2">
                            <FileText className="w-8 h-8 text-blue-600" />
                            <h1 className="text-2xl font-bold text-gray-900">PDFStation</h1>
                        </Link>
                        <Link to="/" className="flex items-center text-gray-700 hover:text-blue-600 transition">
                            <ArrowLeft className="w-5 h-5 mr-2" />
                            Back
                        </Link>
                    </div>
                </div>
            </header>

            <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                <div className="text-center mb-12">
                    <h2 className="text-4xl font-bold text-white mb-4">Compress PDF</h2>
                    <p className="text-lg text-gray-200">
                        Reduce file size while maintaining quality
                    </p>
                </div>

                {/* Upload Section */}
                {!file && !jobId && (
                    <div
                        className={`border-3 border-dashed rounded-2xl p-12 text-center transition-all ${isDragging
                            ? 'border-blue-500 bg-blue-50'
                            : 'border-gray-300 hover:border-blue-400 bg-white'
                            }`}
                        onDragOver={(e) => {
                            e.preventDefault();
                            setIsDragging(true);
                        }}
                        onDragLeave={() => setIsDragging(false)}
                        onDrop={handleDrop}
                    >
                        <Upload className="w-16 h-16 mx-auto mb-4 text-gray-400" />
                        <h3 className="text-xl font-semibold text-gray-900 mb-2">
                            Drop your PDF here
                        </h3>
                        <p className="text-gray-600 mb-6">or</p>
                        <label className="inline-block cursor-pointer">
                            <input
                                type="file"
                                accept="application/pdf"
                                onChange={handleFileSelect}
                                className="hidden"
                            />
                            <span className="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition inline-block">
                                Browse Files
                            </span>
                        </label>
                        <p className="text-sm text-gray-500 mt-4">Maximum file size: 20MB</p>
                        {error && <p className="text-red-600 mt-4">{error}</p>}
                    </div>
                )}

                {/* File Selected */}
                {file && !jobId && (
                    <div className="bg-white rounded-2xl shadow-lg p-8 animate-slide-up">
                        <div className="flex items-center justify-between mb-8 pb-6 border-b">
                            <div className="flex items-center space-x-4">
                                <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                                    <FileText className="w-6 h-6 text-red-600" />
                                </div>
                                <div>
                                    <h3 className="font-semibold text-gray-900">{file.name}</h3>
                                    <p className="text-sm text-gray-600">{formatBytes(file.size)}</p>
                                </div>
                            </div>
                            <button
                                onClick={resetUpload}
                                className="text-gray-500 hover:text-red-600 transition"
                            >
                                Remove
                            </button>
                        </div>

                        {/* Quality Slider */}
                        <div className="mb-8">
                            <label className="block text-sm font-semibold text-gray-900 mb-4">
                                Compression Quality: {quality}%
                            </label>
                            <input
                                type="range"
                                min="5"
                                max="95"
                                value={quality}
                                onChange={(e) => setQuality(Number(e.target.value))}
                                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
                            />
                            <div className="flex justify-between text-xs text-gray-600 mt-2">
                                <span>Maximum Compression</span>
                                <span>Best Quality</span>
                            </div>
                            <div className="mt-4 p-4 bg-blue-50 rounded-lg">
                                <p className="text-sm text-blue-900">
                                    💡 <strong>Recommended:</strong> 50% for balanced compression, 75% for high quality
                                </p>
                            </div>
                        </div>

                        <button
                            onClick={handleCompress}
                            disabled={isProcessing}
                            className="w-full py-4 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                        >
                            {isProcessing ? (
                                <>
                                    <Loader2 className="w-5 h-5 animate-spin" />
                                    <span>{status}</span>
                                </>
                            ) : (
                                <span>Compress PDF</span>
                            )}
                        </button>
                    </div>
                )}

                {/* Processing/Complete */}
                {jobId && (
                    <div className="bg-white rounded-2xl shadow-lg p-8 animate-slide-up">
                        {status === 'COMPLETED' ? (
                            <div className="text-center">
                                <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                    <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                    </svg>
                                </div>
                                <h3 className="text-2xl font-bold text-gray-900 mb-2">Compression Complete!</h3>
                                <p className="text-gray-600 mb-8">Your PDF has been successfully compressed</p>

                                <div className="flex gap-4 justify-center">
                                    <button
                                        onClick={handleDownload}
                                        className="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition flex items-center space-x-2"
                                    >
                                        <Download className="w-5 h-5" />
                                        <span>Download</span>
                                    </button>
                                    <button
                                        onClick={resetUpload}
                                        className="px-6 py-3 bg-gray-200 text-gray-900 rounded-lg font-semibold hover:bg-gray-300 transition"
                                    >
                                        Compress Another
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="text-center">
                                <Loader2 className="w-16 h-16 text-blue-600 animate-spin mx-auto mb-4" />
                                <h3 className="text-xl font-semibold text-gray-900 mb-2">Processing...</h3>
                                <p className="text-gray-600">This may take a few moments</p>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
}
