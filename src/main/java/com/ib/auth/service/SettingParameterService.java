package com.ib.auth.service;

import com.ib.auth.dto.SettingParameterDto;
import com.ib.auth.repository.SettingParameterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingParameterService {

    private final SettingParameterRepository repository;

    public SettingParameterService(SettingParameterRepository repository) {
        this.repository = repository;
    }

    public List<SettingParameterDto> getAll() {
        return repository.findAll();
    }

    public SettingParameterDto getById(Long id) {
        SettingParameterDto param = repository.findById(id);
        if (param == null) {
            throw new RuntimeException("setting.not_found");
        }
        return param;
    }

    public SettingParameterDto getByName(String name) {
        SettingParameterDto param = repository.findByName(name);
        if (param == null) {
            throw new RuntimeException("setting.not_found");
        }
        return param;
    }

    public SettingParameterDto create(SettingParameterDto request, String createdBy) {
        if (repository.findByName(request.getName()) != null) {
            throw new RuntimeException("setting.already_exists");
        }
        repository.insert(request, createdBy);
        return repository.findByName(request.getName());
    }

    public SettingParameterDto update(Long id, SettingParameterDto request, String updatedBy) {
        SettingParameterDto existing = repository.findById(id);
        if (existing == null) {
            throw new RuntimeException("setting.not_found");
        }
        SettingParameterDto duplicate = repository.findByName(request.getName());
        if (duplicate != null && !duplicate.getId().equals(id)) {
            throw new RuntimeException("setting.already_exists");
        }
        request.setId(id);
        repository.update(request, updatedBy);
        return repository.findById(id);
    }

    public void delete(Long id) {
        SettingParameterDto existing = repository.findById(id);
        if (existing == null) {
            throw new RuntimeException("setting.not_found");
        }
        repository.deleteById(id);
    }
}
