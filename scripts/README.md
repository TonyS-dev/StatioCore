# 🚀 ParkNexus Setup & Run Scripts

Cross-platform scripts to set up and run ParkNexus with a single command.

## 📋 Prerequisites

- **Linux/macOS**: bash shell (preinstalled)  
- **Windows**: PowerShell 5.0+ (preinstalled on Windows 10+)  
- **Docker** (scripts can help install it)

## 🎯 Quick Start

### Linux / macOS

````bash
# 1. Make scripts executable (only the first time)
chmod +x scripts/setup.sh scripts/run.sh

# 2. Run setup (detects Docker, creates .env)
./scripts/setup.sh

# 3. Edit .env with secure values
nano .env  # or your preferred editor

# 4. Run the application
./scripts/run.sh
````

### Windows (PowerShell)

````powershell
# 1. Open PowerShell as Administrator

# 2. Enable script execution (only the first time)
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

# 3. Run setup (detects Docker, creates .env)
.\scripts\setup.ps1

# 4. Edit .env with secure values
notepad .env

# 5. Run the application
.\scripts\run.ps1
````

## 📦 What the scripts do

### `setup.sh` / `setup.ps1`

✅ Detects the operating system  
✅ Checks if Docker is installed  
✅ Offers to install Docker if not present  
✅ Checks Docker Compose  
✅ Creates `.env` from `.env.example`  
✅ Shows next steps

### `run.sh` / `run.ps1`

✅ Verifies that `.env` exists (runs setup if missing)  
✅ Checks that Docker is running  
✅ Builds Docker images  
✅ Brings up all containers  
✅ Shows access URLs

## 🌐 Access URLs

After running `run`, the application will be available at:

| Service    | URL                          |
|------------|------------------------------|
| Frontend   | http://localhost:80          |
| Backend    | http://localhost:8080        |
| Prometheus | http://localhost:9090        |
| Grafana    | http://localhost:3000        |

## 🛠️ Useful Commands

````bash
# View logs in real time
docker compose logs -f

# View logs for a specific service
docker compose logs -f frontend
docker compose logs -f backend

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v

# Restart a specific service
docker compose restart frontend

# Rebuild images
docker compose up --build
````

## 🔒 Security - Environment Variables

**IMPORTANT:** After running `setup`, edit the `.env` file and change:

- `DB_PASSWORD` - Use a strong password
- `JWT_SECRET` - At least 32 random characters

Example of generating secure secrets:

### Linux/macOS

````bash
# Generate random JWT_SECRET
openssl rand -base64 32

# Generate random DB_PASSWORD
openssl rand -base64 16
````

### Windows (PowerShell)

````powershell
# Generate random JWT_SECRET
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Generate random DB_PASSWORD
[Convert]::ToBase64String((1..16 | ForEach-Object { Get-Random -Maximum 256 }))
````

## 🐛 Troubleshooting

### Docker is not running

**Error:** `Cannot connect to the Docker daemon`

**Solution:**
- **Linux:** `sudo systemctl start docker`
- **macOS/Windows:** Open Docker Desktop

### Port already in use

**Error:** `Bind for 0.0.0.0:80 failed: port is already allocated`

**Solution:** Change the port in `docker-compose.yml`:

````yaml
frontend:
  ports:
    - "8081:80"  # Change 80 to another port
````

### Permissions on Linux

**Error:** `permission denied while trying to connect to the Docker daemon`

**Solution:**
````bash
# Add user to docker group
sudo usermod -aG docker $USER

# Log out and log back in, or run:
newgrp docker
````

### Build fails due to insufficient memory

**Solution:** Increase memory allocated to Docker:
- Docker Desktop → Settings → Resources → Memory (minimum 4GB recommended)

## 📚 More Information

- See `README.md` in the project root for full documentation
- See `docs/API_Documentation.md` for API reference
- See `frontend/README.md` and `backend/README.md` for module-specific details

## 🤝 Contributing

If you find issues with the scripts, please open an issue on GitHub.

---

**Author:** Antonio Santiago (TonyS-dev)  
**Email:** santiagor.acarlos@gmail.com  
**GitHub:** https://github.com/TonyS-dev
```