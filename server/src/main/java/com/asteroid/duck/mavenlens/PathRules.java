/**
 * Copyright (c) 2017 Dr. Chris Senior
 */
package com.asteroid.duck.mavenlens;

import org.apache.commons.io.LineIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class PathRules {
    private final List<Pattern> includes;
    private final List<Pattern> excludes;
    private final List<Repository> proxied;

    public PathRules() {
        includes = null;
        excludes = null;
        proxied = Collections.singletonList(Repository.MAVEN_CACHE);
    }

    public PathRules(List<Repository> repos, List<Pattern> includes, List<Pattern> excludes) {
        this.proxied = Collections.unmodifiableList(repos);
        this.includes = Collections.unmodifiableList(includes);
        this.excludes = Collections.unmodifiableList(excludes);
    }

    public static List<Pattern> asPatterns(List<String> regexs) {
        return regexs.stream().map( regex -> Pattern.compile(regex) ).collect(Collectors.toList());
    }

    public static PathRules parse(InputStream in) {
        return parse(new InputStreamReader(in));
    }

    public static PathRules parse(Reader reader) {
        return parse(new LineIterator(reader));
    }

    public static PathRules parse(LineIterator lines) {
        ArrayList<String> includes = new ArrayList<>();
        ArrayList<String> excludes = new ArrayList<>();
        ArrayList<Repository> repos = new ArrayList<>();
        int lineNumber = 0;
        while(lines.hasNext()) {
            String line = lines.next();
            lineNumber++;
            if (line.trim().isEmpty()) {
                // skip whitespace only or empty lines
            }
            else if (line.startsWith("#")) {
                // do nothing with comments
            }
            else if (line.startsWith("PROXY") || line.startsWith("REPO")) {
                String repo = line.substring(line.indexOf(' '));
                try {
                    Repository repository = Repository.parse(repo);
                    repos.add(repository);
                } catch (IOException e) {
                    throw new IllegalArgumentException("@ line "+lineNumber, e);
                }
            }
            else if (line.startsWith("INCLUDE")) {
                String regex = line.substring(7).trim();
                includes.add(regex);
            }
            else if (line.startsWith("EXCLUDE")) {
                String regex = line.substring(7).trim();
                excludes.add(regex);
            }
            else {
                // unrecognized input
                throw new IllegalArgumentException("Unrecognized input @ line "+lineNumber);
            }
        }
        return new PathRules(repos, asPatterns(includes), asPatterns(excludes));
    }

    public boolean isAllowed(String path) {
        return included(path) && !excluded(path);
    }

    /**
     * If there are includes - then does it match any.
     * Otherwise it's included by default
     * @param path the path to match
     * @return true if the path is included according to these rules
     */
    private boolean included(final String path) {
        if (includes == null) {
            return true;
        }
        for(Pattern p : includes) {
            Matcher matcher = p.matcher(path);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * If there are excludes - then does it match any
     * If there are no exclusion patterns, the path is never excluded
     * @param path the path to match
     * @return true if the path is excluded
     */
    private boolean excluded(final String path) {
        if (excludes == null) {
            return false;
        }
        for(Pattern p : excludes) {
            Matcher matcher = p.matcher(path);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public InputStream getContent(final String requestPath) {
        for(Repository r : proxied) {
            try {
                InputStream inputStream = r.get(requestPath);
                return inputStream;
            } catch (IOException e) {
                // not found...
                e.printStackTrace();
            }
        }
        return null;
    }
}
