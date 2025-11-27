# Workspace App Layer

### 1. Install gunicorn
```
pip3 install gunicorn --force-reinstall
```

### 2. Install required dependencies
```
pip3 install -r requirements.txt --force-reinstall
```

### 2. Run app layer as daemon
```
gunicorn --worker-class uvicorn.workers.UvicornWorker --bind '0.0.0.0:8000' --daemon main:app --workers=4
```
