# PDFStation

> **Production-grade PDF processing service** with modern web interface

A high-performance, scalable PDF manipulation platform featuring industry-standard compression, merging, and real-time job processing.

---

## 🏗️ Monorepo Structure

```
pdfstation/
├── backend/          # Spring Boot API (Java 17)
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/         # React + TypeScript (Vite)
│   ├── src/
│   ├── package.json
│   └── README.md
└── devdocs/         # Documentation
    ├── API.md
    ├── DEPLOYMENT.md
    └── COMPRESSION_ANALYSIS.md
```

---

## ⚡ Quick Start

### Prerequisites
- **Backend**: Java 17+, PostgreSQL, Kafka (optional)
- **Frontend**: Node.js 18+
- **Recommended**: Docker Desktop (for local setup)

### Backend Setup

```bash
cd backend
./mvnw spring-boot:run
```

**Backend runs on:** http://localhost:8081  
**API Docs (Swagger):** http://localhost:8081/swagger-ui.html

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

**Frontend runs on:** http://localhost:5173

---

## 🚀 Features

### Current (Phase 1)
- ✅ **PDF Compression** - Industry-standard compression with quality control (0-100%)
- ✅ **PDF Merge** - Combine multiple PDFs into a single document
- ✅ **Real-time Status** - Job processing with live updates
- ✅ **Job History** - Track and download completed jobs
- ✅ **RESTful API** - Complete API with Swagger documentation

### Planned (Phase 2+)
- 🔄 **PDF Split** - Extract specific pages or ranges
- 🔄 **PDF Protect** - Password protection and permissions
- 🔄 **PDF Watermark** - Add text/image watermarks
- 🔄 **Batch Processing** - Process multiple files simultaneously

---

## 📚 Tech Stack

### Backend
- **Framework:** Spring Boot 4.0.1
- **Database:** PostgreSQL
- **Message Queue:** Apache Kafka
- **PDF Library:** Apache PDFBox 3.0.2
- **API Docs:** Springdoc OpenAPI (Swagger)

### Frontend
- **Framework:** React 18 + TypeScript
- **Build Tool:** Vite
- **State Management:** Zustand + React Query
- **UI Components:** Shadcn/ui + Tailwind CSS
- **Animations:** Framer Motion

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [API Documentation](devdocs/API.md) | Complete API reference with examples |
| [Deployment Guide](devdocs/DEPLOYMENT.md) | Deploy to Render + Vercel (free tier) |
| [Compression Analysis](devdocs/COMPRESSION_ANALYSIS.md) | Technical details on compression |
| [Frontend Design](devdocs/frontend_design.md) | Frontend architecture |

---

## 🔧 Development Workflow

### Run Full Stack Locally

**Option 1: Separate Terminals**
```bash
# Terminal 1: Backend
cd backend && ./mvnw spring-boot:run

# Terminal 2: Frontend  
cd frontend && npm run dev
```

**Option 2: Docker Compose** (Coming Soon)
```bash
docker-compose up
```

### API Testing
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Postman Collection:** Available in `devdocs/`

---

## 🌐 Deployment

### Free Tier (Recommended for Demo)
- **Backend:** [Render.com](https://render.com) (Spring Boot + PostgreSQL)
- **Frontend:** [Vercel](https://vercel.com) (React)
- **Cost:** $0/month

See [DEPLOYMENT.md](devdocs/DEPLOYMENT.md) for complete setup instructions.

---

## 📊 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/pdf/jobs` | POST | Create compression/merge job |
| `/api/pdf/jobs/{jobId}/download` | GET | Download compressed PDF |
| `/api/pdf/jobs/{jobId}/download-merged` | GET | Download merged PDF |
| `/api/pdf/jobs/merge` | POST | Merge multiple PDFs |
| `/api/pdf/jobs/estimate-size` | GET | Estimate compressed size |

**Full API documentation:** http://localhost:8081/swagger-ui.html

---

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm run test
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 Configuration

### Backend Configuration
Edit `backend/src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/pdfstation

# Kafka
kafka.topic.pdf-jobs=pdf-jobs

# Storage
pdfstation.storage.upload-dir=uploads/
```

### Frontend Configuration
Edit `frontend/.env`:
```env
VITE_API_URL=http://localhost:8081
VITE_MAX_FILE_SIZE=20971520
```

---

## 🐛 Troubleshooting

**Backend won't start?**
- Check PostgreSQL is running
- Verify Kafka broker (or disable Kafka in dev mode)
- Check port 8081 is available

**Frontend build fails?**
- Delete `node_modules` and reinstall: `npm clean-install`
- Clear Vite cache: `rm -rf node_modules/.vite`

**CORS errors?**
- Verify `CorsConfig.java` allows frontend origin
- Check `VITE_API_URL` points to correct backend

---

## 📄 License

MIT License - see LICENSE file for details

---

## 🙏 Acknowledgments

- **Apache PDFBox** - PDF manipulation library
- **Spring Boot** - Backend framework
- **React** - Frontend library
- **Vite** - Build tool

---

## 📧 Support

- **Issues:** [GitHub Issues](https://github.com/yourusername/pdfstation/issues)
- **Discussions:** [GitHub Discussions](https://github.com/yourusername/pdfstation/discussions)
- **Email:** support@pdfstation.com

---

**Built with ❤️ for the developer community**
