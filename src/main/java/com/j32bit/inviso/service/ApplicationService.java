package com.j32bit.inviso.service;

import com.j32bit.inviso.domain.Application;
import com.j32bit.inviso.dto.ApplicationDto;
import com.j32bit.inviso.dto.FormComponentDto;
import com.j32bit.inviso.dto.OptionDto;
import com.j32bit.inviso.dto.ValidationDto;
import com.j32bit.inviso.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    public List<FormComponentDto> getAllComponents() {
        return applicationRepository.getAllComponents()
                .stream().map(e -> modelMapper.map(e, FormComponentDto.class))
                .collect(Collectors.toList());
    }

    public List<OptionDto> getAllOptionTypes() {
        return applicationRepository.getAllOptions()
                .stream().map(e -> modelMapper.map(e, OptionDto.class))
                .collect(Collectors.toList());
    }

    public List<ValidationDto> getAllValidations() {
        return applicationRepository.getAllValidations()
                .stream().map(e -> modelMapper.map(e, ValidationDto.class))
                .collect(Collectors.toList());
    }

    public Application save(ApplicationDto applicationDto) {
        return applicationRepository.save(modelMapper.map(applicationDto, Application.class));
    }
}
