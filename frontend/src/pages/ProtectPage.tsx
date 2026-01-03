import React, { useState } from 'react';
import { apiClient } from '../api/client';
import { ArrowLeft, Lock, Shield, Download, FileText, Check, AlertCircle } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function ProtectPage() {
    const [file, setFile] = useState<File | null>(null);
    const [userPassword, setUserPassword] = useState('');
    const [ownerPassword, setOwnerPassword] = useState('');
    const [permissions, setPermissions] = useState({
        allowPrinting: true,
        allowCopying: true,
        allowModification: false,
        allowAssembly: false
    });

    const [jobId, setJobId] = useState<string | null>(null);
    const [status, setStatus] = useState<'idle' | 'uploading' | 'processing' | 'completed' | 'error'>('idle');
    const [error, setError] = useState<string | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
            setError(null);
            setStatus('idle');
            setJobId(null);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!file) return;
        if (!userPassword) {
            setError('Please set a User Password (required to open the PDF)');
            return;
        }

        try {
            setStatus('uploading');
            const response = await apiClient.protectPdf(
                file,
                userPassword,
                ownerPassword || undefined,
                permissions
            );

            setJobId(response.id);
            pollStatus(response.id);
        } catch (err: any) {
            console.error(err);
            setError(err.message || 'Failed to protect PDF');
            setStatus('error');
        }
    };

    const pollStatus = async (id: string) => {
        setStatus('processing');
        const interval = setInterval(async () => {
            try {
                const job = await apiClient.getJobStatus(id);
                if (job.status === 'COMPLETED') {
                    clearInterval(interval);
                    setStatus('completed');
                } else if (job.status === 'FAILED') {
                    clearInterval(interval);
                    setStatus('error');
                    setError(job.errorMessage || 'Processing failed');
                }
            } catch (err) {
                console.error(err);
                clearInterval(interval);
                setStatus('error');
                setError('Failed to check status');
            }
        }, 2000);
    };

    const handleDownload = () => {
        if (jobId) {
            window.location.href = apiClient.getProtectedDownloadUrl(jobId);
        }
    };

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            <Link to="/" className="inline-flex items-center text-gray-600 hover:text-gray-900 mb-8 transition-colors">
                <ArrowLeft className="w-5 h-5 mr-2" />
                Back to Home
            </Link>

            <div className="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100">
                <div className="p-8 border-b border-gray-100 bg-gradient-to-r from-red-50 to-pink-50">
                    <div className="flex items-center space-x-4">
                        <div className="p-3 bg-red-100 rounded-lg text-red-600">
                            <Lock className="w-8 h-8" />
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">Protect PDF</h1>
                            <p className="text-gray-600 mt-1">Encrypt your PDF with passwords and permissions</p>
                        </div>
                    </div>
                </div>

                <div className="p-8 space-y-8">
                    {/* File Upload Section */}
                    <div className="space-y-4">
                        <label className="block text-sm font-medium text-gray-700">Select PDF File</label>
                        <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center transition-all hover:border-red-400 hover:bg-red-50 group">
                            <input
                                type="file"
                                accept=".pdf"
                                onChange={handleFileChange}
                                className="hidden"
                                id="file-upload"
                            />
                            <label htmlFor="file-upload" className="cursor-pointer flex flex-col items-center">
                                {file ? (
                                    <>
                                        <FileText className="w-12 h-12 text-red-500 mb-3" />
                                        <span className="text-lg font-medium text-gray-900">{file.name}</span>
                                        <span className="text-sm text-gray-500 mt-1">{(file.size / 1024 / 1024).toFixed(2)} MB</span>
                                        <button
                                            type="button"
                                            className="mt-4 text-sm text-red-600 font-medium hover:text-red-700"
                                            onClick={(e) => {
                                                e.preventDefault();
                                                document.getElementById('file-upload')?.click();
                                            }}
                                        >
                                            Change file
                                        </button>
                                    </>
                                ) : (
                                    <>
                                        <div className="p-4 bg-red-100 rounded-full text-red-500 mb-4 group-hover:scale-110 transition-transform">
                                            <Lock className="w-8 h-8" />
                                        </div>
                                        <span className="text-lg font-medium text-gray-900">Drop PDF here or click to upload</span>
                                        <span className="text-sm text-gray-500 mt-2">Supports PDF files up to 20MB</span>
                                    </>
                                )}
                            </label>
                        </div>
                    </div>

                    {file && status !== 'completed' && (
                        <form onSubmit={handleSubmit} className="space-y-6 animate-fade-in">
                            <div className="grid md:grid-cols-2 gap-6">
                                {/* Password Section */}
                                <div className="space-y-4">
                                    <h3 className="font-semibold text-gray-900 flex items-center">
                                        <Lock className="w-4 h-4 mr-2" /> Security
                                    </h3>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            User Password <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="password"
                                            value={userPassword}
                                            onChange={(e) => setUserPassword(e.target.value)}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                            placeholder="Required to open the file"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Owner Password
                                        </label>
                                        <input
                                            type="password"
                                            value={ownerPassword}
                                            onChange={(e) => setOwnerPassword(e.target.value)}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                            placeholder="To change permissions (optional)"
                                        />
                                    </div>
                                </div>

                                {/* Permissions Section */}
                                <div className="space-y-4">
                                    <h3 className="font-semibold text-gray-900 flex items-center">
                                        <Shield className="w-4 h-4 mr-2" /> Permissions
                                    </h3>
                                    <div className="bg-gray-50 rounded-xl p-4 space-y-3 border border-gray-200">
                                        {[
                                            { key: 'allowPrinting', label: 'Allow Printing' },
                                            { key: 'allowCopying', label: 'Allow Copying Content' },
                                            { key: 'allowModification', label: 'Allow Modifications' },
                                            { key: 'allowAssembly', label: 'Allow Document Assembly' }
                                        ].map(({ key, label }) => (
                                            <label key={key} className="flex items-center space-x-3 cursor-pointer">
                                                <input
                                                    type="checkbox"
                                                    checked={permissions[key as keyof typeof permissions]}
                                                    onChange={(e) => setPermissions(prev => ({ ...prev, [key]: e.target.checked }))}
                                                    className="w-4 h-4 text-red-600 rounded border-gray-300 focus:ring-red-500"
                                                />
                                                <span className="text-sm text-gray-700">{label}</span>
                                            </label>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            {error && (
                                <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-center text-red-700">
                                    <AlertCircle className="w-5 h-5 mr-3 flex-shrink-0" />
                                    {error}
                                </div>
                            )}

                            <button
                                type="submit"
                                disabled={status !== 'idle' && status !== 'error'}
                                className={`w-full py-4 px-6 rounded-xl font-bold text-white text-lg transition-all transform hover:scale-[1.02] 
                                    ${status === 'processing' || status === 'uploading'
                                        ? 'bg-gray-400 cursor-not-allowed'
                                        : 'bg-gradient-to-r from-red-600 to-pink-600 hover:from-red-700 hover:to-pink-700 shadow-lg hover:shadow-xl'
                                    }`}
                            >
                                {status === 'uploading' ? 'Uploading...' :
                                    status === 'processing' ? 'Processing...' : 'Protect PDF'}
                            </button>
                        </form>
                    )}

                    {status === 'completed' && (
                        <div className="animate-fade-in text-center py-8">
                            <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-6">
                                <Check className="w-8 h-8" />
                            </div>
                            <h2 className="text-2xl font-bold text-gray-900 mb-2">Protection Applied!</h2>
                            <p className="text-gray-600 mb-8">Your PDF has been successfully encrypted.</p>

                            <div className="flex justify-center gap-4">
                                <button
                                    onClick={handleDownload}
                                    className="flex items-center px-8 py-3 bg-red-600 text-white rounded-xl font-bold hover:bg-red-700 shadow-lg hover:shadow-xl transition-all"
                                >
                                    <Download className="w-5 h-5 mr-2" />
                                    Download Protected PDF
                                </button>
                                <button
                                    onClick={() => {
                                        setFile(null);
                                        setStatus('idle');
                                        setUserPassword('');
                                        setOwnerPassword('');
                                    }}
                                    className="px-8 py-3 bg-gray-100 text-gray-700 rounded-xl font-bold hover:bg-gray-200 transition-all"
                                >
                                    Protect Another
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
