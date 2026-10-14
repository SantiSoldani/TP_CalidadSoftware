package test.integracion;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.RetornoAutomatico;
import modelo.modeloDominio.ambulancia.State;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;
import modelo.modeloDominio.util.Domicilio;

public class TestAmbulanciaMocks {

    private Ambulancia ambulancia;
    private State estadoMock;
    private Asociado asociado;
    private Operario operario;

    @Before
    public void setUp() {
        // Escenario: ambulancia libre, con simulacion activa y un mock como estado actual
        ambulancia = new Ambulancia();
        estadoMock = mock(State.class);
        ambulancia.setState(estadoMock);

        Domicilio domicilio = new Domicilio("Colon", 123, "Mar del Plata");
        asociado = new Asociado("Juan", "Perez", "12345678", "2235551234", domicilio);
        operario = new Operario("Pedro", "Gomez", "87654321", "2235555678", domicilio, ambulancia);
    }

    // ========================================================
    // 1. PEDIDOS CON LA AMBULANCIA LIBRE
    // ========================================================
    @Test
    public void testTrasladarALaClinica() {
        ambulancia.trasladarALaClinica(asociado);

        verify(estadoMock).SolicitudDeTraslado();
        verifyNoMoreInteractions(estadoMock);
        assertTrue("La ambulancia debe quedar ocupada", ambulancia.isOcupado());
    }

    @Test
    public void testAtenderDomicilio() {
        ambulancia.atenderDomicilio(asociado);

        verify(estadoMock).SolicitudDeAtencionDomicilio();
        verifyNoMoreInteractions(estadoMock);
        assertTrue("La ambulancia debe quedar ocupada", ambulancia.isOcupado());
    }

    @Test
    public void testSolicitarMantenimiento() {
        ambulancia.solicitarMantenimiento(operario);

        verify(estadoMock).SolicitudMantenimiento();
        verifyNoMoreInteractions(estadoMock);
        assertTrue("La ambulancia debe quedar ocupada", ambulancia.isOcupado());
    }

    // ========================================================
    // 2. RETORNOS
    // ========================================================
    @Test
    public void testRegresarSinPaciente() {
        ambulancia.setOcupado(true);

        ambulancia.regresarSinPaciente();

        verify(estadoMock).RetornoClinica();
        verifyNoMoreInteractions(estadoMock);
        assertFalse("La ambulancia debe quedar libre", ambulancia.isOcupado());
    }

    @Test
    public void testVolviendoDelTaller() {
        ambulancia.setOcupado(true);

        ambulancia.volviendoDelTaller();

        verify(estadoMock).SolicitudMantenimiento();
        verifyNoMoreInteractions(estadoMock);
        assertFalse("La ambulancia debe quedar libre", ambulancia.isOcupado());
    }

    @Test
    public void testRetornoAutomatico() {
        ambulancia.retornoAutomatico(new RetornoAutomatico(ambulancia));

        verify(estadoMock).RetornoClinica();
        verifyNoMoreInteractions(estadoMock);
    }

    // ========================================================
    // 3. PEDIDO CON LA AMBULANCIA OCUPADA Y LA SIMULACION FINALIZADA
    // ========================================================
    @Test
    public void testTrasladarConSimulacionFinalizada() {
        ambulancia.setOcupado(true);
        ambulancia.setSimulacionActiva(false);

        ambulancia.trasladarALaClinica(asociado);

        verify(estadoMock, never()).SolicitudDeTraslado();
    }

}
