package org.maxgamer.rs.powerbot.deob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.javadeobfuscator.deobfuscator.DeobfuscatorMain;
import com.javadeobfuscator.deobfuscator.config.Configuration;
import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.config.TransformerConfigDeserializer;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * TODO: Document this
 */
public class Main {
    public static void main(String[] args) throws Exception {
        int result = DeobfuscatorMain.run(args);
        if (result != 0) {
            System.exit(result);
        }

        Configuration configuration = getConfiguration(args);
        extractJar(configuration);
    }

    public static int extractJar(Configuration config) throws IOException {
        File deobJar = config.getOutput();

        ZipFile zipFile = new ZipFile(deobJar);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        File classesRoot = new File(deobJar.getAbsoluteFile().getParentFile(), "classes");

        long updated = 0;
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();

            String name = entry.getName();

            File outputClass = new File(classesRoot, name);
            byte[] buffer = new byte[1024];
            if (outputClass.exists()) {

                try (FileInputStream crcInput = new FileInputStream(outputClass)) {
                    CRC32 crc = new CRC32();

                    int n;
                    while ((n = crcInput.read(buffer)) > 0) {
                        crc.update(buffer, 0, n);
                    }

                    long expected = crc.getValue();
                    if (expected == entry.getCrc()) {
                        System.out.println("Unmodified: " + entry.getName());
                        continue;
                    }
                }
            }

            System.out.println("Modified: " + entry.getName());
            int n;
            try (InputStream in = zipFile.getInputStream(entry);
                 FileOutputStream out = new FileOutputStream(outputClass)) {
                while ((n = in.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }
            }
            updated++;
        }

        System.out.println("-- Total " + updated + " updated files --");

        return 0;
    }

    public static Configuration getConfiguration(String[] args) throws IOException, ParseException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .registerModule(
                        new SimpleModule().addDeserializer(TransformerConfig.class, new TransformerConfigDeserializer(LoggerFactory.getLogger(Main.class)))
                );

        Options options = new Options();
        options.addOption("c", "config", true, "The configuration file to use");

        CommandLineParser cmdlineParser = new DefaultParser();
        CommandLine cmdLine = cmdlineParser.parse(options, args);

        return mapper.readValue(new File(cmdLine.getOptionValue("config")), Configuration.class);
    }
}
