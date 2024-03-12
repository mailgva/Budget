# Budget  
<img src="http://s1.iconbird.com/ico/1012/EcommerceBusinessIcons/w256h2561350823474wallet256x256.png" height="200" width="200">

The application for management and planning family budget.
Features:
 - groups of users (there is the link to join to the group in section Profile)
 - contains 3 types operation: Spending, Profit, Exchange
 - regular operation (every day, special day of month, special day of week). Automatic create by scheduler, check the tasks every day at 12:00.    
 - section Dictionaries contains: kinds of operations and kinds of currencies
 - after the user registered, he has:
    - some kinds of operations
    - some currencies (grn, usd, eur)
 - section Statistic - allows to show the operations by days with different filters
 - section Group Statistics:
    - shows summary by operation for period and able to show:
        * count operations by kind
        * percent from total type sum (white line) 
        * percent from max kind sum (yellow line)
        * details for kind
        * charts: doughnut, horizontal bar
        * vertical chart for show dynamic by kind
        * remains dynamic chart
 - currency exchange
 - backup database by scheduler 

 Technology stack:
   * Java 21     
   * Maven
   * PostgreSQL    
   * Spring Boot 3.2.3      
     * Spring Data JPA
     * Spring Security
     * Spring Validation
     * Spring Web
   * Thymeleaf      
     * Thymeleaf Security
     * Thymeleaf Java8time
   * Google Drive (backup data by scheduler)   
   * Webjars    
     * Jquery
     * Bootstrap
     * Mdbootstrap-bootstrap-material-design (charts)
     * Font-awesome

    
 
     

  
