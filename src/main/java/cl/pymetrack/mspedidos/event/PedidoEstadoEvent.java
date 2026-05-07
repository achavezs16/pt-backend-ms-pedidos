package cl.pymetrack.mspedidos.event;

import java.time.LocalDateTime;

public class PedidoEstadoEvent {

    private Long pedidoId;
    private Long idPyme;
    private String estadoAnterior;
    private String estadoNuevo;
    private Long repartidorId;
    private String observacion;
    private LocalDateTime fechaEvento;

    public PedidoEstadoEvent() {}

    public PedidoEstadoEvent(
            Long pedidoId,
            Long idPyme,
            String estadoAnterior,
            String estadoNuevo,
            Long repartidorId,
            String observacion
    ) {
        this.pedidoId = pedidoId;
        this.idPyme = idPyme;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.repartidorId = repartidorId;
        this.observacion = observacion;
        this.fechaEvento = LocalDateTime.now();
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getIdPyme() {
        return idPyme;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public String getObservacion() {
        return observacion;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public void setIdPyme(Long idPyme) {
        this.idPyme = idPyme;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}
