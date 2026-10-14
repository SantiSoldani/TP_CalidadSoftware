package test.integracion;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.AtendiendoEnDomicilioState;
import modelo.modeloDominio.ambulancia.DisponibleState;
import modelo.modeloDominio.ambulancia.EnTallerState;
import modelo.modeloDominio.ambulancia.RegresandoClinicaSinPacienteState;
import modelo.modeloDominio.ambulancia.RegresandoDelTallerState;
import modelo.modeloDominio.ambulancia.RetornoAutomatico;
import modelo.modeloDominio.ambulancia.TrasladandoPacienteClinicaState;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;
import modelo.modeloDominio.util.Domicilio;

public class TestAmbulanciaIntegracion {

    private Ambulancia ambulancia;
    private Asociado asociado1;
    private Asociado asociado2;
    private Operario operario;

    @Before
    public void setUp() {
        // Escenario: ambulancia nueva (DisponibleState, libre y con simulacion activa)
        ambulancia = new Ambulancia();
        Domicilio domicilio = new Domicilio("Colon", 123, "Mar del Plata");
        asociado1 = new Asociado("Juan", "Perez", "12345678", "2235551234", domicilio);
        asociado2 = new Asociado("Maria", "Lopez", "11223344", "2235558765", domicilio);
        operario = new Operario("Pedro", "Gomez", "87654321", "2235555678", domicilio, ambulancia);
    }

    @After
    public void tearDown() {
        ambulancia.setSimulacionActiva(false);
    }

    // ========================================================
    // 1. FLUJOS COMPLETOS DE LA MAQUINA DE ESTADOS
    // ========================================================
    @Test
    public void testTrasladoCompleto() {
        ambulancia.trasladarALaClinica(asociado1);
        assertTrue("Debe estar trasladando al paciente", ambulancia.getEstadoActual() instanceof TrasladandoPacienteClinicaState);
        assertTrue(ambulancia.isOcupado());

        ambulancia.regresarSinPaciente();
        assertTrue("Despues del traslado debe volver a Disponible", ambulancia.getEstadoActual() instanceof DisponibleState);
        assertFalse(ambulancia.isOcupado());
    }

    @Test
    public void testAtencionDomicilioCompleta() {
        ambulancia.atenderDomicilio(asociado1);
        assertTrue("Debe estar atendiendo en domicilio", ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
        assertTrue(ambulancia.isOcupado());

        ambulancia.regresarSinPaciente();
        assertTrue("Debe estar regresando sin paciente", ambulancia.getEstadoActual() instanceof RegresandoClinicaSinPacienteState);
        assertFalse(ambulancia.isOcupado());

        ambulancia.retornoAutomatico(new RetornoAutomatico(ambulancia));
        assertTrue("Debe volver a Disponible", ambulancia.getEstadoActual() instanceof DisponibleState);
    }

    @Test
    public void testMantenimientoCompleto() {
        ambulancia.solicitarMantenimiento(operario);
        assertTrue("Debe estar en el taller", ambulancia.getEstadoActual() instanceof EnTallerState);
        assertTrue(ambulancia.isOcupado());

        ambulancia.volviendoDelTaller();
        assertTrue("Debe estar regresando del taller", ambulancia.getEstadoActual() instanceof RegresandoDelTallerState);
        assertFalse(ambulancia.isOcupado());

        ambulancia.retornoAutomatico(new RetornoAutomatico(ambulancia));
        assertTrue("Debe volver a Disponible", ambulancia.getEstadoActual() instanceof DisponibleState);
    }

    @Test
    public void testTrasladandoRechazaMantenimiento() {
        ambulancia.trasladarALaClinica(asociado1);

        ambulancia.getEstadoActual().SolicitudMantenimiento();

        assertTrue("No debe cambiar de estado mientras traslada", ambulancia.getEstadoActual() instanceof TrasladandoPacienteClinicaState);
    }

    // ========================================================
    // 2. CONCURRENCIA - ESPERA Y LIBERACION DE LA AMBULANCIA
    // ========================================================
    @Test
    public void testSegundoPedidoEsperaHastaQueSeLibere() throws InterruptedException {
        ambulancia.atenderDomicilio(asociado1);

        Thread hilo = new Thread(() -> ambulancia.trasladarALaClinica(asociado2));
        hilo.start();
        Thread.sleep(500);

        assertEquals("El segundo pedido debe quedar esperando", Thread.State.WAITING, hilo.getState());
        assertTrue(ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);

        ambulancia.regresarSinPaciente();
        hilo.join(2000);

        assertFalse("El hilo debe terminar al liberarse la ambulancia", hilo.isAlive());
        assertTrue("Debe atender el traslado pendiente", ambulancia.getEstadoActual() instanceof TrasladandoPacienteClinicaState);
        assertTrue(ambulancia.isOcupado());
    }

    @Test
    public void testFinalizarSimulacionDespiertaHilosEnEspera() throws InterruptedException {
        ambulancia.atenderDomicilio(asociado1);

        Thread hilo = new Thread(() -> ambulancia.trasladarALaClinica(asociado2));
        hilo.start();
        Thread.sleep(500);

        ambulancia.setSimulacionActiva(false);
        hilo.join(2000);

        assertFalse("El hilo en espera debe terminar al finalizar la simulacion", hilo.isAlive());
        assertTrue("No debe atender el pedido pendiente", ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
    }

    @Test
    public void testRegresoNoBloqueaLaAmbulancia() throws InterruptedException {
        ambulancia.trasladarALaClinica(asociado1);

        Thread hilo = new Thread(() -> ambulancia.regresarSinPaciente());
        hilo.start();
        Thread.sleep(100);

        long inicio = System.currentTimeMillis();
        ambulancia.isOcupado();
        long demora = System.currentTimeMillis() - inicio;
        hilo.join(2000);

        assertTrue("Consultar la ambulancia no deberia quedar bloqueado (" + demora + " ms)", demora < 100);
    }

    // ========================================================
    // 3. HILO DE RETORNO AUTOMATICO
    // ========================================================
    @Test
    public void testRetornoAutomaticoConSimulacionActiva() throws InterruptedException {
        ambulancia.atenderDomicilio(asociado1);

        Thread hilo = new Thread(new RetornoAutomatico(ambulancia));
        hilo.start();
        Thread.sleep(11000);

        assertTrue("El retorno automatico debe hacer volver a la ambulancia", ambulancia.getEstadoActual() instanceof RegresandoClinicaSinPacienteState);
    }

}
