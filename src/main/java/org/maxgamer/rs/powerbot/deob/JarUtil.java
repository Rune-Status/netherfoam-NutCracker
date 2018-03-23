package org.maxgamer.rs.powerbot.deob;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * TODO: Document this
 */
public class JarUtil {
    public static void cleanManifest(File jar) throws IOException {
        File output = new File(jar.getParentFile(), jar.getName() + ".manifestless.jar");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));

        ZipFile input = new ZipFile(jar);

        for (Enumeration e = input.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) e.nextElement();

            if (!entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
                out.putNextEntry(entry);
                try (InputStream content = input.getInputStream(entry)) {
                    IOUtils.copy(content, out);
                }
                out.closeEntry();
                continue;
            }

            rewrite(input.getInputStream(entry), out);
        }
        out.closeEntry();
        out.close();
    }

    public static void rewrite(InputStream manifest, ZipOutputStream output) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(manifest.available());
        PrintStream ps = new PrintStream(buffer);

        Scanner sc = new Scanner(manifest);
        while(sc.hasNextLine()) {
            String line = sc.nextLine();

            if (line.startsWith("Name:") && line.contains(".class")) {
                continue;
            }
            if (line.startsWith("SHA-256-Digest:")) {
                continue;
            }

            ps.println(line);
        }
        ZipEntry entry = new ZipEntry("META-INF/MANIFEST.MF");
        output.putNextEntry(entry);
        output.write(buffer.toByteArray());
        output.closeEntry();

        sc.close();
        buffer.close();
        ps.close();
    }
}
