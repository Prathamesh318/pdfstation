# PDFStation API Documentation

> **Version**: 1.0.0  
> **Base URL**: `http://localhost:8081/api/pdf/jobs`  
> **Authentication**: Currently None (can be added with Spring Security)

## Table of Contents
- [Overview](#overview)
- [Common Responses](#common-responses)
- [Compression API](#compression-api)
- [Merge API](#merge-api)
- [Download API](#download-api)
- [Size Estimation API](#size-estimation-api)
- [Error Codes](#error-codes)

---

## Overview

PDFStation provides REST APIs for PDF manipulation including compression, merging, and more features coming soon.

### Response Format

All successful job creation requests return:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "CREATED"
}
```

---

## Common Responses

### Success Response
**Status Code**: `200 OK`

```json
{
  "id": "uuid",
  "status": "CREATED" | "PROCESSING" | "COMPLETED" | "FAILED"
}
```

### Error Response
**Status Code**: `400 Bad Request` | `413 Payload Too Large` | `500 Internal Server Error`

```json
{
  "timestamp": "2026-01-03T22:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/pdf/jobs"
}
```

---

## Compression API

### Compress PDF

Compress a PDF file with configurable quality settings.

**Endpoint**: `POST /api/pdf/jobs`

**Content-Type**: `multipart/form-data`

#### Request Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `operation` | string | Yes | - | Must be `COMPRESS` |
| `file` | file | Yes | - | PDF file to compress (max 20MB) |
| `quality` | integer | No | 50 | Compression quality 0-100 |

#### Quality Guide

| Quality | DPI | JPEG Quality | Use Case | Expected Reduction |
|---------|-----|--------------|----------|-------------------|
| 90-100 | 300 | 0.91-0.95 | Print quality | 20-40% |
| 50-80 | 150 | 0.85-0.91 | Screen/digital | 40-60% |
| 10-50 | 96 | 0.75-0.85 | Web/email | 60-80% |

#### Example Request

```bash
curl -X POST "http://localhost:8081/api/pdf/jobs?operation=COMPRESS&quality=80" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@document.pdf"
```

#### Example Response

```json
{
  "id": "a3f2c1b0-9876-4321-abcd-1234567890ab",
  "status": "CREATED"
}
```

#### Processing Flow

1. File uploaded and saved
2. Job created with status `CREATED`
3. Kafka event triggers async processing
4. Job status changes to `PROCESSING`
5. Compression phases execute:
   - Image optimization (DPI-aware)
   - Font subsetting
   - Content stream compression
   - Duplicate object removal
6. Job status changes to `COMPLETED`
7. Download available via download endpoint

---

## Merge API

### Merge Multiple PDFs

Merge multiple PDF files into a single document.

**Endpoint**: `POST /api/pdf/jobs/merge`

**Content-Type**: `multipart/form-data`

#### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `files` | file[] | Yes | Array of PDF files to merge (max 20MB total) |

#### Example Request

```bash
curl -X POST "http://localhost:8081/api/pdf/jobs/merge" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@document1.pdf" \
  -F "files=@document2.pdf" \
  -F "files=@document3.pdf"
```

#### Example Response

```json
{
  "id": "b7e4d3c2-1234-5678-wxyz-9876543210cd",
  "status": "CREATED"
}
```

---

## Download API

### Download Compressed PDF

Download a processed (compressed) PDF file.

**Endpoint**: `GET /api/pdf/jobs/{jobId}/download`

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `jobId` | UUID | Yes | Job ID from creation response |

#### Example Request

```bash
curl "http://localhost:8081/api/pdf/jobs/a3f2c1b0-9876-4321-abcd-1234567890ab/download" \
  -o compressed_output.pdf
```

#### Response

**Content-Type**: `application/pdf`

Binary PDF file stream with headers:
```
Content-Disposition: attachment; filename="compressed_{jobId}.pdf"
Content-Type: application/pdf
```

#### Error Cases

| Status | Condition |
|--------|-----------|
| 404 | Job not found |
| 400 | Job not completed yet (status != COMPLETED) |
| 500 | File not accessible |

---

### Download Merged PDF

Download a merged PDF file.

**Endpoint**: `GET /api/pdf/jobs/{jobId}/download-merged`

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `jobId` | UUID | Yes | Job ID from merge creation response |

#### Example Request

```bash
curl "http://localhost:8081/api/pdf/jobs/b7e4d3c2-1234-5678-wxyz-9876543210cd/download-merged" \
  -o merged_output.pdf
```

#### Response

**Content-Type**: `application/pdf`

Binary PDF file stream with headers:
```
Content-Disposition: attachment; filename="merged_{jobId}.pdf"
Content-Type: application/pdf
```

---

## Size Estimation API

### Estimate Compressed Size

Get an estimate of the compressed file size before processing.

**Endpoint**: `GET /api/pdf/jobs/estimate-size`

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `originalSize` | long | Yes | Original file size in bytes |
| `quality` | integer | Yes | Compression quality 0-100 |

#### Example Request

```bash
curl "http://localhost:8081/api/pdf/jobs/estimate-size?originalSize=5000000&quality=50"
```

#### Example Response

```json
"2125000"
```

Response is the estimated size in bytes as a string.

#### Estimation Formula

```
estimatedSize = originalSize × (quality / 100) × 0.85
```

*Note: This is a heuristic. Actual compression varies based on content.*

---

## Error Codes

### HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 | OK | Successful request |
| 400 | Bad Request | Invalid parameters, missing required fields |
| 404 | Not Found | Job ID doesn't exist |
| 413 | Payload Too Large | File exceeds 20MB limit |
| 500 | Internal Server Error | Processing error, file corruption |

### Application Error Messages

| Error Message | Cause | Solution |
|--------------|-------|----------|
| "File too large" | File > 20MB | Reduce file size or contact admin |
| "Job not found" | Invalid job ID | Verify job ID from creation response |
| "PDF not ready yet" | Download before completion | Wait for status=COMPLETED |
| "Invalid operation" | Wrong operation parameter | Use COMPRESS or MERGE |
| "No files provided" | Empty file array | Provide at least 1 file |

---

## Rate Limiting

Currently, no rate limiting is implemented. Future versions will include:
- 100 requests per minute per IP
- 1GB total upload per hour per IP

---

## Best Practices

### 1. Quality Selection
- **Print documents**: Use quality 90-95
- **Digital sharing**: Use quality 60-80
- **Web/email**: Use quality 30-50

### 2. File Naming
- Original filenames are preserved in storage
- Use descriptive names for easy identification

### 3. Polling for Completion
```bash
# Check job status (not implemented yet, coming soon)
curl "http://localhost:8081/api/pdf/jobs/{jobId}/status"
```

### 4. Error Handling
Always check HTTP status codes and handle errors gracefully in your application.

---

## Examples

### Complete Workflow: Compress and Download

```bash
# Step 1: Upload and compress
RESPONSE=$(curl -X POST "http://localhost:8081/api/pdf/jobs?operation=COMPRESS&quality=75" \
  -F "file=@large_document.pdf")

# Step 2: Extract job ID (using jq)
JOB_ID=$(echo $RESPONSE | jq -r '.id')

# Step 3: Wait a few seconds for processing
sleep 5

# Step 4: Download compressed file
curl "http://localhost:8081/api/pdf/jobs/$JOB_ID/download" \
  -o compressed_document.pdf
```

### Complete Workflow: Merge PDFs

```bash
# Step 1: Merge multiple files
RESPONSE=$(curl -X POST "http://localhost:8081/api/pdf/jobs/merge" \
  -F "files=@chapter1.pdf" \
  -F "files=@chapter2.pdf" \
  -F "files=@chapter3.pdf")

# Step 2: Extract job ID
JOB_ID=$(echo $RESPONSE | jq -r '.id')

# Step 3: Wait for processing
sleep 3

# Step 4: Download merged file
curl "http://localhost:8081/api/pdf/jobs/$JOB_ID/download-merged" \
  -o complete_book.pdf
```

---

## Postman Collection

Import the following JSON to test APIs in Postman:

```json
{
  "info": {
    "name": "PDFStation API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Compress PDF",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/api/pdf/jobs?operation=COMPRESS&quality=80",
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": "/path/to/file.pdf"
            }
          ]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8081"
    }
  ]
}
```

---

## Support

For issues or questions:
- GitHub Issues: [github.com/yourrepo/pdfstation/issues](https://github.com)
- Email: support@pdfstation.com
- Documentation: [Full README](../README.md)

---

**Last Updated**: 2026-01-03  
**API Version**: 1.0.0
