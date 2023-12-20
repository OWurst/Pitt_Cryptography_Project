package com.project.Auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

@SpringBootApplication
@ComponentScan(basePackages = { "com.project.Auth", "com.project.Auth.Utils" })
public class AuthServerApplication {

	public static void main(String[] args) throws Exception {
		try {
			if (args.length < 3) {
				throw new RuntimeErrorException(null,
						"please provide port, path to public key and path to private key");
			}

			String port = args[0];
			String pathToPublicKey = args[1];
			String pathToPrivateKey = args[2];

			if (!Files.exists(Paths.get(new URI(("file:///" + pathToPublicKey))))) {
				throw new RuntimeErrorException(null, "path to public key file does not exist");
			}
			if (!Files.exists(Paths.get(new URI(("file:///" + pathToPrivateKey))))) {
				throw new RuntimeErrorException(null, "path to private key file does not exist");
			}

			SpringApplication authServer = new SpringApplication(AuthServerApplication.class);
			Map<String, Object> defaultProperties = new HashMap<>();

			defaultProperties.put("server.port", port);
			defaultProperties.put("auth.pubKeyPath", pathToPublicKey);
			defaultProperties.put("auth.privKeyPath", pathToPrivateKey);

			authServer.setDefaultProperties(defaultProperties);
			authServer.run();
		} catch (Error e) {
			throw new RuntimeErrorException(e);
		}
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedMethods("*")
						.allowedHeaders("*")
						.allowCredentials(true)
						.allowedOriginPatterns("*")
						.exposedHeaders("Authorization");
			}
		};
	}
}