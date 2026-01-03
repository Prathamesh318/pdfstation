export interface Job {
    id: string;
    operation: 'COMPRESS' | 'MERGE';
    status: 'CREATED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
    inputPaths?: string[];
    outputPath?: string;
    compressionQuality?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateJobResponse {
    id: string;
    status: string;
}

export interface ApiError {
    message: string;
    status: number;
}
