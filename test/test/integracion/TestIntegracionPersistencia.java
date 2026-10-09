package test.integracion;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import persistencia.AsociadoDTO;
import persistencia.BaseDeDatos;
import persistencia.DataAccessObject;

public class TestIntegracionPersistencia {

    private DataAccessObject dao;
    private BaseDeDatos bd;

    @Before
    public void setUp() {
        // Obtenemos la instancia de base de datos y reseteamos el esquema con las semillas
        bd = BaseDeDatos.getInstancia();
        bd.iniciarBD(); // Asegura estado inicial limpio (20 asociados) para cada prueba
        dao = new DataAccessObject();
    }

    // ========================================================
    // 1. TEST PATRÓN SINGLETON Y CONECTIVIDAD
    // ========================================================
    @Test
    public void testSingletonYConexionActiva() {
        BaseDeDatos otraInstancia = BaseDeDatos.getInstancia();

        // assertSame verifica que ambas variables apunten exactamente a la misma posición de memoria
        assertSame("Debe cumplir con el patron Singleton devolviendo la misma instancia", bd, otraInstancia);

        Connection conexion = bd.getConexion();
        assertNotNull("La conexion a MySQL no debe ser nula", conexion);

        try {
            assertFalse("La conexion a MySQL debe encontrarse abierta", conexion.isClosed());
        } catch (SQLException e) {
            fail("Error al consultar el estado de la conexion: " + e.getMessage());
        }
    }

    // ========================================================
    // 2. TEST DE INICIALIZACIÓN DE TABLAS Y DATOS SEMILLA
    // ========================================================
    @Test
    public void testInicializacionDatosSemilla() {
        try {
            ArrayList<AsociadoDTO> lista = dao.cargarBD();
            assertNotNull("La lista cargada no debe ser nula", lista);
            assertEquals("La base de datos debe contener los 20 registros semilla iniciales", 20, lista.size());
        } catch (SQLException e) {
            fail("Fallo al recuperar la lista completa de asociados: " + e.getMessage());
        }
    }

    // ========================================================
    // 3. TEST DE CARGA INDIVIDUAL (MAPEO DE DATOS)
    // ========================================================
    @Test
    public void testCargarAsociadoPorId() {
        try {
            // El ID 1 en los datos semilla es: Juan State, DNI 12345678, Buenos Aires 123
            AsociadoDTO socio = dao.cargar(1);

            assertNotNull("El asociado con ID 1 debe existir en la base", socio);
            assertEquals("Juan", socio.getNombre());
            assertEquals("State", socio.getApellido());
            assertEquals("12345678", socio.getDni());
            assertEquals("Buenos Aires", socio.getCalle());
            assertEquals(123, socio.getNumero());
            assertEquals("Mar del Plata", socio.getCiudad());
            assertEquals("2235551234", socio.getTelefono());
        } catch (SQLException e) {
            fail("Error al cargar asociado por ID: " + e.getMessage());
        }
    }

    // ========================================================
    // 4. TEST DE CARGA CON LÍMITE
    // ========================================================
    @Test
    public void testCargarConLimite() {
        try {
            ArrayList<AsociadoDTO> lista = dao.cargarConLimite(5);
            assertNotNull(lista);
            assertEquals("Debe retornar exactamente 5 registros", 5, lista.size());
        } catch (SQLException e) {
            fail("Error al ejecutar consulta con limite: " + e.getMessage());
        }
    }
    
// ========================================================
    // 5. TEST DE ELIMINACIÓN EXITOSA POR DNI
    // ========================================================
    @Test
    public void testEliminarAsociadoExistente() throws SQLException {
        String dniAEliminar = "12345678";

        // Ejecutamos la baja lógica/física en la base
        dao.eliminar(dniAEliminar);

        // Verificamos que la cantidad total de registros se redujo a 19
        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        assertEquals("La base de datos debe contener 19 registros tras la eliminacion", 19, lista.size());

        // Verificamos que el DNI eliminado efectivamente ya no esté en la base
        boolean encontrado = false;
        for (AsociadoDTO a : lista) {
            if (dniAEliminar.equals(a.getDni())) {
                encontrado = true;
                break;
            }
        }
        assertFalse("El asociado eliminado no debe existir en la base de datos", encontrado);
    }

}
