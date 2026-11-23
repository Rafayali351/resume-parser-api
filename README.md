# Resume Parser API (`resume-parser-api`)

An AI-powered Resume-to-JSON Parser Microservice built with **Java 21**, **Spring Boot 3.2+**, **Spring Web**, and Google's **Gemini AI API**.

---

## Features
- **Resume-to-JSON Parsing (`POST /api/v1/resume/parse`)**: Extracts candidate `name`, `email`, `phone`, `skills` (array of strings), and `experience` (array of objects or strings) into clean, raw JSON.
- **Markdown Fence Stripping**: Automatically cleans any markdown code fences (e.g. ```json ... ```) returned by the model.
- **Input Validation**: Rejects empty, missing, or blank resume texts with HTTP 400 and clear JSON error payloads.
- **Error Handling**: Gracefully handles exceptions and network issues by returning HTTP 500 with a structured JSON error payload.
- **Health Check (`GET /api/v1/resume/health`)**: Lightweight endpoint returning `{"status":"UP"}` for hosting platforms (such as Render, Railway, AWS ECS, Fly.io, etc.).
- **Spring `RestClient`**: Uses modern Spring Boot 3 `RestClient` to interact with Gemini API.

---

## Configuration

In `src/main/resources/application.properties`:
```properties
spring.application.name=resume-parser-api
server.port=${PORT:8080}

gemini.api.key=${GEMINI_API_KEY}
gemini.model=${GEMINI_MODEL:gemini-3.5-flash-lite}
```

You can customize the API key or model by setting environment variables:
- `GEMINI_API_KEY`: Your Gemini API key.
- `GEMINI_MODEL`: (Optional) Gemini model name (defaults to `gemini-3.5-flash-lite`).
- `PORT`: (Optional) Service listening port (defaults to `8080`).

---

## API Endpoints

### 1. Health Check
- **Method**: `GET`
- **Path**: `/api/v1/resume/health`
- **Response**: `200 OK`
```json
{
  "status": "UP"
}
```

### 2. Parse Resume
- **Method**: `POST`
- **Path**: `/api/v1/resume/parse`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "text": "Alex Morgan\nEmail: alex.morgan@example.com\nPhone: +1 (555) 019-2834\nSkills: Java, Spring Boot, REST APIs, PostgreSQL, Docker\nExperience:\n- Senior Software Engineer at FinTech Labs (2021 - Present): Led backend architecture for payment processing service.\n- Software Developer at CloudWorks (2018 - 2021): Developed microservices using Spring Boot."
}
```

- **Success Response**: `200 OK`
```json
{
  "name": "Alex Morgan",
  "email": "alex.morgan@example.com",
  "phone": "+1 (555) 019-2834",
  "skills": [
    "Java",
    "Spring Boot",
    "REST APIs",
    "PostgreSQL",
    "Docker"
  ],
  "experience": [
    {
      "role": "Senior Software Engineer",
      "company": "FinTech Labs",
      "period": "2021 - Present",
      "description": "Led backend architecture for payment processing service."
    },
    {
      "role": "Software Developer",
      "company": "CloudWorks",
      "period": "2018 - 2021",
      "description": "Developed microservices using Spring Boot."
    }
  ]
}
```

- **Validation Error**: `400 Bad Request`
```json
{
  "error": "Resume text must not be empty or blank"
}
```

- **Internal Error**: `500 Internal Server Error`
```json
{
  "error": "Failed to parse resume: <details>"
}
```

---

## Running Locally

### Prerequisites
- Java 21 JDK
- Maven (or use included `mvnw` wrapper)

### Build and Run
```bash
# Set environment variables
export GEMINI_API_KEY="your-api-key"

# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
```

The application will start on port `8080`.

---

## Running with Docker
```bash
docker build -t resume-parser-api .
docker run -p 8080:8080 -e GEMINI_API_KEY="your-api-key" resume-parser-api
```