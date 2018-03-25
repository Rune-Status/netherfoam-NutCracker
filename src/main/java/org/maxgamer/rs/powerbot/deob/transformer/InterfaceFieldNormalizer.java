package org.maxgamer.rs.powerbot.deob.transformer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.normalizer.AbstractNormalizer;
import com.javadeobfuscator.deobfuscator.transformers.normalizer.CustomRemapper;
import com.javadeobfuscator.deobfuscator.utils.ClassTree;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Document this
 */
@TransformerConfig.ConfigOptions(configClass = InterfaceFieldNormalizer.Config.class)
public class InterfaceFieldNormalizer extends AbstractNormalizer<InterfaceFieldNormalizer.Config> {

    @Override
    public void remap(CustomRemapper remapper) {
        AtomicInteger id = new AtomicInteger(0);
        classNodes().forEach(classNode -> {
            classNode.
        });
    }

    public boolean isGarbageName(FieldNode field) {
        if (getConfig().illegalFields.contains(field.name)) return true;
        if (field.name.length() < 4) return true;
        char firstletter = field.name.charAt(0);

        // names can't start with letters
        if (firstletter >= 0 && firstletter <= 9) return true;
        if (firstletter == '$') return true;

        return false;
    }

    public String suggestName(List<FieldNode> allFields, FieldNode field, int attempt) {
        boolean collisions = false;
        for (FieldNode other : allFields) {
            if (other.desc.equalsIgnoreCase(field.desc)) {
                collisions = true;
                break;
            }
        }

        String desc = field.desc;

        String[] parts = desc.split("/");
        String lastPart = parts[parts.length - 1];
        String name = lastPart;

        if (name.endsWith(";")) {
            name = lastPart.substring(0, name.length() - 1);
        }

        if (Character.isUpperCase(name.charAt(0))) {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }

        if (desc.startsWith("[")) {
            name = name + "s";
        }

        if (!collisions) {
            return name;
        }

        return name + "_" + attempt;
    }

    public static class Config extends AbstractNormalizer.Config {
        @JsonProperty
        private Set<String> illegalFields;

        public Config() {
            super(InterfaceFieldNormalizer.class);
        }

        public Set<String> getIllegalFields() {
            return illegalFields;
        }

        public void setIllegalFields(Set<String> illegalFields) {
            this.illegalFields = illegalFields;
        }
    }
}
