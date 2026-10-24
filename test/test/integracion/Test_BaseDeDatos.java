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

public class Test_BaseDeDatos {

    private DataAccessObject dao;

    @Before
    public void setUp() {
        // Inicializamos la base de datos con los datos semilla limpios antes de cada test
        BaseDeDatos.getInstancia().iniciarBD();

        // Instanciamos el DAO: toda la comunicación y persistencia se hace a través de este objeto
        dao = new DataAccessObject();
    }

    /**
     * 1. Verifica que la infraestructura subyacente cumpla el patrón Singleton.
     */
    @Test
    public void testSingletonInstanciaUnica() {
        BaseDeDatos bd1 = BaseDeDatos.getInstancia();
        BaseDeDatos bd2 = BaseDeDatos.getInstancia();

        assertNotNull("La instancia no debe ser nula", bd1);
        assertSame("Ambas referencias deben ser el mismo objeto en memoria (Singleton)", bd1, bd2);
    }

    /**
     * 2. Verifica que la conexión con el motor MySQL esté abierta.
     */
    @Test
    public void testConexionAbierta() throws SQLException {
        Connection con = BaseDeDatos.getInstancia().getConexion();

        assertNotNull("La conexión no debe ser nula", con);
        assertFalse("La conexión a MySQL debe estar abierta", con.isClosed());
    }

    /**
     * 3. Verifica la carga completa de datos semilla utilizando el DAO (Read All).
     * El método cargarBD() devuelve una colección de objetos AsociadoDTO.
     */
    @Test
    public void testCargarBD_DatosSemilla() throws SQLException {
        ArrayList<AsociadoDTO> lista = dao.cargarBD();

        assertNotNull("La lista de asociados no debe ser nula", lista);
        assertEquals("Deben existir exactamente 20 registros iniciales en la base de datos", 20, lista.size());

        // Verificamos el mapeo DTO del primer registro semilla ("Juan State")
        AsociadoDTO primero = lista.get(0);
        assertEquals("Juan", primero.getNombre());
        assertEquals("State", primero.getApellido());
        assertEquals("12345678", primero.getDni());
        assertEquals("Buenos Aires", primero.getCalle());
        assertEquals(123, primero.getNumero());
        assertEquals("Mar del Plata", primero.getCiudad());
        assertEquals("2235551234", primero.getTelefono());
    }

    /**
     * 4. Verifica la consulta con límite utilizando el DAO (Read Paged/Limited).
     */
    @Test
    public void testCargarConLimite() throws SQLException {
        ArrayList<AsociadoDTO> lista = dao.cargarConLimite(5);

        assertNotNull("La lista no debe ser nula", lista);
        assertEquals("Debe retornar exactamente 5 registros", 5, lista.size());
    }

    /**
     * 5. Verifica la persistencia de una entidad del dominio (Create) a través del DAO.
     * dao.guardar() recibe un objeto Asociado del dominio, lo mapea internamente a AsociadoDTO
     * y lo inserta en la base de datos.
     */
    @Test
    public void testGuardarAsociado() throws SQLException {
        Domicilio domicilio = new Domicilio("Alvear", 500, "Mar del Plata");
        Asociado asociadoNuevo = new Asociado("Carlos", "Gomez", "44111222", "2234567890", domicilio);

        dao.guardar(asociadoNuevo);

        // Verificamos que ahora haya 21 asociados en la base de datos
        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        assertEquals(21, lista.size());

        // Buscamos el asociado recién insertado en la lista recuperada
        boolean encontrado = false;
        for (AsociadoDTO dto : lista) {
            if ("44111222".equals(dto.getDni())) {
                encontrado = true;
                assertEquals("Carlos", dto.getNombre());
                assertEquals("Gomez", dto.getApellido());
                assertEquals("Alvear", dto.getCalle());
                assertEquals(500, dto.getNumero());
                break;
            }
        }
        assertTrue("El nuevo asociado debe encontrarse en la base de datos", encontrado);
    }

    /**
     * 6. Verifica la eliminación de un asociado por DNI (Delete) mediante el DAO.
     */
    @Test
    public void testEliminarAsociado() throws SQLException {
        // DNI 12345678 corresponde al id 1 de los datos semilla
        dao.eliminar("12345678");

        ArrayList<AsociadoDTO> lista = dao.cargarBD();
        assertEquals("Deberían quedar 19 asociados tras eliminar uno", 19, lista.size());

        // Verificamos que el DNI eliminado ya no aparezca en la lista
        for (AsociadoDTO dto : lista) {
            assertNotEquals("El asociado eliminado no debe existir en la base", "12345678", dto.getDni());
        }
    }

    /**
     * 7. Detección de excepciones SQL al violar la restricción UNIQUE del DNI:
     * Si intentamos guardar una entidad Asociado con un DNI ya existente,
     * el DAO debe propagar la SQLException generada por MySQL.
     */
    @Test(expected = SQLException.class)
    public void testGuardarDniDuplicadoLanzaSQLException() throws SQLException {
        Domicilio domicilio = new Domicilio("San Martin", 100, "Mar del Plata");
        // DNI "12345678" ya existe en los datos semilla
        Asociado duplicado = new Asociado("Juan", "Duplicado", "12345678", "2230000000", domicilio);

        dao.guardar(duplicado);
    }

    /**
     * 8. Detección de fallo/inconsistencia en el código provisto:
     * El método dao.cargar(id) no invoca 'resultado.next()' antes de leer las columnas,
     * por lo que el cursor de JDBC está antes del primer registro y lanza SQLException.
     * Este test demuestra cómo la suite atrapa este error en el código provisto.
     */
    @Test(expected = SQLException.class)
    public void testCargarPorId_FallaPorFaltaDeNext() throws SQLException {
        dao.cargar(1);
    }
}
