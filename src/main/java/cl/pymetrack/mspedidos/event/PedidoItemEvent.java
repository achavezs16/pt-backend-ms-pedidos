package cl.pymetrack.mspedidos.event;

public class PedidoItemEvent {

    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;

    public PedidoItemEvent() {}

    public PedidoItemEvent(Long productoId, String nombreProducto, Integer cantidad) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}