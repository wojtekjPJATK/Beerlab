package com.app;

import com.app.repository.ProductRepository;
import com.app.repository.RoleRepository;
import com.app.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        BeerlabApplication.class,
        Jsr310JpaConverters.class
})
@EnableJdbcHttpSession
@EnableScheduling
public class BeerlabApplication {
    private RoleRepository roleRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;

    public BeerlabApplication(RoleRepository roleRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BeerlabApplication.class, args);

    }

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }
/*    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() != RoleName.values().length) {
            roleRepository.deleteAll();
            Arrays
                    .stream(RoleName.values())
                    .forEach(role -> roleRepository.save(Role.builder().roleName(role).build()));
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        userRepository.save(User.builder().email("manager@beerlab.com").username("manager").roles(Collections.singletonList(roleRepository.findByRoleName(RoleName.ROLE_ADMIN).get())).password(bCryptPasswordEncoder.encode("manager")).build());
        userRepository.save(User.builder().email("barman@beerlab.com").username("barman").roles(Collections.singletonList(roleRepository.findByRoleName(RoleName.ROLE_BARMAN).get())).password(bCryptPasswordEncoder.encode("barman")).build());
    }*/
}
