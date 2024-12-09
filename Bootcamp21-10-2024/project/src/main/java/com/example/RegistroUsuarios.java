package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistroUsuarios extends JFrame {

    private Usuario[] usuarios = new Usuario[100]; // Array fijo para usuarios
    private Supervisor[] supervisores = new Supervisor[100]; // Array fijo para supervisores
    private SolicitudPermiso[] permisosPendientes = new SolicitudPermiso[100]; // Array fijo para permisos
    private int usuarioCount = 0; // Contador de usuarios
    private int supervisorCount = 0; // Contador de supervisores
    private int permisoCount = 0; // Contador de permisos
    private String rolActual; // Para almacenar el rol del usuario actual

    public RegistroUsuarios() {
        setTitle("Sistema de Gestión de Permisos");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar ventana

        mostrarLogin(); // Mostrar pantalla de inicio de sesión
    }

    private void mostrarLogin() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Iniciar Sesión"));

        JTextField idField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Iniciar Sesión");
        JComboBox<String> rolComboBox = new JComboBox<>(new String[] { "Usuario", "Supervisor" });

        loginPanel.add(new JLabel("ID:"));
        loginPanel.add(idField);
        loginPanel.add(new JLabel("Contraseña:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel("Rol:"));
        loginPanel.add(rolComboBox);
        loginPanel.add(loginButton);

        loginButton.addActionListener(e -> {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());
            String rol = (String) rolComboBox.getSelectedItem();

            // Verificar las credenciales
            if (rol.equals("Usuario")) {
                if (validarUsuario(id, password)) {
                    rolActual = "Usuario";
                    mostrarInterfazUsuario();
                } else {
                    JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos.");
                }
            } else if (rol.equals("Supervisor")) {
                if (validarSupervisor(id, password)) {
                    rolActual = "Supervisor";
                    mostrarInterfazSupervisor();
                } else {
                    JOptionPane.showMessageDialog(null, "Supervisor o contraseña incorrectos.");
                }
            }
        });

        add(loginPanel);
        setVisible(true);
    }

    private void mostrarInterfazUsuario() {
        getContentPane().removeAll();
        revalidate();
        repaint();

        // Panel de Registro
        JPanel registroPanel = new JPanel();
        registroPanel.setLayout(new GridLayout(3, 2, 10, 10)); // Espaciado entre componentes
        registroPanel.setBorder(BorderFactory.createTitledBorder("Registro de Usuario"));

        JLabel nombreLabel = new JLabel("Nombre:");
        JTextField nombreField = new JTextField();
        JButton registrarButton = new JButton("Registrar Usuario");
        JButton registrarSupervisorButton = new JButton("Registrar Supervisor");
        JButton mostrarRegistroButton = new JButton("Mostrar Registro");

        registroPanel.add(nombreLabel);
        registroPanel.add(nombreField);
        registroPanel.add(new JLabel("")); // Espacio vacío
        registroPanel.add(registrarButton);
        registroPanel.add(new JLabel("")); // Espacio vacío
        registroPanel.add(registrarSupervisorButton);
        registroPanel.add(mostrarRegistroButton); // Agregar el botón para mostrar registro

        // Panel de Permisos
        JPanel permisosPanel = new JPanel();
        permisosPanel.setLayout(new GridLayout(2, 1, 10, 10)); // Espaciado entre componentes
        permisosPanel.setBorder(BorderFactory.createTitledBorder("Gestión de Permisos"));

        JButton solicitarPermisoButton = new JButton("Solicitar Permiso de Trabajo");
        JButton verPermisosButton = new JButton("Ver Permisos Pendientes");

        permisosPanel.add(solicitarPermisoButton);
        permisosPanel.add(verPermisosButton);

        // Agregar paneles a la ventana principal
        add(registroPanel, BorderLayout.NORTH);
        add(permisosPanel, BorderLayout.CENTER);

        // Acciones de los botones
        registrarButton.addActionListener(e -> {
            String nombre = nombreField.getText();
            if (!nombre.isEmpty() && usuarioCount < usuarios.length) {
                String id = JOptionPane.showInputDialog("Ingrese la ID del usuario:");
                String password = JOptionPane.showInputDialog("Ingrese la contraseña:");
                usuarios[usuarioCount++] = new Usuario(id, nombre, password);
                JOptionPane.showMessageDialog(null, "Usuario registrado: " + nombre);
                nombreField.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor ingrese un nombre o el registro está lleno.");
            }
        });

        registrarSupervisorButton.addActionListener(e -> {
            String nombre = nombreField.getText();
            if (!nombre.isEmpty() && supervisorCount < supervisores.length) {
                String id = JOptionPane.showInputDialog("Ingrese la ID del supervisor:");
                String password = JOptionPane.showInputDialog("Ingrese la contraseña:");
                supervisores[supervisorCount++] = new Supervisor(id, nombre, password);
                JOptionPane.showMessageDialog(null, "Supervisor registrado: " + nombre);
                nombreField.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor ingrese un nombre o el registro está lleno.");
            }
        });

        mostrarRegistroButton.addActionListener(e -> {
            StringBuilder registro = new StringBuilder("Registro de Usuarios:\n");
            for (int i = 0; i < usuarioCount; i++) {
                registro.append(usuarios[i].getNombre()).append(" (ID: ").append(usuarios[i].getId()).append(")\n");
            }
            registro.append("\nRegistro de Supervisores:\n");
            for (int i = 0; i < supervisorCount; i++) {
                registro.append(supervisores[i].getNombre()).append(" (ID: ").append(supervisores[i].getId())
                        .append(")\n");
            }
            JOptionPane.showMessageDialog(null, registro.toString());
        });

        solicitarPermisoButton.addActionListener(e -> {
            // Ventana para crear la solicitud de trabajo
            JFrame solicitudFrame = new JFrame("Solicitud de Permiso de Trabajo");
            solicitudFrame.setSize(400, 300);
            solicitudFrame.setLayout(new GridLayout(6, 2, 10, 10)); // Espaciado entre componentes
            solicitudFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            solicitudFrame.setLocationRelativeTo(null); // Centrar la ventana

            JTextField idField = new JTextField();
            JTextField rolField = new JTextField("Usuario", 20);
            rolField.setEditable(false); // Deshabilitar edición de rol
            JTextField fechaInicioField = new JTextField();
            JTextField fechaFinField = new JTextField();
            JTextArea asuntoField = new JTextArea();
            asuntoField.setLineWrap(true);
            asuntoField.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(asuntoField); // Para que el campo de asunto sea desplazable

            solicitudFrame.add(new JLabel("ID del Usuario:"));
            solicitudFrame.add(idField);
            solicitudFrame.add(new JLabel("Rol del Usuario:"));
            solicitudFrame.add(rolField);
            solicitudFrame.add(new JLabel("Fecha de Inicio (dd/mm/yyyy):"));
            solicitudFrame.add(fechaInicioField);
            solicitudFrame.add(new JLabel("Fecha Final (dd/mm/yyyy):"));
            solicitudFrame.add(fechaFinField);
            solicitudFrame.add(new JLabel("Asunto:"));
            solicitudFrame.add(scrollPane);

            JButton enviarButton = new JButton("Enviar Solicitud");
            solicitudFrame.add(enviarButton);

            // Acción del botón enviar
            enviarButton.addActionListener(evt -> {
                String id = idField.getText();
                String rol = rolField.getText();
                String fechaInicio = fechaInicioField.getText();
                String fechaFin = fechaFinField.getText();
                String asunto = asuntoField.getText();

                // Validar que el usuario existe
                boolean usuarioValido = validarExistenciaUsuario(id);
                if (usuarioValido) {
                    if (permisoCount < permisosPendientes.length) {
                        permisosPendientes[permisoCount++] = new SolicitudPermiso(id, rol, fechaInicio, fechaFin,
                                asunto);
                        JOptionPane.showMessageDialog(null, "Solicitud de permiso enviada.");
                        solicitudFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "No se pueden enviar más solicitudes.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Usuario no registrado.");
                }
            });

            solicitudFrame.setVisible(true);
        });

        verPermisosButton.addActionListener(e -> {
            if (permisoCount == 0) {
                JOptionPane.showMessageDialog(null, "No hay permisos pendientes.");
            } else {
                StringBuilder permisos = new StringBuilder("Permisos Pendientes:\n");
                for (int i = 0; i < permisoCount; i++) {
                    permisos.append(permisosPendientes[i]).append("\n");
                }
                JOptionPane.showMessageDialog(null, permisos.toString());
            }
        });

        setVisible(true);
    }

    private void mostrarInterfazSupervisor() {
        getContentPane().removeAll();
        revalidate();
        repaint();

        // Panel de Permisos
        JPanel permisosPanel = new JPanel();
        permisosPanel.setLayout(new GridLayout(3, 1, 10, 10)); // Espaciado entre componentes
        permisosPanel.setBorder(BorderFactory.createTitledBorder("Gestión de Permisos"));

        JButton verPermisosButton = new JButton("Ver Permisos Pendientes");
        JButton aceptarButton = new JButton("Aceptar Permiso");
        JButton denegarButton = new JButton("Denegar Permiso");

        permisosPanel.add(verPermisosButton);
        permisosPanel.add(aceptarButton);
        permisosPanel.add(denegarButton);

        // Agregar panel a la ventana principal
        add(permisosPanel, BorderLayout.CENTER);

        verPermisosButton.addActionListener(e -> {
            if (permisoCount == 0) {
                JOptionPane.showMessageDialog(null, "No hay permisos pendientes.");
            } else {
                StringBuilder permisos = new StringBuilder("Permisos Pendientes:\n");
                for (int i = 0; i < permisoCount; i++) {
                    permisos.append(permisosPendientes[i]).append("\n");
                }
                JOptionPane.showMessageDialog(null, permisos.toString());
            }
        });

        aceptarButton.addActionListener(e -> {
            String id = JOptionPane.showInputDialog("Ingrese la ID para aceptar permiso:");
            SolicitudPermiso permiso = buscarPermisoPorID(id);
            if (permiso != null) {
                eliminarPermiso(permiso);
                JOptionPane.showMessageDialog(null, "Permiso aceptado para: " + permiso.getIdUsuario());
            } else {
                JOptionPane.showMessageDialog(null, "No hay permiso pendiente para: " + id);
            }
        });

        denegarButton.addActionListener(e -> {
            String id = JOptionPane.showInputDialog("Ingrese la ID para denegar permiso:");
            SolicitudPermiso permiso = buscarPermisoPorID(id);
            if (permiso != null) {
                eliminarPermiso(permiso);
                JOptionPane.showMessageDialog(null, "Permiso denegado para: " + permiso.getIdUsuario());
            } else {
                JOptionPane.showMessageDialog(null, "No hay permiso pendiente para: " + id);
            }
        });

        setVisible(true);
    }

    private boolean validarUsuario(String id, String password) {
        for (Usuario usuario : usuarios) {
            if (usuario != null && usuario.getId().equals(id) && usuario.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private boolean validarSupervisor(String id, String password) {
        for (Supervisor supervisor : supervisores) {
            if (supervisor != null && supervisor.getId().equals(id) && supervisor.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private boolean validarExistenciaUsuario(String id) {
        for (Usuario usuario : usuarios) {
            if (usuario != null && usuario.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private SolicitudPermiso buscarPermisoPorID(String id) {
        for (SolicitudPermiso permiso : permisosPendientes) {
            if (permiso != null && permiso.getIdUsuario().equals(id)) {
                return permiso;
            }
        }
        return null;
    }

    private void eliminarPermiso(SolicitudPermiso permiso) {
        for (int i = 0; i < permisoCount; i++) {
            if (permisosPendientes[i] != null && permisosPendientes[i].equals(permiso)) {
                permisosPendientes[i] = null;
                // Reorganizar el array
                for (int j = i; j < permisoCount - 1; j++) {
                    permisosPendientes[j] = permisosPendientes[j + 1];
                }
                permisosPendientes[--permisoCount] = null; // Disminuir contador
                return;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistroUsuarios::new);
    }
}

class Usuario {
    private String id;
    private String nombre;
    private String password;

    public Usuario(String id, String nombre, String password) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPassword() {
        return password;
    }
}

class Supervisor {
    private String id;
    private String nombre;
    private String password;

    public Supervisor(String id, String nombre, String password) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPassword() {
        return password;
    }
}

class SolicitudPermiso {
    private String idUsuario;
    private String rolUsuario;
    private String fechaInicio;
    private String fechaFin;
    private String asunto;

    public SolicitudPermiso(String idUsuario, String rolUsuario, String fechaInicio, String fechaFin, String asunto) {
        this.idUsuario = idUsuario;
        this.rolUsuario = rolUsuario;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.asunto = asunto;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    @Override
    public String toString() {
        return "ID: " + idUsuario + ", Rol: " + rolUsuario + ", Desde: " + fechaInicio + ", Hasta: " + fechaFin
                + ", Asunto: " + asunto;
    }
}
