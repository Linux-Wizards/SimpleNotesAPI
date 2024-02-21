package com.linuxwizards.simplenotesapi;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

interface NoteRepository extends CrudRepository<Note, Long>, PagingAndSortingRepository<Note, Long> {
    Note findByIdAndOwner(Long id, String owner);
    Page<Note> findByOwner(String owner, PageRequest pageRequest);

    boolean existsByIdAndOwner(Long id, String owner);
}
