package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.entities.Correo;
import com.example.entities.Departamento;
import com.example.entities.Empleado;
import com.example.entities.Telefono;
import com.example.model.Genero;
import com.example.services.CorreoService;
import com.example.services.DepartamentoService;
import com.example.services.EmpleadoService;
import com.example.services.TelefonoService;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class CrudEmpleadosSpringMvcApplication implements CommandLineRunner {
	
	private final EmpleadoService empleadoService;
	private final DepartamentoService departamentoService;
	
	// private final CorreoService correoService;
	// private final TelefonoService telefonoService;

	public static void main(String[] args) {
		SpringApplication.run(CrudEmpleadosSpringMvcApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		// Crear registros de ejemplo en la base de datos, lo 
		// cual nos permite comprobar que la aplicación funciona correctamente, 
		// concretamente la capa de servicio y la capa de persistencia.
		
		/* Crear Departamentos */
		
		Departamento departamento1 = Departamento.builder()
				.nombre("RRHH")
				.build();
		
		Departamento departamento2 = Departamento.builder()
				.nombre("IT")
				.build();
		
		Departamento departamento3 = Departamento.builder()
				.nombre("Marketing")
				.build();
		
		Departamento departamento4 = Departamento.builder()
				.nombre("Ventas")
				.build();
		
		Departamento departamento5 = Departamento.builder()
				.nombre("Informatica")
				.build();
		
		// Persistir los departamentos en la base de datos
		
		departamentoService.saveDepartamento(departamento1);
		departamentoService.saveDepartamento(departamento2);
		departamentoService.saveDepartamento(departamento3);
		departamentoService.saveDepartamento(departamento4);
		departamentoService.saveDepartamento(departamento5);
		
		// Crear Empleados
		
		Empleado empleado1 = Empleado.builder()
				.nombre("Juan")
				.primerApellido("Pérez")
				.segundoApellido("García")
				.genero(Genero.HOMBRE)
				.fechaAlta(LocalDate.of(2020, 1, 15))
				.departamento(departamento1)
				.salario(new BigDecimal(3500.50))
				.telefonos(Set.of(Telefono.builder().numero("123456789").build(),
						Telefono.builder().numero("987654321").build()))
				.emails(Set.of(Correo.builder().email("emp1@g.com").build(),
						Correo.builder().email("emp1@gg.com").build()))
				.build();
		
		Empleado empleado2 = Empleado.builder()
				.nombre("Maria")
				.primerApellido("López")
				.segundoApellido("Martínez")
				.genero(Genero.MUJER)
				.fechaAlta(LocalDate.of(2021, 3, 10))
				.departamento(departamento2)
				.salario(new BigDecimal(4000.00))
				.telefonos(Set.of(Telefono.builder().numero("555555555").build()))
				.emails(Set.of(Correo.builder().email("emp2@g.com").build()))
				.build();
		
		Empleado empleado3 = Empleado.builder()
				.nombre("Pedro")
				.primerApellido("Gómez")
				.segundoApellido("Sánchez")
				.genero(Genero.HOMBRE)
				.fechaAlta(LocalDate.of(2022, 5, 20))
				.departamento(departamento3)
				.salario(new BigDecimal(3000.75))
				.telefonos(Set.of(Telefono.builder().numero("111111111").build(),
						Telefono.builder().numero("222222222").build()))
				.emails(Set.of(Correo.builder().email("emp3@g.com").build(),
						Correo.builder().email("emp3@outlook.com").build()))
				.build();
		
		// Antes de persitir el empleado, para que en las tablas de correos y telefonos
		// el campo empleado_id no sea nulo, hay que establecer la relación entre 
		// el empleado y sus correos y teléfonos.
		
		empleado1.getTelefonos().forEach(telefono -> telefono.setEmpleado(empleado1));
		empleado1.getEmails().forEach(correo -> correo.setEmpleado(empleado1));	
		
		empleado2.getTelefonos().forEach(telefono -> telefono.setEmpleado(empleado2));
		empleado2.getEmails().forEach(correo -> correo.setEmpleado(empleado2));	
		
		empleado3.getTelefonos().forEach(telefono -> telefono.setEmpleado(empleado3));
		empleado3.getEmails().forEach(correo -> correo.setEmpleado(empleado3));	
		
		empleadoService.saveEmpleado(empleado1);
		empleadoService.saveEmpleado(empleado2);
		empleadoService.saveEmpleado(empleado3);
		

	}

}
