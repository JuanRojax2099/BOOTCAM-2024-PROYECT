package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Clase que representa una solicitud de trabajo
class Solicitud implements Serializable {
    private static final long serialVersionUID = 1L; // Versión de serialización
    private String tipoTrabajo;
    private int duracion;
    private String nivelRiesgo;
    private String estado;
    private long tiempoSolicitud;

    public Solicitud(String tipoTrabajo, int duracion, String nivelRiesgo) {
        this.tipoTrabajo = tipoTrabajo;
        this.duracion = duracion;
        this.nivelRiesgo = nivelRiesgo;
        this.estado = "pendiente";
        this.tiempoSolicitud = System.currentTimeMillis(); // Timestamp de la solicitud
    }

    public String getTipoTrabajo() {
        return tipoTrabajo;
    }

    public int getDuracion() {
        return duracion;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getTiempoSolicitud() {
        return tiempoSolicitud;
    }
}

// Clase que representa el sistema de gestión de permisos
class SistemaPermisos {
    private List<Solicitud> solicitudes;
    private List<Long> tiemposDeAprobacion;
    private static final String FILE_PATH = "solicitudes.dat"; // Archivo para almacenar solicitudes

    public SistemaPermisos() {
        this.solicitudes = new ArrayList<>();
        this.tiemposDeAprobacion = new ArrayList<>();
        cargarSolicitudes(); // Cargar las solicitudes al iniciar el sistema
    }

    public void guardarSolicitudes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(solicitudes); // Escribimos las solicitudes en el archivo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cargarSolicitudes() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
                List<Solicitud> solicitudesCargadas = (List<Solicitud>) ois.readObject();
                for (Solicitud solicitud : solicitudesCargadas) {
                    if (!existeSolicitud(solicitud)) { // Verificar si ya existe
                        solicitudes.add(solicitud);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean existeSolicitud(Solicitud solicitud) {
        return solicitudes.stream().anyMatch(s -> s.getTipoTrabajo().equals(solicitud.getTipoTrabajo()) &&
                s.getDuracion() == solicitud.getDuracion() &&
                s.getNivelRiesgo().equals(solicitud.getNivelRiesgo()) &&
                s.getEstado().equals(solicitud.getEstado()));
    }

    public void agregarSolicitud(Solicitud solicitud) {
        if (!existeSolicitud(solicitud)) { // Solo agregar si no existe
            solicitudes.add(solicitud);
            guardarSolicitudes(); // Guardar cambios al agregar
        }
    }

    public void procesarSolicitud(Solicitud solicitud, boolean aprobar) {
        long tiempoAprobacion = System.currentTimeMillis() - solicitud.getTiempoSolicitud();
        solicitud.setEstado(aprobar ? "aprobada" : "rechazada");
        if (aprobar) {
            tiemposDeAprobacion.add(tiempoAprobacion);
        }
        guardarSolicitudes(); // Guardar cambios al procesar
    }

    public List<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    public String generarReporte() {
        int totalSolicitudes = solicitudes.size();
        int aprobadas = (int) solicitudes.stream().filter(s -> s.getEstado().equals("aprobada")).count();
        int rechazadas = (int) solicitudes.stream().filter(s -> s.getEstado().equals("rechazada")).count();
        double promedioAprobacion = tiemposDeAprobacion.stream().mapToLong(Long::longValue).average().orElse(0);

        return "===== Reporte de Permisos =====\n" +
                "Total de solicitudes: " + totalSolicitudes + "\n" +
                "Solicitudes aprobadas: " + aprobadas + "\n" +
                "Solicitudes rechazadas: " + rechazadas + "\n" +
                "Tiempo promedio de aprobación: " + promedioAprobacion + " ms\n";
    }
}

// Clase para manejar la interfaz gráfica de gestión de permisos
class GestionPermisosGUI extends JFrame {
    private SistemaPermisos sistemaPermisos;
    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;
    private String nombreUsuario;

    public GestionPermisosGUI(String nombreUsuario, boolean esSupervisor, List<Solicitud> solicitudesExistentes) {
        this.nombreUsuario = nombreUsuario;
        sistemaPermisos = new SistemaPermisos();
        setTitle("Gestión de Permisos de Trabajo");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar las solicitudes existentes
        for (Solicitud solicitud : solicitudesExistentes) {
            sistemaPermisos.agregarSolicitud(solicitud);
        }

        // Crear los componentes
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2));

        // Campos para crear solicitud
        JLabel labelTipoTrabajo = new JLabel("Tipo de trabajo:");
        JTextField campoTipoTrabajo = new JTextField();
        JLabel labelDuracion = new JLabel("Duración (horas):");
        JTextField campoDuracion = new JTextField();
        JLabel labelRiesgo = new JLabel("Nivel de riesgo:");
        JTextField campoRiesgo = new JTextField();
        JButton botonCrearSolicitud = new JButton("Crear Solicitud");

        // Agregar componentes al formulario
        panelFormulario.add(labelTipoTrabajo);
        panelFormulario.add(campoTipoTrabajo);
        panelFormulario.add(labelDuracion);
        panelFormulario.add(campoDuracion);
        panelFormulario.add(labelRiesgo);
        panelFormulario.add(campoRiesgo);
        panelFormulario.add(new JLabel());
        panelFormulario.add(botonCrearSolicitud);

        // Tabla para mostrar solicitudes
        String[] columnas = { "Tipo Trabajo", "Duración", "Riesgo", "Estado" };
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaSolicitudes = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaSolicitudes);

        // Botones de acción (solo para supervisores)
        JButton botonAprobar = new JButton("Aprobar");
        JButton botonRechazar = new JButton("Rechazar");
        JButton botonEliminar = new JButton("Eliminar"); // Nuevo botón de eliminar
        JButton botonGenerarReporte = new JButton("Generar Reporte");
        JButton botonRegresar = new JButton("Regresar");

        JPanel panelBotones = new JPanel();
        if (esSupervisor) {
            panelBotones.add(botonAprobar);
            panelBotones.add(botonRechazar);
            panelBotones.add(botonEliminar); // Agregar botón de eliminar
            panelBotones.add(botonGenerarReporte);
        }
        panelBotones.add(botonRegresar); // Agregar botón de regresar

        // Agregar componentes al panel principal
        panelPrincipal.add(panelFormulario, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Agregar el panel principal al JFrame
        add(panelPrincipal);

        // Cargar solicitudes en la tabla
        cargarSolicitudesEnTabla();

        // ActionListener para crear una nueva solicitud
        botonCrearSolicitud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipoTrabajo = campoTipoTrabajo.getText();
                String duracionTexto = campoDuracion.getText();
                String riesgo = campoRiesgo.getText();

                try {
                    int duracion = Integer.parseInt(duracionTexto); // Validar la duración como un entero

                    // Crear y agregar la solicitud al sistema
                    Solicitud solicitud = new Solicitud(tipoTrabajo, duracion, riesgo);
                    sistemaPermisos.agregarSolicitud(solicitud);

                    // Agregar la solicitud a la tabla
                    modeloTabla.addRow(new Object[] { tipoTrabajo, duracion, riesgo, solicitud.getEstado() });

                    // Limpiar los campos
                    campoTipoTrabajo.setText("");
                    campoDuracion.setText("");
                    campoRiesgo.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Por favor, ingrese una duración válida.");
                }
            }
        });

        // ActionListener para aprobar una solicitud
        botonAprobar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaSolicitudes.getSelectedRow();
                if (filaSeleccionada != -1) {
                    Solicitud solicitud = sistemaPermisos.getSolicitudes().get(filaSeleccionada);
                    sistemaPermisos.procesarSolicitud(solicitud, true);
                    modeloTabla.setValueAt(solicitud.getEstado(), filaSeleccionada, 3);
                } else {
                    JOptionPane.showMessageDialog(null, "Seleccione una solicitud para aprobar.");
                }
            }
        });

        // ActionListener para rechazar una solicitud
        botonRechazar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaSolicitudes.getSelectedRow();
                if (filaSeleccionada != -1) {
                    Solicitud solicitud = sistemaPermisos.getSolicitudes().get(filaSeleccionada);
                    sistemaPermisos.procesarSolicitud(solicitud, false);
                    modeloTabla.setValueAt(solicitud.getEstado(), filaSeleccionada, 3);
                } else {
                    JOptionPane.showMessageDialog(null, "Seleccione una solicitud para rechazar.");
                }
            }
        });

        // ActionListener para eliminar una solicitud
        botonEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaSolicitudes.getSelectedRow(); // Obtener fila seleccionada
                if (filaSeleccionada != -1) {
                    Solicitud solicitud = sistemaPermisos.getSolicitudes().get(filaSeleccionada);

                    // Confirmar eliminación
                    int confirm = JOptionPane.showConfirmDialog(null,
                            "¿Está seguro de que desea eliminar la solicitud?", "Confirmación",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Eliminar solicitud del sistema y la tabla
                        sistemaPermisos.getSolicitudes().remove(solicitud);
                        modeloTabla.removeRow(filaSeleccionada);

                        // Guardar cambios en el archivo
                        sistemaPermisos.guardarSolicitudes();

                        JOptionPane.showMessageDialog(null, "Solicitud eliminada correctamente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Seleccione una solicitud para eliminar.");
                }
            }
        });

        // ActionListener para generar el reporte
        botonGenerarReporte.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String reporte = sistemaPermisos.generarReporte();
                JOptionPane.showMessageDialog(null, reporte);
            }
        });

        // ActionListener para regresar
        botonRegresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sistemaPermisos.guardarSolicitudes(); // Guardar solicitudes antes de regresar
                dispose(); // Cerrar la ventana actual
                new LoginFrame(sistemaPermisos.getSolicitudes()).setVisible(true); // Regresar a la pantalla de inicio
            }
        });
    }

    private void cargarSolicitudesEnTabla() {
        for (Solicitud solicitud : sistemaPermisos.getSolicitudes()) {
            modeloTabla.addRow(new Object[] { solicitud.getTipoTrabajo(), solicitud.getDuracion(),
                    solicitud.getNivelRiesgo(), solicitud.getEstado() });
        }
    }
}

// Clase de inicio de sesión
class LoginFrame extends JFrame {
    private List<Solicitud> solicitudes; // Almacenar las solicitudes

    public LoginFrame(List<Solicitud> solicitudes) {
        this.solicitudes = solicitudes; // Inicializar solicitudes
        setTitle("Inicio de Sesión");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new GridLayout(4, 2));
        JLabel labelUsuario = new JLabel("Usuario:");
        JTextField campoUsuario = new JTextField();
        JLabel labelContraseña = new JLabel("Contraseña:");
        JPasswordField campoContraseña = new JPasswordField();
        JButton botonEstudiante = new JButton("Iniciar como Estudiante");
        JButton botonSupervisor = new JButton("Iniciar como Supervisor");

        panelPrincipal.add(labelUsuario);
        panelPrincipal.add(campoUsuario);
        panelPrincipal.add(labelContraseña);
        panelPrincipal.add(campoContraseña);
        panelPrincipal.add(new JLabel());
        panelPrincipal.add(botonEstudiante);
        panelPrincipal.add(new JLabel());
        panelPrincipal.add(botonSupervisor);

        add(panelPrincipal);

        // Iniciar sesión como estudiante
        botonEstudiante.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = campoUsuario.getText();
                if (!usuario.isEmpty()) {
                    // Abrir la ventana de gestión para estudiantes
                    new GestionPermisosGUI(usuario, false, solicitudes).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Ingrese un nombre de usuario");
                }
            }
        });

        // Iniciar sesión como supervisor
        botonSupervisor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = campoUsuario.getText();
                String contraseña = new String(campoContraseña.getPassword());

                if (!usuario.isEmpty() && contraseña.equals("1234")) { // Contraseña "fija" para este ejemplo
                    // Abrir la ventana de gestión para supervisores
                    new GestionPermisosGUI(usuario, true, solicitudes).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Credenciales incorrectas");
                }
            }
        });
    }
}

// Método principal
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame(new ArrayList<>()).setVisible(true)); // Mostrar pantalla de
                                                                                              // inicio de sesión
    }
}
