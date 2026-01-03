const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

export const apiClient = {
    // Compress PDF
    compressPdf: async (file: File, quality: number) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('operation', 'COMPRESS');
        formData.append('quality', quality.toString());

        const response = await fetch(`${API_BASE_URL}/api/pdf/jobs/compress`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error('Failed to create compression job');
        }

        return response.json();
    },

    // Merge PDFs
    mergePdfs: async (files: File[]) => {
        const formData = new FormData();
        files.forEach((file) => {
            formData.append('files', file);
        });

        const response = await fetch(`${API_BASE_URL}/api/pdf/jobs/merge`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error('Failed to create merge job');
        }

        return response.json();
    },

    // Get job status
    getJobStatus: async (jobId: string) => {
        const response = await fetch(`${API_BASE_URL}/api/pdf/jobs/${jobId}`);

        if (!response.ok) {
            throw new Error('Failed to fetch job status');
        }

        return response.json();
    },

    // Download compressed PDF
    getDownloadUrl: (jobId: string) => {
        return `${API_BASE_URL}/api/pdf/jobs/${jobId}/download`;
    },

    // Download merged PDF
    getMergedDownloadUrl: (jobId: string) => {
        return `${API_BASE_URL}/api/pdf/jobs/${jobId}/download-merged`;
    },

    // Split PDF
    splitPdf: async (
        file: File,
        splitType: 'pages' | 'interval' | 'all',
        ranges?: string,
        interval?: number
    ) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('splitType', splitType);

        if (ranges) {
            formData.append('ranges', ranges);
        }
        if (interval !== undefined) {
            formData.append('interval', interval.toString());
        }

        const response = await fetch(`${API_BASE_URL}/api/pdf/jobs/split`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error('Failed to create split job');
        }

        return response.json();
    },

    // Download split PDFs (ZIP)
    getSplitDownloadUrl: (jobId: string) => {
        return `${API_BASE_URL}/api/pdf/jobs/${jobId}/download-split`;
    },

    // Estimate size
    estimateSize: async (originalSize: number, quality: number) => {
        const response = await fetch(
            `${API_BASE_URL}/api/pdf/jobs/estimate-size?originalSize=${originalSize}&quality=${quality}`
        );

        if (!response.ok) {
            throw new Error('Failed to estimate size');
        }

        return response.text();
    },
};
