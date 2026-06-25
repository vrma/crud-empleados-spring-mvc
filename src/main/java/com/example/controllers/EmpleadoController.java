package com.example.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.entities.Correo;
import com.example.entities.Empleado;
import com.example.entities.Telefono;
import com.example.services.CorreoService;
import com.example.services.DepartamentoService;
import com.example.services.EmpleadoService;
import com.example.services.TelefonoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

	private static final Logger LOG = Logger.getLogger("EmpleadoController");

	private final EmpleadoService empleadoService;
	private final DepartamentoService departamentoService;
	private final CorreoService correoService;
	private final TelefonoService telefonoService;

	@GetMapping("/listar")
	public String listarEmpleados(Model model) {

		model.addAttribute("empleados",
				empleadoService.getAllEmpleados());
		// Agrega la lista de empleados al modelo

		return "listadoEmpleados"; // Retorna el nombre de la vista para listar empleados
	}

	// Método para mostrar el formulario de creación de empleado
	@GetMapping("/alta")
	public String mostrarFormularioAlta(Model model,
			@ModelAttribute Empleado empleado) {

		// Se necesitan los departamentos desde la capa de servicios
		model.addAttribute("departamentos",
				departamentoService.getAllDepartamentos());

		// Se necesita enviar un objeto Empleado vacio, para que se vinculen
		// sus propiedades con cada control (elemento input, select, etc)
		// del formulario

		// El codigo siguiente se comenta porque el objeto se pasa como atributo
		// al modelo a traves de la anotacion @ModelAttribute que se recibe como un
		// parametro del metodo
		// model.addAttribute("empleado", new Empleado());

		return "formularioAltaModificacion";
	}

	// Método para recibir los datos del formulario de creación de empleado
	@PostMapping("/persistir")
	public String procesarFormularioAltaModificacion(
			@Valid @ModelAttribute Empleado empleado,
			BindingResult result,
			@RequestParam String numerosTelefono,
			@RequestParam String direccionesCorreo,
			Model model,
			@RequestParam(name = "file", required = false) MultipartFile file) {

		// Comprobar si hay errores en la informacion procedente del formulario
		if (result.hasErrors()) {

			model.addAttribute("departamentos",
					departamentoService.getAllDepartamentos());

			return "formularioAltaModificacion";
		}

		// Preguntar si me han enviado foto para el empleado, y si es asi,
		// guardar el nombre de la foto en la propiedad, atributo, o variable miembro de
		// la clase, foto,
		// y guardar el contenido de la foto como un archivo en el sistema de archivos
		// (files system) del
		// servidor

		if (file != null && !file.isEmpty()) {

			Path rutaRelativa = Paths.get("src/main/resources/static/imagenes");
			String rutaAbsoluta = rutaRelativa.toFile().getAbsolutePath();
			Path rutaCompleta = Paths.get(rutaAbsoluta + "/" + file.getOriginalFilename());

			try {
				byte[] bytesFotoRecibida = file.getBytes();
				Files.write(rutaCompleta, bytesFotoRecibida);
				empleado.setFoto(file.getOriginalFilename());
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		LOG.info("Objeto empleado recibido ");
		LOG.info(empleado.toString());
		LOG.info("Numeros de telefono recibidos: " + numerosTelefono);
		LOG.info("Direcciones de correo recibidas: " + direccionesCorreo);

		// Hay que procesar los datos de los telefonos y correos, que vienen en un
		// String
		// separados por comas, y convertirlos en listas de objetos Telefono y Correo,
		// para luego agregarlos al objeto Empleado antes de persistirlo en la BD.

		// Set<Telefono> telefonos = new HashSet<Telefono>();

		if (!numerosTelefono.isEmpty() && !numerosTelefono.isBlank()) {

			String[] arrayNumerosTelefono = numerosTelefono.split(";");
			List<String> listadoNumeros = Arrays.asList(arrayNumerosTelefono);

			listadoNumeros.forEach(numero -> {
				empleado.getTelefonos().add(Telefono.builder().numero(numero)
						.empleado(empleado).build());
			});

			// empleado.setTelefonos(telefonos);
		}

		if (!direccionesCorreo.isEmpty() && !direccionesCorreo.isBlank()) {

			String[] arrayDirCorreos = direccionesCorreo.split(";");
			List<String> listadoCorreos = Arrays.asList(arrayDirCorreos);

			listadoCorreos.forEach(dirCorr -> {
				empleado.getEmails().add(Correo.builder()
						.email(dirCorr).empleado(empleado).build());
			});
		}
		/**
		 * Antes de persistir el empleado, hay que eliminar los telefonos y los correos
		 * que tenga
		 */
		if (empleado.getId() != 0) {
			if (telefonoService.existsByEmpleado(empleado))
				telefonoService.deleteByEmpleado(empleado);

			if (correoService.existsByEmpleado(empleado))
				correoService.deleteByEmpleado(empleado);
		}

		// Se recibe un objeto Empleado con los datos del formulario
		// Se envía a la capa de servicios para que lo guarde en la BD
		empleadoService.saveEmpleado(empleado);

		return "redirect:/empleados/listar"; // Redirige a la lista de empleados
	}

	// Metodo que muestra los detalles de un empleado cuyo id se recibe como
	// parametro
	@GetMapping("/details/{id}")
	public String mostrarDetalles(Model model,
			@PathVariable(name = "id", required = true) int empleado_id) {

		// Recuperar el empleado cuyo id se recibe como parametro
		model.addAttribute("empleado",
				empleadoService.getEmpleadoById(empleado_id));

		return "details";
	}

	// Metodo para actualizar un empleado.
	// Muestra en el formulario de Alta/Modificacion la informacion
	// del empleado que se va a actualizar
	@GetMapping("/update/{id}")
	public String updateEmpleado(Model model,
			@PathVariable(name = "id", required = true) int idEmpleado) {

		Empleado empleado = empleadoService.getEmpleadoById(idEmpleado);

		model.addAttribute("empleado", empleado);

		model.addAttribute("departamentos",
				departamentoService.getAllDepartamentos());

		// Procesando los telefonos y los correos porque no se debe hacer
		// en la vista, es decir, en el formularioAltaModificacion.html
		Set<Telefono> telefonos = empleado.getTelefonos();

		if (telefonos.size() > 0) {

			String numerosTelefono = telefonos.stream()
					.map(telefono -> telefono.getNumero())
					.collect(Collectors.joining(";"));

			model.addAttribute("numerosTelefono", numerosTelefono);
		}

		Set<Correo> correos = empleado.getEmails();

		if (correos.size() > 0) {

			String direccionesCorreos = correos.stream()
					.map(correo -> correo.getEmail())
					.collect(Collectors.joining(";"));

			model.addAttribute("direccionesCorreos", direccionesCorreos);
		}

		return "formularioAltaModificacion";
	}

	// Metodo para eliminar un empleado, con sus correos y sus telefonos correspondientes
	// Hay que eliminar tambien el archivo de foto del empleado, en caso de tenerla
	@GetMapping("/delete/{idEmpleado}")
	public String deleteEmpleado(Model model, @PathVariable int idEmpleado) {

		// Comprobar si el empleado tiene foto para eliminarla

		Empleado empleadoEliminar = empleadoService.getEmpleadoById(idEmpleado);

		if (empleadoEliminar.getFoto() != null) {

			// Ruta relativa del fichero que se va a eliminar
			Path rutaRelativa = Paths.get("src/main/resources/static/imagenes/"
			                                           + empleadoEliminar.getFoto());

			if (Files.exists(rutaRelativa)) {

				try {
					Files.delete(rutaRelativa);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		// Eliminar el empleado

		empleadoService.deleteEmpleado(empleadoEliminar);

		return "redirect:/empleados/listar";
	}
}
