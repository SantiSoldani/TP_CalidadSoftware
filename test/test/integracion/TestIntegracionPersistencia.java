package test.integracion;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.util.Domicilio;
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
    // 1. CONECTIVIDAD Y PATRÓN SINGLETON
    // ========================================================

    @Test
    public void test1_SingletonYConexionActiva() {
        BaseDeDatos otraInstancia = BaseDeDatos.getInstancia();

        // assertSame verifica que ambas variables apunten a la misma posición en memoria
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
    // 2. CREATE - OPERACIONES DE INSERCIÓN (guardar)
    // ========================================================

    @Test
    public void test2_GuardarAsociadoExitoso() throws SQLException {
        Domicilio dom = new Domicilio("Alvear", 500, "Mar del Plata");
        Asociado nuevo = new Asociado("Lucas", "Gomez", "99112233", "2234005566", dom);

        dao.guardar(nuevo);

        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        assertEquals("La base de datos debe contener 21 registros tras la insercion", 21, lista.size());

        // Verificamos que el nuevo DNI exista y conserve los atributos ingresados
        boolean encontrado = false;
        for (AsociadoDTO a : lista) {
            if ("99112233".equals(a.getDni())) {
                encontrado = true;
                assertEquals("Lucas", a.getNombre());
                assertEquals("Gomez", a.getApellido());
                assertEquals("Alvear", a.getCalle());
                assertEquals(500, a.getNumero());
                assertEquals("Mar del Plata", a.getCiudad());
                assertEquals("2234005566", a.getTelefono());
                break;
            }
        }
        assertTrue("El nuevo asociado debe persistirse correctamente en la base", encontrado);
    }

    @Test(expected = SQLException.class)
    public void test3_GuardarDniDuplicadoLanzaSQLException() throws SQLException {
        // '12345678' ya existe en las semillas iniciales (Juan State)
        Domicilio dom = new Domicilio("Mitre", 100, "Mar del Plata");
        Asociado duplicado = new Asociado("Pedro", "Otro", "12345678", "2234999999", dom);

        // La base debe rechazar la violacion de unicidad en la columna DNI
        dao.guardar(duplicado);
    }

    @Test(expected = AssertionError.class)
    public void test4_GuardarAsociadoNuloDisparaAssertionError() throws SQLException {
        dao.guardar(null);
    }

    // ========================================================
    // 3. READ - OPERACIONES DE CONSULTA (cargarBD, cargar, cargarConLimite)
    // ========================================================

    @Test
    public void test5_InicializacionDatosSemilla() {
        try {
            ArrayList<AsociadoDTO> lista = dao.cargarBD();
            assertNotNull("La lista cargada no debe ser nula", lista);
            assertEquals("La base de datos debe contener los 20 registros semilla iniciales", 20, lista.size());
        } catch (SQLException e) {
            fail("Fallo al recuperar la lista completa de asociados: " + e.getMessage());
        }
    }

    @Test
    public void test6_CargarAsociadoPorId() {
        try {
            // El ID 1 en las semillas es: Juan State, DNI 12345678, Buenos Aires 123
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

    @Test
    public void test7_CargarConLimite() {
        try {
            ArrayList<AsociadoDTO> lista = dao.cargarConLimite(5);
            assertNotNull("La lista recuperada no debe ser nula", lista);
            assertEquals("Debe retornar exactamente 5 registros", 5, lista.size());
        } catch (SQLException e) {
            fail("Error al ejecutar consulta con limite: " + e.getMessage());
        }
    }

    @Test(expected = AssertionError.class)
    public void test8_CargarIdInvalidoDisparaAssertionError() throws SQLException {
        dao.cargar(0);
    }

    @Test(expected = AssertionError.class)
    public void test9_CargarConLimiteInvalidoDisparaAssertionError() throws SQLException {
        dao.cargarConLimite(0);
    }

    // ========================================================
    // 4. UPDATE - OPERACIONES DE MODIFICACIÓN (actualizar)
    // ========================================================

    @Test
    public void test10_ActualizarAsociadoIdUno() throws SQLException {
        // En BaseDeDatos.java, ejecutarActualizacion tiene fijado el id en 1 (stmt.setInt(8, 1))
        Domicilio domActualizado = new Domicilio("San Martin", 999, "Mar del Plata");
        Asociado modificado = new Asociado("JuanModificado", "StateModificado", "12345678", "2235550000", domActualizado);

        dao.actualizar(modificado);

        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        AsociadoDTO socio1 = null;
        for (AsociadoDTO a : lista) {
            if ("12345678".equals(a.getDni())) {
                socio1 = a;
                break;
            }
        }

        assertNotNull("El asociado con ID 1 debe existir tras la actualizacion", socio1);
        assertEquals("JuanModificado", socio1.getNombre());
        assertEquals("StateModificado", socio1.getApellido());
        assertEquals("San Martin", socio1.getCalle());
        assertEquals(999, socio1.getNumero());
        assertEquals("2235550000", socio1.getTelefono());
    }

    @Test(expected = AssertionError.class)
    public void test11_ActualizarAsociadoNuloDisparaAssertionError() throws SQLException {
        dao.actualizar(null);
    }

    // ========================================================
    // 5. DELETE - OPERACIONES DE ELIMINACIÓN (eliminar)
    // ========================================================

    @Test
    public void test12_EliminarAsociadoExistente() throws SQLException {
        String dniAEliminar = "12345678";

        // Ejecutamos la baja en la base
        dao.eliminar(dniAEliminar);

        // Verificamos que la cantidad total de registros se redujo a 19
        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        assertEquals("La base de datos debe contener 19 registros tras la eliminacion", 19, lista.size());

        // Verificamos que el DNI eliminado efectivamente ya no esté presente
        boolean encontrado = false;
        for (AsociadoDTO a : lista) {
            if (dniAEliminar.equals(a.getDni())) {
                encontrado = true;
                break;
            }
        }
        assertFalse("El asociado eliminado no debe existir en la base de datos", encontrado);
    }

    @Test
    public void test13_EliminarDniInexistenteNoModificaBase() throws SQLException {
        dao.eliminar("99999999");
        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        assertEquals("La base de datos debe mantener los 20 registros intactos", 20, lista.size());
    }

    @Test(expected = AssertionError.class)
    public void test14_EliminarDniVacioDisparaAssertionError() throws SQLException {
        // La precondición del método rechaza la cadena vacía ("")
        dao.eliminar("");
    }

}
