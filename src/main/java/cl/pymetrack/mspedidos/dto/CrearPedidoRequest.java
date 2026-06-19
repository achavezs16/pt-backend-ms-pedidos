package cl.pymetrack.mspedidos.dto;

import java.math.BigDecimal;
import java.util.List;

public class CrearPedidoRequest {

    private Long idPyme;
    private String numeroOrdenPyme;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private String direccionEntregaChile;
    private String comunaEntregaChile;
    private String regionEntregaChile;
    private BigDecimal subtotal;
    private BigDecimal costoDespachoChile;
    private BigDecimal totalPedido;
    private String etiquetaDespachoPyme;
    private String notasPedido;
    private List<CrearPedidoItemRequest> items;

    public Long getIdPyme() { return idPyme; }
    public void setIdPyme(Long idPyme) { this.idPyme = idPyme; }

    public String getNumeroOrdenPyme() { return numeroOrdenPyme; }
    public void setNumeroOrdenPyme(String numeroOrdenPyme) { this.numeroOrdenPyme = numeroOrdenPyme; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public String getDireccionEntregaChile() { return direccionEntregaChile; }
    public void setDireccionEntregaChile(String direccionEntregaChile) { this.direccionEntregaChile = direccionEntregaChile; }

    public String getComunaEntregaChile() { return comunaEntregaChile; }
    public void setComunaEntregaChile(String comunaEntregaChile) { this.comunaEntregaChile = comunaEntregaChile; }

    public String getRegionEntregaChile() { return regionEntregaChile; }
    public void setRegionEntregaChile(String regionEntregaChile) { this.regionEntregaChile = regionEntregaChile; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getCostoDespachoChile() { return costoDespachoChile; }
    public void setCostoDespachoChile(BigDecimal costoDespachoChile) { this.costoDespachoChile = costoDespachoChile; }

    public BigDecimal getTotalPedido() { return totalPedido; }
    public void setTotalPedido(BigDecimal totalPedido) { this.totalPedido = totalPedido; }

    public String getEtiquetaDespachoPyme() { return etiquetaDespachoPyme; }
    public void setEtiquetaDespachoPyme(String etiquetaDespachoPyme) { this.etiquetaDespachoPyme = etiquetaDespachoPyme; }

    public String getNotasPedido() { return notasPedido; }
    public void setNotasPedido(String notasPedido) { this.notasPedido = notasPedido; }

    public List<CrearPedidoItemRequest> getItems() { return items; }
    public void setItems(List<CrearPedidoItemRequest> items) { this.items = items; }
}