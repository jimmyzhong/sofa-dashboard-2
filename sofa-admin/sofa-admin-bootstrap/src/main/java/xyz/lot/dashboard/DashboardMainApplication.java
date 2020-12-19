package xyz.lot.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class,
        DataSourceAutoConfiguration.class})
//,MongoAutoConfiguration.class
//@EnableCaching
@ComponentScan(value = {"xyz.lot"})
@Slf4j
public class DashboardMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardMainApplication.class, args);
    }

//    @Value("${dashboard.mongodb.uri}")
//    private String mongoUri;

//    @Bean(name = "mongoTemplate")
//    public MongoTemplate mongoTemplate() throws Exception {
//        return new MongoTemplate(primaryFactory());
//    }
//
//    public MongoDbFactory primaryFactory() throws Exception {
//        log.info("mongo uri:{}",mongoUri);
//        MongoClientURI mongoClientURI = new MongoClientURI(mongoUri);
//        return new SimpleMongoDbFactory(new MongoClient(mongoClientURI),mongoClientURI.getDatabase());
//    }

    @PostConstruct
    public void init() {
        //Annotation[] anns = DashboardMainApplication.class.getDeclaredAnnotations();
        //Arrays.stream(anns).forEach(e -> System.out.println(e.annotationType()));
    }
}
