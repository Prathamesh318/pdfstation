import React, { useState, useEffect } from 'react';
import { FileText, Download, Loader2, ArrowLeft, AlertCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/client';

export default function PdfToWordPage() {
    const [file, setFile] = useState<File | null>(null);
    const [isConverting, setIsConverting] = useState(false);
    const [jobId, setJobId] = useState<string | null>(null);
    const [status, setStatus] = useState<string>('');
    const [error, setError] = useState<string | null>(null);

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
                    setIsConverting(false);
                } else if (jobStatus.status === 'FAILED') {
                    console.log('❌ Job failed!');
                    setIsConverting(false);
                    setError('Split failed. Please try again.');
                } else if (pollCount >= maxPolls) {
                    console.log('⏱️ Max polls reached');
                    setIsConverting(false);
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

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
            setError(null);
            setStatus('');
            setJobId(null);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!file) return;

        setIsConverting(true);
        setError(null);

        try {
            const response = await apiClient.convertPdfToWord(file);
            setJobId(response.jobId);
            setStatus('PROCESSING');
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Conversion failed');
            setIsConverting(false);
        }
    };

    const handleDownload = async () => {
        if (!jobId) return;
        try {
            const blob = await apiClient.downloadWordDoc(jobId);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `converted_${jobId}.docx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (err) {
            setError('Failed to download file');
        }
    };

    return (
        <div className="max-w-4xl mx-auto px-4 py-12">
            <Link to="/" className="inline-flex items-center text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white mb-8 transition-colors">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back to Home
            </Link>

            <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-xl p-8 border border-gray-100 dark:border-slate-800">
                <div className="flex items-center mb-8">
                    <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-xl flex items-center justify-center mr-4">
                        <FileText className="w-6 h-6 text-blue-600 dark:text-blue-400" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">PDF to Word</h1>
                        <p className="text-gray-600 dark:text-gray-400">Convert your PDF documents to editable Word files</p>
                    </div>
                </div>

                <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded-xl p-4 mb-8 flex items-start">
                    <AlertCircle className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5 mr-3 flex-shrink-0" />
                    <p className="text-sm text-blue-800 dark:text-blue-200">
                        <strong>Note:</strong> This tool extracts text and images but may not preserve complex layouts (like multiple columns or floating elements) perfectly. Best for text-heavy documents.
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
                            Select PDF File
                        </label>
                        <input
                            type="file"
                            accept=".pdf"
                            onChange={handleFileChange}
                            className="block w-full text-sm text-gray-500 dark:text-gray-400
                                file:mr-4 file:py-2 file:px-4
                                file:rounded-full file:border-0
                                file:text-sm file:font-semibold
                                file:bg-blue-50 file:text-blue-700
                                dark:file:bg-blue-900/30 dark:file:text-blue-400
                                hover:file:bg-blue-100 dark:hover:file:bg-blue-900/50
                                transition-colors"
                        />
                    </div>

                    {error && (
                        <div className="p-4 bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 rounded-xl flex items-center">
                            <AlertCircle className="w-5 h-5 mr-2" />
                            {error}
                        </div>
                    )}

                    <div className="flex items-center justify-end espacio-x-4 pt-4">
                        {status === 'COMPLETED' ? (
                            <button
                                type="button"
                                onClick={handleDownload}
                                className="flex items-center px-6 py-3 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl transition-colors shadow-sm"
                            >
                                <Download className="w-5 h-5 mr-2" />
                                Download Word Doc
                            </button>
                        ) : (
                            <button
                                type="submit"
                                disabled={!file || isConverting}
                                className={`flex items-center px-6 py-3 font-semibold rounded-xl transition-all shadow-sm
                                    ${!file || isConverting
                                        ? 'bg-gray-100 dark:bg-slate-800 text-gray-400 dark:text-slate-600 cursor-not-allowed'
                                        : 'bg-blue-600 hover:bg-blue-700 text-white hover:shadow-md'
                                    }`}
                            >
                                {isConverting ? (
                                    <>
                                        <Loader2 className="w-5 h-5 mr-2 animate-spin" />
                                        Converting...
                                    </>
                                ) : (
                                    <>
                                        Start Conversion
                                    </>
                                )}
                            </button>
                        )}
                    </div>
                </form>
            </div>
        </div>
    );
}
