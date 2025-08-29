@echo off
echo Starting SSH tunnel in the background...

start "SSH Tunnel" ssh -N -L 3307:drhong-db.cny6cmeagio6.ap-northeast-2.rds.amazonaws.com:3306 -i C:/Users/user/Desktop/drhong-bastion-key.pem ec2-user@43.202.67.248

echo Script finished, allowing IntelliJ to proceed.
exit 0