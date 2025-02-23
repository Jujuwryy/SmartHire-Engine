# AIJobMatcher

This project implements an advanced job matching system powered by AI vector embeddings and MongoDB Atlas Vector Search. Using natural language processing (NLP) and machine learning, it transforms job postings and user profiles into semantic vectors, enabling precise, context-aware job matching with confidence scores and detailed reasoning.

## Overview

The system fetches job postings, generates embeddings using the `mixedbread-ai/mxbai-embed-large-v1` model from Hugging Face, and stores them in MongoDB Atlas. It then uses vector similarity search to match user profiles to jobs, providing ranked results with confidence scores and match explanations. Built with Spring Boot, it offers a robust RESTful API for integration.

## Key Features

- **AI-Driven Embeddings**: Leverages the `mixedbread-ai/mxbai-embed-large-v1` transformer model for high-quality semantic vector representations.
- **Vector Search**: Utilizes MongoDB Atlas Vector Search for efficient k-nearest neighbors (KNN) matching.
- **Semantic Matching**: Captures meaning beyond keywords, e.g., "Java developer" matches "Software engineer with Java skills."
- **Confidence Scoring**: Provides similarity scores (0-1) and human-readable match reasons.
- **RESTful API**: Exposes endpoints for generating embeddings and finding job matches.

## Technology Stack

- **Language**: Java
- **Framework**: Spring Boot
- **Database**: MongoDB Atlas with Vector Search
- **AI Model**: Hugging Face `mixedbread-ai/mxbai-embed-large-v1` via LangChain4J
- **Dependencies**:
  - Spring Data MongoDB
  - LangChain4J
  - MongoDB Java Driver

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

## How to Run It

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Jujuwryy/AIJobMatcher.git
   cd AIJobMatcher

