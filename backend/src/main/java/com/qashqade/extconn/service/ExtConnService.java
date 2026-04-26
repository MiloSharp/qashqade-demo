package com.qashqade.extconn.service;

import com.qashqade.extconn.model.ExtConn;
import com.qashqade.extconn.repository.ExtConnRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExtConnService {

    private final ExtConnRepository repo;

    public ExtConnService(ExtConnRepository repo) {
        this.repo = repo;
    }

    public Page<ExtConn> findAll(int page, int size, String search, String sortBy) {
        PageRequest pageRequest = PageRequest.of(
            page, size, Sort.by(Sort.Direction.ASC, sortBy)
        );
        if (search != null && !search.isBlank()) {
            return repo.findByNameContainingIgnoreCase(search.trim(), pageRequest);
        }
        return repo.findAll(pageRequest);
    }

    public Optional<ExtConn> findById(Long id) {
        return repo.findById(id);
    }

    public ExtConn save(ExtConn extConn) {
        return repo.save(extConn);
    }

    public ExtConn update(Long id, ExtConn incoming) {
        return repo.findById(id).map(existing -> {
            existing.setName(incoming.getName());
            existing.setBaseUrl(incoming.getBaseUrl());
            existing.setAuthLocation(incoming.getAuthLocation());
            existing.setAuthKey(incoming.getAuthKey());
            existing.setAuthValue(incoming.getAuthValue());
            existing.setAuthMethod(incoming.getAuthMethod());
            existing.setDescription(incoming.getDescription());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("ExtConn not found: " + id));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
