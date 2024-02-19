package com.linuxwizards.simplenotesapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NoteJsonTest {
    @Autowired
    private JacksonTester<Note> json;

    @Test
    void noteSerializationTest() throws IOException {
        Note note = new Note(99L, "This is a title", "This is a note");
        assertThat(json.write(note)).isStrictlyEqualToJson("expected.json");

        assertThat(json.write(note)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(note)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);

        assertThat(json.write(note)).hasJsonPathStringValue("@.title");
        assertThat(json.write(note)).extractingJsonPathStringValue("@.title")
                .isEqualTo("This is a title");

        assertThat(json.write(note)).hasJsonPathStringValue("@.content");
        assertThat(json.write(note)).extractingJsonPathStringValue("@.content")
                .isEqualTo("This is a note");
    }

    @Test
    void noteDeserializationTest() throws IOException {
        String expected = """
           {
               "id": 99,
               "title": "This is a title",
               "content": "This is a note"
           }
           """;

        assertThat(json.parse(expected))
                .isEqualTo(new Note(99L, "This is a title", "This is a note"));

        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).title()).isEqualTo("This is a title");
        assertThat(json.parseObject(expected).content()).isEqualTo("This is a note");
    }
}
