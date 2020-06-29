#Budget  

The application for management and planning family budget.
Features:
 - group of users (in section Profile has link to join to group)
 - contains 2 types operation - Spanding and Profit
 - section Dictionaries contains: kinds of operations and kinds of currencies
 - after user registred, he has:
    - some kinds of operation
    - some currencies (grn, usd, eur)
 - section Statistic - shows operations by days, with filters
 - section Group Statistics:
    - shows summury by operation for period and able to show:
        * count operations by kind
        * percent from total type sum (white line) 
        * percent from max kind sum (yellow line)
        * details for kind
        * charts: doughnut, horizontal bar
        * vertical chart for show dynamic by kind
 - backuping database by sheduler 

 Technology stack:
   * Java 1.8 
   * MongoDB 3.6
   * Spring Boot 2.1.4
     * Spring Data-Mongodb
     * Spring Security
     * Spring Validation
     * Spring Web
   * Thymeleaf
     * Thymeleaf Security
     * Thymeleaf Java8time
   * Jackson
   * Project Lombok
   * Webjars
     * Jquery
     * Bootstrap
     * Mdbootstrap-bootstrap-material-design (charts)
     * Font-awesome
  
For run app you need:
 1. Install and run **MongoDB server**. 
 2. Create environment variable **MONGODB_URI**.  
    Example for localhost:  
    ``MONGODB_URI=mongodb://localhost:27017/budget``   
 3. Run app (4 ways):  
    - execute ``hr.bat``
    - in Idea through Tomcat
    - in Idea through main
    - mvn clean package spring-boot:run
 
 
    
 
     

  