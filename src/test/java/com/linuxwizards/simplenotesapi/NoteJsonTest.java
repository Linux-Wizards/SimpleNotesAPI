package com.linuxwizards.simplenotesapi;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private JacksonTester<Note[]> jsonList;

    private Note[] notes;

    @BeforeEach
    void setUp() {
        notes = Arrays.array(
                new Note(99L, "This is a title", "This is a note"),
                new Note(100L, "Second title", "Second note"),
                new Note(101L, "Another title", "Another note")
        );
    }

    @Test
    void noteSerializationTest() throws IOException {
        Note note = notes[0];
        assertThat(json.write(note)).isStrictlyEqualToJson("single.json");

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

    @Test
    void noteListSerializationTest() throws IOException {
        assertThat(jsonList.write(notes)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void noteListDeserializationTest() throws IOException {
        String expected = """
         [
            { "id": 99, "title": "This is a title", "content": "This is a note" },
            { "id": 100, "title": "Second title", "content": "Second note" },
            { "id": 101, "title": "Another title", "content": "Another note" }
         ]
         """;

        assertThat(jsonList.parse(expected)).isEqualTo(notes);
    }
}
