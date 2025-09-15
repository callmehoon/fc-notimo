package com.jober.final2teamdrhong.service.validator;

import com.jober.final2teamdrhong.repository.PhoneBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhoneBookValidator {

    private final PhoneBookRepository phoneBookRepository;
}
