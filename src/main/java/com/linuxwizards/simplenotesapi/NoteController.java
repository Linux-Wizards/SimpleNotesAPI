package com.linuxwizards.simplenotesapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/notes")
class NoteController {
    private static final int maxTitleLength = 30;
    private static final int maxContentLength = 1000;
    private static final int maxOwnerLength = 255;

    private final NoteRepository noteRepository;

    private NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    private Note findNote(Long requestedId, Principal principal) {
        return noteRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    private boolean isNoteValid(Note noteRequest, Principal principal) {
        return noteRequest.title().length() <= maxTitleLength
                && noteRequest.content().length() <= maxContentLength
                && principal.getName().length() <= maxOwnerLength;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<Note> findById(@PathVariable Long requestedId, Principal principal) {
        Note note = findNote(requestedId, principal);

        if (note != null) {
            return ResponseEntity.ok(note);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    private ResponseEntity<List<Note>> findAll(Pageable pageable, Principal principal) {
        Page<Note> page = noteRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "id"))
                ));

        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> createNote(@RequestBody Note newNoteRequest, UriComponentsBuilder ucb, Principal principal) {
        if (!isNoteValid(newNoteRequest, principal)) {
            return ResponseEntity.badRequest().build();
        }

        Note noteWithOwner = new Note(null, newNoteRequest.title(), newNoteRequest.content(), principal.getName());
        Note savedNote = noteRepository.save(noteWithOwner);

        URI locationOfNewNote = ucb
                .path("notes/{id}")
                .buildAndExpand(savedNote.id())
                .toUri();

        return ResponseEntity.created(locationOfNewNote).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putNote(@PathVariable Long requestedId, @RequestBody Note noteUpdate, Principal principal) {
        Note note = findNote(requestedId, principal);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        Note updatedNote = new Note(note.id(), noteUpdate.title(), noteUpdate.content(), principal.getName());

        if (!isNoteValid(updatedNote, principal)) {
            return ResponseEntity.badRequest().build();
        }

        noteRepository.save(updatedNote);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteNote(@PathVariable Long id, Principal principal) {
        if (!noteRepository.existsByIdAndOwner(id, principal.getName())) {
            return ResponseEntity.notFound().build();
        }

        noteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
