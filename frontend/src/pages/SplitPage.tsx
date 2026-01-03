import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FileText, Upload, Download, Loader2, ArrowLeft, Scissors } from 'lucide-react';
import { apiClient } from '../api/client';
import type { CreateJobResponse } from '../types/api';

export default function SplitPage() {
    const [file, setFile] = useState<File | null>(null);
    const [splitType, setSplitType] = useState<'pages' | 'interval' | 'all'>('pages');
    const [ranges, setRanges] = useState('');
    const [intervalPages, setIntervalPages] = useState(5);
    const [isDragging, setIsDragging] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [jobId, setJobId] = useState<string | null>(null);
    const [status, setStatus] = useState<string>('');
    const [error, setError] = useState<string>('');

    // Poll for job status when jobId is set
    useEffect(() => {
        if (!jobId || status === 'COMPLETED' || status === 'FAILED') {
            return;
        }

        console.log('🚀 Starting polling for job:', jobId);
        let pollCount = 0;
        const maxPolls = 60;

        const pollInterval = setInterval(async () => {
            pollCount++;
            console.log(`📡 Poll #${pollCount} for job ${jobId}`);

            try {
                const jobStatus = await apiClient.getJobStatus(jobId);
                console.log(`✅ Poll #${pollCount} - Status: ${jobStatus.status}`);
                setStatus(jobStatus.status);

                if (jobStatus.status === 'COMPLETED') {
                    console.log('🎉 Job completed!');
                    setIsProcessing(false);
                } else if (jobStatus.status === 'FAILED') {
                    console.log('❌ Job failed!');
                    setIsProcessing(false);
                    setError('Split failed. Please try again.');
                } else if (pollCount >= maxPolls) {
                    console.log('⏱️ Max polls reached');
                    setIsProcessing(false);
                    setError('Processing timeout. Please try again.');
                }
            } catch (err) {
                console.error(`❌ Poll #${pollCount} error:`, err);
            }
        }, 2000);

        console.log('✅ Polling intervalPages created');

        return () => {
            console.log('🛑 Cleaning up polling intervalPages');
            clearInterval(pollInterval);
        };
    }, [jobId, status]);

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

    const validateInput = (): boolean => {
        if (splitType === 'pages') {
            if (!ranges.trim()) {
                setError('Please enter page ranges (e.g., 1-3,5,7-10)');
                return false;
            }
            // Basic validation for page ranges format
            const rangePattern = /^(\d+(-\d+)?)(,\d+(-\d+)?)*$/;
            if (!rangePattern.test(ranges.trim())) {
                setError('Invalid page range format. Use: 1-3,5,7-10');
                return false;
            }
        } else if (splitType === 'interval') {
            if (intervalPages < 1) {
                setError('Interval must be at least 1');
                return false;
            }
        }
        return true;
    };

    const handleSplit = async () => {
        if (!file) return;
        if (!validateInput()) return;
        if (isProcessing) {
            console.log('⚠️ Already processing, ignoring duplicate call');
            return; // Prevent double-submission
        }

        setIsProcessing(true);
        setError('');
        setStatus('Uploading...');

        try {
            const response: CreateJobResponse = await apiClient.splitPdf(
                file,
                splitType,
                splitType === 'pages' ? ranges : undefined,
                splitType === 'interval' ? intervalPages : undefined
            );
            setJobId(response.id);
            setStatus('Splitting...');
            console.log('✅ Job created:', response.id);
        } catch (err) {
            setIsProcessing(false);
            setError('Failed to split PDF. Please try again.');
            console.error(err);
        }
    };

    const handleDownload = () => {
        if (jobId) {
            window.location.href = apiClient.getSplitDownloadUrl(jobId);
        }
    };

    const resetUpload = () => {
        setFile(null);
        setJobId(null);
        setStatus('');
        setError('');
        setIsProcessing(false);
        setRanges('');
        setIntervalPages(5);
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
                    <h2 className="text-4xl font-bold text-white mb-4">Split PDF</h2>
                    <p className="text-lg text-gray-200">
                        Extract pages or split into multiple documents
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

                        {/* Split Mode Selection */}
                        <div className="mb-8">
                            <label className="block text-sm font-semibold text-gray-900 mb-4">
                                Split Mode
                            </label>
                            <div className="space-y-3">
                                <label className="flex items-center p-4 border-2 rounded-lg cursor-pointer hover:bg-gray-50 transition">
                                    <input
                                        type="radio"
                                        name="splitType"
                                        value="pages"
                                        checked={splitType === 'pages'}
                                        onChange={(e) => setSplitType(e.target.value as 'pages')}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <div className="ml-3">
                                        <div className="font-medium text-gray-900">Extract Specific Pages</div>
                                        <div className="text-sm text-gray-600">Extract selected page ranges (e.g., 1-3,5,7-10)</div>
                                    </div>
                                </label>

                                <label className="flex items-center p-4 border-2 rounded-lg cursor-pointer hover:bg-gray-50 transition">
                                    <input
                                        type="radio"
                                        name="splitType"
                                        value="interval"
                                        checked={splitType === 'interval'}
                                        onChange={(e) => setSplitType(e.target.value as 'interval')}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <div className="ml-3">
                                        <div className="font-medium text-gray-900">Split by intervalPages</div>
                                        <div className="text-sm text-gray-600">Split into chunks of N pages</div>
                                    </div>
                                </label>

                                <label className="flex items-center p-4 border-2 rounded-lg cursor-pointer hover:bg-gray-50 transition">
                                    <input
                                        type="radio"
                                        name="splitType"
                                        value="all"
                                        checked={splitType === 'all'}
                                        onChange={(e) => setSplitType(e.target.value as 'all')}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <div className="ml-3">
                                        <div className="font-medium text-gray-900">Split All Pages</div>
                                        <div className="text-sm text-gray-600">Create one PDF per page</div>
                                    </div>
                                </label>
                            </div>
                        </div>

                        {/* Page Ranges Input */}
                        {splitType === 'pages' && (
                            <div className="mb-8">
                                <label className="block text-sm font-semibold text-gray-900 mb-2">
                                    Page Ranges
                                </label>
                                <input
                                    type="text"
                                    value={ranges}
                                    onChange={(e) => setRanges(e.target.value)}
                                    placeholder="e.g., 1-3,5,7-10"
                                    className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none"
                                />
                                <p className="text-sm text-gray-600 mt-2">
                                    Enter page numbers and ranges separated by commas
                                </p>
                            </div>
                        )}

                        {/* Interval Input */}
                        {splitType === 'interval' && (
                            <div className="mb-8">
                                <label className="block text-sm font-semibold text-gray-900 mb-2">
                                    Pages per Split: {intervalPages}
                                </label>
                                <input
                                    type="range"
                                    min="1"
                                    max="20"
                                    value={intervalPages}
                                    onChange={(e) => setIntervalPages(Number(e.target.value))}
                                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
                                />
                                <div className="flex justify-between text-xs text-gray-600 mt-2">
                                    <span>1 page</span>
                                    <span>20 pages</span>
                                </div>
                            </div>
                        )}

                        {error && <p className="text-red-600 mb-4">{error}</p>}

                        <button
                            onClick={handleSplit}
                            disabled={isProcessing}
                            className="w-full py-4 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                        >
                            {isProcessing ? (
                                <>
                                    <Loader2 className="w-5 h-5 animate-spin" />
                                    <span>{status}</span>
                                </>
                            ) : (
                                <>
                                    <Scissors className="w-5 h-5" />
                                    <span>Split PDF</span>
                                </>
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
                                <h3 className="text-2xl font-bold text-gray-900 mb-2">Split Complete!</h3>
                                <p className="text-gray-600 mb-8">Your PDF has been successfully split</p>

                                <div className="flex gap-4 justify-center">
                                    <button
                                        onClick={handleDownload}
                                        className="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition flex items-center space-x-2"
                                    >
                                        <Download className="w-5 h-5" />
                                        <span>Download ZIP</span>
                                    </button>
                                    <button
                                        onClick={resetUpload}
                                        className="px-6 py-3 bg-gray-200 text-gray-900 rounded-lg font-semibold hover:bg-gray-300 transition"
                                    >
                                        Split Another
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="text-center">
                                <Loader2 className="w-16 h-16 text-blue-600 animate-spin mx-auto mb-4" />
                                <h3 className="text-xl font-semibold text-gray-900 mb-2">Processing...</h3>
                                <p className="text-gray-600">Splitting your PDF</p>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
}

