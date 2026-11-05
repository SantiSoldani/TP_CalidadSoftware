package test.integracion;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.AtendiendoEnDomicilioState;
import modelo.modeloDominio.ambulancia.DisponibleState;
import modelo.modeloDominio.ambulancia.EnTallerState;
import modelo.modeloDominio.ambulancia.RegresandoClinicaSinPacienteState;
import modelo.modeloDominio.ambulancia.RegresandoDelTallerState;
import modelo.modeloDominio.ambulancia.State;
import modelo.modeloDominio.ambulancia.TrasladandoPacienteClinicaState;

public class TestEstadosAmbulancia {

    private Ambulancia ambulanciaMock;

    @Before
    public void setUp() {
        // Escenario: cada estado se prueba aislado, con un mock en lugar de la ambulancia
        ambulanciaMock = mock(Ambulancia.class);
    }

    private void verificarCambio(Class<? extends State> estadoEsperado) {
        verify(ambulanciaMock).setState(isA(estadoEsperado));
        clearInvocations(ambulanciaMock);
    }

    private void verificarSinCambio() {
        verify(ambulanciaMock, never()).setState(any());
        clearInvocations(ambulanciaMock);
    }

    // ========================================================
    // MATRIZ DE TRANSICIONES: CADA ESTADO ANTE LOS 4 EVENTOS
    // ========================================================
    @Test
    public void testDisponible() {
        State estado = new DisponibleState(ambulanciaMock);

        estado.SolicitudDeTraslado();
        verificarCambio(TrasladandoPacienteClinicaState.class);
        estado.SolicitudDeAtencionDomicilio();
        verificarCambio(AtendiendoEnDomicilioState.class);
        estado.SolicitudMantenimiento();
        verificarCambio(EnTallerState.class);
        estado.RetornoClinica();
        verificarSinCambio();
    }

    @Test
    public void testTrasladandoPacienteClinica() {
        State estado = new TrasladandoPacienteClinicaState(ambulanciaMock);

        estado.SolicitudDeTraslado();
        verificarSinCambio();
        estado.SolicitudDeAtencionDomicilio();
        verificarSinCambio();
        estado.SolicitudMantenimiento();
        verificarSinCambio();
        estado.RetornoClinica();
        verificarCambio(DisponibleState.class);
    }

    @Test
    public void testAtendiendoEnDomicilio() {
        State estado = new AtendiendoEnDomicilioState(ambulanciaMock);

        estado.SolicitudDeTraslado();
        verificarSinCambio();
        estado.SolicitudDeAtencionDomicilio();
        verificarSinCambio();
        estado.SolicitudMantenimiento();
        verificarSinCambio();
        estado.RetornoClinica();
        verificarCambio(RegresandoClinicaSinPacienteState.class);
    }

    @Test
    public void testEnTaller() {
        State estado = new EnTallerState(ambulanciaMock);

        estado.SolicitudDeTraslado();
        verificarSinCambio();
        estado.SolicitudDeAtencionDomicilio();
        verificarSinCambio();
        estado.RetornoClinica();
        verificarSinCambio();
        estado.SolicitudMantenimiento();
        verificarCambio(RegresandoDelTallerState.class);
    }

    @Test
    public void testRegresandoClinicaSinPaciente() {
        State estado = new RegresandoClinicaSinPacienteState(ambulanciaMock);

        estado.SolicitudDeTraslado();
        verificarCambio(TrasladandoPacienteClinicaState.class);
        estado.SolicitudDeAtencionDomicilio();
        verificarCambio(AtendiendoEnDomicilioState.class);
        estado.SolicitudMantenimiento();
        verificarSinCambio();
        estado.RetornoClinica();
        verificarCambio(DisponibleState.class);
    }

    @Test
    public void testRegresandoDelTaller() {
        State estado = new RegresandoDelTallerState(ambulanciaMock);

        estado.SolicitudDeTraslado();
        verificarSinCambio();
        estado.SolicitudDeAtencionDomicilio();
        verificarSinCambio();
        estado.SolicitudMantenimiento();
        verificarSinCambio();
        estado.RetornoClinica();
        verificarCambio(DisponibleState.class);
    }

}
