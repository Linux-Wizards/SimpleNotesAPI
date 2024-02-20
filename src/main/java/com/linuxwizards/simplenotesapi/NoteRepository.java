package com.linuxwizards.simplenotesapi;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface NoteRepository extends CrudRepository<Note, Long>, PagingAndSortingRepository<Note, Long> {
}
