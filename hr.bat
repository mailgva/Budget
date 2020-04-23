rem mvn spring-boot:run
if [ -z ${GDRIVE_CLIENT_ID} ]; then export GDRIVE_CLIENT_ID=''; fi
if [ -z ${GDRIVE_SECRET} ]; then export GDRIVE_SECRET=''; fi
if [ -z ${GDRIVE_REFRESH_TOKEN} ]; then export GDRIVE_REFRESH_TOKEN=''; fi
if [ -z ${GDRIVE_FOLDERID} ]; then export GDRIVE_FOLDERID=''; fi

call mvn -B -s settings.xml -DskipTests=true clean package
call java -jar target/dependency/webapp-runner.jar target/*.war
