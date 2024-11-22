package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(Usuario usuario, RedirectAttributes redirectAttributes) {
        usuarioService.registrarUsuario(usuario);

        // Generar y enviar código de confirmación
        String codigo = usuarioService.generarCodigoConfirmacion(usuario.getCorreo());
        usuarioService.enviarCorreoConfirmacion(usuario.getCorreo(), codigo);

        // Redirigir a la página de confirmación
        redirectAttributes.addFlashAttribute("correo", usuario.getCorreo());
        return "redirect:/confirmar";
    }

    @GetMapping("/confirmar")
    public String mostrarFormularioConfirmacion(Model model) {
        if (!model.containsAttribute("correo")) {
            model.addAttribute("correo", ""); // Campo vacío si no hay correo en el modelo
        }
        return "confirmacion"; // Plantilla para confirmación
    }

    @PostMapping("/confirmar")
    public String confirmarCuenta(String correo, String codigo, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
        boolean esValido = usuarioService.validarCodigoConfirmacion(correo, codigo);

        if (esValido) {
            // Llama al método para confirmar la cuenta
            usuarioService.confirmarCuenta(correo);

            // Obtiene el usuario y lo guarda en la sesión
            Usuario usuario = usuarioService.obtenerUsuarioPorCorreo(correo);
            session.setAttribute("usuario", usuario);

            // Redirige a la página de bienvenida
            return "redirect:/bienvenido";
        } else {
            model.addAttribute("error", "Código de confirmación inválido o expirado.");
            model.addAttribute("correo", correo);
            return "confirmacion";
        }
    }

    @GetMapping("/bienvenido")
    public String mostrarPaginaBienvenida(HttpSession session, Model model) {
        // Recupera el usuario desde la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login"; // Redirige si no hay usuario en la sesión
        }
        model.addAttribute("usuario", usuario); // Pasa el usuario al modelo
        return "bienvenido";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "login";
    }

        @GetMapping("/datos-clasica")
    public String mostrarDatosClasica() {
        return "datos-clasica"; // Nombre del archivo HTML de la pantalla
    }

        @GetMapping("/datos-moderna")
    public String mostrarDatosModerna() {
        return "datos-moderna"; // Nombre del archivo HTML para "Criptografía Moderna"
    }

    @GetMapping("/datos-tendencias")
    public String mostrarDatosTendencias() {
        return "datos-tendencias"; // Nombre del archivo HTML para "Tendencias Criptográficas"
    }



    @GetMapping("/recuperar-contra")
    public String mostrarFormularioRecuperacion(Model model) {
        return "recuperar-contra"; // Nombre de la plantilla HTML para la recuperación
    }


    @PostMapping("/login")
    public String iniciarSesion(String correo, String contraseña, Model model, HttpSession session) {
        Usuario usuario = usuarioService.obtenerUsuarioPorCorreo(correo);
    
        if (usuario != null && usuarioService.validarContraseña(usuario, contraseña)) {
            if (!usuario.isVerificado()) {
                model.addAttribute("error", "Debes confirmar tu cuenta antes de iniciar sesión.");
                model.addAttribute("correo", usuario.getCorreo());
                return "confirmacion";
            }
    
            session.setAttribute("usuario", usuario); // Establecer sesión de usuario
            model.addAttribute("usuario", usuario); // Agregar usuario al modelo para Thymeleaf
            return "bienvenido";
        }
    
        model.addAttribute("error", "Credenciales inválidas.");
        return "login";
    }
    
    

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate(); // Invalida la sesión actual
        return "redirect:/login"; // Redirige a la página de inicio de sesión
    }


    @PostMapping("/recuperar")
public String procesarRecuperacion(String correo, RedirectAttributes redirectAttributes) {
    try {
        usuarioService.enviarEnlaceRecuperacion(correo);
        redirectAttributes.addFlashAttribute("mensaje", "Se ha enviado un enlace de recuperación a tu correo.");
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("mensaje", "El correo no está registrado en el sistema.");
    }
    return "redirect:/recuperar-contra"; // Redirige a la misma pantalla de recuperación con el mensaje
}


    @GetMapping("/restablecer")
    public String mostrarFormularioRestablecer(String token, Model model) {
        try {
            Usuario usuario = usuarioService.verificarToken(token);
            model.addAttribute("token", token);
            return "restablecer-contra";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensaje", "El enlace de recuperación no es válido o ha expirado.");
            return "recuperar-contra";
        }
    }


    @PostMapping("/restablecer")
    public String procesarRestablecimiento(String token, String nuevaContraseña, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.restablecerContraseña(token, nuevaContraseña);
            redirectAttributes.addFlashAttribute("mensaje", "Tu contraseña ha sido restablecida correctamente.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/recuperar-contra";
        }
    }
    














    
}
