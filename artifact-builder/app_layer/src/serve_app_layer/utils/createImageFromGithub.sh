#!/bin/sh

git_repo=$1
branch=$2
app_name=$3
file_path=$4
image_tag=$5
app_dir=$6
is_custom=$7

echo "<----- Input Parameters ----->"
echo "Git Repository: $git_repo"
echo "Branch: $branch"
echo "App Name: $app_name"
echo "File Path: $file_path"
echo "Image Tag: $image_tag"
echo "App Directory: $app_dir"
echo "Is Custom: $is_custom"

# Clone the repository if not already present
cd "$file_path" || exit

if [ "$is_custom" = "False" ]; then
  if [ -d "$app_name" ]; then
    echo "Deleting existing directory $app_name for a fresh clone..."
    rm -rf "$app_name"
  fi

  if [ ! -d "$app_name" ]; then
    retries=10
    success=false

    for i in $(seq 1 $retries); do
      if [ -n "$branch" ]; then
        echo "Attempt $i: Cloning the $branch branch of repository $git_repo to $file_path/$app_name"
        sudo git clone "$git_repo" -b "$branch" "$app_name"
      else
        echo "Attempt $i: Cloning the master branch of repository $git_repo to $file_path/$app_name"
        sudo git clone "$git_repo" "$app_name"
      fi

      if [ $? -eq 0 ]; then
        echo "PROJECT PULLED"
        success=true
        break
      else
        echo "Clone failed, retrying..."
      fi
    done

    if [ "$success" = true ]; then
      if [ -f Dockerfile ]; then
        if [ -n "$app_dir" ]; then
          echo "Dockerfile found in the root directory. Moving to $app_name/$app_dir"
          mv Dockerfile "$app_name/$app_dir/Dockerfile" || { echo "Failed to move Dockerfile"; exit 1; }
          cd "$app_name/$app_dir" || { echo "Failed to change directory to $app_name/$app_dir"; exit 1; }
        else
          echo "Dockerfile found in the root directory. Moving to $app_name"
          mv Dockerfile "$app_name/Dockerfile" || { echo "Failed to move Dockerfile"; exit 1; }
          cd "$app_name" || { echo "Failed to change directory to $app_name"; exit 1; }
        fi
      else
        if [ -n "$app_dir" ]; then
          echo "Dockerfile not found in the root directory. Checking in $app_name/$app_dir"
          cd "$app_name/$app_dir" || { echo "Failed to change directory to $app_name/$app_dir"; exit 1; }
        else
          echo "Dockerfile not found in the root directory. Checking in $app_name"
          cd "$app_name" || { echo "Failed to change directory to $app_name"; exit 1; }
        fi
      fi
    else
      echo "Failed to clone the repository after $retries attempts."
    fi
  fi
fi



echo "<----- Dockerfile contents ----->"
# Display the content of the Dockerfile
cat Dockerfile && echo ""

echo "<----- Logs ----->"

CONTAINER_IMAGE_PREFIX="${CONTAINER_IMAGE_PREFIX:-darwin}"
CONTAINER_IMAGE_PREFIX_GCP="${CONTAINER_IMAGE_PREFIX_GCP:-ray-images}"
AWS_ECR_ACCOUNT_ID="${AWS_ECR_ACCOUNT_ID}"
AWS_ECR_REGION="${AWS_ECR_REGION:-us-east-1}"
GCP_PROJECT_ID="${GCP_PROJECT_ID}"
GCP_CREDS_PATH="${GCP_CREDS_PATH:-/home/admin/gcp_creds.json}"
LOCAL_REGISTRY="${LOCAL_REGISTRY:-}"

if [ -n "$AWS_ECR_ACCOUNT_ID" ]; then
  echo "Logging into AWS ECR..."
  aws ecr get-login-password --region "$AWS_ECR_REGION" | sudo docker login --username AWS --password-stdin "$AWS_ECR_ACCOUNT_ID.dkr.ecr.$AWS_ECR_REGION.amazonaws.com" || exit
fi

if [ -n "$GCP_PROJECT_ID" ] && [ -f "$GCP_CREDS_PATH" ]; then
  echo "Logging into GCP GCR..."
  cat "$GCP_CREDS_PATH" | sudo docker login -u _json_key --password-stdin https://gcr.io
fi

echo "IMAGE BUILD START with tag:$image_tag" &&
sudo docker build -t "$CONTAINER_IMAGE_PREFIX":"$image_tag" ./ &&

if [ -n "$AWS_ECR_ACCOUNT_ID" ]; then
  sudo docker tag "$CONTAINER_IMAGE_PREFIX":"$image_tag" "$AWS_ECR_ACCOUNT_ID.dkr.ecr.$AWS_ECR_REGION.amazonaws.com/$CONTAINER_IMAGE_PREFIX":"$image_tag" &&
  echo "IMAGE TAGGED for AWS ECR"
fi

if [ -n "$GCP_PROJECT_ID" ]; then
  sudo docker tag "$CONTAINER_IMAGE_PREFIX":"$image_tag" gcr.io/"$GCP_PROJECT_ID"/"$CONTAINER_IMAGE_PREFIX_GCP":"$image_tag" &&
  echo "IMAGE TAGGED for GCP GCR"
fi

if [ -n "$LOCAL_REGISTRY" ]; then
  echo "Tagging for Local Registry: $LOCAL_REGISTRY"
  sudo docker tag "$CONTAINER_IMAGE_PREFIX":"$image_tag" "$LOCAL_REGISTRY/$CONTAINER_IMAGE_PREFIX":"$image_tag" &&
  echo "IMAGE TAGGED for Local Registry"
fi

echo ""

if [ "$is_custom" = "True" ]; then
  echo "Creating Docker container with image - $image_tag..."
  sudo docker run -dit --name "$app_name" "$CONTAINER_IMAGE_PREFIX":"$image_tag"
  echo "Checking if ray start is working..."
  sudo docker exec -i "$app_name" ray start --head > out.txt
  sudo docker stop "$app_name"
  sudo docker rm "$app_name"
  if grep -q "Ray runtime started" out.txt; then
    echo "Ray start validated for image"
  else
    echo "Ray is not starting in this image"
    sudo docker system prune -af
    exit 1
  fi
fi

if [ -n "$AWS_ECR_ACCOUNT_ID" ]; then
  echo "Pushing to AWS ECR..."
  sudo docker push "$AWS_ECR_ACCOUNT_ID.dkr.ecr.$AWS_ECR_REGION.amazonaws.com/$CONTAINER_IMAGE_PREFIX":"$image_tag" &&
  echo "Successfully pushed to AWS ECR"
fi

if [ -n "$GCP_PROJECT_ID" ]; then
  echo "Pushing to GCP GCR..."
  sudo docker push gcr.io/"$GCP_PROJECT_ID"/"$CONTAINER_IMAGE_PREFIX_GCP":"$image_tag" &&
  echo "Successfully pushed to GCP GCR"
fi

if [ -n "$LOCAL_REGISTRY" ]; then
  echo "Pushing to Local Registry..."
  sudo docker push "$LOCAL_REGISTRY/$CONTAINER_IMAGE_PREFIX":"$image_tag" &&
  echo "Successfully pushed to Local Registry"
fi

sudo docker system prune -af
