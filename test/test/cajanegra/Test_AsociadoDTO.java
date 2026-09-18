package test.cajanegra;

import static org.junit.Assert.*;

import org.junit.Test;

import persistencia.AsociadoDTO;

public class Test_AsociadoDTO {
    // test setNombre(string nombre)

    @Test // Carlos
    public void testSetNombre() {
        AsociadoDTO x = new AsociadoDTO();
        x.setNombre("Carlos");

        assertEquals("Carlos", x.getNombre());
    }


    @Test(expected = AssertionError.class) // null
    public void testSetNombreNull() {
        AsociadoDTO x = new AsociadoDTO();
        x.setNombre(null); 
    }

}

