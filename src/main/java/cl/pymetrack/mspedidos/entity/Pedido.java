package cl.pymetrack.mspedidos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import cl.pymetrack.mspedidos.model.EstadoPedido;

@Entity
@Table(name = "pedido")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_pyme", nullable = false)
    private Long idPyme;
    
    @Column(name = "numero_orden_pyme", nullable = false, unique = true, length = 50)
    private String numeroOrdenPyme;
    
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;
    
    @Column(name = "email_cliente", nullable = false, length = 100)
    private String emailCliente;
    
    @Column(name = "telefono_cliente", length = 20)
    private String telefonoCliente;
    
    @Column(name = "direccion_entrega_chile", nullable = false, columnDefinition = "TEXT")
    private String direccionEntregaChile;
    
    @Column(name = "comuna_entrega_chile", nullable = false, length = 50)
    private String comunaEntregaChile;
    
    @Column(name = "region_entrega_chile", nullable = false, length = 50)
    private String regionEntregaChile;
    
    @Column(name = "estado_pedido_pyme", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoPedidoPyme = EstadoPedido.DISPONIBLE;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "costo_despacho_chile", precision = 10, scale = 2)
    private BigDecimal costoDespachoChile = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPedido;
    
    @Column(name = "etiqueta_despacho_pyme", unique = true, length = 100)
    private String etiquetaDespachoPyme;
    
    @Column(columnDefinition = "TEXT")
    private String notasPedido;
    
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
    
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();
    
    
    // Constructors
    public Pedido() {}
    
    public Pedido(Long idPyme, String numeroOrdenPyme, String nombreCliente, String emailCliente, 
                 String direccionEntregaChile, String comunaEntregaChile, String regionEntregaChile, 
                 BigDecimal subtotal, BigDecimal totalPedido) {
        this.idPyme = idPyme;
        this.numeroOrdenPyme = numeroOrdenPyme;
        this.nombreCliente = nombreCliente;
        this.emailCliente = emailCliente;
        this.direccionEntregaChile = direccionEntregaChile;
        this.comunaEntregaChile = comunaEntregaChile;
        this.regionEntregaChile = regionEntregaChile;
        this.subtotal = subtotal;
        this.totalPedido = totalPedido;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getIdPyme() {
        return idPyme;
    }
    
    public void setIdPyme(Long idPyme) {
        this.idPyme = idPyme;
    }
    
    public String getNumeroOrdenPyme() {
        return numeroOrdenPyme;
    }
    
    public void setNumeroOrdenPyme(String numeroOrdenPyme) {
        this.numeroOrdenPyme = numeroOrdenPyme;
    }
    
    public String getNombreCliente() {
        return nombreCliente;
    }
    
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    
    public String getEmailCliente() {
        return emailCliente;
    }
    
    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }
    
    public String getTelefonoCliente() {
        return telefonoCliente;
    }
    
    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }
    
    public String getDireccionEntregaChile() {
        return direccionEntregaChile;
    }
    
    public void setDireccionEntregaChile(String direccionEntregaChile) {
        this.direccionEntregaChile = direccionEntregaChile;
    }
    
    public String getComunaEntregaChile() {
        return comunaEntregaChile;
    }
    
    public void setComunaEntregaChile(String comunaEntregaChile) {
        this.comunaEntregaChile = comunaEntregaChile;
    }
    
    public String getRegionEntregaChile() {
        return regionEntregaChile;
    }
    
    public void setRegionEntregaChile(String regionEntregaChile) {
        this.regionEntregaChile = regionEntregaChile;
    }
    
    public EstadoPedido getEstadoPedidoPyme() {
        return estadoPedidoPyme;
    }
    
    public void setEstadoPedidoPyme(EstadoPedido estadoPedidoPyme) {
        this.estadoPedidoPyme = estadoPedidoPyme;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getCostoDespachoChile() {
        return costoDespachoChile;
    }
    
    public void setCostoDespachoChile(BigDecimal costoDespachoChile) {
        this.costoDespachoChile = costoDespachoChile;
    }
    
    public BigDecimal getTotalPedido() {
        return totalPedido;
    }
    
    public void setTotalPedido(BigDecimal totalPedido) {
        this.totalPedido = totalPedido;
    }
    
    public String getEtiquetaDespachoPyme() {
        return etiquetaDespachoPyme;
    }
    
    public void setEtiquetaDespachoPyme(String etiquetaDespachoPyme) {
        this.etiquetaDespachoPyme = etiquetaDespachoPyme;
    }
    
    public String getNotasPedido() {
        return notasPedido;
    }
    
    public void setNotasPedido(String notasPedido) {
        this.notasPedido = notasPedido;
    }
    
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
    
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
    
    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }
    
    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public List<PedidoItem> getItems() {
        return items;
    }

    public void setItems(List<PedidoItem> items) {
        this.items = items;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
