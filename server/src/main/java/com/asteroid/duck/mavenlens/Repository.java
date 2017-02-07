/**
 * Copyright (c) 2017 Dr. Chris Senior
 */
package com.asteroid.duck.mavenlens;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public abstract class Repository {

    public static final Repository MAVEN_CENTRAL;
    public static final Repository MAVEN_CACHE;

    static {
        URL url = null;
        try {
            url = new URL(System.getProperty("maven.central.url", "http://repo1.maven.org/maven2"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        MAVEN_CENTRAL = remote(url);
        MAVEN_CACHE = local(new File(System.getProperty("user.home"), ".m2"+File.separator+"repository"));
    }

    public abstract InputStream get(String path) throws IOException;

    private static class Local extends Repository {
        private final File base;

        public Local(final File base) {
            this.base = base;
        }

        @Override
        public InputStream get(final String path) throws IOException {
            String localPath = path.replace('/', File.separatorChar);
            if (localPath.startsWith(File.separator)) {
                localPath = localPath.substring(File.separator.length());
            }
            return new FileInputStream(new File(base, localPath));
        }
    }

    private static class Remote extends Repository {

        private final URL base;

        public Remote(final URL base) {
            this.base = base;
        }

        @Override
        public InputStream get(final String path) throws IOException {
            return new URL(base, path).openStream();
        }
    }

    public static Repository local(File base) {
        return new Local(base);
    }

    public static Repository remote(URL base) {
        return new Remote(base);
    }

    public static Repository parse(String repo) throws IOException {
        if (repo.startsWith("remote")) {
            String url = repo.substring(6).trim();
            return remote(new URL(url));
        }
        else if (repo.startsWith("local")) {
            String dir = repo.substring(5).trim();
            return local(new File(dir));
        }
        throw new IllegalArgumentException("Unrecognized repository : "+repo);
    }
}
