package com.dataox.service.impl;

import com.dataox.model.Tag;
import com.dataox.repository.TagRepository;
import com.dataox.service.TagService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public Set<Tag> resolveTags(Set<Tag> tags) {
        return tags.stream()
                .map(this::findOrCreateTag)
                .collect(Collectors.toSet());
    }

    @Override
    public Tag findOrCreateTag(Tag tag) {
        return tagRepository.findByName(tag.getName())
                .orElseGet(() -> tagRepository.save(tag));
    }
}
