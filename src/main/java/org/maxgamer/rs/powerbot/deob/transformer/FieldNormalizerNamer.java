package org.maxgamer.rs.powerbot.deob.transformer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.transformers.normalizer.AbstractNormalizer;
import com.javadeobfuscator.deobfuscator.transformers.normalizer.CustomRemapper;
import com.javadeobfuscator.deobfuscator.transformers.normalizer.FieldNormalizer;
import com.javadeobfuscator.deobfuscator.utils.ClassTree;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Document this
 */
@TransformerConfig.ConfigOptions(configClass = FieldNormalizerNamer.Config.class)
public class FieldNormalizerNamer extends AbstractNormalizer<FieldNormalizerNamer.Config> {

    @Override
    public void remap(CustomRemapper remapper) {
        AtomicInteger id = new AtomicInteger(0);
        classNodes().forEach(classNode -> {
            ClassTree tree = this.getDeobfuscator().getClassTree(classNode.name);
            Set<String> allClasses = new HashSet<>();
            Set<String> tried = new HashSet<>();
            LinkedList<String> toTry = new LinkedList<>();
            toTry.add(tree.thisClass);
            while (!toTry.isEmpty()) {
                String t = toTry.poll();
                if (tried.add(t) && !t.equals("java/lang/Object")) {
                    ClassTree ct = this.getDeobfuscator().getClassTree(t);
                    allClasses.add(t);
                    allClasses.addAll(ct.parentClasses);
                    allClasses.addAll(ct.subClasses);
                    toTry.addAll(ct.parentClasses);
                    toTry.addAll(ct.subClasses);
                }
            }
            for (FieldNode fieldNode : classNode.fields) {
                List<String> references = new ArrayList<>();
                for (String possibleClass : allClasses) {
                    ClassNode otherNode = this.getDeobfuscator().assureLoaded(possibleClass);
                    boolean found = false;
                    for (FieldNode otherField : otherNode.fields) {
                        if (otherField.name.equals(fieldNode.name) && otherField.desc.equals(fieldNode.desc)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        references.add(possibleClass);
                    }
                }

                if(!isGarbageName(fieldNode)) {
                    continue;
                }

                if (!remapper.fieldMappingExists(classNode.name, fieldNode.name, fieldNode.desc)) {
                    AtomicInteger attempts = new AtomicInteger(0);
                    while (true) {
                        String newName = suggestName(classNode.fields, fieldNode, attempts.getAndIncrement());

                        if (remapper.mapFieldName(classNode.name, fieldNode.name, fieldNode.desc, newName, false)) {
                            for (String s : references) {
                                remapper.mapFieldName(s, fieldNode.name, fieldNode.desc, newName, true);
                            }
                            break;
                        }
                    }
                }
            }
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
            super(FieldNormalizerNamer.class);
        }

        public Set<String> getIllegalFields() {
            return illegalFields;
        }

        public void setIllegalFields(Set<String> illegalFields) {
            this.illegalFields = illegalFields;
        }
    }
}
