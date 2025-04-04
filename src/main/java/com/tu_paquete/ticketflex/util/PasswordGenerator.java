package com.tu_paquete.ticketflex.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Contraseñas originales
        String passwordJuan = "admin123";
        String passwordAndres = "admin234";  
        String passwordNicol = "1506";


        // Generar hash de contraseñas
        String hashJuan = encoder.encode(passwordJuan);
        String hashAndres = encoder.encode(passwordAndres);
        String hashNicol = encoder.encode(passwordNicol);


        // Imprimir los hashes
        System.out.println("Hash para Juan: " + hashJuan);
        System.out.println("Hash para Andres: " + hashAndres);
        System.out.println("Hash para Nicol: " + hashNicol);


        // Simulación de verificación de contraseña (ejemplo)
        System.out.println("Verificación Juan: " + encoder.matches(passwordJuan, hashJuan));
        System.out.println("Verificación Andres: " + encoder.matches(passwordAndres, hashAndres));
        System.out.println("Verificación Nicol: " + encoder.matches(passwordNicol, hashNicol));

    }
}
