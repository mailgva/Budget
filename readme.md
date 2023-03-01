# Budget  
<img src="http://s1.iconbird.com/ico/1012/EcommerceBusinessIcons/w256h2561350823474wallet256x256.png" height="200" width="200">

The application for management and planning family budgetItem.
Features:
 - group of users (in section Profile has link to join to group)
 - contains 2 types operation - Spanding and Profit
 - regular operation (every day, special day of month, special day of week). Automatic create by scheduler, check tasks every day at 12:00.    
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
   * Java 17     
     <img src="https://upload.wikimedia.org/wikipedia/uk/2/2e/Java_Logo.svg" height="200" width="200">
   * Maven
   * MongoDB 6.0 (Atlas)    
     <img src="https://webassets.mongodb.com/_com_assets/cms/MongoDB-Atlas-Logo-Black-hvfxuesorm.svg" height="200" width="400">
   * Spring Boot 3.0.2      
     <img src="https://commons.bmstu.wiki/images/5/59/Spring-boot-logo.png" height="200" width="400">
     * Spring Data-Mongodb
     * Spring Security
     * Spring Validation
     * Spring Web
   * Thymeleaf      
     <img src="https://www.thymeleaf.org/images/thymeleaf.png" width="200" height="200">     
     * Thymeleaf Security
     * Thymeleaf Java8time
   * Google Drive (backuping data by scheduler)   
     <img src="https://ssl.gstatic.com/images/branding/product/2x/hh_drive_96dp.png" width="200" height="200">
   * Jackson     
     <img src="https://play-lh.googleusercontent.com/7j-Q-OEatmJ0-8Cw8S0wHHm34v_KSVLbNziEMvUu4P6F3Vza2nx5e1DiSGjnGfvqYKI" height="200" width="400">
   * Project Lombok     
     <img src="https://isis.apache.org/_/img/home/built-with/project-lombok.png" height="200" width="400">
   * Webjars    
     <img src="https://www.webjars.org/assets/logo.png" height="100" width="100">    
     * Jquery
     * Bootstrap
     * Mdbootstrap-bootstrap-material-design (charts)
     * Font-awesome
  
For run app you need:
 1. Install and run **MongoDB server**. 
 2. Create environment variables (see examples in src/main/resources/application.properties).
 3. Run app.
 
 
    
 
     

  
