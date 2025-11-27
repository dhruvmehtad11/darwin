-- Grant permissions to darwin-catalog user from any host
-- This ensures the user can connect from Docker network IPs
CREATE USER IF NOT EXISTS 'darwin-catalog'@'%' IDENTIFIED BY 'darwin-catalog';
GRANT ALL PRIVILEGES ON darwin_catalog.* TO 'darwin-catalog'@'%';
FLUSH PRIVILEGES;

