package com.gorbatenko.budget.config;

import com.gorbatenko.budget.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMongoRepositories
public class WebSecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserService userService) {
        DaoAuthenticationProvider authProvider
                = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        List<AuthenticationProvider> providers = List.of(authProvider);

        return new ProviderManager(providers);
    }

//    protected void configure(AuthenticationManagerBuilder auth) {
//        auth.authenticationProvider(authenticationProvider());
//    }
//
//
//    private DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider
//                = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests(
                        (requests) -> requests
                                .requestMatchers( new AntPathRequestMatcher("/js/**")).permitAll()
                                .requestMatchers( new AntPathRequestMatcher("/css/**")).permitAll()
                                .requestMatchers( new AntPathRequestMatcher("/images/**")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                                .requestMatchers( new AntPathRequestMatcher("/profile/register")).permitAll()
                                .requestMatchers( new AntPathRequestMatcher("/")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/**/*.js", "/**/*.css")).permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login-error")
                .defaultSuccessUrl("/menu")
                .permitAll()
                .and()
                .logout().deleteCookies("JSESSIONID")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login")
                .permitAll()
                .and()
                .rememberMe().key("remember-me").userDetailsService(userService).tokenValiditySeconds(60*60*24*365*10);
        return http.build();
    }
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests().antMatchers("/webjars/**").permitAll();
//        http.authorizeRequests().antMatchers("/js/**").permitAll();
//        http.authorizeRequests().antMatchers("/css/**").permitAll();
//        http.authorizeRequests().antMatchers("/images/**").permitAll();
//
//        http
//                .authorizeRequests()
//                .antMatchers("/", "/profile/register").permitAll()
//                .antMatchers("/**/*.js", "/**/*.css").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .failureUrl("/login-error")
//                .defaultSuccessUrl("/menu")
//                .permitAll()
//                .and()
//                .logout().deleteCookies("JSESSIONID")
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                .logoutSuccessUrl("/login")
//                .permitAll()
//                .and()
//                .rememberMe().key("remember-me").userDetailsService(userService).tokenValiditySeconds(60*60*24*365*10); // 10 years
//    }

//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }

}
