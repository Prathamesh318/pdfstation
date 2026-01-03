import { useState } from 'react';
import { Link } from 'react-router-dom';
import { FileText, Upload, Download, Loader2, ArrowLeft, X } from 'lucide-react';
import { apiClient } from '../api/client';
import type { CreateJobResponse } from '../types/api';

export default function MergePage() {
    const [files, setFiles] = useState<File[]>([]);
    const [isDragging, setIsDragging] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [jobId, setJobId] = useState<string | null>(null);
    const [status, setStatus] = useState<string>('');
    const [error, setError] = useState<string>('');

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
        const droppedFiles = Array.from(e.dataTransfer.files).filter(
            (file) => file.type === 'application/pdf'
        );
        if (droppedFiles.length > 0) {
            setFiles((prev) => [...prev, ...droppedFiles]);
            setError('');
        } else {
            setError('Please upload PDF files only');
        }
    };

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFiles = e.target.files;
        if (selectedFiles) {
            const pdfFiles = Array.from(selectedFiles).filter(
                (file) => file.type === 'application/pdf'
            );
            setFiles((prev) => [...prev, ...pdfFiles]);
            setError('');
        }
    };

    const removeFile = (index: number) => {
        setFiles((prev) => prev.filter((_, i) => i !== index));
    };

    const moveFile = (index: number, direction: 'up' | 'down') => {
        const newFiles = [...files];
        const targetIndex = direction === 'up' ? index - 1 : index + 1;
        if (targetIndex >= 0 && targetIndex < files.length) {
            [newFiles[index], newFiles[targetIndex]] = [newFiles[targetIndex], newFiles[index]];
            setFiles(newFiles);
        }
    };

    const handleMerge = async () => {
        if (files.length < 2) {
            setError('Please upload at least 2 PDF files');
            return;
        }

        setIsProcessing(true);
        setError('');
        setStatus('Uploading...');

        try {
            const response: CreateJobResponse = await apiClient.mergePdfs(files);
            setJobId(response.id);
            setStatus('Merging...');

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
                        setError('Merge failed. Please try again.');
                    }
                } catch (err) {
                    console.error('Error polling status:', err);
                }
            }, 2000);
        } catch (err) {
            setIsProcessing(false);
            setError('Failed to merge PDFs. Please try again.');
            console.error(err);
        }
    };

    const handleDownload = () => {
        if (jobId) {
            window.location.href = apiClient.getMergedDownloadUrl(jobId);
        }
    };

    const resetUpload = () => {
        setFiles([]);
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
                    <h2 className="text-4xl font-bold text-white mb-4">Merge PDFs</h2>
                    <p className="text-lg text-gray-200">
                        Combine multiple PDF files into one document
                    </p>
                </div>

                {/* Upload Section */}
                {!jobId && (
                    <>
                        <div
                            className={`border-3 border-dashed rounded-2xl p-12 text-center transition-all mb-8 ${isDragging
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
                                Drop your PDFs here
                            </h3>
                            <p className="text-gray-600 mb-6">or</p>
                            <label className="inline-block cursor-pointer">
                                <input
                                    type="file"
                                    accept="application/pdf"
                                    multiple
                                    onChange={handleFileSelect}
                                    className="hidden"
                                />
                                <span className="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition inline-block">
                                    Browse Files
                                </span>
                            </label>
                            <p className="text-sm text-gray-500 mt-4">Select multiple PDF files (minimum 2)</p>
                            {error && <p className="text-red-600 mt-4">{error}</p>}
                        </div>

                        {/* File List */}
                        {files.length > 0 && (
                            <div className="bg-white rounded-2xl shadow-lg p-8 mb-8">
                                <h3 className="text-xl font-semibold text-gray-900 mb-6">
                                    Files to Merge ({files.length})
                                </h3>
                                <div className="space-y-3 mb-8">
                                    {files.map((file, index) => (
                                        <div
                                            key={index}
                                            className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
                                        >
                                            <div className="flex items-center space-x-4 flex-1">
                                                <span className="text-sm font-semibold text-gray-500 w-8">#{index + 1}</span>
                                                <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                                    <FileText className="w-5 h-5 text-red-600" />
                                                </div>
                                                <div className="flex-1 min-w-0">
                                                    <h4 className="font-medium text-gray-900 truncate">{file.name}</h4>
                                                    <p className="text-sm text-gray-600">{formatBytes(file.size)}</p>
                                                </div>
                                            </div>
                                            <div className="flex items-center space-x-2 ml-4">
                                                <button
                                                    onClick={() => moveFile(index, 'up')}
                                                    disabled={index === 0}
                                                    className="p-2 text-gray-600 hover:text-blue-600 disabled:opacity-30 disabled:cursor-not-allowed"
                                                >
                                                    ↑
                                                </button>
                                                <button
                                                    onClick={() => moveFile(index, 'down')}
                                                    disabled={index === files.length - 1}
                                                    className="p-2 text-gray-600 hover:text-blue-600 disabled:opacity-30 disabled:cursor-not-allowed"
                                                >
                                                    ↓
                                                </button>
                                                <button
                                                    onClick={() => removeFile(index)}
                                                    className="p-2 text-gray-600 hover:text-red-600"
                                                >
                                                    <X className="w-5 h-5" />
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <button
                                    onClick={handleMerge}
                                    disabled={isProcessing || files.length < 2}
                                    className="w-full py-4 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                                >
                                    {isProcessing ? (
                                        <>
                                            <Loader2 className="w-5 h-5 animate-spin" />
                                            <span>{status}</span>
                                        </>
                                    ) : (
                                        <span>Merge PDFs</span>
                                    )}
                                </button>
                            </div>
                        )}
                    </>
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
                                <h3 className="text-2xl font-bold text-gray-900 mb-2">Merge Complete!</h3>
                                <p className="text-gray-600 mb-8">Your PDFs have been successfully merged</p>

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
                                        Merge More
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="text-center">
                                <Loader2 className="w-16 h-16 text-blue-600 animate-spin mx-auto mb-4" />
                                <h3 className="text-xl font-semibold text-gray-900 mb-2">Processing...</h3>
                                <p className="text-gray-600">Merging your PDF files</p>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
}
