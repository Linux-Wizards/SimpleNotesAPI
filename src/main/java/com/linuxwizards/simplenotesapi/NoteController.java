package com.linuxwizards.simplenotesapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notes")
class NoteController {
    @GetMapping("/{requestedId}")
    private ResponseEntity<Note> findById(@PathVariable Long requestedId) {
        if (requestedId.equals(99L)) {
            Note note = new Note(99L, "This is a title", "This is a note");
            return ResponseEntity.ok(note);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
