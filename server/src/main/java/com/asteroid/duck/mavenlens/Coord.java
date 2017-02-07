/**
 * Copyright (c) 2017 Dr. Chris Senior
 */
package com.asteroid.duck.mavenlens;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.*;

/**
 *
 */
public class Coord {
    private final String groupId;
    private final String artifactId;
    private final String platformId;
    private final String version;
    private final String classifier;
    private final String type;

    // ${groupId.replace('.','/')}/${artifactId}${platformId==null?'':'-'+platformId}/${version}/${artifactId}${platformId==null?'':'-'+platformId}-${version}${classifier==null?'':'-'+classifier}.${type}
    private final String FORMAT = "${groupId}/${artifactId}${platformId}/${version}/${artifactId}${platformId}-${version}${classifier}.${type}";

    public Coord(final String groupId, final String artifactId, final String platformId, final String version, final String classifier, final String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.platformId = platformId;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
    }

    public Coord(final String groupId, final String artifactId, final String version) {
        this(groupId, artifactId, null, version, null, "jar");
    }

    public static Coord parseColon(String str) {
        String[] strings = str.split(":");
        return from(strings);
    }

    public static Coord parseUri(String str) {
        if (str.startsWith("/")) {
            str = str.substring(1);
        }
        int lastDotIndex = str.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            throw new IllegalArgumentException("No . in path");
        }
        String type = str.substring(lastDotIndex + 1);
        int lastSlash = str.lastIndexOf('/');
        if (lastSlash <= 0) {
            throw new IllegalArgumentException("No / in path");
        }
        String dirs = str.substring(0, lastSlash);
        List<String> path = Arrays.asList(dirs.split("/"));
        String version = path.get(path.size() - 1);
        return null;
    }

    public static Coord from(final String ... args) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException("Not enough parameters");
        }
        if (args.length >= 6) {
            return new Coord(args[0], args[1], args[2], args[3], args[4], args[5]);
        }
        return new Coord(args[0], args[1], args[2]);
    }

    public String uriForm() {
        Map<String, String> values = new HashMap<>();
        values.put("groupId",  groupId.replace('.', '/'));
        values.put("artifactId", artifactId);
        values.put("platformId", (platformId == null ? "" : ("-" + platformId)));
        values.put("version", version);
        values.put("classifier", (classifier == null ? "" : ("-" + classifier)));
        values.put("type", type);
        StrSubstitutor sub = new StrSubstitutor(values);
        return sub.replace(FORMAT);
    }
    
    public List<String> components() {
        ArrayList<String> result = new ArrayList<>();
        result.add(groupId);
        result.add(artifactId);
        if (platformId != null) result.add(platformId);
        result.add(version);
        if (classifier != null) result.add(classifier);
        if (!type.equals("jar")) result.add(type);
        return result;
    }
    
    public String colonForm() {
        Iterator<String> iter = components().iterator();
        StringBuilder result = new StringBuilder();
        while(iter.hasNext()) {
            result.append(iter.next());
            if (iter.hasNext()) {
                result.append(':');
            }
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return uriForm();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Coord coord = (Coord) o;

        if (!groupId.equals(coord.groupId)) return false;
        if (!artifactId.equals(coord.artifactId)) return false;
        if (platformId != null ? !platformId.equals(coord.platformId) : coord.platformId != null) return false;
        if (!version.equals(coord.version)) return false;
        if (classifier != null ? !classifier.equals(coord.classifier) : coord.classifier != null) return false;
        return type.equals(coord.type);
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + (platformId != null ? platformId.hashCode() : 0);
        result = 31 * result + version.hashCode();
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }
}
