# API Documentation - VERA Survey Platform

# API Documentation - VERA Survey Platform

## Overview

VERA is a high-performance survey and quiz management platform built with modern Java technologies. The API provides
comprehensive endpoints for creating, managing, and analyzing surveys and quizzes with real-time analytics and
AI-powered features.

### Technical Stack

- **Framework**: Jersey REST (4.0.0) with Grizzly HTTP server
- **Java Version**: 24 (latest LTS)
- **Database**: PostgreSQL with HikariCP connection pooling
- **Security**: JWT authentication with Argon2 password hashing
- **Documentation**: OpenAPI 3.0 with Swagger UI
- **Monitoring**: Dropwizard Metrics for performance tracking
- **Logging**: Log4j2 with SLF4J bridge

### Core Features

- **Survey Management**: Create, publish, and manage surveys with flexible question types
- **Quiz System**: Built-in quiz functionality with scoring and leaderboards
- **Real-time Analytics**: Comprehensive response analysis and statistics
- **AI-Powered Anonymization**: Automatic data anonymization with configurable levels
- **Version Control**: Complete audit trail with survey and question history
- **Public Sharing**: Secure token-based access for public surveys
- **Data Export**: Multi-format export capabilities (CSV, JSON, Excel)

### API Specifications

- **Base URL**: `http://localhost:8080/api`
- **Authentication**: JWT Bearer tokens
- **Data Format**: JSON for all requests and responses

### Database Schema

The platform uses a robust PostgreSQL database with:

- User management with role-based access control
- Flexible survey and question structures
- Comprehensive response tracking
- Full audit history for compliance
- Optimized indexes for high performance

### Security Features

- Argon2 password hashing for maximum security
- JWT-based stateless authentication
- Role-based authorization (User, Owner, Admin)
- Configurable data anonymization levels
- Secure public sharing with unique tokens

---

## Authentication Endpoints

### Login

- **Endpoint:** `POST /api/auth/login`
- **Description:** Authenticate user and return JWT token
- **Request Body:** `{email: string, password: string}`
- **Response:** JWT token with user claims

### Logout

- **Endpoint:** `POST /api/auth/logout`
- **Description:** Invalidate user session
- **Status:** TODO - IN PROGRESS

### Registration

- **Endpoint:** `POST /api/auth/register`
- **Description:** Create new user account
- **Request Body:** `{email: string, password: string, name: string, surname: string}`

### Token Refresh

- **Endpoint:** `POST /api/auth/refresh`
- **Description:** Refresh expired JWT token

### Password Management

- **Endpoint:** `GET /api/auth/reset`
- **Description:** Reset user password

- **Endpoint:** `GET /api/auth/forgot`
- **Description:** Initiate password recovery process
- **Status:** TODO - IN PROGRESS

---

## User Management

### List Users

- **Endpoint:** `GET /api/users`
- **Description:** Retrieve all users (Admin only)
- **Access:** Admin

### Create User

- **Endpoint:** `POST /api/users`
- **Description:** Create new user account
- **Access:** Admin

### Get User

- **Endpoint:** `GET /api/users/{id}`
- **Description:** Retrieve user details
- **Access:** Admin & Owner

### Update User

- **Endpoint:** `PUT /api/users/{id}`
- **Description:** Modify user information
- **Access:** Admin & Owner

### Delete User

- **Endpoint:** `DELETE /api/users/{id}`
- **Description:** Remove user account
- **Access:** Admin

### Find User by Email

- **Endpoint:** `GET /api/users/email`
- **Description:** Search user by email address
- **Access:** Admin

### User Statistics

- **Endpoint:** `PUT /api/users/count`
- **Description:** Get total user count
- **Access:** Admin

### Role Management

- **Endpoint:** `PUT /api/users/:id/role`
- **Description:** Modify user role permissions
- **Access:** Admin
- **Status:** TODO

---

## Survey Management

### List Surveys

- **Endpoint:** `GET /api/surveys`
- **Description:** Retrieve user's surveys
- **Status:** TODO

### Create Survey

- **Endpoint:** `POST /api/surveys`
- **Description:** Create new survey or quiz
- **Status:** TODO

### Get Survey Details

- **Endpoint:** `GET /api/surveys/:id`
- **Description:** Retrieve survey details and configuration
- **Status:** TODO

### Update Survey

- **Endpoint:** `PUT /api/surveys/:id`
- **Description:** Modify survey properties
- **Status:** TODO

### Delete Survey

- **Endpoint:** `DELETE /api/surveys/:id`
- **Description:** Remove survey and associated data
- **Status:** TODO

### Publish Survey

- **Endpoint:** `POST /api/surveys/:id/publish`
- **Description:** Make survey active and available for responses
- **Status:** TODO

### Unpublish Survey

- **Endpoint:** `POST /api/surveys/:id/unpublish`
- **Description:** Deactivate survey and stop accepting responses
- **Status:** TODO

### Clone Survey

- **Endpoint:** `POST /api/surveys/:id/clone`
- **Description:** Duplicate survey with all questions
- **Status:** TODO

---

## Question Management

### List Questions

- **Endpoint:** `GET /api/surveys/:id/questions`
- **Description:** Retrieve all questions for a survey
- **Status:** TODO

### Add Question

- **Endpoint:** `POST /api/surveys/:id/questions`
- **Description:** Create new question in survey
- **Status:** TODO

### Update Question

- **Endpoint:** `PUT /api/questions/:id`
- **Description:** Modify question content and settings
- **Status:** TODO

### Delete Question

- **Endpoint:** `DELETE /api/questions/:id`
- **Description:** Remove question from survey
- **Status:** TODO

### Reorder Questions

- **Endpoint:** `PUT /api/surveys/:id/questions/reorder`
- **Description:** Update question display order
- **Status:** TODO

---

## Response Management

### Submit Responses

- **Endpoint:** `POST /api/surveys/:id/responses`
- **Description:** Submit survey or quiz responses
- **Status:** TODO

### View Responses

- **Endpoint:** `GET /api/surveys/:id/responses`
- **Description:** Retrieve all responses for analysis
- **Access:** Survey Owner
- **Status:** TODO

### Response Analytics

- **Endpoint:** `GET /api/surveys/:id/responses/analytics`
- **Description:** Get statistical analysis of responses
- **Status:** TODO

### Export Data

- **Endpoint:** `GET /api/surveys/:id/responses/export`
- **Description:** Export responses in various formats (CSV, JSON, Excel)
- **Status:** TODO

---

## Quiz Features

### Quiz Results

- **Endpoint:** `GET /api/quiz/:id/results`
- **Description:** Detailed quiz results and scoring
- **Status:** TODO

### Leaderboard

- **Endpoint:** `GET /api/quiz/:id/leaderboard`
- **Description:** Participant rankings and scores
- **Status:** TODO

### Quiz Correction

- **Endpoint:** `GET /api/quiz/:id/correction`
- **Description:** Detailed answer correction and explanations
- **Status:** TODO

---

## History & Versioning

### Survey History

- **Endpoint:** `GET /api/surveys/:id/history`
- **Description:** View modification history and versions
- **Status:** TODO

### Restore Version

- **Endpoint:** `POST /api/surveys/:id/history/:id/restore`
- **Description:** Revert to previous survey version
- **Status:** TODO

---

## Sharing & Access Control

### Public Access

- **Endpoint:** `GET /api/surveys/public/:token`
- **Description:** Access survey via share token
- **Status:** TODO

### Generate Share Link

- **Endpoint:** `POST /api/surveys/:id/share`
- **Description:** Create public sharing link
- **Status:** TODO

---

## Administration

### All Surveys

- **Endpoint:** `GET /api/admin/surveys`
- **Description:** Retrieve all platform surveys
- **Access:** Admin
- **Status:** TODO

### API Health Check

- **Endpoint:** `GET /api/admin/health`
- **Description:** System health and status monitoring
- **Access:** Admin

### Platform Metrics

- **Endpoint:** `GET /api/admin/metric`
- **Description:** Global platform statistics and analytics
- **Access:** Admin

### API Documentation

- **Endpoint:** `GET /api/swagger-ui`
- **Description:** Interactive API documentation
- **Access:** Public

### OpenAPI JSON

- **Endpoint:** `GET /api/openapi.json`
- **Description:** Download OpenAPI specification (JSON)
- **Access:** Public

### OpenAPI YAML

- **Endpoint:** `GET /api/openapi.yaml`
- **Description:** Download OpenAPI specification (YAML)
- **Access:** Public

---

## Access Levels

- **Public:** No authentication required
- **User:** Authenticated platform user
- **Owner:** Creator of the resource
- **Admin:** System administrator with full access