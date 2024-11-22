package com.example.demo;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;

    private final Map<String, String> codigosConfirmacion = new HashMap<>();

    public UsuarioService(UsuarioRepository usuarioRepository, JavaMailSender mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.mailSender = mailSender;
    }

    public void registrarUsuario(Usuario usuario) {
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        validarFormatoContraseña(usuario.getContraseña());

        String contraseñaCifrada = cifrarContraseñaSHA3(usuario.getContraseña());
        usuario.setContraseña(contraseñaCifrada);

        usuarioRepository.save(usuario);
    }

    private void validarFormatoContraseña(String contraseña) {
        if (!contraseña.matches(".{8,}")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!contraseña.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra mayúscula");
        }
        if (!contraseña.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una letra minúscula");
        }
        if (!contraseña.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un número");
        }
        if (!contraseña.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un carácter especial (!@#$%^&*)");
        }
    }

    private String cifrarContraseñaSHA3(String contraseña) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(contraseña.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar el hash de la contraseña", e);
        }
    }

    public String generarCodigoConfirmacion(String correo) {
        String codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        codigosConfirmacion.put(correo, codigo);
        return codigo;
    }

    public void enviarCorreoConfirmacion(String correo, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Confirmación de Cuenta");
        mensaje.setText("Gracias por registrarte. Tu código de confirmación es: " + codigo);

        mailSender.send(mensaje);
    }

    public boolean validarCodigoConfirmacion(String correo, String codigo) {
        if (codigosConfirmacion.containsKey(correo)) {
            String codigoGuardado = codigosConfirmacion.get(correo);
            if (codigoGuardado.equals(codigo)) {
                Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow(() ->
                        new IllegalArgumentException("El usuario no existe"));
                usuario.setVerificado(true);
                usuarioRepository.save(usuario);

                codigosConfirmacion.remove(correo);
                return true;
            }
        }
        return false;
    }

    public Usuario obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    public void confirmarCuenta(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setVerificado(true);
        usuarioRepository.save(usuario);
    }

    public boolean validarCredenciales(String correo, String contraseñaIngresada) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByCorreo(correo);
        if (optionalUsuario.isEmpty()) {
            return false;
        }

        Usuario usuario = optionalUsuario.get();
        String hashIngresado = cifrarContraseñaSHA3(contraseñaIngresada);
        return hashIngresado.equals(usuario.getContraseña());
    }

    public void enviarEnlaceRecuperacion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = UUID.randomUUID().toString();

        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(30);

        usuario.setTokenRecuperacion(token);
        usuario.setExpiracionToken(Timestamp.valueOf(expiracion));
        usuarioRepository.save(usuario);

        String enlace = "http://localhost:8080/restablecer?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Recuperación de Contraseña");
        mensaje.setText("Haz clic en el siguiente enlace para restablecer tu contraseña: " + enlace);

        mailSender.send(mensaje);
    }

    public Usuario verificarToken(String token) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

        if (usuario.getExpiracionToken().before(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("El token ha expirado");
        }

        return usuario;
    }

    public void restablecerContraseña(String token, String nuevaContraseña) {
        Usuario usuario = verificarToken(token); // Verifica el token
    
        // Valida y cifra la nueva contraseña
        validarFormatoContraseña(nuevaContraseña);
        String hash = cifrarContraseñaSHA3(nuevaContraseña);
    
        // Actualiza la contraseña y limpia los tokens
        usuario.setContraseña(hash);
        usuario.setTokenRecuperacion(null);
        usuario.setExpiracionToken(null);
        usuarioRepository.save(usuario);
    }
    


    public boolean validarContraseña(Usuario usuario, String contraseñaIngresada) {
        // Cifra la contraseña ingresada usando SHA-3
        String hashIngresado = cifrarContraseñaSHA3(contraseñaIngresada);
    
        // Compara el hash ingresado con el almacenado en la base de datos
        return hashIngresado.equals(usuario.getContraseña());
    }


    
    
}
