package com.asteroid.duck.mavenlens;

import org.junit.Test;

import static org.junit.Assert.*;

public class CoordTest {
    private Coord simple = new Coord("org.simple", "simple", "4.0.0");
    private Coord complex = new Coord("org.complex", "some-very-hard", "win32", "4.0.0-SNAPSHOT", "release", "war");

    @Test
    public void uriForm() throws Exception {
        String uriForm = simple.uriForm();
        assertEquals("org/simple/simple/4.0.0/simple-4.0.0.jar", uriForm);
        uriForm = complex.uriForm();
        assertEquals("org/complex/some-very-hard-win32/4.0.0-SNAPSHOT/some-very-hard-win32-4.0.0-SNAPSHOT-release.war", uriForm);
    }

    @Test
    public void parse() {
        Coord parsed = Coord.parse("org.simple:simple:4.0.0");
        assertEquals(simple, parsed);
        parsed = Coord.parse("org.complex:some-very-hard:win32:4.0.0-SNAPSHOT:release:war");
        assertEquals(complex, parsed);
    }

    @Test
    public void colonForm() throws Exception {
        String uriForm = simple.colonForm();
        assertEquals("org.simple:simple:4.0.0", uriForm);
        uriForm = complex.colonForm();
        assertEquals("org.complex:some-very-hard:win32:4.0.0-SNAPSHOT:release:war", uriForm);
    }

}