package tn.iteam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableDiscoveryClient
public class KeycloakAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeycloakAuthServiceApplication.class, args);
    }

}
