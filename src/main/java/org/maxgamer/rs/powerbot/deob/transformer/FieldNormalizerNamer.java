package org.maxgamer.rs.powerbot.deob.transformer;

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
                if (!remapper.fieldMappingExists(classNode.name, fieldNode.name, fieldNode.desc)) {
                    while (true) {
                        String newName = "field" + id.getAndIncrement();
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
    }/*

    public String suggestName(FieldNode field) {

    }*/

    public static class Config extends AbstractNormalizer.Config {
        public Config() {
            super(FieldNormalizerNamer.class);
        }
    }
}
