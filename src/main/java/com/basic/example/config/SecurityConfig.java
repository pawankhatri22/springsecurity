package com.basic.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration //to create the beans
@EnableWebSecurity // to tell spring boot we are going to work with security related configuration
@EnableMethodSecurity // for adding role based annotation on method preauthorize
public class SecurityConfig {

    @Autowired
    DataSource dataSource;

    @Bean
    //It controls all configurations related to spring authroization we can customize this SecurityFilterChain
    SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth->
                auth.requestMatchers("/h2-console/**").permitAll() // permit usls with this base url without authentication
                        .anyRequest().authenticated() // authenticate all other the endpoints
                       );
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")); // Disable CSRF for H2 console
        http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // make request stateless remove jsesion from basic auth
        http.httpBasic(Customizer.withDefaults()); // enable basic authentication
        http.headers(headers-> headers.frameOptions(frame->frame.sameOrigin())); //frames are disabled by default, we have to enable these for same origin, it
        return http.build();
    }


    /*
    * As we have studied before UserDetailsService is use to load users and Authentication Provider matches() those users with input credentials
    * So we can override UserDetailsService and use InMemoryUserDetailsManager implementation of this to add as many users as we want.
    * */
//    @Bean
//    UserDetailsService inMemoryUserDetails(){
//        //create user , password should be encoded but for simple example I am using noop with indicate password can be saved as plaintext
//        UserDetails user = User.withUsername("testUser")
//                .password("{noop}test1")
//                .roles("USER")
//                .build();
//        //create admin user
//        UserDetails admin = User.withUsername("adminUser")
//                .password("{noop}test2")
//                .roles("ADMIN")
//                .build();
//
//
//        //In memory Implementation of UserDetailsService. Good for testing purpose
//        return new InMemoryUserDetailsManager(user,admin);
//    }


    /*
     * As we have studied before UserDetailsService is use to load users and Authentication Provider matches() those users with input credentials
     * So we can override UserDetailsService and use JdbcUserDetailsManager implementation of this to add as many users as we want in database.
     * */
    @Bean
    UserDetailsService jdbcUserDetailsManager(){
        //create user , password should be encoded but for simple example I am using noop with indicate password can be saved as plaintext
        UserDetails user = User.withUsername("testUser")
                .password(bcryptEncoder().encode("test1"))
                .roles("USER")
                .build();
        //create admin user
        UserDetails admin = User.withUsername("adminUser")
                .password("{noop}test2")
                .roles("ADMIN")
                .build();


        //Using JdbcUserDetailsManager, need datasource to be password, so autowire datasource, bean of datasource is create using auto configuration feature of spring boot.
        UserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);

        //create users in h2 database. first need to create users table, which is in schema.sql file. it runs when application starts.
        userDetailsManager.createUser(user);
        userDetailsManager.createUser(admin);
        return userDetailsManager;
    }


    @Bean
    public PasswordEncoder bcryptEncoder(){
        return new BCryptPasswordEncoder(); // we can use any custom implementation as well
    }
}
