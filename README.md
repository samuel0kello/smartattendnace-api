# 📚 Smart Attendance API (WIP 🚧)

## Overview
Smart Attendance API is a backend system designed to manage educational resources such as users, courses, assignments, and attendance using QR codes and geofencing.  

For technical details, see:
- [Database Design](./docs/db.md)
- [System Architecture](./docs/architecture.md)

🔗 **API Swagger Docs:**  
[Smart Attendance API Documentation](https://smartattendance-backend-bug4bxgybhbwecey.canadacentral-01.azurewebsites.net/swagger)  
[Alternative Swagger URL](https://smart-attendance-api-image-production.up.railway.app/swagger)

---

## Milestones

### ✅ Completed
- Initial project scaffolding and layered structure.  
- Database schema defined and documented.  
- Authentication and role setup.  
- Core documentation (`db.md`, `architecture.md`).  

### 🚧 In Progress
- Refining Exposed-based persistence layer.  
- Enhancing error handling and response structure.  
- Adding unit & integration tests.  

### 🔮 Planned
- **CI/CD Pipeline (GitHub Actions)**  
  - Build & test application.  
  - Run code linting.  
  - Dockerize the application.  
  - Push Docker image to Azure Container Registry (ACR).  
  - Deploy to Azure Web App (Linux) on merge to `main`.  
  - Use Azure SQL Database for production storage.  

- **Azure Infrastructure**  
  | Service                   | Purpose                                  |
  |---------------------------|------------------------------------------|
  | Azure SQL Database        | Store production data                    |
  | Azure Container Registry  | Store Docker images                      |
  | Azure Web App (Linux)     | Host REST API                            |
  | Azure Resource Group      | Organize and manage all resources        |

- Role-based access control (RBAC).  
- Caching (Redis) for session performance.  
- Notification service (push/email).  
- Frontend client integration.  

---

## 📌 Project Status
- Core API and authentication: ✅ Implemented  
- Swagger documentation: ✅ Live  
- Database ERD: ✅ Finalized  
- Persistence layer & testing: 🚧 In progress  
- Frontend client: 🔮 Planned  

---

## 📄 License
MIT License – see the `LICENSE` file for details.
