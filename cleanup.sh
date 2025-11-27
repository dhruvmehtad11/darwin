#!/bin/bash
set -e

echo "🧹 Starting comprehensive cleanup of Darwin workflow resources..."
echo "================================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Set kubeconfig
export KUBECONFIG=./kind-kubeconfig.yaml

echo ""
echo "📦 Step 1: Cleaning up Kubernetes resources..."
echo "----------------------------------------------"

# Check if kind cluster exists
if kind get clusters | grep -q "kind"; then
    print_status "Kind cluster found, cleaning up Kubernetes resources..."
    
    # Delete specific resources
    kubectl delete deployment darwin-workflow -n darwin --ignore-not-found=true 2>/dev/null || true
    kubectl delete job darwin-datastore-health-check -n darwin --ignore-not-found=true 2>/dev/null || true
    kubectl delete service darwin-workflow -n darwin --ignore-not-found=true 2>/dev/null || true
    kubectl delete ingress darwin-workflow -n darwin --ignore-not-found=true 2>/dev/null || true
    
    # Delete all pods in darwin namespace
    kubectl delete pods --all -n darwin --ignore-not-found=true 2>/dev/null || true
    
    # Delete all deployments in darwin namespace
    kubectl delete deployments --all -n darwin --ignore-not-found=true 2>/dev/null || true
    
    # Delete all services in darwin namespace
    kubectl delete services --all -n darwin --ignore-not-found=true 2>/dev/null || true
    
    # Delete all jobs in darwin namespace
    kubectl delete jobs --all -n darwin --ignore-not-found=true 2>/dev/null || true
    
    # Delete all ingresses in darwin namespace
    kubectl delete ingresses --all -n darwin --ignore-not-found=true 2>/dev/null || true
    
    print_status "Kubernetes resources cleaned up"
else
    print_warning "Kind cluster not found, skipping Kubernetes cleanup"
fi

echo ""
echo "🐳 Step 2: Cleaning up Docker images..."
echo "--------------------------------------"

# Delete darwin-workflow specific images
print_status "Removing darwin-workflow images..."
docker rmi darwin-workflow:latest 2>/dev/null || true
docker rmi localhost:5000/darwin-workflow:latest 2>/dev/null || true

# Delete all darwin-related images
print_status "Removing all darwin-related images..."
docker images | grep darwin | awk '{print $3}' | xargs docker rmi -f 2>/dev/null || true

# Delete all localhost:5000 images
print_status "Removing localhost:5000 images..."
docker images | grep "localhost:5000" | awk '{print $3}' | xargs docker rmi -f 2>/dev/null || true

print_status "Docker images cleaned up"

echo ""
echo "🗑️  Step 3: Cleaning up Docker containers and services..."
echo "--------------------------------------------------------"

# Stop all running containers
print_status "Stopping all running containers..."
docker stop $(docker ps -q) 2>/dev/null || true

# Remove all containers
print_status "Removing all containers..."
docker rm $(docker ps -aq) 2>/dev/null || true

# Remove all services
print_status "Removing all Docker services..."
docker service ls -q | xargs docker service rm 2>/dev/null || true

# Remove all stacks
print_status "Removing all Docker stacks..."
docker stack ls -q | xargs docker stack rm 2>/dev/null || true

print_status "Docker containers and services cleaned up"

echo ""
echo "🧽 Step 4: Cleaning up Docker system resources..."
echo "------------------------------------------------"

# Clean up networks
print_status "Cleaning up unused networks..."
docker network prune -f 2>/dev/null || true

# Clean up volumes
print_status "Cleaning up unused volumes..."
docker volume prune -f 2>/dev/null || true

# Clean up build cache
print_status "Cleaning up build cache..."
docker builder prune -f 2>/dev/null || true

# Clean up all unused resources
print_status "Cleaning up all unused Docker resources..."
docker system prune -a -f 2>/dev/null || true

print_status "Docker system resources cleaned up"

echo ""
echo "📁 Step 5: Cleaning up local files..."
echo "------------------------------------"

# Remove generated files
print_status "Removing generated files..."
rm -f kind-kubeconfig.yaml 2>/dev/null || true
rm -rf workflow/target/ 2>/dev/null || true

# Remove any temporary files
print_status "Removing temporary files..."
find . -name "*.tmp" -delete 2>/dev/null || true
find . -name ".DS_Store" -delete 2>/dev/null || true

print_status "Local files cleaned up"

echo ""
echo "🔍 Step 6: Verifying cleanup..."
echo "-------------------------------"

# Check remaining containers
remaining_containers=$(docker ps -aq | wc -l)
if [ "$remaining_containers" -eq 0 ]; then
    print_status "No containers remaining"
else
    print_warning "$remaining_containers containers still exist"
    docker ps -a
fi

# Check remaining images
remaining_images=$(docker images | grep -E "(darwin|localhost:5000)" | wc -l)
if [ "$remaining_images" -eq 0 ]; then
    print_status "No darwin-related images remaining"
else
    print_warning "$remaining_images darwin-related images still exist"
    docker images | grep -E "(darwin|localhost:5000)"
fi

# Check kind cluster
if kind get clusters | grep -q "kind"; then
    print_warning "Kind cluster still exists"
    echo "To remove the kind cluster, run: kind delete cluster --name kind"
else
    print_status "Kind cluster removed"
fi

echo ""
echo "🎉 Cleanup completed!"
echo "===================="
echo ""
echo "Summary of what was cleaned up:"
echo "• Kubernetes deployments, services, jobs, and ingresses"
echo "• Docker containers and services"
echo "• Docker images (darwin-workflow and localhost:5000)"
echo "• Docker networks, volumes, and build cache"
echo "• Local generated files (kind-kubeconfig.yaml, workflow/target/)"
echo ""
echo "Note: The kind cluster itself was not deleted."
echo "To remove the kind cluster completely, run:"
echo "  kind delete cluster --name kind"
echo ""
