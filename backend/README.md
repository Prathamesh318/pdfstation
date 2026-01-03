# PDFStation 📄

A high-performance, production-ready PDF processing service built with Spring Boot, Kafka, and Apache PDFBox. PDFStation provides industry-standard PDF compression and manipulation capabilities through a clean REST API.

## ✨ Features

### Current Features

#### 🗜️ **Advanced PDF Compression**
- **Industry-Standard Quality Preservation**
  - DPI-aware image downsampling (96-300 DPI based on quality)
  - High JPEG quality (0.75-0.95 range)
  - Bicubic interpolation for premium resizing
  - Smart image selection
- **Three-Phase Advanced Optimization**
  - **Font Subsetting**: Removes unused glyphs (20-40% savings on text-heavy PDFs)
  - **Content Stream Compression**: Lossless Flate compression (30-50% additional savings)
  - **Duplicate Object Removal**: MD5-based deduplication (10-30% savings)
- **User-Controlled Quality** (0-100%)
- **Size Estimation API** before compression

#### 📑 **PDF Merge**
- Merge multiple PDF files into single document
- Preserves quality and formatting
- Multi-file upload support (up to 20MB)

### 🚀 Upcoming Features

- **PDF Split**: Split PDFs by page range or extract specific pages
- **PDF Protection**: Password encryption and permission controls
- **PDF Watermarking**: Add text or image watermarks
- **PDF Rotation**: Rotate pages individually or in batch
- **PDF Metadata Editing**: Modify title, author, keywords
- **PDF to Image**: Convert PDF pages to PNG/JPEG
- **OCR Integration**: Extract text from scanned documents

## 🏗️ Architecture

PDFStation follows **Clean Architecture** principles with clear separation of concerns:

```
├── API Layer (com.app.pdfstation.api)
│   ├── Controllers: REST endpoints
│   ├── DTOs: Data transfer objects
│   └── Exception Handling: Global error management
├── Domain Layer (com.app.pdfstation.domain)
│   ├── Entities: JPA entities
│   └── Repositories: Data access
├── Service Layer (com.app.pdfstation.service)
│   ├── Business logic
│   └── PDF processing
└── Infrastructure Layer (com.app.pdfstation.infrastructure)
    ├── Kafka: Event-driven processing
    └── Storage: File system operations
```

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.x, Java 17+
- **Database**: PostgreSQL
- **Message Queue**: Apache Kafka
- **PDF Processing**: Apache PDFBox 3.0.2
- **Build Tool**: Maven
- **Security**: Spring Security with SSL/TLS support

## 📋 Prerequisites

- Java 17 or higher
- PostgreSQL 13+
- Apache Kafka 3.x
- Maven 3.8+

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/pdfstation.git
cd pdfstation
```

### 2. Configure Database

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pdfstation
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Start Kafka

```bash
# Start Zookeeper
zookeeper-server-start config/zookeeper.properties

# Start Kafka
kafka-server-start config/server.properties
```

### 4. Build and Run

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

The application will start on `http://localhost:8081` (or `https://localhost:8443` if SSL is enabled).

## 📖 API Documentation

For complete API documentation including all endpoints, parameters, examples, and best practices, see:

**[📘 Full API Documentation](devdocs/API.md)**

### Quick API Reference

### Compress PDF

**Endpoint**: `POST /api/pdf/jobs`

**Parameters**:
- `operation`: `COMPRESS`
- `file`: PDF file (multipart/form-data)
- `quality`: Compression quality 0-100 (optional, default: 50)

**Example**:
```bash
curl -X POST "http://localhost:8081/api/pdf/jobs?operation=COMPRESS&quality=80" \
  -F "file=@yourfile.pdf"
```

**Response**:
```json
{
  "id": "uuid",
  "status": "CREATED"
}
```

### Merge PDFs

**Endpoint**: `POST /api/pdf/jobs/merge`

**Parameters**:
- `files[]`: Multiple PDF files (multipart/form-data)

**Example**:
```bash
curl -X POST "http://localhost:8081/api/pdf/jobs/merge" \
  -F "files=@file1.pdf" \
  -F "files=@file2.pdf"
```

### Estimate Compressed Size

**Endpoint**: `GET /api/pdf/jobs/estimate-size`

**Parameters**:
- `originalSize`: File size in bytes
- `quality`: Compression quality 0-100

**Example**:
```bash
curl "http://localhost:8081/api/pdf/jobs/estimate-size?originalSize=1000000&quality=50"
```

### Download Processed PDF

**Endpoint**: `GET /api/pdf/jobs/{jobId}/download`

**Example**:
```bash
curl "http://localhost:8081/api/pdf/jobs/{jobId}/download" -o output.pdf
```

## ⚙️ Configuration

### File Upload Limits

```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### Kafka Topics

```properties
spring.kafka.bootstrap-servers=localhost:9092
kafka.topic.pdf-jobs=pdf-jobs
```

### SSL/HTTPS (Optional)

```properties
pdfstation.security.require-https=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_password
```

## 📊 Performance

### Compression Results

| Document Type | Typical Compression |
|--------------|---------------------|
| Scanned Documents | 60-85% reduction |
| Image-Heavy PDFs | 50-75% reduction |
| Text-Only PDFs | 30-50% reduction |
| Already Optimized | 10-20% reduction |

### Processing Speed

- Small PDFs (<1MB): ~0.5-1 second
- Medium PDFs (1-10MB): ~2-5 seconds
- Large PDFs (10-50MB): ~10-30 seconds

## 🔒 Security Features

- Spring Security integration
- SSL/TLS support
- File size validation
- Global exception handling
- MaxUploadSizeExceeded protection

## 🐛 Error Handling

PDFStation includes comprehensive error handling:
- `MaxUploadSizeExceededException`: File too large
- `IOException`: File processing errors
- `RuntimeException`: General errors

All errors return structured JSON responses.

## 📝 Logging

Detailed logging at every compression phase:
- Image optimization metrics
- Font subsetting results
- Content stream compression statistics
- Duplicate removal counts
- Overall compression ratios

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Authors

- **Your Name** - Initial work

## 🙏 Acknowledgments

- Apache PDFBox team for the excellent PDF library
- Spring Boot community
- Contributors and testers

## 📞 Support

For support, email support@pdfstation.com or open an issue on GitHub.

---

**Made with ❤️ by the PDFStation Team**
