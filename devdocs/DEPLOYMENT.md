# PDFStation Deployment Guide

## Repository Structure Recommendation

**Use a Monorepo** with separate deployments for frontend and backend.

### Recommended Structure
```
pdfstation/
├── backend/              # Spring Boot application (current code)
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── ...
├── frontend/             # React + Vite application
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts
│   └── ...
├── .github/
│   └── workflows/        # CI/CD pipelines
│       └── deploy.yml
├── docs/                 # Documentation
├── README.md
└── docker-compose.yml    # For local development
```

---

## Free Deployment Strategy

### Backend: Render.com (Free Tier)

**Features:**
- ✅ Free PostgreSQL database (1GB storage)
- ✅ Automatic deploys from GitHub
- ✅ 750 hours/month free
- ✅ Auto-sleep after 15min inactivity
- ✅ Custom domains

**Limitations:**
- ❌ No Kafka support on free tier
- ❌ Cold starts (15-30 seconds after sleep)
- ❌ Limited to 750 hours/month

**Setup:**

Create `render.yaml` in repository root:

```yaml
services:
  - type: web
    name: pdfstation-api
    env: java
    rootDir: backend
    buildCommand: ./mvnw clean package -DskipTests
    startCommand: java -jar target/pdfstation-0.0.1-SNAPSHOT.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: pdfstation-db
          property: connectionString
      - key: SPRING_DATASOURCE_USERNAME
        fromDatabase:
          name: pdfstation-db
          property: user
      - key: SPRING_DATASOURCE_PASSWORD
        fromDatabase:
          name: pdfstation-db
          property: password

databases:
  - name: pdfstation-db
    databaseName: pdfstation
    user: pdfstation
    plan: free
```

**Environment Variables to Set in Render Dashboard:**
```properties
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8081
SPRING_JPA_HIBERNATE_DDL_AUTO=update
LOGGING_LEVEL_ROOT=INFO
```

---

### Frontend: Vercel (Free Tier)

**Features:**
- ✅ Unlimited bandwidth
- ✅ Automatic deploys from GitHub
- ✅ Global CDN
- ✅ Custom domains
- ✅ Preview deployments for PRs
- ✅ Zero configuration for Vite/React

**Setup:**

Create `vercel.json` in `frontend/` directory:

```json
{
  "version": 2,
  "buildCommand": "npm run build",
  "outputDirectory": "dist",
  "devCommand": "npm run dev",
  "installCommand": "npm install",
  "framework": "vite",
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://pdfstation-api.onrender.com/api/:path*"
    }
  ]
}
```

**Environment Variables to Set in Vercel Dashboard:**
```env
VITE_API_URL=https://pdfstation-api.onrender.com
VITE_MAX_FILE_SIZE=20971520
```

---

## Kafka Alternative for Free Deployment

Since Render's free tier doesn't support Kafka, you have several options:

### Option 1: CloudKarafka (Free Tier)
- **Plan:** Developer Duck
- **Features:** 10 topics, 5MB retention
- **Cost:** Free
- **Setup:** https://www.cloudkarafka.com/

### Option 2: Upstash Kafka (Free Tier)
- **Plan:** Free tier
- **Features:** Serverless, HTTP-based, 10K messages/day
- **Cost:** Free
- **Setup:** https://upstash.com/

### Option 3: Remove Kafka for Initial Deployment
- Process jobs synchronously
- Simpler deployment
- Add Kafka later when scaling
- **Recommended for demo/learning**

To remove Kafka:
1. Comment out Kafka dependencies in `pom.xml`
2. Modify `PdfJobConsumer` to process jobs directly
3. Remove `@KafkaListener` annotations
4. Call processing services directly from controllers

---

## CORS Configuration for Production

Update `CorsConfig.java`:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
```

Add to `application.properties`:
```properties
# Development
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000

# Production (add your Vercel URL)
# app.cors.allowed-origins=https://your-app.vercel.app,https://pdfstation.vercel.app
```

---

## CI/CD Pipeline

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Test with Maven
        working-directory: ./backend
        run: ./mvnw test

  test-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        working-directory: ./frontend
        run: npm ci
      - name: Run tests
        working-directory: ./frontend
        run: npm test

  deploy-backend:
    needs: [test-backend]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Render
        run: echo "Backend auto-deploys via Render webhook"

  deploy-frontend:
    needs: [test-frontend]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Vercel
        run: echo "Frontend auto-deploys via Vercel webhook"
```

---

## Cost Breakdown

### Free Tier Limits

| Service | Free Tier | Limits |
|---------|-----------|--------|
| **Vercel** | Unlimited | 100GB bandwidth/month, Unlimited builds |
| **Render** | 750 hours/month | Auto-sleep after 15min inactivity |
| **Render Postgres** | 1GB storage | 90-day expiration for inactive services |
| **CloudKarafka** | 10 topics | 5MB retention |
| **Upstash Kafka** | 10K messages/day | HTTP-based only |

### Paid Tier (If Needed)

| Service | Plan | Cost/Month |
|---------|------|------------|
| **Vercel Pro** | Production | $20 |
| **Render Starter** | Always-on | $7 |
| **Render Postgres** | Persistent DB | $7 |
| **CloudKarafka** | Production | $9 |

**Total Free Tier Cost: $0/month**  
**Total Paid Tier Cost: ~$43/month** (if all services upgraded)

---

## Deployment Steps

### 1. Prepare Repository

```bash
# Restructure repository
cd pdfstation
mkdir backend
mv src pom.xml mvnw mvnw.cmd backend/

# Backend is now in backend/ folder
# Frontend will be in frontend/ folder (to be created)
```

### 2. Deploy Backend to Render

1. **Create Render Account**: https://render.com
2. **New Web Service** → Connect GitHub repo
3. **Select repository**: `pdfstation`
4. **Root directory**: `backend`
5. **Build command**: `./mvnw clean package -DskipTests`
6. **Start command**: `java -jar target/pdfstation-0.0.1-SNAPSHOT.jar`
7. **Create PostgreSQL database** (free tier)
8. **Link database** to web service
9. **Deploy**

### 3. Deploy Frontend to Vercel

1. **Create Vercel Account**: https://vercel.com
2. **Import Git Repository**
3. **Framework Preset**: Vite
4. **Root Directory**: `frontend`
5. **Add environment variable**: `VITE_API_URL=https://your-backend.onrender.com`
6. **Deploy**

### 4. Update CORS Configuration

In `application.properties`:
```properties
app.cors.allowed-origins=https://your-app.vercel.app
```

Commit and push to trigger redeployment.

---

## Alternative Deployment Options

### Option 1: Both on Render
- Deploy React as static site on Render
- **Pros:** Single platform
- **Cons:** Vercel is better optimized for React

### Option 2: Both on Vercel
- Deploy Spring Boot as serverless function
- **Pros:** Single platform
- **Cons:** Not ideal for Spring Boot, complex setup

### Option 3: Railway.app (Alternative to Render)
- Better Spring Boot support
- PostgreSQL included
- **Cost:** $5/month (no free tier anymore)

### Option 4: Fly.io
- Better for Java apps
- Global deployment
- **Cost:** Free tier available but limited

---

## Monorepo vs Separate Repos

### ✅ Monorepo (Recommended)

**Advantages:**
- Single source of truth
- Easier API contract sync
- Shared documentation
- One PR for full-stack features
- Simpler for contributors

**Disadvantages:**
- Slightly larger repo
- Need to configure deployment paths

### Separate Repos

**Advantages:**
- Cleaner separation
- Independent versioning
- Smaller individual repos

**Disadvantages:**
- API contract sync issues
- Multiple repos to manage
- Documentation fragmentation
- More complex for contributors

---

## Production Checklist

### Backend
- [ ] Set up Render PostgreSQL database
- [ ] Configure environment variables
- [ ] Set up automatic deploys
- [ ] Configure CORS for Vercel domain
- [ ] Set up logging/monitoring
- [ ] Configure file upload limits
- [ ] Test all API endpoints

### Frontend
- [ ] Build optimized production bundle
- [ ] Configure Vercel deployment
- [ ] Set API URL environment variable
- [ ] Test CORS requests
- [ ] Add error tracking (Sentry)
- [ ] Configure analytics (optional)
- [ ] Test on mobile devices

### Security
- [ ] Use HTTPS only
- [ ] Configure secure headers
- [ ] Rate limiting (if needed)
- [ ] Input validation
- [ ] File type validation
- [ ] Maximum file size enforcement

---

## Monitoring & Logging

### Render Monitoring
- Built-in logs viewer
- Health checks
- Metrics dashboard

### Vercel Analytics
- Built-in analytics
- Performance monitoring
- Error tracking

### External Services (Optional)
- **Sentry** - Error tracking
- **LogRocket** - Session replay
- **DataDog** - Application monitoring

---

## Scaling Strategy

### When to Upgrade from Free Tier

**Indicators:**
- Frequent cold starts affecting UX
- 750 hours/month exceeded
- Need always-on service
- Require more storage
- Need Kafka for async processing

### Upgrade Path
1. **Month 1-2:** Free tier (MVP, testing)
2. **Month 3-6:** Render Starter ($7) + Vercel free
3. **Month 6+:** Consider Railway/Fly.io for better Java support
4. **Production:** AWS/GCP/Azure with Kubernetes

---

## Recommended Approach

### For Development/Demo
```
✅ Monorepo structure
✅ Render (Backend + PostgreSQL)
✅ Vercel (Frontend)
✅ No Kafka (synchronous processing)
✅ Cost: $0/month
```

### For Production
```
✅ Monorepo structure
✅ Railway.app or Fly.io (Backend)
✅ Vercel (Frontend)
✅ Managed Kafka (CloudKarafka/Upstash)
✅ Cost: ~$20-50/month
```

---

## Next Steps

1. ✅ **Review** this deployment guide
2. ⬜ **Restructure** repository to monorepo format
3. ⬜ **Create** deployment configurations
4. ⬜ **Deploy** backend to Render
5. ⬜ **Build** and deploy frontend to Vercel
6. ⬜ **Test** end-to-end workflow
7. ⬜ **Monitor** and optimize

---

## Support Resources

- **Render Documentation**: https://render.com/docs
- **Vercel Documentation**: https://vercel.com/docs
- **CloudKarafka Setup**: https://www.cloudkarafka.com/docs
- **Spring Boot on Render**: https://render.com/docs/deploy-spring-boot

---

**Ready to deploy? Let me know if you need help with any step!** 🚀
