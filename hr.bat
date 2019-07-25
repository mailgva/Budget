mvn spring-boot:run
rem call mvn -B -s settings.xml -DskipTests=true clean package
rem call java -jar target/dependency/webapp-runner.jar target/*.war