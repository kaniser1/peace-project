package com.andmeanalyys.peace_project;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeaceProjectApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
		System.setProperty("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
		System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
		System.setProperty("AI_ENDPOINT", dotenv.get("AI_ENDPOINT"));
		System.setProperty("SUPABASE_URL", dotenv.get("SUPABASE_URL"));
		System.setProperty("BUCKET_NAME", dotenv.get("BUCKET_NAME"));
		System.setProperty("AI_API_KEY", dotenv.get("AI_API_KEY"));
		System.setProperty("SUPABASE_API_KEY", dotenv.get("SUPABASE_API_KEY"));
		System.setProperty("CLARIFYAI_PAT", dotenv.get("CLARIFYAI_PAT"));

		SpringApplication.run(PeaceProjectApplication.class, args);
	}
}
