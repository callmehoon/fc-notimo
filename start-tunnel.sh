#!/bin/bash

# Load environment variables from .env file if it exists
if [ -f .env ]; then
  echo "Loading environment variables from .env file..."
  set -a
  source .env
  set +a
fi

echo "Checking for DRHONG_PEM_KEY_PATH..."
if [ -z "$DRHONG_PEM_KEY_PATH" ]; then
  echo "Error: DRHONG_PEM_KEY_PATH is not set in your system or .env file."
  echo "Please create a .env file from .env.example and set the path."
  exit 1
fi

echo "Starting SSH tunnel in the background..."
# Check if a tunnel is already running on the port and kill it
lsof -i :3307 | awk 'NR>1 {print $2}' | xargs -r kill -9

ssh -fN -L 3307:drhong-db.cny6cmeagio6.ap-northeast-2.rds.amazonaws.com:3306 -i "$DRHONG_PEM_KEY_PATH" ec2-user@43.202.67.248

if [ $? -eq 0 ]; then
  echo "SSH tunnel command issued successfully."
else
  echo "Error: Failed to issue SSH tunnel command."
  exit 1
fi

sleep 3
echo "Script finished."
exit 0