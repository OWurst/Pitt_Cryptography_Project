package com.project.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

import com.project.Resource.Mods.Helper;
import com.project.Resource.Service.FileService;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

@SpringBootApplication
public class ResourceServerApplication {

	@Resource
	FileService storageService;

	public static void main(String[] args) throws Exception {
		try {
			SpringApplication resourceServer = new SpringApplication(ResourceServerApplication.class);
			Map<String, Object> defaultProperties = new HashMap<>();
			if (args.length < 4)
				throw new RuntimeErrorException(null, "please provide port, pubKeyPath, privKeyPath, authPubKeyPath");
			else {
				if (!Files.exists(Paths.get(new URI(("file:///" + args[1]))))) {
					throw new RuntimeErrorException(null, "path to resource server public key file does not exist");
				}
				if (!Files.exists(Paths.get(new URI(("file:///" + args[2]))))) {
					throw new RuntimeErrorException(null, "path to resource server private key file does not exist");
				}
				if (!Files.exists(Paths.get(new URI(("file:///" + args[3]))))) {
					throw new RuntimeErrorException(null, "path to auth server public key file does not exist");
				}
				defaultProperties.put("server.port", args[0]);
				defaultProperties.put("resource.pubKeyPath", args[1]);
				defaultProperties.put("resource.privKeyPath", args[2]);
				defaultProperties.put("auth.authPubKeyPath", args[3]);
				resourceServer.setDefaultProperties(defaultProperties);
				resourceServer.run();

				Helper helper = new Helper();
				helper.generateRSID(args[1]);
			}
		} catch (Error e) {
			throw new RuntimeErrorException(e, "please provide port");
		}
	}

	@Bean
	public void initStorageService() {
		storageService.init();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*").allowedMethods("*")
						.allowedHeaders("*");
			}
		};
	}
}
