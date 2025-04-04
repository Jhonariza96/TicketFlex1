package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tu_paquete.ticketflex.Model.Rol;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.RolRepository;
import com.tu_paquete.ticketflex.Repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("El usuario ya existe con ese correo electrónico");
        });

        // Asignar rol por defecto (Usuario)
        Rol rolUsuario = rolRepository.findByNombreRol("Usuario")
            .orElseThrow(() -> new RuntimeException("Rol Usuario no encontrado en la base de datos"));
        usuario.setRol(rolUsuario);

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario iniciarSesion(String email, String password) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return usuario;
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public boolean esAdministrador(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
            .map(usuario -> {
                if (usuario.getRol() == null) return false;
                return "Administrador".equalsIgnoreCase(usuario.getRol().getNombreRol());
            })
            .orElse(false);
    }
}
