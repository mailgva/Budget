rem mvn spring-boot:run
call mvn -B -s settings.xml -DskipTests=true clean package
call java -jar target/dependency/webapp-runner.jar target/*.war --add-opens java.base/java.time=ALL-UNNAMED
