package org.maxgamer.rs.powerbot.deob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.javadeobfuscator.deobfuscator.Deobfuscator;
import com.javadeobfuscator.deobfuscator.DeobfuscatorMain;
import com.javadeobfuscator.deobfuscator.config.Configuration;
import com.javadeobfuscator.deobfuscator.config.TransformerConfig;
import com.javadeobfuscator.deobfuscator.config.TransformerConfigDeserializer;
import com.javadeobfuscator.deobfuscator.exceptions.NoClassInPathException;
import com.javadeobfuscator.deobfuscator.exceptions.PreventableStackOverflowError;
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
        Configuration configuration = getConfiguration(args);

        int result = run(configuration);
        if (result != 0) {
            System.exit(result);
        }

        JarUtil.cleanManifest(configuration.getOutput());

        extractJar(configuration);
    }

    private static int run(Configuration configuration) {
        Deobfuscator deobfuscator = new Deobfuscator(configuration);

        try {
            deobfuscator.start();
            return 0;
        } catch (NoClassInPathException ex) {
            for (int i = 0; i < 5; i++)
                System.out.println();
            System.out.println("** DO NOT OPEN AN ISSUE ON GITHUB **");
            System.out.println("Could not locate a class file.");
            System.out.println("Have you added the necessary files to the -path argument?");
            System.out.println("The error was:");
            ex.printStackTrace(System.out);
            return -2;
        } catch (PreventableStackOverflowError ex) {
            for (int i = 0; i < 5; i++)
                System.out.println();
            System.out.println("** DO NOT OPEN AN ISSUE ON GITHUB **");
            System.out.println("A StackOverflowError occurred during deobfuscation, but it is preventable");
            System.out.println("Try increasing your stack size using the -Xss flag");
            System.out.println("The error was:");
            ex.printStackTrace(System.out);
            return -3;
        } catch (Throwable t) {
            for (int i = 0; i < 5; i++)
                System.out.println();
            System.out.println("Deobfuscation failed. Please open a ticket on GitHub and provide the following error:");
            t.printStackTrace(System.out);
            return -1;
        }
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
                        continue;
                    }
                }
            }

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
