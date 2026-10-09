# #!/bin/bash

# echo "========================================"
# echo "Building HSB Backend..."
# echo "========================================"

# cd "$(dirname "$0")"

# # Clean and build the project
# mvn clean package -DskipTests

# if [ $? -ne 0 ]; then
#     echo ""
#     echo "========================================"
#     echo "BUILD FAILED!"
#     echo "========================================"
#     exit 1
# fi

# echo ""
# echo "========================================"
# echo "Build successful! Deploying to WildFly..."
# echo "========================================"

# # Copy WAR to WildFly deployments
# cp -f target/HSB.war /mnt/c/wildfly-33.0.2.Final/standalone/deployments/

# if [ $? -ne 0 ]; then
#     echo ""
#     echo "========================================"
#     echo "DEPLOYMENT COPY FAILED!"
#     echo "========================================"
#     exit 1
# fi

# echo ""
# echo "========================================"
# echo "Deployment complete!"
# echo "HSB.war copied to WildFly deployments"
# echo "========================================"
