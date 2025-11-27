# Install nfs client for mounting EFS
# sudo apt install nfs-common -y
pip3 install -e app-layer/. --force-reinstall
pip3 install -e core/. --force-reinstall
pip3 install urllib3==1.26.6 --force-reinstall
echo "Requirements installed"
