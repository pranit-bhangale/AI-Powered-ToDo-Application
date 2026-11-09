# Sample Dataset & API Reference for AI-Powered ToDo Application

This document provides a comprehensive sample dataset in JSON format covering all REST API endpoints. Every request payload, response structure, query parameter, and business constraint is documented with realistic examples.

> **Base URL**: `http://localhost:8080`  
> **API Version**: `v1`  
> **Authentication**: Bearer Token via header: `Authorization: Bearer <JWT_ACCESS_TOKEN>`  
> **Date Reference**: Current / future dates use `2026-11-09` onwards (start dates must be today or in the future).

---

## Table of Contents
1. [Authentication Endpoints](#1-authentication-endpoints)
   - [Register User](#11-register-user)
   - [Login User](#12-login-user)
   - [Get Current User Profile](#13-get-current-user-profile)
   - [Logout User](#14-logout-user)
2. [Todo CRUD Endpoints](#2-todo-crud-endpoints)
   - [Create Todo (with Nested Tasks)](#21-create-todo-with-nested-tasks)
   - [Create Todo (without Initial Tasks)](#22-create-todo-without-initial-tasks)
   - [Get All Todos (Paginated & Security-Context Tenant Isolated)](#23-get-all-todos-paginated)
   - [Get Todo by ID](#24-get-todo-by-id)
   - [Update Todo](#25-update-todo)
   - [Toggle Todo Completion](#26-toggle-todo-completion)
   - [Delete Todo (Cascade Tasks)](#27-delete-todo-cascade-tasks)
3. [Task CRUD Endpoints](#3-task-crud-endpoints)
   - [Create Task under Todo](#31-create-task-under-todo)
   - [Get All Tasks for a Todo (Paginated)](#32-get-all-tasks-for-a-todo-paginated)
   - [Get Task by ID](#33-get-task-by-id)
   - [Update Task](#34-update-task)
   - [Toggle Task Completion](#35-toggle-task-completion)
   - [Delete Task](#36-delete-task)
4. [Full Sample Scenario Dataset (Ready for Bulk Ingestion / Postman)](#4-full-sample-scenario-dataset)
5. [Business Rules & Validation Error Responses](#5-business-rules--validation-error-responses)
   - [Past Start Date Error](#51-past-start-date-error)
   - [Immutable Start Date Update Error](#52-immutable-start-date-update-error)
   - [Todo Completion with Incomplete Tasks Error](#53-todo-completion-with-incomplete-tasks-error)
   - [Todo Deletion with Incomplete Tasks Error](#54-todo-deletion-with-incomplete-tasks-error)
   - [Duplicate Title Error](#55-duplicate-title-error)

---

## 1. Authentication Endpoints

### 1.1 Register User
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Headers**: `Content-Type: application/json`

#### Request Body
```json
{
  "username": "alex_developer",
  "email": "alex.developer@example.com",
  "password": "StrongPassword123!",
  "confirmPassword": "StrongPassword123!"
}
```

#### Response (`201 Created`)
```json
{
  "message": "Account created successfully. Please log in to continue."
}
```

---

### 1.2 Login User
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Headers**: `Content-Type: application/json`

#### Request Body
```json
{
  "username": "alex_developer",
  "password": "StrongPassword123!"
}
```

#### Response (`200 OK`)
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbGV4X2RldmVsb3BlciIsImlkIjoiZjRhYjFkMmUtM2M0NS00Y2E4LWExZjMtYzgxNmQxY2UxYWI1Iiwicm9sZXMiOlsicm9sZV91c2VyIl0sImV4cCI6MTc5NDIxNzYwMH0...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 1.3 Get Current User Profile
- **Method**: `GET`
- **URL**: `/api/auth/me`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### Response (`200 OK`)
```json
{
  "id": "f4ab1d2e-3c45-4ca8-a1f3-c816d1ce1ab5",
  "username": "alex_developer",
  "email": "alex.developer@example.com",
  "roles": [
    "ROLE_USER"
  ]
}
```

---

### 1.4 Logout User
- **Method**: `POST`
- **URL**: `/api/auth/logout`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### Response (`204 No Content`)
*(Empty response body)*

---

## 2. Todo CRUD Endpoints

### 2.1 Create Todo (with Nested Tasks)
- **Method**: `POST`
- **URL**: `/api/todos`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <TOKEN>`

#### Request Body
```json
{
  "title": "Release AI Todo Application v1.0",
  "description": "Production release milestones including security audit, load tests, and deployment.",
  "startDate": "2026-11-09",
  "endDate": "2026-11-20",
  "tasks": [
    {
      "title": "Complete OWASP Security Audit",
      "description": "Scan endpoints for injection vulnerabilities and verify JWT signature verification.",
      "startDate": "2026-11-09",
      "endDate": "2026-11-12"
    },
    {
      "title": "Perform K6 Load & Stress Testing",
      "description": "Benchmark paginated Todo and Task queries under 500 concurrent virtual users.",
      "startDate": "2026-11-12",
      "endDate": "2026-11-15"
    },
    {
      "title": "Execute Flyway Production Migrations",
      "description": "Apply V1 schema migration to managed PostgreSQL instance and verify indexes.",
      "startDate": "2026-11-15",
      "endDate": "2026-11-16"
    }
  ]
}
```

#### Response (`201 Created`)
**Header**: `Location: /api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6`
```json
{
  "id": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Release AI Todo Application v1.0",
  "description": "Production release milestones including security audit, load tests, and deployment.",
  "completed": false,
  "startDate": "2026-11-09",
  "endDate": "2026-11-20",
  "totalTasks": 3,
  "completedTasks": 0,
  "tasks": [
    {
      "id": "18f50435-081e-4cb8-8c17-1f4ae8b7f781",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Complete OWASP Security Audit",
      "description": "Scan endpoints for injection vulnerabilities and verify JWT signature verification.",
      "completed": false,
      "startDate": "2026-11-09",
      "endDate": "2026-11-12",
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T09:30:00Z"
    },
    {
      "id": "27c9d921-2ef4-4f24-9b22-8326a09efbf4",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Perform K6 Load & Stress Testing",
      "description": "Benchmark paginated Todo and Task queries under 500 concurrent virtual users.",
      "completed": false,
      "startDate": "2026-11-12",
      "endDate": "2026-11-15",
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T09:30:00Z"
    },
    {
      "id": "3bb62c72-9844-4861-a068-d0554efc85a2",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Execute Flyway Production Migrations",
      "description": "Apply V1 schema migration to managed PostgreSQL instance and verify indexes.",
      "completed": false,
      "startDate": "2026-11-15",
      "endDate": "2026-11-16",
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T09:30:00Z"
    }
  ],
  "createdAt": "2026-11-09T09:30:00Z",
  "updatedAt": "2026-11-09T09:30:00Z"
}
```

---

### 2.2 Create Todo (without Initial Tasks)
- **Method**: `POST`
- **URL**: `/api/todos`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <TOKEN>`

#### Request Body
```json
{
  "title": "Cloud Architecture Review",
  "description": "Review Kubernetes deployment manifests and Helm chart values.",
  "startDate": "2026-11-10",
  "endDate": "2026-11-14"
}
```

#### Response (`201 Created`)
```json
{
  "id": "c138b301-447a-4a25-83e9-fb823a078d12",
  "title": "Cloud Architecture Review",
  "description": "Review Kubernetes deployment manifests and Helm chart values.",
  "completed": false,
  "startDate": "2026-11-10",
  "endDate": "2026-11-14",
  "totalTasks": 0,
  "completedTasks": 0,
  "tasks": [],
  "createdAt": "2026-11-09T09:35:00Z",
  "updatedAt": "2026-11-09T09:35:00Z"
}
```

---

### 2.3 Get All Todos (Paginated)
- **Method**: `GET`
- **URL**: `/api/todos?page=0&size=10&sort=createdAt,desc`
- **Headers**: `Authorization: Bearer <TOKEN>`
- *Note: User identity is extracted automatically from the security context token.*

#### Query Parameters
| Parameter | Type | Required | Default | Allowed Values |
| :--- | :--- | :--- | :--- | :--- |
| `page` | Integer | No | `0` | `0` or greater |
| `size` | Integer | No | `10` | `1` to `100` |
| `sort` | String | No | `createdAt,desc` | `createdAt`, `updatedAt`, `title`, `completed`, `startDate`, `endDate` (`asc`/`desc`) |

#### Response (`200 OK`)
```json
{
  "contents": [
    {
      "id": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Release AI Todo Application v1.0",
      "description": "Production release milestones including security audit, load tests, and deployment.",
      "completed": false,
      "startDate": "2026-11-09",
      "endDate": "2026-11-20",
      "totalTasks": 3,
      "completedTasks": 0,
      "tasks": [
        {
          "id": "18f50435-081e-4cb8-8c17-1f4ae8b7f781",
          "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
          "title": "Complete OWASP Security Audit",
          "description": "Scan endpoints for injection vulnerabilities and verify JWT signature verification.",
          "completed": false,
          "startDate": "2026-11-09",
          "endDate": "2026-11-12",
          "createdAt": "2026-11-09T09:30:00Z",
          "updatedAt": "2026-11-09T09:30:00Z"
        }
      ],
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T09:30:00Z"
    },
    {
      "id": "c138b301-447a-4a25-83e9-fb823a078d12",
      "title": "Cloud Architecture Review",
      "description": "Review Kubernetes deployment manifests and Helm chart values.",
      "completed": false,
      "startDate": "2026-11-10",
      "endDate": "2026-11-14",
      "totalTasks": 0,
      "completedTasks": 0,
      "tasks": [],
      "createdAt": "2026-11-09T09:35:00Z",
      "updatedAt": "2026-11-09T09:35:00Z"
    }
  ],
  "currentPage": 0,
  "pageSize": 10,
  "totalElements": 2,
  "totalPages": 1,
  "isFirstPage": true,
  "isLastPage": true,
  "hasNext": false,
  "hasPrevious": false
}
```

---

### 2.4 Get Todo by ID
- **Method**: `GET`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### Response (`200 OK`)
```json
{
  "id": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Release AI Todo Application v1.0",
  "description": "Production release milestones including security audit, load tests, and deployment.",
  "completed": false,
  "startDate": "2026-11-09",
  "endDate": "2026-11-20",
  "totalTasks": 3,
  "completedTasks": 0,
  "tasks": [
    {
      "id": "18f50435-081e-4cb8-8c17-1f4ae8b7f781",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Complete OWASP Security Audit",
      "description": "Scan endpoints for injection vulnerabilities and verify JWT signature verification.",
      "completed": false,
      "startDate": "2026-11-09",
      "endDate": "2026-11-12",
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T09:30:00Z"
    }
  ],
  "createdAt": "2026-11-09T09:30:00Z",
  "updatedAt": "2026-11-09T09:30:00Z"
}
```

---

### 2.5 Update Todo
- **Method**: `PUT`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <TOKEN>`

> **Rule**: `startDate` is strictly immutable. Omit `startDate` or pass the exact existing start date.

#### Request Body
```json
{
  "title": "Release AI Todo Application v1.0 [Final Polish]",
  "description": "Final release sprint with updated acceptance criteria and extended sign-off date.",
  "endDate": "2026-11-25",
  "tasks": [
    {
      "title": "Complete OWASP Security Audit",
      "description": "Audit completed and verified by security team.",
      "completed": true,
      "endDate": "2026-11-11"
    }
  ]
}
```

#### Response (`200 OK`)
```json
{
  "id": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Release AI Todo Application v1.0 [Final Polish]",
  "description": "Final release sprint with updated acceptance criteria and extended sign-off date.",
  "completed": false,
  "startDate": "2026-11-09",
  "endDate": "2026-11-25",
  "totalTasks": 3,
  "completedTasks": 1,
  "tasks": [
    {
      "id": "18f50435-081e-4cb8-8c17-1f4ae8b7f781",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Complete OWASP Security Audit",
      "description": "Audit completed and verified by security team.",
      "completed": true,
      "startDate": "2026-11-09",
      "endDate": "2026-11-11",
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T10:15:00Z"
    }
  ],
  "createdAt": "2026-11-09T09:30:00Z",
  "updatedAt": "2026-11-09T10:15:00Z"
}
```

---

### 2.6 Toggle Todo Completion
- **Method**: `PATCH`
- **URL**: `/api/todos/c138b301-447a-4a25-83e9-fb823a078d12/toggle`
- **Headers**: `Authorization: Bearer <TOKEN>`

> **Rule**: If a Todo has tasks, all tasks must be completed first. For a Todo with 0 tasks or all completed tasks, toggle succeeds.

#### Response (`200 OK`)
```json
{
  "id": "c138b301-447a-4a25-83e9-fb823a078d12",
  "title": "Cloud Architecture Review",
  "description": "Review Kubernetes deployment manifests and Helm chart values.",
  "completed": true,
  "startDate": "2026-11-10",
  "endDate": "2026-11-14",
  "totalTasks": 0,
  "completedTasks": 0,
  "tasks": [],
  "createdAt": "2026-11-09T09:35:00Z",
  "updatedAt": "2026-11-09T10:20:00Z"
}
```

---

### 2.7 Delete Todo (Cascade Tasks)
- **Method**: `DELETE`
- **URL**: `/api/todos/c138b301-447a-4a25-83e9-fb823a078d12`
- **Headers**: `Authorization: Bearer <TOKEN>`

> **Rule**: A Todo cannot be deleted if any associated task is incomplete. Once all tasks are completed, deleting cascades and removes all related tasks.

#### Response (`204 No Content`)
*(Empty response body)*

---

## 3. Task CRUD Endpoints

### 3.1 Create Task under Todo
- **Method**: `POST`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <TOKEN>`

#### Request Body
```json
{
  "title": "Write API Documentation in OpenAPI 3.0",
  "description": "Add Swagger schema definitions, response codes, and authentication requirements.",
  "startDate": "2026-11-10",
  "endDate": "2026-11-13"
}
```

#### Response (`201 Created`)
**Header**: `Location: /api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks/5e4299b1-561b-4d43-a602-dc5e1e813872`
```json
{
  "id": "5e4299b1-561b-4d43-a602-dc5e1e813872",
  "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Write API Documentation in OpenAPI 3.0",
  "description": "Add Swagger schema definitions, response codes, and authentication requirements.",
  "completed": false,
  "startDate": "2026-11-10",
  "endDate": "2026-11-13",
  "createdAt": "2026-11-09T10:30:00Z",
  "updatedAt": "2026-11-09T10:30:00Z"
}
```

---

### 3.2 Get All Tasks for a Todo (Paginated)
- **Method**: `GET`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks?page=0&size=5&sort=startDate,asc`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### Response (`200 OK`)
```json
{
  "contents": [
    {
      "id": "18f50435-081e-4cb8-8c17-1f4ae8b7f781",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Complete OWASP Security Audit",
      "description": "Scan endpoints for injection vulnerabilities and verify JWT signature verification.",
      "completed": true,
      "startDate": "2026-11-09",
      "endDate": "2026-11-11",
      "createdAt": "2026-11-09T09:30:00Z",
      "updatedAt": "2026-11-09T10:15:00Z"
    },
    {
      "id": "5e4299b1-561b-4d43-a602-dc5e1e813872",
      "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
      "title": "Write API Documentation in OpenAPI 3.0",
      "description": "Add Swagger schema definitions, response codes, and authentication requirements.",
      "completed": false,
      "startDate": "2026-11-10",
      "endDate": "2026-11-13",
      "createdAt": "2026-11-09T10:30:00Z",
      "updatedAt": "2026-11-09T10:30:00Z"
    }
  ],
  "currentPage": 0,
  "pageSize": 5,
  "totalElements": 2,
  "totalPages": 1,
  "isFirstPage": true,
  "isLastPage": true,
  "hasNext": false,
  "hasPrevious": false
}
```

---

### 3.3 Get Task by ID
- **Method**: `GET`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks/5e4299b1-561b-4d43-a602-dc5e1e813872`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### Response (`200 OK`)
```json
{
  "id": "5e4299b1-561b-4d43-a602-dc5e1e813872",
  "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Write API Documentation in OpenAPI 3.0",
  "description": "Add Swagger schema definitions, response codes, and authentication requirements.",
  "completed": false,
  "startDate": "2026-11-10",
  "endDate": "2026-11-13",
  "createdAt": "2026-11-09T10:30:00Z",
  "updatedAt": "2026-11-09T10:30:00Z"
}
```

---

### 3.4 Update Task
- **Method**: `PUT`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks/5e4299b1-561b-4d43-a602-dc5e1e813872`
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <TOKEN>`

> **Rule**: `startDate` is immutable. Modifying it returns `400 Bad Request`.

#### Request Body
```json
{
  "title": "Write Comprehensive OpenAPI 3.0 & Postman Docs",
  "description": "Documentation completed with sample requests, schemas, and curl scripts.",
  "completed": true,
  "endDate": "2026-11-14"
}
```

#### Response (`200 OK`)
```json
{
  "id": "5e4299b1-561b-4d43-a602-dc5e1e813872",
  "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Write Comprehensive OpenAPI 3.0 & Postman Docs",
  "description": "Documentation completed with sample requests, schemas, and curl scripts.",
  "completed": true,
  "startDate": "2026-11-10",
  "endDate": "2026-11-14",
  "createdAt": "2026-11-09T10:30:00Z",
  "updatedAt": "2026-11-09T11:00:00Z"
}
```

---

### 3.5 Toggle Task Completion
- **Method**: `PATCH`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks/5e4299b1-561b-4d43-a602-dc5e1e813872/toggle`
- **Headers**: `Authorization: Bearer <TOKEN>`

> **Rule**: Toggling a completed task back to incomplete automatically reopens the parent Todo if it was completed.

#### Response (`200 OK`)
```json
{
  "id": "5e4299b1-561b-4d43-a602-dc5e1e813872",
  "todoId": "9e5c7078-4389-4e78-a720-3024c084f7b6",
  "title": "Write Comprehensive OpenAPI 3.0 & Postman Docs",
  "description": "Documentation completed with sample requests, schemas, and curl scripts.",
  "completed": false,
  "startDate": "2026-11-10",
  "endDate": "2026-11-14",
  "createdAt": "2026-11-09T10:30:00Z",
  "updatedAt": "2026-11-09T11:15:00Z"
}
```

---

### 3.6 Delete Task
- **Method**: `DELETE`
- **URL**: `/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/tasks/5e4299b1-561b-4d43-a602-dc5e1e813872`
- **Headers**: `Authorization: Bearer <TOKEN>`

#### Response (`204 No Content`)
*(Empty response body)*

---

## 4. Full Sample Scenario Dataset

Below are 5 ready-to-use JSON payloads for creating realistic Todos across different domains.

### Scenario 1: Software Sprint Milestone (Engineering)
```json
{
  "title": "Q4 Payment Gateway Integration",
  "description": "Implement Stripe and PayPal webhooks with idempotent transaction handling.",
  "startDate": "2026-11-10",
  "endDate": "2026-11-28",
  "tasks": [
    {
      "title": "Implement Stripe Signature Verification",
      "description": "Verify stripe-signature header against raw webhook payload.",
      "startDate": "2026-11-10",
      "endDate": "2026-11-14"
    },
    {
      "title": "Design Idempotent Event Repository",
      "description": "Store processed event IDs with a unique constraint to avoid double charges.",
      "startDate": "2026-11-14",
      "endDate": "2026-11-18"
    },
    {
      "title": "End-to-End Automated Checkout Tests",
      "description": "Verify successful checkout, card decline, and refund events.",
      "startDate": "2026-11-18",
      "endDate": "2026-11-25"
    }
  ]
}
```

### Scenario 2: Cloud Infrastructure & DevOps
```json
{
  "title": "Production Kubernetes Cluster Migration",
  "description": "Migrate microservices from single-node Docker Compose to managed Amazon EKS.",
  "startDate": "2026-11-12",
  "endDate": "2026-11-30",
  "tasks": [
    {
      "title": "Provision VPC & EKS Cluster via Terraform",
      "description": "Setup private subnets, NAT gateways, and IAM role associations.",
      "startDate": "2026-11-12",
      "endDate": "2026-11-16"
    },
    {
      "title": "Deploy Cert-Manager & Ingress NGINX",
      "description": "Automate TLS certificate issuance via Let's Encrypt ACME resolver.",
      "startDate": "2026-11-16",
      "endDate": "2026-11-20"
    },
    {
      "title": "Configure Prometheus & Grafana Alerts",
      "description": "Setup Slack notifications for pod crash loops and 5xx rate spikes.",
      "startDate": "2026-11-20",
      "endDate": "2026-11-24"
    }
  ]
}
```

### Scenario 3: Security & Compliance
```json
{
  "title": "SOC2 Type II Compliance Audit",
  "description": "Gather system evidence, access reviews, and policy documentation for auditor review.",
  "startDate": "2026-11-15",
  "endDate": "2026-12-15",
  "tasks": [
    {
      "title": "Quarterly User Access Review",
      "description": "Revoke stale AWS IAM credentials and GitHub repository permissions.",
      "startDate": "2026-11-15",
      "endDate": "2026-11-20"
    },
    {
      "title": "Automate Database Backup Encryption Audit",
      "description": "Verify all PostgreSQL backups use AES-256 KMS customer-managed keys.",
      "startDate": "2026-11-20",
      "endDate": "2026-11-25"
    }
  ]
}
```

### Scenario 4: Learning & Skill Development
```json
{
  "title": "Master Spring Boot 4 & Spring AI",
  "description": "Build agentic applications using MCP tools, OpenAI models, and vector stores.",
  "startDate": "2026-11-09",
  "endDate": "2026-11-30",
  "tasks": [
    {
      "title": "Study Model Context Protocol (MCP)",
      "description": "Understand STDIO and SSE transports for client-server tool execution.",
      "startDate": "2026-11-09",
      "endDate": "2026-11-15"
    },
    {
      "title": "Implement RAG with PgVector",
      "description": "Store document embeddings and query top-k similar chunks using cosine distance.",
      "startDate": "2026-11-15",
      "endDate": "2026-11-22"
    }
  ]
}
```

### Scenario 5: Simple Quick Task (No Initial Nested Tasks)
```json
{
  "title": "Renew Application SSL Certificates",
  "description": "Verify wildcard certificates for api.domain.com before November 30.",
  "startDate": "2026-11-10",
  "endDate": "2026-11-12"
}
```

---

## 5. Business Rules & Validation Error Responses

### 5.1 Past Start Date Error
- **Trigger**: Attempting to create a Todo or Task with a `startDate` in the past (e.g. `2025-01-01`).
- **Response**: `400 Bad Request`
```json
{
  "status": 400,
  "message": "Start date cannot be in the past. Please select today or a future date.",
  "path": "/api/todos",
  "timestamp": "2026-11-09T12:00:00Z"
}
```

---

### 5.2 Immutable Start Date Update Error
- **Trigger**: Submitting a `PUT` request with a modified `startDate`.
- **Response**: `400 Bad Request`
```json
{
  "status": 400,
  "message": "Start date is immutable and cannot be updated.",
  "path": "/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6",
  "timestamp": "2026-11-09T12:05:00Z"
}
```

---

### 5.3 Todo Completion with Incomplete Tasks Error
- **Trigger**: Attempting to toggle or update a Todo to `completed: true` when any nested task has `completed: false`.
- **Response**: `400 Bad Request`
```json
{
  "status": 400,
  "message": "Cannot complete Todo: all tasks in this todo must be completed first.",
  "path": "/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6/toggle",
  "timestamp": "2026-11-09T12:10:00Z"
}
```

---

### 5.4 Todo Deletion with Incomplete Tasks Error
- **Trigger**: Attempting to `DELETE /api/todos/{id}` when any associated task is not yet completed.
- **Response**: `400 Bad Request`
```json
{
  "status": 400,
  "message": "Cannot delete Todo: all associated tasks must be completed before deleting this Todo.",
  "path": "/api/todos/9e5c7078-4389-4e78-a720-3024c084f7b6",
  "timestamp": "2026-11-09T12:15:00Z"
}
```

---

### 5.5 Duplicate Title Error
- **Trigger**: Creating or updating a Todo or Task with a title that already exists in the same scope.
- **Response**: `409 Conflict`
```json
{
  "status": 409,
  "message": "Todo with title 'Release AI Todo Application v1.0' already exists.",
  "path": "/api/todos",
  "timestamp": "2026-11-09T12:20:00Z"
}
```
