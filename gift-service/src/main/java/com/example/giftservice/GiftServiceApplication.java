package com.example.giftservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@SpringBootApplication
@EnableDiscoveryClient
public class GiftServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiftServiceApplication.class, args);
	}

}

@Component
class initializer implements ApplicationRunner {

	private final GiftRepository giftRepository;

	public initializer(GiftRepository giftRepository) {
		this.giftRepository = giftRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Stream.of("Lego", "matchbox", "some more lego", "Nintendo switch", "Super mario")
				.forEach(name -> giftRepository.save(new Gift(null, name)));

		giftRepository.findAll().forEach(System.out::println);
	}
}

interface GiftRepository extends JpaRepository<Gift, Long> {
	// select * from gifts where gift_name = name;
	Collection<Gift> findByGiftName(String name);
}


@RestController
@RefreshScope
class MessageRestController {

	private final String message;

	public MessageRestController(@Value("${message}") String message) {
		this.message = message;
	}

	@GetMapping("/message")
	public String read() {
		return this.message;
	}
}


@RestController
class GiftRestController {

	private final GiftRepository giftRepository;

	public GiftRestController(GiftRepository giftRepository) {
		this.giftRepository = giftRepository;
	}

	@GetMapping("/gifts")
	Collection<Gift> gifts() {
		return this.giftRepository.findAll();
	}
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Gift {
	@Id
	@GeneratedValue
	private Long id;

	private String giftName;

}

