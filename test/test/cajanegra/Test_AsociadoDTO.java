package test.cajanegra;

import static org.junit.Assert.*;

import org.junit.Before;
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
    
    
 // test setApellido(string apellido)

    @Test // Perez
    public void testSetApellido() {
        AsociadoDTO x = new AsociadoDTO();
        x.setApellido("Perez");

        assertEquals("Perez", x.getApellido());
    }


 // test setDNI(string dni)
    @Test(expected = AssertionError.class) // null
    public void testSetApellidoNull() {
        AsociadoDTO x = new AsociadoDTO();
        x.setApellido(null); 
    }
    
    
    
    @Test 
    public void testSetDni() {
        AsociadoDTO a1 = new AsociadoDTO();
        a1.setDni("123456");
    }

    @Test 
    public void testSetDniLetras() {
        AsociadoDTO a1 = new AsociadoDTO();
        a1.setDni("abcdef");
    }

    @Test (expected = AssertionError.class)
    public void testSetDniNull() {
        AsociadoDTO a1 = new AsociadoDTO();
        a1.setDni(null);
    }


    @Test
    public void testDniduplicado() {
        // En testing unitario aislado del DTO, dos instancias distintas 
        // pueden tener el mismo DNI porque el DTO no maneja colecciones ni persistencia.
        AsociadoDTO socio1 = new AsociadoDTO();
        AsociadoDTO socio2 = new AsociadoDTO();

        socio1.setDni("12345678");
        socio2.setDni("12345678");

        // Ambos objetos retienen el valor asignado sin arrojar excepción.
        // La restricción de unicidad del SRS 3.1 debe delegarse a la capa de persistencia (DAO/BD).
        assertEquals("12345678", socio1.getDni());
        assertEquals("12345678", socio2.getDni());
    }
    
    
    // test setNumero (int num)
    // creamos el escenario 
    
    private AsociadoDTO asociado;
    
    @Before
    public void setUp() {
        asociado = new AsociadoDTO();
    }
    
    @Test
    public void testSetNumeroValorTipico() {
        asociado.setNumero(23);

        assertEquals(23, asociado.getNumero());
    }

   
    @Test
    public void testSetNumeroValorLimiteCero() {
        
        asociado.setNumero(0);

        assertEquals("El número 0 debe ser aceptado según SRS 3.1", 0, asociado.getNumero());
    }

  
    @Test(expected = AssertionError.class)
    public void testSetNumeroValorInvalidoNegativo() {
        
        asociado.setNumero(-2);
    }
}

