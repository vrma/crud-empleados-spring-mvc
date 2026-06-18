package com.example.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.entities.Empleado;
import com.example.services.DepartamentoService;
import com.example.services.EmpleadoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {
	
	private final EmpleadoService empleadoService;
	private final DepartamentoService departamentoService;

	@GetMapping("/listar")
	public String listarEmpleados(Model model) {
		
		model.addAttribute("empleados",
				empleadoService.getAllEmpleados()); 
		// Agrega la lista de empleados al modelo
		
		return "listadoEmpleados"; // Retorna el nombre de la vista para listar empleados
	}
	
	// Método para mostrar el formulario de creación de empleado
	@GetMapping("/alta")
	public String mostrarFormularioAlta(Model model) {
		
		// Se necesitan los departamentos desde la capa de servicios
		model.addAttribute("departamentos", 
				departamentoService.getAllDepartamentos());
		
		// Se necesita enviar un objeto Empleado vacio, para que se vinculen
		// sus propiedades con cada control (elemento input, select, etc) 
		// del formulario
		model.addAttribute("empleado",
				new Empleado());
		
		return "formularioAltaModificacion";
	}
}
