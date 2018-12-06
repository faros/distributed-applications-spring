package com.example.giftclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableZuulProxy
@EnableHystrix
public class GiftClientApplication {
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(GiftClientApplication.class, args);
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Gift {
	private Long id;
	private String giftName;
}

/*
@RestController
class GiftApiAdapterRestController {

	private final RestTemplate restTemplate;

	public GiftApiAdapterRestController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/gift/names")
	public Collection<Gift> names() {
		ResponseEntity<List<Gift>> response = restTemplate.exchange(
				"http://gift-service/gifts",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Gift>>() {
				});

		return response.getBody();
	}
}
*/


@FeignClient("gift-service")
interface GiftClient {
	@GetMapping("/gifts")
	Collection<Gift> read();
}

@RestController
class GiftApiAdapterRestController {

	private final GiftClient client;

	public GiftApiAdapterRestController(GiftClient client) {
		this.client = client;
	}

	public Collection<String> fallback() {
		return new ArrayList<>();
	}

	@GetMapping("/gift/names")
	@HystrixCommand(fallbackMethod = "fallback")
	public Collection<String> names() {

		return this.client.read()
				.stream()
				.map(Gift::getGiftName)
				.collect(Collectors.toList());
	}
}