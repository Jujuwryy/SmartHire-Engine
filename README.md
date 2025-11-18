# SmartHire-Engine

This project implements an advanced job matching system powered by AI vector embeddings and MongoDB Atlas Vector Search. Using natural language processing (NLP) and machine learning, it transforms job postings and user profiles into semantic vectors, enabling precise, context-aware job matching with confidence scores and detailed reasoning.

## Overview

The system fetches job postings, generates embeddings using the `mixedbread-ai/mxbai-embed-large-v1` model from Hugging Face, and stores them in MongoDB Atlas. It then uses vector similarity search to match user profiles to jobs, providing ranked results with confidence scores and match explanations. Built with Spring Boot, it offers a robust RESTful API for integration.

## Key Features

- **AI-Driven Embeddings**: Leverages the `mixedbread-ai/mxbai-embed-large-v1` transformer model for high-quality semantic vector representations.
- **Vector Search**: Utilizes MongoDB Atlas Vector Search for efficient k-nearest neighbors (KNN) matching.
- **Semantic Matching**: Captures meaning beyond keywords, e.g., "Java developer" matches "Software engineer with Java skills."
- **Confidence Scoring**: Provides similarity scores (0-1) and human-readable match reasons with detailed explanations.
- **RESTful API**: Comprehensive RESTful API with Swagger/OpenAPI documentation.
- **Caching**: Intelligent caching layer for embeddings and query results to improve performance.
- **Monitoring**: Built-in metrics and health checks via Spring Boot Actuator.
- **Error Handling**: Comprehensive error handling with structured error responses.
- **Validation**: Request validation with detailed error messages.
- **Pagination & Filtering**: Advanced filtering options including confidence thresholds and result limits.

## Technology Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.2.0
- **Database**: MongoDB Atlas with Vector Search
- **AI Model**: Hugging Face `mixedbread-ai/mxbai-embed-large-v1` via LangChain4J
- **API Documentation**: Swagger/OpenAPI 3.0
- **Caching**: Caffeine
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus
- **Dependencies**:
  - Spring Data MongoDB
  - Spring Boot Web
  - Spring Boot Validation
  - LangChain4J
  - MongoDB Java Driver
  - SpringDoc OpenAPI
  - Caffeine Cache

## Workflow

### 1. Embedding Generation and Storage
1. **Trigger**: An HTTP GET request to `/generate-embeddings` initiates the process.
2. **Fetch Posts**: Retrieves all job postings from a repository (assumed to be pre-populated).
3. **Generate Embeddings**:
   - Job descriptions are extracted and sent to the `mixedbread-ai/mxbai-embed-large-v1` model.
   - The model tokenizes the text, processes it through transformer layers, and outputs 1024-dimensional vectors.
   - Vectors are converted to `BsonArray` format for MongoDB compatibility.
4. **Store**: Inserts documents (job details + embeddings) into the `JobPost` collection in MongoDB Atlas `sample_db`.

### 2. Job Matching
1. **Trigger**: An HTTP POST request to `/jobs/match` with a user profile string (e.g., "Experienced Java developer").
2. **Generate User Embedding**:
   - The user profile is processed by the same Hugging Face model to create a vector.
3. **Vector Search**:
   - MongoDB Atlas performs a KNN search using the user embedding against job embeddings.
   - Returns the top X matches, in this configuration, (10) with cosine similarity scores.
4. **Process Results**:
   - Confidence scores are set from MongoDB’s `searchScore` (0-1).
   - Match reasons are generated based on score thresholds (>0.8 = "very strong", >0.6 = "good") and tech keyword overlaps.
5. **Return**: Delivers a list of `JobMatch` objects as JSON.


## Prerequisites

- **Java 17+**: Required to run the Spring Boot application.
- **MongoDB Atlas**: A cluster with Vector Search enabled.
  - Create a vector index named `vector_index` on the `JobPost.embedding` field.
- **Hugging Face Account**: For API access to the `mixedbread-ai/mxbai-embed-large-v1` model.
- **Maven**: To build and manage dependencies.
- **Environment Variables**:
  - `ATLAS_CONNECTION_STRING`: MongoDB Atlas connection string (e.g., `mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/`).
  - `HUGGING_FACE_ACCESS_TOKEN`: Your Hugging Face API token.

## API Endpoints

### Generate Embeddings
- **GET** `/api/v1/vectors/generate`
  - Generates embeddings for all job posts in the repository
  - Returns success message upon completion

### Find Matching Jobs
- **POST** `/api/v1/vectors/jobs/match`
  - Request body: `JobMatchRequest` (JSON)
    ```json
    {
      "userProfile": "Experienced Java developer with 5 years of experience",
      "limit": 10,
      "minConfidence": 0.6,
      "preferredTechs": ["Java", "Spring"],
      "location": "Remote",
      "maxExperience": 10
    }
    ```
  - Returns: `JobMatchResponse` with matching jobs, confidence scores, and match reasons

- **POST** `/api/v1/vectors/jobs/match/simple`
  - Simplified endpoint accepting plain text user profile
  - Request body: `"Experienced Java developer"`
  - Returns: `JobMatchResponse`

### Health Check
- **GET** `/api/v1/health`
  - Returns application health status

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## How to Run It

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Jujuwryy/AIJobMatcher.git
   cd AIJobMatcher
   ```

2. **Set Environment Variables**:
   ```bash
   export ATLAS_CONNECTION_STRING="mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/"
   export HUGGING_FACE_ACCESS_TOKEN="your_hf_token_here"
   ```

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

5. **Access the API**:
   - API Base URL: `http://localhost:8080/api/v1`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Health Check: `http://localhost:8080/api/v1/health`
   - Metrics: `http://localhost:8080/actuator/metrics`

