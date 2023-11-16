import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public void cambiarContrasena(String nuevaContrasena) {
        this.contrasena = nuevaContrasena;
    }

    public void cambiarTipoCliente() {
        this.esPremium = !this.esPremium;
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
    private static final String RESERVAS_FILE = "usuarios.csv";
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
        System.out.println("Modo confirmación:");
        System.out.print("Ingrese el número de tarjeta: ");
        String numeroTarjeta = scanner.nextLine();

        int cuotas = 1;
        if (!usuario.esPremium) {
            System.out.print("Defina la cantidad de cuotas (1 a 24): ");
            cuotas = scanner.nextInt();
            scanner.nextLine();
        }

        String claseVuelo = "Coach";
        if (!usuario.esPremium) {
            System.out.print("Defina la clase para el vuelo (Coach/Primera Clase): ");
            claseVuelo = scanner.nextLine();
        }

        int numeroAsiento = 0;
        int cantidadMaletas = 0;
        if (usuario.esPremium) {
            System.out.print("Seleccione el número de asiento: ");
            numeroAsiento = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Defina la cantidad de maletas: ");
            cantidadMaletas = scanner.nextInt();
            scanner.nextLine();
        }

        imprimirItinerario(usuario, numeroTarjeta, cuotas, claseVuelo, numeroAsiento, cantidadMaletas);
    }

    private void imprimirItinerario(Usuario usuario, String numeroTarjeta, int cuotas, String claseVuelo,
            int numeroAsiento, int cantidadMaletas) {
        System.out.println("\nItinerario de Reserva:");
        System.out.println("Usuario: " + usuario.getUsuario());
        System.out.println("Número de Tarjeta: " + numeroTarjeta);
        System.out.println("Cuotas: " + cuotas);
        System.out.println("Clase de Vuelo: " + claseVuelo);
        if (usuario.esPremium) {
            System.out.println("Número de Asiento: " + numeroAsiento);
            System.out.println("Cantidad de Maletas: " + cantidadMaletas);
        }
    }

    public void guardarUsuarios() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Usuario usuario : usuarios) {
                writer.write(usuario.getUsuario() + "," + usuario.getContrasena() + ","
                        + (usuario.esPremium ? "premium" : "gratis") + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar usuarios: " + e.getMessage());
        }
    }

    private void cargarUsuarios() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    Usuario usuario = "premium".equals(parts[2])
                            ? new UsuarioPremium(parts[0], parts[1])
                            : new UsuarioGratis(parts[0], parts[1]);
                    usuarios.add(usuario);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void guardarReservas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESERVAS_FILE))) {
            for (Reserva reserva : reservas) {
                writer.write(reserva.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar reservas: " + e.getMessage());
        }
    }

    private void cargarReservas() {
        File file = new File(RESERVAS_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {

            }
        } catch (IOException e) {
            System.out.println("Error al cargar reservas: " + e.getMessage());
        }
    }
}

public class Principal {
    private static Kayac kayak = new Kayac();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            System.out.println("Bienvenido a Kayac");
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar sesión");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    registrarUsuario();
                    break;
                case 2:
                    iniciarSesion();
                    break;
                case 3:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    private static void registrarUsuario() {
        System.out.print("Ingrese su nombre de usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Ingrese su contraseña: ");
        String contrasena = scanner.nextLine();
        System.out.print("¿Desea ser usuario premium? (si/no): ");
        boolean esPremium = "si".equalsIgnoreCase(scanner.nextLine());

        Usuario nuevoUsuario = esPremium
                ? new UsuarioPremium(usuario, contrasena)
                : new UsuarioGratis(usuario, contrasena);
        kayak.registroUsuario(nuevoUsuario);
        System.out.println("Usuario registrado exitosamente.");
    }

    private static void iniciarSesion() {
        System.out.print("Ingrese su nombre de usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Ingrese su contraseña: ");
        String contrasena = scanner.nextLine();

        Usuario usuarioAutenticado = kayak.autenticarUsuario(usuario, contrasena);
        if (usuarioAutenticado != null) {
            System.out.println("Inicio de sesión exitoso.");
            menuUsuario(usuarioAutenticado);
        } else {
            System.out.println("Usuario o contraseña incorrectos.");
        }
    }

    private static void menuUsuario(Usuario usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("Bienvenido, " + usuario.getUsuario());
            System.out.println("1. Realizar reserva");
            System.out.println("2. Confirmar reserva");
            System.out.println("3. Modo perfil");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    usuario.realizarReserva(scanner, kayak);
                    break;
                case 2:
                    usuario.confirmarReserva(scanner, kayak);
                    break;
                case 3:
                    modoPerfil(usuario);
                    break;
                case 4:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    private static void modoPerfil(Usuario usuario) {
        System.out.println("Modo perfil");
        System.out.println("1. Modificar tipo de cliente");
        System.out.println("2. Aplicar cupón de descuento");
        System.out.println("3. Cambiar contraseña");
        System.out.print("Seleccione una opción: ");
        int opcion = scanner.nextInt();
        scanner.nextLine();

        switch (opcion) {
            case 1:
                usuario.cambiarTipoCliente();
                System.out.println("Tipo de cliente modificado.");
                break;
            case 2:
                if (!usuario.esPremium) {
                    System.out.println("Aplicando cupón de 10% de descuento...");

                } else {
                    System.out.println("Los usuarios premium no necesitan cupones de descuento.");
                }
                break;
            case 3:
                System.out.print("Ingrese su nueva contraseña: ");
                String nuevaContrasena = scanner.nextLine();
                usuario.cambiarContrasena(nuevaContrasena);
                System.out.println("Contraseña cambiada exitosamente.");
                break;
            default:
                System.out.println("Opción no válida.");
        }
    }
}
