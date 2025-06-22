package com.dataox.service;

import com.dataox.model.Tag;
import java.util.Set;

public interface TagService {
    Set<Tag> resolveTags(Set<Tag> tags);

    Tag findOrCreateTag(Tag tag);
}
