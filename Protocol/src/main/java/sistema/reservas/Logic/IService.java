// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/IService.java
// Si I3 ya publicó IService.java, pega solo el bloque "I2" (imports incluidos).
//
// OJO (fuera de mi alcance, es de I3 en su paso 2): para que Reserva viaje por el socket,
// CategoriaRecurso, Usuario, Funcionario y Administrador también deben ser Serializable
// (hoy no lo son y aún tienen anotaciones JAXB).
// ============================================================
package sistema.reservas.Logic;

import java.util.List;

public interface IService {

    // ---------- I3: 99 Cierre ----------
    void stop();

    // ---------- I1: 101-399 Usuarios, Funcionarios, Categorías ----------
    // Usuarios: devuelve el usuario con password = null; lanza Exception si no coincide
    Usuario login(String username, String password) throws Exception;

    void cambiarClave(String username, String claveActual, String claveNueva) throws Exception;

    // Funcionarios: la clave inicial (= id) la asigna el Backend
    void create(Funcionario funcionario) throws Exception;

    Funcionario read(Funcionario funcionario) throws Exception;      // busca por id

    void update(Funcionario funcionario) throws Exception;

    void delete(Funcionario funcionario) throws Exception;

    List<Funcionario> findAll(Funcionario filtro) throws Exception;

    List<Funcionario> findFuncionariosByNombre(String nombre) throws Exception;

    // Categorías: create devuelve la categoría con el id que generó MySQL
    CategoriaRecurso create(CategoriaRecurso categoria) throws Exception;

    CategoriaRecurso read(CategoriaRecurso categoria) throws Exception;   // busca por id

    void update(CategoriaRecurso categoria) throws Exception;

    void delete(CategoriaRecurso categoria) throws Exception;

    List<CategoriaRecurso> findAll(CategoriaRecurso filtro) throws Exception;

    List<CategoriaRecurso> findCategoriasByDescripcion(String descripcion) throws Exception;

    // ---------- I2: 401-499 Recursos ----------
    void create(Recurso recurso) throws Exception;

    Recurso read(Recurso recurso) throws Exception;            // busca por id; lanza Exception si no existe

    void update(Recurso recurso) throws Exception;

    void delete(Recurso recurso) throws Exception;

    List<Recurso> findAll(Recurso filtro) throws Exception;    // 'filtro' solo desambigua el tipo, como en Personas

    List<Recurso> findRecursosByCategoria(CategoriaRecurso categoria) throws Exception;

    // ---------- I2: 501-599 Reservas ----------
    // create y update devuelven la reserva guardada: el id lo genera MySQL (AUTO_INCREMENT)
    Reserva create(Reserva reserva) throws Exception;

    Reserva update(Reserva reserva) throws Exception;

    void cancelar(Reserva reserva) throws Exception;           // estado CANCELADA, no borra

    List<Reserva> findAll(Reserva filtro) throws Exception;

    List<Reserva> findReservasByFuncionario(Funcionario funcionario) throws Exception;

    ReservaExtraccion extraerConIA(String frase) throws Exception;   // se ejecuta solo en el Backend
}