import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

abstract class Usuario {
    protected String usuario;
    protected String contrasena;
    protected boolean esPremium;

    public Usuario(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.esPremium = false;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public boolean autenticar(String contrasena) {
        return this.contrasena.equals(contrasena);
    }

    abstract void realizarReserva(Scanner scanner, Kayac kayak);

    abstract void confirmarReserva(Scanner scanner, Kayac kayak);
}

class UsuarioGratis extends Usuario {
    public UsuarioGratis(String usuario, String contrasena) {
        super(usuario, contrasena);
    }

    @Override
    void realizarReserva(Scanner scanner, Kayac kayak) {
        kayak.realizarReserva(scanner, this);
    }

    @Override
    void confirmarReserva(Scanner scanner, Kayac kayak) {
        kayak.confirmarReserva(scanner, this);
    }
}

class UsuarioPremium extends Usuario {
    public UsuarioPremium(String usuario, String contrasena) {
        super(usuario, contrasena);
        this.esPremium = true;
    }

    @Override
    void realizarReserva(Scanner scanner, Kayac kayak) {
        kayak.realizarReserva(scanner, this);
    }

    @Override
    void confirmarReserva(Scanner scanner, Kayac kayak) {
        kayak.confirmarReserva(scanner, this);
    }
}

class Reserva {
    private String fechaVuelo;
    private boolean idaYVuelta;
    private int cantidadBoletos;
    private String aerolinea;
    private Usuario usuario;

    public Reserva(String fechaVuelo, boolean idaYVuelta, int cantidadBoletos, String aerolinea, Usuario usuario) {
        this.fechaVuelo = fechaVuelo;
        this.idaYVuelta = idaYVuelta;
        this.cantidadBoletos = cantidadBoletos;
        this.aerolinea = aerolinea;
        this.usuario = usuario;
    }

    public String getFechaVuelo() {
        return fechaVuelo;
    }

    public boolean isIdaYVuelta() {
        return idaYVuelta;
    }

    public int getCantidadBoletos() {
        return cantidadBoletos;
    }

    public String getAerolinea() {
        return aerolinea;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void imprimirConfirmacion() {
        System.out.println("Confirmación de Reserva:");
        System.out.println("Usuario: " + usuario.getUsuario());
        System.out.println("Fecha de Vuelo: " + fechaVuelo);
        System.out.println("Tipo de Vuelo: " + (idaYVuelta ? "Ida y Vuelta" : "Solo Ida"));
        System.out.println("Cantidad de Boletos: " + cantidadBoletos);
        System.out.println("Aerolínea: " + aerolinea);
    }
}

class Kayac {
    private static final String USERS_FILE = "usuarios.txt";
    private static final String RESERVAS_FILE = "reservas.csv";
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Reserva> reservas = new ArrayList<>();

    public Kayac() {
        cargarUsuarios();
        cargarReservas();
    }

    public void registroUsuario(Usuario usuario) {
        usuarios.add(usuario);
        guardarUsuarios();
    }

    public Usuario autenticarUsuario(String usuario, String contrasena) {
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(usuario) && u.autenticar(contrasena)) {
                return u;
            }
        }
        return null;
    }

    public void realizarReserva(Scanner scanner, Usuario usuario) {
        System.out.print("Ingrese la fecha de su vuelo (YYYY-MM-DD): ");
        String fechaVuelo = scanner.nextLine();
        System.out.print("¿Será ida y vuelta? (si/no): ");
        boolean idaYVuelta = "si".equalsIgnoreCase(scanner.nextLine());
        System.out.print("Ingrese la cantidad de boletos: ");
        int cantidadBoletos = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Ingrese la aerolínea: ");
        String aerolinea = scanner.nextLine();

        Reserva reserva = new Reserva(fechaVuelo, idaYVuelta, cantidadBoletos, aerolinea, usuario);
        reservas.add(reserva);
        System.out.println("Reserva realizada exitosamente.");
        guardarReservas();
    }

    public void confirmarReserva(Scanner scanner, Usuario usuario) {
        System.out.println("Reserva confirmada para " + usuario.getUsuario());
    }

    public void guardarUsuarios() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Usuario usuario : usuarios) {
                writer.write(usuario.getUsuario() + "," + usuario.getContrasena() + ","
                        + (usuario.esPremium ? "premium" : "gratis"));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    private void cargarUsuarios() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 3) {
                    Usuario usuario = datos[2].equals("premium") ? new UsuarioPremium(datos[0], datos[1])
                            : new UsuarioGratis(datos[0], datos[1]);
                    usuarios.add(usuario);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private void guardarReservas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESERVAS_FILE))) {
            for (Reserva reserva : reservas) {
                writer.write(
                        reserva.getUsuario().getUsuario() + "," +
                                reserva.getFechaVuelo() + "," +
                                reserva.isIdaYVuelta() + "," +
                                reserva.getCantidadBoletos() + "," +
                                reserva.getAerolinea());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo de reservas: " + e.getMessage());
        }
    }

    private void cargarReservas() {
        File file = new File(RESERVAS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 5) {
                    Usuario usuario = buscarUsuario(datos[0]);
                    if (usuario != null) {
                        Reserva reserva = new Reserva(datos[1], Boolean.parseBoolean(datos[2]),
                                Integer.parseInt(datos[3]), datos[4], usuario);
                        reservas.add(reserva);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de reservas: " + e.getMessage());
        }
    }

    private Usuario buscarUsuario(String nombreUsuario) {
        return usuarios.stream()
                .filter(u -> u.getUsuario().equals(nombreUsuario))
                .findFirst()
                .orElse(null);
    }
}

public class Principal {
    private static Kayac kayak = new Kayac();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\nBienvenido al sistema de reservas Kayac.");
            System.out.println("1. Registrarse");
            System.out.println("2. Ingresar");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    registrarUsuario();
                    break;
                case 2:
                    Usuario usuario = autenticarUsuario();
                    if (usuario != null) {
                        System.out.println("Autenticación exitosa. Bienvenido " + usuario.getUsuario());
                        manejarAccionesUsuario(usuario);
                    }
                    break;
                case 3:
                    salir = true;
                    kayak.guardarUsuarios();
                    System.out.println("Gracias por usar el sistema de reservas Kayac.");
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        }
        scanner.close();
    }

    private static void registrarUsuario() {
        System.out.print("Ingrese un nombre de usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Ingrese una contraseña: ");
        String contrasena = scanner.nextLine();
        System.out.print("¿Desea registrarse como usuario Premium? (si/no): ");
        String respuesta = scanner.nextLine();

        if ("si".equalsIgnoreCase(respuesta)) {
            kayak.registroUsuario(new UsuarioPremium(usuario, contrasena));
            System.out.println("Usuario Premium registrado exitosamente.");
        } else {
            kayak.registroUsuario(new UsuarioGratis(usuario, contrasena));
            System.out.println("Usuario registrado exitosamente como usuario gratis.");
        }
    }

    private static Usuario autenticarUsuario() {
        System.out.print("Ingrese su nombre de usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Ingrese su contraseña: ");
        String contrasena = scanner.nextLine();

        Usuario usuarioAutenticado = kayak.autenticarUsuario(usuario, contrasena);
        if (usuarioAutenticado == null) {
            System.out.println("Autenticación fallida.");
        }
        return usuarioAutenticado;
    }

    private static void manejarAccionesUsuario(Usuario usuario) {
        if (usuario.esPremium) {
            mostrarMenuPremium(usuario);
        } else {
            mostrarMenuGratis(usuario);
        }
    }

    private static void mostrarMenuGratis(Usuario usuario) {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\nMenú de Usuario Gratis");
            System.out.println("1. Realizar reserva");
            System.out.println("2. Confirmar reserva");
            System.out.println("3. Salir");
            System.out.print("Elija una opción: ");
            int eleccion = scanner.nextInt();
            scanner.nextLine();

            switch (eleccion) {
                case 1:
                    usuario.realizarReserva(scanner, kayak);
                    break;
                case 2:
                    usuario.confirmarReserva(scanner, kayak);
                    break;
                case 3:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción no válida");
                    break;
            }
        }
    }

    private static void mostrarMenuPremium(Usuario usuario) {
        boolean continuar = true;
        while (continuar) {
            System.out.println("\nMenú de Usuario Premium");
            System.out.println("1. Realizar reserva");
            System.out.println("2. Confirmar reserva");
            System.out.println("3. Salir");
            System.out.print("Elija una opción: ");
            int eleccion = scanner.nextInt();
            scanner.nextLine();

            switch (eleccion) {
                case 1:
                    usuario.realizarReserva(scanner, kayak);
                    break;
                case 2:
                    usuario.confirmarReserva(scanner, kayak);
                    break;
                case 3:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opción no válida");
                    break;
            }
        }
    }
}
