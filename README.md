# 📚 Smart Attendance API - Backend Documentation (WIP 🚧)

## ✅ Project Overview
Smart Attendance API is a backend system designed to manage educational resources like users, courses, assignments, and attendance using QR codes and geofencing.

🔗 **API Swagger Docs:**  
[Smart Attendance API Documentation](https://smartattendance-backend-bug4bxgybhbwecey.canadacentral-01.azurewebsites.net/swagger)

or

[Smart Attendnace Api Docs](https://smart-attendance-api-image-production.up.railway.app/swagger)



## 🛠️ Tech Stack
| Technology            | Purpose                                          |
|------------------------|---------------------------------------------------|
| **Kotlin**             | Backend development                              |
| **Ktor Framework**     | REST API                                         |
| **Exposed ORM**        | Database interaction                             |
| **MySQL / Azure SQL**  | Data storage                                     |
| **HikariCP**           | Connection pooling                               |
| **JWT**                | Authentication & Authorization                   |
| **Docker**             | Containerization                                 |
| **GitHub Actions**     | CI/CD pipeline                                   |
| **Azure Web Service**  | Hosting REST API                                 |
| **Azure Container Registry** | Docker image registry                      |



## 🗂️ API Endpoints
The API exposes the following endpoints:

### 🔐 **Authentication**
- `POST /auth/login` - Login & receive tokens
- `POST /auth/refresh` - Refresh tokens
- `POST /auth/signup` - Register a new user

### 👤 **Users**
- `GET /users` - Get all users
- `POST /users/create` - Create a user
- `GET /users/{id}` - Fetch a user
- `DELETE /users/delete/{id}` - Delete a user

### 📚 **Courses**
- `POST /courses/create` - Create a course
- `GET /courses` - Get all courses
- `GET /courses/{id}` - Get a course by ID
- `PUT /courses/update/{id}` - Update a course
- `DELETE /courses/delete/{id}` - Delete a course

### 📍 **Attendance**
- `POST /attendance/sessions` - Create an attendance session
- `GET /attendance/sessions/qr` - Get QR for session
- `POST /attendance/mark` - Mark student attendance

For full API documentation and schemas, visit the Swagger UI:  
🔗 [Smart Attendance Swagger Docs](https://smartattendance-backend-bug4bxgybhbwecey.canadacentral-01.azurewebsites.net/swagger)

---

## 🗄️ Database Design (ERD)
The database is designed with the following core entities:
- **Users (Admin, Lecturer, Student)**
- **Courses**
- **Attendance Sessions**
- **Submissions**
- **Assignments**
- **Grades**

📌 **ER Diagram:**  
> *Refer to the `erd/` folder or documentation files for the full ERD.*  
> (ERD image preview uploaded separately — e.g., `erd/SmartAttendanceERD.jpeg`)

*Example preview:*  
![ERD Preview](./erd/SmartAttendanceERD.jpeg)

---

## 🔒 Authentication & Authorization
- **Method:** JWT Bearer Tokens
- **Access Token Validity:** 15 minutes
- **Refresh Token Validity:** 7 days
- **Authorization:** Role-based (Admin, Lecturer, Student)
- **Failure Handling:** Returns JSON error with `401 Unauthorized`

---

## ⚙️ CI/CD Pipeline (GitHub Actions)
✅ **Continuous Integration:**
- Build & Test the application
- Run code linting
- Dockerize the application
- Push Docker image to Azure Container Registry (ACR)

✅ **Continuous Deployment:**
- Deploys to Azure Web App (Linux Container) on merge to `main`
- Azure SQL Database used for production storage

---

## ☁️ Azure Infrastructure
| Azure Service              | Purpose                                  |
|--------------------------- |-------------------------------------------|
| Azure SQL Database         | Store production data                    |
| Azure Container Registry   | Store Docker images                      |
| Azure Web App (Linux)      | Host REST API                            |
| Azure Resource Group       | Organize and manage all resources         |

---

## 🗳️ Sample Response Structure
```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```
**Error Example:**
```json
{
  "success": false,
  "error": "Invalid token"
}
```

---

## 🚀 Future Improvements (Planned)
- ✅ Add OpenAPI schema export
- ✅ Complete ERD design & publish
- Add role-based access control (RBAC)
- Add caching (Redis) for session performance
- Write unit & integration tests
- Add notification service (push/email)

---

## 📌 Project Status
✅ Core API and authentication implemented  
✅ Swagger documentation live  
🚧 Database ERD finalization in progress  
🚧 Testing & performance tuning ongoing  
🚧 UI front-end in planning phase  

---

## 🤝 Contributors
- **Backend Developer:** [Your Name]
- **Infrastructure & DevOps:** [Your Name]

---

## 📄 License
MIT License - See `LICENSE` file for details.

---
