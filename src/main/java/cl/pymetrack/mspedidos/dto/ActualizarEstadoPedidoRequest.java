package cl.pymetrack.mspedidos.dto;

public class ActualizarEstadoPedidoRequest {

    private String estado;
    private Long repartidorId;
    private String observacion;

    public ActualizarEstadoPedidoRequest() {}

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}