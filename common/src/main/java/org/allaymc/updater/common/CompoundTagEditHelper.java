package org.allaymc.updater.common;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;

// TODO: Refactor this class
@SuppressWarnings("unchecked")
public class CompoundTagEditHelper {
    private final Deque<Object> parentTag = new ArrayDeque<>();
    private final Deque<String> tagName = new ArrayDeque<>();

    @Getter
    private Map<String, Object> rootTag;
    @Getter
    private Object tag;

    public CompoundTagEditHelper(Map<String, Object> tag) {
        this.rootTag = tag;
        this.tag = tag;
    }

    public Map<String, Object> getCompoundTag() {
        return (Map<String, Object>) tag;
    }

    public Map<String, Object> getParent() {
        if (!this.parentTag.isEmpty()) {
            var tag = this.parentTag.peekLast();
            if (tag instanceof Map) {
                return (Map<String, Object>) tag;
            }
        }

        return null;
    }

    public boolean canPopChild() {
        return !this.parentTag.isEmpty();
    }

    public void popChild() {
        if (!this.parentTag.isEmpty()) {
            this.tag = this.parentTag.pollLast();
            this.tagName.pollLast();
        }
    }

    public void pushChild(String name) {
        Objects.requireNonNull(name, "name");
        if (!(tag instanceof Map)) throw new IllegalStateException("Tag is not of Compound type");

        this.parentTag.addLast(this.tag);
        this.tagName.addLast(name);
        this.tag = ((Map<String, Object>) this.tag).get(name);
    }

    public void replaceWith(String name, Object newTag) {
        this.tag = newTag;
        if (this.parentTag.isEmpty()) {
            this.rootTag = ((Map<String, Object>) tag);
            return;
        }

        var tag = this.parentTag.getLast();
        if (tag instanceof Map) {
            var map = (Map<String, Object>) tag;
            map.remove(this.tagName.pollLast());
            map.put(name, newTag);
            this.tagName.offerLast(name);
        }
    }
}
