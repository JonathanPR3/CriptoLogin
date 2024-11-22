package com.example.demo;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contraseña;

    @Column(nullable = false)
    private String nombreUsuario;

    @Column(nullable = false)
    private boolean verificado = false;

    @Column
    private String tokenRecuperacion; // Campo para el token de recuperación

    @Column
    private Timestamp expiracionToken; // Campo para la expiración del token de recuperación

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getTokenRecuperacion() {
        return tokenRecuperacion;
    }

    public void setTokenRecuperacion(String tokenRecuperacion) {
        this.tokenRecuperacion = tokenRecuperacion;
    }

    public Timestamp getExpiracionToken() {
        return expiracionToken;
    }

    public void setExpiracionToken(Timestamp expiracionToken) {
        this.expiracionToken = expiracionToken;
    }
}
