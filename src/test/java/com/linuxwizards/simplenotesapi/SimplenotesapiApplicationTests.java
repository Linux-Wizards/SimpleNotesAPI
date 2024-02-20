package com.linuxwizards.simplenotesapi;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimplenotesapiApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldReturnANoteWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/notes/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		String title = documentContext.read("$.title");
		assertThat(title).isEqualTo("This is a title");

		String content = documentContext.read("$.content");
		assertThat(content).isEqualTo("This is a note");
	}

	@Test
	void shouldNotReturnANoteWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/notes/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	void shouldCreateANewNote() {
		Note newNote = new Note(null, "This is second note", "This is second content");
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/notes", newNote, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewNote = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewNote, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		String title = documentContext.read("$.title");
		String content = documentContext.read("$.content");

		assertThat(id).isNotNull();
		assertThat(title).isEqualTo("This is second note");
		assertThat(content).isEqualTo("This is second content");
	}
}
